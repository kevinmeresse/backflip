package com.km.backfront.ui;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class TosActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// No title bar
		//this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		
		// Set the layout
		WebView webview = new WebView(this);
		setContentView(webview);
		
		// Load the web page
		webview.loadUrl("http://www.bckflp.co/tos/index.html");
	}

}
