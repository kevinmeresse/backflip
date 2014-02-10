package com.km.backfront.ui;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.km.backfront.R;
import com.km.backfront.model.Moment;
import com.km.backfront.util.BackflipException;
import com.km.backfront.util.BitmapHelper;
import com.km.backfront.util.CacheManager;
import com.km.backfront.util.PostPhotoOnFacebookRunnable;
import com.km.backfront.util.PostPhotoOnInstagramRunnable;
import com.km.backfront.util.PostPhotoOnTwitterRunnable;
import com.km.backfront.util.PublishCallback;
import com.km.backfront.util.Utils;
import com.km.backfront.util.path.PathUtils;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFacebookUtils.Permissions;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import com.facebook.*;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.model.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ShareMomentFragment extends Fragment {
	
	protected static final String TAG = "ShareMomentFragment";
	protected static final int FACEBOOK_REQUEST_CODE = 76;
	protected static final int TWITTER_REQUEST_CODE = 77;
	protected static final int INSTAGRAM_REQUEST_CODE = 78;
	protected static final int PATH_REQUEST_CODE = 79;
	private ParseFile photoFile;
	private TextView momentCaption;
	private TextView momentLocation;
	private ImageView momentPreview;
	private Button shareButton;
	private Button shareSaveImageButton;
	private TextView shareSaveImageDisabled;
	private ImageButton topBarIconButton;
	private Button topBarTextButton;
	private FrameLayout shareLoading;
	private ImageButton shareFacebookIcon;
	private TextView shareFacebookText;
	private ImageButton shareTwitterIcon;
	private TextView shareTwitterText;
	private ImageButton shareInstagramIcon;
	private TextView shareInstagramText;
	private ImageButton sharePathIcon;
	private TextView sharePathText;
	
	private boolean shareOnFacebook = false;
	private boolean shareOnTwitter = false;
	private boolean shareOnInstagram = false;
	private boolean shareOnPath = false;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	
    	// Inflate the layout for this fragment
    	View v = inflater.inflate(R.layout.fragment_share_moment, container, false);
    	
    	// Get the view objects
    	momentCaption = (TextView) v.findViewById(R.id.share_moment_caption);
    	momentLocation = (TextView) v.findViewById(R.id.share_moment_location);
    	momentPreview = (ImageView) v.findViewById(R.id.share_image_preview);
    	shareButton = (Button) v.findViewById(R.id.share_button);
    	shareSaveImageButton = (Button) v.findViewById(R.id.share_save_image_button);
    	shareSaveImageDisabled = (TextView) v.findViewById(R.id.share_save_image_disabled);
    	topBarIconButton = (ImageButton) v.findViewById(R.id.share_top_bar_icon);
    	topBarTextButton = (Button) v.findViewById(R.id.share_top_bar_text);
    	shareLoading = (FrameLayout) v.findViewById(R.id.share_loading);
    	shareFacebookIcon = (ImageButton) v.findViewById(R.id.share_facebook_icon);
    	shareFacebookText = (TextView) v.findViewById(R.id.share_facebook_text);
    	shareTwitterIcon = (ImageButton) v.findViewById(R.id.share_twitter_icon);
    	shareTwitterText = (TextView) v.findViewById(R.id.share_twitter_text);
    	shareInstagramIcon = (ImageButton) v.findViewById(R.id.share_instagram_icon);
    	shareInstagramText = (TextView) v.findViewById(R.id.share_instagram_text);
    	sharePathIcon = (ImageButton) v.findViewById(R.id.share_path_icon);
    	sharePathText = (TextView) v.findViewById(R.id.share_path_text);
    	
    	// Set the preview image
    	Bitmap momentImageScaled = BitmapHelper.scaleToFitWidth(((NewMomentActivity) getActivity()).getCurrentPhoto(), 200);
    	momentPreview.setImageBitmap(momentImageScaled);
    	
    	// Save the image in cache
    	CacheManager.cachePicture(getActivity(), BitmapHelper.scaleToFitWidth(((NewMomentActivity) getActivity()).getCurrentPhoto(), 800), "full.jpg", Bitmap.CompressFormat.JPEG);
    	
    	// Action: click on share
    	shareButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Check if Internet connection
		    	if (!Utils.hasConnection((NewMomentActivity) getActivity())) {
		    		Utils.showToast(getActivity(), "No internet connection...");
		    	} else {
		    		// Check if user is anonymous
		    		ParseUser currentUser = ParseUser.getCurrentUser();
		    		if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
		    			shareMoment();
		    		} else {
		    			Intent intent = new Intent(v.getContext(), SignUpActivity.class);
				    	startActivityForResult(intent, 0);
		    		}
		    	}
		    }
    	});
    	
    	// Action: click on top bar icon
    	topBarIconButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	getActivity().getSupportFragmentManager().popBackStack("ShareMomentFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
		    }
    	});
    	
    	// Action: click on top bar text
    	topBarTextButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	getActivity().getSupportFragmentManager().popBackStack("ShareMomentFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
		    }
    	});
    	
    	// Action: click on save image
    	shareSaveImageButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	shareSaveImageDisabled.setVisibility(View.VISIBLE);
		    	shareSaveImageButton.setVisibility(View.INVISIBLE);
		    	try {
					Bitmap momentImage = ((NewMomentActivity) getActivity()).getCurrentPhoto();
		    		BitmapHelper.saveImageInGallery(getActivity(), momentImage);
					Utils.showToast(getActivity(), "Picture successfully saved");
				} catch (Exception e) {
					Utils.showToast(getActivity(), "Sorry, picture could not be saved...");
					shareSaveImageButton.setVisibility(View.VISIBLE);
			    	shareSaveImageDisabled.setVisibility(View.INVISIBLE);
					e.printStackTrace();
				}
		    }
    	});
    	
    	// Action: click on Facebook
    	shareFacebookIcon.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	toggleFacebookShare(shareOnFacebook);
		    	if (shareOnFacebook) loginOnFacebook();
		    }
    	});
    	shareFacebookText.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	toggleFacebookShare(shareOnFacebook);
		    	if (shareOnFacebook) loginOnFacebook();
		    }
    	});
    	
    	// Action: click on Twitter
    	shareTwitterIcon.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	toggleTwitterShare(shareOnTwitter);
		    	if (shareOnTwitter) loginOnTwitter();
		    }
    	});
    	shareTwitterText.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	toggleTwitterShare(shareOnTwitter);
		    	if (shareOnTwitter) loginOnTwitter();
		    }
    	});
    	
    	// Action: click on Instagram
    	shareInstagramIcon.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	toggleInstagramShare(shareOnInstagram);
		    }
    	});
    	shareInstagramText.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	toggleInstagramShare(shareOnInstagram);
		    }
    	});
    	
    	// Action: click on Path
    	sharePathIcon.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	togglePathShare(shareOnPath);
		    	if (shareOnPath) loginOnPath();
		    }
    	});
    	sharePathText.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	togglePathShare(shareOnPath);
		    	if (shareOnPath) loginOnPath();
		    }
    	});
    	
    	return v;
	}
	
	private void toggleFacebookShare(boolean isCurrentlyActivated) {
		if (!isCurrentlyActivated) {
			shareOnFacebook = true;
			shareFacebookIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_color));
			shareFacebookText.setTextColor(getResources().getColor(R.color.text_black));
		} else {
			shareOnFacebook = false;
			shareFacebookIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_gray));
			shareFacebookText.setTextColor(getResources().getColor(R.color.separator_grey));
		}
	}
	
	private void toggleTwitterShare(boolean isCurrentlyActivated) {
		if (!isCurrentlyActivated) {
			shareOnTwitter = true;
			shareTwitterIcon.setImageDrawable(getResources().getDrawable(R.drawable.twitter_color));
			shareTwitterText.setTextColor(getResources().getColor(R.color.text_black));
		} else {
			shareOnTwitter = false;
			shareTwitterIcon.setImageDrawable(getResources().getDrawable(R.drawable.twitter_gray));
			shareTwitterText.setTextColor(getResources().getColor(R.color.separator_grey));
		}
	}
	
	private void toggleInstagramShare(boolean isCurrentlyActivated) {
		if (!isCurrentlyActivated) {
			shareOnInstagram = true;
			shareInstagramIcon.setImageDrawable(getResources().getDrawable(R.drawable.instagram_color));
			shareInstagramText.setTextColor(getResources().getColor(R.color.text_black));
		} else {
			shareOnInstagram = false;
			shareInstagramIcon.setImageDrawable(getResources().getDrawable(R.drawable.instagram_gray));
			shareInstagramText.setTextColor(getResources().getColor(R.color.separator_grey));
		}
	}
	
	private void togglePathShare(boolean isCurrentlyActivated) {
		if (!isCurrentlyActivated) {
			shareOnPath = true;
			sharePathIcon.setImageDrawable(getResources().getDrawable(R.drawable.path_color));
			sharePathText.setTextColor(getResources().getColor(R.color.text_black));
		} else {
			shareOnPath = false;
			sharePathIcon.setImageDrawable(getResources().getDrawable(R.drawable.path_gray));
			sharePathText.setTextColor(getResources().getColor(R.color.separator_grey));
		}
	}
	
	private void loginOnFacebook() {
		Log.d(TAG, "Clicked on Share on Facebook.");
    	ParseUser currentUser = ParseUser.getCurrentUser();
    	try {
			currentUser.save();
	    	if (!ParseFacebookUtils.isLinked(currentUser)) {
	    		Log.d(TAG, "Connecting to Facebook...");
	    		ParseFacebookUtils.link(currentUser, getActivity(), FACEBOOK_REQUEST_CODE, new SaveCallback() {
	    			@Override
	    		    public void done(ParseException ex) {
	    				if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
	    					getFacebookIdInBackground();
	    					// Request permission to post on Facebook
	    					ParseFacebookUtils.getSession().requestNewPublishPermissions(new NewPermissionsRequest(getActivity(), Arrays.asList(Permissions.Extended.PUBLISH_STREAM, Permissions.User.PHOTOS)));
	    					ParseFacebookUtils.saveLatestSessionData(ParseUser.getCurrentUser());
	    					Log.d(TAG, "Woohoo, user logged in with Facebook!");
	    				} else {
	    					Log.d(TAG, "Aaarggh, NOT logged in with Facebook:"+ex.getMessage());
	    					toggleFacebookShare(shareOnFacebook);
	    				}
	    		    }
	    		});
    		} else {
    			Log.d(TAG, "Good news, the user is already linked to a Facebook account.");
    		}
    	} catch (ParseException e) {
			e.printStackTrace();
			toggleFacebookShare(shareOnFacebook);
		}
	}
	
	private void loginOnTwitter() {
		Log.d(TAG, "Clicked on Share on Twitter.");
    	ParseUser currentUser = ParseUser.getCurrentUser();
    	try {
			currentUser.save();
			if (!ParseTwitterUtils.isLinked(currentUser)) {
	    		Log.d(TAG, "Connecting to Twitter...");
	    		ParseTwitterUtils.link(currentUser, getActivity(), new SaveCallback() {
	    			@Override
	    		    public void done(ParseException ex) {
	    				if (ParseTwitterUtils.isLinked(ParseUser.getCurrentUser())) {
	    					Log.d(TAG, "Woohoo, user logged in with Twitter!");
	    					getTwitterId();
	    				} else {
	    					Log.d(TAG, "Aaarggh, NOT logged in with Twitter:"+ex.getMessage());
	    					toggleTwitterShare(shareOnTwitter);
	    				}
	    		    }
	    		});
    		} else {
    			Log.d(TAG, "Good news, the user is already linked to a Twitter account.");
    		}
    	} catch (ParseException e) {
    		toggleTwitterShare(shareOnTwitter);
			e.printStackTrace();
		}
	}
	
	private void loginOnPath() {
		Log.d(TAG, "Clicked on Share on Path.");
		ParseUser currentUser = ParseUser.getCurrentUser();
		try {
			currentUser.save();
			if (currentUser.get("pathAccessToken") == null) {
				Intent intent = new Intent(getActivity(), LoginPathActivity.class);
		    	startActivityForResult(intent, PATH_REQUEST_CODE);
			}
		} catch (ParseException e) {
			togglePathShare(shareOnPath);
			e.printStackTrace();
		}
		
	}
	
	
	
	private static void getFacebookIdInBackground() {
		Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (user != null) {
					ParseUser.getCurrentUser().put("facebookId", user.getId());
					ParseUser.getCurrentUser().saveInBackground();
				}
			}
		}).executeAsync();
	}
	
	private static void getTwitterId() {
		String userId = ParseTwitterUtils.getTwitter().getUserId();
		if (!Utils.isEmptyString(userId)) {
			ParseUser.getCurrentUser().put("twitterId", userId);
			ParseUser.getCurrentUser().saveInBackground();
		}
	}
	
	private static void getFriendsInBackground() {
		Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {

			  @Override
			  public void onCompleted(List<GraphUser> users, Response response) {
			    if (users != null) {
			      List<String> friendsList = new ArrayList<String>();
			      for (GraphUser user : users) {
			        friendsList.add(user.getId());
			      }

			      // Construct a ParseUser query that will find friends whose
			      // facebook IDs are contained in the current user's friend list.
			      ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();
			      friendQuery.whereContainedIn("facebookId", friendsList);

			      // findObjects will return a list of ParseUsers that are friends with
			      // the current user
			      try {
					List<ParseUser> friendUsers = friendQuery.find();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    }
			  }
			}).executeAsync();
	}
	
	public void shareMoment() {
		Log.d(TAG, "Sharing moment on Backfront...");
		// Display the loading bar
    	shareLoading.setVisibility(View.VISIBLE);
    	// Retrieve the current photo from activity
    	Bitmap momentImage = ((NewMomentActivity) getActivity()).getCurrentPhoto();
    	// Convert photo to a ParseFile
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	momentImage.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		byte[] scaledData = bos.toByteArray();
		photoFile = new ParseFile("moment_photo.jpg", scaledData);
		// Save the image to Parse
		photoFile.saveInBackground(new SaveCallback() {

			public void done(ParseException e) {
				if (e != null) {
					shareLoading.setVisibility(View.INVISIBLE);
					Log.e(TAG, "Error when saving picture to Parse: " + e.getMessage());
					Utils.showToast(getActivity(), "Error when saving picture on the server... Please try again shortly!");
				} else {
					Log.d(TAG, "Finished saving picture to Parse. URL: "+photoFile.getUrl());
					Moment moment = createMomentAndReturn();
					if (moment != null) {
						publishOnSocialNetworksInBackground(moment);
						// Finish this activity and go back to the main activity
						getActivity().setResult(Activity.RESULT_OK);
						getActivity().finish();
					}
				}
			}
		});
	}
	
	public void publishOnSocialNetworksInBackground(Moment moment) {
		String fullCaption = moment.getCaption();
		if (!Utils.isEmptyString(moment.getLocationDescription())) {
			fullCaption += " - From "+moment.getLocationDescription();
		}
		if (shareOnFacebook) {
			Log.d(TAG, "Sharing moment on Facebook...");
			Thread thread = new Thread(new PostPhotoOnFacebookRunnable(getActivity(), photoFile.getUrl(), fullCaption));
	        thread.start();
		}
		if (shareOnTwitter && !Utils.isEmptyString(moment.getObjectId())) {
			Log.d(TAG, "Sharing moment on Twitter...");
			Thread thread = new Thread(new PostPhotoOnTwitterRunnable(getActivity(), moment.getObjectId(), moment.getCaption()));
	        thread.start();
		}
		if (shareOnPath) {
			Log.d(TAG, "Sharing moment on Path...");
			PathUtils.publishPhoto(photoFile.getUrl(), fullCaption, new PublishCallback() {

				@Override
				public void done(BackflipException e) {
					if (e != null) {
						Utils.showToast(getActivity(), "An error occured when sharing on Path");
					}
				}
			});
			//Thread thread = new Thread(new PostPhotoOnPathRunnable(getActivity(), photoFile.getUrl(), fullCaption));
	        //thread.start();
		}
		if (shareOnInstagram) {
			Log.d(TAG, "Sharing moment on Instagram...");
			Thread thread = new Thread(new PostPhotoOnInstagramRunnable(getActivity()));
	        thread.start();
		}
	}
	
	public Moment createMomentAndReturn() {
		Moment moment = new Moment();
		// Add data to the moment object:
		moment.setPhotoFile(photoFile);
		moment.setCaption(momentCaption.getText().toString());
		moment.setLocationDescription(momentLocation.getText().toString());
		// Associate the moment with the current user
		moment.setAuthor(ParseUser.getCurrentUser());
		// The moment is not a favorite by default
		moment.setIsFavorite(false);
		
		// Save the moment to Parse
		try {
			moment.save();
		} catch (ParseException e) {
			Log.e(TAG, "Error when saving moment to Parse: " + e.getMessage());
			e.printStackTrace();
			Utils.showToast(getActivity(), "Error when saving moment on the server... Please try again shortly!");
			return null;
		}
		
		// Update moment with latest info from Parse (ObjectId)
		try {
			moment.fetch();
		} catch (ParseException e) {
			Log.e(TAG, "Couldn't fetch moment from Parse: " + e.getMessage());
			e.printStackTrace();
		}
		
		return moment;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "On activity result... Request code: "+requestCode);
		if (requestCode == PATH_REQUEST_CODE) {
			Log.d(TAG, "Returning from LoginPathActivity...");
			if (resultCode != Activity.RESULT_OK) {
				Log.d(TAG, "Apparently it has been canceled. Disabling Path sharing...");
				togglePathShare(shareOnPath);
				Utils.showToast(getActivity(), "Could not connect to Path...");
			}
		}else if (resultCode == Activity.RESULT_OK) {
			ParseUser currentUser = ParseUser.getCurrentUser();
			if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
				shareMoment();
			} else {
				Utils.showToast(getActivity(), "We couldn't sign you in...");
			}
		}/* else if (resultCode == FACEBOOK_ACTIVITY_CODE) {
			Log.d(TAG, "Finishing Facebook authentication....");
			ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
		}*/
	}
}
