/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.chrismorais.android.sunshine.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.chrismorais.android.sunshine.app.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

	private final String LOG_TAG = MainActivity.class.getSimpleName();
	private static final String DETAILFRAGMENT_TAG = "DFTAG";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	/**
	 * Substitute you own project number here. This project number comes
	 * from the Google Developers Console.
	 */
	static final String PROJECT_NUMBER = "318697903911";

	private boolean mTwoPane;
	private String mLocation;
	private GoogleCloudMessaging mGcm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocation = Utility.getPreferredLocation(this);

		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayShowTitleEnabled(false);

		if (findViewById(R.id.weather_detail_container) != null) {
			mTwoPane = true;

			if (savedInstanceState == null) {
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
						.commit();
			}
		} else {
			mTwoPane = false;
			getSupportActionBar().setElevation(0f);
		}

		ForecastFragment forecastFragment =  ((ForecastFragment)getSupportFragmentManager()
				.findFragmentById(R.id.fragment_forecast));
		forecastFragment.setUseTodayLayout(!mTwoPane);

		SunshineSyncAdapter.initializeSyncAdapter(this);

		// If Google Play Services is not available, some features, such as GCM-powered weather
		// alerts, will not be available.
		if (checkPlayServices()) {
			mGcm = GoogleCloudMessaging.getInstance(this);
			String regId = getRegistrationId(this);

			if (PROJECT_NUMBER.equals("Your Project Number")) {
				new AlertDialog.Builder(this)
						.setTitle("Needs Project Number")
						.setMessage("GCM will not function in Sunshine until you set the Project Number to the one from the Google Developers Console.")
						.setPositiveButton(android.R.string.ok, null)
						.create().show();
			} else if (regId.isEmpty()) {
				registerInBackground(this);
			}
		} else {
			Log.i(LOG_TAG, "No valid Google Play Services APK. Weather alerts will be disabled.");
			// Store regID as null
			storeRegistrationId(this, null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();

		String location = Utility.getPreferredLocation(this);
		if (location != null && !location.equals(mLocation)) {
			ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
			if ( null != ff ) {
				ff.onLocationChanged();
			}
			DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
			if ( null != df ) {
				df.onLocationChanged(location);
			}
			mLocation = location;
		}
	}

	@Override
	public void onItemSelected(Uri contentUri) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle args = new Bundle();
			args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

			DetailFragment fragment = new DetailFragment();
			fragment.setArguments(args);

			getSupportFragmentManager().beginTransaction()
					.replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
					.commit();
		} else {
			Intent intent = new Intent(this, DetailActivity.class).setData(contentUri);

			ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
			ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(LOG_TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(LOG_TAG, "GCM Registration not found.");
			return "";
		}

		// Check if app was updated; if so, it must clear the registration ID
		// since the existing registration ID is not guaranteed to work with
		// the new app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(LOG_TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences() {
		return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			// Should never happen. WHAT DID YOU DO?!?!
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground(final Context context) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				String msg;
				try {
					if (mGcm == null) {
						mGcm = GoogleCloudMessaging.getInstance(context);
					}
					String regId = mGcm.register(PROJECT_NUMBER);
					msg = "Device registered, registration ID=" + regId;
					Log.d(LOG_TAG, msg);

					storeRegistrationId(context, regId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					Log.e(LOG_TAG, msg);
				}
				return null;
			}
		}.execute(null, null, null);
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences();
		int appVersion = getAppVersion(context);

		Log.i(LOG_TAG, "Saving regId on app version " + appVersion);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.apply();
	}
}