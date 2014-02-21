package com.km.backfront.ui;

import java.net.URISyntaxException;

import com.km.backfront.R;
import com.km.backfront.util.Utils;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableRow;

public class SettingsActivity extends FragmentActivity {
	
	private ImageButton feedButton;
	private ImageButton cameraButton;
	private ImageButton settingsButton;
	private TableRow settingsProfile;
	private TableRow settingsFollowers;
	private TableRow settingsFollowing;
	private TableRow settingsNotifications;
	private TableRow settingsLogout;
	private TableRow settingsSignup;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		
		// Set the layout
		setContentView(R.layout.activity_settings);
		
		// Get the view objects
		feedButton = (ImageButton) findViewById(R.id.menu_feed_button);
		cameraButton = (ImageButton) findViewById(R.id.menu_add_button);
		settingsButton = (ImageButton) findViewById(R.id.menu_settings_button);
		settingsProfile = (TableRow) findViewById(R.id.settings_profile);
		settingsFollowers = (TableRow) findViewById(R.id.settings_followers);
		settingsFollowing = (TableRow) findViewById(R.id.settings_following);
		settingsNotifications = (TableRow) findViewById(R.id.settings_notifications);
		settingsLogout = (TableRow) findViewById(R.id.settings_logout);
		settingsSignup = (TableRow) findViewById(R.id.settings_signup);
		
		// Action: Go to feed page
		feedButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	finish();
		    }
		});
		
		// Action: Go to camera page
		cameraButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Intent intent = new Intent(v.getContext(), NewMomentActivity.class);
		    	startActivityForResult(intent, 0);
		    }
		});
		
		// Action: Go to settings page
		settingsButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Do nothing
		    }
		});
		
		// Action: Profile
		settingsProfile.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (Utils.userLoggedIn(v.getContext())) {
		    		Intent intent = new Intent(v.getContext(), ProfileActivity.class);
		    		startActivity(intent);
		    	}
		    }
		});
		
		// Action: Followers
		settingsFollowers.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (Utils.userLoggedIn(v.getContext())) {
		    		Intent intent = new Intent(v.getContext(), FollowersActivity.class);
		    		startActivity(intent);
		    	}
		    }
		});
		
		// Action: Following
		settingsFollowing.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (Utils.userLoggedIn(v.getContext())) {
		    		Intent intent = new Intent(v.getContext(), FollowingActivity.class);
		    		startActivity(intent);
		    	}
		    }
		});
		
		// Action: Following
		settingsNotifications.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (Utils.userLoggedIn(v.getContext())) {
		    		Intent intent = new Intent(v.getContext(), ManageNotificationsActivity.class);
		    		startActivity(intent);
		    	}
		    }
		});
		
		// Action: Logout
		settingsLogout.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	ParseUser.logOut();
		    	Utils.showToast(SettingsActivity.this, "You have been logged out!");
		    	finish();
		    }
		});
		
		// Action: Signup
		settingsSignup.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Intent intent = new Intent(v.getContext(), SignUpActivity.class);
		    	startActivityForResult(intent, 0);
		    }
		});
		
		// Action: Help
		findViewById(R.id.settings_help).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Intent intent = new Intent(v.getContext(), HelpActivity.class);
		    	startActivity(intent);
		    }
		});
		
		// Action: Report a problem
		findViewById(R.id.settings_problem).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	try {
			    	Intent intent = Intent.parseUri("mailto:support@bckflp.co?subject=Backflip - Report a problem", Intent.URI_INTENT_SCHEME);
				    startActivity(intent);
		    	} catch (URISyntaxException e) {
		    		Utils.showToast(SettingsActivity.this, "Sorry, something went wrong...");
		    	}
		    }
		});
		
		// Action: Rate the app
		findViewById(R.id.settings_rate).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Uri uri = Uri.parse("market://details?id=" + getPackageName());
		        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		        try {
		            startActivity(goToMarket);
		        } catch (ActivityNotFoundException e) {
		        	Utils.showToast(SettingsActivity.this, "Couldn't launch the market...");
		        }
		    }
		});
    }
    
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			finish();
		}
	}
    
    @Override
	public void onPause() {
    	overridePendingTransition(0, 0);
    	super.onPause();
    }
    
    @Override
    protected void onResume() {
    	// Check if user is logged in
		if (ParseUser.getCurrentUser() != null) {
			settingsSignup.setVisibility(View.GONE);
			settingsLogout.setVisibility(View.VISIBLE);
		} else {
			settingsLogout.setVisibility(View.GONE);
			settingsSignup.setVisibility(View.VISIBLE);
		}
    	super.onResume();
    }
}