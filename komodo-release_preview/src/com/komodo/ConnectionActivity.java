package com.komodo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.livotov.zxscan.ZXScanHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ConnectionActivity extends Activity {
	private boolean ipEdited = false;
	private EditText targetEditText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connection_activity);

		targetEditText = (EditText) findViewById(R.id.editText1);
		targetEditText
				.setOnEditorActionListener(new DoneOnEditorActionListener());

		targetEditText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!ipEdited) {
					targetEditText.setText("");
					ipEdited = true;
				}
			}

		});

		ImageButton button1 = (ImageButton) findViewById(R.id.imageButton2);
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				handleConnection(((TextView) targetEditText).getText()
						.toString());
			}
		});

		ImageButton button2 = (ImageButton) findViewById(R.id.imageButton1);
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getQRCode();
			}
		});
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.qrButton);
		linearLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getQRCode();
			}
		});

		TextView textview = (TextView) findViewById(R.id.textView2);
		textview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getQRCode();
			}
		});

	}

	protected void getQRCode() {
		ZXScanHelper.scan(this, 1);
	}

	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (resultCode == RESULT_OK && requestCode == 1) {
			String scannedCode = ZXScanHelper.getScannedCode(data);
			targetEditText.setText(scannedCode);
			handleConnection(scannedCode);
		}
	}

	private void handleConnection(String address) {
		Pattern pattern = Pattern
				.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
		Matcher matcher = pattern.matcher(address);

		if (!(matcher.find() && matcher.group().equals(address))) {
			new AlertDialog.Builder(this)
		    .setTitle("Invalid IP")
		    .setMessage("You've typed an invalid IP.")
		    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
		     })
		     .show();
			return;
		}

		Session.filesOnServer.clear();
		Session.serverAddress = address;
		Intent s = new Intent(ConnectionActivity.this,
				MediaExplorerActivity.class);
		startActivity(s);
	}

	class DoneOnEditorActionListener implements OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_GO) {
				handleConnection(((TextView) v).getText().toString());
				return true;
			}
			return false;
		}
	}

}
