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

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

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

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.XasScannable;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.commands.ScannableCommands;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.scan.ConcurrentScan;
import gda.scan.ScanPlotSettings;
import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.beans.exafs.Region;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ScannableCommands.class, ConcurrentScan.class })
public class EnergyScanTest {

	private BeamlinePreparer beamlinePreparer;
	private DetectorPreparer detectorPreparer;
	private SampleEnvironmentPreparer samplePreparer;
	private OutputPreparer outputPreparer;
	private NXMetaDataProvider metashop;
	private ScannableMotor energy_scannable;
	private EnergyScan xasscan;
	private LoggingScriptController loggingScriptController;
	private XanesScanParameters xanesParams;
	private DetectorParameters detParams;
	private ISampleParameters sampleParams;
	private IOutputParameters outputParams;
	private TfgScalerWithFrames ionchambers;
	private final String experimentalFullPath = "/scratch/test/xml/path/";
	private ConcurrentScan mockScan;
	private ScanPlotSettings mockPlotSettings;

	private Set<IonChamberParameters> makeIonChamberParameters() {
		IonChamberParameters ionParams = new IonChamberParameters();
		ionParams.setChangeSensitivity(true);
		ionParams.setAutoFillGas(true);
		ionParams.setName("I0");
		ionParams.setDeviceName("counterTimer01");
		ionParams.setGain("1 nA/V");
		ionParams.setOffset("1 pA");
		ionParams.setGasType("Ar");
		ionParams.setPercentAbsorption(15.0);
		ionParams.setTotalPressure(1.1);
		ionParams.setPressure(99.63);
		ionParams.setGas_fill1_period_box(200.0);
		ionParams.setGas_fill2_period_box(200.0);

		IonChamberParameters ionParamsOff = new IonChamberParameters();
		ionParamsOff.setChangeSensitivity(false);
		ionParamsOff.setAutoFillGas(false);

		LinkedHashSet<IonChamberParameters> set = new LinkedHashSet<IonChamberParameters>();
		set.add(ionParams);
		set.add(ionParamsOff);

		return set;

	}

	@Before
	public void setup() throws DeviceException {

		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");

		ionchambers = PowerMockito.mock(TfgScalerWithFrames.class);
		Mockito.when(ionchambers.getName()).thenReturn("ionchambers");
		Mockito.when(ionchambers.readout()).thenReturn(new double[] { 1.0, 2.0, 3.0 });
		Mockito.when(ionchambers.getExtraNames()).thenReturn(new String[] { "i0", "it", "iref" });
		Mockito.when(ionchambers.getInputNames()).thenReturn(new String[] { "time" });
		Mockito.when(ionchambers.getOutputFormat()).thenReturn(new String[] { "%.2f", "%.2f", "%.2f", "%.2f" });

		ClientDetails details = Mockito.mock(ClientDetails.class);
		Mockito.when(details.getVisitID()).thenReturn("0-0");

		JythonServerFacade jythonserverfacade = Mockito.mock(JythonServerFacade.class);
		Mockito.when(jythonserverfacade.getBatonHolder()).thenReturn(details);
		InterfaceProvider.setTerminalPrinterForTesting(jythonserverfacade);
		InterfaceProvider.setAuthorisationHolderForTesting(jythonserverfacade);
		InterfaceProvider.setBatonStateProviderForTesting(jythonserverfacade);
		InterfaceProvider.setJythonNamespaceForTesting(jythonserverfacade);
		InterfaceProvider.setScanStatusHolderForTesting(jythonserverfacade);
		Mockito.when(jythonserverfacade.getFromJythonNamespace("ionchambers")).thenReturn(ionchambers);

		JythonServer jythonserver = Mockito.mock(JythonServer.class);
		InterfaceProvider.setDefaultScannableProviderForTesting(jythonserver);
		InterfaceProvider.setCurrentScanInformationHolderForTesting(jythonserver);
		InterfaceProvider.setJythonServerNotiferForTesting(jythonserver);
		Mockito.when(jythonserver.getDefaultScannables()).thenReturn(new Vector<Scannable>());

		InterfaceProvider.setScanDataPointProviderForTesting(jythonserverfacade);

		// create the preparers
		beamlinePreparer = PowerMockito.mock(BeamlinePreparer.class);
		detectorPreparer = PowerMockito.mock(DetectorPreparer.class);
		samplePreparer = PowerMockito.mock(SampleEnvironmentPreparer.class);
		outputPreparer = PowerMockito.mock(OutputPreparer.class);
		metashop = new NXMetaDataProvider();
		loggingScriptController = PowerMockito.mock(LoggingScriptController.class);

		energy_scannable = PowerMockito.mock(ScannableMotor.class);
		Mockito.when(energy_scannable.getName()).thenReturn("energy_scannable");
		Mockito.when(energy_scannable.getInputNames()).thenReturn(new String[] { "energy_scannable" });
		Mockito.when(energy_scannable.getExtraNames()).thenReturn(new String[] {});
		Mockito.when(energy_scannable.getOutputFormat()).thenReturn(new String[] { "%.2f" });
		Mockito.when(energy_scannable.getPosition()).thenReturn(7000.0);

		// create XasScan object
		XasScanFactory theFactory = new XasScanFactory();
		theFactory.setBeamlinePreparer(beamlinePreparer);
		theFactory.setDetectorPreparer(detectorPreparer);
		theFactory.setSamplePreparer(samplePreparer);
		theFactory.setOutputPreparer(outputPreparer);
		theFactory.setLoggingScriptController(loggingScriptController);
		theFactory.setMetashop(metashop);
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setEnergyScannable(energy_scannable);
		theFactory.setScanName("energyScan");
		xasscan = theFactory.createEnergyScan();

		// create the beans and give to the XasScan
		Region region = new Region();
		region.setEnergy(7000.0);
		region.setStep(3.0);
		region.setTime(1.0);

		xanesParams = new XanesScanParameters();
		xanesParams.setEdge("K");
		xanesParams.setElement("Fe");
		xanesParams.addRegion(region);
		xanesParams.setFinalEnergy(7021.0);

		Set<IonChamberParameters> ionParamsSet = makeIonChamberParameters();

		TransmissionParameters transParams = new TransmissionParameters();
		transParams.setCollectDiffractionImages(false);
		transParams.setDetectorType("transmission");
		for (IonChamberParameters params : ionParamsSet) {
			transParams.addIonChamberParameter(params);
		}

		DetectorGroup transmissionDetectors = new DetectorGroup("Transmission", new String[] { "ionchambers" });
		List<DetectorGroup> detectorGroups = new ArrayList<DetectorGroup>();
		detectorGroups.add(transmissionDetectors);

		detParams = new DetectorParameters();
		detParams.setTransmissionParameters(transParams);
		detParams.setExperimentType(DetectorParameters.TRANSMISSION_TYPE);
		detParams.setDetectorGroups(detectorGroups);

		sampleParams = PowerMockito.mock(ISampleParameters.class);
		Mockito.when(sampleParams.getName()).thenReturn("My Sample");
		Mockito.when(sampleParams.getDescriptions()).thenReturn(new ArrayList<String>());

		outputParams = PowerMockito.mock(IOutputParameters.class);
		Mockito.when(outputParams.getAsciiFileName()).thenReturn("");
		Mockito.when(outputParams.getAsciiDirectory()).thenReturn("ascii");
		Mockito.when(outputParams.getNexusDirectory()).thenReturn("nexus");
		Mockito.when(outputParams.getMetadataList()).thenReturn(new ArrayList<MetadataParameters>());
		Mockito.when(outputParams.getAfterScriptName()).thenReturn("");
		Mockito.when(outputParams.getBeforeScriptName()).thenReturn("");
		Mockito.when(outputParams.getSignalList()).thenReturn(new ArrayList<SignalParameters>());

	}

	@Test
	public void testSingleXanesScan() {

		try {

			prepareMockScan();

			SampleEnvironmentIterator it = PowerMockito.mock(SampleEnvironmentIterator.class);
			Mockito.when(it.getNumberOfRepeats()).thenReturn(1);
			Mockito.when(it.getNextSampleName()).thenReturn("My sample");
			Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());

			Mockito.when(samplePreparer.createIterator("Transmission")).thenReturn(it);

			xasscan.configureCollection(sampleParams, xanesParams, detParams, outputParams, null, experimentalFullPath, 1);
			xasscan.doCollection();

			// check that the the correct order of preparers and scan were called
			InOrder inorder = Mockito.inOrder(beamlinePreparer,detectorPreparer,samplePreparer,outputPreparer,it,outputParams,mockScan,outputParams);

			inorder.verify(beamlinePreparer).configure(xanesParams, detParams, sampleParams, outputParams,
					experimentalFullPath);
			inorder.verify(detectorPreparer).configure(xanesParams, detParams, outputParams, experimentalFullPath);
			inorder.verify(samplePreparer).configure(xanesParams,sampleParams);
			inorder.verify(outputPreparer).configure(outputParams, xanesParams, detParams,sampleParams);

			inorder.verify(samplePreparer).createIterator("Transmission");
			inorder.verify(beamlinePreparer).prepareForExperiment();

			inorder.verify(it).resetIterator();
			inorder.verify(it).next();
			inorder.verify(it).getNextSampleName();
			inorder.verify(it).getNextSampleDescriptions();

			inorder.verify(outputParams).getBeforeScriptName();

			inorder.verify(detectorPreparer).beforeEachRepetition();
			inorder.verify(outputPreparer).beforeEachRepetition();

			inorder.verify(outputParams).getSignalList();
			inorder.verify(outputPreparer).getPlotSettings();

			// as the scan is not really run but mocked, instead check that the args given to the scan are as expected.
			Object[] args = xasscan.createScanArguments("sample 1", new ArrayList<String>());
			org.junit.Assert.assertTrue(args[0] instanceof XasScannable);
			org.junit.Assert.assertTrue(energy_scannable.getName().equals(((XasScannable) args[0]).getEnergyScannable().getName()));
			org.junit.Assert.assertTrue(args[2] instanceof Detector);
			org.junit.Assert.assertTrue(ionchambers.getName().equals(((Scannable) args[2]).getName()));

			inorder.verify(outputParams).getAfterScriptName();
			inorder.verify(detectorPreparer).completeCollection();
			inorder.verify(beamlinePreparer).completeExperiment();

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private void prepareMockScan() throws NoSuchMethodException, SecurityException {
		// create mock scan
		mockScan = PowerMockito.mock(ConcurrentScan.class);

		// runScan is a void method, so have to make an Answer for just that method
		try {
			PowerMockito.doAnswer(new org.mockito.stubbing.Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					return null;
				}

			}).when(mockScan).runScan();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		mockPlotSettings = PowerMockito.mock(ScanPlotSettings.class);
		Mockito.when(mockScan.getScanPlotSettings()).thenReturn(mockPlotSettings);

		// then stub the factory method and make sure that it always retruns the stub
		Method staticMethod = ScannableCommands.class.getMethod("createConcurrentScan", Object[].class);
		MethodStubStrategy<Object> stubbedMethod = MemberModifier.stub(staticMethod);
		stubbedMethod.toReturn(mockScan);

	}

	@Test
	public void testMultipleRepetitionsXanesScan() {
		try {

			prepareMockScan();

			SampleEnvironmentIterator it = PowerMockito.mock(SampleEnvironmentIterator.class);
			Mockito.when(it.getNumberOfRepeats()).thenReturn(1);
			Mockito.when(it.getNextSampleName()).thenReturn("My sample");
			Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());

			Mockito.when(samplePreparer.createIterator("Transmission")).thenReturn(it);

			xasscan.configureCollection(sampleParams, xanesParams, detParams, outputParams, null, experimentalFullPath, 3);
			xasscan.doCollection();

			// check that the the correct order of preparers and scan were called

			// performed after repetition loop
			Mockito.verify(beamlinePreparer).configure(xanesParams, detParams, sampleParams, outputParams,
					experimentalFullPath);
			Mockito.verify(detectorPreparer).configure(xanesParams, detParams, outputParams, experimentalFullPath);
			Mockito.verify(samplePreparer).configure(xanesParams,sampleParams);
			Mockito.verify(outputPreparer).configure(outputParams, xanesParams, detParams,sampleParams);

			Mockito.verify(samplePreparer).createIterator("Transmission");
			Mockito.verify(beamlinePreparer).prepareForExperiment();

			// this is the repetition loop
			{
				Mockito.verify(it, Mockito.times(3)).resetIterator();
				Mockito.verify(it, Mockito.times(3)).next();
				Mockito.verify(it, Mockito.times(3)).getNextSampleName();
				Mockito.verify(it, Mockito.times(3)).getNextSampleDescriptions();

				Mockito.verify(outputParams, Mockito.times(3)).getBeforeScriptName();

				Mockito.verify(detectorPreparer, Mockito.times(3)).beforeEachRepetition();
				Mockito.verify(outputPreparer, Mockito.times(3)).beforeEachRepetition();
				Mockito.verify(outputPreparer, Mockito.times(3)).getPlotSettings();

				Mockito.verify(outputParams, Mockito.times(3)).getSignalList();

				Mockito.verify(outputParams, Mockito.times(3)).getAfterScriptName();
			}
			// end of repetition loop

			// performed after repetition loop
			Mockito.verify(detectorPreparer).completeCollection();
			Mockito.verify(beamlinePreparer).completeExperiment();

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void testSampleEnvironmentLoopsXanesScan() {
		try {
			// 2 sample env loops + 3 repetitions, so 6 scans in total

			prepareMockScan();

			SampleEnvironmentIterator it = PowerMockito.mock(SampleEnvironmentIterator.class);
			Mockito.when(it.getNumberOfRepeats()).thenReturn(2);
			Mockito.when(it.getNextSampleName()).thenReturn("My sample");
			Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());

			Mockito.when(samplePreparer.createIterator("Transmission")).thenReturn(it);

			xasscan.configureCollection(sampleParams, xanesParams, detParams, outputParams, null, experimentalFullPath, 3);
			xasscan.doCollection();

			// check that the the correct order of preparers and scan were called

			// performed after repetition loop
			Mockito.verify(beamlinePreparer).configure(xanesParams, detParams, sampleParams, outputParams,
					experimentalFullPath);
			Mockito.verify(detectorPreparer).configure(xanesParams, detParams, outputParams, experimentalFullPath);
			Mockito.verify(samplePreparer).configure(xanesParams,sampleParams);
			Mockito.verify(outputPreparer).configure(outputParams, xanesParams, detParams,sampleParams);

			Mockito.verify(samplePreparer).createIterator("Transmission");
			Mockito.verify(beamlinePreparer).prepareForExperiment();

			// this is the repetition loop
			{
				Mockito.verify(it, Mockito.times(3)).resetIterator();

				// this the sam env loop
				{
					Mockito.verify(it, Mockito.times(6)).next();
					Mockito.verify(it, Mockito.times(6)).getNextSampleName();
					Mockito.verify(it, Mockito.times(6)).getNextSampleDescriptions();

					Mockito.verify(outputParams, Mockito.times(6)).getBeforeScriptName();

					Mockito.verify(detectorPreparer, Mockito.times(6)).beforeEachRepetition();
					Mockito.verify(outputPreparer, Mockito.times(6)).beforeEachRepetition();
					Mockito.verify(outputPreparer, Mockito.times(6)).getPlotSettings();

					Mockito.verify(outputParams, Mockito.times(6)).getSignalList();

					Mockito.verify(outputParams, Mockito.times(6)).getAfterScriptName();
				}
				// end of sam env loop
			}
			// end of repetition loop

			// performed after repetition loop
			Mockito.verify(detectorPreparer).completeCollection();
			Mockito.verify(beamlinePreparer).completeExperiment();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
