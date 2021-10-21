/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.samplerack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.MotorException;

public class LadderSampleRack extends SampleRackBase {
	static final Logger logger = LoggerFactory.getLogger(LadderSampleRack.class);

	private int cells;

	@Override
	protected void scanSample(Sample sample) throws SampleRackException, MotorException {
		var cell = Integer.parseInt(sample.getCell())-1;
		terminalPrinter.print("Moving sample rack to position " + cell);
		var currentCellxPosition = rackConfiguration.columnPosition(cell);
		logger.debug("Moving {} to {}", rackConfiguration.xColumnPositioner().getName(), currentCellxPosition);
		var currentCellyPosition = rackConfiguration.rowPosition(cell);
		logger.debug("Moving {} to {}", rackConfiguration.yRowPositioner().getName(), currentCellyPosition);
		terminalPrinter.print(String.format("    %s: %f, %s: %f", rackConfiguration.yRowPositioner().getName(), currentCellyPosition,
				rackConfiguration.xColumnPositioner().getName(), currentCellxPosition));

		retryingMove(sample, rackConfiguration.xColumnPositioner(), currentCellxPosition, 3);
		retryingMove(sample, rackConfiguration.yRowPositioner(), currentCellyPosition, 3);
		runner.runScan(sample, rackConfiguration.xColumnPositioner(), rackConfiguration.yRowPositioner());
	}

	@Override
	public boolean validLocation(String element) {
		try {
			var loc = Integer.valueOf(element);
			// 1-index sample positions
			return loc > 0 && loc <= cells;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	public int getCells() {
		return cells;
	}

	public void setCells(int cells) {
		this.cells = cells;
	}
}