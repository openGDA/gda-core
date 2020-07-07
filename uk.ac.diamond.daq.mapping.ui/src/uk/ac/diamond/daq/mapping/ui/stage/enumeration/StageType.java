/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.stage.enumeration;

import uk.ac.diamond.daq.mapping.ui.stage.CommonStage;
import uk.ac.diamond.daq.mapping.ui.stage.GTSStage;
import uk.ac.diamond.daq.mapping.ui.stage.TR6Stage;

/**
 * Associates a specific implementation to a {@link Stage}
 */
public enum StageType {

	GTS(Stage.GTS, new GTSStage()),
	TR6(Stage.TR6, new TR6Stage());

	private final CommonStage stageImpl;
	private final Stage stage;

	StageType(Stage stage, CommonStage stageImpl) {
		this.stage = stage;
		this.stageImpl = stageImpl;
	}

	public Stage getStage() {
		return stage;
	}

	public CommonStage getCommonStage() {
		return stageImpl;
	}
}
