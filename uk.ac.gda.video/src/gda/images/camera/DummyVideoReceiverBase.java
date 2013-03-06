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

import gda.factory.FactoryException;

import java.awt.Dimension;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DummyVideoReceiverBase<T> implements VideoReceiver<T> {

	private static final Logger logger = LoggerFactory.getLogger(DummyVideoReceiverBase.class);
	
	protected int desiredFrameRate = 10;
	
	protected Dimension imageSize = new Dimension(1024, 768);
	
	private Set<ImageListener<T>> listeners = new LinkedHashSet<ImageListener<T>>();
	
	/**
	 * Sets the desired frame rate. (The default is 10fps.)
	 */
	public void setDesiredFrameRate(int desiredFrameRate) {
		this.desiredFrameRate = desiredFrameRate;
	}
	
	public void setImageSize(Dimension imageSize) {
		this.imageSize = imageSize;
	}
	
	private String displayName = "DummyVideoReceiver";
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void addImageListener(ImageListener<T> listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeImageListener(ImageListener<T> listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void configure() throws FactoryException {
		createConnection();
	}
	
	@Override
	public void createConnection() {
		start();
	}
	
	private Timer timer;
	
	@Override
	public synchronized void start() {
		if (timer != null) {
			return;
		}
		
		logger.info("Starting");
		
		createBlankImage();
		circleX = (int) (Math.random() * imageSize.width);
		circleY = (int) (Math.random() * imageSize.height);
		
		TimerTask creationTask = createTimerTask();
		final int period = 1000 / desiredFrameRate;
		final String timerName = String.format("%s(period=%dms)", getClass().getSimpleName(), period);
		timer = new Timer(timerName);
		timer.scheduleAtFixedRate(creationTask, 0, period);
	}
	
	private TimerTask createTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				T image = updateImage();
				try {
					for (ImageListener<T> listener : listeners) {
						listener.processImage(image);
					}
				} catch (Exception e) {
					logger.error("Unable to dispatch image", e);
				}
			}
		};
	}

	@Override
	public synchronized void stop() {
		if (timer == null) {
			return;
		}
		
		logger.info("Stopping");
		
		timer.cancel();
		timer = null;
	}
	
	@Override
	public void closeConnection() {
		stop();
	}
	
	/** Current X position of circle. */
	protected int circleX;
	
	/** Current Y position of circle. */
	protected int circleY;
	
	protected static final int CIRCLE_SIZE = 10;
	
	/** X distance that circle moves from one frame to the next. */
	protected int circleDeltaX = 5;
	
	/** Y distance that circle moves from one frame to the next. */
	protected int circleDeltaY = 5;
	
	/**
	 * Creates the initial image.
	 */
	protected abstract void createBlankImage();
	
	/**
	 * Updates the image. Called at regular intervals.
	 */
	protected abstract T updateImage();
	
}
