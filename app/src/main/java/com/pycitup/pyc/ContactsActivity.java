package com.pycitup.pyc;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ContactsActivity extends Activity {

    String[] mProjection;
    CustomAdapter mAdapter;
    ArrayList<Contact> mContacts;

    Menu mMenu;

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

        mContacts = new ArrayList();

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
                contact.setId(position);
                mContacts.add( contact );

                position++;
            }

            // Create a Contact object to store the name/number details
            Contact contact = new Contact(name, number, false);
            contact.setId(position);
            mContacts.add( contact );

            position++;
        }

        // Creating our custom adapter
        mAdapter = new CustomAdapter(this, mContacts);

        // Create the list view and bind the adapter
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(((ListView) parent).isItemChecked(position));
            }
        });
    }

    public class Contact {
        public String mName;
        public String mNumber;
        public boolean mIsSeparator;
        public int mId;

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

        public void setId(int id) {
            mId = id;
        }
    }

    public class CustomAdapter extends BaseAdapter implements Filterable {

        private Context mContext;
        private ArrayList<Contact> mList;

        // View Type for Separators
        private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
        // View Type for Regular rows
        private static final int ITEM_VIEW_TYPE_REGULAR = 1;
        // Types of Views that need to be handled
        // -- Separators and Regular rows --
        private static final int ITEM_VIEW_TYPE_COUNT = 2;

        ContactsFilter mContactsFilter;

        private long[] mCheckedItemIds;

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
            return mList.get(position).mId;
        }

        @Override
        public boolean hasStableIds() {
            return true;
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
            return false;
            //return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

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


                CheckedTextView contactSelection = (CheckedTextView) view.findViewById(R.id.contactSelection);

                final ListView listView = (ListView) parent;

                long itemId = getItemId(position);

                // if (listView.isItemChecked(position)) {

                // contactSelection.setChecked(false);

                if (mCheckedItemIds != null) {
                    if (Arrays.asList(ArrayUtils.toObject(mCheckedItemIds)).contains(itemId)) {
                        contactSelection.setChecked(true);
                    } else {
                        contactSelection.setChecked(false);
                    }
                }

                /*if (mCheckedItemIds != null) {
                    for (long item : mCheckedItemIds) {
                        if (item == itemId) {
                            contactSelection.setChecked(true);
                            break;
                        }
                    }
                }*/

                /*if (Arrays.asList(mCheckedItemIds).contains(itemId)) {
                    contactSelection.setChecked(true);
                }
                else {
                    contactSelection.setChecked(false);
                }*/

                contactSelection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listView.isItemChecked(position)) {
                            listView.setItemChecked(position, false);
                            ((CheckedTextView) v).setChecked(false);

                            mCheckedItemIds = ArrayUtils.removeElement(mCheckedItemIds, getItemId(position));
                        } else {
                            listView.setItemChecked(position, true);
                            ((CheckedTextView) v).setChecked(true);
                        }

                        // System.out.println(listView.isItemChecked(position));
                        if (mCheckedItemIds == null) {
                            mCheckedItemIds = listView.getCheckedItemIds();
                        }
                        else {
                            Long[] longObjectArray = ArrayUtils.toObject(mCheckedItemIds);
                            Set<Long> set = new LinkedHashSet<Long>( Arrays.asList(longObjectArray) );

                            Long[] newLongObjectArray = ArrayUtils.toObject(listView.getCheckedItemIds());
                            set.addAll( Arrays.asList(newLongObjectArray) );

                            mCheckedItemIds = ArrayUtils.toPrimitive( set.toArray(new Long[0]) );
                        }

                        if (mCheckedItemIds.length > 0) {
                            mMenu.findItem(R.id.share).setVisible(true);
                        }
                        else {
                            mMenu.findItem(R.id.share).setVisible(false);
                        }
                    }
                });
            }

            return view;
        }

        @Override
        public Filter getFilter() {
            if (mContactsFilter == null)
                mContactsFilter = new ContactsFilter();

            return mContactsFilter;
        }

        // Filter

        private class ContactsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // Create a FilterResults object
                FilterResults results = new FilterResults();

                // If the constraint (search string/pattern) is null
                // or its length is 0, i.e., its empty then
                // we just set the `values` property to the
                // original contacts list which contains all of them
                if (constraint == null || constraint.length() == 0) {
                    results.values = mContacts;
                    results.count = mContacts.size();
                }
                else {
                    // Some search copnstraint has been passed
                    // so let's filter accordingly
                    ArrayList<Contact> filteredContacts = new ArrayList<Contact>();

                    // We'll go through all the contacts and see
                    // if they contain the supplied string
                    for (Contact c : mContacts) {
                        if (c.mName.toUpperCase().contains( constraint.toString().toUpperCase() )) {
                            // if `contains` == true then add it
                            // to our filtered list
                            filteredContacts.add(c);
                        }
                    }

                    // Finally set the filtered values and size/count
                    results.values = filteredContacts;
                    results.count = filteredContacts.size();
                }

                // Return our FilterResults object
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mList = (ArrayList<Contact>) results.values;
                notifyDataSetChanged();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contacts, menu);

        mMenu = menu;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()) );

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true; // handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                mAdapter.getFilter().filter(newText);

                return true;
            }
        });

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
        else if (id == R.id.share) {
            // We have the absolute position data from which we can get contacts

            // We need images data too that needs to be shared
            ArrayList<Integer> imageIDs = getIntent().getExtras().getIntegerArrayList("imageIDs");
            for (int i : imageIDs) {
                // System.out.println("intent: " + i);
            }
        }

        return super.onOptionsItemSelected(item);
    }

}