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

package gda.mscan.processor;

import java.util.List;

import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.mscan.ClausesContext;

/**
 * A Clause Element Processor for {@link ScannableGroup} elements
 */
public class ScannableGroupElementProcessor extends ElementProcessorBase<ScannableGroup> {

	public ScannableGroupElementProcessor(final ScannableGroup source) {
		super(source);
	}

	/**
	 * Confirm that a {@link Scannable} is allowed as the next type to be processed in the MScan Clause grammar
	 * and if so, unpack the {@link Scannable}s from the group used in construction and add them to the context's
	 * list. A maximum group size of two is currently allowed.
	 *
	 * @param context	The {@link ClausesContext} object being completed for the current MSCan clause
	 * @param index		The index of the clause element associated with the processor within the current clause
	 *
	 * @throws			IllegalStateException if the previous element of the context is null (this should never occur)
	 * 					IllegalArgumentException if there is no list of successors corresponding to the type of the
	 * 					previous element i.e. it is not a valid element type
	 */
	@Override
	public void process(final ClausesContext context,
			final List<IClauseElementProcessor> clauseProcessors, final int index) {
		if(isValidElement(context, this.getClass().getName(), Scannable.class)) {
			// Must use asList implementation else some Mocked tests NullPointer
			if (enclosed.getGroupMembers().size() > 2) {
				throw new UnsupportedOperationException(
						"The maximum supported number of Scannable Group members (2) has been exceeded");
			}
			for (Scannable member : enclosed.getGroupMembers()) {
				context.addScannable(member);
			}
		}
	}

	@Override
	public boolean hasScannable() {
		return true;
	}

	@Override
	public boolean hasScannableGroup() {
		return true;
	}

	/**
	 * Retrieve the name of the enclosed {@link ScannableGroup}
	 *
	 * @return the name of the enclosed {@link ScannableGroup}
	 */
	@Override
	public String getElementValue() {
		return enclosed.getName();
	}
}
