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

package uk.ac.diamond.daq.service.rest;

import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import uk.ac.diamond.daq.service.ScanningAcquisitionFileService;
import uk.ac.diamond.daq.service.ScanningAcquisitionService;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.DiffractionContextFile;
import uk.ac.gda.core.tool.spring.DiffractionFileContext;
import uk.ac.gda.core.tool.spring.TomographyContextFile;
import uk.ac.gda.core.tool.spring.TomographyFileContext;

/**
 * Configure the spring environment for ConfigurationsServiceTest
 *
 * @author Maurizio Nagni
 */
@Configuration
@ComponentScan(basePackages = {
		"uk.ac.gda.core.tool.spring",
		"uk.ac.diamond.daq.mapping.api.document",
		"uk.ac.diamond.daq.service.rest"
		},
excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ExperimentRestService.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = AcquisitionFileContext.class)

		})
public class ConfigurationsServiceTestConfiguration {

	@Bean
	public AcquisitionFileContext acquisitionFileContext() throws IOException {
		AcquisitionFileContext fileContext = Mockito.mock(AcquisitionFileContext.class);
		DiffractionFileContext diffContext = diffractionFileContext();
		TomographyFileContext tomoContext = tomographyFileContext();
		doReturn(diffContext).when(fileContext).getDiffractionContext();
		doReturn(tomoContext).when(fileContext).getTomographyContext();
		return fileContext;
	}

	private DiffractionFileContext diffractionFileContext() throws IOException {
		URL configurationDir = new File("test/resources/diffConfigurations/diffOne.map").getParentFile().toURI().toURL();
		DiffractionFileContext context = Mockito.mock(DiffractionFileContext.class);
		doReturn(configurationDir).when(context).getContextFile(DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY);
		return context;
	}

	private TomographyFileContext tomographyFileContext() throws MalformedURLException {
		URL configurationDir = new File("test/resources/tomoConfigurations/tomoOne.map").getParentFile().toURI().toURL();
		TomographyFileContext context = Mockito.mock(TomographyFileContext.class);
		doReturn(configurationDir).when(context).getContextFile(TomographyContextFile.TOMOGRAPHY_CONFIGURATION_DIRECTORY);
		return context;
	}

	@Bean
	public ScanningAcquisitionService scanningAcquisitionService() {
		return Mockito.mock(ScanningAcquisitionService.class);
	}

	@Bean
	public ScanningAcquisitionFileService scanningAcquisitionFileService() {
		return Mockito.mock(ScanningAcquisitionFileService.class);
	}

}
