package com.komodo.tcp;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TCPServer extends Thread {
	private static String filetype;
	private static ArrayList<String> mediaFiles;
	private static String path;

	public static void main(String args[]) {
		new TCPServer(".").run();
	}

	public TCPServer(String absolutePath) {
		path = absolutePath;
	}

	@Override
	public void run() {
		super.run();

		try {
			ServerSocket ss = new ServerSocket(8081);
			Socket s;

			while (true) {
				s = ss.accept();

				searchFiles();
				PrintWriter output = new PrintWriter(s.getOutputStream(), true);

				for (int i = 0; i < mediaFiles.size(); i++) {
					if (output != null && !output.checkError()) {
						output.println(mediaFiles.get(i));
						output.flush();
					}
				}
				s.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean validExtensions(String files) {
		// Sounds
		if (files.endsWith(".mp3") || files.endsWith(".flac")
				|| files.endsWith(".m4a") || files.endsWith(".mid")
				|| files.endsWith(".ogg") || files.endsWith(".mkv")
				|| files.endsWith(".wav")) {
			filetype = "sound";
			return true;
		}/*
		// Images
		else if (files.endsWith(".jpg") || files.endsWith(".gif")
				|| files.endsWith(".png") || files.endsWith(".bmp")
				|| files.endsWith(".webp")) {
			filetype = "image";
			return true;
		}*/
		// Videos
		else if (files.endsWith(".mkv") || files.endsWith(".3gp")
				|| files.endsWith(".mp4")) {
			filetype = "video";
			return true;
		} else
			return false;
	}

	public void searchFiles(String relativePath) {// path é o diretório apartir
													// do qual
		// te dá relatórios relativos
		/*
		 * Instruções 1. Criar 2 ArrayList fora deste método 2.ArrayList1 -
		 * Nomes dos Ficheiros 3.ArrayList2 - Diretórios dos Ficheiros 4.Correr
		 * com o diretório inicial, "." para o actual
		 */
		// Directory path here
		String files;
		File folder = new File(relativePath);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles.length != 0) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					files = listOfFiles[i].getName();
					if (validExtensions(files)) {
						mediaFiles.add(files + "::" + relativePath.substring(path.length()-1) + "::"
								+ filetype);
					}
				} else if (listOfFiles[i].isDirectory()) {
					searchFiles(relativePath + "/" + listOfFiles[i].getName());
				}
			}
		}
	}

	public void searchFiles() {
		mediaFiles = new ArrayList<String>();
		searchFiles(path);
		Collections.sort(mediaFiles);
	}
}
