package com.pycitup.pyc;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactsActivity extends Activity {

    String[] mProjection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        mProjection = new String[] {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        final Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                mProjection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        int nameIndex = cursor.getColumnIndex(mProjection[0]);
        int numberIndex = cursor.getColumnIndex(mProjection[1]);

        ArrayList contacts = new ArrayList();

        int position = 0;
        boolean isSeparator = false;
        while(cursor.moveToNext()) {
            isSeparator = false;

            String name = cursor.getString(nameIndex);
            String number = cursor.getString(numberIndex);

            char[] nameArray;

            // If it is the first item then need a separator
            if (position == 0) {
                isSeparator = true;
                nameArray = name.toCharArray();
            }
            else {
                // Move to previous
                cursor.moveToPrevious();

                // Get the previous contact's name
                String previousName = cursor.getString(nameIndex);

                // Convert the previous and current contact names
                // into char arrays
                char[] previousNameArray = previousName.toCharArray();
                nameArray = name.toCharArray();

                // Compare the first character of previous and current contact names
                if (nameArray[0] != previousNameArray[0]) {
                    isSeparator = true;
                }

                // Don't forget to move to next
                // which is basically the current item
                cursor.moveToNext();
            }

            // Need a separator? Then create a Contact
            // object and save it's name as the section
            // header while pass null as the phone number
            if (isSeparator) {
                Contact contact = new Contact(String.valueOf(nameArray[0]), null, isSeparator);
                contacts.add( contact );
            }

            // Create a Contact object to store the name/number details
            Contact contact = new Contact(name, number, false);
            contacts.add( contact );

            position++;
        }

        // Creating our custom adapter
        CustomAdapter adapter = new CustomAdapter(this, contacts);

        // Create the list view and bind the adapter
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
    }

    public class Contact {
        public String mName;
        public String mNumber;
        public boolean mIsSeparator;

        public Contact(String name, String number, boolean isSeparator) {
            mName = name;
            mNumber = number;
            mIsSeparator = isSeparator;
        }

        public void setName(String name) {
            mName = name;
        }

        public void setNumber(String number) {
            mNumber = number;
        }

        public void setIsSection(boolean isSection) {
            mIsSeparator = isSection;
        }
    }

    public class CustomAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<Contact> mList;

        // View Type for Separators
        private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
        // View Type for Regular rows
        private static final int ITEM_VIEW_TYPE_REGULAR = 1;
        // Types of Views that need to be handled
        // -- Separators and Regular rows --
        private static final int ITEM_VIEW_TYPE_COUNT = 2;

        public CustomAdapter(Context context, ArrayList list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_VIEW_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            boolean isSeparator = mList.get(position).mIsSeparator;

            if (isSeparator) {
                return ITEM_VIEW_TYPE_SEPARATOR;
            }
            else {
                return ITEM_VIEW_TYPE_REGULAR;
            }
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;

            Contact contact = mList.get(position);
            int itemViewType = getItemViewType(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
                    // If its a section ?
                    view = inflater.inflate(R.layout.contact_section_header, null);
                }
                else {
                    // Regular row
                    view = inflater.inflate(R.layout.contact_item, null);
                }
            }
            else {
                view = convertView;
            }


            if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
                // If separator

                TextView separatorView = (TextView) view.findViewById(R.id.separator);
                separatorView.setText(contact.mName);
            }
            else {
                // If regular

                // Set contact name and number
                TextView contactNameView = (TextView) view.findViewById(R.id.contact_name);
                TextView phoneNumberView = (TextView) view.findViewById(R.id.phone_number);

                contactNameView.setText( contact.mName );
                phoneNumberView.setText( contact.mNumber );
            }

            return view;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contacts, menu);
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
