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

import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.BufferedDetector;
import gda.device.detector.countertimer.BufferedScaler;
import gda.device.detector.xspress.Xspress2BufferedDetector;
import gda.device.scannable.ContinuouslyScannable;
import gda.jython.commands.ScannableCommands;
import gda.scan.ConcurrentScan;
import gda.scan.ContinuousScan;
import gda.scan.ScanPlotSettings;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.aspectj.util.Reflection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.api.support.membermodification.strategy.MethodStubStrategy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import uk.ac.gda.client.microfocus.scan.RasterMap;
import uk.ac.gda.client.microfocus.scan.RasterMapDetectorPreparer;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ScannableCommands.class, ConcurrentScan.class })
public class RasterMapTest {

	private MicrofocusMapTestComponent testHelper;
	private ScanPlotSettings mockPlotSettings;
	private XasScanBase mapscan;
	private ContinuouslyScannable x_traj_scannable;
	private ConcurrentScan mockScan;

	@Before
	public void setup() throws InterruptedException, Exception {

		testHelper = new MicrofocusMapTestComponent();
		testHelper.setup();

		{
			// create mock scan
			mockScan = PowerMockito.mock(ConcurrentScan.class);

			// runScan is a void method, so have to make an Answer for just that method
			PowerMockito.doAnswer(new org.mockito.stubbing.Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					return null;
				}

			}).when(mockScan).runScan();

			mockPlotSettings = PowerMockito.mock(ScanPlotSettings.class);
			Mockito.when(mockScan.getScanPlotSettings()).thenReturn(mockPlotSettings);

			// then stub the factory method and make sure that it always retruns the stub
			Method staticMethod = Reflection.getMatchingMethod(ScannableCommands.class, "createConcurrentScan",
					new Object[] { new Object[0] });
			MethodStubStrategy<Object> stubbedMethod = MemberModifier.stub(staticMethod);
			stubbedMethod.toReturn(mockScan);
		}
	}

	protected SampleEnvironmentIterator createSingleScanIterator() {
		SampleEnvironmentIterator it = PowerMockito.mock(SampleEnvironmentIterator.class);
		Mockito.when(it.getNumberOfRepeats()).thenReturn(1);
		Mockito.when(it.getNextSampleName()).thenReturn("My sample");
		Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());
		return it;
	}

	@Test
	public void testRasterMapScan() throws Exception {

		testHelper.getMapscanParams().setRaster(true);

		x_traj_scannable = testHelper.createMockContinuosulyScannableMotor("x_scannable");

		BufferedDetector[] detectors = createBufferedDetectors();
		testHelper.setDetectorPreparer(PowerMockito.mock(RasterMapDetectorPreparer.class));
		Mockito.when(((RasterMapDetectorPreparer) testHelper.getDetectorPreparer()).getRasterMapDetectors())
				.thenReturn(detectors);

		SampleEnvironmentIterator it = createSingleScanIterator();
		Mockito.when(testHelper.getSamplePreparer().createIterator("Fluorescence")).thenReturn(it);

		mapscan = new RasterMap(testHelper.getBeamlinepreparer(),
				(RasterMapDetectorPreparer) testHelper.getDetectorPreparer(), testHelper.getSamplePreparer(),
				testHelper.getOutputPreparer(), testHelper.getCommandQueueProcessor(),
				testHelper.getXASLoggingScriptController(), testHelper.getDatawriterconfig(),
				new ArrayList<AsciiMetadataConfig>(), testHelper.getEnergy_scannable(), testHelper.getMetashop(), true,
				x_traj_scannable, null, testHelper.getY_scannable(), testHelper.getZ_scannable(), null, null);

		mapscan.doCollection(testHelper.getSampleParams(), testHelper.getMapscanParams(), testHelper.getDetParams(),
				testHelper.getOutputParams(), testHelper.getXspressConfigurationParameters(),
				testHelper.getExperimentalFullPath(), 1);

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
				testHelper.getMapscanParams(), testHelper.getDetParams());

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
		org.junit.Assert.assertTrue(args[5] instanceof ContinuousScan);
		org.junit.Assert.assertTrue(detectors[0].getName().equals(
				((ContinuousScan) args[5]).getAllDetectors().get(0).getName()));
		org.junit.Assert.assertTrue(detectors[1].getName().equals(
				((ContinuousScan) args[5]).getAllDetectors().get(1).getName()));
		org.junit.Assert.assertTrue(x_traj_scannable.getName().equals(
				((ContinuousScan) args[5]).getAllScannables().get(0).getName()));

		inorder.verify(mockScan).runScan();

		inorder.verify(testHelper.getOutputParams()).getAfterScriptName();
		inorder.verify(testHelper.getDetectorPreparer()).completeCollection();
		inorder.verify(testHelper.getBeamlinepreparer()).completeExperiment();
	}

	private BufferedDetector[] createBufferedDetectors() throws DeviceException {
		BufferedScaler bufferedionchambers = PowerMockito.mock(BufferedScaler.class);
		Mockito.when(bufferedionchambers.getName()).thenReturn("bufferedionchambers");
		Mockito.when(bufferedionchambers.readout()).thenReturn(new double[] { 1.0, 2.0, 3.0 });
		Mockito.when(bufferedionchambers.getExtraNames()).thenReturn(new String[] { "i0", "it", "iref" });
		Mockito.when(bufferedionchambers.getInputNames()).thenReturn(new String[] { "time" });
		Mockito.when(bufferedionchambers.getOutputFormat()).thenReturn(new String[] { "%.2f", "%.2f", "%.2f", "%.2f" });

		Xspress2BufferedDetector bufferedXspress2 = PowerMockito.mock(Xspress2BufferedDetector.class);
		Mockito.when(bufferedXspress2.getName()).thenReturn("bufferedXspress2");
		Mockito.when(bufferedXspress2.getExtraNames()).thenReturn(new String[] { "i0", "it", "iref" });
		Mockito.when(bufferedXspress2.getInputNames()).thenReturn(new String[] { "time" });
		Mockito.when(bufferedXspress2.getOutputFormat()).thenReturn(new String[] { "%.2f", "%.2f", "%.2f", "%.2f" });

		return new BufferedDetector[] { bufferedionchambers, bufferedXspress2 };
	}

}
