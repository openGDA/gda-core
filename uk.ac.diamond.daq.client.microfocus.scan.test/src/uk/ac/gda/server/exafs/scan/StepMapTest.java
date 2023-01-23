/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan;

import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import gda.device.CounterTimer;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.jython.commands.ScannableCommands;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.scan.ConcurrentScan;
import gda.scan.ScanPlotSettings;
import uk.ac.gda.client.microfocus.scan.MapFactory;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

public class StepMapTest {

	private ConcurrentScan mockScan;
	private ScannableMotor x_scannable;
	private XasScanBase mapscan;
	private MicrofocusMapTestComponent testHelper;
	private ScanPlotSettings mockPlotSettings;
	private MockedStatic<ScannableCommands> scannableCommandsMock;

	@Before
	public void setup() throws Exception {

		scannableCommandsMock = Mockito.mockStatic(ScannableCommands.class);

		testHelper = new MicrofocusMapTestComponent();
		testHelper.setup();

		x_scannable = testHelper.createMockMotorScannable("x_scannable");

		{
			// create mock scan
			mockScan = Mockito.mock(ConcurrentScan.class);

			// runScan is a void method, so have to make an Answer for just that method
			Mockito.doAnswer(new org.mockito.stubbing.Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					return null;
				}

			}).when(mockScan).runScan();

			mockPlotSettings = Mockito.mock(ScanPlotSettings.class);
			Mockito.when(mockScan.getScanPlotSettings()).thenReturn(mockPlotSettings);

			// then stub the factory method and make sure that it always retruns the stub
			Mockito.when(ScannableCommands.createConcurrentScan(any())).thenReturn(mockScan);
		}
	}

	@After
	public void closeMock() {
		scannableCommandsMock.close();
	}

	@Test
	public void testSingleMapScan() throws Exception {

		SampleEnvironmentIterator it = createSingleScanIterator();
		Mockito.when(testHelper.getSamplePreparer().createIterator("Fluorescence")).thenReturn(it);

		// create MapScan object
		MapFactory theFactory = new MapFactory();

		theFactory.setBeamlinePreparer(testHelper.getBeamlinepreparer());
		theFactory.setDetectorPreparer(testHelper.getDetectorPreparer());
		theFactory.setSamplePreparer(testHelper.getSamplePreparer());
		theFactory.setOutputPreparer(testHelper.getOutputPreparer());
		theFactory.setLoggingScriptController(testHelper.getXASLoggingScriptController());
		theFactory.setEnergyNoGapScannable(testHelper.getEnergy_scannable());
		theFactory.setEnergyWithGapScannable(testHelper.getEnergy_scannable());
		theFactory.setMetashop(testHelper.getMetashop());
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("mapScan");

		theFactory.setCounterTimer(Mockito.mock(CounterTimer.class));
		theFactory.setxScan(x_scannable);
		theFactory.setyScan(testHelper.getY_scannable());
		theFactory.setzScan(testHelper.getZ_scannable());
		theFactory.setElementListScriptController(Mockito.mock(ScriptControllerBase.class));

		mapscan = theFactory.createStepMap();

		mapscan.configureCollection(testHelper.getSampleParams(), testHelper.getMapscanParams(), testHelper.getDetParams(),
				testHelper.getOutputParams(), testHelper.getXspressConfigurationParameters(),
				testHelper.getExperimentalFullPath(), 1);
		mapscan.doCollection();

		// check that the the correct order of preparers and scan were called
		InOrder inorder = Mockito.inOrder(testHelper.getBeamlinepreparer(), testHelper.getDetectorPreparer(),
				testHelper.getSamplePreparer(), testHelper.getOutputPreparer(), it, testHelper.getOutputParams(),
				mockScan);

		inorder.verify(testHelper.getBeamlinepreparer()).configure(testHelper.getMapscanParams(),
				testHelper.getDetParams(), testHelper.getSampleParams(), testHelper.getOutputParams(),
				testHelper.getExperimentalFullPath());
		inorder.verify(testHelper.getDetectorPreparer()).configure(testHelper.getMapscanParams(),
				testHelper.getDetParams(), testHelper.getOutputParams(), testHelper.getExperimentalFullPath());
		inorder.verify(testHelper.getSamplePreparer()).configure(testHelper.getMapscanParams(),
				testHelper.getSampleParams());
		inorder.verify(testHelper.getOutputPreparer()).configure(testHelper.getOutputParams(),
				testHelper.getMapscanParams(), testHelper.getDetParams(), testHelper.getSampleParams());

		inorder.verify(testHelper.getSamplePreparer()).createIterator("Fluorescence");
		inorder.verify(testHelper.getBeamlinepreparer()).prepareForExperiment();

		// iterator is always called, even if it only does one repetition
		inorder.verify(it).resetIterator();
		inorder.verify(it).next();
		inorder.verify(it).getNextSampleName();
		inorder.verify(it).getNextSampleDescriptions();

		inorder.verify(testHelper.getOutputParams()).getBeforeScriptName();

		inorder.verify(testHelper.getDetectorPreparer()).beforeEachRepetition();
		inorder.verify(testHelper.getOutputPreparer()).beforeEachRepetition();

		// need to test that the args given to the scan were correct
		Object[] args = mapscan.createScanArguments("sample 1", new ArrayList<String>());
		org.junit.Assert.assertTrue(args[0] instanceof Scannable);
		org.junit.Assert.assertTrue(testHelper.getY_scannable().getName().equals(((Scannable) args[0]).getName()));
		org.junit.Assert.assertTrue(args[4] instanceof Scannable);
		org.junit.Assert.assertTrue(x_scannable.getName().equals(((Scannable) args[4]).getName()));
		org.junit.Assert.assertTrue(args[8] instanceof Scannable);
		org.junit.Assert.assertTrue(testHelper.getZ_scannable().getName().equals(((Scannable) args[8]).getName()));
		org.junit.Assert.assertTrue(args[9] instanceof Detector);
		org.junit.Assert.assertTrue(testHelper.getIonchambers().getName().equals(((Scannable) args[9]).getName()));

		inorder.verify(mockScan).runScan();

		inorder.verify(testHelper.getOutputParams()).getAfterScriptName();
		inorder.verify(testHelper.getDetectorPreparer()).completeCollection();
		inorder.verify(testHelper.getBeamlinepreparer()).completeExperiment();
	}

	protected SampleEnvironmentIterator createSingleScanIterator() {
		SampleEnvironmentIterator it = Mockito.mock(SampleEnvironmentIterator.class);
		Mockito.when(it.getNumberOfRepeats()).thenReturn(1);
		Mockito.when(it.getNextSampleName()).thenReturn("My sample");
		Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());
		return it;
	}

}
