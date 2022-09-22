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

package uk.ac.gda.tomography.browser.provider;

import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.viewers.StyledString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Tests for the {@link ExposureLabelProvider}
 *
 * @author Maurizio Nagni
 */
public class ExposureLabelProviderTest {

	private ExposureLabelProvider provider;

	@Before
	public void before() {
		provider = new ExposureLabelProvider();
	}

	/**
	 * Acquisition = {@code null} returns {@link ClientMessages#NOT_AVAILABLE}
	 */
	@Test
	public void comparatorNullAcquisitionTest() {
		StyledString styledString = provider.getStyledText(null);
		Assert.assertEquals(getMessage(ClientMessages.NOT_AVAILABLE), styledString.getString());
	}

	/**
	 * Acquisition --> configuration --> parameters --> detector = {@code null} returns {@link ClientMessages#NOT_AVAILABLE}
	 */
	@Test
	public void comparatorNullDetectorTest() {
		ScanningAcquisition scanningAcquisition = new ScanningAcquisition();

		ScanningConfiguration scanningConfiguration = new ScanningConfiguration();
		scanningAcquisition.setAcquisitionConfiguration(scanningConfiguration);

		ScanningParameters scanningParameters = new ScanningParameters();
		scanningConfiguration.setAcquisitionParameters(scanningParameters);

		AcquisitionConfigurationResource<ScanningAcquisition> resource = null;
		try {
			resource = new AcquisitionConfigurationResource<>(new URL("http://dummy"), scanningAcquisition);
		} catch (MalformedURLException e) {
			Assert.fail();
		}
		StyledString styledString = provider.getStyledText(resource);
		Assert.assertEquals(getMessage(ClientMessages.NOT_AVAILABLE), styledString.getString());
	}
}
