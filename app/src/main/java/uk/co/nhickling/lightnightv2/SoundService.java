package uk.co.nhickling.lightnightv2;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.paramsen.noise.Noise;
import com.paramsen.noise.NoiseOptimized;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import static android.content.ContentValues.TAG;

/**
 * Created by Chrysalis on 15/01/2018.
 */

public class SoundService extends Service{

    static DatagramSocket dSocket;
    static Socket scSendSocket;
    private class AudioThread implements Runnable{
        private MediaRecorder mRecorder = null;
        private int iMinReadSize = 0;
        private AudioRecord micRecorder = null;
        private Messenger hostMessage;
        private boolean bLastSend = false;
        private int sampleRate = 48000;
        public boolean recording = false;
        float[] buffer;
        Intent hostIntent;
        public boolean isRunning = false;
        AudioThread(Intent intent)
        {
            hostIntent =intent;
            hostMessage = (Messenger) intent.getParcelableExtra("messageReturn");
            scSendSocket= new Socket();

            try {
                scSendSocket.setSoTimeout(75);
            } catch(java.net.SocketException ex)
            {

            }
        }

        public void run() {

            Log.i("com.hackoldham.ufb", "AudioServerService_Run: Thread is now running");
            recording = isRunning = true;
            iMinReadSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT);
            Log.i(TAG, "AudioServerService_Run: Min size found as " + String.valueOf(iMinReadSize));
            micRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT,
                    iMinReadSize);
            Log.i(TAG, "AudioServerService_Run: Recorder made");

            int iSamplesToRead = 8192;
            buffer = new float[iSamplesToRead];
            float[] buffer2 = new float[iSamplesToRead];


            micRecorder.startRecording();
            NoiseOptimized noise = Noise.real().optimized()
                    .init(iSamplesToRead, true);


            double bucketRatio = sampleRate / (iSamplesToRead);
            double dMaxBassBucket = LightNightV2.bass_threshold / bucketRatio;

            int envelopes[] = new int[LightNightV2.envelope_count+1];
            int FirstSampleRun=0;
            micRecorder.read(buffer, 0, iSamplesToRead , AudioRecord.READ_BLOCKING);
            while(isRunning)
            {
                ByteArrayOutputStream bStream = new ByteArrayOutputStream(LightNightV2.envelope_count + 2);
                int iMaxFreq = 0;
                int iBytesRead = 0;
                boolean bFirstRead = true;
                double dbLoad = 0;
                try {
                    Thread.sleep(50);
                }catch(InterruptedException ex)
                {}
                iBytesRead = micRecorder.read(buffer2, 0, iSamplesToRead, AudioRecord.READ_NON_BLOCKING); // record data from mic into buffer

                System.arraycopy(buffer, iBytesRead, buffer, 0, iSamplesToRead - iBytesRead);
                System.arraycopy(buffer2, 0, buffer, iSamplesToRead - iBytesRead, iBytesRead);

                for(int i = 0; i < envelopes.length; ++i)
                    if(envelopes[i]>0)
                        envelopes[i]-= Math.min(envelopes[i], LightNightV2.envelope_Deccrement);
                double dSens = LightNightV2.SenseLimit;
                //Log.i(TAG, "AudioServerService_Run: buffer built current Sens: " + String.valueOf(dSens));

                float[] ftBuckets = noise.fft(buffer);
                float[] ftReal = new float[ftBuckets.length / 2];
                for(int i = 0; i < ftBuckets.length / 2; i+=2)
                    ftReal[i] = ftBuckets[i*2];

                String strComSep = "";
                double bBassLevel=0, dBassFreqRatio=0;
                for(int i = 0; i < (int)dMaxBassBucket; ++i)
                {
                    if(ftReal[i] > bBassLevel) {
                        bBassLevel = ftReal[i];
                        dBassFreqRatio = i;
                    }
                    if(ftReal[iMaxFreq] < ftReal[i])
                        iMaxFreq = i;
                }
                if(bBassLevel > LightNightV2.bass_trim)
                bStream.write((int) Math.sqrt(bBassLevel));
                else
                    bStream.write((int)0);

                int iMaxBucket =(int) Math.min(ftReal.length, LightNightV2.treble_limit/ bucketRatio) - 1;
                int iArrayOffset = (int) dMaxBassBucket;
                int envelopDivis = (iMaxBucket - iArrayOffset) / LightNightV2.envelope_count;
                float bucketenv = iMaxBucket /  (float)envelopes.length;
                for(int i = iArrayOffset; i <iMaxBucket; ++i)
                {
                    float fSum = ftReal[i];
                    if(fSum > LightNightV2.SenseLimit)
                    {
                        int iBucketSample = i - iArrayOffset;
                        float fBucketSegment = (float)iBucketSample / bucketenv;
                        int iBuck = (int) fBucketSegment;
                        fBucketSegment -= iBuck;
                        fSum = (float) Math.sqrt(fSum);
                        float iBaseIncrement = (float)(LightNightV2.envelope_IncrementFactor *fSum);

                        envelopes[iBuck] = (int)Math.min(envelopes[iBuck] + (iBaseIncrement * fBucketSegment), 255);
                        envelopes[iBuck+1] = (int)Math.min(envelopes[iBuck+1] + (iBaseIncrement * (1.0-fBucketSegment)), 255);
                    }

                    if(ftReal[iMaxFreq] < ftReal[i])
                        iMaxFreq = i;
                }
                for(int i = 0; i < envelopes.length-1; ++i)
                    bStream.write(envelopes[i]);
                byte bSet[] = bStream.toByteArray();
                if(LightNightV2.flash)
                {
                    for(int i = 0; i < bSet.length; i++)
                        bSet[i] = (byte)0xFF;
                    LightNightV2.flash = false;
                }
                Bundle bAssemble = new Bundle();

                double dFreq = iMaxFreq * bucketRatio;
                bAssemble.putDouble("Average", dFreq);
                bAssemble.putDouble("Load", (double)iBytesRead / (double)iSamplesToRead);
                bAssemble.putBoolean("Transmitting", bLastSend);
                bAssemble.putByteArray("Data", bSet);
                Message response = new Message();
                response.setData(bAssemble);
                try {
                    hostMessage.send(response);
                } catch(RemoteException e) {

                }
                bLastSend = false;
                try {
                    byte byHead[] = new byte[3 + bSet.length];
                    byte bSum = 0;
                    byHead[0] = (byte)(bSet.length >> 8);
                    byHead[1] = (byte)(bSet.length);
                    for(int i = 0; i < bSet.length; ++i) {
                        bSum += bSet[i];
                        byHead[i+2] = bSet[i];
                    }
                    byHead[2+bSet.length] = bSum;

                    DatagramPacket dp = new DatagramPacket(byHead, byHead.length, InetAddress.getByName("192.168.4.1"), 8080);
                    try {
                        if(dSocket == null)
                            dSocket = new DatagramSocket();
                        dSocket.send(dp);
                    }
                    catch(Exception ex) {
                        dSocket = null;
                    }
                    bLastSend = true;
                } catch (java.io.IOException e)
                {
                    Log.i(TAG, "Exception: " + e.toString());

                    scSendSocket= new Socket();
                    try {
                        scSendSocket.setSoTimeout(75);
                    } catch(java.net.SocketException ex)
                    {

                    }

                }finally {
                }
            }
            micRecorder.stop();
            recording = false;
        }
    }

    AudioThread recHandler = null;

    @Override
    public void onCreate() {


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: Starting");
        if(intent != null) {
            recHandler = new AudioThread(intent);
            new Thread(recHandler).start();
            Log.i(TAG, "onStartCommand: Thread running");
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "service ending");
        recHandler.isRunning = false;
        while(recHandler.recording);
        recHandler = null;

        if(LightNightV2.runService) {
            Log.i(TAG, "service restarting ");
            Intent broadcastRestart = new Intent("uk.co.nhickling.lightnightv2.RestartService");
            sendBroadcast(broadcastRestart);
        }

    }

    public SoundService()
    {

    }
}
