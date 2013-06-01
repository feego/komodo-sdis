package com.komodo.http;

import java.io.File;
import java.io.IOException;

public class HttpServerThread extends Thread {
	private final int HttpPort = 8080;
	private String hostname;
	private String rootPath;
	private WebServer server;
	
	
	public HttpServerThread(String host, String rootFolderPath) {
		hostname = host;
		rootPath = rootFolderPath;
	}
	
	@Override
	public void run() {
		File root = new File(rootPath).getAbsoluteFile();		
		server = new WebServer(hostname, HttpPort, root);

		try {
			server.start();
			System.out.println("Http server up.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		try {
			System.in.read();
		} catch (Throwable ignored) {
		}

		server.stop();
		System.out.println("Http server down.");
	}
	
	@Override
	public void destroy() {
		server.stop();
	}
}
