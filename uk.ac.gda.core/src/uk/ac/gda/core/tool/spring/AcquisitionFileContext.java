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

package uk.ac.gda.core.tool.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Defines the application file structure as <a href="https://confluence.diamond.ac.uk/display/DIAD/File+System">detailed in Confluence</a>
 *
 * At the moment contains only references to essential URLs. In future will contain reference to the whole beamline
 * configuration (stages, cameras, other)
 *
 * @author Maurizio Nagni
 */
@Component
public class AcquisitionFileContext {

	@Autowired
	private DiffractionFileContext diffractionContext;

	@Autowired
	private TomographyFileContext tomographyContext;

	@Autowired
	private ExperimentFileContext experimentContext;

	/**
	 * The diffraction file structure context
	 * @return the diffraction file context
	 */
	public DiffractionFileContext getDiffractionContext() {
		return diffractionContext;
	}

	/**
	 * The tomography file structure context
	 * @return the tomography file context
	 */
	public TomographyFileContext getTomographyContext() {
		return tomographyContext;
	}

	/**
	 * The experiment file structure context
	 * @return the experient file context
	 */
	public ExperimentFileContext getExperimentContext() {
		return experimentContext;
	}
}
