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

package uk.ac.gda.tomography.ui.controller;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.ui.mode.TomographyBaseMode;
import uk.ac.gda.tomography.ui.mode.TomographyDefaultMode;
import uk.ac.gda.tomography.ui.mode.TomographyTR6Mode;

/**
 * Controls the tomography perspective delegating its subsections to other specialised controllers.
 *
 * @author Maurizio Nagni
 */
public class TomographyPerspectiveController {

	public enum StageType {
		DEFAULT("Default", new TomographyDefaultMode()),
		TR6("TR6", new TomographyTR6Mode());

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

	private TomographyParametersAcquisitionController tomographyAcquisitionController;

	private static final Logger logger = LoggerFactory.getLogger(TomographyPerspectiveController.class);

	public TomographyParametersAcquisitionController getTomographyAcquisitionController() {
		if (Objects.isNull(tomographyAcquisitionController)) {
			tomographyAcquisitionController = new TomographyParametersAcquisitionController();
			try {
				tomographyAcquisitionController.loadData(TomographyParametersAcquisitionController.createNewAcquisition());
			} catch (AcquisitionControllerException e) {
				logger.error("Cannot create the acquisition controller", e);
			}
		}
		return tomographyAcquisitionController;
	}
}
