package com.pycitup.pyc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText firstNameView = (EditText) findViewById(R.id.firstName);
        final EditText lastNameView = (EditText) findViewById(R.id.lastName);
        final Spinner countryView = (Spinner) findViewById(R.id.country);
        final EditText countryCodeView = (EditText) findViewById(R.id.countryCode);
        final EditText phoneNumberView = (EditText) findViewById(R.id.phoneNumber);
        Button loginButtonView = (Button) findViewById(R.id.loginButton);

        // Spinner Items
        ArrayList<String> countries = new ArrayList<String>();
        countries.add("India");
        countries.add("United States");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        countryView.setAdapter(adapter);


        loginButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String phoneNumber = phoneNumberView.getText().toString().trim();
                String firstName = firstNameView.getText().toString().trim();
                String lastName = lastNameView.getText().toString().trim();
                String countryCode = countryCodeView.getText().toString().trim();
                String country = countryView.getSelectedItem().toString().trim();

                if (phoneNumber.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || countryCode.isEmpty() || country.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Please make sure you entered all the fields correctly.")
                            .setTitle("Oops!")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return;
                }

                final ParseUser user = new ParseUser();

                user.setUsername(phoneNumber);
                user.setPassword("Fake Password");
                user.put("firstName", firstName);
                user.put("lastName", lastName);
                user.put("country", country);
                user.put("countryCode", countryCode);

                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", phoneNumber);
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> parseUsers, ParseException e) {

                        if (e == null) {
                            // Successful Query
                            System.out.println(parseUsers.size());
                            // User already exists ?
                            if (parseUsers.size() > 0) {
                                loginUser(phoneNumber, "Fake Password");
                            }
                            else {
                                signupUser(user);
                            }
                        }
                        else {
                            // Shit happened!
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage(e.getMessage())
                                    .setTitle("Oops!")
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
            }
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.

                    navigateToHome();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                }
            }
        });
    }

    private void signupUser(ParseUser user) {
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Signup successful!

                    navigateToHome();
                } else {
                    // Fail!
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle("Oops!")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void navigateToHome() {
        // Let's go to the MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
