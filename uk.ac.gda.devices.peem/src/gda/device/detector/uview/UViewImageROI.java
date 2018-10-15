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

import java.awt.Color;
import java.awt.Rectangle;

/**
 * UViewImageROI Class
 */
public class UViewImageROI implements java.io.Serializable {

	private static final long serialVersionUID = 3333900291815094339L;

	int id;

	Rectangle roiRect = null;

	Color boundColor = Color.RED;

	/**
	 * Constructor
	 */
	public UViewImageROI() {
		id = 0;
		roiRect = new Rectangle(0, 0, 10, 10);
	}

	/**
	 * @param id
	 * @param rect
	 */
	public UViewImageROI(int id, Rectangle rect) {
		this.id = id;
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
	 * @return boundary colour
	 */
	public Color getBoundaryColor() {
		return boundColor;
	}

	/**
	 * @param color
	 */
	public void setBoundaryColor(Color color) {
		boundColor = color;
	}

	/**
	 * @return ROI rectangle
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

}
