package com.lakra.word;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.lakra.word.R;

/**
 *
 */
public class WordActivity extends AppCompatActivity {

    private EditText mWordEditText;

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        mWordEditText = findViewById(R.id.wordEditText);

        //add button
        final Button mAddButton = findViewById(R.id.addButton);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param view
             */
            public void onClick(View view) {
                Intent replyIntent = new Intent();
                if (TextUtils.isEmpty(mWordEditText.getText())) {
                    setResult(RESULT_CANCELED, replyIntent);
                } else {
                    String word = mWordEditText.getText().toString();
                    replyIntent.putExtra(Constants.ACTION, ActionType.ADD);
                    replyIntent.putExtra(Constants.VALUE, word);
                    setResult(RESULT_OK, replyIntent);
                }
                finish();
            }
        });

        //update button
        final Button mUpdateButton = findViewById(R.id.updateButton);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param view
             */
            public void onClick(View view) {
                Intent replyIntent = new Intent();
                if (TextUtils.isEmpty(mWordEditText.getText())) {
                    setResult(RESULT_CANCELED, replyIntent);
                } else {
                    String word = mWordEditText.getText().toString();
                    replyIntent.putExtra(Constants.ACTION, ActionType.UPDATE);
                    replyIntent.putExtra(Constants.VALUE, word);
                    setResult(RESULT_OK, replyIntent);
                }
                finish();
            }
        });


        //delete button
        final Button mDeleteButton = findViewById(R.id.deleteButton);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param view
             */
            public void onClick(View view) {
                Intent replyIntent = new Intent();
                if (TextUtils.isEmpty(mWordEditText.getText())) {
                    setResult(RESULT_CANCELED, replyIntent);
                } else {
                    String word = mWordEditText.getText().toString();
                    replyIntent.putExtra(Constants.ACTION, ActionType.DELETE);
                    replyIntent.putExtra(Constants.VALUE, word);
                    setResult(RESULT_OK, replyIntent);
                }
                finish();
            }
        });
    }


}
