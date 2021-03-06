package com.km.backflip.ui;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import com.km.backflip.model.Moment;
import com.km.backflip.util.BackflipException;
import com.km.backflip.util.BitmapHelper;
import com.km.backflip.util.CacheManager;
import com.km.backflip.util.PostPhotoOnInstagramRunnable;
import com.km.backflip.util.PublishCallback;
import com.km.backflip.util.Utils;
import com.km.backflip.util.facebook.FacebookUtils;
import com.km.backflip.util.path.PathUtils;
import com.km.backflip.util.twitter.TwitterUtils;
import com.km.backflip.R;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFacebookUtils.Permissions;
import com.parse.ParseFile;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import com.crashlytics.android.Crashlytics;
import com.facebook.Session.NewPermissionsRequest;
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
	
	public static final String TAG = ShareMomentFragment.class.getSimpleName();
	protected static final int INSTANT_SHARE_REQUEST_CODE = 75;
	protected static final int FACEBOOK_REQUEST_CODE = 76;
	protected static final int TWITTER_REQUEST_CODE = 77;
	protected static final int INSTAGRAM_REQUEST_CODE = 78;
	protected static final int PATH_REQUEST_CODE = 79;
	private ParseFile photoFile;
	private ParseFile photoThumbnail;
	private ParseFile photoBadPreview;
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
    	
    	// Check if we still have the picture
    	if (((NewMomentActivity) getActivity()).getCurrentPhoto() == null) {
    		Bitmap restoredPic = CacheManager.retrievePicture(getActivity(), "full.jpg");
    		if (restoredPic != null) {
    			((NewMomentActivity) getActivity()).setCurrentPhoto(restoredPic);
    		} else {
    			getActivity().getSupportFragmentManager().popBackStack("ShareMomentFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    		}
    	}
    	
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
		    		// Check if user is logged in
		    		if (ParseUser.getCurrentUser() != null) {
		    			shareMoment();
		    		} else {
		    			Intent intent = new Intent(v.getContext(), SignUpActivity.class);
				    	startActivityForResult(intent, INSTANT_SHARE_REQUEST_CODE);
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
					Crashlytics.logException(e);
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
		    	if (Utils.userLoggedIn(getActivity())) {
		    		toggleFacebookShare(shareOnFacebook);
		    	}
		    }
    	});
    	shareFacebookText.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (Utils.userLoggedIn(getActivity())) {
		    		toggleFacebookShare(shareOnFacebook);
		    	}
		    }
    	});
    	
    	// Action: click on Twitter
    	shareTwitterIcon.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (Utils.userLoggedIn(getActivity())) {
		    		toggleTwitterShare(shareOnTwitter);
		    	}
		    }
    	});
    	shareTwitterText.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (Utils.userLoggedIn(getActivity())) {
		    		toggleTwitterShare(shareOnTwitter);
		    	}
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
		    	if (Utils.userLoggedIn(getActivity())) {
		    		togglePathShare(shareOnPath);
		    	}
		    }
    	});
    	sharePathText.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	if (Utils.userLoggedIn(getActivity())) {
		    		togglePathShare(shareOnPath);
		    	}
		    }
    	});
    	
    	return v;
	}
	
	private void toggleFacebookShare(boolean isCurrentlyActivated) {
		if (!isCurrentlyActivated) {
			shareOnFacebook = true;
			shareFacebookIcon.setImageDrawable(getResources().getDrawable(R.drawable.facebook_color));
			shareFacebookText.setTextColor(getResources().getColor(R.color.text_black));
			
			// Login on Facebook
			boolean success = FacebookUtils.login(getActivity(), new SaveCallback() {
				
				@Override
				public void done(ParseException e) {
					if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
						FacebookUtils.saveUserId();
						// Request permission to post on Facebook
    					ParseFacebookUtils.getSession().requestNewPublishPermissions(new NewPermissionsRequest(getActivity(), Arrays.asList(Permissions.Extended.PUBLISH_STREAM, Permissions.User.PHOTOS)));
    					ParseFacebookUtils.saveLatestSessionData(ParseUser.getCurrentUser());
    					Log.d(TAG, "Woohoo, user logged in with Facebook!");
    					FacebookUtils.followFriendsInBackground();
    				} else {
    					Log.d(TAG, "Aaarggh, NOT logged in with Facebook.");
    					if (e != null) Log.d(TAG, "Error: " + e.getMessage());
    					e.printStackTrace();
    					toggleFacebookShare(shareOnFacebook);
    				}
				}
			});
			if (!success) {
				toggleFacebookShare(shareOnFacebook);
			}
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
			
			// Login on Twitter
			boolean success = TwitterUtils.login(getActivity(), new SaveCallback() {
				
				@Override
				public void done(ParseException e) {
					if (ParseTwitterUtils.isLinked(ParseUser.getCurrentUser())) {
    					Log.d(TAG, "Woohoo, user logged in with Twitter!");
    					TwitterUtils.saveUserId();
    				} else {
    					Log.d(TAG, "Aaarggh, NOT logged in with Twitter.");
    					if (e != null) Log.d(TAG, "Error: " + e.getMessage());
    					toggleTwitterShare(shareOnTwitter);
    				}
				}
			});
			if (!success) {
				toggleTwitterShare(shareOnTwitter);
			}
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
			loginOnPath();
		} else {
			shareOnPath = false;
			sharePathIcon.setImageDrawable(getResources().getDrawable(R.drawable.path_gray));
			sharePathText.setTextColor(getResources().getColor(R.color.separator_grey));
		}
	}
	
	private void loginOnPath() {
		Log.d(TAG, "Clicked on Share on Path.");
		if (Utils.isEmptyString((String) ParseUser.getCurrentUser().get("pathAccessToken"))) {
			Intent intent = new Intent(getActivity(), LoginPathActivity.class);
	    	startActivityForResult(intent, PATH_REQUEST_CODE);
		}
	}
	
	public void shareMoment() {
		Log.d(TAG, "Sharing moment on Backflip...");
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
		
		// Create bad preview
		Bitmap badPreview = BitmapHelper.scaleToFitWidth(momentImage, 50);
		badPreview = BitmapHelper.applyGaussianBlur(badPreview);
		ByteArrayOutputStream bosBadPreview = new ByteArrayOutputStream();
		badPreview.compress(Bitmap.CompressFormat.JPEG, 50, bosBadPreview);
		byte[] scaledDataBadPreview = bosBadPreview.toByteArray();
		photoBadPreview = new ParseFile("moment_bad_preview.jpg", scaledDataBadPreview);
		photoBadPreview.saveInBackground();
		
		// Create thumbnail
		Bitmap thumbnail = BitmapHelper.scaleToFitWidth(momentImage, 200);
		ByteArrayOutputStream bosThumbnail = new ByteArrayOutputStream();
		thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bosThumbnail);
		byte[] scaledDataThumbnail = bosThumbnail.toByteArray();
		photoThumbnail = new ParseFile("moment_thumbnail.jpg", scaledDataThumbnail);
		photoThumbnail.saveInBackground();
	}
	
	public void publishOnSocialNetworksInBackground(Moment moment) {
		String fullCaption = moment.getCaption();
		if (!Utils.isEmptyString(moment.getLocationDescription())) {
			fullCaption += " - From "+moment.getLocationDescription();
		}
		
		// Publish on Facebook
		if (shareOnFacebook) {
			Log.d(TAG, "Sharing moment on Facebook...");
			FacebookUtils.publishPhoto(photoFile.getUrl(), fullCaption, new PublishCallback() {

				@Override
				public void done(BackflipException e) {
					if (e != null) {
						Crashlytics.logException(e);
						Log.e(TAG, "An error occured when sharing on Facebook: " + e.getMessage());
						Utils.showToast(getActivity(), "An error occured when sharing on Facebook");
					}
				}
			});
		}
		
		// Publish on Twitter
		if (shareOnTwitter && !Utils.isEmptyString(moment.getObjectId())) {
			Log.d(TAG, "Sharing moment on Twitter...");
			TwitterUtils.publishPhoto(moment.getObjectId(), moment.getCaption(), new PublishCallback() {

				@Override
				public void done(BackflipException e) {
					if (e != null) {
						Crashlytics.logException(e);
						Log.e(TAG, "An error occured when sharing on Twitter: " + e.getMessage());
						Utils.showToast(getActivity(), "An error occured when sharing on Twitter");
					}
				}
			});
		}
		
		// Publish on Path
		if (shareOnPath) {
			Log.d(TAG, "Sharing moment on Path...");
			PathUtils.publishPhoto(photoFile.getUrl(), fullCaption, new PublishCallback() {

				@Override
				public void done(BackflipException e) {
					if (e != null) {
						Crashlytics.logException(e);
						Log.e(TAG, "An error occured when sharing on Path" + e.getMessage());
						Utils.showToast(getActivity(), "An error occured when sharing on Path");
					}
				}
			});
		}
		
		// Publish on Instagram
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
		moment.setThumbnail(photoThumbnail);
		moment.setBadPreview(photoBadPreview);
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
			Crashlytics.logException(e);
			Log.e(TAG, "Error when saving moment to Parse: " + e.getMessage());
			e.printStackTrace();
			Utils.showToast(getActivity(), "Error when saving moment on the server... Please try again shortly!");
			return null;
		}
		
		// Update moment with latest info from Parse (ObjectId)
		try {
			moment.fetch();
		} catch (ParseException e) {
			Crashlytics.logException(e);
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
		} else if (requestCode == INSTANT_SHARE_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK && ParseUser.getCurrentUser() != null) {
				shareMoment();
			} else {
				Utils.showToast(getActivity(), "We couldn't sign you in...");
			}
		}
	}
}
