package com.komodo.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class WebServer extends NanoHTTPD {

	private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("css", "text/css");
			put("htm", "text/html");
			put("html", "text/html");
			put("xml", "text/xml");
			put("txt", "text/plain");
			put("asc", "text/plain");
			put("gif", "image/gif");
			put("jpg", "image/jpeg");
			put("jpeg", "image/jpeg");
			put("png", "image/png");
			put("mp3", "audio/mpeg");
			put("m3u", "audio/mpeg-url");
			put("mp4", "video/mp4");
			put("ogv", "video/ogg");
			put("flv", "video/x-flv");
			put("mov", "video/quicktime");
			put("swf", "application/x-shockwave-flash");
			put("js", "application/javascript");
			put("pdf", "application/pdf");
			put("doc", "application/msword");
			put("ogg", "application/x-ogg");
			put("zip", "application/octet-stream");
			put("exe", "application/octet-stream");
			put("class", "application/octet-stream");
		}
	};

	private File root;

	public WebServer(String host, int port, File root) {
		super(host, port);
		this.root = root;
	}

	public File getRootDir() {
		return root;
	}

	private String encodeUri(String uri) {
		String newUri = "";
		StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("/"))
				newUri += "/";
			else if (tok.equals(" "))
				newUri += "%20";
			else {
				try {
					newUri += URLEncoder.encode(tok, "UTF-8");
				} catch (UnsupportedEncodingException ignored) {
				}
			}
		}
		return newUri;
	}

	public Response serveFile(String uri, Map<String, String> header,
			File homeDir) {
		Response res = null;

		if (!homeDir.isDirectory())
			res = new Response(Response.Status.INTERNAL_ERROR,
					NanoHTTPD.MIME_PLAINTEXT,
					"INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");

		if (res == null) {
			uri = uri.trim().replace(File.separatorChar, '/');
			if (uri.indexOf('?') >= 0)
				uri = uri.substring(0, uri.indexOf('?'));

			if (uri.startsWith("src/main") || uri.endsWith("src/main")
					|| uri.contains("../"))
				res = new Response(Response.Status.FORBIDDEN,
						NanoHTTPD.MIME_PLAINTEXT,
						"FORBIDDEN: Won't serve ../ for security reasons.");
		}

		File f = new File(homeDir, uri);
		if (res == null && !f.exists())
			res = new Response(Response.Status.NOT_FOUND,
					NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");

		if (res == null && f.isDirectory()) {
			if (!uri.endsWith("/")) {
				uri += "/";
				res = new Response(Response.Status.REDIRECT,
						NanoHTTPD.MIME_HTML,
						"<html><body>Redirected: <a href=\"" + uri + "\">"
								+ uri + "</a></body></html>");
				res.addHeader("Location", uri);
			}

			if (res == null) {
				if (new File(f, "index.html").exists())
					f = new File(homeDir, uri + "/index.html");
				else if (new File(f, "index.htm").exists())
					f = new File(homeDir, uri + "/index.htm");
				else if (f.canRead()) {
					String[] files = f.list();
					String msg = "<html><body><h1>Directory " + uri
							+ "</h1><br/>";

					if (uri.length() > 1) {
						String u = uri.substring(0, uri.length() - 1);
						int slash = u.lastIndexOf('/');
						if (slash >= 0 && slash < u.length())
							msg += "<b><a href=\""
									+ uri.substring(0, slash + 1)
									+ "\">..</a></b><br/>";
					}

					if (files != null) {
						for (int i = 0; i < files.length; ++i) {
							File curFile = new File(f, files[i]);
							boolean dir = curFile.isDirectory();
							if (dir) {
								msg += "<b>";
								files[i] += "/";
							}

							msg += "<a href=\"" + encodeUri(uri + files[i])
									+ "\">" + files[i] + "</a>";

							if (curFile.isFile()) {
								long len = curFile.length();
								msg += " &nbsp;<font size=2>(";
								if (len < 1024)
									msg += len + " bytes";
								else if (len < 1024 * 1024)
									msg += len / 1024 + "."
											+ (len % 1024 / 10 % 100) + " KB";
								else
									msg += len / (1024 * 1024) + "." + len
											% (1024 * 1024) / 10 % 100 + " MB";

								msg += ")</font>";
							}
							msg += "<br/>";
							if (dir)
								msg += "</b>";
						}
					}
					msg += "</body></html>";
					res = new Response(msg);
				} else {
					res = new Response(Response.Status.FORBIDDEN,
							NanoHTTPD.MIME_PLAINTEXT,
							"FORBIDDEN: No directory listing.");
				}
			}
		}

		try {
			if (res == null) {
				String mime = null;
				int dot = f.getCanonicalPath().lastIndexOf('.');
				if (dot >= 0)
					mime = MIME_TYPES.get(f.getCanonicalPath()
							.substring(dot + 1).toLowerCase());
				if (mime == null)
					mime = NanoHTTPD.MIME_DEFAULT_BINARY;

				String etag = Integer.toHexString((f.getAbsolutePath()
						+ f.lastModified() + "" + f.length()).hashCode());

				long startFrom = 0;
				long endAt = -1;
				String range = header.get("range");
				if (range != null) {
					if (range.startsWith("bytes=")) {
						range = range.substring("bytes=".length());
						int minus = range.indexOf('-');
						try {
							if (minus > 0) {
								startFrom = Long.parseLong(range.substring(0,
										minus));
								endAt = Long.parseLong(range
										.substring(minus + 1));
							}
						} catch (NumberFormatException ignored) {
						}
					}
				}

				long fileLen = f.length();
				if (range != null && startFrom >= 0) {
					if (startFrom >= fileLen) {
						res = new Response(
								Response.Status.RANGE_NOT_SATISFIABLE,
								NanoHTTPD.MIME_PLAINTEXT, "");
						res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
						res.addHeader("ETag", etag);
					} else {
						if (endAt < 0)
							endAt = fileLen - 1;
						long newLen = endAt - startFrom + 1;
						if (newLen < 0)
							newLen = 0;

						final long dataLen = newLen;
						FileInputStream fis = new FileInputStream(f) {
							@Override
							public int available() throws IOException {
								return (int) dataLen;
							}
						};
						fis.skip(startFrom);

						res = new Response(Response.Status.PARTIAL_CONTENT,
								mime, fis);
						res.addHeader("Content-Length", "" + dataLen);
						res.addHeader("Content-Range", "bytes " + startFrom
								+ "-" + endAt + "/" + fileLen);
						res.addHeader("ETag", etag);
					}
				} else {
					if (etag.equals(header.get("if-none-match")))
						res = new Response(Response.Status.NOT_MODIFIED, mime,
								"");
					else {
						res = new Response(Response.Status.OK, mime,
								new FileInputStream(f));
						res.addHeader("Content-Length", "" + fileLen);
						res.addHeader("ETag", etag);
					}
				}
			}
		} catch (IOException ioe) {
			res = new Response(Response.Status.FORBIDDEN,
					NanoHTTPD.MIME_PLAINTEXT,
					"FORBIDDEN: Reading file failed.");
		}

		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

	@Override
	public Response serve(String uri, Method method,
			Map<String, String> header, Map<String, String> parms,
			Map<String, String> files) {
		System.out.println(method + " '" + uri + "' ");

		Iterator<String> e = header.keySet().iterator();
		while (e.hasNext()) {
			String value = e.next();
			System.out.println("  HDR: '" + value + "' = '" + header.get(value)
					+ "'");
		}

		e = parms.keySet().iterator();
		while (e.hasNext()) {
			String value = e.next();
			System.out.println("  PRM: '" + value + "' = '" + parms.get(value)
					+ "'");
		}
		e = files.keySet().iterator();
		while (e.hasNext()) {
			String value = e.next();
			System.out.println("  UPLOADED: '" + value + "' = '"
					+ files.get(value) + "'");
		}

		return serveFile(uri, header, getRootDir());
	}

	public static void main(String[] args) {
		int port = 8080;
		String host = "192.168.1.89";
		File root = new File(".").getAbsoluteFile();

		WebServer server = new WebServer(host, port, root);

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
}
