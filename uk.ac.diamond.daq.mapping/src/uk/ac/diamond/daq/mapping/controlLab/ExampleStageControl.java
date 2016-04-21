/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.controlLab;

import gda.device.Scannable;
import gda.factory.Finder;

public class ExampleStageControl {

	private final Scannable stage_x;
	private final Scannable stage_y;

	public ExampleStageControl() {
		// Use the finder to populate the fields
		stage_x = Finder.getInstance().find("stage_x");
		stage_y = Finder.getInstance().find("stage_y");
	}

	public Scannable getStage_x() {
		return stage_x;
	}

	public Scannable getStage_y() {
		return stage_y;
	}

}
