package kriskires.artolela;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    Switch Classic;
    Switch Audio;
    Switch FullAudio;
    Switch NoHelp;
    Switch FileName;

    boolean classic = true;
    boolean audio = false;
    boolean fullaudio = false;
    boolean nohelp = false;
    boolean filename = false;


    public static final String APP_PREFERENCES = "settings";
    public static final String APP_PREFERENCES_BOOLEAN_CLASSIC = "classic";
    public static final String APP_PREFERENCES_BOOLEAN_AUDIO = "audio";
    public static final String APP_PREFERENCES_BOOLEAN_FULL_AUDIO = "fullaudio";
    public static final String APP_PREFERENCES_BOOLEAN_NOHELP = "nohelp";
    public static final String APP_PREFERENCES_BOOLEAN_FILENAME = "filename";
    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        classic = mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_CLASSIC, true);
        audio = mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_AUDIO, false);
        fullaudio = mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_FULL_AUDIO, false);
        nohelp = mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_NOHELP, false);
        filename = mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_FILENAME, false);

        Classic = (Switch) findViewById(R.id.switchClassic);
        Audio = (Switch) findViewById(R.id.switchAudio);
        FullAudio = (Switch) findViewById(R.id.switchFullAudio);
        NoHelp = (Switch) findViewById(R.id.switchNoHelp);
        FileName = (Switch) findViewById(R.id.switchFileName);

        Classic.setChecked(classic);
        Audio.setChecked(audio);
        FullAudio.setChecked(fullaudio);
        NoHelp.setChecked(nohelp);
        FileName.setChecked(filename);

        if(Classic.isChecked() == true){
            Audio.setChecked(false);
            FullAudio.setChecked(false);
            NoHelp.setChecked(false);
        } else
        if(Audio.isChecked() == true){
            Classic.setChecked(false);
            FullAudio.setChecked(false);
            NoHelp.setChecked(false);
        } else
        if(FullAudio.isChecked() == true){
            Classic.setChecked(false);
            Audio.setChecked(false);
            NoHelp.setChecked(false);
        } else
        if(NoHelp.isChecked() == true){
            Classic.setChecked(false);
            Audio.setChecked(false);
            FullAudio.setChecked(false);
        } else {
            Classic.setChecked(true);
            Audio.setChecked(false);
            FullAudio.setChecked(false);
            NoHelp.setChecked(false);
        }

        Classic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Classic.isChecked() == true){
                    Audio.setChecked(false);
                    FullAudio.setChecked(false);
                    NoHelp.setChecked(false);
                } else
                    Classic.setChecked(true);
            }
        });
        Audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Audio.isChecked() == true){
                    Classic.setChecked(false);
                    FullAudio.setChecked(false);
                    NoHelp.setChecked(false);
                } else
                    Classic.setChecked(true);
            }
        });
        FullAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FullAudio.isChecked() == true){
                    Classic.setChecked(false);
                    Audio.setChecked(false);
                    NoHelp.setChecked(false);
                } else
                    Classic.setChecked(true);
            }
        });
        NoHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NoHelp.isChecked() == true){
                    Classic.setChecked(false);
                    Audio.setChecked(false);
                    FullAudio.setChecked(false);
                } else
                    Classic.setChecked(true);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        classic = Classic.isChecked();
        audio = Audio.isChecked();
        fullaudio = FullAudio.isChecked();
        nohelp = NoHelp.isChecked();
        filename = FileName.isChecked();

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(APP_PREFERENCES_BOOLEAN_CLASSIC, classic);
        editor.putBoolean(APP_PREFERENCES_BOOLEAN_AUDIO, audio);
        editor.putBoolean(APP_PREFERENCES_BOOLEAN_FULL_AUDIO, fullaudio);
        editor.putBoolean(APP_PREFERENCES_BOOLEAN_NOHELP, nohelp);
        editor.putBoolean(APP_PREFERENCES_BOOLEAN_FILENAME, filename);
        editor.apply();
    }

}
