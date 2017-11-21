package kriskires.artolela;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Random;

import kriskires.artolela.Data.PictureContract;

/* Gaming screen */

public class GameActivity extends AppCompatActivity  implements TextToSpeech.OnInitListener{
    private SQLiteHelper dbHelper;
    String wikiLink = "";
    String labelLanguageFrom = "";
    String labelLanguageTo = "";
    final int answersCount = 4;
    int questionsCount;
    int[] rightAnswers;
    int[] receivedAnswers;
    int[] pictureIds;
    ImageView picture;
    ImageView fullSizePicture;
    LinearLayout answers;
    TextView pictureLabel;
    TextView pictureIDLabel;
    RadioGroup pictureRadioGroup;
    Button nextButton;
    Button helpButton;
    TextView levelLabel;
    String languageFrom;
    String languageTo;
    int answeredQuestionsCount = 0;
    RadioButton[] radioButtonAnswers;
    boolean isScaled = false; // True - picture is scaled now, false - picture is't scaled now

    private Button playButton;
    private TextToSpeech mTTS;
    private TextToSpeech mTTSanswer;
    public static final String APP_PREFERENCES = "settings";
    public static final String APP_PREFERENCES_BOOLEAN_CLASSIC = "classic";
    public static final String APP_PREFERENCES_BOOLEAN_AUDIO = "audio";
    public static final String APP_PREFERENCES_BOOLEAN_FULL_AUDIO = "fullaudio";
    public static final String APP_PREFERENCES_BOOLEAN_NOHELP = "nohelp";
    public static final String  APP_PREFERENCES_BOOLEAN_FILENAME = "filename";
    private SharedPreferences mSettings;
    String[] AnswersString;
    String pictureID;
    int rightNumberAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        answers = (LinearLayout) findViewById(R.id.answers);
        picture = (ImageView) findViewById(R.id.picture);
        fullSizePicture = (ImageView) findViewById(R.id.full_size_picture);
        pictureLabel = (TextView) findViewById(R.id.picture_label);
        pictureIDLabel  = (TextView) findViewById(R.id.picture_id);
        pictureRadioGroup = (RadioGroup) findViewById(R.id.pictures_radio_group);
        nextButton = (Button) findViewById(R.id.nextButton);
        helpButton = (Button) findViewById(R.id.helpButton);
        levelLabel = (TextView) findViewById(R.id.level_label);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        radioButtonAnswers = new RadioButton[answersCount];
        for (int i = 0; i < answersCount; i++) {
            radioButtonAnswers[i] = new RadioButton(this);
        }
        languageFrom = getIntent().getStringExtra("languageFrom");
        languageTo = getIntent().getStringExtra("languageTo");
        questionsCount = getIntent().getIntExtra("questionsCount", 5);

        rightAnswers = new int[questionsCount];
        receivedAnswers = new int[questionsCount];
        pictureIds = new int[questionsCount];

        for (int i = 0; i < questionsCount; i++) {
            rightAnswers[i] = -1;
            receivedAnswers[i] = -1;
            pictureIds[i] = -1;
        }

        dbHelper = new SQLiteHelper(this);

        levelLabel.setText((" 0 / " + answeredQuestionsCount + " / "+ questionsCount + " "));

        newQuestion(0);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pictureRadioGroup.getCheckedRadioButtonId() != -1) {
                    if (((RadioButton) pictureRadioGroup.getChildAt(0)).isChecked())
                        ((RadioButton) pictureRadioGroup.getChildAt(0)).setBackgroundColor(Color.RED);
                    if (((RadioButton) pictureRadioGroup.getChildAt(1)).isChecked())
                        ((RadioButton) pictureRadioGroup.getChildAt(1)).setBackgroundColor(Color.RED);
                    if (((RadioButton) pictureRadioGroup.getChildAt(2)).isChecked())
                        ((RadioButton) pictureRadioGroup.getChildAt(2)).setBackgroundColor(Color.RED);
                    if (((RadioButton) pictureRadioGroup.getChildAt(3)).isChecked())
                        ((RadioButton) pictureRadioGroup.getChildAt(3)).setBackgroundColor(Color.RED);

                    ((RadioButton) pictureRadioGroup.getChildAt(rightNumberAnswer)).setBackgroundColor(Color.GREEN);

                    ((RadioButton) pictureRadioGroup.getChildAt(0)).setEnabled(false);
                    ((RadioButton) pictureRadioGroup.getChildAt(1)).setEnabled(false);
                    ((RadioButton) pictureRadioGroup.getChildAt(2)).setEnabled(false);
                    ((RadioButton) pictureRadioGroup.getChildAt(3)).setEnabled(false);
                } else {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.custom_toast_container));

                    TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText(R.string.error3);

                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receivedAnswers[answeredQuestionsCount] != -1) {
                    answeredQuestionsCount++;
                    if (answeredQuestionsCount == questionsCount) {
                        dbHelper.close();
                        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                        intent.putExtra("rightAnswers", rightAnswers);
                        intent.putExtra("receivedAnswers", receivedAnswers);
                        intent.putExtra("questionsCount", questionsCount);
                        startActivity(intent);
                    } else if (MainActivity.isNetworkAvailable(GameActivity.this)) {
                        if (answeredQuestionsCount == questionsCount - 1) {
                            nextButton.setText(getResources().getString(R.string.finish));
                        }
                        if (answeredQuestionsCount < questionsCount) {
                            ((RadioButton)pictureRadioGroup.getChildAt(0)).setEnabled(true);
                            ((RadioButton)pictureRadioGroup.getChildAt(1)).setEnabled(true);
                            ((RadioButton)pictureRadioGroup.getChildAt(2)).setEnabled(true);
                            ((RadioButton)pictureRadioGroup.getChildAt(3)).setEnabled(true);
                            ((RadioButton)pictureRadioGroup.getChildAt(0)).setBackgroundColor(0x00FFFFFF);
                            ((RadioButton)pictureRadioGroup.getChildAt(1)).setBackgroundColor(0x00FFFFFF);
                            ((RadioButton)pictureRadioGroup.getChildAt(2)).setBackgroundColor(0x00FFFFFF);
                            ((RadioButton)pictureRadioGroup.getChildAt(3)).setBackgroundColor(0x00FFFFFF);

                            int rightAnswersCount = 0;
                            for (int i = 0; i < answeredQuestionsCount; i++) {
                                if (rightAnswers[i] == receivedAnswers[i]) {
                                    rightAnswersCount++;
                                }
                            }
                            //pictureIDLabel.setText(pictureID);
                            levelLabel.setText(( " " + rightAnswersCount + " / " + answeredQuestionsCount + " / "+ questionsCount + " "));
                            newQuestion(answeredQuestionsCount);
                        }
                    } else {
                        answeredQuestionsCount--;
                    }
                } else {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.custom_toast_container));

                    TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText(R.string.error3);

                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });


        mTTS = new TextToSpeech(this, this);
        mTTSanswer  = new TextToSpeech(this, this);

        //mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        playButton = (Button) findViewById(R.id.playButton);
        if(mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_AUDIO, false)) {
            pictureLabel.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.VISIBLE);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = pictureLabel.getText().toString();
                    mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        }
        if(mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_FULL_AUDIO, false)) {
            pictureLabel.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.VISIBLE);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = pictureLabel.getText().toString();
                    mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        }
        if(mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_NOHELP, false)) {
            pictureLabel.setVisibility(View.GONE);
            playButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.exit:
                Intent intent_settings = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent_settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub
        if (status == TextToSpeech.SUCCESS) {

            String loc = "en";
            String locanswer = "en";

            if (languageFrom.equals("Italiano")) loc = "it" ;
            if (languageFrom.equals("English")) loc = "en" ;
            if (languageFrom.equals("Русский")) loc = "ru" ;

            if (languageTo.equals("Italiano")) locanswer = "it" ;
            if (languageTo.equals("English")) locanswer = "en" ;
            if (languageTo.equals("Русский")) locanswer = "ru" ;

            Locale locale = new Locale(loc);
            Locale localeanswer = new Locale(locanswer);

            int result = mTTS.setLanguage(locale);
            int resultanswer = mTTSanswer.setLanguage(localeanswer);
            //int result = mTTS.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Sorry, this language is not supported.");
            } else {
                playButton.setEnabled(true);
            }

            if (resultanswer == TextToSpeech.LANG_MISSING_DATA
                    || resultanswer == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Sorry, this language is not supported.");
            }

        } else {
            Log.e("TTS", "Error!");
        }

    }


    @Override
    public void onDestroy() {
        // Don't forget to shutdown mTTS!
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    // Generate new level: picture, label on one language and options on other languages
    private void newQuestion(int questionNumber) {
        pictureRadioGroup.clearCheck();
        final String pictureFilename = getPictureLink(getRandomPictureId(questionNumber) + 1);
        pictureID = pictureFilename;
        if(mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_FILENAME, false)) {
            pictureIDLabel.setText(pictureID);
            pictureIDLabel.setVisibility(View.VISIBLE);
        }
        String pictureUrl = "http://artolela.krc.karelia.ru/pictures-en-it-ru-2017/" + pictureFilename; // Link to the picture
        Picasso.with(this).load(pictureUrl).error(R.mipmap.icon).into(picture); // Download picture and put it into ImageView
        Picasso.with(this).load(pictureUrl).error(R.mipmap.icon).into(fullSizePicture);

        // If touch picture, then hide gaming screen and show full size picture
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picture.setVisibility(View.GONE);
                answers.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                helpButton.setVisibility(View.GONE);
                levelLabel.setVisibility(View.GONE);
                pictureIDLabel.setVisibility(View.GONE);
                fullSizePicture.setVisibility(View.VISIBLE);
                // Resize picture
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutId);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) fullSizePicture.getLayoutParams();
                params.width = linearLayout.getWidth();
                params.height = linearLayout.getHeight();
                fullSizePicture.setLayoutParams(params);
                isScaled = true;
            }
        });



        // If touch full size picture, then hide full size picture and show gaming screen
        fullSizePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullSizePicture.setVisibility(View.GONE);
                picture.setVisibility(View.VISIBLE);
                answers.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
                helpButton.setVisibility(View.VISIBLE);
                levelLabel.setVisibility(View.VISIBLE);
                pictureIDLabel.setVisibility(View.VISIBLE);
                isScaled = false;
            }
        });


        getPictureLabels(languageFrom, languageTo);

        String[] answers = new String[answersCount];
        AnswersString = new String[answersCount];
        Random random = new Random();
        int rightNumber = random.nextInt(answersCount - 1);
        rightAnswers[questionNumber] = rightNumber;

        pictureLabel.setText(labelLanguageFrom);
        answers[rightNumber] = labelLanguageTo;
        AnswersString[rightNumber] = labelLanguageTo;
        rightNumberAnswer = rightNumber;

        for (int i = 0; i < answersCount; i++) {
            int randomNumber = random.nextInt((int) getRowsCount(PictureContract.Picture.TABLE_NAME)) + 1;
            if (i != rightNumber) {
                answers[i] = getAnswers(randomNumber);
                AnswersString[i] = getAnswers(randomNumber);

//                if(answers[i].equals(answers[rightNumber])){
//                    i--;
//                }

                for( int j = 0; j < i; j++){
                    if(answers[i].equals(answers[j])) {
                        i--;
                        break;
                    }
                }

            }
        }

        for (int i = 0; i < answersCount; i++) {
            radioButtonAnswers[i].setId(i);
            radioButtonAnswers[i].setText(answers[i]);
            if (pictureRadioGroup.getChildCount() < answersCount) {
                pictureRadioGroup.addView(radioButtonAnswers[i]);
            }
            if(mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_FULL_AUDIO, false)) {
                radioButtonAnswers[i].setText(R.string.play_button);
            }

        }

        pictureRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()

        {
            @Override
            public void onCheckedChanged(RadioGroup group, final int checkedId) {
                String text = "";
                if (checkedId >= 0) {
                    if (radioButtonAnswers[checkedId].isChecked()) {
                        receivedAnswers[answeredQuestionsCount] = checkedId;
                        //text = ((RadioButton)group.getChildAt(checkedId)).getText().toString();
                        text = AnswersString[checkedId];
                    }
                }
                if(mSettings.getBoolean(APP_PREFERENCES_BOOLEAN_FULL_AUDIO, false)) {
                    mTTSanswer.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    // Get random picture ID from table Images
    private int getRandomPictureId(int questionNumber) {
        Random random = new Random();
        int randomNumber = random.nextInt((int) getRowsCount(PictureContract.Image.TABLE_NAME));
        for (int i = 0; i < questionNumber; i++) {
            if (pictureIds[i] == randomNumber) {
                randomNumber = getRandomPictureId(questionNumber);
                break;
            }
        }
        pictureIds[questionNumber] = randomNumber;
        return randomNumber;
    }

    // Get rows count in table tableName
    private long getRowsCount(String tableName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, tableName);
        db.close();
        return count;
    }

    // Get picture link for obtained picture ID from table Images
    private String getPictureLink(int id) {
        SQLiteDatabase dataBase = dbHelper.getReadableDatabase();

        // Set the condition for the selection - the list of columns
        String[] projection = {
                PictureContract.Image.ID,
                PictureContract.Image.COLUMN_FILENAME,
                PictureContract.Image.COLUMN_URL};

        String where = PictureContract.Image.ID + " = " + id;

        // Request
        Cursor cursor = dataBase.query(
                PictureContract.Image.TABLE_NAME,   // Table
                projection,            // Columns
                where,                 // Columns for WHERE
                null,                  // Values for WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                 // Sorting order

        String currentFilename;
        try {
            // Find the index of each column
            int filenameColumnIndex = cursor.getColumnIndex(PictureContract.Image.COLUMN_FILENAME);
            int urlColumnIndex = cursor.getColumnIndex(PictureContract.Image.COLUMN_URL);
            cursor.moveToNext();
            currentFilename = cursor.getString(filenameColumnIndex);
            wikiLink = cursor.getString(urlColumnIndex);
        } finally {
            cursor.close();
            dataBase.close();
        }
        return currentFilename;
    }

    // Get labels on 2 languages (name on first language and right answer on second language) for chosen picture
    private void getPictureLabels(String languageFrom, String languageTo) {
        SQLiteDatabase dataBase = dbHelper.getReadableDatabase();

        String labelFrom = getColumnName(languageFrom);
        String labelTo = getColumnName(languageTo);

        String[] projection = {labelFrom, labelTo};

        String where = PictureContract.Picture.COLUMN_IMAGE_LINK + " = " + '"' + wikiLink + '"';

        Cursor cursor = dataBase.query(
                PictureContract.Picture.TABLE_NAME,
                projection,
                where,
                null,
                null,
                null,
                null);

        try {
            int labelFromColumnIndex = cursor.getColumnIndex(labelFrom);
            int labelToColumnIndex = cursor.getColumnIndex(labelTo);

            cursor.moveToNext();
            labelLanguageFrom = cursor.getString(labelFromColumnIndex);
            labelLanguageTo = cursor.getString(labelToColumnIndex);

        } finally {
            cursor.close();
            dataBase.close();
        }
    }

    // Get wrong answers for chosen picture
    private String getAnswers(int id) {
        SQLiteDatabase dataBase = dbHelper.getReadableDatabase();

        String labelTo = getColumnName(languageTo);

        String[] projection = {labelTo};
        String where = PictureContract.Picture.ID + " = " + id;

        Cursor cursor = dataBase.query(
                PictureContract.Picture.TABLE_NAME,
                projection,
                where,
                null,
                null,
                null,
                null);

        String label2;
        try {
            int labelColumnIndex = cursor.getColumnIndex(labelTo);

            cursor.moveToNext();
            label2 = cursor.getString(labelColumnIndex);

        } finally {
            cursor.close();
            dataBase.close();
        }
        return label2;
    }

    // Get column name for table Picture depending on the language
    private String getColumnName(String language) {
        String columnName = "";
        switch (language) {
            case "Русский":
                columnName = PictureContract.Picture.COLUMN_LABEL_RU;
                break;
            case "English":
                columnName = PictureContract.Picture.COLUMN_LABEL_EN;
                break;
            case "Italiano":
                columnName = PictureContract.Picture.COLUMN_LABEL_IT;
                break;
        }
        return columnName;
    }

    // Disable button "Back"
    @Override
    public void onBackPressed() {

    }
}
