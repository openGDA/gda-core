/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ACQUIRE_TIME_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ARRAY_COUNTER;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ARRAY_COUNTER_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.CONNECT;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.DETECTOR_STATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.DET_CONNECTED;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.DISCONNECT;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.DTC_ENERGY_KEV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA5_WINDOW_HIGH;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA5_WINDOW_HIGH_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA5_WINDOW_LOW;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA5_WINDOW_LOW_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA6_WINDOW_HIGH;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA6_WINDOW_HIGH_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA6_WINDOW_LOW;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA6_WINDOW_LOW_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.TRIGGER_MODE_TEMPLATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.ACQUIRE_STATE;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.XSPRESS3_EPICS_STATUS;

public class XspressCamControl extends XspressPvProviderBase {
	// PV to set the exposure time (used for Software triggered collection)
	protected PV<Double> pvAcquireTime = null;

	// PV to start/stop the detector
	protected PV<ACQUIRE_STATE> pvAcquire = null;

	// PV to set the number of images to be captured.
	protected PV<Integer> pvNumImages = null;

	// Readback value of the number of images to be captured
	protected ReadOnlyPV<Integer> pvNumImagesRbv = null;

	protected PV<Integer> pvTriggerMode = null;

	// Number of captured images
	protected PV<Integer> pvArrayCounter = null;

	protected ReadOnlyPV<Integer> pvArrayCounterRbv = null;

	protected PV<Double> pvDtcEnergyKev = null;

	/** Pvs for setting the scaler window limits (one PV per channel of the detector) */
	protected List<PV<Integer>> pvScaler5LowLimit;
	protected List<PV<Integer>> pvScaler5HighLimit;
	protected List<PV<Integer>> pvScaler6LowLimit;
	protected List<PV<Integer>> pvScaler6HighLimit;

	/** Pvs for getting the Readback values of scaler window limits (one PV per channel of the detector) */
	protected List<PV<Integer>> pvScaler5LowLimitRbv;
	protected List<PV<Integer>> pvScaler5HighLimitRbv;
	protected List<PV<Integer>> pvScaler6LowLimitRbv;
	protected List<PV<Integer>> pvScaler6HighLimitRbv;

	/** Detector state Pvs **/
	protected ReadOnlyPV<XSPRESS3_EPICS_STATUS> pvGetState = null;
	protected PV<Integer> pvConnect = null;
	protected PV<Integer> pvDisconnect = null;
	protected ReadOnlyPV<Boolean> pvIsConnected = null; // 0 = No, 1 = Yes

	private int numChannels;

	public void setNumChannels(int numChannels) {
		this.numChannels = numChannels;
	}
	@Override
	public void createPvs() {
		pvTriggerMode = LazyPVFactory.newIntegerPV(getPvName(TRIGGER_MODE_TEMPLATE));
		pvAcquireTime = LazyPVFactory.newDoublePV( getPvName(ACQUIRE_TIME_TEMPLATE));
		pvArrayCounter = LazyPVFactory.newIntegerPV( getPvName(ARRAY_COUNTER));
		pvArrayCounterRbv = LazyPVFactory.newReadOnlyIntegerPV(getPvName(ARRAY_COUNTER_RBV));
		pvDtcEnergyKev = LazyPVFactory.newDoublePV(getPvName(DTC_ENERGY_KEV));
		pvAcquire = LazyPVFactory.newEnumPV(getPvName(XspressPvName.ACQUIRE), ACQUIRE_STATE.class);
		pvNumImages = LazyPVFactory.newIntegerPV(getPvName(XspressPvName.NUM_IMAGES));
		pvNumImagesRbv = LazyPVFactory.newReadOnlyIntegerPV(getPvName(XspressPvName.NUM_IMAGES_RBV));

		pvGetState = LazyPVFactory.newEnumPV(getPvName(DETECTOR_STATE), XSPRESS3_EPICS_STATUS.class);

		pvScaler5LowLimit = createWindowLimitPvs(getPvName(SCA5_WINDOW_LOW));
		pvScaler5HighLimit = createWindowLimitPvs(getPvName(SCA5_WINDOW_HIGH));
		pvScaler6LowLimit = createWindowLimitPvs(getPvName(SCA6_WINDOW_LOW));
		pvScaler6HighLimit = createWindowLimitPvs(getPvName(SCA6_WINDOW_HIGH));

		pvScaler5LowLimitRbv = createWindowLimitPvs(getPvName(SCA5_WINDOW_LOW_RBV));
		pvScaler5HighLimitRbv = createWindowLimitPvs(getPvName(SCA5_WINDOW_HIGH_RBV));
		pvScaler6LowLimitRbv = createWindowLimitPvs(getPvName(SCA6_WINDOW_LOW_RBV));
		pvScaler6HighLimitRbv = createWindowLimitPvs(getPvName(SCA6_WINDOW_HIGH_RBV));

		pvConnect = LazyPVFactory.newIntegerPV(getPvName(CONNECT));
		pvDisconnect = LazyPVFactory.newIntegerPV(getPvName(DISCONNECT));
		pvIsConnected = LazyPVFactory.newReadOnlyBooleanFromIntegerPV(getPvName(DET_CONNECTED));
	}

	private List<PV<Integer>> createWindowLimitPvs(String template) {
		List<PV<Integer>> pvs = new ArrayList<>();
		for(int i=1; i<=numChannels; i++) {
			pvs.add(LazyPVFactory.newIntegerPV(String.format(template, i)));
		}
		return pvs;
	}

	@Override
	protected Stream<ReadOnlyPV<?>> getPvs() {
		Stream<ReadOnlyPV<?>> windowPvs = Stream.of(pvScaler5LowLimit, pvScaler5HighLimit, pvScaler6HighLimit, pvScaler6LowLimit,
											pvScaler5LowLimitRbv, pvScaler5HighLimitRbv, pvScaler6HighLimitRbv, pvScaler6LowLimitRbv)
										.flatMap(Collection::stream);
		Stream<ReadOnlyPV<?>> controlPvs=  Stream.of(pvTriggerMode, pvAcquireTime, pvArrayCounter, pvArrayCounterRbv,
				pvDtcEnergyKev, pvAcquire, pvNumImages, pvNumImagesRbv, pvGetState,
				pvConnect, pvDisconnect, pvIsConnected);

		return Stream.concat(windowPvs, controlPvs);
	}


}
