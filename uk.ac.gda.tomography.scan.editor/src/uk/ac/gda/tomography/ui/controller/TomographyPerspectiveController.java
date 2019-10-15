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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import uk.ac.gda.tomography.controller.AcquisitionControllerException;

/**
 * Controls the tomography perspective delegating its subsections to other specialised controllers.
 *
 * @author Maurizio Nagni
 */
@Controller
public class TomographyPerspectiveController {

	@Autowired
	private TomographyParametersAcquisitionController tomographyAcquisitionController;

	private static final Logger logger = LoggerFactory.getLogger(TomographyPerspectiveController.class);

	public TomographyParametersAcquisitionController getTomographyAcquisitionController() {
		// This conditional statement should be removed and managed by the controller when necessary
		if (Objects.isNull(tomographyAcquisitionController.getAcquisition())) {
			try {
				tomographyAcquisitionController.loadData(TomographyParametersAcquisitionController.createNewAcquisition());
			} catch (AcquisitionControllerException e) {
				logger.error("Cannot create the acquisition controller", e);
			}
		}
		return tomographyAcquisitionController;
	}
}
