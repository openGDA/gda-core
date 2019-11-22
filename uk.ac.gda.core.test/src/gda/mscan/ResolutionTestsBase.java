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

package gda.mscan;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import gda.device.Detector;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.mscan.element.AreaScanpath;
import gda.mscan.element.RegionShape;
import gda.mscan.processor.AreaScanpathElementProcessor;
import gda.mscan.processor.IClauseElementProcessor;
import gda.mscan.processor.NumberElementProcessor;
import gda.mscan.processor.RegionShapeElementProcessor;
import gda.mscan.processor.ScannableElementProcessor;
import gda.mscan.processor.ScannableGroupElementProcessor;

public class ResolutionTestsBase {

	protected List<IClauseElementProcessor> scan;

	protected static Scannable s1;
	protected static Scannable s2;
	protected static ScannableGroup sg1;
	protected static Detector d1;
	protected static Detector d2;
	protected static Monitor m1;
	protected static Monitor m2;
	protected static IClauseElementProcessor s1Proc = mockScannableProc(s1, "DummyOne");
	protected static IClauseElementProcessor s2Proc = mockScannableProc(s2, "DummyTwo");
	protected static IClauseElementProcessor sGProc = mockScannableGroupProc(s1, s2, "Group1");
	protected static IClauseElementProcessor d1Proc = mockDetectorProc(d1, "DetectorOne");
	protected static IClauseElementProcessor d2Proc = mockDetectorProc(d2, "DetectorTwo");
	protected static IClauseElementProcessor m1Proc = mockMonitorProc(m1, "MonitorOne");
	protected static IClauseElementProcessor m2Proc = mockMonitorProc(m2, "MonitorTwo");
	protected static IClauseElementProcessor num1Proc = mockNumberProc(1);
	protected static IClauseElementProcessor gridProc = mockAreaScanpathProc(AreaScanpath.GRID_POINTS);
	protected static IClauseElementProcessor rectProc = mockRoiProc(RegionShape.RECTANGLE);

	protected static IClauseElementProcessor mockScannableProc(Scannable scannable, final String name) {
		IClauseElementProcessor proc = baseScannableProcSetup(scannable, name);
		when(proc.hasScannableGroup()).thenReturn(false);
		when(proc.hasDetector()).thenReturn(false);
		when(proc.hasMonitor()).thenReturn(false);
		return proc;
	}

	protected static  IClauseElementProcessor mockScannableGroupProc(Scannable scannable1, Scannable scannable2, final String name) {
		scannable1 = mock(ScannableMotor.class);
		scannable2 = mock(ScannableMotor.class);
		sg1 = mock(ScannableGroup.class);
		when(sg1.getGroupMembers()).thenReturn(Arrays.asList(scannable1, scannable2));
		when(sg1.getName()).thenReturn(name);
		IClauseElementProcessor proc = mock(ScannableGroupElementProcessor.class);
		when(proc.getElement()).thenReturn(sg1);
		when(proc.hasScannable()).thenReturn(true);
		when(proc.hasRoi()).thenReturn(false);
		when(proc.hasNumber()).thenReturn(false);
		when(proc.hasScannableGroup()).thenReturn(true);
		when(proc.hasDetector()).thenReturn(false);
		when(proc.hasMonitor()).thenReturn(false);
		return proc;
	}

	protected static IClauseElementProcessor mockDetectorProc(Scannable scannable, final String name) {
		IClauseElementProcessor proc = baseScannableProcSetup(scannable, name);
		when(proc.hasScannableGroup()).thenReturn(false);
		when(proc.hasDetector()).thenReturn(true);
		when(proc.hasMonitor()).thenReturn(false);
		return proc;
	}

	protected static IClauseElementProcessor mockMonitorProc(Scannable scannable, final String name) {
		IClauseElementProcessor proc = baseScannableProcSetup(scannable, name);
		when(proc.hasScannableGroup()).thenReturn(false);
		when(proc.hasDetector()).thenReturn(false);
		when(proc.hasMonitor()).thenReturn(true);
		return proc;
	}

	protected static IClauseElementProcessor baseScannableProcSetup(Scannable scannable, final String name) {
		scannable = mock(ScannableMotor.class);
		when(scannable.getName()).thenReturn(name);
		IClauseElementProcessor proc = mock(ScannableElementProcessor.class);
		when(proc.getElement()).thenReturn(scannable);
		when(proc.hasScannable()).thenReturn(true);
		when(proc.hasRoi()).thenReturn(false);
		when(proc.hasNumber()).thenReturn(false);
		return proc;
	}

	protected static IClauseElementProcessor mockNumberProc(Number num) {
		IClauseElementProcessor proc = mock(NumberElementProcessor.class);
		when(proc.getElement()).thenReturn(num);
		when(proc.hasScannable()).thenReturn(false);
		when(proc.hasRoi()).thenReturn(false);
		when(proc.hasNumber()).thenReturn(true);
		when(proc.hasScannableGroup()).thenReturn(false);
		when(proc.hasDetector()).thenReturn(false);
		when(proc.hasMonitor()).thenReturn(false);
		return proc;
	}

	protected static  IClauseElementProcessor mockAreaScanpathProc(AreaScanpath scanpath) {
		IClauseElementProcessor proc = mock(AreaScanpathElementProcessor.class);
		when(proc.getElement()).thenReturn(scanpath);
		when(proc.hasScannable()).thenReturn(false);
		when(proc.hasRoi()).thenReturn(false);
		when(proc.hasNumber()).thenReturn(false);
		when(proc.hasScannableGroup()).thenReturn(false);
		when(proc.hasDetector()).thenReturn(false);
		when(proc.hasMonitor()).thenReturn(false);
		return proc;
	}

	protected static IClauseElementProcessor mockRoiProc(RegionShape roi) {
		IClauseElementProcessor proc = mock(RegionShapeElementProcessor.class);
		when(proc.getElement()).thenReturn(roi);
		when(proc.hasScannable()).thenReturn(false);
		when(proc.hasRoi()).thenReturn(true);
		when(proc.hasNumber()).thenReturn(false);
		when(proc.hasScannableGroup()).thenReturn(false);
		when(proc.hasDetector()).thenReturn(false);
		when(proc.hasMonitor()).thenReturn(false);
		return proc;
	}
}
