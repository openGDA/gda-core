/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.mscan.processor;

import java.util.List;

import gda.mscan.ClausesContext;
import gda.mscan.element.ScanDataConsumer;

/**
 * A Clause Element Processor for {@link ScanDataConsumer} elements
 */
public class ScanDataConsumerElementProcessor extends ElementProcessorBase<ScanDataConsumer> {

	public ScanDataConsumerElementProcessor(final ScanDataConsumer source) {
		super(source);
	}

	/**
	 * Confirms that both scan path(s) and detector(s) have been defined to produce output to consume and also checks
	 * the constituents of the consumer definition before adding them to the current {@link ClausesContext}
	 */
	@Override
	public void process(ClausesContext context, final List<IClauseElementProcessor> clauseProcessors, int index) {
		throwIf(!context.isScanPathSeen(), "No scan path defined - Nothing to consume");
		throwIf(!context.isDetectorClauseSeen(), "No output specified - Nothing to consume");
		throwIf(clauseProcessors.size() != 2, "Incorrect number of parameters for ScanDataConsumer, must be 1");
		IClauseElementProcessor procTwo = withNullProcessorCheck(clauseProcessors.get(1));
		throwIf(!procTwo.hasTokenString(), "2nd element of unexpected type in Scan Consumer clause");
		context.addScanDataConsumer(enclosed, procTwo.getElementValue());
	}

	@Override
	public String getElementValue() {
		return enclosed.name();
	}

	@Override
	public boolean hasScanDataConsumer() {
		return true;
	}

}
