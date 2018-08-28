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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gda.mscan.element.AreaScanpath;
import gda.mscan.element.Roi;
import gda.mscan.processor.IClauseElementProcessor;

public class ScanClausesResolverTest extends ResolutionTestsBase {

	private ScanClausesResolver target;
	private List<List<IClauseElementProcessor>> result;

	private IClauseElementProcessor num2Proc;
	private IClauseElementProcessor num3Proc;
	private IClauseElementProcessor numPoint1Proc;
	private IClauseElementProcessor numPoint3Proc;
	private IClauseElementProcessor numPoint5Proc;
	private IClauseElementProcessor spiralProc;
	private IClauseElementProcessor polyProc;

	@Before
	public void setup() {
		num2Proc = mockNumberProc(2);
		num3Proc = mockNumberProc(3);
		numPoint1Proc = mockNumberProc(0.1);
		numPoint3Proc = mockNumberProc(0.3);
		numPoint5Proc = mockNumberProc(0.5);
		spiralProc = mockAreaScanpathProc(AreaScanpath.SPIRAL);
		polyProc = mockRoiProc(Roi.POLYGON);
	}

	@Test(expected = IllegalArgumentException.class)
	public void firstElementMustBeAScannableProcessor() {
		scan = Arrays.asList(num1Proc);
		resolve(scan);
	}

	@Test(expected = IllegalArgumentException.class)
	public void detectorsMustBeAfterAllScanDefs() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc, num1Proc,
				d1Proc,
				s1Proc, num1Proc, num1Proc, num1Proc);
		resolve(scan);
	}

	@Test(expected = IllegalArgumentException.class)
	public void monitorsMustBeAfterAllScanDefs() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc, num1Proc,
				m1Proc,
				s1Proc, num1Proc, num1Proc, num1Proc);
		resolve(scan);
	}

	@Test(expected = IllegalArgumentException.class)
	public void scannableReadoutsMustBeAfterAllScanDefsWithSpecStyle() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc, num1Proc,
				s1Proc,
				s1Proc, num1Proc, num1Proc, num1Proc,
				m1Proc,
				d2Proc, num1Proc,
				s1Proc);
		resolve(scan);
	}

	@Test(expected = IllegalArgumentException.class)
	public void scannableReadoutsMustBeAfterAllScanDefsWithMappingStyle() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc, num1Proc,
				s1Proc,
				s1Proc, s2Proc, num1Proc, num2Proc, num1Proc, num2Proc, num1Proc, num2Proc,
				m1Proc,
				d2Proc, num1Proc,
				s1Proc);
		resolve(scan);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidLengthMScanClausesAtEndOfCommandAreRejected() {
		scan = Arrays.asList(
				s1Proc, s2Proc, rectProc, num1Proc, num1Proc);
		resolve(scan);
	}

	@Test
	public void oldStyle() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc, num1Proc,
				s2Proc, num1Proc, num1Proc, num1Proc);
		resolve(scan);
		assertThat(result.size(), is(2));
		assertThat(result.get(0).size(), is(4));
		assertThat(result.get(0), contains(s1Proc, num1Proc, num1Proc, num1Proc));
		assertThat(result.get(1).size(), is(4));
		assertThat(result.get(1), contains(s2Proc, num1Proc, num1Proc, num1Proc));
	}

	@Test
	public void oldStyleWithScannableGroup() {
		scan = Arrays.asList(
				sGProc, num1Proc, num2Proc, num1Proc, num2Proc, num1Proc, num2Proc,
				sGProc, num2Proc, num1Proc, num2Proc, num1Proc, num2Proc, num1Proc);
		resolve(scan);
		assertThat(result.size(), is(2));
		assertThat(result.get(0).size(), is(7));
		assertThat(result.get(0), contains(sGProc, num1Proc, num2Proc, num1Proc, num2Proc, num1Proc, num2Proc));
		assertThat(result.get(1).size(), is(7));
		assertThat(result.get(1), contains(sGProc, num2Proc, num1Proc, num2Proc, num1Proc, num2Proc, num1Proc));
	}

	@Test
	public void oldStyleStartStep() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc,
				s2Proc, num1Proc, num1Proc, num1Proc);
		resolve(scan);
		assertThat(result.size(), is(2));
		assertThat(result.get(0).size(), is(3));
		assertThat(result.get(0), contains(s1Proc, num1Proc, num1Proc));
		assertThat(result.get(1).size(), is(4));
		assertThat(result.get(1), contains(s2Proc, num1Proc, num1Proc, num1Proc));
	}

	@Test
	public void oldStyleStartStepWithTrailingPureScannable() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc,
				s2Proc, num1Proc, num1Proc, num1Proc,
				sGProc);
		resolve(scan);
		assertThat(result.size(), is(3));
		assertThat(result.get(0).size(), is(3));
		assertThat(result.get(0), contains(s1Proc, num1Proc, num1Proc));
		assertThat(result.get(1).size(), is(4));
		assertThat(result.get(1), contains(s2Proc, num1Proc, num1Proc, num1Proc));
		assertThat(result.get(2).size(), is(1));
		assertThat(result.get(2), contains(sGProc));
	}

	@Test
	public void oldStyleJustDetectorWithParam() {
		scan = Arrays.asList(
				d1Proc, num1Proc);
		resolve(scan);
		assertThat(result.size(), is(1));
		assertThat(result.get(0).size(), is(2));
		assertThat(result.get(0), contains(d1Proc, num1Proc));
	}

	@Test
	public void oldStyleJustDetectorWithoutParam() {
		scan = Arrays.asList(
				d1Proc);
		resolve(scan);
		assertThat(result.size(), is(1));
		assertThat(result.get(0).size(), is(1));
		assertThat(result.get(0), contains(d1Proc));
	}

	@Test
	public void oldStyleJustTwoDetectorsWithParam() {
		scan = Arrays.asList(
				d1Proc, num1Proc,
				d2Proc, num1Proc);
		resolve(scan);
		assertThat(result.size(), is(2));
		assertThat(result.get(0).size(), is(2));
		assertThat(result.get(0), contains(d1Proc, num1Proc));
		assertThat(result.get(1).size(), is(2));
		assertThat(result.get(1), contains(d2Proc, num1Proc));
	}

	@Test
	public void oldStyleJustMonitorPlusDetectorWithParamAfter() {
		scan = Arrays.asList(
				m1Proc,
				d2Proc, num1Proc);
		resolve(scan);
		assertThat(result.size(), is(2));
		assertThat(result.get(0).size(), is(1));
		assertThat(result.get(0), contains(m1Proc));
		assertThat(result.get(1).size(), is(2));
		assertThat(result.get(1), contains(d2Proc, num1Proc));
	}

	@Test
	public void oldStyleJustTwoDetectorsOneWithParamInbetween() {
		scan = Arrays.asList(
				d1Proc, num1Proc,
				d2Proc);
		resolve(scan);
		assertThat(result.size(), is(2));
		assertThat(result.get(0).size(), is(2));
		assertThat(result.get(0), contains(d1Proc, num1Proc));
		assertThat(result.get(1).size(), is(1));
		assertThat(result.get(1), contains(d2Proc));
	}

	@Test
	public void oldStyleScanDefsPlusMonitorsDetectorsAndSingleScannables() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc, num1Proc,
				s2Proc, num1Proc, num1Proc,
				m1Proc,
				d2Proc, num1Proc,
				s1Proc,
				s2Proc,
				s2Proc,
				s1Proc);
		resolve(scan);
		assertThat(result.size(), is(8));
		assertThat(result.get(0).size(), is(4));
		assertThat(result.get(0), contains(s1Proc, num1Proc, num1Proc, num1Proc));
		assertThat(result.get(1).size(), is(3));
		assertThat(result.get(1), contains(s2Proc, num1Proc, num1Proc));
		assertThat(result.get(2).size(), is(1));
		assertThat(result.get(2), contains(m1Proc));
		assertThat(result.get(3).size(), is(2));
		assertThat(result.get(3), contains(d2Proc, num1Proc));
		assertThat(result.get(4).size(), is(1));
		assertThat(result.get(4), contains(s1Proc));
		assertThat(result.get(5).size(), is(1));
		assertThat(result.get(5), contains(s2Proc));
		assertThat(result.get(6).size(), is(1));
		assertThat(result.get(6), contains(s2Proc));
		assertThat(result.get(7).size(), is(1));
		assertThat(result.get(7), contains(s1Proc));
	}

	@Test
	public void newStyleSingle() {
		scan = Arrays.asList(
				s1Proc, s2Proc, rectProc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc);
		resolve(scan);
		assertThat(result.size(), is(1));
		assertThat(result.get(0).size(), is(10));
		assertThat(result.get(0), contains(s1Proc, s2Proc, rectProc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc));
	}

	@Test
	public void newStyleSingleWithScannableGroup() {
		scan = Arrays.asList(
				sGProc, rectProc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc);
		resolve(scan);
		assertThat(result.size(), is(1));
		assertThat(result.get(0).size(), is(9));
		assertThat(result.get(0), contains(sGProc, rectProc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc));
	}

	@Test
	public void newStyleSingleDefaultRoiWithScannableGroup() {
		scan = Arrays.asList(
				sGProc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc);
		resolve(scan);
		assertThat(result.size(), is(1));
		assertThat(result.get(0).size(), is(8));
		assertThat(result.get(0), contains(sGProc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc));
	}

	@Test
	public void newStyleSingleDefaultRoi() {
		scan = Arrays.asList(
				s1Proc, s2Proc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc);
		resolve(scan);
		assertThat(result.size(), is(1));
		assertThat(result.get(0).size(), is(9));
		assertThat(result.get(0), contains(s1Proc, s2Proc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc));
	}

	@Test
	public void newStyleMultiple() {
		scan = Arrays.asList(
				s1Proc, s2Proc, rectProc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc,
				s1Proc, s2Proc, polyProc, num1Proc, num1Proc, num2Proc, num2Proc, num3Proc, num3Proc, spiralProc, numPoint5Proc, numPoint5Proc, numPoint3Proc);
		resolve(scan);
		assertThat(result.size(), is(2));
		assertThat(result.get(0).size(), is(10));
		assertThat(result.get(0), contains(s1Proc, s2Proc, rectProc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc));
		assertThat(result.get(1).size(), is(13));
		assertThat(result.get(1), contains(s1Proc, s2Proc, polyProc, num1Proc, num1Proc, num2Proc, num2Proc, num3Proc, num3Proc, spiralProc, numPoint5Proc, numPoint5Proc, numPoint3Proc));
	}

	@Test
	public void bothWithDetectorsMonitorsAndSingleScannables() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc, num1Proc,
				s1Proc, s2Proc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc,
				s2Proc, num1Proc, num1Proc,
				s1Proc, s2Proc, polyProc, num1Proc, num1Proc, num2Proc, num2Proc, num3Proc, num3Proc, spiralProc, numPoint5Proc, numPoint5Proc, numPoint3Proc,
				m1Proc,
				d2Proc, num1Proc,
				s2Proc,
				d2Proc);
		resolve(scan);
		assertThat(result.size(), is(8));
		assertThat(result.get(0).size(), is(4));
		assertThat(result.get(0), contains(s1Proc, num1Proc, num1Proc, num1Proc));
		assertThat(result.get(1).size(), is(9));
		assertThat(result.get(1), contains(s1Proc, s2Proc, num1Proc, num1Proc, num2Proc, num2Proc, gridProc, numPoint1Proc, numPoint1Proc));
		assertThat(result.get(2).size(), is(3));
		assertThat(result.get(2), contains(s2Proc, num1Proc, num1Proc));
		assertThat(result.get(3).size(), is(13));
		assertThat(result.get(3), contains(s1Proc, s2Proc, polyProc, num1Proc, num1Proc, num2Proc, num2Proc, num3Proc, num3Proc, spiralProc, numPoint5Proc, numPoint5Proc, numPoint3Proc));
		assertThat(result.get(4).size(), is(1));
		assertThat(result.get(4), contains(m1Proc));
		assertThat(result.get(5).size(), is(2));
		assertThat(result.get(5), contains(d2Proc, num1Proc));
		assertThat(result.get(6).size(), is(1));
		assertThat(result.get(6), contains(s2Proc));
		assertThat(result.get(7).size(), is(1));
		assertThat(result.get(7), contains(d2Proc));
	}

	@Test
	public void oldStyleMultipleDetectorsAndMonitors() {
		scan = Arrays.asList(
				s1Proc, num1Proc, num1Proc, num1Proc,
				m1Proc,
				m2Proc,
				d1Proc,
				d2Proc, num1Proc,
				d1Proc, num1Proc,
				d2Proc, num1Proc,
				m1Proc,
				d2Proc, num1Proc,
				m1Proc,
				d1Proc,
				d2Proc,
				m1Proc);
		resolve(scan);
		assertThat(result.size(), is(13));
		assertThat(result.get(0).size(), is(4));
		assertThat(result.get(0), contains(s1Proc, num1Proc, num1Proc, num1Proc));
		assertThat(result.get(1).size(), is(1));
		assertThat(result.get(1), contains(m1Proc));
		assertThat(result.get(2).size(), is(1));
		assertThat(result.get(2), contains(m2Proc));
		assertThat(result.get(3).size(), is(1));
		assertThat(result.get(3), contains(d1Proc));
		assertThat(result.get(4).size(), is(2));
		assertThat(result.get(4), contains(d2Proc, num1Proc));
		assertThat(result.get(5).size(), is(2));
		assertThat(result.get(5), contains(d1Proc, num1Proc));
		assertThat(result.get(6).size(), is(2));
		assertThat(result.get(6), contains(d2Proc, num1Proc));
		assertThat(result.get(7).size(), is(1));
		assertThat(result.get(7), contains(m1Proc));
		assertThat(result.get(8).size(), is(2));
		assertThat(result.get(8), contains(d2Proc, num1Proc));
		assertThat(result.get(9).size(), is(1));
		assertThat(result.get(9), contains(m1Proc));
		assertThat(result.get(10).size(), is(1));
		assertThat(result.get(10), contains(d1Proc));
		assertThat(result.get(11).size(), is(1));
		assertThat(result.get(11), contains(d2Proc));
		assertThat(result.get(12).size(), is(1));
		assertThat(result.get(12), contains(m1Proc));
	}

	private void resolve(final List<IClauseElementProcessor> scan) {
		target = new ScanClausesResolver(scan);
		result = target.resolveScanClauses();
	}
}
