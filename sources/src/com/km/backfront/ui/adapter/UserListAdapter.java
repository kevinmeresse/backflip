package com.km.backfront.ui.adapter;

import java.util.List;

import com.km.backfront.R;
import com.km.backfront.util.ActivityUtils;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
	
	private Activity activity;
	private List<ParseUser> users = null;
	private boolean allFollowed = false;
	
	public UserListAdapter(Activity activity, List<ParseUser> users) {
		this.activity = activity;
		this.users = users;
	}
	
	public UserListAdapter(Activity activity, List<ParseUser> users, boolean allFollowed) {
		this.activity = activity;
		this.users = users;
		this.allFollowed = allFollowed;
	}

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
		
		// reuse a given view, or inflate a new one from the xml
		if (convertView == null) {
			view = activity.getLayoutInflater().inflate(R.layout.follow_list_row, null);
		} else {
			view = convertView;
		}
		
		// make sure it's worth drawing the view
		if (users.get(position) == null) {
			return view;
		}
		
		// pull out the object
		final ParseUser user = users.get(position);
		
		ParseImageView avatar = (ParseImageView) view.findViewById(R.id.avatar);
		TextView username = (TextView) view.findViewById(R.id.name);
		final Button followButton = (Button) view.findViewById(R.id.follow_button);
		final Button unfollowButton = (Button) view.findViewById(R.id.unfollow_button);
		
		avatar.setParseFile((ParseFile) user.get("avatar"));
		avatar.loadInBackground();
		username.setText(user.getUsername());
		if (allFollowed || user.getBoolean("isFollowed")) {
			unfollowButton.setVisibility(View.VISIBLE);
			followButton.setVisibility(View.GONE);
		} else {
			followButton.setVisibility(View.VISIBLE);
			unfollowButton.setVisibility(View.GONE);
		}
		
		followButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Toggle button
		    	unfollowButton.setVisibility(View.VISIBLE);
		    	followButton.setVisibility(View.GONE);
		    	
		    	// Follow
		    	ActivityUtils.follow(ParseUser.getCurrentUser(), user);
		    }
    	});
		
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
