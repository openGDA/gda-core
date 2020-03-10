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

package gda.device.scannable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import gda.factory.FactoryException;

/**
 * Minimal tests on {@link MultiPVScannable}
 *
 * @author Maurizio Nagni
 */
public class MultiPVScannableTest {

	private MultiPVScannable scannable;
	private static final String SCANNABLE_NAME = "ScannableName";
	private static final String SCANNABLE_WRITE_PV_NAME = "WritePVName";
	private static final String SCANNABLE_READ_PV_NAME = "ReadPVName";

	@Before
	public void before() {
		scannable = new MultiPVScannable();
		scannable.setName(SCANNABLE_NAME);
	}

	@Test
	public void testSimpleMultiPVScannable() throws FactoryException {
		assertEquals(SCANNABLE_NAME, scannable.getName());
	}

	/**
	 * Without calling {@link MultiPVScannable#configure()}, {@link MultiPVScannable#getInputNames()} returns default values
	 *
	 * @throws FactoryException
	 */
	@Test
	public void testInputNameBeforeConfiguration() throws FactoryException {
		assertArrayEquals(new String[] { ScannableBase.DEFAULT_INPUT_NAME }, scannable.getInputNames());
	}

	/**
	 * Cannot configure the instance without a readPV name
	 *
	 * @throws FactoryException
	 */
	@Test(expected = FactoryException.class)
	public void testConfigurationWithoutReadPV() throws FactoryException {
		scannable.setWritePV(SCANNABLE_WRITE_PV_NAME);
		scannable.configure();
	}

	/**
	 * Cannot configure the instance without a writePV name
	 *
	 * @throws FactoryException
	 */
	@Test(expected = FactoryException.class)
	public void testConfigurationWithoutWritePV() throws FactoryException {
		scannable.setReadPV(SCANNABLE_READ_PV_NAME);
		scannable.configure();
	}

	/**
	 * MultiPVScannable.getInputNames contains the scannable name not the default
	 *
	 * @throws FactoryException
	 */
	@Test
	public void testInputNameAfterConfiguration() throws FactoryException {
		scannable.setWritePV(SCANNABLE_WRITE_PV_NAME);
		scannable.setReadPV(SCANNABLE_READ_PV_NAME);
		scannable.configure();
		assertArrayEquals(new String[] { SCANNABLE_NAME }, scannable.getInputNames());
	}
}
