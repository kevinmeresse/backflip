package com.km.backfront.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.km.backfront.R;
import com.km.backfront.model.Like;
import com.km.backfront.model.Moment;
import com.km.backfront.model.Report;
import com.km.backfront.util.BitmapHelper;
import com.km.backfront.util.Utils;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DisplayMomentActivity extends Activity {
	
	protected static final String TAG = "DisplayMomentActivity";

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
    private ProgressBar momentLoading;
    private TextView momentNotFound;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Remove the title bar
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
				
		// Set the layout
		setContentView(R.layout.fragment_feed_pager_details);
		
		// Get the view objects
		imageBadPreview = (ParseImageView) findViewById(R.id.imageBadPreview);
        imageView = (ParseImageView) findViewById(R.id.imageView);
        momentAuthorView = (TextView) findViewById(R.id.moment_author);
        momentCaptionView = (TextView) findViewById(R.id.moment_caption);
        momentCreatedView = (TextView) findViewById(R.id.moment_created);
        momentLikeView = (ImageButton) findViewById(R.id.moment_like);
        momentMoreButton = (ImageButton) findViewById(R.id.moment_more);
        momentLikeCount = (TextView) findViewById(R.id.moment_like_count);
        momentLoading = (ProgressBar) findViewById(R.id.moment_loading);
        momentNotFound = (TextView) findViewById(R.id.moment_not_found);
        
        // Retrieve specified moment
        Bundle extras = getIntent().getExtras();
    	if (extras != null && !Utils.isEmptyString(extras.getString("MOMENT_ID"))) {
    		ParseQuery<Moment> query = ParseQuery.getQuery(Moment.class);
    		query.include("author");
    		query.getInBackground(extras.getString("MOMENT_ID"), new GetCallback<Moment>() {
    		  public void done(Moment retrievedMoment, ParseException e) {
    		    if (e == null) {
    		    	moment = retrievedMoment;
    		    	// Load data in view
    		    	loadMoment();
    		    } else {
    		    	// Display error
    		    	momentLoading.setVisibility(View.GONE);
    		    	momentNotFound.setVisibility(View.VISIBLE);
    		    }
    		  }
    		});
    	} else {
    		// Display error
    		momentLoading.setVisibility(View.GONE);
	    	momentNotFound.setVisibility(View.VISIBLE);
    	}
        
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
        
     // Create popup menu
        popup = new PopupMenu(this, momentMoreButton);
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
				            	BitmapHelper.saveImageInGallery(DisplayMomentActivity.this, momentImage);
				            	Utils.showToast(DisplayMomentActivity.this, "Picture successfully saved");
			            	} catch (FileNotFoundException e) {
			            		Utils.showToast(DisplayMomentActivity.this, "Sorry, picture could not be saved...");
							} catch (IOException e) {
			            		Utils.showToast(DisplayMomentActivity.this, "Sorry, picture could not be saved...");
							} catch (ParseException e) {
			            		Utils.showToast(DisplayMomentActivity.this, "Sorry, picture could not be saved...");
			            		e.printStackTrace();
							}
			                return true;
			            default:
			                return false;
	                }
	            }
            }
        );
	}
	
	public void loadMoment() {
		// Load the bad preview first
    	imageBadPreview.setParseFile(moment.getBadPreview());
    	imageBadPreview.loadInBackground(new GetDataCallback() {
        	@Override
        	public void done(byte[] data, ParseException e) {
        		momentLikeView.setVisibility(View.VISIBLE);
        		momentMoreButton.setVisibility(View.VISIBLE);
        		updateLikeCount();
        	}
        });
    	
    	// Then load the full picture
        imageView.setParseFile(moment.getPhotoFile());
        imageView.loadInBackground();
        
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
					Log.e(TAG, "Error saving: " + e.getMessage());
				}
			}
		});
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
                					Log.e(TAG, "Error saving: " + e.getMessage());
        				     	}
                			}
                		});
                	}
                } else {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }
	
	public void reportProblem() {
    	// Create report
    	Report report = new Report();
    	report.setFromUser(ParseUser.getCurrentUser());
    	report.setMoment(moment);
    	// Save report to Parse
    	report.saveInBackground();
    	
    	// Notify user
    	Utils.showToast(DisplayMomentActivity.this, "Your report has been sent. Thank you for your help!");
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
