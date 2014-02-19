package com.km.backfront.util.facebook;

import java.net.URL;
import com.km.backfront.util.BackflipException;
import com.km.backfront.util.Utils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import android.os.AsyncTask;

public class SaveFacebookProfilePictureAsyncTask extends AsyncTask<Void, Void, BackflipException> {

	private String userId;
	
	public SaveFacebookProfilePictureAsyncTask(String userId) {
		this.userId = userId;
	}
	
	@Override
	protected BackflipException doInBackground(Void... args) {
		try {
			URL imageURL = new URL("http://graph.facebook.com/" + userId + "/picture?type=large");
			byte[] data = Utils.toByteArray(imageURL.openConnection().getInputStream());
			ParseFile photoFile = new ParseFile("avatar.jpg", data);
			ParseUser.getCurrentUser().put("avatar", photoFile);
			ParseUser.getCurrentUser().save();
			photoFile.saveInBackground();
		} catch (Exception e) {
			e.printStackTrace();
			return new BackflipException("Unable to save profile picture: " + e.getMessage());
		}
		
		return null;
	}

	@Override
    protected void onPostExecute(BackflipException exception) {
        // called in UI thread
        super.onPostExecute(exception);
    }
}
