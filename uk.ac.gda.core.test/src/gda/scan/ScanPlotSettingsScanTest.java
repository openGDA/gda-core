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

package gda.scan;

import static org.junit.Assert.assertEquals;

import org.eclipse.january.dataset.Dataset;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.Scannable;
import gda.jython.commands.ScannableCommands;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanPlotSettingsScanTest {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ScanPlotSettingsScanTest.class);

	static String testScratchDirectoryName = null;

	/**
	 * Setups of environment for the tests
	 *
	 * @param name
	 *            of test
	 * @param makedir
	 *            if true the scratch dir is deleted and constructed
	 * @throws Exception
	 *             if setup fails
	 */
	public static void setUp(String name, boolean makedir) throws Exception {
		testScratchDirectoryName = TestHelpers.setUpTest(ScanPlotSettingsScanTest.class, name, makedir);
		LocalProperties.clearProperty(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST);
		LocalProperties.clearProperty(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_XAXIS);
		LocalProperties.clearProperty(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES_INVISIBLE);
		LocalProperties.clearProperty(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES);
	}

	/**
	 * Creates a scan and tests the generated ScanPlotSettings when GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST is True
	 * and XAXIS_INDEX is 1
	 *
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testScanPlotSettings2() throws Exception {
		setUp("testScanPlotSettings1", true);

		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST, "True");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_XAXIS, "1");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES_INVISIBLE, "8");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES, "0 2 5");

		ScanPlotSettings expected = new ScanPlotSettings();
		expected.setXAxisName("SC1I1"); // index=1
		expected.setYAxesShown(new String[] { "SC1I0", "SC1E0", "SC2I1" });
		expected.setYAxesNotShown(new String[] { "SimpleDetector1_0" });

		ConcurrentScan scan = createScan(false);
		performCheck(expected, scan);
	}

	/**
	 * Creates a scan and tests the generated ScanPlotSettings when GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST is True
	 * and XAXIS_INDEX is -2
	 *
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testScanPlotSettings3() throws Exception {
		setUp("testScanPlotSettings3", true);
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST, "True");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_XAXIS, "-2");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES_INVISIBLE, "-3");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES, "-1 -2");

		ScanPlotSettings expected = new ScanPlotSettings();
		expected.setXAxisName("SC1I0"); // index=1
		// SimpleDetector1 returns multiple values, so each element of the data should be known as 'SimpleDetector1_x'
		// the use of the axes values set above to match the names fixed below is not used anywhere else in the code
		// except for these tests:
		// so is the the right way to be doing this test? Are these values used at all in the 'real' code? So should
		// this test be done at all?
		// also the use of negative values is not clear and not mentioned at all in the Javadoc so are these values a
		// fudge to get the test working?
		// An explanation in the Javadoc would help.
		expected.setYAxesShown(new String[] { "SimpleDetector1_9", "SimpleDetector1_8" });
		expected.setYAxesNotShown(new String[] { "SimpleDetector1_7" });

		ConcurrentScan scan = createScan(false);
		performCheck(expected, scan);
	}

	private void performCheck(ScanPlotSettings expected, ConcurrentScan theScan) throws Exception {
		ScanPlotSettings sps = ScannableCommands.createScanPlotSettings(theScan);
		assertEquals(expected, sps);
	}

	/**
	 * Creates a scan and tests the generated ScanPlotSettings when GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST is True
	 * and XAXIS_INDEX is 1 for a nested scan
	 *
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testScanPlotSettings4() throws Exception {
		setUp("testScanPlotSettings4", true);
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST, "True");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_XAXIS, "-1");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES_INVISIBLE, "-2");
		LocalProperties.set(ScannableCommands.GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES, "-1");

		ScanPlotSettings expected = new ScanPlotSettings();
		expected.setXAxisName("SC1I1"); // index=1
		expected.setYAxesShown(new String[] { "SimpleDetector1_9" });
		expected.setYAxesNotShown(new String[] { "SimpleDetector1_8" });

		ConcurrentScan scan = createScan(false);
		performCheck(expected, scan);
	}

	private ConcurrentScan createScan(boolean nested) {
		Scannable simpleScannable1 = TestHelpers.createTestScannable("SimpleScannable1", 0., new String[] { "SC1E0",
				"SC1E1" }, new String[] { "SC1I0", "SC1I1" }, 0, new String[] { "%5.2g" }, null);
		Scannable simpleScannable2 = TestHelpers.createTestScannable("SimpleScannable2", 0., new String[] { "SC2E0",
				"SC2E1" }, new String[] { "SC2I0", "SC2I1" }, 0, new String[] { "%5.2g" }, null);

		int[] dims1 = new int[] { 10 };

		Detector simpleDetector1 = TestHelpers.createTestDetector("SimpleDetector1", 0., new String[] { "SD1" },
				new String[] {}, 0, new String[] { "%5.2g" }, TestHelpers.createTestNexusGroupData(dims1, Dataset.FLOAT64, true),
				null, "description1", "detectorID1", "detectorType1");

		Object[] args = nested ? new Object[] { simpleScannable1, new Double[] { 0., 0. }, new Double[] { 10., 10. },
				new Double[] { 1., 1. }, simpleScannable2, new Double[] { 0., 0. }, new Double[] { 10., 10. },
				new Double[] { 1., 1. }, simpleDetector1 } : new Object[] { simpleScannable1,
				new Double[] { 0., 0. }, new Double[] { 10., 10. }, new Double[] { 1., 1. }, simpleScannable2,
				simpleDetector1 };

		ConcurrentScan scan = new ConcurrentScan(args);
		return scan;
	}
}