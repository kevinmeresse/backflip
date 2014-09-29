package com.km.backflip.ui.adapter;

import java.util.List;

import com.km.backflip.util.ActivityUtils;
import com.km.backflip.R;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class UserListAdapter2 extends BaseAdapter {
	
	private Activity activity;
	private List<ParseUser> users = null;
	private boolean allFollowed = false;
	
	/*
	 * Constructors
	 */
	
	public UserListAdapter2(Activity activity, List<ParseUser> users) {
		this.activity = activity;
		this.users = users;
	}
	
	public UserListAdapter2(Activity activity, List<ParseUser> users, boolean allFollowed) {
		this.activity = activity;
		this.users = users;
		this.allFollowed = allFollowed;
	}

	/*
	 * Inherited methods
	 */
	
	@Override
	public int getCount() {
		if (users == null) return 0;
		return users.size();
	}

	@Override
	public Object getItem(int position) {
		if (users == null) return null;
		return users.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view;
		
		// Reuse a given view, or inflate a new one from the xml
		if (convertView == null) {
			view = activity.getLayoutInflater().inflate(R.layout.follow_list_row, null);
		} else {
			view = convertView;
		}
		
		// Make sure it's worth drawing the view
		if (users.get(position) == null) {
			return view;
		}
		
		// Pull out the object
		final ParseUser user = users.get(position);
		
		// Get the view objects
		final Button followButton = (Button) view.findViewById(R.id.follow_button);
		final Button unfollowButton = (Button) view.findViewById(R.id.unfollow_button);
		ParseImageView avatar = (ParseImageView) view.findViewById(R.id.avatar);
		TextView username = (TextView) view.findViewById(R.id.name);
		
		// Load the avatar in background
		avatar.setParseFile((ParseFile) user.get("avatar"));
		avatar.loadInBackground();
		
		// Set the username
		username.setText(user.getUsername());
		
		// Display the right follow/unfollow button
		if (allFollowed || user.getBoolean("isFollowed")) {
			unfollowButton.setVisibility(View.VISIBLE);
			followButton.setVisibility(View.GONE);
		} else {
			followButton.setVisibility(View.VISIBLE);
			unfollowButton.setVisibility(View.GONE);
		}
		
		// Action: Click on FOLLOW
		followButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Toggle button
		    	unfollowButton.setVisibility(View.VISIBLE);
		    	followButton.setVisibility(View.GONE);
		    	
		    	// Follow
		    	ActivityUtils.follow(ParseUser.getCurrentUser(), user);
		    }
    	});
		
		// Action: Click on UNFOLLOW
		unfollowButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Toggle button
		    	followButton.setVisibility(View.VISIBLE);
		    	unfollowButton.setVisibility(View.GONE);
		    	
		    	// Unfollow
		    	ActivityUtils.unfollow(ParseUser.getCurrentUser(), user);
		    }
    	});
		
		return view;
	}

}
