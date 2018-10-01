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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.mscan.processor.IClauseElementProcessor;


/**
 * Encapsulates a recursive function which determines if the current {@link Scannable} or {@link ScannableGroup} in
 * the MScan command is being used as a readout (like a monitor). At the simplest level this is true if it is
 * followed by a {@link Detector} or {@link Monitor}, or by another {{@link Scannable}/{@link ScannableGroup}.
 * However this is only true if we are already within or at the beginning of the section of the command string
 * that is after all the scan path definitions, since two {@link Scannable}s one after another could be the start
 * of a valid mapping scan path definition.
 *
 * Because of this, the rule is enforced that the overall scan command must be made up of a first section that
 * contains only scan path definitions and no {@link Monitor}s, {@link Detector}s or {link @Scannable} readouts
 * and a second section for which the reverse is true. This means that {{@link #isAPureScannableUsedAsReadout} must
 * be called after the current {@link Scannable} has been checked to see if it is the start of a SPEC style scan or
 * a Mapping one, since it operates on the basis that these cases have already been eliminated.
 *
 * @since GDA 9.10
 */
final class ReadoutChecker {

	private static final Logger logger = LoggerFactory.getLogger(ReadoutChecker.class);

	private int nArgs;
	private List<IClauseElementProcessor> processors;

	/**
	 * Constructor
	 *
	 * @param elementProcessors		The list of {@link IClauseElementProcessor}s that corresponds to the MScan command
	 */
	public ReadoutChecker(final List<IClauseElementProcessor> elementProcessors) {
		processors = elementProcessors;
		nArgs = processors.size();
	}
	/**
	 * Check whether the current element is a 'pure' {@link Scannable} (or {@link ScannableGroup}) being used as a
	 * readout device (i.e. like a {@link Monitor} or {@link Detector} without actually being one. This would true if:
	 * a) it's the final one in the command
	 * b) it's the last but one and the final one is a {@link Scannable} (possibly also a {@link Detector} or {@link Monitor})
	 * c) it's neither a) nor b) and is followed by a {@link Scannable} or {@link Monitor} and then another
	 * {@link Scannable} based class or by a {@link Detector} followed by a {@link Number} or another {@link Scannable}
	 * based class
	 *
	 * Though the method will reject non-{@link Scannable}s it's more efficient to only call it when you know you have
	 * one. If thisProcessor does not refer to a {@link Scannable} or {@link ScannableGroup} it will return false
	 * straight away otherwise it will pass the current processor to the recursive {{@link #checkProcessor} method which
	 * will examine it and the subsequent processors in the command. The depth integer is maintained within the function
	 * so that is is bounded at a max of 3 iterations for both the success and fail outcomes.
	 *
	 * @return 		true if {@code thisProcessor} refers to a {@link Scannable} or {@link ScannableGroup} being used as
	 * 				a readout.
	 *
	 * @throws		IllegalArgumentException if an illegal sequence is detected
	 */
	final boolean isAPureScannableUsedAsReadout(final IClauseElementProcessor thisProcessor,
													final int index, final boolean afterScanPaths) {
		if (thisProcessor.hasScannable() && !thisProcessor.hasDetector() && !thisProcessor.hasMonitor()) {
			// current element is a scannable but is also neither a detector nor a monitor
			boolean detected = checkProcessor(thisProcessor, index, afterScanPaths, 0);
			if (detected) {
				logger.debug("Scannable used as readout detected at position {}", index);
			}
			return  detected;
		}
		return false;
	}

	/**
	 * Check the currently referenced processor; because this method is recursive, it might not be a 'pure'
	 * {@link Scannable} processor or indeed a {@link Scannable} one at all, unless depth is 0. The method is limited
	 * to being recursively called 3 times (a sequence of 3 {@link Scannable}s/{@link ScannableGroup}s after which, if
	 * the current processor is also a {@link Scannable} or {@link ScannableGroup} it will return success. If this is
	 * not the case after 3 iterations the sequence is invalid and an {@link IllegalArgumentException} is thrown. The
	 * method can also exit early if the last {@link IClauseElementProcessor} in the command sequence is reached or if
	 * the last two {@link IClauseElementProcessor}s in the sequence refere to {@link Scannable}s/{@link ScannableGroup}s
	 *
	 * The depth parameter keeps track of this and must be set to 0 when the method is first called. Additionally the
	 * afterScanPaths flag is considered, this should be true when the caller knows the current {@link IClauseElementProcessor}
	 * is already within the non-scan path defnition section of the command sequence. When this is the case it is
	 * already certain that a {@link Scannable}/{@link ScannableGroup} followed by a non {@link Scannable} is an illegal
	 * sequence whereas before this is true it could merely mark the beginning of a new scan path definition
	 *
	 * @param thisProcessor		The {@link IClauseElementProcessor} currently being examined
	 * @param index				The position of thisPorcessor within the command sequence
	 *
	 * @return					true if the current {@link IClauseElementProcessor} refers to a @link Scannable}/
	 * 							{@link ScannableGroup} being used as a readout false otherwise
	 *
	 * @throws					IllegalArgumentException if an illegal sequence is detected
	 */
	private boolean checkProcessor(final IClauseElementProcessor thisProcessor,
									final int index, final boolean afterScanPaths, int depth) {
		if (!thisProcessor.hasScannable()) {
			throw new IllegalArgumentException("checkProcessor must only be called on Scannable Processors");
		}
		// First check if this is the last processor entry in the command. If it is and (only) if it references a simple
		// scannable/scannable group, then that's fine and we can return success, otherwise throw as non scannables  are
		// invalid, provided this is not the first time through the check. This is true since it would mean having a
		// pure scannable followed by a non-scannable or a scannable group as the final element. The only things that
		// are valid following a pure scannable in the last position are another one or a detector or a monitor.
		if (index == nArgs -1) {
			return true;
		}
		depth++;
		IClauseElementProcessor next = processors.get(index + 1);
		// pure scannable followed by detector or monitor
		if (next.hasDetector() || next.hasMonitor()) {
			return true;
		}
		// pure scannable followed by pure scannable - could be a potential mapping scan definition
		if (next.hasScannable()) {
			// unless they're the last two entries or we have 3 in a row followed by another or a monitor or a detector
			// which guarantees we are no longer on the scan paths section
			if (depth > 2 || index == nArgs - 2) {
				return true;
			} else {
				return checkProcessor(next, index + 1, afterScanPaths, depth);
			}
		// pure scannable followed non- scannable processor; only valid for spec/mapping scan defs provided we're
		// not in the section containing monitors, detectors and readout scannables. This also means a sequence of
		// 3 scannables before this section is invalid
		} else if (afterScanPaths || depth > 2) {
			throw new IllegalArgumentException(
					"Your mScan command contains an invalid sequence in the Detectors/Monitors section.");
		}
		return false;
	}
}
