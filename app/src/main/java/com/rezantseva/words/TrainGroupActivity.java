package com.rezantseva.words;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.rezantseva.words.db.Database;
import com.rezantseva.words.db.Group;
import com.rezantseva.words.db.Word;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class TrainGroupActivity extends AppCompatActivity {

    private static final String TAG = "TrainGroupActivity";
    public static final String DATA_KEY_GROUP = "group";
    private static final String ACTIVITY_TRAIN_GROUP_STATE_BUNDLE = "activity-train-group-state-bundle";
    private static final String ACTIVITY_TRAIN_GROUP_STATE_CURRENT_POSITION = "activity-train-group-state-current-position";
    public static final int REQUEST_CODE_EDIT_GROUP = 1;

    private Group mGroup;
    private Button mOriginWordButton;
    private Button[] mTranslationButtons;

    private ArrayList<Word> mWords = new ArrayList<>();
    private int mCurrentWordPos = 0;
    private Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_group);
        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(ACTIVITY_TRAIN_GROUP_STATE_BUNDLE);
            mGroup = Group.fromBundle(bundle);
            mCurrentWordPos = bundle.getInt(ACTIVITY_TRAIN_GROUP_STATE_CURRENT_POSITION);
        } else {
            mGroup = getIntent().getParcelableExtra(DATA_KEY_GROUP);
        }
        mOriginWordButton = findViewById(R.id.originWordButton);
        mTranslationButtons = new Button[]{
                findViewById(R.id.translationButton1),
                findViewById(R.id.translationButton2),
                findViewById(R.id.translationButton3),
                findViewById(R.id.translationButton4)
        };
        View.OnClickListener translationButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTranslationClicked((Button) v);
            }
        };
        for (Button button : mTranslationButtons) {
            button.setOnClickListener(translationButtonClickListener);
        }
        resetButtonColors();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Context context = getApplicationContext();
        final Database db = new Database(context);
        AsyncTask<Void, Void, ArrayList<Word>> task = new AsyncTask<Void, Void, ArrayList<Word>>() {
            @Override
            protected ArrayList<Word> doInBackground(Void... params) {
                try {
                    return db.getGroupWords(mGroup.getId());
                } catch (SQLException e) {
                    Log.e(TAG, "Words loading error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ArrayList<Word> words) {
                startTrain(words);
            }
        };
        task.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        mGroup.toBundle(bundle);
        bundle.putInt(ACTIVITY_TRAIN_GROUP_STATE_CURRENT_POSITION, mCurrentWordPos);
        outState.putBundle(ACTIVITY_TRAIN_GROUP_STATE_BUNDLE, bundle);
    }

    private void startTrain(ArrayList<Word> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        mWords = words;
        trainWord();
    }

    private void trainWord() {

        resetButtonColors();
        mOriginWordButton.setText(mWords.get(mCurrentWordPos).getOrigin());

        ArrayList<Word> wWords = getWrongWords();
        final int correctWordPos = mRandom.nextInt(mTranslationButtons.length);
        int wWordPos = 0;
        for (int i = 0; i < mTranslationButtons.length; i++) {
            String translation;
            if (i == correctWordPos) {
                translation = mWords.get(mCurrentWordPos).getTranslation();
            } else {
                if (wWordPos < wWords.size()) {
                    translation = wWords.get(wWordPos).getTranslation();
                } else {
                    translation = "-";
                }
                wWordPos++;
            }
            mTranslationButtons[i].setText(translation);
        }

    }

    private void onTranslationClicked(Button b) {
        String translation = mWords.get(mCurrentWordPos).getTranslation();
        if (translation.equals(b.getText())) {
            mCurrentWordPos++;
            if (mCurrentWordPos >= mWords.size()) {
                finish();
                return;
            }
            trainWord();
        } else {
            b.setBackgroundColor(Color.RED);
        }
    }

    public void onEditGroupBtnClick(View v) {
        Intent intent = new Intent(this, EditGroupActivity.class);
        intent.putExtra(EditGroupActivity.DATA_KEY_GROUP, mGroup);
        startActivityForResult(intent, REQUEST_CODE_EDIT_GROUP);
    }

    public void onDeleteGroupBtnClick(View v) {
        final Context context = this;
        new AlertDialog.Builder(this).setTitle(R.string.activity_delete_group_title).setMessage(mGroup.getName()).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final Database db = new Database(context);
                db.deleteGroupAsync(context, mGroup.getId());
                finish();
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    private void resetButtonColors() {
        for (Button button : mTranslationButtons) {
            button.setBackgroundColor(Color.LTGRAY);
        }
    }

    private ArrayList<Word> getWrongWords() {
        ArrayList<Word> wWords = new ArrayList<>(mWords);
        wWords.remove(mCurrentWordPos);
        Collections.shuffle(wWords);
        return wWords;
    }
}
