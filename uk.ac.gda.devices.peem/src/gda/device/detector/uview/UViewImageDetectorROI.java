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

package gda.device.detector.uview;

import gda.device.DeviceException;
import gda.device.UViewROI;
import gda.device.detector.DetectorBase;
import gda.factory.FactoryException;
import gda.factory.Finder;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UViewImageDetectorROI Class
 */

public class UViewImageDetectorROI extends DetectorBase implements UViewROI {

	private static final Logger logger = LoggerFactory.getLogger(UViewImageDetectorROI.class);

	boolean isConfigured = false;

	String baseDetector = null;

	String name = null;

	String boundaryColor = null;

	private UViewImageDetector uvid = null;

	Rectangle bgRect = new Rectangle(0, 0, 512, 512);

	/**
	 * Constructor
	 */
	public UViewImageDetectorROI() {
	}

	@Override
	public void configure() throws FactoryException {
		Finder finder = Finder.getInstance();
		uvid = (UViewImageDetector) finder.find(baseDetector);
		if (uvid != null) {
			logger.debug("ROI finds UViewImageDetector!");

			uvid.createROI(name);
			UViewImageDetector.hashROIs.get(name).setBoundaryColor(StringToColor(boundaryColor));

			isConfigured = true;
			logger.info("UViewImageDetector found. ROI enabled");
		} else {
			logger.error("Could not find UViewImageDetector. ROI disabled");
		}

	}

	@Override
	public void collectData() throws DeviceException {
		uvid.collectData();
	}

	@Override
	public int getStatus() throws DeviceException {
		return uvid.getStatus();
	}

	@Override
	public Object readout() throws DeviceException {
		return uvid.readoutROI(name);
	}

	@Override
	public void setCollectionTime(double collectionTime) {

		uvid.setCollectionTime(collectionTime);
	}

	@Override
	public double getCollectionTime() {
		return uvid.getCollectionTime();

	}

	// method for UViewROI interface
	@Override
	public void setBounds(int x, int y, int width, int height) throws IOException {

		// uvid.setBoundsROI(name, x, y, width, height);

		Rectangle newBoundary = this.validateBoundary(x, y, width, height);
		newBoundary = this.convertBoundary(newBoundary);
		uvid.setBoundsROI(name, newBoundary.x, newBoundary.y, newBoundary.width, newBoundary.height);
	}

	@Override
	public void setLocation(int x, int y) throws IOException {

		Rectangle rect = getCustomiseBounds();

		Rectangle newBoundary = this.validateBoundary(x, y, rect.width, rect.height);
		newBoundary = this.convertBoundary(newBoundary);

		// uvid.setBoundsROI(name, x, y, rect.width, rect.height);
		uvid.setBoundsROI(name, newBoundary.x, newBoundary.y, newBoundary.width, newBoundary.height);
	}

	@Override
	public void setSize(int width, int height) throws IOException {

		Rectangle rect = getCustomiseBounds();
		Rectangle newBoundary = this.validateBoundary(rect.x, rect.y, width, height);
		newBoundary = this.convertBoundary(newBoundary);

		// uvid.setBoundsROI(name, rect.x, rect.y, width, height);
		uvid.setBoundsROI(name, newBoundary.x, newBoundary.y, newBoundary.width, newBoundary.height);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return base detector
	 */
	public String getBaseDetector() {
		return baseDetector;
	}

	/**
	 * @param detectorName
	 */
	public void setBaseDetector(String detectorName) {
		this.baseDetector = detectorName;
	}

	/**
	 * @return boundary colour
	 */
	public String getBoundaryColor() {
		return boundaryColor;
	}

	/**
	 * @param boundaryColor
	 */
	public void setBoundaryColor(String boundaryColor) {
		this.boundaryColor = boundaryColor;
	}

	private Color StringToColor(String strColor) {
		Color c = Color.white;

		if (strColor.equalsIgnoreCase("black"))
			c = Color.BLACK;
		else if (strColor.equalsIgnoreCase("blue"))
			c = Color.BLUE;
		else if (strColor.equalsIgnoreCase("cyan"))
			c = Color.CYAN;
		else if (strColor.equalsIgnoreCase("gray"))
			c = Color.GRAY;
		else if (strColor.equalsIgnoreCase("green"))
			c = Color.GREEN;
		else if (strColor.equalsIgnoreCase("magenta"))
			c = Color.MAGENTA;
		else if (strColor.equalsIgnoreCase("orange"))
			c = Color.ORANGE;
		else if (strColor.equalsIgnoreCase("pink"))
			c = Color.PINK;
		else if (strColor.equalsIgnoreCase("red"))
			c = Color.RED;
		else if (strColor.equalsIgnoreCase("white"))
			c = Color.WHITE;
		else if (strColor.equalsIgnoreCase("yellow"))
			c = Color.YELLOW;
		else if (strColor.equalsIgnoreCase("darkGray"))
			c = Color.DARK_GRAY;
		else if (strColor.equalsIgnoreCase("lightGray"))
			c = Color.LIGHT_GRAY;
		else {
			System.out.println("Wrong Color definition, use default WHITE");
		}

		return c;
	}

	// Convert the user required coordination system to standard Up-Left
	// Corner
	// pixel coordination
	private Rectangle convertBoundary(Rectangle rect) {
		int newWidth = rect.width;
		int newHeight = rect.height;
		int newX = rect.x;
		int newY = bgRect.height - rect.y - rect.height;

		Rectangle internalRect = new Rectangle(newX, newY, newWidth, newHeight);

		System.out.println("New Converted Internal rect is: " + internalRect.toString());

		return internalRect;
	}

	/**
	 * Get the user required coordination system from the standard internal Up-Left Corner pixel coordination
	 * 
	 * @return the user required coordination system from the standard internal Up-Left Corner pixel coordination
	 */
	public Rectangle getCustomiseBounds() {
		Rectangle internalRectr = this.getBounds();
		int newWidth = internalRectr.width;
		int newHeight = internalRectr.height;
		int newX = internalRectr.x;
		int newY = bgRect.height - internalRectr.y - internalRectr.height;

		Rectangle customRect = new Rectangle(newX, newY, newWidth, newHeight);

		System.out.println("The User oriented rect is: " + customRect.toString());

		return customRect;
	}

	/**
	 * Return a un-customised ROI Boundary, which is used internally.
	 * 
	 * @return a un-customised ROI Boundary, which is used internally.
	 */
	public Rectangle getBounds() {
		try {
			return (Rectangle) uvid.getBoundsROI(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Rectangle validateBoundary(Rectangle rect) {

		int minWidth = 10, minHeight = 10; // minium ROI is 10*10 pixels

		// make sure it is not too small
		int newWidth = Math.max(minWidth, rect.width);
		int newHeight = Math.max(minHeight, rect.height);

		// make sure it is not too big
		newWidth = Math.min(newWidth, bgRect.width - 1);
		newHeight = Math.min(newHeight, bgRect.height - 2);

		// make sure it is a square
		newWidth = Math.min(newWidth, newHeight);
		newHeight = Math.min(newWidth, newHeight);

		// make sure it is not outside the background boundary
		// 1 <= newX <= bgRect.width - newWidth
		int newX = Math.min(rect.x, bgRect.width - newWidth - 1);
		int newY = Math.min(rect.y, bgRect.height - newHeight);
		newX = Math.max(1, newX);
		newY = Math.max(0, newY);

		Rectangle validRect = new Rectangle(newX, newY, newWidth, newHeight);

		System.out.println("New validated rect is: " + validRect.toString());

		return validRect;

	}

	private Rectangle validateBoundary(int x, int y, int width, int height) {

		return this.validateBoundary(new Rectangle(x, y, width, height));

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "UView Image Detector ROI";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "unknown";
	}

}
