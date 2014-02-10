package com.km.backfront.util.path;

import com.km.backfront.util.LoginCallback;
import com.km.backfront.util.PublishCallback;

public class PathUtils {
	
	protected static final String TAG = "PathUtils";
	
	public static final String REDIRECT_URI = "backflip://authenticate";
	public static final String AUTHORIZATION_URI = "https://partner.path.com/oauth2/authenticate?response_type=code&client_id=";
	public static final String ACCESS_TOKEN_URI = "https://partner.path.com/oauth2/access_token";
	public static final String PUBLISH_PHOTO_URI = "https://partner.path.com/1/moment/photo";
	public static final String CLIENT_ID = "ed192f0dca6d539a59b83669941ae74874e81fc7";
	public static final String CLIENT_SECRET = "ac83f1b9c764bfe6ed74bcecec7a9330933cb9ac";
	
	public static void login(String code, LoginCallback callback) {
		LoginPathAsyncTask task = new LoginPathAsyncTask(code, callback);
		task.execute();
	}
	
	public static boolean urlContainsCode(String url) {
		if ( url.indexOf("code=") != -1 ) {
			return true;
		}
		return false;
	}
	
	public static String extractCodeFromUrl(String url) {
		String[] array = url.split("code=");
		if (array.length > 1) return array[1];
		return null;
	}
	
	public static void publishPhoto(String photoUrl, String photoCaption, PublishCallback callback) {
		PublishPathAsyncTask task = new PublishPathAsyncTask(photoUrl, photoCaption, callback);
		task.execute();
	}
}
