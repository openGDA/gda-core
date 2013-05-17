/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
import gda.device.EnumPositioner;
import uk.ac.gda.ui.viewer.IPositionSource;
import uk.ac.gda.ui.viewer.IUnitsDescriptor;

/**
 * An IPositionSource for EnumPositioner Scannables
 */
public class EnumPositionerSource implements IPositionSource<String>, IUnitsDescriptor {

	protected EnumPositioner scannable;
	private String label = null;
	private boolean hideLabel = false;

	public EnumPositionerSource(EnumPositioner scannable) {
		this.scannable = scannable;
	}

	@Override
	public String getPosition() throws DeviceException {
		return (String) scannable.getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return scannable.isBusy();
	}

	@Override
	public void setPosition(final String value) throws DeviceException {
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
		return scannable.getPositions().length;
	}

	@Override
	public double getMinimumLimit() throws DeviceException {
		return 0;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String[] getPositions() throws DeviceException {
		return scannable.getPositions();
	}
}
