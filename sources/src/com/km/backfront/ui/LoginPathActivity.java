package com.km.backfront.ui;

import com.km.backfront.util.BackflipException;
import com.km.backfront.util.LoginCallback;
import com.km.backfront.util.path.PathUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginPathActivity extends Activity {
	
	protected static final String TAG = "LoginPathActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// No title bar
		//this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		
		// Set the layout
		WebView webview = new WebView(this);
		setContentView(webview);
		
		webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	
            	Log.d(TAG, "OverrideUrlLoading...");
            	if ( url.startsWith(PathUtils.REDIRECT_URI) ) {
            		Log.d(TAG, "Matching REDIRECT_URI.");
	                // extract code appended in url
	                if ( PathUtils.urlContainsCode(url) ) {
	                	Log.d(TAG, "Found a 'code' parameter in URL. Extracting it...");
	                    String code = PathUtils.extractCodeFromUrl(url);
	                    Log.d(TAG, "Received code is: "+code);
	                    
	                    if (code != null) {
		                    PathUtils.login(code, new LoginCallback() {
		            			public void done(BackflipException e) {
		            				if (e != null) {
		            					Log.e(TAG, "Path login error: " + e.getMessage());
		            					setResult(Activity.RESULT_CANCELED);
		            				} else {
		            					setResult(Activity.RESULT_OK);
		            				}
		            				finish();
		            			}
		                    });
	                    } else {
	                    	Log.e(TAG, "Couldn't extract code from URL...");
	                    	setResult(Activity.RESULT_CANCELED);
	                    	finish();
	                    }
	                }
	
	                // don't go to redirectUri
	                return true;
                }
            	
            	Log.d(TAG, "Not matching REDIRECT_URI. Displaying web page...");
                // load the webpage from url: login and grant access
                return super.shouldOverrideUrlLoading(view, url); // return false;
            }
        });
		
		// Load the web page
		webview.loadUrl(PathUtils.AUTHORIZATION_URI + PathUtils.CLIENT_ID);
	}
}
