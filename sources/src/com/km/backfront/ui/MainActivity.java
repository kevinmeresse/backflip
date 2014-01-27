package com.km.backfront.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.km.backfront.R;
import com.km.backfront.model.Like;
import com.km.backfront.model.Moment;
import com.km.backfront.model.Report;
import com.km.backfront.ui.vertical.FragmentStatePagerAdapter;
import com.km.backfront.ui.vertical.VerticalViewPager;
import com.km.backfront.util.BitmapHelper;
import com.km.backfront.util.Utils;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends FragmentActivity {
	
	MyAdapter mAdapter;
    VerticalViewPager mPager;
    public static List<Moment> moments;
	
	private ImageButton feedButton;
	private ImageButton cameraButton;
	private ImageButton settingsButton;
	private ProgressBar feedLoading;
	private RelativeLayout feedOffline;
	private ImageButton feedOfflineReload;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		
		// Track statistics with Parse
		ParseAnalytics.trackAppOpened(getIntent());
		
		// Set the layout
		setContentView(R.layout.activity_main);
		
		// Get the view objects
		feedButton = (ImageButton) findViewById(R.id.menu_feed_button);
		cameraButton = (ImageButton) findViewById(R.id.menu_add_button);
		settingsButton = (ImageButton) findViewById(R.id.menu_settings_button);
		mPager = (VerticalViewPager)findViewById(R.id.pager);
		feedLoading = (ProgressBar) findViewById(R.id.feed_loading);
		feedOffline = (RelativeLayout) findViewById(R.id.feed_offline);
		feedOfflineReload = (ImageButton) findViewById(R.id.feed_offline_pic);
		
		// Set the pager adapter
		mAdapter = new MyAdapter(getSupportFragmentManager(), 0);
        mPager.setAdapter(mAdapter);
        
        // Load the feed
        updateFeed();
		
		// Action: Go to feed page
		feedButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	updateFeed();
		    }
		});
		
		// Action: Go to camera page
		cameraButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Intent intent = new Intent(v.getContext(), NewMomentActivity.class);
		    	startActivityForResult(intent, 0);
		    }
		});
		
		// Action: Go to settings page
		settingsButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Intent intent = new Intent(v.getContext(), SettingsActivity.class);
		    	//intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		    	startActivityForResult(intent, 0);
		    }
		});
		
		// Action: Reload when offline
		feedOfflineReload.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	feedOffline.setVisibility(View.INVISIBLE);
		    	feedLoading.setVisibility(View.VISIBLE);
		    	updateFeed();
		    }
		});
	}
	
	public static class MyAdapter extends FragmentStatePagerAdapter {
    	private int mSize;
    	
    	public MyAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            return MomentFragment.newInstance(position);
        }
        
        public void setSize(int size) {
        	mSize = size;
        }
        
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
	
	public static class MomentFragment extends Fragment {
        private static final String IMAGE_POSITION = "imgPos";
		private static final String TAG = "MomentFragment";
		private Moment moment;
        private ParseImageView imageView;
        private TextView momentAuthorView;
        private TextView momentCaptionView;
        private TextView momentCreatedView;
        private ImageButton momentLikeView;
        private ImageButton momentMoreButton;
        private TextView momentLikeCount;
        private PopupMenu popup;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static MomentFragment newInstance(int num) {
        	MomentFragment f = new MomentFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt(IMAGE_POSITION, num);
            f.setArguments(args);

            return f;
        }
        
        // Empty constructor, required as per Fragment docs
        public MomentFragment() {}

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            int mImageNum = getArguments() != null ? getArguments().getInt(IMAGE_POSITION) : -1;
            moment = moments.get(mImageNum);
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_feed_pager_details, container, false);
            imageView = (ParseImageView) v.findViewById(R.id.imageView);
            //imageView.setPlaceholder(getResources().getDrawable(R.drawable.placeholder));
            imageView.setPlaceholder(null);
            momentAuthorView = (TextView) v.findViewById(R.id.moment_author);
            momentCaptionView = (TextView) v.findViewById(R.id.moment_caption);
            momentCreatedView = (TextView) v.findViewById(R.id.moment_created);
            momentLikeView = (ImageButton) v.findViewById(R.id.moment_like);
            momentMoreButton = (ImageButton) v.findViewById(R.id.moment_more);
            momentLikeCount = (TextView) v.findViewById(R.id.moment_like_count);
            
            // Action: Click on LIKE
            momentLikeView.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	// Unlike
    		    	if (moment.isLiked()) {
    		    		momentLikeView.setImageResource(R.drawable.icon_love);
    		    		unlikeMoment();
    		    	// Like
    		    	} else {
	    		    	momentLikeView.setImageResource(R.drawable.icon_love_red);
	    		    	likeMoment();
    		    	}
    		    }
    		});
            
            // Create popup menu
            popup = new PopupMenu(getActivity(), momentMoreButton);
	        MenuInflater menuInflater = popup.getMenuInflater();
	        menuInflater.inflate(R.menu.moment_menu, popup.getMenu());
	        
	        // Handle menu item clicks
	        popup.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
		            @Override
		            public boolean onMenuItemClick(MenuItem item) {
		                switch (item.getItemId()) {
			                case R.id.report:
				            	Log.i(TAG, "Clicked on 'Report'");
				            	reportProblem();
				                return true;
				            case R.id.save:
				            	try {
					            	byte[] data = moment.getPhotoFile().getData();
					            	Bitmap momentImage = BitmapFactory.decodeByteArray(data, 0, data.length);
					            	BitmapHelper.saveImageInGallery(getActivity(), momentImage);
					            	Toast.makeText(
										getActivity().getApplicationContext(),
										"Picture successfully saved",
										Toast.LENGTH_SHORT).show();
				            	} catch (FileNotFoundException e) {
									Toast.makeText(
										getActivity().getApplicationContext(),
										"Sorry, picture could not be saved...",
										Toast.LENGTH_SHORT).show();
				            	} catch (IOException e) {
									Toast.makeText(
										getActivity().getApplicationContext(),
										"Sorry, picture could not be saved...",
										Toast.LENGTH_SHORT).show();
				            	} catch (ParseException e) {
				            		Toast.makeText(
										getActivity().getApplicationContext(),
										"Sorry, picture could not be saved...",
										Toast.LENGTH_SHORT).show();
									e.printStackTrace();
								}
				                return true;
				            default:
				                return false;
		                }
		            }
                }
            );
            
            // Action: Click on MORE
            momentMoreButton.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		        popup.show();
    		    }
    		});
            
            return v;
        }
        
        public void reportProblem() {
        	// Create report
        	Report report = new Report();
        	report.setFromUser(ParseUser.getCurrentUser());
        	report.setMoment(moment);
	    	// Save report to Parse
        	report.saveInBackground();
	    	
        	// Notify user
        	Toast.makeText(
					getActivity().getApplicationContext(),
					"Your report has been sent. Thank you for your help!",
					Toast.LENGTH_SHORT).show();
        }
        
        public void unlikeMoment() {
        	ParseQuery<Like> query1 = ParseQuery.getQuery(Like.class);
        	query1.whereEqualTo("moment", moment);
        	query1.whereEqualTo("fromUser", ParseUser.getCurrentUser());
	    	query1.findInBackground(new FindCallback<Like>() {
	            public void done(List<Like> likes, ParseException e) {
	                if (e == null) {
	                	if (likes.size() > 0) {
	                		likes.get(0).deleteInBackground(new DeleteCallback() {
	                			public void done(ParseException e) {
	                				if (e == null) {
	                					updateLikeCount();
	                				} else {
	                					momentLikeView.setImageResource(R.drawable.icon_love_red);
	            						Toast.makeText(
	            								getActivity().getApplicationContext(),
	            								"Error saving: " + e.getMessage(),
	            								Toast.LENGTH_SHORT).show();
            				     	}
	                			}
	                		});
	                	}
	                } else {
	                    Log.d("backfront", "Error: " + e.getMessage());
	                }
	            }
	        });
        }
        
        public void likeMoment() {
        	Like like = new Like();
	    	like.setFromUser(ParseUser.getCurrentUser());
	    	like.setMoment(moment);
	    	
	    	// Save the like to Parse
	    	like.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						updateLikeCount();
					} else {
						momentLikeView.setImageResource(R.drawable.icon_love);
						Toast.makeText(
								getActivity().getApplicationContext(),
								"Error saving: " + e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}

			});
        }
        
        public void updateLikeCount() {
        	ParseQuery<Like> query1 = ParseQuery.getQuery(Like.class);
        	query1.whereEqualTo("moment", moment);
        	query1.whereEqualTo("fromUser", ParseUser.getCurrentUser());
	    	query1.findInBackground(new FindCallback<Like>() {
	            public void done(List<Like> likes, ParseException e) {
	                if (e == null) {
	                	if (likes.size() > 0) {
	                		momentLikeView.setImageResource(R.drawable.icon_love_red);
	                		moment.isLiked(true);
	                	} else {
	                		momentLikeView.setImageResource(R.drawable.icon_love);
	                		moment.isLiked(false);
	                	}
	                } else {
	                    Log.d("backfront", "Error: " + e.getMessage());
	                }
	            }
	        });
        	
        	ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        	query.whereEqualTo("moment", moment);
	    	query.findInBackground(new FindCallback<Like>() {
	            public void done(List<Like> likes, ParseException e) {
	                if (e == null) {
	                	if (likes.size() > 0) {
	                		momentLikeCount.setText(likes.size()+"");
	                	} else {
	                		momentLikeCount.setText("");
	                	}
	                } else {
	                    Log.d("backfront", "Error: " + e.getMessage());
	                }
	            }
	        });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (moments.size() > 0) {
	            imageView.setParseFile(moment.getPhotoFile());
	            imageView.loadInBackground(new GetDataCallback() {
	            	@Override
	            	public void done(byte[] data, ParseException e) {
	            		momentLikeView.setVisibility(View.VISIBLE);
	            		momentMoreButton.setVisibility(View.VISIBLE);
	            		updateLikeCount();
	            	}
	            });
	            
	            momentAuthorView.setText(moment.getAuthor().getUsername());
	            
	            if (moment.getCaption() != null && !moment.getCaption().trim().equals("")) {
		            momentCaptionView.setText(moment.getCaption());
		            momentCaptionView.setVisibility(View.VISIBLE);
	            }
	            String location = moment.getLocationDescription();
	            if (!location.isEmpty()) {
	            	momentCreatedView.setText(Utils.getTimeFromDateToNow(moment.getCreatedAt()) + " from " + location);
	            } else {
	            	momentCreatedView.setText(Utils.getTimeFromDateToNow(moment.getCreatedAt()));
	            }
            }
        }
    }
	
	public void updateFeed() {
		if (!Utils.hasConnection(this)) {
			Log.d("backfront", "No internet connection...");
            feedLoading.setVisibility(View.INVISIBLE);
            feedOffline.setVisibility(View.VISIBLE);
		} else {
			Log.d("backfront", "Internet connection found :-)");
	    	ParseQuery<Moment> query = ParseQuery.getQuery(Moment.class);
	    	query.include("author");
	        query.orderByDescending("createdAt");
	        query.findInBackground(new FindCallback<Moment>() {
	            public void done(List<Moment> momentList, ParseException e) {
	                if (e == null) {
	                	Log.d("backfront", "Retrieved " + momentList.size() + " moments");
	                	moments = momentList;
	                    mAdapter.setSize(moments.size());
	                    mAdapter.notifyDataSetChanged();
	                    mPager.setCurrentItem(0);
	                    feedLoading.setVisibility(View.INVISIBLE);
	                } else {
	                    Log.d("backfront", "Error: " + e.getMessage());
	                    feedLoading.setVisibility(View.INVISIBLE);
	                    feedOffline.setVisibility(View.VISIBLE);
	                }
	            }
	        });
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			updateFeed();
		}
	}
}