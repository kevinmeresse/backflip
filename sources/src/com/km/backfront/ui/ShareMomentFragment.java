package com.km.backfront.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.km.backfront.album.BaseAlbumDirFactory;
import com.km.backfront.album.FroyoAlbumDirFactory;
import com.km.backfront.album.AlbumStorageDirFactory;
import com.km.backfront.R;
import com.km.backfront.model.Moment;
import com.km.backfront.util.BitmapHelper;
import com.km.backfront.util.Utils;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

public class ShareMomentFragment extends Fragment {
	
	protected static final String TAG = "ShareMomentFragment";
	protected static final int FACEBOOK_ACTIVITY_CODE = 76;
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
    	
    	// Set the preview image
    	Bitmap momentImageScaled = BitmapHelper.scaleToFitWidth(((NewMomentActivity) getActivity()).getCurrentPhoto(), 200);
    	momentPreview.setImageBitmap(momentImageScaled);
    	
    	// Action: click on share
    	shareButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	// Check if Internet connection
		    	if (!Utils.hasConnection((NewMomentActivity) getActivity())) {
		    		Toast.makeText(
							getActivity().getApplicationContext(),
							"No internet connection...",
							Toast.LENGTH_SHORT).show();
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
					Toast.makeText(
						getActivity().getApplicationContext(),
						"Picture successfully saved",
						Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(
						getActivity().getApplicationContext(),
						"Sorry, picture could not be saved...",
						Toast.LENGTH_SHORT).show();
					shareSaveImageButton.setVisibility(View.VISIBLE);
			    	shareSaveImageDisabled.setVisibility(View.INVISIBLE);
					e.printStackTrace();
				}
		    }
    	});
    	
    	// Action: click on Facebook
    	shareFacebookIcon.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Log.d(TAG, "Clicked on Share on Facebook.");
		    	ParseUser currentUser = ParseUser.getCurrentUser();
		    	if (!ParseFacebookUtils.isLinked(currentUser)) {
		    		Log.d(TAG, "Connecting to Facebook...");
		    		ParseFacebookUtils.link(currentUser, getActivity(), FACEBOOK_ACTIVITY_CODE, new SaveCallback() {
		    			@Override
		    		    public void done(ParseException ex) {
		    				if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
		    					Log.d(TAG, "Woohoo, user logged in with Facebook!");
		    				} else {
		    					Log.d(TAG, "Aaarggh, NOT logged in with Facebook:"+ex.getMessage());
		    				}
		    		    }
		    		});
	    		} else {
	    			Log.d(TAG, "Good news, the user is already linked to a Facebook account.");
	    		}
		    }
    	});
    	
    	return v;
	}
	
	public void shareMoment() {
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
					Toast.makeText(getActivity().getApplicationContext(),
							"Error saving: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				} else {
					createMomentAndReturn();
				}
			}
		});
	}
	
	public void createMomentAndReturn() {
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
		moment.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {
					getActivity().setResult(Activity.RESULT_OK);
					getActivity().finish();
				} else {
					Toast.makeText(
							getActivity().getApplicationContext(),
							"Error saving: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "On activity result... Request code: "+requestCode);
		if (resultCode == Activity.RESULT_OK) {
			ParseUser currentUser = ParseUser.getCurrentUser();
			if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
    			shareMoment();
			} else {
				Toast.makeText(getActivity().getApplicationContext(),
						"We couldn't sign you in...",
						Toast.LENGTH_LONG).show();
			}
		} else if (resultCode == FACEBOOK_ACTIVITY_CODE) {
			Log.d(TAG, "Finishing Facebook authentication....");
			ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
		}
		//super.onActivityResult(requestCode, resultCode, data);
	}
}
