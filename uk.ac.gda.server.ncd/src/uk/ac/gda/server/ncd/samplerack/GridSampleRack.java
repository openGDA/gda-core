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

import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.MotorException;

public class GridSampleRack extends SampleRackBase {
	static final Logger logger = LoggerFactory.getLogger(GridSampleRack.class);
	private static final Pattern CELL_REGEX = Pattern.compile("(?<column>[a-zA-Z])(?<row>\\d+)");

	private int rows;
	private int columns;

	@Override
	protected void scanSample(Sample sample) throws SampleRackException, MotorException {
		var cell = Cell.from(sample.getCell()).orElseThrow(() -> new IllegalArgumentException("Sample location is not valid"));
		terminalPrinter.print("Moving sample rack to position " + sample.getCell());
		var currentxPos = rackConfiguration.columnPosition(cell.column);
		var currentyPos = rackConfiguration.rowPosition(cell.row);
		logger.debug("Moving {} to {}", rackConfiguration.xColumnPositioner().getName(), currentxPos);
		logger.debug("Moving {} to {}", rackConfiguration.yRowPositioner().getName(), currentyPos);
		terminalPrinter.print(String.format("    %s: %f, %s: %f", rackConfiguration.yRowPositioner().getName(), currentyPos,
				rackConfiguration.xColumnPositioner().getName(), currentxPos));

		retryingMove(sample, rackConfiguration.xColumnPositioner(), currentxPos, 3);
		retryingMove(sample, rackConfiguration.yRowPositioner(), currentyPos, 3);
		runner.runScan(sample, rackConfiguration.yRowPositioner(), rackConfiguration.xColumnPositioner());
	}

	@Override
	public boolean validLocation(String element) {
		return Cell.from(element).isPresent();
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	private static class Cell {
		final int row;
		final int column;

		public Cell(int row, int column) {
			this.row = row;
			this.column = column;
		}

		static Optional<Cell> from(String location) {
			var matcher = CELL_REGEX.matcher(location);
			if (matcher.matches()) {
				var column = matcher.group("column").toUpperCase().charAt(0) - 'A';
				var row = Integer.parseInt(matcher.group("row"))-1;
				return Optional.of(new Cell(row, column));
			} else {
				return Optional.empty();
			}
		}
	}
}
