/**
 * Created by Chrysalis on 15/01/2018.
 */

package uk.co.nhickling.lightnightv2;

import android.content.Intent;
import android.app.Application;
import android.content.Context;

import android.content.SharedPreferences;



public class LightNightV2 extends Application{

    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    public static void Save()
    {
        SharedPreferences sp = getContext().getSharedPreferences("OlLighNight", 0);
        SharedPreferences.Editor ed = sp.edit();
        ed.putFloat("SenseLim", (float)SenseLimit);
        ed.putFloat("bass_threshold", (float)bass_threshold);
        ed.putFloat("treble_limit", (float)treble_limit);
        ed.putInt("envelope_count", envelope_count);
        ed.putInt("envelope_IncrementFactor", envelope_IncrementFactor);
        ed.putInt("envelope_Deccrement", envelope_Deccrement);
        ed.commit();
    }

    public static void Load()
    {
        SharedPreferences sp = getContext().getSharedPreferences("OlLighNight", 0);
        SenseLimit = sp.getFloat("SenseLim", 20);
        bass_threshold = sp.getFloat("bass_threshold", 300);
        treble_limit = sp.getFloat("treble_limit", 2000);
        envelope_count = 93*3;//sp.getInt("envelope_count", 93*3);
        envelope_IncrementFactor = sp.getInt("envelope_IncrementFactor", 4);
        envelope_Deccrement = sp.getInt("envelope_Deccrement", 20);

    }

    public static double SenseLimit = 20;

    public static double bass_threshold = 300;
    public static double treble_limit = 2000; //highest frequency
	
	
	public static int envelope_count = 93*3;

	public static int envelope_IncrementFactor = 4;
    public static int envelope_Deccrement = 20;

    //globals
    public static Intent soundServer=null;
    public static boolean runService = false;

    public static boolean flash = false;
}
