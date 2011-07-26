package jmx.remote.jms;

//import java.awt.AWTException;

import java.awt.Color;

import java.awt.Graphics2D;

import java.awt.MenuItem;

import java.awt.PopupMenu;

import java.awt.SystemTray;

import java.awt.TrayIcon;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;



public class Test {

	private static final int WIDTH		= 24;

	private static final int HEIGHT		= 24;

	private int              value		= 0;	// valeur

	private TrayIcon         trayIcon	= null;

	private MyImageRefresh   refresh	= null;



	public static void main(final String[] args) {

		new Test();

	}



	public Test() {

		buildTray();

	}



	private void buildTray() {

		if (SystemTray.isSupported()) {



			final SystemTray tray = SystemTray.getSystemTray();

			final PopupMenu popup = new PopupMenu();

			final MenuItem defaultItem = new MenuItem("Quitter");

			defaultItem.addActionListener(new ActionListener() {

				public void actionPerformed(final ActionEvent e) {

					if (refresh != null)

						refresh.end();

					System.exit(0);

				}

			});

			popup.add(defaultItem);



			trayIcon = new TrayIcon(getImage(), "Test icon thread !", popup);



			final ActionListener actionListener = new ActionListener() {

				public void actionPerformed(final ActionEvent e) {

					trayIcon.displayMessage("Java 6 new feature !",

							"Le System Tray en action !",

							TrayIcon.MessageType.INFO);

				}

			};

			trayIcon.setImageAutoSize(true);

			trayIcon.addActionListener(actionListener);



			try {

				tray.add(trayIcon);

				// d�marrage du thread

				refresh = new MyImageRefresh();

				refresh.start();

			} catch (final Exception e) {

				e.printStackTrace();

			}



		} else {

			// ...

		}

	}



	public BufferedImage getImage() {

		// cr�ation de l'image

		// ici le code est fait � l'arache et absolument pas optimis�.

		final BufferedImage img = new BufferedImage(WIDTH, HEIGHT,

				BufferedImage.TYPE_INT_ARGB);

		final Graphics2D g2 = img.createGraphics();

		g2.setColor(Color.BLACK);

		g2.drawString("" + value, 2, 15);

		return img;

	}



	private class MyImageRefresh extends Thread {

		private boolean	end	= false;



		public void run() {

			while (!end) {

				// refresh toute les secondes

				try {

					sleep(1000);

				} catch (final InterruptedException e) {

					e.printStackTrace();

				}

				// juste un petit test pour faire bouger le text de l'icon

				if (value < 100)

					value++;

				else

					value = 0;

                                // modification de l'image

				synchronized (trayIcon) {

					trayIcon.setImage(getImage());

				}

			}

		}



		public void end() {

			end = true;

		}

	}

}