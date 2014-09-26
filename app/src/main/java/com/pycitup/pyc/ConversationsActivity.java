package com.pycitup.pyc;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pycitup.pyc.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConversationsActivity extends Activity {

    protected ListView mListView;
    protected ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        // Fetch all the conversations from parse to
        // which this user is tagged as a recipient
        // sorted by date

        ParseQuery<ParseObject> conversationRecipientsQuery = ParseQuery.getQuery("ConversationRecipients");
        conversationRecipientsQuery.whereEqualTo(
                "userId",
                ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId())
        );

        conversationRecipientsQuery.include("conversationId");

        conversationRecipientsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ArrayList<String> conversationTitles = new ArrayList<String>();
                for (ParseObject conversationRecipient : parseObjects) {
                    ParseObject conversation = conversationRecipient.getParseObject("conversationId");
                    conversationTitles.add(conversation.getString("name"));
                }

                ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(
                        ConversationsActivity.this,
                        android.R.layout.simple_list_item_1,
                        conversationTitles
                );
                mListView = (ListView) findViewById(R.id.listView);
                mListView.setAdapter(mAdapter);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
