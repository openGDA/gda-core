package gda.device.hidenrga;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import uk.ac.gda.api.io.IPathConstructor;
import uk.ac.gda.api.io.PathConstructor;

/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

public class HidenRGATest {

	private int[] masses;
	private LinkedHashSet<Integer> massesSet;
	private HidenRGAScannable rga;
	private HidenRGAController mockedController;
	private MockedStatic<LocalProperties> localPropertiesMock;

	@Before
	public void setup() throws DeviceException, IOException {

		localPropertiesMock = Mockito.mockStatic(LocalProperties.class);

		JythonServerFacade jythonserverfacade = Mockito.mock(JythonServerFacade.class);
		InterfaceProvider.setTerminalPrinterForTesting(jythonserverfacade);

		masses = new int[] { 16, 32, 48 };
		massesSet = new LinkedHashSet<>();
		massesSet.add(16);
		massesSet.add(32);
		massesSet.add(48);

		rga = new HidenRGAScannable();
		rga.setName("rga");

		mockedController = Mockito.mock(HidenRGAController.class);

		Mockito.when(mockedController.readout()).thenReturn(new double[]{12.5,177.7, 145.2});
		Mockito.when(mockedController.readValve()).thenReturn(0.7);
		Mockito.when(mockedController.readtemp()).thenReturn(20.0);


		IPathConstructor pathConstructor = Mockito.mock(PathConstructor.class);
		InterfaceProvider.setPathConstructorForTesting(pathConstructor);
		Mockito.when(pathConstructor.createFromDefaultProperty()).thenReturn("/tmp");

		Mockito.when(LocalProperties.get(LocalProperties.GDA_VAR_DIR)).thenReturn("/tmp");


		rga.setController(mockedController);
	}

	@After
	public void tearDown() {
		localPropertiesMock.close();
	}


	@Test
	public void testExtraNamesAfterSettingsMasses() {
		rga.setMasses(new int[] { 16, 32, 48 });

		String[] extraNames = rga.getExtraNames();
		assertEquals("16_amu", extraNames[0]);
		assertEquals("32_amu", extraNames[1]);
		assertEquals("48_amu", extraNames[2]);
	}

	@Test
	public void testHidenStartedCorrectlyAtScanStart() throws DeviceException, IOException {

		rga.setMasses(masses);
		rga.atScanStart();

		Mockito.verify(mockedController).setMasses(massesSet);
		Mockito.verify(mockedController).setContinuousCycles();
		Mockito.verify(mockedController).writeToRGA();
		Mockito.verify(mockedController).startScan();
	}

	@Test
	public void testHidenStoppedCorrectlyAtScanEnd() throws DeviceException, IOException {
		rga.atScanEnd();
		Mockito.verify(mockedController).stopScan();
	}

	@Test
	public void testHidenStoppedCorrectlyOnScanFailure() throws DeviceException, IOException {
		rga.atCommandFailure();
		Mockito.verify(mockedController).stopScan();
	}

	@Test
	public void testHidenStartedCorrectlyWhenRecordingStarted() throws IOException {
		rga.setMasses(masses);
		rga.startRecording();
		Mockito.verify(mockedController).setMasses(massesSet);
		Mockito.verify(mockedController).setContinuousCycles();
		Mockito.verify(mockedController).writeToRGA();
		Mockito.verify(mockedController).startScan();
	}

	@Test
	public void testHidenIsBusyAfterRecordingStarted() throws IOException, DeviceException {
		rga.setMasses(masses);
		rga.startRecording();
		assertTrue(rga.isBusy());
	}

	@Test
	public void testHidenStoppedCorrectlyWhenRecordingFinished() throws IOException, InterruptedException {
		rga.setMasses(masses);
		rga.startRecording();
		Thread.sleep(1000); // give it a chance to run for a bit!
		rga.stopRecording();
		Thread.sleep(3000); // give it a chance to run for a bit!
		Mockito.verify(mockedController).stopScan();
	}

	@Test(expected = DeviceException.class) //Should throw a DeviceException when calling atScanStart after startRecording
	public void testHidenThrowsErrorIfRecordingAtScanStart() throws IOException, DeviceException {
		rga.setMasses(masses);
		rga.startRecording();
		rga.atScanStart();
	}

	@Test
	public void testHidenReadsCorrectValuesOnReadout() throws DeviceException, IOException, FactoryException {
		rga.configure();
		rga.setMasses(masses);
		rga.getPosition();

		Mockito.verify(mockedController).readout();
		Mockito.verify(mockedController).readValve();
		Mockito.verify(mockedController).readtemp();
	}
}
