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

import java.awt.Rectangle;

/**
 * UViewROI Class
 */
public class UViewROI {
	int id;

	Rectangle roiRect = null;

	Rectangle bgRect = null;

	/**
	 * Constructor
	 */
	public UViewROI() {
		id = 0;
		bgRect = new Rectangle(0, 0, 512, 512);
		roiRect = new Rectangle(0, 0, 10, 10);
	}

	/**
	 * @param id
	 * @param rect
	 */
	public UViewROI(int id, Rectangle rect) {
		this.id = id;
		bgRect = new Rectangle(0, 0, 512, 512);
		roiRect = new Rectangle(rect);
	}

	/**
	 * @return id
	 */
	public int getID() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * @return ROI
	 */
	public Rectangle getROI() {
		return roiRect;
	}

	/**
	 * @param rect
	 */
	public void setROI(Rectangle rect) {

		roiRect.setBounds(rect);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setROI(int x, int y, int width, int height) {

		roiRect.setBounds(x, y, width, height);
	}

	/**
	 * @param width
	 * @param height
	 */
	public void setBG(int width, int height) {
		bgRect.setSize(width, height);

	}

}
