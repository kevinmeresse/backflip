package com.km.backflip.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/*
 * An extension of ParseObject that makes
 * it more convenient to access information
 * about a given Follow
 */

@ParseClassName("Follow")
public class Follow extends ParseObject {

	public Follow() {
		// A default constructor is required.
	}

	public ParseUser getFromUser() {
		return getParseUser("fromUser");
	}

	public void setFromUser(ParseUser user) {
		put("fromUser", user);
	}

	public ParseUser getToUser() {
		return getParseUser("toUser");
	}

	public void setToUser(ParseUser user) {
		put("toUser", user);
	}
}
