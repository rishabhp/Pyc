package com.pycitup.pyc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ConversationsListActivity extends Activity {

    protected ListView mListView;
    protected ArrayAdapter mAdapter;
    ArrayList<String> mConversationIds = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations_list);

        // Fetch all the conversations_list from parse to
        // which this user is tagged as a recipient
        // sorted by date

        // Get the list view
        mListView = (ListView) findViewById(R.id.listView);

        // Filter ConversationRecipients by userId which will be
        // the current user's objectId
        ParseQuery<ParseObject> conversationRecipientsQuery = ParseQuery.getQuery("ConversationRecipients");
        conversationRecipientsQuery.whereEqualTo(
                "userId",
                ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId())
        );

        // Include data from Conversation class (conversationId is a Pointer)
        conversationRecipientsQuery.include("conversationId");

        // Fire the query to get data
        conversationRecipientsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // List of conversation titles
                ArrayList<String> conversationTitles = new ArrayList<String>();

                // Loop
                for (ParseObject conversationRecipient : parseObjects) {
                    // Get the Conversation data attached to the
                    // ConversationRecipient row
                    ParseObject conversation = conversationRecipient.getParseObject("conversationId");
                    // Add the Conversation title to the list
                    conversationTitles.add(conversation.getString("name"));

                    // Add the Conversation ID to a list
                    mConversationIds.add(conversation.getObjectId());
                }

                // Array adapter
                ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(
                        ConversationsListActivity.this,
                        android.R.layout.simple_list_item_1,
                        conversationTitles
                );
                // Attach the adapter to it
                mListView.setAdapter(mAdapter);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Fire the Conversations Activity and pass
                // the Conversation ID to it

                Intent intent = new Intent(ConversationsListActivity.this, ConversationsActivity.class);
                intent.putExtra("conversationId", mConversationIds.get(position));
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversations_list, menu);
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
