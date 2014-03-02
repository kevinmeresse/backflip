package com.km.backfront.ui.adapter;

import java.util.List;

import se.emilsjolander.flipview.FlipView;

import com.km.backfront.R;
import com.km.backfront.model.Like;
import com.km.backfront.model.Moment;
import com.km.backfront.model.Report;
import com.km.backfront.ui.ProfileActivity;
import com.km.backfront.util.BitmapHelper;
import com.km.backfront.util.Utils;
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

public class FlipAdapter extends BaseAdapter implements OnClickListener {

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
	
	public FlipAdapter(Activity activity, FlipView flipview, View offlineView, View refreshAndLoadMore, View feedMessage) {
		inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.flipview = flipview;
		this.offlineView = offlineView;
		this.refreshAndLoadMore = refreshAndLoadMore;
		this.feedMessage = feedMessage;
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
		final Moment moment = moments.get(position);
		
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.fragment_feed_pager_details, parent, false);
			
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
			
			/*holder.firstPage.setOnClickListener(this);
			holder.lastPage.setOnClickListener(this);*/
			
			convertView.setTag(holder);
		} else {
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
			            	Log.i(TAG, "Clicked on 'Report'");
			            	reportProblem(moment);
			                return true;
			            case R.id.save:
			            	try {
				            	byte[] data = moment.getPhotoFile().getData();
				            	Bitmap momentImage = BitmapFactory.decodeByteArray(data, 0, data.length);
				            	BitmapHelper.saveImageInGallery(activity, momentImage);
				            	Utils.showToast(activity, "Picture successfully saved");
			            	} catch (Exception e) {
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
		    	holder.popup.show();
		    }
		});
		
		// Action: Click on author
		holder.momentAuthorView.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
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
		    	holder.momentLoading.setVisibility(View.VISIBLE);
		    	holder.imageBadPreview.setVisibility(View.VISIBLE);
		    	holder.pictureNotFoundView.setVisibility(View.GONE);
		    	holder.imageBadPreview.loadInBackground();
		    	holder.imageView.loadInBackground(new GetDataCallback() {
	            	@Override
	            	public void done(byte[] data, ParseException e) {
	            		if (e == null) {
	            			holder.momentLikeView.setVisibility(View.VISIBLE);
	            			holder.momentMoreButton.setVisibility(View.VISIBLE);
		            		displayLikeCount(moment, holder);
	            		} else {
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
            			holder.momentLikeView.setVisibility(View.VISIBLE);
            			holder.momentMoreButton.setVisibility(View.VISIBLE);
            			displayLikeCount(moment, holder);
            		} else {
            			holder.momentLoading.setVisibility(View.GONE);
            			holder.imageBadPreview.setVisibility(View.GONE);
            			holder.pictureNotFoundView.setVisibility(View.VISIBLE);
            		}
            	}
            });
			
			holder.momentAuthorView.setText(moment.getAuthor().getUsername());
            
            if (moment.getCaption() != null && !moment.getCaption().trim().equals("")) {
            	holder.momentCaptionView.setText(moment.getCaption());
            	holder.momentCaptionView.setVisibility(View.VISIBLE);
            } else {
            	holder.momentCaptionView.setVisibility(View.GONE);
            }
            String location = moment.getLocationDescription();
            if (!Utils.isEmptyString(location)) {
            	holder.momentCreatedView.setText(Utils.getTimeFromDateToNow(moment.getCreatedAt()) + " from " + location);
            } else {
            	holder.momentCreatedView.setText(Utils.getTimeFromDateToNow(moment.getCreatedAt()));
            }
            if (moment.getIsFavorite()) {
            	holder.momentPicked.setVisibility(View.VISIBLE);
            }
		}
		return holder;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		/*case R.id.first_page:
			if(callback != null){
				callback.onPageRequested(0);
			}
			break;*/
		}
	}
	
	public void updateFeed() {
		feedMessage.setVisibility(View.VISIBLE);
		if (!Utils.hasConnection(activity)) {
			Log.d(TAG, "No internet connection...");
            offlineView.setVisibility(View.VISIBLE);
            // Notify user
        	Utils.showToast(activity, "No internet connection...");
        	feedMessage.setVisibility(View.GONE);
		} else {
			if (!isUpdating) {
				isUpdating = true;
				Log.d(TAG, "Internet connection found :-)");
				Utils.searchMomentsForFeed(0, 20, new FindCallback<Moment>() {
		            public void done(List<Moment> momentList, ParseException e) {
		                if (e == null) {
		                	Log.d(TAG, "Retrieved " + momentList.size() + " moments");
		                	flipview.setVisibility(View.VISIBLE);
		                	moments = momentList;
		                    notifyDataSetChanged();
		                    flipview.flipTo(0);
		                    flipview.peakNext(true);
		                    skip = momentList.size();
		                    offlineView.setVisibility(View.GONE);
		                    refreshAndLoadMore.setVisibility(View.VISIBLE);
		                } else {
		                    Log.e(TAG, "Error: " + e.getMessage());
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
	
	public void addMoreMoments(int amount) {
		if (!isUpdating) {
			if (!Utils.hasConnection(activity)) {
				Log.d(TAG, "No internet connection...");
	        } else {
				Log.d(TAG, "Internet connection found :-)");
				isUpdating = true;
				Utils.searchMomentsForFeed(skip, 20, new FindCallback<Moment>() {
		            public void done(List<Moment> momentList, ParseException e) {
		                if (e == null) {
		                	Log.d(TAG, "Retrieved " + momentList.size() + " moments");
		                	if (momentList != null && momentList.size() > 0) {
			                	for (Moment moment : momentList) {
									moments.add(moment);
								}
			                    notifyDataSetChanged();
			                    skip = skip + momentList.size();
		                	}
		                } else {
		                    Log.e(TAG, "Error: " + e.getMessage());
		                    offlineView.setVisibility(View.VISIBLE);
		                }
		                isUpdating = false;
		            }
		        });
			}
		}
	}
	
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
	
	public void displayLikeCount(final Moment moment, final ViewHolder holder) {
		if (moment.isLiked() != Moment.LIKE_UNDEFINED) {
			if (moment.isLiked() == Moment.LIKED) {
				holder.momentLikeView.setImageResource(R.drawable.icon_like_red);
			} else if (moment.isLiked() == Moment.NOT_LIKED) {
				holder.momentLikeView.setImageResource(R.drawable.icon_like);
			}
			if (moment.getLikeCount() > 0) {
				holder.momentLikeCount.setText(moment.getLikeCount()+"");
        		holder.momentLikeBoxTop.setVisibility(View.VISIBLE);
        		holder.momentLikeBoxBottomOutside.setVisibility(View.VISIBLE);
        		holder.momentLikeBoxBottomInside.setVisibility(View.VISIBLE);
			} else {
				holder.momentLikeCount.setText("");
        		holder.momentLikeBoxTop.setVisibility(View.INVISIBLE);
        		holder.momentLikeBoxBottomOutside.setVisibility(View.INVISIBLE);
        		holder.momentLikeBoxBottomInside.setVisibility(View.INVISIBLE);
			}
		} else {
			// Check if current user likes this moment
	    	if (ParseUser.getCurrentUser() != null) {
	        	ParseQuery<Like> query1 = ParseQuery.getQuery(Like.class);
	        	query1.whereEqualTo("moment", moment);
	        	query1.whereEqualTo("fromUser", ParseUser.getCurrentUser());
	        	query1.countInBackground(new CountCallback() {
		            public void done(int count, ParseException e) {
		            	
		                if (e == null) {
		                	if (count > 0) {
		                		holder.momentLikeView.setImageResource(R.drawable.icon_like_red);
		                		moment.isLiked(Moment.LIKED);
		                	} else {
		                		holder.momentLikeView.setImageResource(R.drawable.icon_like);
		                		moment.isLiked(Moment.NOT_LIKED);
		                	}
		                } else {
		                    Log.e(TAG, "Error when counting likes: " + e.getMessage());
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
	                	if (count > 0) {
	                		holder.momentLikeCount.setText(count+"");
	                		holder.momentLikeBoxTop.setVisibility(View.VISIBLE);
	                		holder.momentLikeBoxBottomOutside.setVisibility(View.VISIBLE);
	                		holder.momentLikeBoxBottomInside.setVisibility(View.VISIBLE);
	                	} else {
	                		holder.momentLikeCount.setText("");
	                		holder.momentLikeBoxTop.setVisibility(View.INVISIBLE);
	                		holder.momentLikeBoxBottomOutside.setVisibility(View.INVISIBLE);
	                		holder.momentLikeBoxBottomInside.setVisibility(View.INVISIBLE);
	                	}
	                } else {
	                    Log.e(TAG, "Error when counting likes: " + e.getMessage());
	                    e.printStackTrace();
	                }
	            }
	        });
		}
    }
	
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
	    	/*like.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						updateLikeCount(moment, holder);
					} else {
						holder.momentLikeView.setImageResource(R.drawable.icon_like);
						Log.e(TAG, "Error saving: " + e.getMessage());
					}
				}

			});*/
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
	                		/*likes.get(0).deleteInBackground(new DeleteCallback() {
	                			public void done(ParseException e) {
	                				if (e == null) {
	                					updateLikeCount(moment, holder);
	                				} else {
	                					holder.momentLikeView.setImageResource(R.drawable.icon_like_red);
	                					Log.e(TAG, "Error saving: " + e.getMessage());
            				     	}
	                			}
	                		});*/
	                	}
	                } else {
	                	Log.e(TAG, "Error: " + e.getMessage());
	                	e.printStackTrace();
	                }
	            }
	        });
    	}
    }
}
