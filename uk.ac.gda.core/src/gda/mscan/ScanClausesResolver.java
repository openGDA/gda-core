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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.mscan.element.RegionShape;
import gda.mscan.processor.IClauseElementProcessor;

/**
 * Splits the incoming MScan command into separate clauses consisting of scan path definitions and/or detector/monitor
 * references.
 *
 * @since GDA 9.10
 */
public class ScanClausesResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScanClausesResolver.class);
	private static final int MIN_MSCAN_NON_SCANNABLE_TERMS = 5;

	private final int nArgs;
	private final List<IClauseElementProcessor> processors;
	private boolean monitorSeen, detectorSeen, readoutSeen;
	private int index = 0;

	public ScanClausesResolver(List<IClauseElementProcessor> elementProcessors) {
		processors = elementProcessors;
		nArgs = processors.size();
	}

	/**
	 * Iterates over the list of {@link IClauseElementProcessor}s used to construct this object detecting a new scan
	 * clause at the appropriate boundaries marked by the occurrence of a new {@link Scannable} under certain conditions.
	 * Not every new {@link Scannable} is a boundary as now the "mscan scannable1 scannable2......." form must be
	 * supported for mapping scans. This produces a {@link List} of {@link List}s of {@link IClauseElementProcessor}s
	 * that can then be individually handled. The class does not attempt to parse the contents of the clauses, just
	 * detect their boundaries so downstream objects must do this.
	 *
	 * @return A {@link List} of {@link List}s of {@link IClauseElementProcessor}s corresponding to the scan clauses
	 *
	 * @throws	IllegalArgumentException if an invalid set of {@link IClauseElementProcessor}s is supplied
	 */
	List<List<IClauseElementProcessor>> resolveScanClauses() {
		if (!(processors.get(0).hasScannable())) {
			throw new IllegalArgumentException("First term must be a scannable");
		}

		final List<List<IClauseElementProcessor>> clauses = new ArrayList<>();
		StringJoiner joiner = new StringJoiner(" ");
		List<IClauseElementProcessor> current = new ArrayList<>();
		ReadoutChecker checker = new ReadoutChecker(processors);

		for (index = 0; index < nArgs; index++) {
			if (index > 1 && clauses.isEmpty()) {
				throw new IllegalArgumentException(
						"Invalid MScan : your scan command does not start with a valid scan clause");
			}
			IClauseElementProcessor thisProcessor = processors.get(index);

			// a potential scan clause boundary, might be the first of a Scannable pair or a ScannableGroup
			// if it's a mapping scan, also have to cope with one or more Detectors or Monitors at end of line
			if (thisProcessor.hasScannable() || thisProcessor.hasDetector() || thisProcessor.hasMonitor()) {
				monitorSeen = monitorSeen ? true : thisProcessor.hasMonitor();     // Latch recognition of a Monitor
				detectorSeen = detectorSeen ? true : thisProcessor.hasDetector();  // Latch recognition of a Detector
				readoutSeen = readoutSeen ? true : checker.isAPureScannableUsedAsReadout(
													thisProcessor, index, afterScanPaths()); // or a Scannable stand-in;
				// Given we have Scannable, the criteria for a new clause is a recognised scan def boundary or that
				// we are in the detectors/monitors list. Single Scannables with no parameter (being used like
				// pseudo-Monitors, real Monitors and Detectors are checked first as they cannot be followed by scan
				// def clauses. The pseudo-Monitor case must be checked first so that the latching of Detectors and
				// Monitors does not bypass this, so that any trailing scan defs after these can be caught. Assuming
				// this test is passed we can then check for scan def clause boundaries proper.
				if (afterScanPaths() || isSPECScanBoundary() || isMScanBoundary(thisProcessor.hasScannableGroup())) {
					current = startNewClause(clauses, joiner);
				}
			} else if (thisProcessor.hasScanDataConsumer()) {
				if (!afterScanPaths()) {
					throw new IllegalArgumentException(
							"Invalid MScan : scans with no detectors or monitors cannot use templates or processors");
				}
				current = startNewClause(clauses, joiner);
			}


			joiner.add(thisProcessor.getElementValue());
			current.add(thisProcessor);
		}
		logClauseDetection(joiner);
		return Collections.unmodifiableList(clauses);
	}

	/**
	 * Logs the detection of a new clauses, creates a new {@link List} and adds it to the {@link List} of clauses to be
	 * filled in by the main resolution loop and alco creates a new holder for the clause logging messages.
	 *
	 * @param clauses	The {@link List} of clauses that belong to the scan being parsed
	 * @param joiner	A {@link StringJoiner} containg the message to be logged about the previous clause
	 * @return			A new {@link List} to receive the elements of the new clause.
	 */
	private List<IClauseElementProcessor> startNewClause(List<List<IClauseElementProcessor>> clauses, StringJoiner joiner) {
		logClauseDetection(joiner);
		List<IClauseElementProcessor> newList = new ArrayList<>();
		clauses.add(newList);
		joiner = new StringJoiner(" ");
		return newList;
	}

	/**
	 * If the next element is not a {@link Scannable} and not a {@link RegionShape}, (two of the three possibilities
	 * allowed by the clause grammar), then we have something of the form ...S1 Number..... This is potentially
	 * an old style SPEC scan clause covering e.g. S1 start stop step etc.: It is if its the first element in the
	 * array, otherwise it is if the previous element is not a {@link Scannable}.
	 *
	 * N.B. this will also catch the mscan case where the clause is ScanGrp Num Num.... i.e. where the Roi is not
	 * specified so the default for the path would be used as this is equivalent to a SPEC style scan using a
	 * Scannable Group. This should not matter though as we're only detecting boundaries here.
	 *
	 * @return				true if the current element represent a SPEC style scan clause boundary.
	 */
	private boolean isSPECScanBoundary() {
		boolean potential = (processors.get(index+1).hasNumber());
		boolean actual = (0 == index) ? potential : (potential && !processors.get(index-1).hasScannable());
		if (actual) {
			LOGGER.debug("Spec scan boundary detected at position {}", index);
		}
		return actual;
	}

	/**
	 * This method is to be evaluated after isSPECScanBoundary() so the possibility of an old style boundary
	 * will have been eliminated at this point.
	 * There are two main possible syntax cases for a mscan boundary: a {@link Scannable} followed by another
	 * followed by a {@link RegionShape} (or a {@link Number} in the default case) or a {@link ScannableGroup} followed by a {@link RegionShape}
	 * (or a {@link Number} in the default case - will in fact be caught by Spec Scan boundary detection as they're equivalent).
	 * Either will return true. The method also evaluates if the remaining array length is less than the minimum possible length
	 * for an mscan clauses throwing if so.
	 *
	 * @return		true if a MScan boundary case is recognised.
	 *
	 * @throws		IllegalArgumentException for invalid sequences of {@link IClauseElementProcessor}s
	 */
	private boolean isMScanBoundary(final boolean scannableGroupCase) {
		int adjustedIndex = index + (scannableGroupCase ? 0 : 1);
		if (adjustedIndex + MIN_MSCAN_NON_SCANNABLE_TERMS <= nArgs) {
			// detect a lone scannable incorrectly followed by a spec scan def
			int i = 1;
			while (i < MIN_MSCAN_NON_SCANNABLE_TERMS) {
				IClauseElementProcessor processor = processors.get(adjustedIndex + i++);
				if (processor.hasScannable()) {
					throw new IllegalArgumentException(
						"The scan command is incorrect - MScan path definition can't contain more than two Scannables");
				}
			}
			// first test for default arrangement: (mscan) S1 S2 Region.......
			IClauseElementProcessor nextProcessor = processors.get(adjustedIndex);
			IClauseElementProcessor nextButOneProcessor = processors.get(adjustedIndex + 1);
			boolean detected2D = nextProcessor.hasScannable() &&
												(nextButOneProcessor.hasRoi() || nextButOneProcessor.hasNumber());
			// then for axial case: (mscan) S1 AxialRegion Number......
			boolean detected1D = (nextProcessor.hasRoi() && nextButOneProcessor.hasNumber() &&
												((RegionShape)nextProcessor.getElement()).equals(RegionShape.AXIAL));
			if (detected2D || detected1D) {
				LOGGER.debug("MScan boundary detected at position {}", index);
			}
			return detected2D || detected1D;
		} else {
			throw new IllegalArgumentException("The scan command is incorrect - final scan path definition is invalid.");
		}
	}

	/**
	 * Aggregate of Detector/Monitor/ScannableReadout detection status
	 *
	 * @return		true if either a {@link Scannable} used as a readout, a {@link Monitor} or a {@link Detector} has
	 * 				been detected in the MScan command sequence.
	 */
	private boolean afterScanPaths() {
		return readoutSeen || monitorSeen || detectorSeen;
	}

	private void logClauseDetection(final StringJoiner joiner) {
		if (joiner.length() > 0) {
			LOGGER.info("MScan clause detected {}", joiner);
		}
	}
}
