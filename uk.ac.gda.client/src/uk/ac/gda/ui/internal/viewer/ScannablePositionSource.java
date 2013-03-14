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
import gda.device.Scannable;
import uk.ac.gda.ui.viewer.IPositionSource;
import uk.ac.gda.ui.viewer.IUnitsDescriptor;

public class ScannablePositionSource implements IPositionSource, IUnitsDescriptor{
	private String label=null;
	
	private boolean hideLabel = false;
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	protected Scannable scannable; 
	
	public ScannablePositionSource(Scannable scannable){
		this.scannable = scannable;
	}
	@Override
	public double getPosition() throws DeviceException {
		return (Double)scannable.getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return scannable.isBusy();
	}


	@Override
	public void setPosition(final double value) throws DeviceException {
		scannable.moveTo(value);
	}
	

	@Override
	public IUnitsDescriptor getDescriptor() {
		return this;
	}

	@Override
	public String getDisplayFormat() {
		return scannable.getOutputFormat()[0];
	}

	@Override
	public String getLabelText() {
		return label != null ? label : scannable.getName();
	}

	@Override
	public double getMaximumLimit() throws DeviceException {
		return Double.MAX_VALUE;
	}

	@Override
	public double getMinimumLimit() throws DeviceException {
		return -Double.MAX_VALUE;
	}

	@Override
	public String getToolTipText() {
		return scannable.getName();
	}

	@Override
	public String getUnit() {
		return "";
	}

	public void setHideLabel(boolean hideLabel) {
		this.hideLabel = hideLabel;
	}
	@Override
	public boolean getHideLabel() {
		return hideLabel;
	}	
	
}
