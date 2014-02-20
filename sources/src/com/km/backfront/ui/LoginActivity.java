package com.km.backfront.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import com.km.backfront.R;
import com.km.backfront.util.Utils;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class LoginActivity extends Activity {
	
	protected static final String TAG = "LoginActivity";
	
	// UI references.
	private EditText usernameView;
	private EditText passwordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    
		// Remove the title bar
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

		// Set the layout
		setContentView(R.layout.activity_login);

		// Get the view objects
		usernameView = (EditText) findViewById(R.id.signup_form_username);
		passwordView = (EditText) findViewById(R.id.signup_form_password);

		// Action: go back to signup screen
		findViewById(R.id.signup_top_bar_login).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
    
		// Action: login
		findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// Validate the login data
				boolean validationError = false;
				StringBuilder validationErrorMessage = new StringBuilder(getResources().getString(R.string.error_intro));
				
				// Check username
				if (isEmpty(usernameView)) {
					validationError = true;
					validationErrorMessage.append(getResources().getString(R.string.error_blank_username));
				}
				
				// Check password
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
					Utils.showToast(LoginActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG);
					return;
				}

				// Set up a progress dialog
				final ProgressDialog dlg = new ProgressDialog(LoginActivity.this);
				dlg.setTitle("Please wait.");
				dlg.setMessage("Logging in.  Please wait.");
				dlg.show();
				
				// Call the Parse login method
				ParseUser.logInInBackground(usernameView.getText().toString().toLowerCase(), passwordView.getText().toString(), new LogInCallback() {

					@Override
					public void done(ParseUser user, ParseException e) {

						dlg.dismiss();
						if (e != null) {
							// Show the error message
							Log.e(TAG, "Couldn't log you in: " + e.getMessage());
							Utils.showToast(LoginActivity.this, "We couldn't log you in. Please try again...", Toast.LENGTH_LONG);
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
