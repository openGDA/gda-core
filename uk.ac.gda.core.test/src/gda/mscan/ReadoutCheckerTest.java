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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gda.mscan.element.RegionShape;
import gda.mscan.element.Scanpath;
import gda.mscan.processor.IClauseElementProcessor;

@RunWith(Parameterized.class)
public class ReadoutCheckerTest extends ResolutionTestsBase {

	private ReadoutChecker target;
	private static final boolean MATCH = true;
	private static final boolean NO_MATCH = false;
	private boolean[] expectedResults;
	private Class<? extends Exception> expectedException;


	private static IClauseElementProcessor[] pathsStem = {s1Proc, num1Proc, num1Proc, num1Proc,
			s1Proc, s1Proc, num1Proc, num1Proc, num1Proc, num1Proc, gridProc, num1Proc, num1Proc};
	private static boolean[] expectedStem = {false, false, false, false,
			false, false, false, false,false, false, false, false, false};

	private static IClauseElementProcessor[] it = ArrayUtils.addAll(pathsStem, s1Proc, s1Proc);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		s1Proc = mockScannableProc(s1, "DummyOne");
		sGProc = mockScannableGroupProc(s1, s2, "Group1");
		d1Proc = mockDetectorProc(d1, "DetectorOne");
		m1Proc = mockMonitorProc(m1, "MonitorOne");
		num1Proc = mockNumberProc(1);
		gridProc = mockScanpathProc(Scanpath.GRID_POINTS);
		rectProc = mockRoiProc(RegionShape.RECTANGLE);
		return Arrays.asList(new Object[][] {
			// single scannables as the last entry that aren't detectors or monitors match
			{"singleScannableAtEndMatches",
			ArrayUtils.addAll(pathsStem, s1Proc), ArrayUtils.addAll(expectedStem, MATCH), null},
			{"singleScannableGroupAtEndMatches",
			ArrayUtils.addAll(pathsStem, sGProc), ArrayUtils.addAll(expectedStem, MATCH), null},
			{"singleDetectorAtEndNoMatch",
			ArrayUtils.addAll(pathsStem, d1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH), null},
			{"singleMonitorAtEndNoMatch",
			ArrayUtils.addAll(pathsStem, m1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH), null},

			// "pure" scannable as second to last entry with no Detectors or Monitors before it match if not followed
			// by non scannables (potential new scan path def)
			{"twoScannableaAtEndMatchesTwice",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH), null},
			{"singleScannableFollowedByDetectorAtEndMatchesOnce",
			ArrayUtils.addAll(pathsStem, s1Proc, d1Proc), ArrayUtils.addAll(expectedStem, MATCH, false), null},
			{"singleScannableFollowedByMonitorAtEndMatchesOnce",
			ArrayUtils.addAll(pathsStem, s1Proc, m1Proc), ArrayUtils.addAll(expectedStem, MATCH, false), null},
			{"singleScannableFollowedByNonScannableAtEndNoMatch",
			ArrayUtils.addAll(pathsStem, s1Proc, num1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH, false), null},
			{"singleScannableFollowedByScannableGroupAtEndMatchesTwice",
			ArrayUtils.addAll(pathsStem, s1Proc, sGProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH), null},

			// scannable group as second to last entry with no Detectors or Monitors before it match if not followed
			// by non scannables (potential new scan path def)
			{"singleScannableGroupFollowedByScannableAtEndMatchesTwice",
			ArrayUtils.addAll(pathsStem, sGProc, s1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH), null},
			{"singleScannableGroupFollowedByDetectorAtEndMatchesOnce",
			ArrayUtils.addAll(pathsStem, sGProc, d1Proc), ArrayUtils.addAll(expectedStem, MATCH, false), null},
			{"singleScannableGroupFollowedByMonitorAtEndMatchesOnce",
			ArrayUtils.addAll(pathsStem, sGProc, m1Proc), ArrayUtils.addAll(expectedStem, MATCH, false), null},
			{"singleScannableGroupFollowedByNonScannableAtEndNoMatch",
			ArrayUtils.addAll(pathsStem, sGProc, num1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH, false), null},
			{"twoScannableGroupsAtEndMatchesTwice",
			ArrayUtils.addAll(pathsStem, sGProc, sGProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH), null},

			// Scannables/Scannable Groups after a Detector or Monitor match unless they are followed a non scannable.
			// In this case, we are definitely after the scan path definitions section so such a sequence is illegal
			{"singleDetectorFollowedByScannableThenMonitorMatchesOnce",
			ArrayUtils.addAll(pathsStem, d1Proc, num1Proc, s1Proc, m1Proc), ArrayUtils.addAll(expectedStem, false, false, MATCH, false), null},
			{"MonitorFollowedByScannableGroupThenMonitorMatchesOnce",
			ArrayUtils.addAll(pathsStem, m1Proc, sGProc, m1Proc), ArrayUtils.addAll(expectedStem, false, MATCH, false), null},
			{"singleDetectorFollowedByScannableGroupThenMonitorMatchesOnce",
			ArrayUtils.addAll(pathsStem, d1Proc, num1Proc, sGProc, m1Proc), ArrayUtils.addAll(expectedStem, false, false, MATCH, false), null},
			{"MonitorFollowedByScannableThenMonitorMatchesOnce",
			ArrayUtils.addAll(pathsStem, m1Proc, s1Proc, m1Proc), ArrayUtils.addAll(expectedStem, false, MATCH, false), null},
			{"singleDetectorFollowedByScannableThenNumberIllegal",
			ArrayUtils.addAll(pathsStem, d1Proc, s1Proc, num1Proc), expectedStem, IllegalArgumentException.class},
			{"singleMonitorFollowedByScannableGroupThenNumberIllegal",
			ArrayUtils.addAll(pathsStem, m1Proc, sGProc, num1Proc), expectedStem, IllegalArgumentException.class},
			{"singleDetectorFollowedByScannableGroupThenPathIllegal",
			ArrayUtils.addAll(pathsStem, d1Proc, sGProc, gridProc), expectedStem, IllegalArgumentException.class},
			{"singleMonitorFollowedByScannableThenPathIllegal",
			ArrayUtils.addAll(pathsStem, m1Proc, s1Proc, gridProc), expectedStem, IllegalArgumentException.class},
			{"singleDetectorFollowedByScannableThenRoiIllegal",
			ArrayUtils.addAll(pathsStem, d1Proc, s1Proc, rectProc), expectedStem, IllegalArgumentException.class},
			{"singleMonitorFollowedByScannableThenRoiIllegal",
			ArrayUtils.addAll(pathsStem, m1Proc, s1Proc, rectProc), expectedStem, IllegalArgumentException.class},
			{"singleDetectorFollowedByValidEntriesThenScannableThenNumberIllegal",
			ArrayUtils.addAll(pathsStem, d1Proc, s1Proc, m1Proc, d1Proc, num1Proc, s1Proc, num1Proc, d1Proc, d1Proc),
			ArrayUtils.addAll(expectedStem, false, MATCH, false, false, false), IllegalArgumentException.class},

			// 'pure' scannable as third to last entry with no Detectors or Monitors before it match if not followed
			// by non scannables (potential new scan path def)
			{"scannableFollowedByDetectorThenNumberMatchesOnce",
			ArrayUtils.addAll(pathsStem, s1Proc, d1Proc, num1Proc), ArrayUtils.addAll(expectedStem, MATCH, false, false), null},
			{"scannableFollowedByDetectorThenRoiMatchesOnce",
			ArrayUtils.addAll(pathsStem, s1Proc, d1Proc, rectProc), ArrayUtils.addAll(expectedStem, MATCH, false, false), null},
			{"scannableFollowedByDetectorThenScanpathMatchesOnce",
			ArrayUtils.addAll(pathsStem, s1Proc, d1Proc, gridProc), ArrayUtils.addAll(expectedStem, MATCH, false, false), null},
			{"threeScannablesInARowMatchesThreeTimes",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, s1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH), null},
			{"twoScannablesFollowedByScannableGroupMatchesThreeTimes",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, sGProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH), null},
			{"scannableFollowedByScannableGroupThenScannableMatchesThreeTimes",
			ArrayUtils.addAll(pathsStem, s1Proc, sGProc, s1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH), null},
			{"scannableFollowedByTwoScannableGroupsMatchesThreeTimes",
			ArrayUtils.addAll(pathsStem, s1Proc, sGProc, sGProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH), null},
			{"twoScannablesFollowedByDetectorMatchesTwice",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, d1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false), null},
			{"scannableFollowedByScannableGroupThenDetectorMatchesTwice",
			ArrayUtils.addAll(pathsStem, s1Proc, sGProc, d1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false), null},
			{"twoScannableFollowedByMonitorMatchesTwice",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, m1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false), null},
			{"scannableFollowedByScannableGroupThenMonitorMatchesTwice",
			ArrayUtils.addAll(pathsStem, s1Proc, sGProc, m1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false), null},
			{"twoScannablesFollowedByNonScannableNoMatch",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, num1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH, NO_MATCH, false), null},
			{"scannableFollowedByScannableGroupThenNonScannableNoMatch",
			ArrayUtils.addAll(pathsStem, s1Proc, sGProc, num1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH, NO_MATCH, false), null},
			{"scannableFollowedByRoiThenNumberNoMatch",
			ArrayUtils.addAll(pathsStem, s1Proc, rectProc, num1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH, false, false), null},
			{"scannableFollowedByNumberThenRoiNoMatch",
			ArrayUtils.addAll(pathsStem, s1Proc, num1Proc, rectProc), ArrayUtils.addAll(expectedStem, NO_MATCH, false, false), null},
			{"scannableFollowedByNumberThenScanpathNoMatch",
			ArrayUtils.addAll(pathsStem, s1Proc, num1Proc, gridProc), ArrayUtils.addAll(expectedStem, NO_MATCH, false, false), null},

			// scannable group as third to last entry with no Detectors or Monitors before it match if not followed
			// by non scannables (potential new scan path def)
			{"scannableGroupFollowedByDetectorThenNumberMatchesOnce",
			ArrayUtils.addAll(pathsStem, sGProc, d1Proc, num1Proc), ArrayUtils.addAll(expectedStem, MATCH, false, false), null},
			{"scannableGroupFollowedByDetectorThenRoiMatchesOnce",
			ArrayUtils.addAll(pathsStem, sGProc, d1Proc, rectProc), ArrayUtils.addAll(expectedStem, MATCH, false, false), null},
			{"ScannableGroupFollowedByDetectorThenScanpathMatchesOnce",
			ArrayUtils.addAll(pathsStem, sGProc, d1Proc, gridProc), ArrayUtils.addAll(expectedStem, MATCH, false, false), null},
			{"scannableGroupFollowedByTwoScannablesMatchesThreeTimes",
			ArrayUtils.addAll(pathsStem, sGProc, s1Proc, s1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH), null},
			{"scannableGroupFollowedByScannableThenScannableGroupMatchesThreeTimes",
			ArrayUtils.addAll(pathsStem, sGProc, s1Proc, sGProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH), null},
			{"twoScannableGroupsThenScannableMatchesThreeTimes",
			ArrayUtils.addAll(pathsStem, sGProc, sGProc, s1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH), null},
			{"threeScannableGroupsMatchesThreeTimes",
			ArrayUtils.addAll(pathsStem, sGProc, sGProc, sGProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH), null},
			{"scannableGroupFollowedByScannableThenDetectorMatchesTwice",
			ArrayUtils.addAll(pathsStem, sGProc, s1Proc, d1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false), null},
			{"twoScannableGroupsThenDetectorMatchesTwice",
			ArrayUtils.addAll(pathsStem, sGProc, sGProc, d1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false), null},
			{"scannableGroupFollowedByScannableThenMonitorMatchesTwice",
			ArrayUtils.addAll(pathsStem, sGProc, s1Proc, m1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false), null},
			{"twoScannableGroupsThenMonitorMatchesTwice",
			ArrayUtils.addAll(pathsStem, sGProc, sGProc, m1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false), null},
			{"scannableGroupFollowedByScannableThenNonScannableNoMatch",
			ArrayUtils.addAll(pathsStem, sGProc, s1Proc, num1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH, NO_MATCH, false), null},
			{"twoScannableGroupsThenNonScannableNoMatch",
			ArrayUtils.addAll(pathsStem, sGProc, sGProc, num1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH, NO_MATCH, false), null},
			{"scannableGroupFollowedByRoiThenNumberNoMatch",
			ArrayUtils.addAll(pathsStem, sGProc, rectProc, num1Proc), ArrayUtils.addAll(expectedStem, NO_MATCH, false, false), null},
			{"scannableGroupFollowedByNumberThenRoiNoMatch",
			ArrayUtils.addAll(pathsStem, sGProc, num1Proc, rectProc), ArrayUtils.addAll(expectedStem, NO_MATCH, false, false), null},
			{"scannableGroupFollowedByNumberThenScanpathNoMatch",
			ArrayUtils.addAll(pathsStem, sGProc, num1Proc, gridProc), ArrayUtils.addAll(expectedStem, NO_MATCH, false, false), null},

			// scannable as fourth to last entry with no Detectors or Monitors before it match if not followed
			// by non scannables (potential new scan path def). This will be covered by previous cases but now also
			// have the possibility of three scannables/groups in a row followed by something. If we have three in a
			// row we are definitely not in the scan path definition section as this sequence is invalid. Thus any
			// such sequence followed by a non scannable is cannot be valid in either section and is hence illegal In
			// this case
			{"twoScannableFollowedByDetectorThenNumberMatches",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, d1Proc, num1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false, false), null},
			{"twoScannableFollowedByMonitorThenNumberMatches",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, m1Proc, num1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false, false), null},
			{"threeScannablesFollowedByNumberIllegal",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, s1Proc, num1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false, false), IllegalArgumentException.class},
			{"threeScannablesFollowedByPathIllegal",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, s1Proc, gridProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false, false), IllegalArgumentException.class},
			{"threeScannablesFollowedByRoiIllegal",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, s1Proc, rectProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false, false), IllegalArgumentException.class},
			{"threeScannablesFollowedByAnotherMatches",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, s1Proc, s1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH, MATCH), null},
			{"threeScannablesFollowedByScannableGroupMatches",
			ArrayUtils.addAll(pathsStem, s1Proc, s1Proc, s1Proc, sGProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, MATCH, MATCH), null},
			{"mixtureOfScannablesand ScannableGroupsFollowedByNumberIllegal",
			ArrayUtils.addAll(pathsStem, s1Proc, sGProc, sGProc, num1Proc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false, false), IllegalArgumentException.class},
			{"mixtureOfScannablesand ScannableGroupsFollowedByPathIllegal",
			ArrayUtils.addAll(pathsStem, sGProc, s1Proc, sGProc, gridProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false, false), IllegalArgumentException.class},
			{"mixtureOfScannablesand ScannableGroupsFollowedByRoiIllegal",
			ArrayUtils.addAll(pathsStem, sGProc, sGProc, s1Proc, rectProc), ArrayUtils.addAll(expectedStem, MATCH, MATCH, false, false), IllegalArgumentException.class},

			// scannable  at any other previous position with no Detectors or Monitors before it match if not followed
			// by non scannables (potential new scan path def). This will be covered by previous cases but now also
			// have the possibility of three scannables/groups in a row followed by something. If we have three in a
			// row we are definitely not in the scan path definition section as this sequence is invalid. Thus any
			// such sequence followed by a non scannable is cannot be valid in either section and is hence illegal
		});
	}

	@SuppressWarnings("unused")
	public ReadoutCheckerTest(String throwAwayTestName,
			IClauseElementProcessor[] scan, boolean[] expectedResults, Class<? extends Exception> expectedException) {
		this.scan = Arrays.asList(scan);
		this.expectedResults = expectedResults;
		this.expectedException = expectedException;
	}

	@Test
	public void paramTest() throws Exception {
		if (expectedException != null) {
			thrown.expect(expectedException);
			test();
		} else {
			assertThat(test(), is(true));
		}
	}

	private boolean test() {
		target = new ReadoutChecker(scan);
		int index = 0;
		boolean result;
		boolean afterScanPaths = false;
		for (IClauseElementProcessor processor : scan) {
			if (processor.hasDetector() || processor.hasMonitor()) {
				afterScanPaths = true;
				index++;
				continue;
			}
			result = target.isAPureScannableUsedAsReadout(processor, index, afterScanPaths);
			afterScanPaths = afterScanPaths ? true : result;
			assertThat(result, is(expectedResults[index]));
			index++;
		}
		return true;

	}
}
