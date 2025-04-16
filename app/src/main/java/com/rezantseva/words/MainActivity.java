package com.rezantseva.words;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;

import com.rezantseva.words.db.Database;
import com.rezantseva.words.db.Group;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int REQUEST_CODE_TRAIN_GROUP = 1;
    public static final int REQUEST_CODE_NEW_GROUP = 2;

    private ListView mGroupsListView;
    private ArrayAdapter<Group> mGroupsListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGroupsListData = new GroupNameArrayAdapter(this);
        mGroupsListView = findViewById(R.id.groups_list);
        mGroupsListView.setAdapter(mGroupsListData);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Context context = getApplicationContext();
        final Database db = new Database(context);
        AsyncTask<Void, Void, ArrayList<Group>> task = new AsyncTask<Void, Void, ArrayList<Group>>() {
            @Override
            protected ArrayList<Group> doInBackground(Void... params) {
                try {
                    return db.getAllGroups();
                } catch (SQLException e) {
                    Log.e(TAG, "Groups loading error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ArrayList<Group> groups) {
                mGroupsListData.clear();
                mGroupsListData.addAll(groups);
            }
        };
        task.execute();
    }

    public void onTrainGroup(Group group) {
        Intent intent = new Intent(this, TrainGroupActivity.class);
        intent.putExtra(TrainGroupActivity.DATA_KEY_GROUP, group);
        startActivityForResult(intent, REQUEST_CODE_TRAIN_GROUP);
    }

    public void onNewGroup() {
        Intent intent = new Intent(this, EditGroupActivity.class);
        intent.putExtra(EditGroupActivity.DATA_KEY_GROUP, new Group("Новая Тема"));
        startActivityForResult(intent, REQUEST_CODE_NEW_GROUP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_group) {
            onNewGroup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class GroupItemViewHolder {
        private Group group;
        private Button button;

        public GroupItemViewHolder(Button button) {
            this.button = button;
        }

        public void setGroup(Group group) {
            this.group = group;
            this.button.setText(group.getName());
            this.button.setTag(group);
        }
    }

    private static class GroupNameArrayAdapter extends ArrayAdapter<Group> {

        private View.OnClickListener mGroupButtonClickListener;

        public GroupNameArrayAdapter(MainActivity context) {
            super(context, R.layout.list_item_group);
            mGroupButtonClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getContext()).onTrainGroup((Group) v.getTag());
                }
            };
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Group group = getItem(position);

            final GroupItemViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_group, parent, false);

                Button button = (Button) convertView.findViewById(R.id.groupItemButton);
                button.setOnClickListener(mGroupButtonClickListener);
                holder = new GroupItemViewHolder(button);
                holder.setGroup(group);

                convertView.setTag(holder);
            } else {
                holder = (GroupItemViewHolder) convertView.getTag();
                holder.setGroup(group);
            }
            return convertView;
        }
    }
}
