/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.addetector;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.TestHelpers;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.addetector.ADPco;
import gda.device.detector.addetector.triggering.SingleExposurePco;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.epics.PV;

import org.junit.Test;
import org.mockito.InOrder;

// TODO: Move file writer enable/disable test up into ADDetectorTest
public class ADPcoTest extends ADDetectorTest {

	private ADPco adPco;
	private ADDriverPco mockAdDriverPco;
	private PV<Boolean> mockArmModePv;
	private NDPluginBase mockNdFilePluginBase;


	@Override
	public ADDetector det() {
		return adPco;
	}

	public ADPco pco() {
		return adPco;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createDetector() {
		adPco = new ADPco();
		mockAdDriverPco = mock(ADDriverPco.class);
		mockArmModePv = mock(PV.class);
		when(mockAdDriverPco.getArmModePV()).thenReturn(mockArmModePv);
		collectionStrategy = spy(new SingleExposurePco(adBase, mockAdDriverPco, 0.1));
		adPco.setCollectionStrategy(collectionStrategy); // default strategy

	}

	@Override
	protected void setUpNoConfigure() throws Exception {
		createDetector();
		super.setUpNoConfigure();
		mockNdFilePluginBase = mock(NDPluginBase.class);

	}
	
	@Override
	@Test
	public void testAtScanStart() throws Exception {
		TestHelpers.setUpTest(ADPcoTest.class, "testPrepareForCollection", true);
		det().setReadFilepath(true);
		super.testAtScanStart();
		InOrder inOrder = inOrder(adBase, mockArmModePv, fileWriter, mockNdFilePluginBase);
		
		// Triggering
		inOrder.verify(adBase).stopAcquiring();
		inOrder.verify(adBase).setTriggerMode(1);
		inOrder.verify(adBase).setImageModeWait(ImageMode.SINGLE);
		inOrder.verify(adBase).setNumImages(1);

		// Arming
		inOrder.verify(mockArmModePv).putCallback(true);
		
		// File writing
//		inOrder.verify(mockNdFilePluginBase).enableCallbacks();
//		inOrder.verify(mockNdFilePluginBase).setBlockingCallbacks(1);
	}
	
	@Test
	public void testAtScanEnd() throws Exception {
		pco().setReadFilepath(true);
		pco().atScanEnd();
	}
	@Test
	public void testAtCommandFailure() throws Exception {
		pco().setReadFilepath(true);
		pco().atCommandFailure();
	}
	@Override
	@Test
	public void testStop() throws Exception {
		pco().setReadFilepath(true);
		pco().stop();
		verify(adBase).stopAcquiring();

	}

}
