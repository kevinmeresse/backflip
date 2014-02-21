package com.km.backfront.util.twitter;

import android.app.Activity;
import android.util.Log;

import com.km.backfront.util.PublishCallback;
import com.km.backfront.util.Utils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class TwitterUtils {
	
	protected static final String TAG = "PathUtils";
	
	public static String getMomentWebpageUrl(String momentId) {
		return "bckflp.co/"+momentId;
	}
	
	public static String getPublishPhotoUri(String message) {
		return "https://api.twitter.com/1.1/statuses/update.json?status=" + message;
	}
	
	public static boolean login(Activity activity, SaveCallback callback) {
		Log.d(TAG, "Clicked on Share on Twitter.");
    	ParseUser currentUser = ParseUser.getCurrentUser();
    	if (!ParseTwitterUtils.isLinked(currentUser)) {
    		Log.d(TAG, "Connecting to Twitter...");
    		ParseTwitterUtils.link(currentUser, activity, callback);
		} else {
			Log.d(TAG, "Good news, the user is already linked to a Twitter account.");
		}
    	
    	return true;
	}
	
	public static void saveUserId() {
		String userId = ParseTwitterUtils.getTwitter().getUserId();
		if (!Utils.isEmptyString(userId)) {
			ParseUser.getCurrentUser().put("twitterId", userId);
			ParseUser.getCurrentUser().saveInBackground();
		}
	}
	
	public static void publishPhoto(String objectId, String photoCaption, PublishCallback callback) {
		PublishTwitterAsyncTask task = new PublishTwitterAsyncTask(objectId, photoCaption, callback);
		task.execute();
	}
}
