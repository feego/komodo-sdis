package com.komodo.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import com.komodo.MediaExplorerActivity;
import com.komodo.MediaExplorerActivity.ListAdapter;
import com.komodo.Session;
import com.komodo.files.File;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TCPClient extends AsyncTask<String, Void, String> {
	private String hostName;
	ListAdapter adapter;
	TextView nofilesMessage;
	ProgressBar loading;

	public TCPClient(ListAdapter mAdapter, TextView nofiles, ProgressBar loading) {
		adapter = mAdapter;
		hostName = com.komodo.Session.serverAddress;
		nofilesMessage = nofiles;
		this.loading = loading;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
		boolean recieving = true;
		MediaExplorerActivity.mediaFiles.clear();

		try {
			System.out.println("Entered");
			InetAddress serverAddr = InetAddress.getByName(hostName);
			Socket s = new Socket(serverAddr, Session.TCP_PORT);
			System.out.println("Connected to " + hostName);

			new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					s.getOutputStream())), true);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			while (recieving) {
				String st = in.readLine();

				if (st != null) {
					System.out.println(st);
					File f = new File(st);
					MediaExplorerActivity.mediaFiles.add(f.getName());
					Session.filesOnServer.add(f);
				} else
					recieving = false;
			}

			System.out.println("Saiu");
			s.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Done";
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		if (MediaExplorerActivity.mediaFiles.size() == 0) {
			nofilesMessage.setVisibility(View.VISIBLE);
		}

		loading.setVisibility(View.GONE);
		adapter.notifyDataSetChanged();
	}
}