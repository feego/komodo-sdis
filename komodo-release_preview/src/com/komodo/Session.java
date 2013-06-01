package com.komodo;

import java.util.ArrayList;

import com.komodo.files.File;

public class Session {
	public final static int HTTP_PORT = 8080;
	public final static int TCP_PORT = 8081;
	
	public static String serverAddress;
	public static ArrayList<File> filesOnServer = new ArrayList<File>();
	public static String playingFilePath;
}
