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

import gda.mscan.ClausesContext;
import gda.mscan.element.RegionShape;
import gda.mscan.element.Scanpath;

/**
 * A Clause Element Processor for {@link RegionShape} elements
 */
public class RegionShapeElementProcessor extends ElementProcessorBase<RegionShape> {

	public RegionShapeElementProcessor(final RegionShape source) {
		super(source);
	}

	/**
	 * Confirm that a {@link RegionShape} is allowed as the next type to be processed in the MScan clause grammar
	 * and if so, set the {@link RegionShape} used in construction on the context object provided this is not the
	 * first element in the clause.
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
		if (!isStatic()) rejectIfFirstElement(index);
		if(isValidElement(context, this.getClass().getName())) {
			context.setRegionShape(enclosed);
			if (isStatic()) context.setScanpath(Scanpath.STATIC);
		}
	}

	@Override
	public boolean hasRoi() {
		return true;
	}

	@Override
	public boolean isStatic() {
		return enclosed == RegionShape.STATIC;
	}

	/**
	 * Retrieve the name of the enclosed {@link RegionShape}
	 *
	 * @return the name of the enclosed {@link RegionShape}
	 */
	@Override
	public String getElementValue() {
		return enclosed.name();
	}
}
