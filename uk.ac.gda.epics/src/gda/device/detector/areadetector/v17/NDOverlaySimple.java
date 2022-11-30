/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17;

import gda.device.DeviceException;

/**
 * A simplified interface to the overlay plugin (c.f. {@link NDOverlay}) allowing you to set centre as well as starting position and to set enums with strings.
 *
 * @since GDA 9.12
 */
public interface NDOverlaySimple {

	/**
	 * Get the (requested) name of the overlay
	 *
	 * @return name of the overlay
	 * @throws DeviceException
	 */
	String getName() throws DeviceException;

	/**
	 * Set the overlay name
	 *
	 * @param name
	 *            the name to be set
	 * @throws DeviceException
	 */
	void setName(String name) throws DeviceException;

	/**
	 * Get the readback value for the overlay's name
	 *
	 * @return readback number of overlay name
	 * @throws DeviceException
	 */
	String getNameRbv() throws DeviceException;

	/**
	 * Get the requested value for the "use" setting, that controls whether the overlay is active
	 * <p>
	 * For the simulator, the values are:
	 * <ul>
	 * <li>0 = No</li>
	 * <li>1 = Yes</li>
	 * </ul>
	 *
	 * @return the integer value of the "use" setting
	 * @throws DeviceException
	 */
	short getUse() throws DeviceException;

	/**
	 * Controls whether the overlay is active
	 * <p>
	 *
	 * @param use
	 *            for values, see {@link #getUse()}
	 * @throws DeviceException
	 */
	void setUse(short use) throws DeviceException;

	/**
	 * Get the readback value for the "use" setting (see {@link #getUse()}
	 *
	 * @return the integer value of the "use" setting
	 * @throws DeviceException
	 */
	short getUseRbv() throws DeviceException;

	/**
	 * Get the request value for the shape of the overlay
	 * <p>
	 * For the simulator, the available values are:
	 * <ul>
	 * <li>0 = Cross</li>
	 * <li>1 = Rectangle</li>
	 * <li>2 = Ellipse</li>
	 * <li>3 = Text</li>
	 * </ul>
	 *
	 * @return the integer value of the shape (as listed above)
	 * @throws DeviceException
	 */
	short getShape() throws DeviceException;

	/**
	 * Set the shape of the overlay
	 *
	 * @param shape
	 *            integer value of the shape (see {@link #getShape()})
	 * @throws DeviceException
	 */
	void setShape(short shape) throws DeviceException;

	/**
	 * Get the readback value for the shape of the overlay (see {@link #getShape()})
	 *
	 * @return the integer value of the shape
	 * @throws DeviceException
	 */
	short getShapeRbv() throws DeviceException;

	/**
	 * Get the request value for the drawing mode
	 * <p>
	 * For the simulator, the available values are:
	 * <ul>
	 * <li>0 = Set</li>
	 * <li>1 = XOR</li>
	 * </ul>
	 *
	 * @return the integer value of the drawing mode (as listed above)
	 * @throws DeviceException
	 */
	short getDrawMode() throws DeviceException;

	/**
	 * Set the value of the drawing mode (see {@link #getDrawMode()})
	 *
	 * @param drawMode
	 *            the value to set for the drawing mode
	 * @throws DeviceException
	 */
	void setDrawMode(short drawMode) throws DeviceException;

	/**
	 * Get the readback value of the drawing mode (see {@link #getDrawMode()})
	 *
	 * @return the integer value of the drawing mode
	 * @throws DeviceException
	 */
	short getDrawModeRbv() throws DeviceException;

	/**
	 * Get the request value of the red component of the overlay shape
	 *
	 * @return value of the red component
	 * @throws DeviceException
	 */
	int getRed() throws DeviceException;

	/**
	 * Set the red component of the overlay shape
	 *
	 * @param value
	 *            the value to set for the red component
	 * @throws DeviceException
	 */
	void setRed(int value) throws DeviceException;

	/**
	 * Get the readback value of the red component of the overlay shape
	 *
	 * @return value of the red component
	 * @throws DeviceException
	 */
	int getRedRbv() throws DeviceException;

	/**
	 * Get the request value of the green component of the overlay shape
	 *
	 * @return value of the green component
	 * @throws DeviceException
	 */
	int getGreen() throws DeviceException;

	/**
	 * Set the green component of the overlay shape
	 *
	 * @param value
	 *            the value to set for the green component
	 * @throws DeviceException
	 */
	void setGreen(int value) throws DeviceException;

	/**
	 * Get the readback value of the green component of the overlay shape
	 *
	 * @return value of the green component
	 * @throws DeviceException
	 */
	int getGreenRbv() throws DeviceException;

	/**
	 * Get the request value of the blue component of the overlay shape
	 *
	 * @return value of the blue component
	 * @throws DeviceException
	 */
	int getBlue() throws DeviceException;

	/**
	 * Set the blue component of the overlay shape
	 *
	 * @param value
	 *            the value to set for the blue component
	 * @throws DeviceException
	 */
	void setBlue(int value) throws DeviceException;

	/**
	 * Get the readback value of the blue component of the overlay shape
	 *
	 * @return value of the blue component
	 * @throws DeviceException
	 */
	int getBlueRbv() throws DeviceException;

	/**
	 * Get the request value for the x position of the shape
	 *
	 * @return x position of the shape
	 * @throws DeviceException
	 */
	int getPositionX() throws DeviceException;

	/**
	 * Set the x position of the shape
	 *
	 * @param position
	 *            x position to set
	 * @throws DeviceException
	 */
	void setPositionX(int position) throws DeviceException;

	/**
	 * Get the readback value of the x position of the shape
	 *
	 * @return x position of the shape
	 * @throws DeviceException
	 */
	int getPositionXRbv() throws DeviceException;

	/**
	 * Get the request value for the y position of the shape
	 *
	 * @return y position of the shape
	 * @throws DeviceException
	 */
	int getPositionY() throws DeviceException;

	/**
	 * Set the y position of the shape
	 *
	 * @param position
	 *            y position to set
	 * @throws DeviceException
	 */
	void setPositionY(int position) throws DeviceException;

	/**
	 * Get the readback value of the y position of the shape
	 *
	 * @return y position of the shape
	 * @throws DeviceException
	 */
	int getPositionYRbv() throws DeviceException;

	/**
	 * Get the request value for the position of the centre of the shape on the x axis
	 *
	 * @return position of the centre of the shape on the x axis
	 * @throws DeviceException
	 */
	int getCentreX() throws DeviceException;

	/**
	 * Set the position of the centre of the shape on the x axis
	 *
	 * @param position
	 *            position of the centre of the shape on the x axis
	 * @throws DeviceException
	 */
	void setCentreX(int position) throws DeviceException;

	/**
	 * Get the readback value for the position of the centre of the shape on the x axis
	 *
	 * @return position of the centre of the shape on the x axis
	 * @throws DeviceException
	 */
	int getCentreXRbv() throws DeviceException;

	/**
	 * Get the request value for the position of the centre of the shape on the y axis
	 *
	 * @return position of the centre of the shape on the y axis
	 * @throws DeviceException
	 */
	int getCentreY() throws DeviceException;

	/**
	 * Set the position of the centre of the shape on the y axis
	 *
	 * @param position
	 *            position of the centre of the shape on the y axis
	 * @throws DeviceException
	 */
	void setCentreY(int position) throws DeviceException;

	/**
	 * Get the readback value for the position of the centre of the shape on the y axis
	 *
	 * @return position of the centre of the shape on the y axis
	 * @throws DeviceException
	 */
	int getCentreYRbv() throws DeviceException;

	/**
	 * Get the request value for the size of the shape in the x direction
	 *
	 * @return size of the shape in the x direction
	 * @throws DeviceException
	 */
	int getSizeX() throws DeviceException;

	/**
	 * Set the size of the shape in the x direction
	 *
	 * @param size
	 *            size of the shape in the x direction
	 * @throws DeviceException
	 */
	void setSizeX(int size) throws DeviceException;

	/**
	 * Get the readback value of the size of the shape in the x direction
	 *
	 * @return size of the shape in the x direction
	 * @throws DeviceException
	 */
	int getSizeXRbv() throws DeviceException;

	/**
	 * Get the request value for the size of the shape in the y direction
	 *
	 * @return size of the shape in the y direction
	 * @throws DeviceException
	 */
	int getSizeY() throws DeviceException;

	/**
	 * Set the size of the shape in the y direction
	 *
	 * @param size
	 *            size of the shape in the y direction
	 * @throws DeviceException
	 */
	void setSizeY(int size) throws DeviceException;

	/**
	 * Get the readback value of the size of the shape in the y direction
	 *
	 * @return size of the shape in the y direction
	 * @throws DeviceException
	 */
	int getSizeYRbv() throws DeviceException;
}
