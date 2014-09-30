package com.km.backflip.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.km.backflip.model.Follow;
import com.km.backflip.model.Moment;
import com.km.backflip.ui.SignUpActivity;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class Utils {
	public static final String TAG = Utils.class.getSimpleName();

	public static String getTimeFromDateToNow(Date date) {
		Date now = new Date();
		long diffInMin = (now.getTime() - date.getTime()) / 60000;
		
		// Less than a minute
		if (diffInMin < 1) {
			return "Now";
		// Less than an hour
		} else if (diffInMin < 60) {
			return diffInMin + " min ago";
		// Less than a day
		} else if (diffInMin < 1440) {
			return (diffInMin/60) + "h ago";
		// Less than a week
		} else if (diffInMin < 10080) {
			return (diffInMin/1440) + "d ago";
		// Less than a month
		} else if (diffInMin < 43830) {
			return (diffInMin/10080) + "w ago";
		// Less than a year
		} else if (diffInMin < 525949) {
			return (diffInMin/43830) + "month ago";
		}
		
		return (diffInMin/525949) + "y ago";
	}
	
	/**
	   * Checks if the device has Internet connection.
	   * 
	   * @return <code>true</code> if the phone is connected to the Internet.
	   */
	  public static boolean hasConnection(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
	        Context.CONNECTIVITY_SERVICE);
	
	    NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if (wifiNetwork != null && wifiNetwork.isConnected()) {
	      return true;
	    }
	
	    NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (mobileNetwork != null && mobileNetwork.isConnected()) {
	      return true;
	    }
	
	    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    if (activeNetwork != null && activeNetwork.isConnected()) {
	      return true;
	    }
	
	    return false;
	  }
	  
	  public static boolean isEmpty(EditText etText) {
		  if (etText.getText().toString().trim().length() > 0) {
		    return false;
		  } else {
		    return true;
		  }
		}
	  
	  public static boolean isValidEmailAddress(String email) {
		  Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		  Matcher m = p.matcher(email);
		  return m.matches();
	  }
	  
	  public static boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		  for (String string : subset) {
			  if (!superset.contains(string)) {
				  return false;
			  }
		  }
		  return true;
	  }
	  
	  public static void showToast(final Activity activity, final String message) {
		  if (activity != null) {
			  activity.runOnUiThread(new Runnable() {
		          public void run() {
		        	  try {
		        		  Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
		        	  } catch (Exception e) {
		        		  Log.i(TAG, "Failed to display a toast message: "+message);
	                  }
		          }
		      });
		  }
	  }
	  
	  public static void showToast(final Activity activity, final String message, final int length) {
		  if (activity != null) {
			  activity.runOnUiThread(new Runnable() {
		          public void run() {
		        	  try {
		        		  Toast.makeText(activity, message, length).show();
		        	  } catch (Exception e) {
		        		  Log.i(TAG, "Failed to display a toast message: "+message);
	                  }
		          }
		      });
		  }
	  }
	  
	  public static boolean isEmptyString(String text) {
		  if (text != null && !text.isEmpty()) {
			  return false;
		  }
		  return true;
	  }
	  
	  public static boolean isUsernameLongEnough(String username) {
		  if (username.length() < 3) {
			  return false;
		  }
		  return true;
	  }
	  
	  public static boolean isAlphanumeric(String str) {
		  for (int i = 0; i < str.length(); i++) {
			  char c = str.charAt(i);
			  if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a) {
				  return false;
			  }
		  }
		  return true;
	    }
	  
	  public static byte[] toByteArray(InputStream is) throws IOException {
		  ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		  int nRead;
		  byte[] data = new byte[16384];

		  while ((nRead = is.read(data, 0, data.length)) != -1) {
		    buffer.write(data, 0, nRead);
		  }

		  buffer.flush();

		  return buffer.toByteArray();
	  }
	  
	  public static boolean userLoggedIn(Context context) {
		  if (ParseUser.getCurrentUser() != null) {
			  return true;
		  }
		  Intent intent = new Intent(context, SignUpActivity.class);
		  context.startActivity(intent);
		  return false;
	  }
	  
	  public static void searchMomentsForFeed(int skip, int amount, FindCallback<Moment> callback) {
		// Create main query: moments from following OR Staff Picks OR me
	    	List<ParseQuery<Moment>> queries = new ArrayList<ParseQuery<Moment>>();
			
			// Sub query for all Staff Picks (favorites)
	    	ParseQuery<Moment> queryPicks = ParseQuery.getQuery(Moment.class);
	    	queryPicks.whereEqualTo("isFavorite", true);
	    	queries.add(queryPicks);
			
			ParseUser currentUser = ParseUser.getCurrentUser();
			if (currentUser != null) {
				// Sub query for all moments from following
				ParseQuery<Follow> innerQuery = ParseQuery.getQuery(Follow.class);
				innerQuery.whereEqualTo("fromUser", currentUser);
		    	ParseQuery<Moment> queryFollowers = ParseQuery.getQuery(Moment.class);
		    	queryFollowers.whereMatchesKeyInQuery("author", "toUser", innerQuery);
		    	queries.add(queryFollowers);
		    	
		    	// Sub query for all moments from current user
		    	ParseQuery<Moment> queryMine = ParseQuery.getQuery(Moment.class);
		    	queryMine.whereEqualTo("author", currentUser);
		    	queries.add(queryMine);
			}
			
	    	// Execute query
	    	ParseQuery<Moment> mainQuery = ParseQuery.or(queries);
	    	mainQuery.include("author");
	    	mainQuery.orderByDescending("createdAt");
	    	if (skip > 0) {
	    		mainQuery.setSkip(skip);
	    	}
	    	if (amount > 0) {
	    		mainQuery.setLimit(amount);
	    	}
	    	mainQuery.findInBackground(callback);
	  }
}
