package com.km.backfront.util.twitter;

import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import com.km.backfront.util.BackflipException;
import com.km.backfront.util.PublishCallback;
import com.km.backfront.util.Utils;
import com.parse.ParseTwitterUtils;
import android.os.AsyncTask;
import android.util.Log;

public class PublishTwitterAsyncTask extends AsyncTask<Void, Void, BackflipException> {

	private static final String TAG = "PublishPathAsyncTask";
	private String momentId;
	private String photoCaption;
	private PublishCallback callback;
	
	public PublishTwitterAsyncTask(String momentId, String photoCaption, PublishCallback callback) {
		this.momentId = momentId;
		this.photoCaption = photoCaption;
		this.callback = callback;
	}
	
	@Override
	protected BackflipException doInBackground(Void... args) {
		try {
	        HttpClient client = new DefaultHttpClient();
	        String message = "";
	        if (!Utils.isEmptyString(photoCaption)) {
	        	message = photoCaption+" - "+TwitterUtils.getMomentWebpageUrl(momentId);
	        } else {
	        	message = TwitterUtils.getMomentWebpageUrl(momentId);
	        }
	        String encodedMessage = URLEncoder.encode(message, "utf-8");
	        Log.d(TAG, "Encoded message: "+ encodedMessage);
	        HttpPost verifyPost = new HttpPost(TwitterUtils.getPublishPhotoUri(encodedMessage));
	        ParseTwitterUtils.getTwitter().signRequest(verifyPost);
	        HttpResponse response = client.execute(verifyPost);
	        // Handle errors
	        if (response.getStatusLine().getStatusCode() != 200) {
	        	return new BackflipException("HTTP post error: "+ response.getStatusLine().getStatusCode()+" - "+response.getStatusLine().getReasonPhrase());
	        }
		} catch (Exception e) {
			e.printStackTrace();
			return new BackflipException("Unknown error: "+ e.getMessage());
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
