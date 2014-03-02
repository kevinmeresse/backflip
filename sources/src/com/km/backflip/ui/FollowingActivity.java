package com.km.backflip.ui;

import java.util.ArrayList;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

public class FollowingActivity extends Activity {

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
		setContentView(R.layout.activity_following);
		
		// Get the view objects
		listview = (ListView) findViewById(R.id.listview);
		topBarIconButton = (ImageButton) findViewById(R.id.top_bar_icon);
    	topBarTextButton = (Button) findViewById(R.id.top_bar_text);
    	listLoading = (ProgressBar) findViewById(R.id.listview_loading);
		
		// Fetch users the current user is following
		final List<ParseUser> users = new ArrayList<ParseUser>();
		
		ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
		query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
		query.include("toUser");
		// execute the query
		query.findInBackground(new FindCallback<Follow>() {
		    public void done(List<Follow> followList, ParseException e) {
		    	for (Follow f : followList) {
		    		users.add(f.getToUser());
		    	}
		    	listLoading.setVisibility(View.GONE);
		    	// Set adapter displaying the list of users
				UserListAdapter adapter = new UserListAdapter(FollowingActivity.this, users, true);
				listview.setAdapter(adapter);
		    }
		});
		
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
}
