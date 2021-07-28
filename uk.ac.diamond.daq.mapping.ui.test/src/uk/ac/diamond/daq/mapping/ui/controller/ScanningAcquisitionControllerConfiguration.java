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

package uk.ac.diamond.daq.mapping.ui.controller;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.gda.ui.tool.rest.ConfigurationsRestServiceClient;
import uk.ac.gda.ui.tool.rest.ExperimentControllerServiceClient;
import uk.ac.gda.ui.tool.rest.ScanningAcquisitionRestServiceClient;
import uk.ac.gda.ui.tool.spring.FinderService;

@Configuration
@ComponentScan(basePackages = {"uk.ac.gda.core.tool.spring",
		"uk.ac.gda.ui.tool.spring",
		"uk.ac.gda.ui.tool.document",
		"uk.ac.gda.client.properties",
		"uk.ac.diamond.daq.client.gui.camera.beam",
		"uk.ac.diamond.daq.client.gui.camera.beam.state",
		"uk.ac.diamond.daq.mapping.ui.controller",
		"uk.ac.diamond.daq.mapping.ui.services.position",
		"uk.ac.diamond.daq.mapping.api.document"},
excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = FinderService.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = IStageController.class)})
public class ScanningAcquisitionControllerConfiguration {

	@Bean
	public FinderService finderService() {
		return Mockito.mock(FinderService.class);
	}

	@Bean(name = "scanningAcquisitionRestServiceClient")
	public ScanningAcquisitionRestServiceClient getScanningAcquisitionRestServiceClient() {
		return Mockito.mock(ScanningAcquisitionRestServiceClient.class);
	}

	@Bean(name = "experimentControllerServiceClient")
	public ExperimentControllerServiceClient getExperimentControllerServiceClient() {
		return Mockito.mock(ExperimentControllerServiceClient.class);
	}

	@Bean(name = "configurationsRestServiceClient")
	public ConfigurationsRestServiceClient getConfigurationsRestServiceClient() {
		return Mockito.mock(ConfigurationsRestServiceClient.class);
	}

	@Bean
	public StageController stageController() {
		return Mockito.mock(StageController.class);
	}
}
