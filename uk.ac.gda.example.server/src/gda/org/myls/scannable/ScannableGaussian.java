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

import gda.device.DeviceException;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

/**
 *
 */
@CorbaImplClass(ScannableImpl.class)
@CorbaAdapterClass(ScannableAdapter.class)
public class ScannableGaussian extends SimpleScannable {
	double centre, width, height, noise;
	Gaussian gaussian;
	//

	/**
	 *
	 */
	public ScannableGaussian() {
		this(new Gaussian(0.0, 1.0, 1.0, 0.0));
	}

	/**
	 * @param g
	 */
	public ScannableGaussian(final Gaussian g) {
		this(g.centre, g.width, g.height, g.noise);
		this.setGaussian(gaussian);
	}

	/**
	 * @param centre
	 * @param width
	 * @param height
	 * @param noise
	 */
	public ScannableGaussian(final double centre, final double width, final double height, final double noise) {
		super("gaussian", 0.0, new String[] { "x" }, new String[] { "y" }, 3,
				new String[] { "%.4f", "%.4f" }, new String[] { "mm", "counts" });
		this.setCentre(centre);
		this.setWidth(width);
		this.setHeight(height);
		this.setNoise(noise);
		// this("gaussian", 0.0, "x", "y", centre, width, height, noise, 3, "%.4f", "%.4f", "mm", "counts");
	}

	/**
	 * @param name
	 * @param xPosition
	 * @param xName
	 * @param yName
	 * @param centre
	 * @param width
	 * @param height
	 * @param noise
	 * @param level
	 * @param outputFormatx
	 * @param outputFormaty
	 * @param unitsx
	 * @param unitsy
	 */
	public ScannableGaussian(final String name, final Double xPosition, final String xName,
			final String yName, final Double centre, final Double width, final Double height,
			final Double noise, final int level, final String outputFormatx,
			final String outputFormaty, final String unitsx, final String unitsy) {
		super(name, xPosition, new String[] { xName }, new String[] {yName},
				level, new String[] {outputFormatx, outputFormaty},
				new String[] {unitsx, unitsy});
		this.setCentre(centre);
		this.setWidth(width);
		this.setHeight(height);
		this.setNoise(noise);
	}

	@Override
	public final Object getPosition() throws DeviceException {
		Double x = ScannableUtils.objectToArray(super.getPosition())[0];
		double x2 = x - centre;
		double sigma = 0.425 * width;
		// Double noiseVal = 0.01 * noise * magnitude * Math.random();
		double noiseVal = height * (Math.random() - 0.5) * noise;
		Double y = Math.exp(-(x2 * x2) / (sigma * sigma)) + noiseVal;
		return new Double[] { x, y };
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
	}

	/**
	 * @return Returns the centre.
	 */
	public final double getCentre() {
		return centre;
	}

	/**
	 * @param centre The centre to set.
	 */
	public final void setCentre(final double centre) {
		this.centre = centre;
	}

	/**
	 * @return Returns the width.
	 */
	public final double getWidth() {
		return width;
	}

	/**
	 * @param width The width to set.
	 */
	public final void setWidth(final double width) {
		this.width = width;
	}

	/**
	 * @return Returns the height.
	 */
	public final double getHeight() {
		return height;
	}

	/**
	 * @param height The height to set.
	 */
	public final void setHeight(final double height) {
		this.height = height;
	}

	/**
	 * @return Returns the noise.
	 */
	public final double getNoise() {
		return noise;
	}

	/**
	 * @param noise The noise to set.
	 */
	public final void setNoise(final double noise) {
		this.noise = noise;
	}

	/**
	 * @return Returns the gaussian.
	 */
	public final Gaussian getGaussian() {
		return gaussian;
	}

	/**
	 * @param gaussian The gaussian to set.
	 */
	public final void setGaussian(final Gaussian gaussian) {
		this.gaussian = gaussian;
	}

}
