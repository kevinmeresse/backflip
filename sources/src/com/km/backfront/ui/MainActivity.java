package com.km.backfront.ui;

import java.util.List;

import se.emilsjolander.flipview.FlipView;
import se.emilsjolander.flipview.OverFlipMode;
import se.emilsjolander.flipview.FlipView.OnFlipListener;
import se.emilsjolander.flipview.FlipView.OnOverFlipListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.km.backfront.R;
import com.km.backfront.model.Moment;
import com.km.backfront.ui.adapter.FlipAdapter.FlipCallback;
import com.km.backfront.ui.adapter.VerticalPagerAdapter;
import com.km.backfront.ui.adapter.FlipAdapter;
import com.km.backfront.ui.vertical.VerticalViewPager;
import com.parse.ParseAnalytics;

public class MainActivity extends FragmentActivity implements FlipCallback, OnFlipListener, OnOverFlipListener {
	
	// The list of moments we need to display
	public static List<Moment> moments;
	
	// Vertical pager
	private VerticalViewPager verticalPager;
    private VerticalPagerAdapter pagerAdapter;
    
    // Flip view
    private FlipView flipView;
	private FlipAdapter flipAdapter;
	
	// Flip or slide?
	private boolean isFlipping = true;
    
    // UI references
	private ImageButton feedButton;
	private ImageButton cameraButton;
	private ImageButton settingsButton;
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
		verticalPager = (VerticalViewPager)findViewById(R.id.pager);
		flipView = (FlipView) findViewById(R.id.flipview);
		feedOffline = (RelativeLayout) findViewById(R.id.feed_offline);
		feedOfflineReload = (ImageButton) findViewById(R.id.feed_offline_pic);
		
		// Honeycomb (api lvl 11) or above is required for the FLIP library to work
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			isFlipping = false;
		}
		
		if (isFlipping) {
			flipAdapter = new FlipAdapter(this, flipView, feedOffline);
			flipAdapter.setCallback(this);
			flipView.setAdapter(flipAdapter);
			flipView.setOnFlipListener(this);
			flipView.peakNext(true);
			flipView.setOverFlipMode(OverFlipMode.RUBBER_BAND);
			//flipView.setEmptyView(findViewById(R.id.empty_view));
			flipView.setOnOverFlipListener(this);
		} else {
			// Set the vertical pager adapter
			pagerAdapter = new VerticalPagerAdapter(this, verticalPager, feedOffline);
			verticalPager.setAdapter(pagerAdapter);
		}
		
        // Action: Go to feed page
		if (!isFlipping) {
			feedButton.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
			    	if (isFlipping && flipAdapter != null) {
			    		flipAdapter.updateFeed();
			    	} else if (pagerAdapter != null) {
			    		pagerAdapter.updateFeed();
			    	}
			    }
			});
		}
		
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
		    	//feedLoading.setVisibility(View.VISIBLE);
		    	if (isFlipping && flipAdapter != null) {
		    		flipAdapter.updateFeed();
		    	} else if (pagerAdapter != null) {
		    		pagerAdapter.updateFeed();
		    	}
		    }
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (isFlipping && flipAdapter != null) {
	    		flipAdapter.updateFeed();
	    	} else if (pagerAdapter != null) {
	    		pagerAdapter.updateFeed();
	    	}
		}
	}
	
	@Override
	public void onPageRequested(int page) {
		flipView.smoothFlipTo(page);
	}

	@Override
	public void onFlippedToPage(FlipView v, int position, long id) {
		Log.i("pageflip", "Page: " + position);
		if (position > flipView.getPageCount() - 3) {
			flipAdapter.addMoreMoments(20);
		}
	}

	@Override
	public void onOverFlip(FlipView v, OverFlipMode mode,
			boolean overFlippingPrevious, float overFlipDistance,
			float flipDistancePerPage) {
		if (overFlippingPrevious && overFlipDistance > 100) {
			Log.i("overflip", "Triggered a Pull to Refresh...");
			// TODO Pull to refresh
			// TODO Display message if offline
			if (isFlipping && flipAdapter != null) {
	    		flipAdapter.updateFeed();
	    	} else if (pagerAdapter != null) {
	    		pagerAdapter.updateFeed();
	    	}
		} else if (!overFlippingPrevious && overFlipDistance > 100) {
			Log.i("overflip", "Triggered a Pull to Add more moments...");
			// TODO Pull to add more moments
			// TODO Display message if offline
		}
	}
}
