package uk.co.nhickling.lightnightv2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by HickliN on 06/02/2018.
 */

public class ServiceRestarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(SoundService.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");

        context.startService(LightNightV2.soundServer);
    }
}