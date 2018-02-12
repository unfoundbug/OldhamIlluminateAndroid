package uk.co.nhickling.lightnightv2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

public class Settings extends AppCompatActivity {

    Spinner m_sBassPass;
    Spinner m_sHighLimit;
    SeekBar m_bIncrement;
    SeekBar m_bDecrement;
    SeekBar m_bBass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_sBassPass = (Spinner) findViewById(R.id.spLPFilter);
        m_sHighLimit = (Spinner) findViewById(R.id.spHFL);
        m_bIncrement = findViewById(R.id.sbIncrement);
        m_bDecrement = findViewById(R.id.sbDecrement);
        m_bBass = findViewById(R.id.sbBassThresh);
        {
            String compareValue = String.valueOf((int) LightNightV2.bass_threshold);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spLPFilter, android.R.layout.simple_spinner_dropdown_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            m_sBassPass.setAdapter(adapter);
            if (compareValue != null) {
                int spinnerPosition = adapter.getPosition(compareValue);
                m_sBassPass.setSelection(spinnerPosition);
            }
        }
        {
            String compareValue = String.valueOf((int) LightNightV2.treble_limit);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spHFL, android.R.layout.simple_spinner_dropdown_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            m_sHighLimit.setAdapter(adapter);
            if (compareValue != null) {
                int spinnerPosition = adapter.getPosition(compareValue);
                m_sHighLimit.setSelection(spinnerPosition);
            }
        }
        m_bIncrement.setProgress(LightNightV2.envelope_IncrementFactor);

        m_bDecrement.setProgress(LightNightV2.envelope_Deccrement / 10);

        m_bBass.setProgress((int)LightNightV2.bass_trim);
    }


    public void btnClick_Cancel(View v)
    {
        finish();
    }
    public void btnClick_Save(View v)
    {
        LightNightV2.treble_limit = Double.parseDouble((String)m_sHighLimit.getSelectedItem());
        LightNightV2.bass_threshold = Double.parseDouble((String)m_sBassPass.getSelectedItem());
        LightNightV2.envelope_IncrementFactor = m_bIncrement.getProgress();
        LightNightV2.envelope_Deccrement = m_bDecrement.getProgress() * 10;
        LightNightV2.bass_trim = m_bBass.getProgress();
        LightNightV2.Save();

        btnClick_Cancel(null);
    }
}
