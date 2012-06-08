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
import java.util.List;
import java.util.Vector;

import net.sf.jlibdc1394.JDC1394Cam;
import net.sf.jlibdc1394.JDC1394CamPort;
import net.sf.jlibdc1394.JDC1394Helper;
import net.sf.jlibdc1394.JDC1394VideoMode;
import net.sf.jlibdc1394.JDC1394VideoSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standalone program, or a Findable object, which collects images from a
 * Firwire camera using the generic CMU driver and then sends them via a socket
 * to GUI objects for display.This is a BFI solution so is not very scalable!
 * The fastest speed I have seen for this are 6 fps.
 * <p>
 * Its advantage is that the camera settings may be adjusted from the
 * client-side, which may be useful in some situations.
 * <p>
 * This must be run on a Windows machine as the CMU driver, and the JLibDC
 * wrapper, only work on such machines.For a performance boost when running from
 * XP, ensure that Windows patch WindowsXP-KB885222-v2-x86-ENU.exe is installed.
 * This will remove the data rate limitation for Firewire devices introduced by
 * XP service Pack 1.
 * <p>
 * See the gda.gui.imaging package for objects which work with this class.
 * <p>
 * This is findable and configurable, so it may be started by an ObjectServer,
 * but it is not intended to be operated over CORBA.
 * J1394 protocol string explaination
 * 
 * j1394://0,1,4,3
 * first number represents camera index
 * second number represents CMU format
 * third number represents CMU Mode
 * fourth number represents CMU Frame rate
 * CMU Format
 * 160 X 120 = 0
 * 320 X 240 = 0
 * 640 X 480 = 0
 * 800 X 600 = 1
 * 1024 X 768 = 1
 * 1280 X 960 = 2
 * 1600 X 1200 = 2
 * 
 * CMU Mode
 * 160 X 120(3)YUV = 0
 * 320 X 240(2)YUV = 1
 * 640 X 480(2)YUV = 3
 * 640 X 480(3)RGB = 4
 * 640 X 480(1)YUV = 5
 * 640 X 480(2)Y   = 6
 * 800 X 600(2)YUV = 0
 * 800 X 600(3)RGB = 1
 * 800 X 600(1)Y   = 2
 * 800 X 600(2)Y  =  6
 * 1024 X 768(3/2)YUV = 3
 * 1024 X 768(3)RGB = 4
 * 1024 X 768(1)Y  =  5
 * 1024 X 768(2)Y  =  7
 * 1280 X 960(2)YUV = 0
 * 1280 X 960(3)RGB = 1
 * 1280 X 960(1)Y   = 2
 * 1280 X 960(2)Y   = 6
 * 1600 X 1200(2)YUV = 3
 * 1600 X 1200(3)RGB = 4
 * 1600 X 1200(1)Y   = 5
 * 
 * CMU Frame Rate
 *  0 = 1.875  
 *  1 = 3.75  
 *  2 = 7.5  
 *  3 = 15  
 *  4 = 30  
 *  5 = 60
 */
public class CMUServer implements Findable, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(CMUServer.class);

	/**
	 * Command string to refresh client with latest values
	 */
	public static final String REFRESH = "REFRESH";

	/**
	 * Command string to set new auto exposure
	 */
	public static final String AUTOEXPOSURE = "AUTOEXPOSURE";

	/**
	 * Command string to set new brightness
	 */
	public static final String BRIGHTNESS = "BRIGHTNESS";

	/**
	 * Command string to set new gain
	 */
	public static final String GAIN = "GAIN";

	/**
	 * Command string to set new gamma
	 */
	public static final String GAMMA = "GAMMA";

	/**
	 * Command string to set new iris
	 */
	public static final String IRIS = "IRIS";

	/**
	 * Command string to set new saturation
	 */
	public static final String SATURATION = "SATURATION";

	/**
	 * Command string to set new shutter
	 */
	public static final String SHUTTER = "SHUTTER";

	/**
	 * Command string to set new whitebalance
	 */
	public static final String WHITEBALANCE = "WHITEBALANCE";

	/**
	 * Command string to set new hue
	 */
	public static final String HUE = "HUE";

	/**
	 * Command string to set new sharpness
	 */
	public static final String SHARPNESS = "SHARPNESS";

	private int[] data;

	private String name = "";

	private ServerSocket serverSocket;

	private ServerSocket controllerSocket;

	private Vector<ObjectOutputStream> clients = new Vector<ObjectOutputStream>();

	private HashMap<ObjectOutputStream, ListenForCommandsThread> controllers = new HashMap<ObjectOutputStream, ListenForCommandsThread>();

	private JDC1394Cam camera;

	/**
	 * To run this as an independent program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		CMUServer server = new CMUServer();
		server.start();
	}

	/**
	 * Constructor.
	 */
	public CMUServer() {
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
	 * Starts the server process. This creates two thread, one looking for new
	 * connections and the other sends out images to any live connections.
	 */
	@SuppressWarnings("rawtypes")
	public void start() {
		try {
			serverSocket = new ServerSocket(7777);
			controllerSocket = new ServerSocket(7778);

			JDC1394CamPort camPort = JDC1394Helper.getJDC1394CamPort();
			// assume only one camera connected
			camera = camPort.selectCamera(0);

			JDC1394VideoMode[] availableModes = camera.getVideoModes().getSupportedVideoModes();

			logger.info("Camera found. Starting CMUServer...");

			System.out.println("\nAvailable modes:");
			for (JDC1394VideoMode mode : availableModes) {
				System.out.println(mode.getDescription() + " Frame rates:");

				List rates = mode.frameRates;

				for (int i = 0; i < rates.size(); i++) {
					System.out.println("\t" + rates.get(i));
				}
			}

			System.out.println("\nusing mode #6.");

			JDC1394VideoMode desiredMode = availableModes[6];

			camera.getVideoModes().setVideoMode(desiredMode);

			/**
			 * Sets the video FrameRate. <br/> Valid values are
			 * 
			 * <pre>
			 *      Code | Rate (fps)  
			 *       0   | 1.875  
			 *       1   | 3.75  
			 *       2   | 7.5  
			 *       3   | 15  
			 *       4   | 30  
			 *       5   | 60
			 * 
			 */
			// camera.getVideoModes().setVideoFrameRate(desiredMode.selectedFrameRate);
			camera.getVideoModes().setVideoFrameRate(1);
			camera.startImageAcquisition();
			data = camera.buildImageBuffer();

			uk.ac.gda.util.ThreadManager.getThread(new AcceptClientsThread()).start();
			uk.ac.gda.util.ThreadManager.getThread(new DistributionThread()).start();
			uk.ac.gda.util.ThreadManager.getThread(new AcceptCommandClientsThread()).start();
		} catch (Exception e) {
			logger.debug("Exception: " + e.getMessage());
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
		CameraSettings currentSettings = new CameraSettings();

		currentSettings.autoExposureValue = camera.getVideoSettings().getAutoExposure().getValue1();
		currentSettings.autoExposureMaximum = camera.getVideoSettings().getAutoExposure().getMaxValue();
		currentSettings.autoExposureMinimum = camera.getVideoSettings().getAutoExposure().getMinValue();

		currentSettings.brightnessValue = camera.getVideoSettings().getBrightness().getValue1();
		currentSettings.brightnessMaximum = camera.getVideoSettings().getBrightness().getMaxValue();
		currentSettings.brightnessMinimum = camera.getVideoSettings().getBrightness().getMinValue();

		currentSettings.gainValue = camera.getVideoSettings().getGain().getValue1();
		currentSettings.gainMaximum = camera.getVideoSettings().getGain().getMaxValue();
		currentSettings.gainMinimum = camera.getVideoSettings().getGain().getMinValue();

		currentSettings.gammaValue = camera.getVideoSettings().getGamma().getValue1();
		currentSettings.gammaMaximum = camera.getVideoSettings().getGamma().getMaxValue();
		currentSettings.gammaMinimum = camera.getVideoSettings().getGamma().getMinValue();

		currentSettings.hueValue = camera.getVideoSettings().getHue().getValue1();
		currentSettings.hueMaximum = camera.getVideoSettings().getHue().getMaxValue();
		currentSettings.hueMinimum = camera.getVideoSettings().getHue().getMinValue();

		currentSettings.irisValue = camera.getVideoSettings().getIris().getValue1();
		currentSettings.irisMaximum = camera.getVideoSettings().getIris().getMaxValue();
		currentSettings.irisMinimum = camera.getVideoSettings().getIris().getMinValue();

		currentSettings.saturationValue = camera.getVideoSettings().getSaturation().getValue1();
		currentSettings.saturationMaximum = camera.getVideoSettings().getSaturation().getMaxValue();
		currentSettings.saturationMinimum = camera.getVideoSettings().getSaturation().getMinValue();

		currentSettings.sharpnessValue = camera.getVideoSettings().getSharpness().getValue1();
		currentSettings.sharpnessMaximum = camera.getVideoSettings().getSharpness().getMaxValue();
		currentSettings.sharpnessMinimum = camera.getVideoSettings().getSharpness().getMinValue();

		currentSettings.shutterValue = camera.getVideoSettings().getShutter().getValue1();
		currentSettings.shutterMaximum = camera.getVideoSettings().getShutter().getMaxValue();
		currentSettings.shutterMinimum = camera.getVideoSettings().getShutter().getMinValue();

		currentSettings.whiteBalanceValue = camera.getVideoSettings().getWhiteBalance().getValue1();
		currentSettings.whiteBalanceMaximum = camera.getVideoSettings().getWhiteBalance().getMaxValue();
		currentSettings.whiteBalanceMinimum = camera.getVideoSettings().getWhiteBalance().getMinValue();

		return currentSettings;
	}

	/**
	 * The thread which broadcasts images.
	 */
	private class DistributionThread extends Thread implements Runnable {
		@Override
		public void run() {
			Vector<ObjectOutputStream> failedStreams = new Vector<ObjectOutputStream>();

			while (true) {
				try {
					// only acquire images if we have any connections
					if (clients.size() > 0) {
						camera.acquireImageEx(true, data);

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
					logger.debug("CMUImageServer " + getName() + " has stopped unexpectedly: " + e.getMessage());
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
							int value = Integer.parseInt(parts[1]);

							if (parts[0].compareTo(CMUServer.AUTOEXPOSURE) == 0) {
								logger.info("changing auto exposure to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getAutoExposure();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setAutoExposure(currentSettings);
							} else if (parts[0].compareTo(CMUServer.BRIGHTNESS) == 0) {
								logger.info("changing brightness to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getBrightness();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setBrightness(currentSettings);
							} else if (parts[0].compareTo(CMUServer.GAIN) == 0) {
								logger.info("changing gain to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getGain();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setGain(currentSettings);
							} else if (parts[0].compareTo(CMUServer.GAMMA) == 0) {
								logger.info("changing gamma to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getGamma();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setGamma(currentSettings);
							} else if (parts[0].compareTo(CMUServer.IRIS) == 0) {
								logger.info("changing iris to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getIris();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setIris(currentSettings);
							} else if (parts[0].compareTo(CMUServer.SATURATION) == 0) {
								logger.info("changing saturation to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getSaturation();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setSaturation(currentSettings);
							} else if (parts[0].compareTo(CMUServer.SHUTTER) == 0) {
								logger.info("changing shutter to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getShutter();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setShutter(currentSettings);
							} else if (parts[0].compareTo(CMUServer.WHITEBALANCE) == 0) {
								logger.info("changing whitebalance to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getWhiteBalance();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setWhiteBalance(currentSettings);
							} else if (parts[0].compareTo(CMUServer.HUE) == 0) {
								logger.info("changing hue to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getHue();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setHue(currentSettings);
							} else if (parts[0].compareTo(CMUServer.SHARPNESS) == 0) {
								logger.info("changing sharpness to " + parts[1]);
								JDC1394VideoSetting currentSettings = camera.getVideoSettings().getSharpness();
								currentSettings.setValue1(value);
								camera.getVideoSettings().setSharpness(currentSettings);
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
