package kriskires.artolela;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/* Finish screen */

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        TextView score = (TextView) findViewById(R.id.score);
        TextView result = (TextView) findViewById(R.id.result);
        Button againButton = (Button) findViewById(R.id.again_button);
        int[] rightAnswers;
        int[] receivedAnswers;
        int questionsCount;
        rightAnswers = getIntent().getIntArrayExtra("rightAnswers");
        receivedAnswers = getIntent().getIntArrayExtra("receivedAnswers");
        questionsCount = getIntent().getIntExtra("questionsCount", 0);

        int rightAnswersCount = 0;
        for (int i = 0; i < questionsCount; i++) {
            if (rightAnswers[i] == receivedAnswers[i]) {
                rightAnswersCount++;
            }
        }

        score.setText(getResources().getString(R.string.score, rightAnswersCount, questionsCount));
        result.setText(getResources().getString(R.string.result));

        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // Disable button "Back"
    @Override
    public void onBackPressed() {

    }

}
