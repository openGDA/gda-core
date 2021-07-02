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

package uk.ac.diamond.daq.service.context;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import uk.ac.diamond.daq.experiment.structure.ExperimentStructureJobResponder;
import uk.ac.diamond.daq.experiment.structure.ExperimentTreeCache;

/**
 * Configure the spring environment for NexusExperimentControllerTest
 * @author Maurizio Nagni
 */
@Configuration
@ComponentScan(basePackages = {"uk.ac.gda.core.tool.spring", "uk.ac.diamond.daq.experiment.structure"},
								excludeFilters = {
										@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE,
												value = ExperimentTreeCache.class),
										@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE,
										value = ExperimentStructureJobResponder.class)
								})
public class AcquisitionFileContextTestConfiguration {

	@Bean
	public ExperimentTreeCache experimentTreeCache() {
		return Mockito.mock(ExperimentTreeCache.class);
	}

	@Bean
	public ExperimentStructureJobResponder experimentStructureJobResponder() {
		return Mockito.mock(ExperimentStructureJobResponder.class);
	}

}
