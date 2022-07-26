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

import static uk.ac.gda.devices.detector.xspress4.XspressPvName.CAPTURE_CONTOL;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.CAPTURE_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.DETECTOR_STATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.HDF_FILENAME;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.HDF_FILEPATH;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.HDF_FILEPATH_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.HDF_FULL_FILENAME_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.NUM_CAPTURE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.NUM_CAPTURED_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.NUM_CAPTURE_RBV;

import java.util.stream.Stream;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.XSPRESS3_EPICS_STATUS;

public class HdfFilePvProvider extends XspressPvProviderBase {

	/** Hdf file writing PVs  */
	protected PV<Integer> pvHdfNumCapture = null;
	protected ReadOnlyPV<Integer> pvHdfNumCaptureRbv = null;
	protected ReadOnlyPV<Integer> pvHdfNumCapturedRbv = null;
	protected PV<Integer> pvHdfCapturedControl = null;
	protected PV<Boolean> pvHdfCapturingRbv = null;
	protected PV<String> pvHdfFilePath = null;
	protected ReadOnlyPV<String> pvHdfFilePathRbv = null;
	protected PV<String> pvHdfFileName = null;
	protected ReadOnlyPV<String> pvHdfFullFileNameRbv = null;
	protected ReadOnlyPV<XSPRESS3_EPICS_STATUS> pvHdfState = null;

	@Override
	public void createPvs() {
		pvHdfNumCapture = LazyPVFactory.newIntegerPV(getPvName(NUM_CAPTURE));
		pvHdfNumCaptureRbv = LazyPVFactory.newReadOnlyIntegerPV(getPvName(NUM_CAPTURE_RBV));
		pvHdfNumCapturedRbv = LazyPVFactory.newReadOnlyIntegerPV(getPvName(NUM_CAPTURED_RBV));
		pvHdfCapturedControl = LazyPVFactory.newIntegerPV(getPvName(CAPTURE_CONTOL));
		pvHdfCapturingRbv = LazyPVFactory.newBooleanFromIntegerPV(getPvName(CAPTURE_RBV));
		pvHdfState = LazyPVFactory.newEnumPV(getPvName(DETECTOR_STATE), XSPRESS3_EPICS_STATUS.class);

		pvHdfFileName = LazyPVFactory.newStringFromWaveformPV(getPvName(HDF_FILENAME));
		pvHdfFilePath = LazyPVFactory.newStringFromWaveformPV(getPvName(HDF_FILEPATH));
		pvHdfFilePathRbv = LazyPVFactory.newStringFromWaveformPV(getPvName(HDF_FILEPATH_RBV));
		pvHdfFullFileNameRbv = LazyPVFactory.newReadOnlyStringFromWaveformPV(getPvName(HDF_FULL_FILENAME_RBV));
	}

	@Override
	public Stream<ReadOnlyPV<?>> getPvs() {
		return Stream.of(pvHdfNumCapture, pvHdfNumCapturedRbv, pvHdfNumCapturedRbv, pvHdfState,
				pvHdfFileName, pvHdfFilePath, pvHdfFilePathRbv, pvHdfFullFileNameRbv);
	}

}
