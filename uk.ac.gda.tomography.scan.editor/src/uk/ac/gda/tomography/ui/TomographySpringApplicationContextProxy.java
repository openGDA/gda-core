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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController;
import uk.ac.gda.tomography.ui.controller.TomographyPerspectiveController;

/**
 * As {@link TomographyPerspectiveController} and {@link TomographyParametersAcquisitionController} are driven by Spring there is no straight way for Eclipse to
 * access these beans. This class acts as proxy for those componets as Views or Perspectives which are directly managed by Eclipse and consequently cannot use
 * Spring's annotations as @Autowired
 *
 * @author Maurizio Nagni
 */
@Component
public class TomographySpringApplicationContextProxy implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		TomographySpringApplicationContextProxy.applicationContext = applicationContext;
	}

	public static TomographyPerspectiveController getTomographyPerspectiveController() {
		return TomographySpringApplicationContextProxy.applicationContext.getBean(TomographyPerspectiveController.class);
	}

}
