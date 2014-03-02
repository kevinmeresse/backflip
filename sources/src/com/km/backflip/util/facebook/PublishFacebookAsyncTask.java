package com.km.backflip.util.facebook;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.km.backflip.util.BackflipException;
import com.km.backflip.util.PublishCallback;
import com.km.backflip.util.Utils;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFacebookUtils.Permissions;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class PublishFacebookAsyncTask extends AsyncTask<Void, Void, BackflipException> {

	private static final String TAG = "PublishPathAsyncTask";
	private String photoUrl;
	private String photoCaption;
	private PublishCallback callback;
	
	public PublishFacebookAsyncTask(String photoUrl, String photoCaption, PublishCallback callback) {
		this.photoUrl = photoUrl;
		this.photoCaption = photoCaption;
		this.callback = callback;
	}
	
	@Override
	protected BackflipException doInBackground(Void... args) {
		// Retrieve Facebook session from Parse
        Session session = ParseFacebookUtils.getSession();
        // List mandatory permissions
        final List<String> PERMISSIONS = Arrays.asList(Permissions.Extended.PUBLISH_STREAM, FacebookUtils.PERMISSION_PHOTO_UPLOAD);
	    
        // We try to post on Facebook only if we have an active session
	    if (session != null) {
	    	Log.d(TAG, "Found Facebook session. Creating post...");

	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!Utils.isSubsetOf(PERMISSIONS, permissions)) {
	        	return new BackflipException("This user has not authorized us to publish...");
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
		    	        		FacebookUtils.ALBUMS_URI, 
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
                    if (item.getString("name").equals(FacebookUtils.ALBUM_NAME)) {
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
        	        params.putString("name", FacebookUtils.ALBUM_NAME);
        	        request = new Request(
        	        		session, 
        	        		FacebookUtils.ALBUMS_URI, 
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
    	        		FacebookUtils.getPublishPhotoUri(albumId), 
    	        		params, 
                        HttpMethod.POST);

    	        // Send request
    	        Log.d(TAG, "Requesting Facebook to post a photo to album ("+albumId+")...");
    	        response = request.executeAndWait();
    	        
    	        // Stop everything if error from Facebook server
    	        error = response.getError();
    	        if (error != null) throw new FacebookException(error.getErrorMessage());
    	    
	        } catch (JSONException e) {
	        	return new BackflipException("JSON error: "+ e.getMessage());
            } catch (FacebookException e) {
            	return new BackflipException("Facebook server error: "+ e.getMessage());
            } catch (Exception e) {
            	return new BackflipException("Unknown error: "+ e.getMessage());
            }
	    }
	    return null;
	}

	@Override
    protected void onPostExecute(BackflipException exception) {
        // called in UI thread
        callback.done(exception);
        super.onPostExecute(exception);
    }
}
