/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.images.camera;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Localizable;

import java.net.InetAddress;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.control.BufferControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.protocol.DataSource;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to an RTP stream, and runs a capture task at a regular fixed interval that captures frames from the stream
 * at (approximately) the desired frame rate, and passes them to a list of {@link ImageListener}s. By default, the
 * desired frame rate is 25fps.
 * 
 * <p>Configuring the receiver establishes the RTP connection and begins image capture. Image capture can be turned off
 * and on using the {@link #stop()} and {@link #start()} methods.
 * 
 * <p>The entire connection can be terminated using {@link #closeConnection()}, and then started again using
 * {@link #createConnection()} (which will also resume image capture once the connection has been established).
 */
public abstract class RTPStreamReceiverBase<E> implements VideoReceiver<E>, Findable, Configurable, ReceiveStreamListener, ControllerListener, Localizable {
	
	private static final Logger logger = LoggerFactory.getLogger(RTPStreamReceiverBase.class);
	
	protected String name;
	
	protected String host;
	
	protected int port;
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	protected int desiredFrameRate = 25;
	
	protected int bufferSize = 350;
	
	protected Set<ImageListener<E>> listeners = new CopyOnWriteArraySet<ImageListener<E>>();
	
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setDesiredFrameRate(int desiredFrameRate) {
		this.desiredFrameRate = desiredFrameRate;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public void addImageListener(ImageListener<E> listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeImageListener(ImageListener<E> listener) {
		listeners.remove(listener);
	}
	
	protected RTPManager rtpManager;
	
	protected Player player;
	
	protected FrameGrabbingControl frameGrabber;

	protected Timer timer;
	
	@Override
	public void configure() throws FactoryException {
		createConnection();
	}
	
	enum ReceiverState {
		
		STOPPED,
		
		STARTING,
		
		STARTED
	}

	protected ReceiverState state = ReceiverState.STOPPED;
	
	protected boolean captureRunning;
	
	protected static final int PLAYER_WAIT_TIME = 30000;
	
	@Override
	public synchronized void createConnection() {
		if (state != ReceiverState.STOPPED) {
			return;
		}
		
		state = ReceiverState.STARTING;
		
		try {
			player = null;
			
			logger.info("Connecting to RTP stream from " + host + ":" + port);

			rtpManager = RTPManager.newInstance();
			rtpManager.addReceiveStreamListener(this);

			InetAddress hostAddress = InetAddress.getByName(host);
			SessionAddress localAddress;
			SessionAddress destinationAddress;
			if (hostAddress.isMulticastAddress()) {
				// local and remote address pairs are identical
				localAddress = new SessionAddress(hostAddress, port, 1);
				destinationAddress = new SessionAddress(hostAddress, port, 1);
			} else {
				localAddress = new SessionAddress(InetAddress.getLocalHost(), port);
				destinationAddress = new SessionAddress(hostAddress, port);
			}

			rtpManager.initialize(localAddress);

			BufferControl bc = (BufferControl) rtpManager.getControl(BufferControl.class.getName());
			if (bc != null) {
				bc.setBufferLength(bufferSize);
			}
			
			rtpManager.addTarget(destinationAddress);

			long startTime = System.currentTimeMillis();
			while (true) {
				long timeWaited = System.currentTimeMillis() - startTime;
				if (player != null || timeWaited > PLAYER_WAIT_TIME) {
					break;
				}
				Thread.sleep(500);
			}
			
			if (player != null) {
				state = ReceiverState.STARTED;
				start();
			} else {
				state = ReceiverState.STOPPED;
				logger.error("Player was not created within " + PLAYER_WAIT_TIME + "ms");
			}
		} catch (Exception e) {
			state = ReceiverState.STOPPED;
			logger.error("Could not connect to RTP stream", e);
		}
	}
	
	@Override
	public synchronized void start() {
		if (captureRunning) {
			return;
		}
		
		captureRunning = true;
		logger.info("Starting frame capture");
		TimerTask captureTask = createCaptureTimerTask();
		final int period = 1000 / desiredFrameRate;
		final String timerName = String.format("RTPStreamReceiver(%s, period=%dms)", getName(), period);
		timer = new Timer(timerName);
		timer.scheduleAtFixedRate(captureTask, 0, period);
	}

	protected TimerTask createCaptureTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				try {
					if (state != ReceiverState.STARTED) {
						Thread.sleep(100);
					} else {
						if( !listeners.isEmpty()){
							E capturedImage = getImage();
							for (ImageListener<E> listener : listeners) {
								listener.processImage(capturedImage);
							}
						}
					}
				} catch (Exception e) {
					logger.error("Unable to capture frame", e);
				}
			}
		};
	}
	
	@Override
	public void update(ReceiveStreamEvent event) {
		
		if (event instanceof RemotePayloadChangeEvent) {
			logger.error("Received a RemotePayloadChangeEvent, which cannot be handled.");
		}

		else if (event instanceof NewReceiveStreamEvent) {
			try {
				ReceiveStream stream = event.getReceiveStream();
				DataSource ds = stream.getDataSource();

				RTPControl ctl = (RTPControl) ds.getControl(RTPControl.class.getName());
				String format = (ctl != null) ? ctl.getFormat().toString() : "unknown";
				logger.info("Received new RTP stream (format is " + format + ")");
				
				// create a player by passing datasource to the Media Manager
				Player p = Manager.createPlayer(ds);
				if (p == null) {
					logger.error("Could not create a Player for the stream");
					return;
				}
				p.addControllerListener(this);
				p.realize();
			} catch (Exception e) {
				logger.error("Could not handle NewReceiveStreamEvent", e);
			}
		}
	}

	@Override
	public void controllerUpdate(ControllerEvent ce) {
		if (ce instanceof RealizeCompleteEvent) {
			Player p = (Player) ce.getSourceController();
			if (p != null) {
				p.start();
				this.player = p;
				logger.info(getName() + " has completed the connection and is now ready to capture images");
				frameGrabber = (FrameGrabbingControl) player.getControl(FrameGrabbingControl.class.getName());
			}
		}
	}
	
	
	@Override
	public synchronized void stop() {
		if (!captureRunning) {
			return;
		}
		
		logger.info("Stopping frame capture");
		timer.cancel();
		timer = null;
		captureRunning = false;
	}
	
	@Override
	public synchronized void closeConnection() {
		if (state != ReceiverState.STARTED) {
			return;
		}
		
		stop();
		
		frameGrabber = null;
		
		if (player != null) {
			player.close();
			player = null;
		}
		
		rtpManager.removeTargets("Disconnecting from RTP stream");
		rtpManager.dispose();
		rtpManager = null;
		
		state = ReceiverState.STOPPED;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public void setLocal(boolean local) {
		// do nothing
	}
	
	private String displayName;
	
	@Override
	public String getDisplayName() {
		return displayName != null ? displayName : (host + ":" + port);
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
