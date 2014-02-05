package com.km.backfront.util;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFacebookUtils.Permissions;

public class PostPhotoOnFacebookRunnable implements Runnable {
	
	private static final String TAG = "PostPhotoOnFacebookRunnable";
	private Activity activity = null;
	private String photoUrl = "";
	private String photoCaption = "";

	public PostPhotoOnFacebookRunnable(Activity activity, String photoUrl, String photoCaption) {
		// store parameter for later user
		this.activity = activity;
		this.photoUrl = photoUrl;
		this.photoCaption = photoCaption;
   }

	@Override
	public void run() {
		// Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        
        // Retrieve Facebook session from Parse
        Session session = ParseFacebookUtils.getSession();
        // List mandatory permissions
        final List<String> PERMISSIONS = Arrays.asList(Permissions.Extended.PUBLISH_STREAM, "photo_upload");
	    
        // We try to post on Facebook only if we have an active session
	    if (session != null) {
	    	Log.d(TAG, "Found Facebook session. Creating post...");

	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!Utils.isSubsetOf(PERMISSIONS, permissions)) {
	        	Log.d(TAG, "This user has not authorized us to publish...");
	        	for (int i = 0; i < permissions.size(); i++) {
	        		Log.d(TAG, "   - Permission: "+permissions.get(i));
	        	}
	        	Utils.showToast(this.activity, "An error occured while posting photo on Facebook...");
	        	return;
	        }
	        Log.d(TAG, "Good. This user has authorized us to publish :-)");
	        
	        // Initialize potential error from Facebook
	        FacebookRequestError error = null;
	        
	        try {
	        	/*** Retrieve user's albums ***/
	        	String albumId = "";
	        	
	        	// Create Facebook request
	        	Bundle params = new Bundle();
	        	Request request = new Request(
	        					session, 
		    	        		"me/albums", 
		    	        		params, 
		                        HttpMethod.GET);
	        	
	        	// Send request
	        	Log.d(TAG, "Retrieving user's albums from Facebook...");
    	        Response response = request.executeAndWait();
    	        
    	        // Stop everything if error from Facebook server
    	        error = response.getError();
    	        if (error != null) throw new FacebookException(error.getErrorMessage());
    	        
                // Parse response to get our album ID
                JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
                JSONArray albumsArray = graphResponse.getJSONArray("data");
                Log.d(TAG, "Found "+albumsArray.length()+" albums. Now searching for Backflip album...");
                for (int i = 0; i < albumsArray.length(); i++) {
                    JSONObject item = albumsArray.getJSONObject(i);
                    Log.d(TAG, "   - Album: "+item.getString("name"));
                    if (item.getString("name").equals("Backflip moments")) {
                    	albumId = item.getString("id");
                    	Log.d(TAG, "Found Backflip album (id: "+albumId+").");
                    	break;
                    }
                }
                
                
                /*** Create Backflip album ***/
                // If we couldn't find our album, we need to create it
                if (Utils.isEmptyString(albumId)) {
                	Log.d(TAG, "Unable to find Backflip album. Creating a new one...");
                	// Create Facebook request
                	params = new Bundle();
        	        params.putString("name", "Backflip moments");
        	        request = new Request(
        	        		session, 
        	        		"me/albums", 
        	        		params, 
                            HttpMethod.POST);
        	        
        	        // Send request
    	        	Log.d(TAG, "Sending request to create new album on Facebook...");
        	        response = request.executeAndWait();
        	        
        	        // Stop everything if error from Facebook server
        	        error = response.getError();
        	        if (error != null) throw new FacebookException(error.getErrorMessage());
                    
        	        // Get the new album ID
                    graphResponse = response.getGraphObject().getInnerJSONObject();
                    albumId = graphResponse.getString("id");
                    
                    // Check if album ID is set
                    if (Utils.isEmptyString(albumId)) {
                    	throw new FacebookException("Couldn't retrieve new album ID from Facebook JSON response.");
                    }
                }
                
                
	        	/*** Post photo to album ***/
                // Create Facebook request
            	params = new Bundle();
    	        params.putString("url", this.photoUrl);
    	        params.putString("message", this.photoCaption);
    	        request = new Request(
    	        		session, 
    	        		albumId+"/photos", 
    	        		params, 
                        HttpMethod.POST);

    	        // Send request
    	        Log.d(TAG, "Requesting Facebook to post a photo to album ("+albumId+")...");
    	        response = request.executeAndWait();
    	        
    	        // Stop everything if error from Facebook server
    	        error = response.getError();
    	        if (error != null) throw new FacebookException(error.getErrorMessage());
    	    
	        } catch (JSONException e) {
                Log.e(TAG, "JSON error: "+ e.getMessage());
                error = new FacebookRequestError(1, "JSON error", e.getMessage());
	        } catch (FacebookException e) {
                Log.e(TAG, "Facebook server error: "+ e.getMessage());
            } catch (Exception e) {
            	Log.e(TAG, "Unknown error: "+ e.getMessage());
            	e.printStackTrace();
            	error = new FacebookRequestError(1, "Unknown error", e.getMessage());
            }
	        
	        // Handle errors
	        if (error != null) {
                Utils.showToast(this.activity, "An error occured while posting photo on Facebook...");
            }
	    }
	}
}
