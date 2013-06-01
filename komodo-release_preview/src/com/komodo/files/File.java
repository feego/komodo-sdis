package com.komodo.files;

import java.util.StringTokenizer;

public class File {
	public static int filesCounter = 0;
	
	private int ID;
	private String type;
	private String serverPath;
	private String name;

	public File(String type, String serverPath, String name) {
		setType(type);
		setServerPath(serverPath);
		setName(name);
		
		setID(filesCounter);
		ID++;
	}
	
	public File(String serverInfoLine) {
		StringTokenizer st = new StringTokenizer(serverInfoLine, "::");

		setName(st.nextToken());
		setServerPath(st.nextToken());
		setType(st.nextToken());
		setServerPath(serverPath + "/" + name);
		
		setID(filesCounter);
		ID++;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getServerPath() {
		return serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = (serverPath.length() > 1) ? serverPath.substring(1) : "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}
}
