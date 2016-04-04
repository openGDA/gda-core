/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.detector;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.scannable.DummyScannable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DummyMandelbrotMappingDetectorTest {

	private DummyMandelbrotMappingDetector detector;

	@Before
	public void setUp() throws Exception {
		detector = new DummyMandelbrotMappingDetector();

		DummyScannable realAxisPositioner = new DummyScannable();
		realAxisPositioner.rawAsynchronousMoveTo(-0.9);
		realAxisPositioner.setIncrement(0.1);

		DummyScannable imaginaryAxisPositioner = new DummyScannable();
		imaginaryAxisPositioner.rawAsynchronousMoveTo(0.3);
		imaginaryAxisPositioner.setIncrement(0.0);

		detector.setRealAxisPositioner(realAxisPositioner);
		detector.setImaginaryAxisPositioner(imaginaryAxisPositioner);
		detector.setCollectionTime(0.1);
		detector.atScanStart();
	}

	@After
	public void tearDown() throws Exception {
		detector.atScanEnd();
		detector = null;
	}

	@Test(expected = DeviceException.class)
	public void shouldThrowIfCollectDataCalledBeforeScanStart() throws Exception {
		new DummyMandelbrotMappingDetector().collectData();
	}

	@Test(expected = DeviceException.class)
	public void shouldThrowIfReadoutCalledTooSoon() throws Exception {
		detector.collectData();
		detector.readout();
	}

	@Test
	public void testWaitWhileBusy() throws Exception {
		detector.collectData();
		assertThat(detector.isBusy(), is(true));
		detector.waitWhileBusy();
		assertThat(detector.isBusy(), is(false));
		detector.readout(); // should not throw
	}

	@Test
	public void isBusyShouldReturnTrueUntilCollectionTimeHasElapsed() throws Exception {
		detector.setCollectionTime(1.0);
		assertThat(detector.isBusy(), is(false));
		detector.collectData();
		assertThat(detector.isBusy(), is(true));
		Thread.sleep(100);
		assertThat(detector.isBusy(), is(true));
		Thread.sleep(500);
		assertThat(detector.isBusy(), is(true));
		Thread.sleep(500);
		assertThat(detector.isBusy(), is(false));
	}

	@Test
	public void testMandelbrotValues() throws Exception {
		detector.collectData();
		detector.waitWhileBusy();
		NexusTreeProvider treeProvider = detector.readout();
		assertThat(treeProvider, is(instanceOf(NXDetectorData.class)));
		NXDetectorData data = (NXDetectorData) treeProvider;
		assertThat(data.getExtraNames().length, is(equalTo(1)));
		assertThat(data.getExtraNames()[0], is(equalTo(DummyMandelbrotMappingDetector.VALUE_NAME)));
		assertThat(data.getDoubleVals().length, is(equalTo(1)));

		// Now check a few example values
		assertThat(data.getDoubleVals()[0], is(equalTo(9.439447928263169))); // at (-0.8, 0.3)

		detector.collectData();
		detector.waitWhileBusy();
		data = (NXDetectorData) detector.readout();
		assertThat(data.getDoubleVals()[0], is(equalTo(23.825816558985046))); // at (-0.7, 0.3)

		detector.collectData();
		detector.waitWhileBusy();
		data = (NXDetectorData) detector.readout();
		assertThat(data.getDoubleVals()[0], is(equalTo(500.0))); // at (-0.6, 0.3) = value of MAX_ITERATIONS
	}

	// TODO test data dimensions and Julia set values
}
