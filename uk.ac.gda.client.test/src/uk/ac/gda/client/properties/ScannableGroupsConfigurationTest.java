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

package uk.ac.gda.client.properties;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.stage.ScannableGroupProperties;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Tests for the {@link AcquisitionConfigurationProperties} based on a detectors.properties file.
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationPropertiesTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ScannableGroupsConfigurationTest {

	@Autowired
	private ClientSpringProperties cnf;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/configurationPropertiesTest");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_CONFIG);
	}

	/**
	 * Verifies that {@link AcquisitionPropertyType#DIFFRACTION} has one associated detector
	 */
	@Test
	public void testAcquisitionCondifigurationProperties() {
		Assert.assertEquals(2, cnf.getScannableGroups().size());

		ScannableGroupProperties acp = cnf.getScannableGroups().get(0);
		Assert.assertEquals("GTS", acp.getId());
		Assert.assertEquals("GTS", acp.getLabel());
		Assert.assertEquals(2, acp.getScannables().size());
		Assert.assertEquals("X", acp.getScannables().get(0).getId());
		Assert.assertEquals("AXIS_X", acp.getScannables().get(0).getLabel());
		Assert.assertEquals("stage_x", acp.getScannables().get(0).getScannable());
		Assert.assertNull(acp.getScannables().get(0).getEnumsMap());

		Assert.assertEquals("Z", acp.getScannables().get(1).getId());
		Assert.assertEquals("AXIS_Z", acp.getScannables().get(1).getLabel());
		Assert.assertEquals("stage_z", acp.getScannables().get(1).getScannable());
		Assert.assertNull(acp.getScannables().get(0).getEnumsMap());

		acp = cnf.getScannableGroups().get(1);
		Assert.assertEquals("shutter", acp.getId());
		Assert.assertEquals("Shutter", acp.getLabel());
		Assert.assertEquals(1, acp.getScannables().size());
		Assert.assertEquals("shutter", acp.getScannables().get(0).getId());
		Assert.assertEquals("Shutter", acp.getScannables().get(0).getLabel());
		Assert.assertEquals("eh_shutter", acp.getScannables().get(0).getScannable());
		Assert.assertEquals(4, acp.getScannables().get(0).getEnumsMap().size());
		Assert.assertEquals("Open", acp.getScannables().get(0).getEnumsMap().getOrDefault("OPEN", ""));
		Assert.assertEquals("Close", acp.getScannables().get(0).getEnumsMap().getOrDefault("CLOSE", ""));
		Assert.assertEquals("Reset", acp.getScannables().get(0).getEnumsMap().getOrDefault("RESET", ""));
		Assert.assertEquals("Closed", acp.getScannables().get(0).getEnumsMap().getOrDefault("CLOSED", ""));

	}
}
