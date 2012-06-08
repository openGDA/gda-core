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

package uk.ac.gda.ui.internal.viewer;

import gda.device.DeviceException;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import uk.ac.gda.ui.viewer.IUnitsDescriptor;

public class ScannableMotionUnitsPositionSource extends ScannablePositionSource  {

	private ScannableMotionUnits scannableMotionUnits; 
	
	public ScannableMotionUnitsPositionSource(ScannableMotionUnits scannable){
		super(scannable);
		this.scannableMotionUnits = scannable;
	}

	@Override
	public IUnitsDescriptor getDescriptor() {
		return this;
	}

	@Override
	public double getMaximumLimit() throws DeviceException {
		Double[] limits = null;
		try {
			//TODO move getInputLimits from ScannableMotionBase to ScannableMotion interface
			limits = (Double[]) scannableMotionUnits.getAttribute(ScannableMotion.FIRSTINPUTLIMITS);
		} catch (DeviceException e) {
			throw new DeviceException("Problem getting limits from motor:", e);
		}
		return (limits == null || limits[1] == null) ? Double.MAX_VALUE : limits[1];
	}

	@Override
	public double getMinimumLimit() throws DeviceException {
		Double[] limits = null;
		try {
			limits = (Double[]) scannableMotionUnits.getAttribute(ScannableMotion.FIRSTINPUTLIMITS);
		} catch (DeviceException e) {
			throw new DeviceException("Problem getting limits from motor:", e);
		}
		return (limits == null || limits[0] == null) ? -Double.MAX_VALUE : limits[0];
	}

	@Override
	public String getUnit() {
		return scannableMotionUnits.getUserUnits();
	}
	

}
