package com.km.backfront.util;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class PostPhotoOnInstagramRunnable implements Runnable {
	
	private static final String TAG = "PostPhotoOnInstagramRunnable";
	private Activity activity = null;
	
	public PostPhotoOnInstagramRunnable(Activity activity) {
		// store parameter for later user
		this.activity = activity;
	}

	@Override
	public void run() {
		// Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage("com.instagram.android");
        if (intent != null) {
        	Log.d(TAG, "User has Instagram app installed :-)");
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setPackage("com.instagram.android");
            String filePath = activity.getCacheDir() + "/full.jpg";
            try {
				shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(activity.getContentResolver(), filePath, "", "")));
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Couldn't read file from Cache folder...");
				e.printStackTrace();
			}
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("image/*");
            activity.startActivity(shareIntent);
        } else {
        	Log.d(TAG, "User doesn't have Instagram app installed. Redirecting to the market...");
            // Bring user to the market to download the app.
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + "com.instagram.android"));
            activity.startActivity(intent);
        }
	}
}
