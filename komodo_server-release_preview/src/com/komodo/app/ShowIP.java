package com.komodo.app;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class ShowIP {

	public ShowIP() throws UnknownHostException {
		/////////GET IP/////////////////////////////
		java.net.InetAddress i = java.net.InetAddress.getLocalHost();
		String address = i.getHostAddress();
		/////////////////////////////////////////////
		
		////////GENERATE QRCODE//////////////////////
		ByteArrayOutputStream out = QRCode.from(address).to(ImageType.PNG).stream();

		String qrcfile = "images/qrc.png";
		try {
			FileOutputStream fout = new FileOutputStream(new File(qrcfile));

			fout.write(out.toByteArray());

			fout.flush();
			fout.close();

		} catch (FileNotFoundException e) {
			// Do Logging
			e.printStackTrace();
		} catch (IOException e) {
			// Do Logging
			e.printStackTrace();
		}

		ImageResultFrame qrcode = new ImageResultFrame(qrcfile, address);
		qrcode.show(qrcfile, address);
	}
}

final class ImageLoader {

	private ImageLoader() {
	}

	public static Image getImage(Class relativeClass, String filename) {
		Image returnValue = null;
		InputStream is = relativeClass.getResourceAsStream(filename);
		if (is != null) {
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				int ch;
				while ((ch = bis.read()) != -1) {
					baos.write(ch);
				}
				returnValue = Toolkit.getDefaultToolkit().createImage(
						baos.toByteArray());
			} catch (IOException exception) {
				System.err.println("Error loading: " + filename);
			}
		}
		return returnValue;
	}
}