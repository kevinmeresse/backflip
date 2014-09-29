package com.km.backflip.ui.adapter;

import java.util.List;

import se.emilsjolander.flipview.FlipView;

import com.crittercism.app.Crittercism;
import com.km.backflip.model.Like;
import com.km.backflip.model.Moment;
import com.km.backflip.model.Report;
import com.km.backflip.ui.ProfileActivity;
import com.km.backflip.util.BitmapHelper;
import com.km.backflip.util.Utils;
import com.km.backflip.R;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FlipAdapter2 extends BaseAdapter implements OnClickListener {

	private static final String TAG = "FlipAdapter";

	public interface FlipCallback{
		public void onPageRequested(int page);
	}
	
	public List<Moment> moments;
	private Activity activity;
	private LayoutInflater inflater;
	private FlipCallback callback;
	private FlipView flipview;
	private View offlineView;
	private View refreshAndLoadMore;
	private View feedMessage;
	private int skip = 0;
	private boolean isUpdating = false;
	
	/*
	 * Constructor
	 */
	public FlipAdapter2(Activity activity, FlipView flipview, View offlineView, View refreshAndLoadMore, View feedMessage) {
		inflater = LayoutInflater.from(activity);
		
		// Set class attributes
		this.activity = activity;
		this.flipview = flipview;
		this.offlineView = offlineView;
		this.refreshAndLoadMore = refreshAndLoadMore;
		this.feedMessage = feedMessage;
		
		// Update the feed
		updateFeed();
	}
	
	public void setCallback(FlipCallback callback) {
		this.callback = callback;
	}

	@Override
	public int getCount() {
		if (moments == null) return 0;
		return moments.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		
		// Get the moment we want to display according to the given position
		final Moment moment = moments.get(position);
		
		// In case the view is not created yet
		if (convertView == null) {
			// Inflate the pager layout
			convertView = inflater.inflate(R.layout.fragment_feed_pager_details, parent, false);
			
			// Create the holder containing all the objects we need
			holder = new ViewHolder();
			holder.imageBadPreview = (ParseImageView) convertView.findViewById(R.id.imageBadPreview);
			holder.imageView = (ParseImageView) convertView.findViewById(R.id.imageView);
			holder.momentAuthorView = (TextView) convertView.findViewById(R.id.moment_author);
			holder.momentCaptionView = (TextView) convertView.findViewById(R.id.moment_caption);
			holder.momentCreatedView = (TextView) convertView.findViewById(R.id.moment_created);
			holder.momentPicked = (ImageView) convertView.findViewById(R.id.moment_picked);
			holder.momentLikeView = (ImageButton) convertView.findViewById(R.id.moment_like);
			holder.momentMoreButton = (ImageButton) convertView.findViewById(R.id.moment_more);
			holder.momentLikeCount = (TextView) convertView.findViewById(R.id.moment_like_count);
			holder.momentLikeBoxTop = (ImageView) convertView.findViewById(R.id.moment_like_box_top);
			holder.momentLikeBoxBottomOutside = (ImageView) convertView.findViewById(R.id.moment_like_box_bottom_outside);
			holder.momentLikeBoxBottomInside = (ImageButton) convertView.findViewById(R.id.moment_like_box_bottom_inside);
			holder.pictureNotFoundView = (RelativeLayout) convertView.findViewById(R.id.picture_not_found);
			holder.momentLoading = (ProgressBar) convertView.findViewById(R.id.moment_loading);
			
			convertView.setTag(holder);
		} else {
			// Most of the time we will just re-use one of the 3 already created views
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Create popup menu
		holder.popup = new PopupMenu(activity, holder.momentMoreButton);
        MenuInflater menuInflater = holder.popup.getMenuInflater();
        menuInflater.inflate(R.menu.moment_menu, holder.popup.getMenu());
        
        // Handle menu item clicks
        holder.popup.setOnMenuItemClickListener(
            new PopupMenu.OnMenuItemClickListener() {
	            @Override
	            public boolean onMenuItemClick(MenuItem item) {
	                switch (item.getItemId()) {
		                case R.id.report:
		                	// Report a problem
			            	reportProblem(moment);
			                return true;
			            case R.id.save:
			            	// Save the current picture in the user's photo album
			            	try {
				            	byte[] data = moment.getPhotoFile().getData();
				            	Bitmap momentImage = BitmapFactory.decodeByteArray(data, 0, data.length);
				            	BitmapHelper.saveImageInGallery(activity, momentImage);
				            	Utils.showToast(activity, "Picture successfully saved");
			            	} catch (Exception e) {
			            		Crittercism.logHandledException(e);
								Utils.showToast(activity, "Sorry, picture could not be saved...");
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
		holder.momentMoreButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Show the popup menu
		    	holder.popup.show();
		    }
		});
		
		// Action: Click on AUTHOR
		holder.momentAuthorView.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Start a new activity displaying the profile of a specific user
		    	Intent intent = new Intent(v.getContext(), ProfileActivity.class);
		    	intent.putExtra("USER_ID", moment.getAuthor().getObjectId());
		    	intent.putExtra("USERNAME", moment.getAuthor().getUsername());
		    	activity.startActivity(intent);
		    }
		});
		
		// Action: Click on LIKE
		holder.momentLikeView.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Unlike
		    	if (moment.isLiked() == Moment.LIKED) {
		    		unlikeMoment(moment, holder);
		    	// Like
		    	} else {
    		    	likeMoment(moment, holder);
		    	}
		    }
		});
		
		// Action: Reload picture
		holder.pictureNotFoundView.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Show loading icon
		    	holder.momentLoading.setVisibility(View.VISIBLE);
		    	
		    	// Hide error message
		    	holder.pictureNotFoundView.setVisibility(View.GONE);
		    	
		    	// Show bad preview surface and load picture from server
		    	holder.imageBadPreview.setVisibility(View.VISIBLE);
		    	holder.imageBadPreview.loadInBackground();
		    	
		    	// Load full picture
		    	holder.imageView.loadInBackground(new GetDataCallback() {
	            	@Override
	            	public void done(byte[] data, ParseException e) {
	            		if (e == null) {
	            			// Display number of likes and menu only when the picture is loaded
	            			holder.momentLikeView.setVisibility(View.VISIBLE);
	            			holder.momentMoreButton.setVisibility(View.VISIBLE);
		            		displayLikeCount(moment, holder);
	            		} else {
	            			// Display error message again
	            			holder.momentLoading.setVisibility(View.GONE);
	            			holder.imageBadPreview.setVisibility(View.GONE);
	            			holder.pictureNotFoundView.setVisibility(View.VISIBLE);
	            		}
	            	}
	            });
		    }
        });
		
		setViewContent(moment, holder);
		return convertView;
	}
	
	/*
	 * Holder containing all the objects we need to display
	 */
	static class ViewHolder {
		ParseImageView imageBadPreview;
        ParseImageView imageView;
        TextView momentAuthorView;
        TextView momentCaptionView;
        TextView momentCreatedView;
        ImageView momentPicked;
        ImageButton momentLikeView;
        ImageButton momentMoreButton;
        TextView momentLikeCount;
        PopupMenu popup;
        ImageView momentLikeBoxTop;
        ImageView momentLikeBoxBottomOutside;
        ImageButton momentLikeBoxBottomInside;
        RelativeLayout pictureNotFoundView;
        ProgressBar momentLoading;
	}
	
	/*
	 * Fill the holder with the right information from the specific moment
	 */
	public ViewHolder setViewContent(final Moment moment, final ViewHolder holder) {
		
		if (moment != null) {
			// Reset default design
			holder.momentLoading.setVisibility(View.VISIBLE);
			holder.imageBadPreview.setVisibility(View.VISIBLE);
			holder.pictureNotFoundView.setVisibility(View.GONE);
			holder.momentLikeView.setVisibility(View.GONE);
			holder.momentMoreButton.setVisibility(View.GONE);
			holder.momentPicked.setVisibility(View.INVISIBLE);
			holder.momentLikeCount.setText("");
    		holder.momentLikeBoxTop.setVisibility(View.GONE);
    		holder.momentLikeBoxBottomOutside.setVisibility(View.GONE);
    		holder.momentLikeBoxBottomInside.setVisibility(View.GONE);
    		holder.momentLikeView.setImageResource(R.drawable.icon_like);
			
			// Load the bad preview first
			holder.imageBadPreview.setParseFile(moment.getBadPreview());
			holder.imageBadPreview.loadInBackground();
			
			// Then load the full picture
			holder.imageView.setParseFile(moment.getPhotoFile());
			holder.imageView.loadInBackground(new GetDataCallback() {
            	@Override
            	public void done(byte[] data, ParseException e) {
            		if (e == null) {
            			// Display number of likes and menu only when the picture is loaded
            			holder.momentLikeView.setVisibility(View.VISIBLE);
            			holder.momentMoreButton.setVisibility(View.VISIBLE);
            			displayLikeCount(moment, holder);
            		} else {
            			// Display error message
            			holder.momentLoading.setVisibility(View.GONE);
            			holder.imageBadPreview.setVisibility(View.GONE);
            			holder.pictureNotFoundView.setVisibility(View.VISIBLE);
            		}
            	}
            });
			
			// Set the author name
			holder.momentAuthorView.setText(moment.getAuthor().getUsername());
            
			// Set the caption
            if (moment.getCaption() != null && !moment.getCaption().trim().equals("")) {
            	holder.momentCaptionView.setText(moment.getCaption());
            	holder.momentCaptionView.setVisibility(View.VISIBLE);
            } else {
            	holder.momentCaptionView.setVisibility(View.GONE);
            }
            
            // Set location and date
            String location = moment.getLocationDescription();
            if (!Utils.isEmptyString(location)) {
            	holder.momentCreatedView.setText(Utils.getTimeFromDateToNow(moment.getCreatedAt()) + " from " + location);
            } else {
            	holder.momentCreatedView.setText(Utils.getTimeFromDateToNow(moment.getCreatedAt()));
            }
            
            // Display favorite label if needed
            if (moment.getIsFavorite()) {
            	holder.momentPicked.setVisibility(View.VISIBLE);
            }
		}
		return holder;
	}
	
	/*
	 * Update the feed
	 */
	public void updateFeed() {
		feedMessage.setVisibility(View.VISIBLE);
		
		// Handle offline
		if (!Utils.hasConnection(activity)) {
			offlineView.setVisibility(View.VISIBLE);
            // Notify user
        	Utils.showToast(activity, "No internet connection...");
        	feedMessage.setVisibility(View.GONE);
		} else {
			if (!isUpdating) {
				isUpdating = true;
				// For performance sake, search only for the first 20 moments
				Utils.searchMomentsForFeed(0, 20, new FindCallback<Moment>() {
		            public void done(List<Moment> momentList, ParseException e) {
		                if (e == null) {
		                	flipview.setVisibility(View.VISIBLE);
		                	// Update the whole list of moments, notify adapter and go to the first one
		                	moments = momentList;
		                    notifyDataSetChanged();
		                    flipview.flipTo(0);
		                    flipview.peakNext(true);
		                    
		                    // Save how many we need to skip when we load more
		                    skip = momentList.size();
		                    offlineView.setVisibility(View.GONE);
		                    refreshAndLoadMore.setVisibility(View.VISIBLE);
		                } else {
		                	// Display error message
		                    offlineView.setVisibility(View.VISIBLE);
		                    refreshAndLoadMore.setVisibility(View.GONE);
		                }
		                isUpdating = false;
		                feedMessage.setVisibility(View.GONE);
		            }
		        });
			}
		}
    }
	
	/*
	 * Add more moments (triggered automatically when reached moment before the last one)
	 */
	public void addMoreMoments(int amount) {
		if (!isUpdating) {
			if (!Utils.hasConnection(activity)) {
				// Do nothing for now
	        } else {
				isUpdating = true;
				
				// Search for the next 20 moments
				Utils.searchMomentsForFeed(skip, 20, new FindCallback<Moment>() {
		            public void done(List<Moment> momentList, ParseException e) {
		                if (e == null) {
		                	if (momentList != null && momentList.size() > 0) {
		                		// Append new moments to the main list and notify adapter
			                	for (Moment moment : momentList) {
									moments.add(moment);
								}
			                    notifyDataSetChanged();
			                    
			                    // Update skip count
			                    skip = skip + momentList.size();
		                	}
		                } else {
		                	// Display error message
		                    offlineView.setVisibility(View.VISIBLE);
		                }
		                isUpdating = false;
		            }
		        });
			}
		}
	}
	
	/*
	 * Report a problem
	 */
	public void reportProblem(Moment moment) {
    	// Create report
    	Report report = new Report();
    	report.setFromUser(ParseUser.getCurrentUser());
    	report.setMoment(moment);
    	
    	// Save report to Parse
    	report.saveInBackground();
    	
    	// Notify user
    	Utils.showToast(activity, "Your report has been sent. Thank you for your help!", Toast.LENGTH_LONG);
    }
	
	/*
	 * Display the number of likes for a specific moment
	 */
	public void displayLikeCount(final Moment moment, final ViewHolder holder) {
		// Check if already retrieved the number of likes
		if (moment.isLiked() != Moment.LIKE_UNDEFINED) {
			// Display the right icon
			if (moment.isLiked() == Moment.LIKED) {
				holder.momentLikeView.setImageResource(R.drawable.icon_like_red);
			} else if (moment.isLiked() == Moment.NOT_LIKED) {
				holder.momentLikeView.setImageResource(R.drawable.icon_like);
			}
			// Display the number
			if (moment.getLikeCount() > 0) {
				holder.momentLikeCount.setText(moment.getLikeCount()+"");
        		holder.momentLikeBoxTop.setVisibility(View.VISIBLE);
        		holder.momentLikeBoxBottomOutside.setVisibility(View.VISIBLE);
        		holder.momentLikeBoxBottomInside.setVisibility(View.VISIBLE);
        	// Or not if no one likes it
			} else {
				holder.momentLikeCount.setText("");
        		holder.momentLikeBoxTop.setVisibility(View.INVISIBLE);
        		holder.momentLikeBoxBottomOutside.setVisibility(View.INVISIBLE);
        		holder.momentLikeBoxBottomInside.setVisibility(View.INVISIBLE);
			}
			
		// If first time, we need to request the server
		} else {
			// First check if current user likes this moment
	    	if (ParseUser.getCurrentUser() != null) {
	        	ParseQuery<Like> query1 = ParseQuery.getQuery(Like.class);
	        	query1.whereEqualTo("moment", moment);
	        	query1.whereEqualTo("fromUser", ParseUser.getCurrentUser());
	        	query1.countInBackground(new CountCallback() {
		            public void done(int count, ParseException e) {
		                if (e == null) {
		                	// Display the right icon
		                	if (count > 0) {
		                		holder.momentLikeView.setImageResource(R.drawable.icon_like_red);
		                		moment.isLiked(Moment.LIKED);
		                	} else {
		                		holder.momentLikeView.setImageResource(R.drawable.icon_like);
		                		moment.isLiked(Moment.NOT_LIKED);
		                	}
		                } else {
		                    e.printStackTrace();
		                }
		            }
		        });
	    	}
	    	
	    	// Retrieve the number of likes for this moment
	    	ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
	    	query.whereEqualTo("moment", moment);
	    	query.countInBackground(new CountCallback() {
	            public void done(int count, ParseException e) {
	                if (e == null) {
	                	moment.setLikeCount(count);
	                	// Display the number
	                	if (count > 0) {
	                		holder.momentLikeCount.setText(count+"");
	                		holder.momentLikeBoxTop.setVisibility(View.VISIBLE);
	                		holder.momentLikeBoxBottomOutside.setVisibility(View.VISIBLE);
	                		holder.momentLikeBoxBottomInside.setVisibility(View.VISIBLE);
	                	// Or not if no one likes it
	                	} else {
	                		holder.momentLikeCount.setText("");
	                		holder.momentLikeBoxTop.setVisibility(View.INVISIBLE);
	                		holder.momentLikeBoxBottomOutside.setVisibility(View.INVISIBLE);
	                		holder.momentLikeBoxBottomInside.setVisibility(View.INVISIBLE);
	                	}
	                } else {
	                    e.printStackTrace();
	                }
	            }
	        });
		}
    }
	
	/*
	 * Like the current moment
	 */
	public void likeMoment(final Moment moment, final ViewHolder holder) {
		if (Utils.userLoggedIn(activity)) {
    		// Change view to be activated
    		holder.momentLikeView.setImageResource(R.drawable.icon_like_red);
    		
    		// Like moment and increment like count
    		moment.isLiked(Moment.LIKED);
    		int likeCount = moment.getLikeCount() + 1;
    		moment.setLikeCount(likeCount);
    		holder.momentLikeCount.setText(likeCount+"");
    		holder.momentLikeBoxTop.setVisibility(View.VISIBLE);
    		holder.momentLikeBoxBottomOutside.setVisibility(View.VISIBLE);
    		holder.momentLikeBoxBottomInside.setVisibility(View.VISIBLE);
    		
    		// Create like
        	Like like = new Like();
	    	like.setFromUser(ParseUser.getCurrentUser());
	    	like.setMoment(moment);
	    	
	    	// Save the like to Parse
	    	like.saveInBackground();
    	}
    }
	
	public void unlikeMoment(final Moment moment, final ViewHolder holder) {
		if (Utils.userLoggedIn(activity)) {
    		// Change view to be deactivated
    		holder.momentLikeView.setImageResource(R.drawable.icon_like);
    		
    		// Unlike moment and increment like count
    		moment.isLiked(Moment.NOT_LIKED);
    		int likeCount = moment.getLikeCount() - 1;
    		if (likeCount < 1) {
    			likeCount = 0;
    			holder.momentLikeCount.setText("");
        		holder.momentLikeBoxTop.setVisibility(View.INVISIBLE);
        		holder.momentLikeBoxBottomOutside.setVisibility(View.INVISIBLE);
        		holder.momentLikeBoxBottomInside.setVisibility(View.INVISIBLE);
    		} else {
    			holder.momentLikeCount.setText(likeCount+"");
    		}
    		moment.setLikeCount(likeCount);
    		
    		
    		// Search like and delete it
        	ParseQuery<Like> query1 = ParseQuery.getQuery(Like.class);
        	query1.whereEqualTo("moment", moment);
        	query1.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        	query1.findInBackground(new FindCallback<Like>() {
	            public void done(List<Like> likes, ParseException e) {
	                if (e == null) {
	                	if (likes.size() > 0) {
	                		likes.get(0).deleteInBackground();
	                	}
	                } else {
	                	e.printStackTrace();
	                }
	            }
	        });
    	}
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
