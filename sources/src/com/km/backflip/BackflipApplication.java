package com.km.backflip;

import android.app.Application;

import com.km.backflip.model.Follow;
import com.km.backflip.model.Like;
import com.km.backflip.model.Moment;
import com.km.backflip.model.Report;
import com.km.backflip.ui.MainActivity;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseTwitterUtils;
import com.parse.PushService;
import com.crashlytics.android.Crashlytics;

public class BackflipApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		/*
		 * Crashlytics initialization
		 */
		Crashlytics.start(this);

		/*
		 * Register all ParseObject subclasses
		 */
		ParseObject.registerSubclass(Moment.class);
		ParseObject.registerSubclass(Like.class);
		ParseObject.registerSubclass(Report.class);
		ParseObject.registerSubclass(Follow.class);

		/*
		 * Fill in this section with your Parse credentials
		 */
		Parse.initialize(this, "ZvtTmUReSlI8EzAK6pJ7zalVxLjTAw5ehzyJPBnc", "o6p2Iyv3L7BtSgYJVhlnH1oiBLgmLYTq8BLAfNS4");
		
		PushService.setDefaultPushCallback(this, MainActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();
		
		ParseFacebookUtils.initialize("198320220368270");
		ParseTwitterUtils.initialize("ccGg382Q7O8A6YmK3T27Q", "mnL7vbiajkrYRGfTso6sESdvlttPJVZ77iZkuvJG2hk");

		
		/*
		 * This app lets an anonymous user create and save photos of meals
		 * they've eaten. An anonymous user is a user that can be created
		 * without a username and password but still has all of the same
		 * capabilities as any other ParseUser.
		 * 
		 * After logging out, an anonymous user is abandoned, and its data is no
		 * longer accessible. In your own app, you can convert anonymous users
		 * to regular users so that data persists.
		 * 
		 * Learn more about the ParseUser class:
		 * https://www.parse.com/docs/android_guide#users
		 */
		//ParseUser.enableAutomaticUser();

		/*
		 * For more information on app security and Parse ACL:
		 * https://www.parse.com/docs/android_guide#security-recommendations
		 */
		ParseACL defaultACL = new ParseACL();

		/*
		 * If you would like all objects to be private by default, remove this
		 * line
		 */
		defaultACL.setPublicReadAccess(true);

		ParseACL.setDefaultACL(defaultACL, true);
	}

}