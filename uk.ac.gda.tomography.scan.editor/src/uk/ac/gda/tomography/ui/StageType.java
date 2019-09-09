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

package uk.ac.gda.tomography.ui;

import uk.ac.gda.tomography.base.TomographyMode.Stage;
import uk.ac.gda.tomography.ui.mode.TomographyBaseMode;
import uk.ac.gda.tomography.ui.mode.TomographyDefaultMode;
import uk.ac.gda.tomography.ui.mode.TomographyTR6Mode;

public enum StageType {
	DEFAULT("Default", new TomographyDefaultMode(Stage.DEFAULT)),
	TR6("TR6", new TomographyTR6Mode(Stage.TR6));

	private final TomographyBaseMode stage;
	private final String label;

	StageType(String label, TomographyBaseMode stage) {
		this.label = label;
		this.stage = stage;
	}

	public String getLabel() {
		return label;
	}

	public TomographyBaseMode getStage() {
		return stage;
	}
}