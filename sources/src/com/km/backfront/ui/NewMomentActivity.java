package com.km.backfront.ui;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.WindowManager;

import com.km.backfront.R;
import com.km.backfront.util.BitmapHelper;

public class NewMomentActivity extends FragmentActivity {
	
	private Bitmap currentPhoto = null;
	//private Bitmap photoBack = null;
	//private Bitmap photoFront = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		
		// Set the layout
		setContentView(R.layout.activity_new_moment);
		
		// Make this activity fullscreen
		/*if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
        	View decorView = getWindow().getDecorView();
        	// Hide the status bar.
        	int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        	decorView.setSystemUiVisibility(uiOptions);
        }*/
		
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
	
	/*public Bitmap getPhotoBack() {
		return photoBack;
	}
	
	public void setPhotoBack(Bitmap photo) {
		photoBack = photo;
	}
	
	public Bitmap getPhotoFront() {
		return photoFront;
	}
	
	public void setPhotoFront(Bitmap photo) {
		photoFront = photo;
	}
	
	public void mergePhotos() {
		currentPhoto = BitmapHelper.mergeImages(photoBack, photoFront);
	}*/
}
