package com.km.backfront.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/*
 * An extension of ParseObject that makes
 * it more convenient to access information
 * about a given Moment
 */

@ParseClassName("Moment")
public class Moment extends ParseObject {
	
	private boolean isLiked = false;

	public Moment() {
		// A default constructor is required.
	}

	public String getCaption() {
		return getString("caption");
	}

	public void setCaption(String caption) {
		put("caption", caption);
	}

	public ParseUser getAuthor() {
		return getParseUser("author");
	}

	public void setAuthor(ParseUser user) {
		put("author", user);
	}

	public Boolean getIsFavorite() {
		return getBoolean("isFavorite");
	}

	public void setIsFavorite(Boolean isFavorite) {
		put("isFavorite", isFavorite);
	}

	public ParseFile getPhotoFile() {
		return getParseFile("photo");
	}

	public void setPhotoFile(ParseFile file) {
		put("photo", file);
	}
	
	public String getLocationDescription() {
		return getString("locationDescription");
	}

	public void setLocationDescription(String locationDescription) {
		put("locationDescription", locationDescription);
	}
	
	public boolean isLiked() {
		return this.isLiked;
	}
	
	public void isLiked(boolean bool) {
		this.isLiked = bool;
	}
}
