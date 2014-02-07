package com.km.backfront.util;

import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.util.Log;

import com.parse.ParseTwitterUtils;

public class PostPhotoOnTwitterRunnable implements Runnable {
	
	private static final String TAG = "PostPhotoOnTwitterRunnable";
	private Activity activity = null;
	private String momentId = "";
	private String photoCaption = "";

	public PostPhotoOnTwitterRunnable(Activity activity, String momentId, String photoCaption) {
		// store parameter for later user
		this.activity = activity;
		this.momentId = momentId;
		this.photoCaption = photoCaption;
   }

	@Override
	public void run() {
		// Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        
        try {
	        HttpClient client = new DefaultHttpClient();
	        String message = "";
	        if (!Utils.isEmptyString(photoCaption)) {
	        	message = photoCaption+" - bckflp.co/"+momentId;
	        } else {
	        	message = "bckflp.co/"+momentId;
	        }
	        String encodedMessage = URLEncoder.encode(message, "utf-8");
	        Log.d(TAG, "Encoded message: "+ encodedMessage);
	        HttpPost verifyPost = new HttpPost("https://api.twitter.com/1.1/statuses/update.json?status="+encodedMessage);
	        ParseTwitterUtils.getTwitter().signRequest(verifyPost);
	        HttpResponse response = client.execute(verifyPost);
	        // Handle errors
	        if (response.getStatusLine().getStatusCode() != 200) {
	        	Log.e(TAG, "HTTP post error: "+ response.getStatusLine().getStatusCode()+" - "+response.getStatusLine().getReasonPhrase());
	        	Utils.showToast(this.activity, "An error occured while posting photo on Twitter...");
	        }
		} catch (Exception e) {
	    	Log.e(TAG, "Unknown error: "+ e.getMessage());
	    	e.printStackTrace();
	    	Utils.showToast(this.activity, "An error occured while posting photo on Twitter...");
	    }
	}
}
