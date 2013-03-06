/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.factory.FactoryException;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import static java.lang.Math.sin;
import static java.lang.Math.abs;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

public class DummyOavAwtVideoReceiver implements VideoReceiver<Image> {

	private Set<ImageListener<Image>> listeners = new LinkedHashSet<ImageListener<Image>>();
	
	@Override
	public void addImageListener(ImageListener<Image> listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeImageListener(ImageListener<Image> listener) {
		listeners.remove(listener);
	}
	
	private int desiredFrameRate = 15;
	
	private Dimension imageSize = new Dimension(640, 480);
	
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
		
		image = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_RGB);
		
		TimerTask creationTask = createTimerTask();
		final int period = 1000 / desiredFrameRate;
		final String timerName = String.format("%s(period=%dms)", getClass().getSimpleName(), period);
		timer = new Timer(timerName);
		timer.scheduleAtFixedRate(creationTask, 0, period);
	}
	private BufferedImage image;
	
	private TimerTask createTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				updateImage();
				for (ImageListener<Image> listener : listeners) {
					listener.processImage(image);
				}
			}
		};
	}
	
	private static final Color LOOP_FILL_COLOUR = new Color(0xdd, 0xdd, 0xdd);
	
	private static final Dimension SIZE_OF_LOOP_IN_MICRONS = new Dimension(150, 50);
	
	private static final int WIDTH_OF_PIN_IN_MICRONS = 20;
	
	private int zoomLevel = 1;
	
	private double omega;
	
	private double gtabx;
	private double gtaby;
	private double gtabz;
	
	private double gonioy;
	private double gonioz;
	
	private double micronsPerXPixel = 2.186;
	private double micronsPerYPixel = 2.114;
	
	private void updateImage() {
		final Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		
		Point3d offsetInMicrons = new Point3d(
			gtabx * 1000,
			gtaby * 1000 + gonioy,
			gtabz * 1000 + gonioz
		);
		Point2d offsetInPixels = new Point2d(
			 (offsetInMicrons.x / micronsPerXPixel) * zoomLevel,
			-(offsetInMicrons.y / micronsPerYPixel) * zoomLevel
		);
		
		g.setColor(Color.BLACK);
		
		Point imageCentre = new Point(
			image.getWidth() / 2,
			image.getHeight() / 2
		);
		
//		drawCrosshair(g, imageCentre, 20);
		
		Point centre = new Point(
			imageCentre.x + (int) offsetInPixels.x,
			imageCentre.y + (int) offsetInPixels.y);
		
		int widthOfPinInPixels = (int) (WIDTH_OF_PIN_IN_MICRONS / micronsPerYPixel * zoomLevel);
		
		Dimension sizeOfLoopInPixels = new Dimension(
			(int) (SIZE_OF_LOOP_IN_MICRONS.width  / micronsPerXPixel * zoomLevel),
			(int) (SIZE_OF_LOOP_IN_MICRONS.height / micronsPerYPixel * zoomLevel * abs(sin(Math.toRadians(90 + omega))))
		);
		if (sizeOfLoopInPixels.height < 1) {
			sizeOfLoopInPixels.height = 1;
		}
		
		// draw pin
		g.fillRect(0, centre.y - widthOfPinInPixels / 2, centre.x, widthOfPinInPixels);
		
		// draw loop
		g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(LOOP_FILL_COLOUR);
		g.fillOval(centre.x, centre.y - sizeOfLoopInPixels.height/2 , sizeOfLoopInPixels.width, sizeOfLoopInPixels.height);
		g.setColor(Color.BLACK);
		g.drawOval(centre.x, centre.y - sizeOfLoopInPixels.height/2 , sizeOfLoopInPixels.width, sizeOfLoopInPixels.height);
	}
	
	@SuppressWarnings("unused")
	private static void drawCrosshair(Graphics2D g, Point centre, int size) {
		int left   = centre.x - size;
		int right  = centre.x + size;
		int top    = centre.y + size;
		int bottom = centre.y - size;
		g.drawLine(left, bottom, right, top);
		g.drawLine(left, top, right, bottom);
	}
	
	@Override
	public void stop() {
		if (timer == null) {
			return;
		}
		
		timer.cancel();
		timer = null;
	}
	
	@Override
	public Image getImage() throws DeviceException {
		return image;
	}
	
	@Override
	public void closeConnection() {
		stop();
	}
	
	@Override
	public String getDisplayName() {
		return getClass().getSimpleName();
	}
	
	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}
	
	public void setGtabx(double gtabx) {
		this.gtabx = gtabx;
	}
	
	public void setGtaby(double gtaby) {
		this.gtaby = gtaby;
	}
	
	public void setGtabz(double gtabz) {
		this.gtabz = gtabz;
	}
	
	public void setGonioy(double gonioy) {
		this.gonioy = gonioy;
	}
	
	public void setGonioz(double gonioz) {
		this.gonioz = gonioz;
	}
	
	public void setOmega(double omega) {
		this.omega = omega;
	}

}
