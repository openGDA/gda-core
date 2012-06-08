/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.server.imaging;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Framegrabber process for cameras operable by the CMU driver. Images are sent over TCP/IP to clients (e.g.
 * CMUIMagePanel). This is a BFI solution so is not very scalable! This is findable and configurable, so it may be
 * started by an ObjectServer, but it is not intended to work over CORBA.
 */
public class CMUDummyServer implements Findable, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(CMUDummyServer.class);

	private int[] data = new int[786432];

	private String name = "";

	private volatile ServerSocket serverSocket;

	private volatile ServerSocket controllerSocket;

	private volatile Vector<ObjectOutputStream> clients = new Vector<ObjectOutputStream>();

	private volatile HashMap<ObjectOutputStream, ListenForCommandsThread> controllers = new HashMap<ObjectOutputStream, ListenForCommandsThread>();

	private volatile CameraSettings currentSettings = null;

	/**
	 * Test harness.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		CMUDummyServer server = new CMUDummyServer();
		server.start();
	}

	/**
	 * Constructor.
	 */
	public CMUDummyServer() {
		currentSettings = new CameraSettings();
	}

	@Override
	public void configure() throws FactoryException {
		start();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Starts the server process. This creates two thread, one looking for new connections and the other sends out
	 * images to any live connections.
	 */
	public void start() {
		try {
			serverSocket = new ServerSocket(7777);
			controllerSocket = new ServerSocket(7778);
			//
			// JDC1394CamPort camPort = JDC1394Helper.getJDC1394CamPort();
			// //assume only one camera connected
			// camera = camPort.selectCamera(0);
			//
			// JDC1394VideoMode[] availableModes =
			// camera.getVideoModes().getSupportedVideoModes();

			logger.info("Starting simulated CMUServer...");

			// System.out.println("\nAvailable modes:");
			// for (JDC1394VideoMode mode : availableModes)
			// {
			// System.out.println(mode.getDescription() + " Frame rates:");
			//            
			// List rates = mode.frameRates;
			//            
			// for (int i =0 ; i < rates.size() ; i++)
			// {
			// System.out.println("\t" + rates.get(i));
			// }
			// }
			// 
			// System.out.println("\nusing mode #6.");
			//
			// JDC1394VideoMode desiredMode = availableModes[6];
			//         
			// camera.getVideoModes().setVideoMode(desiredMode);
			//
			// /**
			// * Sets the video FrameRate. <br/> Valid values are
			// *
			// * <pre>
			// * Code | Rate (fps)
			// * 0 | 1.875
			// * 1 | 3.75
			// * 2 | 7.5
			// * 3 | 15
			// * 4 | 30
			// * 5 | 60
			// *
			// */
			// //
			// camera.getVideoModes().setVideoFrameRate(desiredMode.selectedFrameRate);
			// camera.getVideoModes().setVideoFrameRate(1);
			// camera.startImageAcquisition();
			// data = camera.buildImageBuffer();

			uk.ac.gda.util.ThreadManager.getThread(new AcceptClientsThread()).start();
			uk.ac.gda.util.ThreadManager.getThread(new DistributionThread()).start();
			uk.ac.gda.util.ThreadManager.getThread(new AcceptCommandClientsThread()).start();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
		}
	}

	private synchronized void notifyAllControllers() {
		CameraSettings currentSettings = getCurrentSettings();

		Vector<ObjectOutputStream> failedStreams = new Vector<ObjectOutputStream>();

		for (ObjectOutputStream thisStream : controllers.keySet()) {
			try {
				thisStream.writeObject(currentSettings);
				thisStream.flush();
				thisStream.reset();
			} catch (IOException e) {
				failedStreams.add(thisStream);
			}
		}

		// clean up any lost connections
		for (ObjectOutputStream failedController : failedStreams) {
			ListenForCommandsThread theThread = controllers.get(failedController);
			// this close is necessary to stop the thread (interrupt doesn't
			// work
			// here)
			try {
				theThread.rd.close();
			} catch (IOException e1) {
				// ignore and carry on
			}
			try {
				failedController.close();
			} catch (IOException e) {
				// ignore this and carry on
			}
			controllers.remove(failedController);
		}
	}

	private synchronized CameraSettings getCurrentSettings() {
		return this.currentSettings;
	}

	/**
	 * The thread which broadcasts images.
	 */
	private class DistributionThread extends Thread implements Runnable {
		@Override
		public void run() {
			Vector<ObjectOutputStream> failedStreams = new Vector<ObjectOutputStream>();

			for (int i = 0; i < 768432; i++) {
				data[i] = 0;
			}

			while (true) {
				try {
					// only acquire images if we have any connections
					if (clients.size() > 0) {
						// camera.acquireImageEx(true, data);

						// loop over all connections and send them the latest
						// image
						for (ObjectOutputStream client : clients) {
							try {
								// this could be faster??
								// BufferedImage image2 = new
								// BufferedImage(1024, 768,
								// BufferedImage.TYPE_INT_RGB);
								// image2.getRaster().setDataElements(1024, 768,
								// data);

								// ImageIO.write(image2, "jpeg", client);
								// ImageIO.write(image2, "jpeg", new
								// File("C:\test.jpg"));

								client.writeObject(data);
								client.flush();
								client.reset();

								Thread.sleep(5000);

							} catch (IOException e) {
								failedStreams.add(client);
							}
						}

						// clean up any lost connections
						for (ObjectOutputStream failedClient : failedStreams) {
							clients.remove(failedClient);
						}
						failedStreams = new Vector<ObjectOutputStream>();

					} else {
						Thread.sleep(5000);
					}
				} catch (Exception e) {
					logger.error("CMUImageServer " + getName() + " has stopped unexpectedly: " + e.getMessage());
				}
			}

		}

	}

	/**
	 * The thread which waits for new connections
	 */
	private class AcceptClientsThread extends Thread implements Runnable {
		@Override
		public void run() {
			while (true) {
				// start a new thread for every connection to this socket
				try {
					clients.add(new ObjectOutputStream(serverSocket.accept().getOutputStream()));
				} catch (IOException e) {
					// ignore!
				}
			}
		}
	}

	/**
	 * The thread which waits for new connections
	 */
	private class AcceptCommandClientsThread extends Thread implements Runnable {
		@Override
		public void run() {
			while (true) {
				// start a new thread for every connection to this socket
				try {
					Socket newSocket = controllerSocket.accept();

					System.out.println("new command socket accepted " + newSocket);

					ListenForCommandsThread commandsThread = new ListenForCommandsThread(new BufferedReader(
							new InputStreamReader(newSocket.getInputStream())));

					ObjectOutputStream updateStream = new ObjectOutputStream(newSocket.getOutputStream());

					controllers.put(updateStream, commandsThread);

					commandsThread.start();
				} catch (IOException e) {
					// ignore!
				}
			}
		}
	}

	/**
	 * The thread which waits for new connections
	 */
	private class ListenForCommandsThread extends Thread implements Runnable {
		protected BufferedReader rd;

		/**
		 * Constructor
		 * 
		 * @param reader
		 */
		public ListenForCommandsThread(BufferedReader reader) {
			rd = reader;
		}

		@Override
		public void run() {
			// start a new thread for every connection to this socket
			try {
				// System.out.println("started new command listening thread");

				while (true) {
					String str;
					while ((str = rd.readLine()) != null) {
						// System.out.println("command recieved: " + str);

						// unpack the str
						String[] parts = str.split(" ");

						// if a single element then expect an update request
						if (parts.length == 1 && parts[0].compareTo(CMUServer.REFRESH) == 0) {
							notifyAllControllers();
						}

						// else make change to camera and update all controllers
						// with
						// new settings
						else if (parts.length == 2) {
							// int value = Integer.parseInt(parts[1]);

							if (parts[0].compareTo(CMUServer.AUTOEXPOSURE) == 0) {
								logger.info("changing auto exposure to " + parts[1]);
								currentSettings.autoExposureValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.BRIGHTNESS) == 0) {
								logger.info("changing brightness to " + parts[1]);
								currentSettings.brightnessValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.GAIN) == 0) {
								logger.info("changing gain to " + parts[1]);
								currentSettings.gainValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.GAMMA) == 0) {
								logger.info("changing gamma to " + parts[1]);
								currentSettings.gammaValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.IRIS) == 0) {
								logger.info("changing iris to " + parts[1]);
								currentSettings.irisValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.SATURATION) == 0) {
								logger.info("changing saturation to " + parts[1]);
								currentSettings.saturationValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.SHUTTER) == 0) {
								logger.info("changing shutter to " + parts[1]);
								currentSettings.shutterValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.WHITEBALANCE) == 0) {
								logger.info("changing whitebalance to " + parts[1]);
								currentSettings.whiteBalanceValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.HUE) == 0) {
								logger.info("changing hue to " + parts[1]);
								currentSettings.hueValue = Integer.parseInt(parts[1]);
							} else if (parts[0].compareTo(CMUServer.SHARPNESS) == 0) {
								logger.info("changing sharpness to " + parts[1]);
								currentSettings.sharpnessValue = Integer.parseInt(parts[1]);
							}
							notifyAllControllers();
						}
					}
				}
			} catch (Exception e) {
				// ignore!
			}
		}
	}
}
