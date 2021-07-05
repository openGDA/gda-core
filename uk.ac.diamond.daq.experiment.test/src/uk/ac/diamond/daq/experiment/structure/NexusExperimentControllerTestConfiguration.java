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

package uk.ac.diamond.daq.experiment.structure;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;



/**
 * Configure the spring environment for testing {@link NexusExperimentController}
 * @author Maurizio Nagni
 */
@Configuration
@ComponentScan(basePackages = {"uk.ac.gda.core.tool.spring", "uk.ac.diamond.daq.experiment.structure"},
				excludeFilters = {
						@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE,
								value = NodeFileRequesterService.class),
						@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE,
								value = ExperimentTreeCache.class),
						// As the class below is annotated with @ComponentScan AND lives in a package
						// scanned by this configuration, it causes NodeFileRequesterService to be loaded
						// instead of the mock below. Here is the reason to add it here.
						@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE,
								value = ExperimentStructureJobResponder.class)



						})
public class NexusExperimentControllerTestConfiguration {

	@Bean
	public NodeFileRequesterService nodeFileRequesterService() {
		return Mockito.mock(NodeFileRequesterService.class);
	}

	@Bean
	public ExperimentTreeCache experimentTreeCache() {
		return Mockito.mock(ExperimentTreeCache.class);
	}

	@Bean
	public ExperimentStructureJobResponder experimentStructureJobResponder() {
		return Mockito.mock(ExperimentStructureJobResponder.class);
	}
}
