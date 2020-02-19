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
import javax.measure.Unit;

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

	@SuppressWarnings("unchecked")
	@Override
	protected <Q extends Quantity<Q>> double[] getLimits(
			Quantity<Q> firstJawMin,
			Quantity<Q> firstJawMax,
			Quantity<Q> secondJawMin,
			Quantity<Q> secondJawMax)
	{
		final Unit<Q> units = (Unit<Q>) unitsComponent.getUserUnit();
		final double minimum = QuantityFactory.createFromObject(firstJawMin.subtract(secondJawMax), units).getValue().doubleValue();
		final double maximum = QuantityFactory.createFromObject(firstJawMax.subtract(secondJawMin), units).getValue().doubleValue();
		return new double[] { minimum > 0. ? minimum : 0., maximum };
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object rawGetPosition() throws DeviceException {
		// return position as a double
		return ((Quantity) getCurrentGap()).to(unitsComponent.getUserUnit()).getValue().doubleValue();
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

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends Quantity<T>> Quantity<T>[] calculateTargets(Object position) throws DeviceException {
		// convert what is supplied to Quantity in user units
		final Quantity<T> target = QuantityFactory.createFromObjectUnknownUnit(position, unitsComponent.getUserUnit());
		final Quantity<T>[] targets = new Quantity[2];

		if (xmlparametersfilename != null) {
			// perform calculation based on the beam centre in the xml file
			try {
				//first check the file exists then load using the New version to ensure any changes outside the VM are picked up
				LocalParameters.getXMLConfiguration(xmlparametersfilename, false);
				final FileConfiguration config = LocalParameters.getNewXMLConfiguration(xmlparametersfilename);
				final double xmlCentreDbl = config.getDouble(getName());
				final Quantity<T> xmlCentre = QuantityFactory.createFromObjectUnknownUnit(xmlCentreDbl, unitsComponent.getUserUnit());
				targets[0] = xmlCentre.add(target.divide(2.0));
				targets[1] = xmlCentre.subtract(target.divide(2.0));
			} catch (Exception e) {
				throw new DeviceException("Error calculating targets for " + position, e);
			}
			//
		} else {
			// perform the calculation based on the current beam centre i.e. current motor positions
			final Quantity<T> currentGap = getCurrentGap();
			final Quantity<T> delta = target.subtract(currentGap).divide(2);
			final Quantity<T> firstJawPosition = QuantityFactory.createFromObject(firstJaw.getPosition(), firstJaw.getUserUnits());
			final Quantity<T> secondJawPosition = QuantityFactory.createFromObject(secondJaw.getPosition(), firstJaw.getUserUnits());

			targets[0] = firstJawPosition.add(delta);
			targets[1] = secondJawPosition.subtract(delta);
		}
		return targets;
	}
}
