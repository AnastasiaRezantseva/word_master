package com.rezantseva.words;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.rezantseva.words.db.Database;
import com.rezantseva.words.db.Group;
import com.rezantseva.words.db.Word;

import java.util.ArrayList;
import java.util.List;

public class EditGroupActivity extends AppCompatActivity {
    private static final String TAG = "EditGroupActivity";
    public static final String DATA_KEY_GROUP = "group";
    private static final String ACTIVITY_EDIT_GROUP_STATE_BUNDLE = "activity-edit-group-state-bundle";

    private Group mGroup;
    private EditText mEditGroupName;
    private ListView mWordsView;
    private WordsArrayAdapter mWordsListData;
    private EditText mEditOrigin;
    private EditText mEditTranslation;
    private int mSelectedPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        if (savedInstanceState != null) {
            mGroup = Group.fromBundle(savedInstanceState.getBundle(ACTIVITY_EDIT_GROUP_STATE_BUNDLE));
        } else {
            mGroup = getIntent().getParcelableExtra(DATA_KEY_GROUP);
        }
        mSelectedPos = -1;

        mEditGroupName = findViewById(R.id.editGroupName);
        mEditGroupName.setText(mGroup.getName());

        mWordsListData = new WordsArrayAdapter(this,  new ArrayList<Word>());
        mWordsView = findViewById(R.id.wordsList);
        mWordsView.setAdapter(mWordsListData);
        mWordsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSelectWord(position);
            }
        });

        mEditOrigin = findViewById(R.id.originEditText);
        mEditTranslation = findViewById(R.id.translationEditText);
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

                mWordsListData.addAll(words);
            }
        };
        task.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        mGroup.toBundle(bundle);
        outState.putBundle(ACTIVITY_EDIT_GROUP_STATE_BUNDLE, bundle);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final Context context = getApplicationContext();
        final Database db = new Database(context);
        mGroup.setName(mEditGroupName.getText().toString());
        db.insertOrUpdateGroup(mGroup, mWordsListData.getWords());
    }

    public void onNewWord(View v) {
        Word word = new Word();
        word.setOrigin(mEditOrigin.getText().toString());
        word.setTranslation(mEditTranslation.getText().toString());
        if (TextUtils.isEmpty(word.getOrigin()) || TextUtils.isEmpty(word.getTranslation())) {
            return;
        }
        mWordsListData.add(word);
        mEditOrigin.setText("");
        mEditTranslation.setText("");
    }

    public void onSelectWord(int position) {
        mSelectedPos = position;
        Word word = mWordsListData.getItem(mSelectedPos);
        mEditOrigin.setText(word.getOrigin());
        mEditTranslation.setText(word.getTranslation());

    }

    public void onApplyWord(View v) {
        if (mSelectedPos < 0) {
            return;
        }
        Word word = mWordsListData.getItem(mSelectedPos);
        word.setOrigin(mEditOrigin.getText().toString());
        word.setTranslation(mEditTranslation.getText().toString());
        mWordsListData.notifyDataSetChanged();
        final Context context = getApplicationContext();
        final Database db = new Database(context);
        db.insertOrUpdateWord(mGroup.getId(), word);
        mSelectedPos = -1;
    }

    public void onDeleteWord(View v) {
        mEditOrigin.setText("");
        mEditTranslation.setText("");
        if (mSelectedPos < 0) {
            return;
        }
        Word word = mWordsListData.getItem(mSelectedPos);
        mWordsListData.remove(word);
        final Context context = getApplicationContext();
        final Database db = new Database(context);
        db.deleteWord(word.getId());
        mSelectedPos = -1;
    }

    private static class WordItemViewHolder {
        private TextView originTextView;
        private TextView translationTextView;

        public WordItemViewHolder(TextView originTextView, TextView translationTextView) {
            this.originTextView = originTextView;
            this.translationTextView = translationTextView;
        }

        public void setWord(Word word) {
            originTextView.setText(word.getOrigin());
            translationTextView.setText(word.getTranslation());
        }
    }

    private static class WordsArrayAdapter extends ArrayAdapter<Word> {
        private List<Word> mWords;

        public WordsArrayAdapter(Context context, List<Word> items) {
            super(context, R.layout.list_item_group, items);
            mWords = items;
        }

        public List<Word> getWords() {
            return this.mWords;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Word word = getItem(position);

            final WordItemViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_word, parent, false);

                TextView originEditText = convertView.findViewById(R.id.originEditText);
                TextView translationEditText = convertView.findViewById(R.id.translationEditText);

                holder = new WordItemViewHolder(originEditText, translationEditText);
                holder.setWord(word);

                convertView.setTag(holder);
            } else {
                holder = (WordItemViewHolder) convertView.getTag();
                holder.setWord(word);
            }
            return convertView;
        }
    }

}
