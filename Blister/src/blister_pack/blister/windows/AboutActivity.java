/*
 * Copyright (C) 2009 Sadko Mobile
 * www.sadko.mobi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0

 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */

package blister_pack.blister.windows;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import blister_pack.blister.R;
import com.sadko.bursaq.BursaqConstants;
import com.sadko.bursaq.GotoBursaqActivity;

/**
 * About dialog with Bursaq payment option integrated
 * 
 * @author benderamp
 *
 */
public class AboutActivity extends Activity {

	private static final String SEARCH_MARKET_COMPONENT_NAMESPACE = "com.android.vending";
	private static final String SEARCH_MARKET_COMPONENT_CLASS = "com.android.vending.SearchAssetListActivity";
	private static final String SEARCH_QUERY_PUBLISHER = "pub:\"Sadko Mobile\"";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		int orientation=this.getResources().getConfiguration().orientation;
		if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setContentView(R.layout.about);
		} else {
			setContentView(R.layout.about_landscape);
		}

		final TextView appNameTxt = (TextView) findViewById(R.id.about_appname_txt);
		appNameTxt.setText(getText(R.string.about_app_name) + " " + getPackageVersion());
		
		final ListView actionsLst = (ListView) findViewById(R.id.about_actions_lst);
		actionsLst.setAdapter(new ArrayAdapter(this,
				R.layout.about_action_list_item, new String[] {
						getString(R.string.about_donate),
						getString(R.string.about_goto_project_page),
						getString(R.string.about_more_content) }));

		actionsLst
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView parent, View v,
							int position, long id) {
						if (position == 0) {
							donate();
						} else if (position == 1) {
							goToProjectPage();
						} else {
							getMoreContent();
						}
					}
				});
	}
	
	/**
	 * Get application version
	 * @return
	 */
	private String getPackageVersion() {
		String version = "";
		
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = pi.versionName;
		} catch (NameNotFoundException e) {
		}
		return version;
	}
	

	/**
	 * Donate to project authors using Bursaq mobile payments manager
	 */
	private void donate() {
		final Intent intent = new Intent();
		intent.setClass(this, GotoBursaqActivity.class);
		
		// set services to show in Bursaq
		final String bursaqPubName = getString(R.string.bursaq_publisher);
		final String bursaqAppName = getString(R.string.bursaq_appname);
		final String bursaqDonate1 = getString(R.string.bursaq_donate1);
		final String bursaqDonate2 = getString(R.string.bursaq_donate2);
		
		intent.putExtra(BursaqConstants.PARAM_PUBLISHER, bursaqPubName);
		intent.putExtra(BursaqConstants.PARAM_PRODUCT_NAME, bursaqAppName);
		intent.putExtra(BursaqConstants.PARAM_SERVICE_NAMES, 
				new String[] {bursaqDonate1, bursaqDonate2});			
		
		startActivity(intent);
	}

	/**
	 * Open project web page in web browser
	 * 
	 * @param context
	 */
	private void goToProjectPage() {
		final String url = getString(R.string.about_project_page);
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}

	/**
	 * Find more content by Sadko Mobile on the market
	 * 
	 * @param context
	 */
	private void getMoreContent() {
		final Intent intent = new Intent(Intent.ACTION_SEARCH);
		intent.setComponent(new android.content.ComponentName(
				SEARCH_MARKET_COMPONENT_NAMESPACE,
				SEARCH_MARKET_COMPONENT_CLASS));
		intent
				.putExtra(android.app.SearchManager.QUERY,
						SEARCH_QUERY_PUBLISHER);
		startActivity(intent);
	}
}