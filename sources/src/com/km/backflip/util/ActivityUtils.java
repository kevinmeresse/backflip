package com.km.backflip.util;

import java.util.List;

import com.km.backflip.model.Follow;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ActivityUtils {

	protected static final String TAG = "ActivityUtils";
	
	public static void follow(ParseUser from, ParseUser to) {
		if (from != null && to != null) {
			// Create object
	    	Follow follow = new Follow();
	    	follow.setFromUser(from);
	    	follow.setToUser(ParseUser.createWithoutData(ParseUser.class, to.getObjectId()));
	    	
	    	// Save the follow to Parse
	    	follow.saveInBackground();
		}
	}
	
	public static void unfollow(ParseUser from, ParseUser to) {
		if (from != null && to != null) {
			ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
	    	query.whereEqualTo("fromUser", from);
	    	query.whereEqualTo("toUser", to);
	    	query.findInBackground(new FindCallback<Follow>() {
	            public void done(List<Follow> follows, ParseException e) {
	                if (e == null && follows.size() > 0) {
	                	for (Follow f : follows) {
	                		f.deleteInBackground();
	                	}
	                }
	            }
	        });
		}
	}
}
