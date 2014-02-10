package com.km.backfront.util.path;

import java.io.UnsupportedEncodingException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.km.backfront.util.BackflipException;
import com.km.backfront.util.PublishCallback;
import com.km.backfront.util.Utils;
import com.parse.ParseUser;
import android.os.AsyncTask;
import android.util.Log;

public class PublishPathAsyncTask extends AsyncTask<Void, Void, BackflipException> {

	private static final String TAG = "PublishPathAsyncTask";
	private String photoUrl;
	private String photoCaption;
	private PublishCallback callback;
	
	public PublishPathAsyncTask(String photoUrl, String photoCaption, PublishCallback callback) {
		this.photoUrl = photoUrl;
		this.photoCaption = photoCaption;
		this.callback = callback;
	}
	
	@Override
	protected BackflipException doInBackground(Void... args) {
		String accessToken = (String) ParseUser.getCurrentUser().get("pathAccessToken");
		
		if (Utils.isEmptyString(accessToken)) {
			return new BackflipException("User doesn't have any Access Token...");
		}
        
        // Exchange the code for the access token through a POST request
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(PathUtils.PUBLISH_PHOTO_URI);
        
        try {
        	JSONObject jsonData = new JSONObject();
        	jsonData.put("source_url", photoUrl);
        	jsonData.put("caption", photoCaption);
        	jsonData.put("private", true);
        	StringEntity entity = new StringEntity(jsonData.toString());
        	entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Authorization", "Bearer " + accessToken);
            httppost.setEntity(entity);
        	
		} catch (JSONException e) {
			return new BackflipException("Couldn't create POST JSON data: " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			return new BackflipException("Couldn't create POST form data: " + e.getMessage());
		}

        try {
        	Log.d(TAG, "Requesting to publish photo...");
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            // Handle errors
            int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != 200 && statusCode != 201 && statusCode != 202) {
	        	Log.e(TAG, "HTTP post error: " + statusCode + " - " + response.getStatusLine().getReasonPhrase());
	        	Log.e(TAG, "Error content: " + EntityUtils.toString(response.getEntity()));
	        	return new BackflipException("Unable to publish photo. HTTP response code from Path: " + statusCode);
	        }
	        Log.d(TAG, "Received a successful response. Parsing it...");
	        
        } catch (Exception e) {
        	return new BackflipException("Unexpected error: " + e.getMessage());
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
