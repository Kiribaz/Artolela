package kriskires.artolela;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

/* Start screen */

public class MainActivity extends AppCompatActivity {

    private SQLiteHelper dbHelper;
    String languageFrom, languageTo;
    String[] languages;
    final int seekBarMinValue = 0;
    final int seekBarMaxValue = 50;
    final int seekBarStep = 5;
    int levelsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new SQLiteHelper(this);
        languages = getResources().getStringArray(R.array.languages);

        final Spinner spinnerFrom = (Spinner) findViewById(R.id.spinnerFrom);
        MyCustomAdapter adapterFrom = new MyCustomAdapter(MainActivity.this, R.layout.spinner, languages);
        spinnerFrom.setAdapter(adapterFrom);
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                String[] chosenLanguageFrom = getResources().getStringArray(R.array.languages);
                languageFrom = chosenLanguageFrom[selectedItemPosition];
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Spinner spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
        MyCustomAdapter adapterTo = new MyCustomAdapter(MainActivity.this, R.layout.spinner, languages);
        spinnerTo.setAdapter(adapterTo);
        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                String[] chosenLanguageTo = getResources().getStringArray(R.array.languages);
                languageTo = chosenLanguageTo[selectedItemPosition];
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        levelsCount = seekBarMinValue;
        SeekBar seekBarLevels = (SeekBar) findViewById(R.id.seekBar);
        final TextView seekBarValue = (TextView) findViewById(R.id.seekBarValue);
        TextView seekBarMin = (TextView) findViewById(R.id.seekBarMinValue);
        TextView seekBarMax = (TextView) findViewById(R.id.seekBarMaxValue);

        seekBarLevels.setMax(seekBarMaxValue);
        seekBarLevels.setProgress(seekBarMinValue);
        seekBarValue.setText(String.valueOf(levelsCount));
        seekBarMin.setText(String.valueOf(seekBarMinValue));
        seekBarMax.setText(String.valueOf(seekBarMaxValue));

        seekBarLevels.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = Math.round(progress / seekBarStep) * seekBarStep;
                seekBar.setProgress(progress);
                seekBarValue.setText(String.valueOf(progress));
                levelsCount = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (languageFrom.equals(languageTo)) {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText(R.string.error);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                } else if (levelsCount == 0) {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText(R.string.error2);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                } else {
                    if (isNetworkAvailable(MainActivity.this)) {
                        dbHelper.close();
                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        intent.putExtra("languageFrom", languageFrom);
                        intent.putExtra("languageTo", languageTo);
                        intent.putExtra("questionsCount", levelsCount);
                        startActivity(intent);
                    }
                }
            }
        });

        TextView textAboutApp = (TextView) findViewById(R.id.about_app);
        textAboutApp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AboutApplicationActivity.class);
                startActivity(intent);
            }
        });

    }

    private class MyCustomAdapter extends ArrayAdapter<String> {

        MyCustomAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.spinner, parent, false);
            TextView label = (TextView) row.findViewById(R.id.language);
            label.setText(languages[position]);
            ImageView icon = (ImageView) row.findViewById(R.id.flag);

            switch (languages[position]) {
                case "Русский":
                    icon.setImageResource(R.mipmap.russian_flag_icon);
                    break;
                case "English":
                    icon.setImageResource(R.mipmap.english_flag_icon);
                    break;
                case "Italiano":
                    icon.setImageResource(R.mipmap.italian_flag_icon);
                    break;
                default:
                    icon.setImageResource(R.mipmap.ic_launcher);
                    break;
            }
            return row;
        }
    }

    // Check for access to the Internet
    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            wiFiAlertDialog(activity);
            return false;
        }
    }

    public static void wiFiAlertDialog(final Activity activity) {
        final WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
        alertDialog.setTitle(R.string.dialog_wifi_title);
        alertDialog.setMessage(R.string.dialog_wifi_message);
        alertDialog.setPositiveButton(R.string.dialog_wifi_positive_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        wifiManager.setWifiEnabled(true);
                        activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        alertDialog.setNegativeButton(R.string.dialog_wifi_negative_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    // Disable button "Back"
    @Override
    public void onBackPressed() {

    }
}