package com.km.backflip.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/*
 * An extension of ParseObject that makes
 * it more convenient to access information
 * about a given Like
 */

@ParseClassName("Like")
public class Like extends ParseObject {

	public Like() {
		// A default constructor is required.
	}

	public ParseUser getFromUser() {
		return getParseUser("fromUser");
	}

	public void setFromUser(ParseUser user) {
		put("fromUser", user);
	}

	public Moment getMoment() {
		return (Moment)getParseObject("moment");
	}

	public void setMoment(Moment moment) {
		put("moment", moment);
	}
}
