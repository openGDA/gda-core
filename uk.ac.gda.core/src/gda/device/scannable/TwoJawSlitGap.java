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

import org.apache.commons.configuration.FileConfiguration;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

import gda.device.DeviceException;
import gda.util.QuantityFactory;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

/**
 * The gap of a two jaw slit.
 * <p>
 * Based on TwoJawSlitPosition.
 */
public class TwoJawSlitGap extends TwoJawSlitPosition {

	private String xmlparametersfilename = null;

	@Override
	protected double[] getLimits(
			Quantity firstJawMin,
			Quantity firstJawMax,
			Quantity secondJawMin,
			Quantity secondJawMax)
	{
		Unit<?> units = unitsComponent.getUserUnit();
		double minimum = QuantityFactory.createFromObject(firstJawMin.minus(secondJawMax),units).getAmount();
		double maximum = QuantityFactory.createFromObject(firstJawMax.minus(secondJawMin),units).getAmount();
		return new double[]{ minimum > 0. ? minimum : 0., maximum};
	}


	@Override
	public Object rawGetPosition() throws DeviceException {

		// return position as a double
		return unitsComponent.convertObjectToUserUnitsAssumeUserUnits(getCurrentGap());
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
	protected Quantity[] calculateTargets(Object position) throws DeviceException {
		// convert what is supplied to Quantity in user units
		Quantity target = QuantityFactory.createFromObject(position, this.unitsComponent.getUserUnit());
		Quantity[] targets = new Quantity[2];

		if (xmlparametersfilename != null) {
			// perform calculation based on the beam centre in the xml file
			try {
				//first check the file exists then load using the New version to ensure any changes outside the VM are picked up
				LocalParameters.getXMLConfiguration(xmlparametersfilename, false);
				FileConfiguration config = LocalParameters.getNewXMLConfiguration(xmlparametersfilename);
				double xmlCentreDbl = config.getDouble(getName());
				Quantity xmlCentre = QuantityFactory.createFromObject(xmlCentreDbl, this.unitsComponent.getUserUnit());
				targets[0] = xmlCentre.plus(target.divide(2.0));
				targets[1] = xmlCentre.minus(target.divide(2.0));
			} catch (Exception e) {
				throw new DeviceException("Error calculating targets for " + position, e);
			}
			//
		} else {
			// perform the calculation based on the current beam centre i.e. current motor positions
			Quantity currentGap = getCurrentGap();
			Quantity delta = target.minus(currentGap).divide(2);
			Quantity firstJawPosition = QuantityFactory.createFromObject(firstJaw.getPosition(), QuantityFactory
					.createUnitFromString(this.firstJaw.getUserUnits()));
			Quantity secondJawPosition = QuantityFactory.createFromObject(secondJaw.getPosition(), QuantityFactory
					.createUnitFromString(this.firstJaw.getUserUnits()));

			targets[0] = firstJawPosition.plus(delta);
			targets[1] = secondJawPosition.minus(delta);
		}
		return targets;
	}
}
