package com.km.backflip.util.path;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.km.backflip.util.BackflipException;
import com.km.backflip.util.LoginCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import android.os.AsyncTask;
import android.util.Log;

public class LoginPathAsyncTask extends AsyncTask<Void, Void, BackflipException> {

	private static final String TAG = "LoginPathAsyncTask";
	private String code;
	private LoginCallback callback;
	
	public LoginPathAsyncTask(String code, LoginCallback loginCallback) {
		this.code = code;
		this.callback = loginCallback;
	}
	
	@Override
	protected BackflipException doInBackground(Void... args) {
		String accessToken = null;
        String pathId = null;
        
        // Exchange the code for the access token through a POST request
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(PathUtils.ACCESS_TOKEN_URI);
        
        try {
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
            nameValuePairs.add(new BasicNameValuePair("client_id", PathUtils.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("client_secret", PathUtils.CLIENT_SECRET));
            nameValuePairs.add(new BasicNameValuePair("code", code));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			return new BackflipException("Couldn't create POST form data: " + e.getMessage());
		}

        try {
        	Log.d(TAG, "Requesting access token...");
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            // Handle errors
            int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != 200 && statusCode != 201 && statusCode != 202) {
	        	Log.e(TAG, "HTTP post error: "+ statusCode+" - "+response.getStatusLine().getReasonPhrase());
	        	return new BackflipException("Access token hasn't been created. HTTP response code from Path: " + statusCode);
	        }
	        Log.d(TAG, "Received a successful response. Parsing it...");
	        // Parse content
	        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
	        accessToken = json.getString("access_token");
	        Log.d(TAG, "Access token: "+accessToken);
	        pathId = json.getString("user_id");
	        Log.d(TAG, "Path ID: "+pathId);
	        
        } catch (JSONException e) {
        	return new BackflipException("Unexpected JSON response from Path: " + e.getMessage());
        } catch (Exception e) {
        	return new BackflipException("Unexpected error: " + e.getMessage());
        }
        
        Log.d(TAG, "Saving both in database...");
        // Save the access token and the Path ID in database
        ParseUser.getCurrentUser().put("pathAccessToken", accessToken);
		ParseUser.getCurrentUser().put("pathId", pathId);
		try {
			ParseUser.getCurrentUser().save();
		} catch (ParseException e) {
			return new BackflipException("Error when saving to Parse: " + e.getMessage());
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
