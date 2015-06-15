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

package gda.org.myls.scannable;

/**
 *
 */
public class Gaussian {
	String name="aGaussian";
	double centre, width, height, noise;

	/**
	 *
	 */
	public Gaussian() {
	}

	/**
	 * @param centre
	 * @param width
	 * @param height
	 * @param noise
	 */
	public Gaussian(double centre, double width, double height, double noise) {
		this.setCentre(centre);
		this.setWidth(width);
		this.setHeight(height);
		this.setNoise(noise);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * @return Returns the centre.
	 */
	public double getCentre() {
		return centre;
	}

	/**
	 * @param centre The centre to set.
	 */
	public void setCentre(double centre) {
		this.centre = centre;
	}

	/**
	 * @return Returns the width.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param width The width to set.
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * @return Returns the height.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height The height to set.
	 */
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * @return Returns the noise.
	 */
	public double getNoise() {
		return noise;
	}

	/**
	 * @param noise The noise to set.
	 */
	public void setNoise(double noise) {
		this.noise = noise;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

}
