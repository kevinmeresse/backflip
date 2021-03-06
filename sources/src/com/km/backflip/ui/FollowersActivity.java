package com.km.backflip.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.km.backflip.model.Follow;
import com.km.backflip.ui.adapter.UserListAdapter;
import com.km.backflip.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

public class FollowersActivity extends Activity {

	public static final String TAG = FollowersActivity.class.getSimpleName();

	private ListView listview;
	
	private ImageButton topBarIconButton;
	private Button topBarTextButton;
	private ProgressBar listLoading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    
		// Remove the title bar
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

		// Set the layout
		setContentView(R.layout.activity_followers);
		
		// Get the view objects
		listview = (ListView) findViewById(R.id.listview);
		topBarIconButton = (ImageButton) findViewById(R.id.top_bar_icon);
    	topBarTextButton = (Button) findViewById(R.id.top_bar_text);
    	listLoading = (ProgressBar) findViewById(R.id.listview_loading);
		
    	fetchFollowers();
    	
		
		
		// Action: click on top bar icon
    	topBarIconButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	finish();
		    }
    	});
    	
    	// Action: click on top bar text
    	topBarTextButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	finish();
		    }
    	});
	}
	
	public void fetchFollowers() {
		Log.d(TAG, "Fetching followers...");
		// First we build a HashSet (will have a constant access time) with the followed followers
		final HashSet<String> set = new HashSet<String>();
		
		// Inner query: all follows from current user
		ParseQuery<Follow> innerQuery = ParseQuery.getQuery(Follow.class);
		innerQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());
		
		// Main query: all follows targeting current user AND from users targeted in the inner query
		ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
		query.whereEqualTo("toUser", ParseUser.getCurrentUser());
		query.whereMatchesKeyInQuery("fromUser", "toUser", innerQuery);
		query.include("fromUser");
		// Execute the query
		Log.d(TAG, "Executing nested query...");
		query.findInBackground(new FindCallback<Follow>() {
		    public void done(List<Follow> followList, ParseException e) {
		    	Log.d(TAG, "Found " + followList.size() + " users who follow current user and vice versa.");
		    	for (Follow f : followList) {
		    		Log.d(TAG, " - " + f.getFromUser().getUsername());
		    		set.add(f.getFromUser().getObjectId());
		    	}
		    	fetchAndSortAllFollowers(set);
		    }
		});
	}
	
	public void fetchAndSortAllFollowers(final HashSet<String> followedUserIds) {
		// Fetch users following current user
		final List<ParseUser> users = new ArrayList<ParseUser>();
		
		ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
		query.whereEqualTo("toUser", ParseUser.getCurrentUser());
		query.include("fromUser");
		// execute the query
		Log.d(TAG, "Executing query to get ALL followers...");
		query.findInBackground(new FindCallback<Follow>() {
		    public void done(List<Follow> followList, ParseException e) {
		    	Log.d(TAG, "Found " + followList.size() + " users who follow current user.");
		    	for (Follow f : followList) {
		    		ParseUser user = f.getFromUser();
		    		Log.d(TAG, " - " + user.getUsername());
		    		if (followedUserIds.contains(user.getObjectId())) {
		    			Log.d(TAG, "   -----> following back");
		    			user.put("isFollowed", true);
		    		}
		    		users.add(user);
		    	}
		    	listLoading.setVisibility(View.GONE);
		    	// Set adapter displaying the list of users
				UserListAdapter adapter = new UserListAdapter(FollowersActivity.this, users);
				listview.setAdapter(adapter);
		    }
		});
	}
}
