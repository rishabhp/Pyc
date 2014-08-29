package com.pycitup.pyc;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        int nameIndex = cursor.getColumnIndex(projection[0]);
        int numberIndex = cursor.getColumnIndex(projection[1]);

        ArrayList contacts = new ArrayList();

        while(cursor.moveToNext()) {
            String name = cursor.getString(nameIndex);
            String number = cursor.getString(numberIndex);

            Contact contact = new Contact(name, number);
            contacts.add( contact );
        }

        CustomAdapter adapter = new CustomAdapter(this, contacts);

        // Create the list view and bind the adapter
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
    }

    public class Contact {
        public String mName;
        public String mNumber;

        public Contact(String name, String number) {
            mName = name;
            mNumber = number;
        }

        public void setName(String name) {
            mName = name;
        }

        public void setNumber(String number) {
            mNumber = number;
        }
    }

    public class CustomAdapter extends ArrayAdapter {

        private Context mContext;
        private ArrayList<Contact> mList;

        public CustomAdapter(Context context, ArrayList list) {
            super(context, R.layout.contact_item, list);

            mContext = context;
            mList = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.contact_item, null);
            }
            else {
                view = convertView;
            }

            TextView contactNameView = (TextView) view.findViewById(R.id.contact_name);
            TextView phoneNumberView = (TextView) view.findViewById(R.id.phone_number);

            contactNameView.setText( mList.get(position).mName );
            phoneNumberView.setText( mList.get(position).mNumber );

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
