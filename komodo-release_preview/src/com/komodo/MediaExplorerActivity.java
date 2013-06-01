package com.komodo;

import java.util.ArrayList;

import com.komodo.player.Player;
import com.komodo.tcp.TCPClient;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import com.komodo.api.imdb.ImdbAPI;
import android.widget.ListView;
import android.widget.TextView;

public class MediaExplorerActivity extends Activity {
	public static ArrayList<String> mediaFiles = new ArrayList<String>();
	private ListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.explorer_activity);

		TextView nofiles = (TextView) findViewById(R.id.textView1);
		nofiles.setVisibility(View.GONE);
		
		ListView mList = (ListView) findViewById(R.id.list);
		mAdapter = new ListAdapter(this, mediaFiles);
		mList.setAdapter(mAdapter);

		new TCPClient(mAdapter, nofiles).execute();
		mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				imdbSearch(Session.filesOnServer.get(arg2).getName());
				return true;
			}
		});
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				Session.playingFilePath = "http://" + Session.serverAddress
						+ ":" + Session.HTTP_PORT + "/"
						+ Session.filesOnServer.get(position).getServerPath();
				System.out.println(Session.playingFilePath);
				Intent s = new Intent(MediaExplorerActivity.this, Player.class);
				startActivity(s);
			}

		});
	}

	protected void imdbSearch(String filename) {
		final EditText input = new EditText(this);
		input.setText(getSimplifiedFilename(filename));

		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		new AlertDialog.Builder(this)
				.setTitle("IMDb")
				.setMessage("Find your media on IMDb.")
				.setView(input)
				.setNeutralButton("Next",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								ImdbAPI imdb = new ImdbAPI();
								boolean found = imdb.imdbApi(null, input
										.getText().toString(), alertDialog);
								if (found) {
									askUserCorrectMedia(
											imdbParseTitle(imdb.info),
											imdbParseURL(imdb.info));
								}
							}
						}).show();

	}

	protected void askUserCorrectMedia(String title, final String imdbUrl) {
		new AlertDialog.Builder(this)
				.setTitle("IMDb")
				.setMessage(title + "\nIs this the movie you're looking for?")
				.setNegativeButton("No", null)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Intent browserIntent = new Intent(
										Intent.ACTION_VIEW, Uri.parse(imdbUrl));
								startActivity(browserIntent);
							}
						}).show();
	}

	private String getSimplifiedFilename(String name) {
		String title;
		if (name.contains(".")) {
			name = name.substring(0, name.lastIndexOf('.'));
			title = name.replaceAll("[^\\p{L}\\p{N}]", " ");
			return title;
		} else
			return name;
	}

	private String imdbParseURL(String[] array) {
		return imdbParse(array, "IMDBURL");
	}

	private String imdbParseTitle(String[] array) {
		return imdbParse(array, "TITLE");
	}

	private String imdbParse(String[] array, String filter) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].contains(filter))
				return array[i + 1];
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public class ListAdapter extends BaseAdapter {
		private ArrayList<String> mListItems;
		private LayoutInflater mLayoutInflater;

		public ListAdapter(Context context, ArrayList<String> arrayList) {
			mListItems = arrayList;
			mLayoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mListItems.size();
		}

		@Override
		public Object getItem(int i) {
			return null;
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup viewGroup) {
			if (view == null) {
				view = mLayoutInflater.inflate(R.layout.list_item, null);
			}

			String stringItem = mListItems.get(position);
			if (stringItem != null) {

				TextView itemName = (TextView) view
						.findViewById(R.id.list_item_text_view);

				if (itemName != null) {
					itemName.setText(stringItem);
				}
			}
			return view;

		}
	}
}
