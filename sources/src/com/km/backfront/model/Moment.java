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
	
	public static final int LIKE_UNDEFINED = 0;
	public static final int LIKED = 1;
	public static final int NOT_LIKED = -1;
	
	private int isLiked = LIKE_UNDEFINED;
	private int likeCount = 0;

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
	
	public ParseFile getThumbnail() {
		return getParseFile("thumbnail");
	}

	public void setThumbnail(ParseFile file) {
		put("thumbnail", file);
	}
	
	public ParseFile getBadPreview() {
		return getParseFile("badPreview");
	}

	public void setBadPreview(ParseFile file) {
		put("badPreview", file);
	}
	
	public String getLocationDescription() {
		return getString("locationDescription");
	}

	public void setLocationDescription(String locationDescription) {
		put("locationDescription", locationDescription);
	}
	
	public int isLiked() {
		return this.isLiked;
	}
	
	public void isLiked(int value) {
		this.isLiked = value;
	}
	
	public int getLikeCount() {
		return this.likeCount;
	}
	
	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}
}
