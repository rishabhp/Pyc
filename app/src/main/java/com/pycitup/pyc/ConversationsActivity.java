package com.pycitup.pyc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ListPopupWindowCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.pycitup.pyc.R;

import org.json.JSONException;
import org.json.JSONObject;

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
    boolean mNewMessageSent = false;

    Handler mHandler = new Handler();

    LayoutInflater mLayoutInflator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        // ViewPager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new CustomPagerAdapter(this));

        // PubNub Integration
        final Pubnub pubnub = new Pubnub(
                PycApplication.PUBNUB_PUB_KEY,
                PycApplication.PUBNUB_SUB_KEY,
                PycApplication.PUBNUB_SECRET_KEY
        );

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
                int i = 0;
                for (ParseObject conversationFile : parseObjects) {

                    final int k = i;

                    // Cache object IDs of files
                    mResourceIds.add(conversationFile.getObjectId());

                    if (mCurrentFileId == null)
                        mCurrentFileId = mResourceIds.get(k);

                    mResources.add(null); // add empty placeholder

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
                                mResources.set(k, bmp);

                                // notify the pager adapter that the
                                // underlying data set has changed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mViewPager.getAdapter().notifyDataSetChanged();
                                    }
                                });
                                //mViewPager.getAdapter().notifyDataSetChanged();
                            }
                            else {

                            }
                        }
                    });

                    ++i;
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

                getChatMessages();
            }
        });


        final EditText chatMessageField = (EditText) findViewById(R.id.chatMessage);
        Button chatSend = (Button) findViewById(R.id.chatSend);
        chatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chat button submitted
                final String chatBody = chatMessageField.getText().toString();

                final ParseObject chatMessage = new ParseObject("ChatMessage");
                chatMessage.put("sender", ParseUser.getCurrentUser().getObjectId());
                chatMessage.put("conversationId", mConversationId);
                chatMessage.put("conversationFileId", mCurrentFileId);
                chatMessage.put("message", chatBody);
                chatMessage.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            mNewMessageSent = true;
                            chatMessageField.setText("");
                            Toast.makeText(ConversationsActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();

                            JSONObject json = new JSONObject();
                            try {
                                json.put("chatMessageId", chatMessage.getObjectId());
                                json.put("conversationId", mConversationId);
                                json.put("conversationFileId", mCurrentFileId);
                                json.put("chatMessage", chatBody);
                            }
                            catch (JSONException je) {
                                je.printStackTrace();
                            }

                            pubnub.publish("conversation_"+mConversationId, json, new Callback() {
                                @Override
                                public void successCallback(String channel, Object message) {
                                    Log.d(TAG, "Publishing done");
                                }
                            });

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
        mChatLogList.setDivider(null);
        mChatLogList.setDividerHeight(0);

        mChatLogAdapter = new CustomAdapter();
        mChatLogList.setAdapter(mChatLogAdapter);

        // Fetch all the chat messages
        // getChatMessages();

        mLayoutInflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // We're handling this with pubnub for now
        mHandler.postDelayed(runnable, 5000);


        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        mChatLogList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.requestDisallowInterceptTouchEvent(true);

                int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_UP:
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                }

                return false;
            }
        });


        try {
            pubnub.subscribe("conversation_"+mConversationId, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    // message should have
                    // { conversationId, conversationFileId, chatMessage, senderId, senderName, sentTime }

                    JSONObject msg = (JSONObject) message;
                    String chatMessageId = msg.optString("chatMessageId");
                    String conversationId = msg.optString("conversationId");
                    String conversationFileId = msg.optString("conversationFileId");
                    String chatMessage = msg.optString("chatMessage");

                    System.out.println("Channel: " + channel + " Message: " + chatMessage);

                    if ( !conversationFileId.equals(mCurrentFileId) ) return;

                    // Get Parse Object

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatMessage");
                    // where conversation ID is this
                    //query.whereEqualTo("conversationId", mConversationId);
                    //query.whereEqualTo("conversationFileId", mCurrentFileId);
                    query.whereEqualTo("objectId", chatMessageId);

                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(final ParseObject parseObject, ParseException e) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mChatMessages.add(parseObject);
                                    mChatLogAdapter.notifyDataSetChanged();

                                    if (mNewMessageSent) {
                                        //mChatLogList.smoothScrollToPosition( mChatLogAdapter.getCount() - 1 );
                                        mChatLogList.setSelection(mChatLogAdapter.getCount() - 1);
                                        mNewMessageSent = false;
                                    }
                                }
                            });
                        }
                    });
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Log.d(TAG, "Main thread: " + PycApplication.isMainThread());

            getChatMessages();
            mHandler.postDelayed(this, 5000);
        }
    };

    public void getChatMessages() {
        // TODO:
        // Optimization so that when someone types
        // and sends message then

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatMessage");
        // where conversation ID is this
        query.whereEqualTo("conversationId", mConversationId);
        query.whereEqualTo("conversationFileId", mCurrentFileId);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // Log.d(TAG, String.valueOf(parseObjects.size()));
                final ArrayList<ParseObject> chatMessages = new ArrayList<ParseObject>();

                for (ParseObject chatMessage : parseObjects) {
                    chatMessages.add(chatMessage);
                }

                // so the takehome is dont touch your adapter data
                // outside of the main thread, and always call
                // notifydatasetchanged immediately afterwards
                runOnUiThread(new Runnable(){
                    public void run() {
                        mChatMessages.clear();
                        mChatMessages.addAll(chatMessages);
                        mChatLogAdapter.notifyDataSetChanged();

                        if (mNewMessageSent) {
                            //mChatLogList.smoothScrollToPosition( mChatLogAdapter.getCount() - 1 );
                            mChatLogList.setSelection(mChatLogAdapter.getCount() - 1);
                            mNewMessageSent = false;
                        }
                        else {

                        }
                    }
                });
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

            ParseObject chatMessage = mChatMessages.get(position);

            TextView chatMessageView = (TextView) view.findViewById(R.id.chatMessage);
            chatMessageView.setText(chatMessage.getString("message"));

            // Convert DP to px
            int sizeInDp = 50;
            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (sizeInDp*scale + 0.5f);
            int dpAsPixels2 = (int) (5*scale + 0.5f);

            // If sender then show message on the right side
            if (chatMessage.getString("sender").equals(ParseUser.getCurrentUser().getObjectId())) {
                // right align text
                //chatMessageView.setGravity(Gravity.END);

                // set left margin
                RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(dpAsPixels, dpAsPixels2, dpAsPixels2, dpAsPixels2);
                // right align text
                llp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                chatMessageView.setLayoutParams(llp);

                chatMessageView.setBackgroundColor(Color.parseColor("#DEEED1"));
            }
            else {
                // left alignt text
                // chatMessageView.setGravity(Gravity.START);

                // set right margin
                RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(dpAsPixels2, dpAsPixels2, dpAsPixels, dpAsPixels2);
                // left align text
                llp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                chatMessageView.setLayoutParams(llp);

                chatMessageView.setBackgroundColor(Color.parseColor("#D6CDC4"));
            }

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
