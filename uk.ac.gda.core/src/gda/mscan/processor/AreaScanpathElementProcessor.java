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

import gda.device.Scannable;
import gda.mscan.ClauseContext;
import gda.mscan.element.AreaScanpath;

/**
 * A Clause Element Processor for {@link AreaScanpath} elements
 */
public class AreaScanpathElementProcessor extends ElementProcessorBase<AreaScanpath> {

	public AreaScanpathElementProcessor(final AreaScanpath source) {
		super(source);
	}

	/**
	 * Confirm that a {@link AreaScanpath} is allowed as the next type to be processed in the MScan clause grammar
	 * and if so, set the {@link AreaScanpath} used in construction on the context object, provided this is not the
	 * first element in the clause as the first element should always be a {@link Scannable}.
	 *
	 * @param context	The {@link ClauseContext} object being completed for the current MSCan clause
	 * @param index		The index of the clause element associated with the processor within the current clause
	 *
	 * @throws			IllegalStateException if the previous element of the context is null (this should never occur)
	 * 					IllegalArgumentException if there is no list of successors corresponding to the type of the
	 * 					previous element i.e. it is not a valid element type
	 */
	@Override
	public void process(final ClauseContext context, final int index) {
		rejectIfFirstElement(index);
		if(isValidElement(context, this.getClass().getName())) {
			context.setAreaScanpath(enclosed);
		}
	}

	/**
	 * Retrieve the name of the enclosed {@link AreaScanpath}
	 *
	 * @return the name of the enclosed {@link AreaScanpath}
	 */
	@Override
	public String getElementValue() {
		return enclosed.name();
	}
}
