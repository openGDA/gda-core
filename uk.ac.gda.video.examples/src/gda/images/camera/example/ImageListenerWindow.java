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

package gda.images.camera.example;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.images.camera.ImageListener;
import gda.images.camera.VideoReceiver;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Window that receives video from a {@link VideoReceiver} and updates the display at (approximately) the desired
 * frame rate.
 */
public class ImageListenerWindow implements Configurable, ImageListener<Image>, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageListenerWindow.class);
	
	public ImageListenerWindow() {
		frame = new JFrame();
		frame.setSize(640, 480);
		
		label = new JLabel();
		frame.add(label);
		
		desiredFrameRate = 10;
	}
	
	private JFrame frame;
	
	private JLabel label;
	
	protected String name;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	protected VideoReceiver<Image> videoReceiver;
	
	public void setVideoReceiver(VideoReceiver<Image> videoReceiver) {
		this.videoReceiver = videoReceiver;
	}
	
	protected int desiredFrameRate;
	
	public void setDesiredFrameRate(int desiredFrameRate) {
		this.desiredFrameRate = desiredFrameRate;
	}
	
	public void setLocation(Point p) {
		frame.setLocation(p);
	}
	
	public void setSize(Dimension d) {
		frame.setSize(d);
	}
	
	public void setSize(int width, int height) {
		frame.setSize(width, height);
	}
	
	public void setVisible(boolean visible) {
		frame.setVisible(visible);
	}
	
	@Override
	public void configure() throws FactoryException {
		videoReceiver.addImageListener(this);
	}
	
	private int imagesReceived;
	
	private boolean timingEnabled;
	
	private long startTime;
	
	protected volatile Image latestImage;
	
	@Override
	public void processImage(Image image) {
		if (!timingEnabled) {
			imagesReceived++;
			if (imagesReceived == 100) {
				imagesReceived = 0;
				startTime = System.currentTimeMillis();
				timingEnabled = true;
				logger.debug("Started timing");
			}
		} else {
			imagesReceived++;
			if ((imagesReceived % 50) == 0) {
				long currentTime = System.currentTimeMillis();
				double seconds = (currentTime - startTime) / 1000.0;
				logger.debug(String.format("Received %d images in %.2f seconds; %.2f fps", imagesReceived, seconds, imagesReceived / seconds));
			}
		}
		latestImage = image;
	}
	
	@Override
	public void run() {
		int sleepTime = 1000 / desiredFrameRate;
		frame.setTitle(String.format("desired frame rate = %dfps (sleep time is %dms)", desiredFrameRate, sleepTime));
		while (true) {
			if (latestImage != null) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						label.setIcon(new ImageIcon(latestImage));
					}
				});
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}
	
}
