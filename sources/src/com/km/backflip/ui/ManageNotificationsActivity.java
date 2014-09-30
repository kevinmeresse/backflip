package com.km.backflip.ui;

import com.crashlytics.android.Crashlytics;
import com.km.backflip.R;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.ToggleButton;

public class ManageNotificationsActivity extends Activity {

	public static final String TAG = ManageNotificationsActivity.class.getSimpleName();

	
	private ImageButton topBarIconButton;
	private Button topBarTextButton;
	private Switch followSwitch;
	private Switch likeSwitch;
	private ToggleButton followToggle;
	private ToggleButton likeToggle;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    
		// Remove the title bar
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

		// Set the layout
		setContentView(R.layout.activity_manage_notifications);
		
		// Get the view objects
		topBarIconButton = (ImageButton) findViewById(R.id.top_bar_icon);
    	topBarTextButton = (Button) findViewById(R.id.top_bar_text);
    	
    	// Get preferences
    	SharedPreferences pref = getPreferences(MODE_PRIVATE);
    	boolean notifyLike = pref.getBoolean("notifyLike", true);
    	boolean notifyFollow = pref.getBoolean("notifyFollow", true);
    	
    	// Set values to views
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    		followToggle = (ToggleButton) findViewById(R.id.follow_toggle);
    		likeToggle = (ToggleButton) findViewById(R.id.like_toggle);
    		followToggle.setChecked(notifyFollow);
    		likeToggle.setChecked(notifyLike);
    		
    		// Action: click on follow Toggle
        	followToggle.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	saveNotificationPreferences();
    		    }
        	});
        	
        	// Action: click on like Toggle
        	likeToggle.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	saveNotificationPreferences();
    		    }
        	});
    	} else {
    		followSwitch = (Switch) findViewById(R.id.follow_switch);
    		likeSwitch = (Switch) findViewById(R.id.like_switch);
    		followSwitch.setChecked(notifyFollow);
    		likeSwitch.setChecked(notifyLike);
    		
    		// Action: click on follow Switch
        	followSwitch.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	saveNotificationPreferences();
    		    }
        	});
        	
        	// Action: click on like Switch
        	likeSwitch.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	saveNotificationPreferences();
    		    }
        	});
    	}
    	
    	// Action: click on top bar icon
    	topBarIconButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	finish();
		    }
    	});
    	
    	// Action: click on top bar text
    	topBarTextButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	finish();
		    }
    	});
    	
    	// Save pref to Parse
    	saveNotificationPreferences();
	}
	
	@SuppressLint("NewApi")
	public void saveNotificationPreferences() {
		boolean notifyFollow;
		boolean notifyLike;
		
		// Retrieve values according to the view used
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			notifyFollow = followToggle.isChecked();
			notifyLike = likeToggle.isChecked();
		} else {
			notifyFollow = followSwitch.isChecked();
			notifyLike = likeSwitch.isChecked();
		}
		
		// Save in preferences
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
	    SharedPreferences.Editor editor = pref.edit();
	    editor.putBoolean("notifyFollow", notifyFollow);
	    editor.putBoolean("notifyLike", notifyLike);
	    editor.commit();
	    
	    // Save in Parse
	    try {
	    	ParseUser currentUser = ParseUser.getCurrentUser();
		    currentUser.put("notifyFollow", notifyFollow);
		    currentUser.put("notifyLike", notifyLike);
			currentUser.save();
		} catch (ParseException e) {
			Crashlytics.logException(e);
			e.printStackTrace();
		}
	}
}
