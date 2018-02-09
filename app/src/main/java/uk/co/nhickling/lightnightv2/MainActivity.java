package uk.co.nhickling.lightnightv2;

import android.Manifest;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class MainActivity extends AppCompatActivity {



    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String TAG = "com.hackoldham.ufb";
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    Handler serverReciever;

    private void UpdateUI(Message msg)
    {
        Bundle dataUpdate = msg.getData();
        double result = dataUpdate.getDouble("Average");
        double dbLoad = dataUpdate.getDouble("Load");



        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        int asInt = (int)result;
        if(asInt > pb.getMax())
            pb.setMax(asInt);
        pb.setProgress(asInt);

        ProgressBar pbLoad = (ProgressBar) findViewById(R.id.pbServiceLoad);
        int loadInt = (int)(dbLoad * 100);
        pbLoad.setProgress(loadInt);

        TextView tBox2 = (TextView) findViewById(R.id.textView5);
        tBox2.setText(String.valueOf((int)result) + "Hz");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "OnCreateCalled");
        LightNightV2.Load();

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET},
                REQUEST_RECORD_AUDIO_PERMISSION);

        AttachSeekbarListener();
        serverReciever = new Handler() {
            @Override public void handleMessage(Message msg) {
                UpdateUI(msg);
                Bundle dataUpdate = msg.getData();
                byte byData[] = dataUpdate.getByteArray("Data");
                boolean bTransmitting = dataUpdate.getBoolean("Transmitting");

                Switch sIcon = (Switch) findViewById(R.id.switch1);
                sIcon.setChecked(bTransmitting);
                TextView tBox = (TextView) findViewById(R.id.txt_Result);
                String strRes = "";

                GraphView graph = (GraphView) findViewById(R.id.grphLvl);

                graph.getViewport().setYAxisBoundsManual(true);
                graph.getViewport().setMaxY(512);
                graph.removeAllSeries();
                DataPoint dp[] = new DataPoint[byData.length];
                for(int i = 0; i < byData.length; ++i)
                {
                    strRes += byData[i] + " ";
                    dp[i] = new DataPoint(i, byData[i] < 0 ? byData[i] + 255 : byData[i]);
                }
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp);

                graph.addSeries(series);
                tBox.setText(strRes);
            }
        };


    }

    //Listeners
    public void AttachSeekbarListener()
    {
        findViewById(R.id.btn_Stop).setEnabled(false);
        ((SeekBar)(findViewById(R.id.bar_Sens))).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                LightNightV2.SenseLimit = progress;
                ((TextView)findViewById(R.id.textView2)).setText("CurrentLimit: " + String.valueOf(progress));
                Log.i(TAG, "New Sens Value: " + String.valueOf(progress));
            }
        });
    }

    //permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    //Hardware buttons
    @Override public void onBackPressed() {
        btnClick_Stop(null);
        finish();
    }

    //UIHandling
    public void btnClick_Start(View view)
    {
        Log.i(TAG, "btnClick_Start 1");
        LightNightV2.runService = true;
        LightNightV2.soundServer = new Intent(this, uk.co.nhickling.lightnightv2.SoundService.class);
        LightNightV2.soundServer.putExtra("messageReturn", new Messenger(serverReciever));


        startService(LightNightV2.soundServer);
        Log.i(TAG, "btnClick_Start 2");
        findViewById(R.id.btn_Start).setEnabled(false);
        findViewById(R.id.btn_Stop).setEnabled(true);
    }

    public void btnClick_Stop(View view) {
        LightNightV2.runService = false;
        if (LightNightV2.soundServer == null) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        else {
            stopService(LightNightV2.soundServer);
            findViewById(R.id.btn_Stop).setEnabled(false);
            findViewById(R.id.btn_Start).setEnabled(true);
        }
    }
    public void btnClick_Settings(View v)
    {
        Intent in = new Intent(this, Settings.class);
        startActivity(in);
    }
    public void btnClick_Flash(View v)
    {
        LightNightV2.flash = true;
    }
}
