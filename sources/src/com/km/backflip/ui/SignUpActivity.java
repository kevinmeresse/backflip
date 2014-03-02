package com.km.backflip.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.km.backflip.util.Utils;
import com.km.backflip.R;

/**
 * Activity which displays a login screen to the user.
 */
public class SignUpActivity extends Activity {
	
	protected static final String TAG = "SignUpActivity";
	
	// UI references.
	private EditText usernameView;
	private EditText passwordView;
	private EditText emailView;
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    
		// Remove the title bar
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

		// Set the layout
		setContentView(R.layout.activity_signup);

		// Get the view objects
		usernameView = (EditText) findViewById(R.id.signup_form_username);
		passwordView = (EditText) findViewById(R.id.signup_form_password);
		emailView = (EditText) findViewById(R.id.signup_form_email);
    
		// Action: go to login screen
		findViewById(R.id.signup_top_bar_login).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), LoginActivity.class);
				startActivityForResult(intent, 0);
			}
		});
    
		// Action: go to the Terms Of Service screen
		findViewById(R.id.signup_tos_link).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), TosActivity.class);
				startActivityForResult(intent, 0);
			}
		});
    
		// Action: sign up
		findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				// Validate the sign up data
				boolean validationError = false;
				StringBuilder validationErrorMessage = new StringBuilder(getResources().getString(R.string.error_intro));
				
				// Check username
				if (isEmpty(usernameView)) {
					validationError = true;
					validationErrorMessage.append(getResources().getString(R.string.error_blank_username));
				} else if (!Utils.isUsernameLongEnough(usernameView.getText().toString())) {
					validationError = true;
					validationErrorMessage.append(getResources().getString(R.string.error_short_username));
				} else if (!Utils.isAlphanumeric(usernameView.getText().toString())) {
					validationError = true;
					validationErrorMessage.append(getResources().getString(R.string.error_alphanumeric_username));
				}
				
				// Check password
				if (isEmpty(passwordView)) {
					if (validationError) {
						validationErrorMessage.append(getResources().getString(R.string.error_join));
					}
					validationError = true;
					validationErrorMessage.append(getResources().getString(R.string.error_blank_password));
				}
				
				// Check email
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
					Utils.showToast(SignUpActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG);
					return;
				}

				// Set up a progress dialog
				final ProgressDialog dlg = new ProgressDialog(SignUpActivity.this);
				dlg.setTitle("Please wait.");
				dlg.setMessage("Signing up.  Please wait.");
				dlg.show();

				// Check is user already logged in
				ParseUser currentUser = ParseUser.getCurrentUser();
				if (currentUser != null) {
					ParseUser.logOut();
				}
        
				// Set up a new Parse user
				ParseUser newUser = new ParseUser();
				newUser.setUsername(usernameView.getText().toString().toLowerCase());
				newUser.setPassword(passwordView.getText().toString());
				newUser.setEmail(emailView.getText().toString().toLowerCase());
				newUser.put("notifyFollow", true);
				newUser.put("notifyLike", true);
				
				// Call the Parse sign up method
				try {
					newUser.signUpInBackground(new SignUpCallback() {
	
						@Override
						public void done(ParseException e) {
							dlg.dismiss();
							if (e != null) {
								// Show the error message
								Log.e(TAG, "Couldn't save user data to server: " + e.getMessage());
								Utils.showToast(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG);
							} else {
								// Link this user to the device installation for Push Notifications
								ParseInstallation installation = ParseInstallation.getCurrentInstallation();
								installation.put("user", ParseUser.getCurrentUser());
								installation.saveInBackground();
								// Return
								setResult(Activity.RESULT_OK);
								finish();
							}
						}
					});
				} catch (Exception e) {
					dlg.dismiss();
					Log.e(TAG, "Couldn't save user data to server: " + e.getMessage());
					Utils.showToast(SignUpActivity.this, "We couldn't sign you up. Please try again...", Toast.LENGTH_LONG);
					e.printStackTrace();
				}
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
