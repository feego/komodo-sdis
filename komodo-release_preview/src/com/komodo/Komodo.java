package com.komodo;

import com.komodo.R;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Komodo extends Activity {
	private boolean wificonnected = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_komodo);

		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wifiMgr.isWifiEnabled()) {
			wificonnected = false;

			new AlertDialog.Builder(this)
					.setTitle("Wi-Fi Connection")
					.setMessage("You must enable Wi-Fi to use Komodo.")
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									//System.exit(-1);
								}
							}).show();

		}

		final ImageView myImageView = (ImageView) findViewById(R.id.kom_imageview);
		final Animation fadeInAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fadeout);

		final Handler handler = new Handler();

		if (wificonnected)
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					myImageView.startAnimation(fadeInAnimation);
					myImageView.setVisibility(View.INVISIBLE);

					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							Intent s = new Intent(Komodo.this,
									ConnectionActivity.class);
							startActivity(s);
							finish();
						}
					}, 2000);
				}
			}, 2000);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
