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

package gda.analysis.functions;

import gda.analysis.DataSet;
import gda.analysis.TerminalPrinter;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;


/**
 * Class that wraps the double-step function: 
 * 
 * <pre>
 *            _______
 *           |       |
 *       ____|       |_______
 * _____|                    |_________
 * 
 * </pre>
 * 
 * 
 * Parameters are
 * <ol>
 *   <li>base</li>
 *   <li>start of outer step</li>
 *   <li>end of outer step</li>
 *   <li>step from base</li>
 *   <li>step from outer</li>
 *   <li>width of inner step as fraction of outer step</li>
 *   <li>offset of inner step as fraction of remaining outer step (0 = left justified, 1 = right)</li>
 * </ol>
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.Step}
 */
@Deprecated
public class Step extends uk.ac.diamond.scisoft.analysis.fitting.functions.Step implements IFunction {

	public Step(IParameter[] params) {
		super(params);
	}

	/**
	 * Constructor that allows for the positioning of all the parameter bounds
	 * 
	 * @param minY
	 *            minimum Y value
	 * @param maxY
	 *            maximum Y value
	 * @param minX1
	 *            minimum X1 value
	 * @param maxX1
	 *            maximum X1 value
	 * @param minX2
	 *            minimum X2 value
	 * @param maxX2
	 *            maximum X2 value
	 * @param minH1
	 *            minimum height of outer peak/trough
	 * @param maxH1
	 *            maximum height outer peak/trough
	 * @param minH2
	 *            minimum height of inner peak/trough
	 * @param maxH2
	 *            maximum height inner peak/trough
	 * @param minW
	 *            minimum proportional width of inner peak/trough ( 0 < width < 1 )
	 * @param maxW
	 *            maximum proportional width of inner peak/trough ( 0 < width < 1 )
	 * @param minPos
	 *            minimum position of inner peak/trough with respect to outer one ( 0 (left) <= pos <= 1 (right))
	 * @param maxPos
	 *            maximum position of inner peak/trough with respect to outer one ( 0 (left) <= pos <= 1 (right))
	 */
	public Step(double minY, double maxY, double minX1, double maxX1, double minX2, double maxX2, double minH1,
			double maxH1, double minH2, double maxH2, double minW, double maxW, double minPos, double maxPos) {

		super(minY, maxY, minX1, maxX1, minX2, maxX2, minH1, maxH1, minH2, maxH2, minW, maxW, minPos, maxPos);
	}

	@Override
	public DataSet makeDataSet(DoubleDataset... values) {
		return DataSet.convertToDataSet(makeSerialDataset(values));
	}

	@Override
	public void disp() {
		TerminalPrinter.print(toString());
	}
}
