/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.epics.DummyPV;
import gda.epics.PV;

public class DummyADDriverPco implements ADDriverPco {

	private PV<Boolean> pvBoolean = new DummyPV<Boolean>("pvBoolean", true);
	private PV<Double> pvDouble = new DummyPV<Double>("pvDouble", 0.45);
	private PV<Integer> pvInteger = new DummyPV<Integer>("pvInteger", 23);

	@Override
	public PV<Boolean> getArmModePV() {
		return pvBoolean;
	}

	@Override
	public PV<Double> getCameraUsagePV() {
		return pvDouble;
	}

	@Override
	public PV<Integer> getAdcModePV() {
		return pvInteger;
	}

	@Override
	public PV<Integer> getTimeStampModePV() {
		return pvInteger;
	}

	@Override
	public PV<Integer> getBinXPV() {
		return pvInteger;
	}

	@Override
	public PV<Integer> getBinYPV() {
		return pvInteger;
	}

	@Override
	public PV<Integer> getMinXPV() {
		return pvInteger;
	}

	@Override
	public PV<Integer> getSizeXPV() {
		return pvInteger;
	}

	@Override
	public PV<Integer> getMinYPV() {
		return pvInteger;
	}

	@Override
	public PV<Integer> getSizeYPV() {
		return pvInteger;
	}

}
