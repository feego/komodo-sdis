package com.komodo.app;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.awt.Font;

public class ImageResultFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblNewLabel_2;
	private ImageIcon croppedImg;

	private boolean backAnimation;
	private Timer timer;
	protected String classification;
	private JLabel label;

	/**
	 * Launch the application.
	 */
	public void show(final String imagem, final String texto) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ImageResultFrame frame = new ImageResultFrame(imagem, texto);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ImageResultFrame(String imgPath, String texto) {
		croppedImg = new ImageIcon(imgPath);
		timer = new Timer(7, this);
		timer.start();

		setUndecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setLocation(0, 0);
		setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width,
				java.awt.Toolkit.getDefaultToolkit().getScreenSize().height);
		lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setVisible(false);
		lblNewLabel_2.setBounds(0, 0, 0, 0);
		contentPane.add(lblNewLabel_2);

		JLabel lblNewLabel = new JLabel(texto);
		lblNewLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setBounds(0, 11, 1366, 19);
		contentPane.add(lblNewLabel);

		label = new JLabel("");
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				Window frame = SwingUtilities.windowForComponent((Component) arg0
						.getSource());
				frame.dispose();
			}
		});
		label.setBounds(0, 0, java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize().width, java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize().height);
		contentPane.add(label);
		setBackground(new Color(0, 0, 0, 0));
		setAlwaysOnTop(true);

		loadImage();
	}

	private void loadImage() {
		int imgWidth = croppedImg.getIconWidth();
		int imgHeight = croppedImg.getIconHeight();

		lblNewLabel_2.setBounds((java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize().width - imgWidth) / 2, (java.awt.Toolkit
				.getDefaultToolkit().getScreenSize().height - imgHeight) / 2,
				imgWidth, imgHeight);
		lblNewLabel_2.setIcon(croppedImg);
		lblNewLabel_2.setVisible(true);

		backAnimation = true;
		setBackground(new Color(0, 0, 0, 1));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		animations();
	}

	private void animations() {
		if (backAnimation) {
			if (getBackground().getAlpha() < 170)
				setBackground(new Color(0, 0, 0, getBackground().getAlpha() + 8));
			else
				backAnimation = false;
		}
	}
}
