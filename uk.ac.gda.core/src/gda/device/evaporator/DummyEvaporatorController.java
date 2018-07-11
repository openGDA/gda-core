/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.evaporator;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Simulated version of an {@link EvaporatorController} for use in testing and
 * dummy mode.
 */
public class DummyEvaporatorController implements EvaporatorController {

	private boolean enabled;
	private boolean remote;
	private double highVoltage;
	private boolean highVoltageEnabled;
	private int shutter;
	private String[] availableShutters = new String[] {"All Open", "All Closed"};
	private List<EvaporatorPocket> pockets;

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	@Override
	public boolean isRemote() {
		return remote;
	}

	@Override
	public void setHighVoltage(double hv) {
		highVoltage = hv;
	}

	@Override
	public double getHighVoltage() {
		return highVoltage;
	}

	@Override
	public void setHighVoltageEnabled(boolean enable) {
		highVoltageEnabled = enable;
	}

	@Override
	public boolean isHighVoltageEnabled() {
		return highVoltageEnabled;
	}

	@Override
	public void setShutter(String shutterPosition) {
		int found = Arrays.binarySearch(availableShutters, shutterPosition);
		if (found >= 0) {
			shutter = found;
		} else {
			throw new IllegalArgumentException("Shutter position is not available");
		}
	}

	@Override
	public String getShutter() {
		return availableShutters[shutter];
	}

	@Override
	public String[] getShutterPositions() {
		return availableShutters.clone();
	}

	@Override
	public int getNumberOfPockets() {
		return pockets.size();
	}

	@Override
	public EvaporatorPocket getPocket(int pocket) {
		return pockets.get(pocket);
	}

	public void setPockets(Collection<EvaporatorPocket> pockets) {
		requireNonNull(pockets, "Pockets must not be null");
		this.pockets = new ArrayList<>(pockets);
	}

	public void setAvailableShutters(Collection<String> shutters) {
		availableShutters = shutters.toArray(new String[] {});
	}

	@Override
	public void clearError() {

	}
}
