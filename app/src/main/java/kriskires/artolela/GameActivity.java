package kriskires.artolela;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
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

import java.util.Random;

import kriskires.artolela.Data.PictureContract;

/* Gaming screen */

public class GameActivity extends AppCompatActivity {
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
    RadioGroup pictureRadioGroup;
    Button nextButton;
    String languageFrom;
    String languageTo;
    int answeredQuestionsCount = 0;
    RadioButton[] radioButtonAnswers;
    boolean isScaled = false; // True - picture is scaled now, false - picture is't scaled now

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        answers = (LinearLayout) findViewById(R.id.answers);
        picture = (ImageView) findViewById(R.id.picture);
        fullSizePicture = (ImageView) findViewById(R.id.full_size_picture);
        pictureLabel = (TextView) findViewById(R.id.picture_label);
        pictureRadioGroup = (RadioGroup) findViewById(R.id.pictures_radio_group);
        nextButton = (Button) findViewById(R.id.nextButton);

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

        newQuestion(0);
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

    }

    // Generate new level: picture, label on one language and options on other languages
    private void newQuestion(int questionNumber) {
        pictureRadioGroup.clearCheck();
        final String pictureFilename = getPictureLink(getRandomPictureId(questionNumber) + 1);
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
                isScaled = false;
            }
        });


        getPictureLabels(languageFrom, languageTo);

        String[] answers = new String[answersCount];
        Random random = new Random();
        int rightNumber = random.nextInt(answersCount - 1);
        rightAnswers[questionNumber] = rightNumber;

        pictureLabel.setText(labelLanguageFrom);
        answers[rightNumber] = labelLanguageTo;

        for (int i = 0; i < answersCount; i++) {
            int randomNumber = random.nextInt((int) getRowsCount(PictureContract.Picture.TABLE_NAME)) + 1;
            if (i != rightNumber) {
                answers[i] = getAnswers(randomNumber);
            }
        }

        for (int i = 0; i < answersCount; i++) {
            radioButtonAnswers[i].setId(i);
            radioButtonAnswers[i].setText(answers[i]);
            if (pictureRadioGroup.getChildCount() < answersCount) {
                pictureRadioGroup.addView(radioButtonAnswers[i]);
            }
        }

        pictureRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()

        {
            @Override
            public void onCheckedChanged(RadioGroup group, final int checkedId) {
                if (checkedId >= 0) {
                    if (radioButtonAnswers[checkedId].isChecked()) {
                        receivedAnswers[answeredQuestionsCount] = checkedId;
                    }
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
