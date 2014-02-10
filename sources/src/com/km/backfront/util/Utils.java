package com.km.backfront.util;

import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class Utils {
	protected static final String TAG = "Utils";

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
	  
	  public static boolean isEmptyString(String text) {
		  if (text != null && !text.isEmpty()) {
			  return false;
		  }
		  return true;
	  }
}
