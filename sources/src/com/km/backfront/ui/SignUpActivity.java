package com.km.backfront.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import com.km.backfront.R;
import com.km.backfront.util.Utils;

/**
 * Activity which displays a login screen to the user.
 */
public class SignUpActivity extends Activity {
  // UI references.
  private EditText usernameView;
  private EditText passwordView;
  private EditText emailView;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

    setContentView(R.layout.activity_signup);

    // Set up the signup form.
    usernameView = (EditText) findViewById(R.id.signup_form_username);
    passwordView = (EditText) findViewById(R.id.signup_form_password);
    emailView = (EditText) findViewById(R.id.signup_form_email);
    
    // Set up the login button click handler
    findViewById(R.id.signup_top_bar_login).setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(v.getContext(), LoginActivity.class);
	    	startActivityForResult(intent, 0);
        }
    });
    
    // Set up the link to the Terms Of Service
    findViewById(R.id.signup_tos_link).setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(v.getContext(), TosActivity.class);
	    	startActivityForResult(intent, 0);
        }
    });
    
    // Set up the submit button click handler
    findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {

        // Validate the sign up data
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
        if (isEmpty(emailView)) {
            if (validationError) {
              validationErrorMessage.append(getResources().getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.error_blank_email));
        } else if (!Utils.isValidEmailAddress(emailView.getText().toString())) {
        	if (validationError) {
        		validationErrorMessage.append(getResources().getString(R.string.error_join));
        	}
        	validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.error_wrong_email));
        }
        validationErrorMessage.append(getResources().getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
          Toast.makeText(SignUpActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
              .show();
          return;
        }

        // Set up a progress dialog
        final ProgressDialog dlg = new ProgressDialog(SignUpActivity.this);
        dlg.setTitle("Please wait.");
        dlg.setMessage("Signing up.  Please wait.");
        dlg.show();

        ParseUser currentUser = ParseUser.getCurrentUser();
    	try {
			currentUser.save();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        // Set up a new Parse user
        //ParseUser user = new ParseUser();
    	currentUser.setUsername(usernameView.getText().toString());
        currentUser.setPassword(passwordView.getText().toString());
        currentUser.setEmail(emailView.getText().toString());
        // Call the Parse signup method
        currentUser.saveInBackground(new SaveCallback() {

          @Override
          public void done(ParseException e) {
            dlg.dismiss();
            if (e != null) {
              // Show the error message
              Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } else {
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
  
  @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

}
