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

import static uk.ac.gda.devices.detector.xspress4.XspressPvName.MCA_ARRAY_DATA_ALL;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_ACQUISITION_ACTIVE_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_CONNECTED_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_FILENAME;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_FLUSH_RATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_FLUSH_RATE_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_FRAMES_WRITTEN_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_OUTPUT_FILE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_STOP;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.META_WRITING_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.TRIGGER_DETECTOR;

import java.util.stream.Stream;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;

public class OdinPvProvider extends XspressPvProviderBase {

	protected PV<Integer> pvMetaFlushRate = null;
	protected ReadOnlyPV<Integer> pvMetaFlushRateRbv = null;
	protected PV<String> pvMetaFileName = null;
	protected ReadOnlyPV<String> pvMetaOutputFileRbv = null;
	protected ReadOnlyPV<Integer> pvMetaFramesWrittenRbv = null;
	protected PV<Integer> pvMetaStop = null;
	protected ReadOnlyPV<Boolean> pvMetaIsConnectedRbv = null;
	protected ReadOnlyPV<Boolean> pvMetaIsActiveRbv = null;
	protected ReadOnlyPV<Boolean> pvMetaIsWritingRbv = null;
	protected PV<Integer> pvSofwareTrigger;
	protected ReadOnlyPV<Integer[]> pvForAllMcaArray = null; // PV containing full MCA data array for detector (4096 * num channel elements)
	protected PV<Integer> pvArrEnableCallback;

	@Override
	public void createPvs() {
		pvMetaFlushRate = LazyPVFactory.newIntegerPV(getPvName(META_FLUSH_RATE));
		pvMetaFlushRateRbv = LazyPVFactory.newReadOnlyIntegerPV(getPvName(META_FLUSH_RATE_RBV));
		pvMetaFileName = LazyPVFactory.newStringPV(getPvName(META_FILENAME));
		pvMetaOutputFileRbv = LazyPVFactory.newReadOnlyStringPV(getPvName(META_OUTPUT_FILE));
		pvMetaFramesWrittenRbv = LazyPVFactory.newReadOnlyIntegerPV(getPvName(META_FRAMES_WRITTEN_RBV));
		pvMetaStop = LazyPVFactory.newIntegerPV(getPvName(META_STOP));
		pvMetaIsConnectedRbv = LazyPVFactory.newReadOnlyBooleanFromIntegerPV(getPvName(META_CONNECTED_RBV));
		pvMetaIsActiveRbv = LazyPVFactory.newReadOnlyBooleanFromIntegerPV(getPvName(META_ACQUISITION_ACTIVE_RBV));
		pvMetaIsWritingRbv = LazyPVFactory.newReadOnlyBooleanFromIntegerPV(getPvName(META_WRITING_RBV));
	}

	public void createCamPvs(String camPrefix) {
		pvSofwareTrigger = LazyPVFactory.newIntegerPV(getPvName(TRIGGER_DETECTOR, camPrefix));
	}

	public void createArrPvs(String arrPrefix) {
		pvForAllMcaArray = LazyPVFactory.newReadOnlyIntegerArrayPV(getPvName(MCA_ARRAY_DATA_ALL, arrPrefix));
		pvArrEnableCallback = LazyPVFactory.newIntegerPV(getPvName(XspressPvName.MCA_ARRAY_ENABLE_CALLBACKS, arrPrefix));
	}

	@Override
	public Stream<ReadOnlyPV<?>> getPvs() {
		return Stream.of(pvMetaFlushRate, pvMetaFlushRateRbv, pvMetaFileName, pvMetaOutputFileRbv, pvMetaFramesWrittenRbv, pvMetaStop,
				pvMetaIsConnectedRbv, pvMetaIsActiveRbv, pvMetaIsWritingRbv, pvSofwareTrigger,
				pvForAllMcaArray, pvArrEnableCallback);
	}
}
