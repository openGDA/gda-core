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
import org.springframework.context.annotation.Primary;

import uk.ac.diamond.daq.mapping.api.document.service.IScanningAcquisitionService;
import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.gda.ui.tool.rest.ScanningAcquisitionRestServiceClient;
import uk.ac.gda.ui.tool.spring.FinderService;

@Configuration
@ComponentScan(basePackages = {"uk.ac.gda.core.tool.spring", "uk.ac.diamond.daq.mapping.ui.controller",
		"uk.ac.diamond.daq.mapping.ui.services.position", "uk.ac.diamond.daq.mapping.api.document"},
excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = FinderService.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = IStageController.class)})
public class ScanningAcquisitionControllerConfiguration {

	@Bean
	public FinderService finderService() {
		return Mockito.mock(FinderService.class);
	}

	@Bean(name = "scanningAcquisitionService")
	@Primary
	public IScanningAcquisitionService scanningAcquisitionService() {
		return Mockito.mock(IScanningAcquisitionService.class);
	}

	@Bean(name = "scanningAcquisitionRestServiceClient")
	public ScanningAcquisitionRestServiceClient getScanningAcquisitionRestServiceClient() {
		return Mockito.mock(ScanningAcquisitionRestServiceClient.class);
	}




	@Bean
	public StageController stageController() {
		return Mockito.mock(StageController.class);
	}
}
