package com.km.backfront.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.km.backfront.R;
import com.km.backfront.model.Follow;
import com.km.backfront.model.Moment;
import com.km.backfront.util.Utils;

/**
 * Activity which displays a login screen to the user.
 */
public class ProfileActivity extends Activity {
	
	protected static final String TAG = "ProfileActivity";
	
	private ParseUser displayedUser;
	private boolean isMyProfile = true;
	
	public List<Moment> moments = null;
	public GridImageAdapter gridAdapter;
	
	// UI references
	private GridView gridview;
	private ProgressBar gridLoading;
	private TextView gridOffline;
	private TextView gridEmpty;
	private ImageButton topBarIconButton;
	private Button topBarTextButton;
	private TextView profileUsername;
	private TextView profileEmail;
	private Button manageSocialButton;
	private Button followButton;
	private Button unfollowButton;
	private TextView numberFollowers;
	private TextView numberFollowing;
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    
		// Remove the title bar
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

		// Set the layout
		setContentView(R.layout.activity_profile);

		// Get the view objects
		gridview = (GridView) findViewById(R.id.gridview);
		gridLoading = (ProgressBar) findViewById(R.id.profile_grid_loading);
		gridOffline = (TextView) findViewById(R.id.profile_grid_offline);
		gridEmpty = (TextView) findViewById(R.id.profile_grid_empty);
		topBarIconButton = (ImageButton) findViewById(R.id.profile_top_bar_icon);
    	topBarTextButton = (Button) findViewById(R.id.profile_top_bar_text);
    	profileUsername = (TextView) findViewById(R.id.profile_username);
    	profileEmail = (TextView) findViewById(R.id.profile_email);
    	manageSocialButton = (Button) findViewById(R.id.profile_manage_social_button);
    	followButton = (Button) findViewById(R.id.profile_follow_button);
    	unfollowButton = (Button) findViewById(R.id.profile_unfollow_button);
    	numberFollowers = (TextView) findViewById(R.id.profile_number_followers);
    	numberFollowing = (TextView) findViewById(R.id.profile_number_following);
    	
    	// Retrieve user to display
    	Bundle extras = getIntent().getExtras();
    	if (extras != null && !Utils.isEmptyString(extras.getString("USER_ID")) && 
    			(ParseUser.getCurrentUser() == null || ParseUser.getCurrentUser().getObjectId() == null || !ParseUser.getCurrentUser().getObjectId().equals(extras.getString("USER_ID")))) {
    		isMyProfile = false;
    		displayedUser = new ParseUser();
    		displayedUser.setObjectId(extras.getString("USER_ID"));
    		displayedUser.setUsername(extras.getString("USERNAME"));
    	} else {
    		displayedUser = ParseUser.getCurrentUser();
    	}
    	
    	// Toggle follow/unfollow button if needed
    	displayUnfollowButtonIfNeeded();
    	
    	// Update user info
    	profileUsername.setText(displayedUser.getUsername());
    	if (isMyProfile) {
    		if (!Utils.isEmptyString(displayedUser.getEmail())) {
    			profileEmail.setText(displayedUser.getEmail());
    			profileEmail.setVisibility(View.VISIBLE);
    		}
    		manageSocialButton.setVisibility(View.VISIBLE);
    	} else {
    		followButton.setVisibility(View.VISIBLE);
    	}
    	updateFollow();
		
		// Create grid adapter and update it
		gridAdapter = new GridImageAdapter(this);
		gridview.setAdapter(gridAdapter);
	    updateGrid();
	    
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
    	
    	// Action: click on Follow
    	followButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Toggle button
		    	unfollowButton.setVisibility(View.VISIBLE);
		    	followButton.setVisibility(View.GONE);
		    	
		    	// Increment count on screen
		    	incrementFollowers();
		    	
		    	// Create object
		    	Follow follow = new Follow();
		    	follow.setFromUser(ParseUser.getCurrentUser());
		    	follow.setToUser(ParseUser.createWithoutData(ParseUser.class, displayedUser.getObjectId()));
		    	
		    	// Save the follow to Parse
		    	follow.saveInBackground();
		    }
    	});
    	
    	// Action: click on Unfollow
    	unfollowButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Toggle button
		    	followButton.setVisibility(View.VISIBLE);
		    	unfollowButton.setVisibility(View.GONE);
		    	
		    	// Decrement count on screen
		    	decrementFollowers();
		    	
		    	ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
	        	query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
	        	query.whereEqualTo("toUser", displayedUser);
		    	query.findInBackground(new FindCallback<Follow>() {
		            public void done(List<Follow> follows, ParseException e) {
		                if (e == null && follows.size() > 0) {
		                	for (Follow f : follows) {
		                		f.deleteInBackground();
		                	}
		                }
		            }
		        });
		    }
    	});

	    // Action: click on picture in the grid
	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	Intent intent = new Intent(v.getContext(), DisplayMomentActivity.class);
	        	intent.putExtra("MOMENT_ID", moments.get(position).getObjectId());
		    	startActivity(intent);
	        }
	    });
	}
	
	public void displayUnfollowButtonIfNeeded() {
		ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
    	query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
    	query.whereEqualTo("toUser", displayedUser);
    	query.findInBackground(new FindCallback<Follow>() {
            public void done(List<Follow> follows, ParseException e) {
                if (e == null && follows.size() > 0) {
                	// Toggle button
    		    	unfollowButton.setVisibility(View.VISIBLE);
    		    	followButton.setVisibility(View.GONE);
                }
            }
        });
	}
	
	public void incrementFollowers() {
		Log.d(TAG, "Incrementing followers...");
		try {
			int count = Integer.parseInt(numberFollowers.getText().toString());
			count = count + 1;
			Log.d(TAG, "New count: " + count);
			numberFollowers.setText("" + count);
		} catch (NumberFormatException e) {
			Log.e(TAG, "Couldn't parse number: " + e.getMessage());
			// Do nothing
		}
	}
	
	public void decrementFollowers() {
		Log.d(TAG, "Decrementing followers...");
		try {
			int count = Integer.parseInt(numberFollowers.getText().toString());
			if (count > 0) count = count - 1;
			Log.d(TAG, "New count: " + count);
			numberFollowers.setText("" + count);
		} catch (NumberFormatException e) {
			Log.e(TAG, "Couldn't parse number: " + e.getMessage());
			// Do nothing
		}
	}

	public void updateGrid() {
		if (!Utils.hasConnection(this)) {
			Log.d(TAG, "No internet connection...");
			gridLoading.setVisibility(View.INVISIBLE);
            gridOffline.setVisibility(View.VISIBLE);
		} else {
			Log.d(TAG, "Internet connection found :-)");
	    	ParseQuery<Moment> query = ParseQuery.getQuery(Moment.class);
	    	query.orderByDescending("createdAt");
	        query.whereEqualTo("author", displayedUser);
	        query.findInBackground(new FindCallback<Moment>() {
	            public void done(List<Moment> momentList, ParseException e) {
	                if (e == null) {
	                	Log.d(TAG, "Retrieved " + momentList.size() + " moments to display on grid.");
	                	moments = momentList;
	                	if (!moments.isEmpty()) {
	                		gridAdapter.notifyDataSetChanged();
		                	gridLoading.setVisibility(View.INVISIBLE);
	                	} else {
	                		gridLoading.setVisibility(View.INVISIBLE);
		                    gridEmpty.setVisibility(View.VISIBLE);
	                	}
	                } else {
	                    Log.e(TAG, "Error: " + e.getMessage());
	                    gridLoading.setVisibility(View.INVISIBLE);
	                    gridOffline.setVisibility(View.VISIBLE);
	                }
	            }
	        });
		}
    }
	
	public void updateFollow() {
		// Get number of followers
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Follow");
    	query.whereEqualTo("toUser", displayedUser);
        query.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                	Log.d(TAG, "Found " + count + " followers.");
                	numberFollowers.setText("" + count);
                } else {
                    Log.e(TAG, "Couldn't get number of followers: " + e.getMessage());
                }
            }
        });
        // Get number of users being followed
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Follow");
    	query2.whereEqualTo("fromUser", displayedUser);
        query2.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                	Log.d(TAG, "Found " + count + " following.");
                	numberFollowing.setText("" + count);
                } else {
                    Log.e(TAG, "Couldn't get number of users being followed: " + e.getMessage());
                }
            }
        });
	}
	
	public class GridImageAdapter extends BaseAdapter {
	    private Context mContext;

	    public GridImageAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	    	if (moments != null) return moments.size();
	    	return 0;
	    }

	    public Object getItem(int position) {
	        return moments.get(position);
	    }

	    public long getItemId(int position) {
	    	return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ParseImageView imageView;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ParseImageView(mContext);
	            imageView.setAdjustViewBounds(true);
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	        } else {
	            imageView = (ParseImageView) convertView;
	        }

	        Moment moment = moments.get(position);
	        if (moment.getThumbnail() != null) {
	        	imageView.setParseFile(moment.getThumbnail());
	        } else {
	        	imageView.setParseFile(moment.getPhotoFile());
	        }
	        imageView.loadInBackground();
	        return imageView;
	    }
	}
}
