package com.komodo.app;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import com.komodo.http.HttpServerThread;
import com.komodo.http.WebServer;
import com.komodo.tcp.TCPServer;

public class Swing extends JFrame {

	private JLabel statusbar;
	protected String streamFolderPath;
	private HttpServerThread httpServer;
	
	private FileOutputStream saveFile;
	private ObjectOutputStream save;

	public Swing() {
		try {
			initGUI();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final void initGUI() throws IOException {
		File f=null;
		try {
			FileInputStream readFile = new FileInputStream("saveFile.sav");
			ObjectInputStream restore = new ObjectInputStream(readFile);
			File file = (File) restore.readObject();
			streamFolderPath = file.getAbsolutePath();

			System.out.println(streamFolderPath);
			new TCPServer(file.getAbsolutePath()).start();
			startHttpServer();
		} catch (IOException | ClassNotFoundException e) {
			streamFolderPath = ".";
			FileOutputStream saveFile = new FileOutputStream("saveFile.sav");
		}

		saveFile = new FileOutputStream("saveFile.sav");
		save = new ObjectOutputStream(saveFile);
		
		f = new File(streamFolderPath);
		save.writeObject(f);

		final TrayIcon trayIcon;
		final SystemTray tray = SystemTray.getSystemTray();

		Image image = new ImageIcon("images/icon.png").getImage();

		PopupMenu popup = new PopupMenu();

		trayIcon = new TrayIcon(image, "Komodo", popup);

		try {
			System.out.println("setting look and feel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Unable to set LookAndFeel");
		}
		if (SystemTray.isSupported()) {
			System.out.println("system tray supported");

			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			ActionListener gerListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						ShowIP grcode = new ShowIP();
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			};
			MenuItem gerItem = new MenuItem("Gerar QRCode");
			gerItem.addActionListener(gerListener);
			popup.add(gerItem);

			ActionListener openListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int returnVal = fc.showDialog(fc,
							"Choose folder to stream...");

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						streamFolderPath = file.getAbsolutePath();
						System.out.println(streamFolderPath	);
						httpServer.destroy();
						try {
							// TODO new TCPServer(streamFolderPath);
							new TCPServer(file.getAbsolutePath());
							startHttpServer();

							saveFile = new FileOutputStream("saveFile.sav");
							save = new ObjectOutputStream(saveFile);
							save.writeObject(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}

					} else {
					}
				}
			};
			MenuItem openItem = new MenuItem("Open");
			openItem.addActionListener(openListener);
			popup.add(openItem);

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Exiting....");
					System.exit(0);
				}
			};
			MenuItem defaultItem = new MenuItem("Exit");
			defaultItem.addActionListener(exitListener);
			popup.add(defaultItem);
		} else {
			System.out.println("system tray not supported");
		}
		try {
			tray.add(trayIcon);
			setVisible(false);
			System.out.println("added to SystemTray");
		} catch (AWTException ex) {
			System.out.println("unable to add to tray");
		}

		setIconImage(image);

		ImageIcon img = new ImageIcon("images/kom.png");

		setIconImage(img.getImage());
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	private void startHttpServer() throws UnknownHostException {
		java.net.InetAddress i = java.net.InetAddress.getLocalHost();
		String host = i.getHostAddress();
		httpServer = new HttpServerThread(host, streamFolderPath);
		httpServer.start();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Swing swing = new Swing();
			}
		});
	}

}