package com.pycitup.pyc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pycitup.pyc.R;

import java.util.ArrayList;
import java.util.List;

public class ConversationsActivity extends Activity {

    private String TAG = ConversationsActivity.class.getSimpleName();

    private ViewPager mViewPager;
    ArrayList<Bitmap> mResources = new ArrayList<Bitmap>();
    ArrayList<String> mResourceIds = new ArrayList<String>();
    String mCurrentFileId = null;
    ListView mChatLogList;
    String mConversationId;
    ArrayList<ParseObject> mChatMessages = new ArrayList<ParseObject>();
    CustomAdapter mChatLogAdapter;

    LayoutInflater mLayoutInflator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        // ViewPager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new CustomPagerAdapter(this));

        // Conversation.objectId
        mConversationId = getIntent().getExtras().getString("conversationId");

        // Get Conversation Files
        ParseQuery<ParseObject> conversationFiles = ParseQuery.getQuery("ConversationFiles");
        conversationFiles.whereEqualTo(
                "conversationId",
                ParseObject.createWithoutData("Conversation", mConversationId)
        );

        conversationFiles.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                for (ParseObject conversationFile : parseObjects) {

                    // Cache object IDs of files
                    mResourceIds.add(conversationFile.getObjectId());
                    if (mCurrentFileId == null)
                        mCurrentFileId = mResourceIds.get(0);

                    ParseFile file = (ParseFile) conversationFile.get("file");
                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            if (e == null) {
                                // bytes data for the file
                                // Convert bytes data into a Bitmap
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                //ImageView imageView = new ImageView(ConversationsActivity.this);
                                // Set the Bitmap data to the ImageView
                                //imageView.setImageBitmap(bmp);

                                // Get the Root View of the layout
                                // ViewGroup layout = (ViewGroup) findViewById(android.R.id.content);
                                // Add the ImageView to the Layout
                                //mViewPager.addView(imageView);
                                mResources.add(bmp);

                                // notify the pager adapter that the
                                // underlying data set has changed
                                mViewPager.getAdapter().notifyDataSetChanged();
                            }
                            else {

                            }
                        }
                    });
                }

                // Fetch all chat messages
                getChatMessages();
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                String objectId = mResourceIds.get(position);
                mCurrentFileId = objectId;
            }
        });

        final EditText chatMessageField = (EditText) findViewById(R.id.chatMessage);
        Button chatSend = (Button) findViewById(R.id.chatSend);
        chatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chat button submitted
                String chatBody = chatMessageField.getText().toString();

                final ParseObject chatMessage = new ParseObject("ChatMessage");
                chatMessage.put("sender", ParseUser.getCurrentUser().getObjectId());
                chatMessage.put("conversationId", mConversationId);
                chatMessage.put("conversationFileId", mCurrentFileId);
                chatMessage.put("message", chatBody);
                chatMessage.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            chatMessageField.setText("");
                            Toast.makeText(ConversationsActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                        }
                        else {

                        }
                    }
                });
            }
        });


        // Fetch all the chat messages and show up
        // in the list
        mChatLogList = (ListView) findViewById(R.id.chatLog);
        mChatLogAdapter = new CustomAdapter();
        mChatLogList.setAdapter(mChatLogAdapter);

        // Fetch all the chat messages
        // getChatMessages();

        mLayoutInflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void getChatMessages() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatMessage");
        // where conversation ID is this
        query.whereEqualTo("conversationId", mConversationId);
        query.whereEqualTo("conversationFileId", mCurrentFileId);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // Log.d(TAG, String.valueOf(parseObjects.size()));
                for (ParseObject chatMessage : parseObjects) {
                    mChatMessages.add(chatMessage);
                }

                mChatLogAdapter.notifyDataSetChanged();
            }
        });
    }

    class CustomPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public CustomPagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mResources.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Log.d(TAG, "instantiateItem " + position);

            View itemView = mLayoutInflater.inflate(R.layout.conversation_pager_item, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
            //imageView.setImageResource(mResources[position]);
            imageView.setImageBitmap(mResources.get(position));

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // Log.d(TAG, "destroyItem " + position);

            container.removeView((LinearLayout) object);
        }
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mChatMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return mChatMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;

            if (convertView == null) {
                view = mLayoutInflator.inflate(R.layout.conversation_chat_msg, null);
            }
            else {
                view = convertView;
            }

            TextView chatMessage = (TextView) view.findViewById(R.id.chatMessage);
            chatMessage.setText(mChatMessages.get(position).getString("message"));

            return view;
        }
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
