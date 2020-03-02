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

/**
 *
 */
package gda.device.scannable;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.apache.commons.configuration.FileConfiguration;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.util.QuantityFactory;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * The gap of a two jaw slit.
 * <p>
 * Based on TwoJawSlitPosition.
 */
@ServiceInterface(ScannableMotionUnits.class)
public class TwoJawSlitGap extends TwoJawSlitPosition {

	private String xmlparametersfilename = null;

	@Override
	protected double[] getLimits(
			Quantity<Length> firstJawMin,
			Quantity<Length> firstJawMax,
			Quantity<Length> secondJawMin,
			Quantity<Length> secondJawMax)
	{
		final double minimum = firstJawMin.subtract(secondJawMax).to(userUnitFromUnitsComponent()).getValue().doubleValue();
		final double maximum = firstJawMax.subtract(secondJawMin).to(userUnitFromUnitsComponent()).getValue().doubleValue();
		return new double[] { minimum > 0. ? minimum : 0., maximum };
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// return position as a double
		return getCurrentGap().to(userUnitFromUnitsComponent()).getValue().doubleValue();
	}

	/**
	 * This string is the name of the xml parameters file which is used to store the beam centre used by these slits.
	 * If this value is set to a non-null value then the behaviour of this scannable is changed to move the gap based
	 * on the beam centre in this xml file rather than use the current beam centre. This is useful for motors which
	 * drift or frequently fail.
	 *
	 * @param xmlparametersfilename
	 *            The xmlparametersfilename to set.
	 */
	public void setXmlparametersfilename(String xmlparametersfilename) {
		this.xmlparametersfilename = xmlparametersfilename;
	}

	/**
	 * @return Returns the xmlparametersfilename.
	 */
	public String getXmlparametersfilename() {
		return xmlparametersfilename;
	}

	@Override
	protected Quantity<Length>[] calculateTargets(Object position) throws DeviceException {
		// convert what is supplied to Quantity in user units
		final Quantity<Length> target = QuantityFactory.createFromObject(position, userUnitFromUnitsComponent());
		@SuppressWarnings("unchecked")
		final Quantity<Length>[] targets = new Quantity[2];

		if (xmlparametersfilename != null) {
			// perform calculation based on the beam centre in the xml file
			try {
				//first check the file exists then load using the New version to ensure any changes outside the VM are picked up
				LocalParameters.getXMLConfiguration(xmlparametersfilename, false);
				final FileConfiguration config = LocalParameters.getNewXMLConfiguration(xmlparametersfilename);
				final double xmlCentreDbl = config.getDouble(getName());
				final Quantity<Length> xmlCentre = QuantityFactory.createFromObject(xmlCentreDbl, userUnitFromUnitsComponent());
				targets[0] = xmlCentre.add(target.divide(2.0));
				targets[1] = xmlCentre.subtract(target.divide(2.0));
			} catch (Exception e) {
				throw new DeviceException("Error calculating targets for " + position, e);
			}
			//
		} else {
			// perform the calculation based on the current beam centre i.e. current motor positions
			final Quantity<Length> currentGap = getCurrentGap();
			final Quantity<Length> delta = target.subtract(currentGap).divide(2);
			final Quantity<Length> firstJawPosition = QuantityFactory.createFromObject(firstJaw.getPosition(), firstJaw.getUserUnits());
			final Quantity<Length> secondJawPosition = QuantityFactory.createFromObject(secondJaw.getPosition(), firstJaw.getUserUnits());

			targets[0] = firstJawPosition.add(delta);
			targets[1] = secondJawPosition.subtract(delta);
		}
		return targets;
	}
}
