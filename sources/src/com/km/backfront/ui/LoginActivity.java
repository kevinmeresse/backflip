package com.km.backfront.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import com.km.backfront.R;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class LoginActivity extends Activity {
  // UI references.
  private EditText usernameView;
  private EditText passwordView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

    setContentView(R.layout.activity_login);

    // Set up the login form.
    usernameView = (EditText) findViewById(R.id.signup_form_username);
    passwordView = (EditText) findViewById(R.id.signup_form_password);

    // Set up the signup button click handler
    findViewById(R.id.signup_top_bar_login).setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	finish();
        }
    });
    
    // Set up the submit button click handler
    findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        // Validate the log in data
        boolean validationError = false;
        StringBuilder validationErrorMessage =
            new StringBuilder(getResources().getString(R.string.error_intro));
        if (isEmpty(usernameView)) {
          validationError = true;
          validationErrorMessage.append(getResources().getString(R.string.error_blank_username));
        }
        if (isEmpty(passwordView)) {
          if (validationError) {
            validationErrorMessage.append(getResources().getString(R.string.error_join));
          }
          validationError = true;
          validationErrorMessage.append(getResources().getString(R.string.error_blank_password));
        }
        validationErrorMessage.append(getResources().getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
          Toast.makeText(LoginActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
              .show();
          return;
        }

        // Set up a progress dialog
        final ProgressDialog dlg = new ProgressDialog(LoginActivity.this);
        dlg.setTitle("Please wait.");
        dlg.setMessage("Logging in.  Please wait.");
        dlg.show();
        // Call the Parse login method
        ParseUser.logInInBackground(usernameView.getText().toString(), passwordView.getText()
            .toString(), new LogInCallback() {

          @Override
          public void done(ParseUser user, ParseException e) {

        	  dlg.dismiss();
        	  if (e != null) {
        		  // Show the error message
        		  Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        	  } else {
        		// Link this user to the device installation for Push Notifications
            	  ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            	  installation.put("user", user);
            	  installation.saveInBackground();
            	  // Return
        		  setResult(Activity.RESULT_OK);
        		  finish();
        	  }
          }
        });
      }
    });
  }

  private boolean isEmpty(EditText etText) {
    if (etText.getText().toString().trim().length() > 0) {
      return false;
    } else {
      return true;
    }
  }
}
