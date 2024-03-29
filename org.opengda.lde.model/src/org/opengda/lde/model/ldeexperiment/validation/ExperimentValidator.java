/*******************************************************************************
 * Copyright © 2009, 2015 Diamond Light Source Ltd
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
 *
 * Contributors:
 * 	Diamond Light Source Ltd
 *******************************************************************************/
/**
 *
 * $Id$
 */
package org.opengda.lde.model.ldeexperiment.validation;

import org.eclipse.emf.common.util.EList;

import org.opengda.lde.model.ldeexperiment.Stage;

/**
 * A sample validator interface for {@link org.opengda.lde.model.ldeexperiment.Experiment}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface ExperimentValidator {
	boolean validate();

	boolean validateName(String value);
	boolean validateStage(EList<Stage> value);

	boolean validateStages(EList<Stage> value);
	boolean validateDescription(String value);

	boolean validateNumberOfStages(int value);

	boolean validateFilename(String value);
}
