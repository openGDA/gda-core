package uk.ac.gda.devices.xspress4;
/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.scan.ConcurrentScan;
import gda.scan.Scan;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressParameters;

public class Xspress4DetectorTest extends TestBase {

	private static final Logger logger = LoggerFactory.getLogger(Xspress4DetectorTest.class);

	@Before
	public void setup() throws Exception {
		setupLocalProperties();
		setupMotor();
		setupDetectorObjects();
	}

	@After
	public void tearDown() {
		// Remove factories from Finder so they do not affect other tests
		Finder.getInstance().removeAllFactories();
	}

	private String runScan(XspressParameters xspressParams) throws Exception {
		this.xspressParams = xspressParams;
		xspress4detector.applyConfigurationParameters(xspressParams);
		Scan scan = new ConcurrentScan(new Object[]{dummyScannableMotor, 0, 1, 0.1, xspress4detector, 1.0});
		scan.runScan();
		return scan.getDataWriter().getCurrentFileName();
	}

	@Test
	public void testScalersAndMca() throws Exception {
		setupForTest(this.getClass(), "testScalersAndMca");
		String filename = runScan( getParameters(XspressParameters.READOUT_MODE_SCALERS_AND_MCA, ResGrades.NONE, 1, 101) );
		checkNexusScalersAndMca(filename);
		checkAsciiScalersAndMca(getAsciiNameFromNexusName(filename));
	}

	@Test
	public void testScalersAndMcaExcludedElements() throws Exception {
		setupForTest(this.getClass(), "testScalersAndMcaExcludedElements");
		XspressParameters params = getParameters(XspressParameters.READOUT_MODE_SCALERS_AND_MCA, ResGrades.NONE, 1, 101);
		params.getDetector(0).setExcluded(true);
		params.getDetector(2).setExcluded(true);
		String filename = runScan(params);

		checkNexusScalersAndMca(filename);
		checkAsciiScalersAndMca(getAsciiNameFromNexusName(filename));
	}

	@Test
	public void testScalersAndMcaWithDtc() throws Exception {
		setupForTest(this.getClass(), "testScalersAndMcaWithDtc");
		XspressParameters params = getParameters(XspressParameters.READOUT_MODE_SCALERS_AND_MCA, ResGrades.NONE, 1, 101);
		params.setShowDTRawValues(true);
		String filename = runScan(params);
		checkNexusScalersAndMca(filename);
		checkAsciiScalersAndMca(getAsciiNameFromNexusName(filename));
	}

	@Test
	public void testScalersAndMcaWithDtcNoElementFF() throws Exception {
		setupForTest(this.getClass(), "testScalersAndMcaWithDtcNoElementFF");
		XspressParameters params = getParameters(XspressParameters.READOUT_MODE_SCALERS_AND_MCA, ResGrades.NONE, 1, 101);
		params.setShowDTRawValues(true);
		params.setOnlyShowFF(true);
		String filename = runScan(params);
		checkNexusScalersAndMca(filename);
		checkAsciiScalersAndMca(getAsciiNameFromNexusName(filename));
	}

	@Test
	public void testRoiAllGrades() throws Exception {
		setupForTest(this.getClass(), "testRoiAllGrades");
		String filename = runScan( getParameters(XspressParameters.READOUT_MODE_REGIONSOFINTEREST, ResGrades.ALLGRADES, 1, 101));
		checkNexusRoiAllResGrade(filename);
		checkAsciiRoiAllResGrade(getAsciiNameFromNexusName(filename));
	}

	@Test
	public void testRoiAllGradesOnlyFF() throws Exception {
		setupForTest(this.getClass(), "testRoiAllGradesOnlyFF");
		XspressParameters params = getParameters(XspressParameters.READOUT_MODE_REGIONSOFINTEREST, ResGrades.ALLGRADES, 1, 101);
		params.setOnlyShowFF(true);
		params.setShowDTRawValues(true);
		String filename = runScan(params);
		checkNexusRoiAllResGrade(filename);
		checkAsciiRoiAllResGrade(getAsciiNameFromNexusName(filename));
	}


	@Test
	public void testRoiNoGrades() throws Exception {
		setupForTest(this.getClass(), "testRoiNoGrades");
		String filename = runScan(getParameters(XspressParameters.READOUT_MODE_REGIONSOFINTEREST, ResGrades.NONE, 1, 101));
		checkNexusRoiNoGrade(filename);
		checkAsciiRoiNoGrade(getAsciiNameFromNexusName(filename));
	}

	@Test
	public void testRoiNoGradesExcludeElements() throws Exception {
		setupForTest(this.getClass(), "testRoiNoGradesExcludeElements");
		XspressParameters params = getParameters(XspressParameters.READOUT_MODE_REGIONSOFINTEREST, ResGrades.NONE, 1, 101);
		params.getDetector(0).setExcluded(true);
		params.getDetector(3).setExcluded(true);
		String filename = runScan(params);
		checkNexusRoiNoGrade(filename);
		checkAsciiRoiNoGrade(getAsciiNameFromNexusName(filename));
	}

	@Test
	public void testRoiThreshold() throws Exception {
		setupForTest(this.getClass(), "testRoiThreshold");
		String resThresholdString = ResGrades.THRESHOLD + " 6";
		XspressParameters params = getParameters(XspressParameters.READOUT_MODE_REGIONSOFINTEREST, resThresholdString, 1, 101);
		params.setShowDTRawValues(true);
		String filename = runScan(params);
		checkNexusRoiThreshold(filename);
		checkAsciiRoiThreshold(getAsciiNameFromNexusName(filename));
	}

	@Test
	public void testRoiThresholdExcludeElementsDtc() throws Exception {
		setupForTest(this.getClass(), "testRoiThresholdExcludeElementsDtc");
		String resThresholdString = ResGrades.THRESHOLD + " 6";
		XspressParameters params = getParameters(XspressParameters.READOUT_MODE_REGIONSOFINTEREST, resThresholdString, 1, 101);
		params.getDetector(0).setExcluded(true);
		params.getDetector(3).setExcluded(true);
		params.setShowDTRawValues(true);
		String filename = runScan(params);
		checkNexusRoiThreshold(filename);
		checkAsciiRoiThreshold(getAsciiNameFromNexusName(filename));
	}
}

