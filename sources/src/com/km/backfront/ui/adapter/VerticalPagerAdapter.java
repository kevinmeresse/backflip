package com.km.backfront.ui.adapter;

import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.km.backfront.ui.ProfileActivity;
import com.km.backfront.ui.vertical.FragmentStatePagerAdapter;
import com.km.backfront.ui.vertical.VerticalViewPager;
import com.km.backfront.util.BitmapHelper;
import com.km.backfront.util.Utils;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class VerticalPagerAdapter extends FragmentStatePagerAdapter {

	private static final String TAG = "VerticalPagerAdapter";

	public static List<Moment> moments;
	private FragmentActivity context;
	private VerticalViewPager verticalPager;
	private View offlineView;
	
	public VerticalPagerAdapter(FragmentActivity context, VerticalViewPager verticalPager, View offlineView) {
        super(context.getSupportFragmentManager());
        this.context = context;
        this.verticalPager = verticalPager;
        this.offlineView = offlineView;
        updateFeed();
    }

    @Override
    public int getCount() {
    	if (moments == null) return 0;
        return moments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return MomentFragment.newInstance(position);
    }
    
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    
    
    public void updateFeed() {
		if (!Utils.hasConnection(context)) {
			Log.d(TAG, "No internet connection...");
            offlineView.setVisibility(View.VISIBLE);
		} else {
			Log.d(TAG, "Internet connection found :-)");
			Utils.searchMomentsForFeed(0, 0, new FindCallback<Moment>() {
	            public void done(List<Moment> momentList, ParseException e) {
	                if (e == null) {
	                	Log.d(TAG, "Retrieved " + momentList.size() + " moments");
	                	moments = momentList;
	                    notifyDataSetChanged();
	                    verticalPager.setCurrentItem(0);
	                } else {
	                    Log.e(TAG, "Error: " + e.getMessage());
	                    offlineView.setVisibility(View.VISIBLE);
	                }
	            }
	        });
		}
    }
    
    
    public static class MomentFragment extends Fragment {
		
        private static final String IMAGE_POSITION = "imgPos";
		private static final String TAG = "MomentFragment";
		private Moment moment;
		private ParseImageView imageBadPreview;
        private ParseImageView imageView;
        private TextView momentAuthorView;
        private TextView momentCaptionView;
        private TextView momentCreatedView;
        private ImageButton momentLikeView;
        private ImageButton momentMoreButton;
        private TextView momentLikeCount;
        private PopupMenu popup;
        private ImageView momentPicked;
        private ImageView momentLikeBoxTop;
        private ImageView momentLikeBoxBottomOutside;
        private ImageButton momentLikeBoxBottomInside;
        private RelativeLayout pictureNotFoundView;
        private ProgressBar momentLoading;

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
            if (moments != null) {
            	moment = moments.get(mImageNum);
            }
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_feed_pager_details, container, false);
            imageBadPreview = (ParseImageView) v.findViewById(R.id.imageBadPreview);
            imageView = (ParseImageView) v.findViewById(R.id.imageView);
            //imageView.setPlaceholder(getResources().getDrawable(R.drawable.placeholder));
            imageView.setPlaceholder(null);
            momentAuthorView = (TextView) v.findViewById(R.id.moment_author);
            momentCaptionView = (TextView) v.findViewById(R.id.moment_caption);
            momentCreatedView = (TextView) v.findViewById(R.id.moment_created);
            momentLikeView = (ImageButton) v.findViewById(R.id.moment_like);
            momentMoreButton = (ImageButton) v.findViewById(R.id.moment_more);
            momentLikeCount = (TextView) v.findViewById(R.id.moment_like_count);
            momentPicked = (ImageView) v.findViewById(R.id.moment_picked);
            momentLikeBoxTop = (ImageView) v.findViewById(R.id.moment_like_box_top);
            momentLikeBoxBottomOutside = (ImageView) v.findViewById(R.id.moment_like_box_bottom_outside);
            momentLikeBoxBottomInside = (ImageButton) v.findViewById(R.id.moment_like_box_bottom_inside);
            pictureNotFoundView = (RelativeLayout) v.findViewById(R.id.picture_not_found);
            momentLoading = (ProgressBar) v.findViewById(R.id.moment_loading);
            
            // Action: Click on LIKE
            momentLikeView.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	// Unlike
    		    	if (moment.isLiked()) {
    		    		unlikeMoment();
    		    	// Like
    		    	} else {
	    		    	likeMoment();
    		    	}
    		    }
    		});
            
            // Action: Reload picture
            pictureNotFoundView.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	momentLoading.setVisibility(View.VISIBLE);
        			imageBadPreview.setVisibility(View.VISIBLE);
        			pictureNotFoundView.setVisibility(View.GONE);
        			imageBadPreview.loadInBackground();
        			imageView.loadInBackground(new GetDataCallback() {
    	            	@Override
    	            	public void done(byte[] data, ParseException e) {
    	            		if (e == null) {
    	            			momentLikeView.setVisibility(View.VISIBLE);
    		            		momentMoreButton.setVisibility(View.VISIBLE);
    		            		updateLikeCount();
    	            		} else {
    	            			momentLoading.setVisibility(View.GONE);
    	            			imageBadPreview.setVisibility(View.GONE);
    	            			pictureNotFoundView.setVisibility(View.VISIBLE);
    	            		}
    	            	}
    	            });
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
					            	Utils.showToast(getActivity(), "Picture successfully saved");
				            	} catch (Exception e) {
									Utils.showToast(getActivity(), "Sorry, picture could not be saved...");
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
            
            // Action: Click on author
            momentAuthorView.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	Intent intent = new Intent(v.getContext(), ProfileActivity.class);
    		    	intent.putExtra("USER_ID", moment.getAuthor().getObjectId());
    		    	intent.putExtra("USERNAME", moment.getAuthor().getUsername());
    		    	startActivity(intent);
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
        	Utils.showToast(getActivity(), "Your report has been sent. Thank you for your help!", Toast.LENGTH_LONG);
        }
        
        public void likeMoment() {
        	if (Utils.userLoggedIn(getActivity())) {
        		// Change view to be activated
        		momentLikeView.setImageResource(R.drawable.icon_like_red);
        		
        		// Create like
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
							momentLikeView.setImageResource(R.drawable.icon_like);
							Log.e(TAG, "Error saving: " + e.getMessage());
						}
					}
	
				});
        	}
        }
        
        public void unlikeMoment() {
        	if (Utils.userLoggedIn(getActivity())) {
        		// Change view to be deactivated
        		momentLikeView.setImageResource(R.drawable.icon_like);
	    		
        		// Search like and delete it
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
		                					momentLikeView.setImageResource(R.drawable.icon_like_red);
		                					Log.e(TAG, "Error saving: " + e.getMessage());
	            				     	}
		                			}
		                		});
		                	}
		                } else {
		                	Log.d(TAG, "Error: " + e.getMessage());
		                }
		            }
		        });
        	}
        }
        
        public void updateLikeCount() {
        	// Check if current user likes this moment
        	if (ParseUser.getCurrentUser() != null) {
	        	ParseQuery<Like> query1 = ParseQuery.getQuery(Like.class);
	        	query1.whereEqualTo("moment", moment);
	        	query1.whereEqualTo("fromUser", ParseUser.getCurrentUser());
	        	query1.countInBackground(new CountCallback() {
		            public void done(int count, ParseException e) {
		                if (e == null) {
		                	if (count > 0) {
		                		momentLikeView.setImageResource(R.drawable.icon_like_red);
		                		moment.isLiked(true);
		                	} else {
		                		momentLikeView.setImageResource(R.drawable.icon_like);
		                		moment.isLiked(false);
		                	}
		                } else {
		                    Log.e(TAG, "Error when counting likes: " + e.getMessage());
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
	                	if (count > 0) {
	                		momentLikeCount.setText(count+"");
	                		momentLikeBoxTop.setVisibility(View.VISIBLE);
	                		momentLikeBoxBottomOutside.setVisibility(View.VISIBLE);
	                		momentLikeBoxBottomInside.setVisibility(View.VISIBLE);
	                	} else {
	                		momentLikeCount.setText("");
	                		momentLikeBoxTop.setVisibility(View.INVISIBLE);
	                		momentLikeBoxBottomOutside.setVisibility(View.INVISIBLE);
	                		momentLikeBoxBottomInside.setVisibility(View.INVISIBLE);
	                	}
	                } else {
	                    Log.d(TAG, "Error when counting likes: " + e.getMessage());
	                }
	            }
	        });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (moments.size() > 0 && moment != null) {
            	// Load the bad preview first
            	imageBadPreview.setParseFile(moment.getBadPreview());
            	imageBadPreview.loadInBackground();
            	
            	// Then load the full picture
	            imageView.setParseFile(moment.getPhotoFile());
	            imageView.loadInBackground(new GetDataCallback() {
	            	@Override
	            	public void done(byte[] data, ParseException e) {
	            		if (e == null) {
	            			momentLikeView.setVisibility(View.VISIBLE);
		            		momentMoreButton.setVisibility(View.VISIBLE);
		            		updateLikeCount();
	            		} else {
	            			momentLoading.setVisibility(View.GONE);
	            			imageBadPreview.setVisibility(View.GONE);
	            			pictureNotFoundView.setVisibility(View.VISIBLE);
	            		}
	            	}
	            });
	            
	            momentAuthorView.setText(moment.getAuthor().getUsername());
	            
	            if (moment.getCaption() != null && !moment.getCaption().trim().equals("")) {
		            momentCaptionView.setText(moment.getCaption());
		            momentCaptionView.setVisibility(View.VISIBLE);
	            }
	            String location = moment.getLocationDescription();
	            if (!Utils.isEmptyString(location)) {
	            	momentCreatedView.setText(Utils.getTimeFromDateToNow(moment.getCreatedAt()) + " from " + location);
	            } else {
	            	momentCreatedView.setText(Utils.getTimeFromDateToNow(moment.getCreatedAt()));
	            }
	            if (moment.getIsFavorite()) {
	            	momentPicked.setVisibility(View.VISIBLE);
	            }
            }
        }
    }
}
