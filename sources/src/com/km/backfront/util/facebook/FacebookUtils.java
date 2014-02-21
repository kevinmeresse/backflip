package com.km.backfront.util.facebook;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.km.backfront.util.ActivityUtils;
import com.km.backfront.util.PublishCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class FacebookUtils {
	
	protected static final String TAG = "PathUtils";
	
	public static final String PERMISSION_PHOTO_UPLOAD = "photo_upload";
	public static final String ALBUM_NAME = "Backflip moments";
	public static final String ALBUMS_URI = "me/albums";
	
	
	public static String getPublishPhotoUri(String albumId) {
		return albumId + "/photos";
	}
	
	public static boolean login(Activity activity, SaveCallback callback) {
		Log.d(TAG, "Clicked on Share on Facebook.");
    	ParseUser currentUser = ParseUser.getCurrentUser();
    	if (!ParseFacebookUtils.isLinked(currentUser)) {
    		Log.d(TAG, "Connecting to Facebook...");
    		ParseFacebookUtils.link(currentUser, activity, callback);
    		followFriendsInBackground();
		} else {
			Log.d(TAG, "Good news, the user is already linked to a Facebook account.");
		}
    	
    	return true;
	}
	
	public static void saveUserId() {
		Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (user != null) {
					ParseUser.getCurrentUser().put("facebookId", user.getId());
					FacebookUtils.saveFacebookProfilePicture(user.getId());
					ParseUser.getCurrentUser().saveInBackground();
				}
			}
		}).executeAsync();
	}
	
	public static void publishPhoto(String photoUrl, String photoCaption, PublishCallback callback) {
		PublishFacebookAsyncTask task = new PublishFacebookAsyncTask(photoUrl, photoCaption, callback);
		task.execute();
	}
	
	public static void followFriendsInBackground() {
		Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {

			  @Override
			  public void onCompleted(List<GraphUser> users, Response response) {
			    if (users != null) {
			      List<String> friendsList = new ArrayList<String>();
			      for (GraphUser user : users) {
			        friendsList.add(user.getId());
			      }

			      // Construct a ParseUser query that will find friends whose
			      // facebook IDs are contained in the current user's friend list.
			      ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();
			      friendQuery.whereContainedIn("facebookId", friendsList);

			      // findObjects will return a list of ParseUsers that are friends with
			      // the current user
			      try {
					List<ParseUser> friendUsers = friendQuery.find();
					for (ParseUser friend : friendUsers) {
						ActivityUtils.follow(ParseUser.getCurrentUser(), friend);
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    }
			  }
			}).executeAsync();
	}
	
	public static void saveFacebookProfilePicture(String userId) {
		SaveFacebookProfilePictureAsyncTask task = new SaveFacebookProfilePictureAsyncTask(userId);
		task.execute();
	}
}
