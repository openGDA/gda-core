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

import java.awt.Dimension;
import java.io.IOException;

import javax.media.Codec;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaException;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.Owned;
import javax.media.Player;
import javax.media.Processor;
import javax.media.control.QualityControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

/**
 * Standalone program which connects to a Firewire camera which can be controlled by the generic CMU J1394 driver and
 * broadcasts it over UDP.
 * <p>
 * This must be run on a Windows machine as the CMU driver only works on that OS. Before use: install jmf, but ensure
 * that the gda's version of jmf.jar and jmf.properties is on your classpath
 * <p>
 * For a performance boost when running from XP, ensure that Windows patch WindowsXP-KB885222-v2-x86-ENU.exe is
 * installed. This will remove the data rate limitation for Firewire devices introduced by XP service Pack 1.
 */
public class VideoTransmit {

	// Input MediaLocator
	// Can be a file or http or capture source
	private MediaLocator locator;
	private String ipAddress;
	private String port;

	private Processor processor = null;
	private DataSink rtptransmitter = null;
	private DataSource dataOutput = null;

	/**
	 * Constructor.
	 * 
	 * @param locator -
	 *            descriptor of the camera e.g. j1394://0,1,5,4
	 * @param ipAddress -
	 *            UDP address to broadcast over
	 * @param port -
	 *            UDP port to broadcast over
	 */
	public VideoTransmit(MediaLocator locator, String ipAddress, String port) {

		this.locator = locator;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	/**
	 * Starts the transmission. Returns null if transmission started ok. Otherwise it returns a string with the reason
	 * why the setup failed.
	 * 
	 * @return null or reason why setup failed
	 */
	public synchronized String start() {
		String result;

		// Create a processor for the specified media locator
		// and program it to output JPEG/RTP
		result = createProcessor();
		if (result != null)
			return result;

		// Create an RTP session to transmit the output of the
		// processor to the specified IP address and port no.
		result = createTransmitter();
		if (result != null) {
			processor.close();
			processor = null;
			return result;
		}

		// Start the transmission
		processor.start();

		return null;
	}

	/**
	 * Stops the transmission if already started
	 */
	public void stop() {
		synchronized (this) {
			if (processor != null) {
				processor.stop();
				processor.close();
				processor = null;
				rtptransmitter.close();
				rtptransmitter = null;
			}
		}
	}

	private String createProcessor() {
		if (locator == null)
			return "Locator is null";

		DataSource ds;
		try {
			ds = Manager.createDataSource(locator);
		} catch (Exception e) {
			return "Couldn't create DataSource";
		}

		// Try to create a processor to handle the input media locator
		try {
			processor = Manager.createProcessor(ds);
		} catch (NoProcessorException npe) {
			return "Couldn't create processor";
		} catch (IOException ioe) {
			return "IOException creating processor";
		}

		// Wait for it to configure
		boolean result = waitForState(processor, Processor.Configured);
		if (result == false)
			return "Couldn't configure processor";

		// Get the tracks from the processor
		TrackControl[] tracks = processor.getTrackControls();

		// Do we have at least one track?
		if (tracks == null || tracks.length < 1)
			return "Couldn't find tracks in processor";

		boolean programmed = false;

		// Search through the tracks for a video track
		for (int i = 0; i < tracks.length; i++) {
			Format format = tracks[i].getFormat();
			if (tracks[i].isEnabled() && format instanceof VideoFormat && !programmed) {

				// Found a video track. Try to program it to output JPEG/RTP
				// Make sure the sizes are multiple of 8's.
				Dimension size = ((VideoFormat) format).getSize();
				float frameRate = ((VideoFormat) format).getFrameRate();
				int w = (size.width % 8 == 0 ? size.width : (size.width / 8) * 8);
				int h = (size.height % 8 == 0 ? size.height : (size.height / 8) * 8);
				VideoFormat jpegFormat = new VideoFormat(VideoFormat.JPEG_RTP, new Dimension(w, h),
						Format.NOT_SPECIFIED, Format.byteArray, frameRate);
				tracks[i].setFormat(jpegFormat);
				System.err.println("Video transmitted as:");
				System.err.println("  " + jpegFormat);
				// Assume succesful
				programmed = true;
			} else
				tracks[i].setEnabled(false);
		}

		if (!programmed)
			return "Couldn't find video track";

		// Set the output content descriptor to RAW_RTP
		ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		processor.setContentDescriptor(cd);

		// Realize the processor. This will internally create a flow
		// graph and attempt to create an output datasource for JPEG/RTP
		// video frames.
		result = waitForState(processor, Controller.Realized);
		if (result == false)
			return "Couldn't realize processor";

		// Set the JPEG quality to .5.
		setJPEGQuality(processor, 0.5f);

		// Get the output data source of the processor
		dataOutput = processor.getDataOutput();
		return null;
	}

	// Creates an RTP transmit data sink. This is the easiest way to create
	// an RTP transmitter. The other way is to use the RTPSessionManager API.
	// Using an RTP session manager gives you more control if you wish to
	// fine tune your transmission and set other parameters.
	private String createTransmitter() {
		// Create a media locator for the RTP data sink.
		// For example:
		// rtp://129.130.131.132:42050/video
		String rtpURL = "rtp://" + ipAddress + ":" + port + "/video";
		MediaLocator outputLocator = new MediaLocator(rtpURL);

		// Create a data sink, open it and start transmission. It will wait
		// for the processor to start sending data. So we need to start the
		// output data source of the processor. We also need to start the
		// processor itself, which is done after this method returns.
		try {
			rtptransmitter = Manager.createDataSink(dataOutput, outputLocator);
			rtptransmitter.open();
			rtptransmitter.start();
			dataOutput.start();
		} catch (MediaException me) {
			return "Couldn't create RTP data sink";
		} catch (IOException ioe) {
			return "Couldn't create RTP data sink";
		}

		return null;
	}

	/**
	 * Setting the encoding quality to the specified value on the JPEG encoder. 0.5 is a good default.
	 * 
	 * @param p
	 * @param val
	 */
	void setJPEGQuality(Player p, float val) {

		Control cs[] = p.getControls();
		QualityControl qc = null;
		VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

		// Loop through the controls to find the Quality control for
		// the JPEG encoder.
		for (int i = 0; i < cs.length; i++) {

			if (cs[i] instanceof QualityControl && cs[i] instanceof Owned) {
				Object owner = ((Owned) cs[i]).getOwner();

				// Check to see if the owner is a Codec.
				// Then check for the output format.
				if (owner instanceof Codec) {
					Format fmts[] = ((Codec) owner).getSupportedOutputFormats(null);
					for (int j = 0; j < fmts.length; j++) {
						if (fmts[j].matches(jpegFmt)) {
							qc = (QualityControl) cs[i];
							qc.setQuality(val);
							System.err.println("- Setting quality to " + val + " on " + qc);
							break;
						}
					}
				}
				if (qc != null)
					break;
			}
		}
	}

	/*******************************************************************************************************************
	 * Convenience methods to handle processor's state changes.
	 ******************************************************************************************************************/

	private Integer stateLock = new Integer(0);
	private boolean failed = false;

	Integer getStateLock() {
		return stateLock;
	}

	void setFailed() {
		failed = true;
	}

	private synchronized boolean waitForState(Processor p, int state) {
		p.addControllerListener(new StateListener());
		failed = false;

		// Call the required method on the processor
		if (state == Processor.Configured) {
			p.configure();
		} else if (state == Controller.Realized) {
			p.realize();
		}

		// Wait until we get an event that confirms the
		// success of the method, or a failure event.
		// See StateListener inner class
		while (p.getState() < state && !failed) {
			synchronized (getStateLock()) {
				try {
					getStateLock().wait();
				} catch (InterruptedException ie) {
					return false;
				}
			}
		}

		if (failed) {
			return false;
		}
		return true;
	}

	/*******************************************************************************************************************
	 * Inner Classes
	 ******************************************************************************************************************/

	class StateListener implements ControllerListener {

		@Override
		public void controllerUpdate(ControllerEvent ce) {

			// If there was an error during configure or
			// realize, the processor will be closed
			if (ce instanceof ControllerClosedEvent)
				setFailed();

			// All controller events, send a notification
			// to the waiting thread in waitForState method.
			synchronized (getStateLock()) {
				getStateLock().notifyAll();
			}
		}
	}

	/**
	 * Starts the stream
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// We need three parameters to do the transmission
		// For example,
		// java VideoTransmit file:/C:/media/test.mov 129.130.131.132 42050

		// test values for flea:
		// args = new String[3];
		// args[0] = "j1394://0,1,5,4";
		// args[1] = "224.120.120.120";
		// args[2] = "22224";

		if (args.length < 3) {
			System.err.println("Usage: VideoTransmit <sourceURL> <destIP> <destPort>");
			System.exit(-1);
		}

		// Create a video transmit object with the specified params.
		VideoTransmit vt = new VideoTransmit(new MediaLocator(args[0]), args[1], args[2]);
		// Start the transmission
		String result = vt.start();

		// result will be non-null if there was an error. The return
		// value is a String describing the possible error. Print it.
		if (result != null) {
			System.err.println("Error : " + result);
			System.exit(0);
		}

	}
}
