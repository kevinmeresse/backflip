package com.km.backflip.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.km.backflip.R;
import com.parse.ParseFacebookUtils;

public class NewMomentActivity extends FragmentActivity {
	
	public static final String TAG = NewMomentActivity.class.getSimpleName();
	private Bitmap currentPhoto = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		
		// Set the layout
		setContentView(R.layout.activity_new_moment);
		
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment = manager.findFragmentById(R.id.newMomentContainer);

		if (fragment == null) {
			fragment = new CameraFragment();
			manager.beginTransaction().add(R.id.newMomentContainer, fragment)
					.commit();
		}
	}
	
	public Bitmap getCurrentPhoto() {
		return currentPhoto;
	}
	
	public void setCurrentPhoto(Bitmap photo) {
		currentPhoto = photo;
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "On activity result... Request code: "+requestCode);
		super.onActivityResult(requestCode, resultCode, data);
	    try {
	    	Log.d(TAG, "Finishing Facebook authentication....");
			ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
		} catch (Exception e) {
			Crashlytics.logException(e);
			Log.e(TAG, "Error: Couldn't finish Facebook authentication.");
		}
	}
}
