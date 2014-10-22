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

package uk.ac.gda.client.microfocus.scan;

import static org.junit.Assert.fail;
import gda.commandqueue.Processor;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.device.scannable.ScannableMotor;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.commands.ScannableCommands;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.scan.ConcurrentScan;
import gda.scan.ScanPlotSettings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

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

import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.beans.exafs.Region;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.server.exafs.scan.BeamlinePreparer;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;
import uk.ac.gda.server.exafs.scan.OutputPreparer;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ScannableCommands.class, ConcurrentScan.class })
public class MapScanTest {

	private BeamlinePreparer beamlinepreparer;
	private DetectorPreparer detectorPreparer;
	private SampleEnvironmentPreparer samplePreparer;
	private OutputPreparer outputPreparer;
	private Processor commandQueueProcessor;
	private NXMetaDataProvider metashop;
	private AsciiDataWriterConfiguration datawriterconfig;
	private ScannableMotor energy_scannable;
	private StepMap mapscan;
	private LoggingScriptController XASLoggingScriptController;
	private MicroFocusScanParameters mapscanParams;
	private DetectorParameters detParams;
	private ISampleParameters sampleParams;
	private IOutputParameters outputParams;
	private TfgScalerWithFrames ionchambers;
	private final String experimentalFullPath = "/scratch/test/xml/path";
	private ScannableMotor x_scannable;
	private ScannableMotor y_scannable;
	private ScannableMotor z_scannable;
	private ConcurrentScan mockScan;
	private XspressParameters xspressConfigurationParameters;
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
	public void setup() throws Exception {

		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");

		ionchambers = PowerMockito.mock(TfgScalerWithFrames.class);
		Mockito.when(ionchambers.getName()).thenReturn("ionchambers");
		Mockito.when(ionchambers.readout()).thenReturn(new double[] { 1.0, 2.0, 3.0 });
		Mockito.when(ionchambers.getExtraNames()).thenReturn(new String[] { "i0", "it", "iref" });
		Mockito.when(ionchambers.getInputNames()).thenReturn(new String[] { "time" });
		Mockito.when(ionchambers.getOutputFormat()).thenReturn(new String[] { "%.2f", "%.2f", "%.2f", "%.2f" });

		XspressROI roi1 = new XspressROI("Fe_k", 500, 520);
		ArrayList<XspressROI> regionList = new ArrayList<XspressROI>();
		regionList.add(roi1);

		xspressConfigurationParameters = new XspressParameters();
		xspressConfigurationParameters.setDetectorName("xspress2system");
		xspressConfigurationParameters.addDetectorElement(new DetectorElement("element0", 0, 500, 510, false,
				regionList));

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
		beamlinepreparer = PowerMockito.mock(BeamlinePreparer.class);
		detectorPreparer = PowerMockito.mock(DetectorPreparer.class);
		samplePreparer = PowerMockito.mock(SampleEnvironmentPreparer.class);
		outputPreparer = PowerMockito.mock(OutputPreparer.class);
		commandQueueProcessor = PowerMockito.mock(Processor.class);
		datawriterconfig = new AsciiDataWriterConfiguration();
		metashop = new NXMetaDataProvider();
		XASLoggingScriptController = PowerMockito.mock(LoggingScriptController.class);

		energy_scannable = createMockMotorScannable("energy_scannable");
		x_scannable = createMockMotorScannable("x_scannable");
		y_scannable = createMockMotorScannable("y_scannable");
		z_scannable = createMockMotorScannable("z_scannable");

		// create XasScan object
		mapscan = new StepMap(beamlinepreparer, detectorPreparer, samplePreparer, outputPreparer,
				commandQueueProcessor, XASLoggingScriptController, datawriterconfig,
				new ArrayList<AsciiMetadataConfig>(), energy_scannable, metashop, true, ionchambers, x_scannable,
				y_scannable, z_scannable, null);

		// create the beans and give to the XasScan
		Region region = new Region();
		region.setEnergy(7000.0);
		region.setStep(3.0);
		region.setTime(1.0);

		mapscanParams = new MicroFocusScanParameters();
		mapscanParams.setEnergy(7000.);
		mapscanParams.setXStart(0.0);
		mapscanParams.setYStart(10.);
		mapscanParams.setXEnd(1.0);
		mapscanParams.setYEnd(20.);
		mapscanParams.setXStepSize(1.0);
		mapscanParams.setYStepSize(5.0);
		mapscanParams.setRaster(false);
		mapscanParams.setCollectionTime(1.);
		mapscanParams.setZValue(6.0);

		Set<IonChamberParameters> ionParamsSet = makeIonChamberParameters();

		FluorescenceParameters fluoParams = new FluorescenceParameters();
		fluoParams.setCollectDiffractionImages(false);
		fluoParams.setDetectorType("Xspress");
		for (IonChamberParameters params : ionParamsSet) {
			fluoParams.addIonChamberParameter(params);
		}

		DetectorGroup transmissionDetectors = new DetectorGroup("Xspress", new String[] { "ionchambers" });
		List<DetectorGroup> detectorGroups = new ArrayList<DetectorGroup>();
		detectorGroups.add(transmissionDetectors);

		detParams = new DetectorParameters();
		detParams.setFluorescenceParameters(fluoParams);
		detParams.setExperimentType(DetectorParameters.FLUORESCENCE_TYPE);
		detParams.setDetectorGroups(detectorGroups);

		// sampleParams = new SampleParametersTestImpl();
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

	private ScannableMotor createMockMotorScannable(String name) throws DeviceException {
		ScannableMotor newMock = PowerMockito.mock(ScannableMotor.class);
		Mockito.when(newMock.getName()).thenReturn(name);
		Mockito.when(newMock.getInputNames()).thenReturn(new String[] { name });
		Mockito.when(newMock.getExtraNames()).thenReturn(new String[] {});
		Mockito.when(newMock.getOutputFormat()).thenReturn(new String[] { "%.2f" });
		Mockito.when(newMock.getPosition()).thenReturn(7000.0);

		return newMock;
	}

	@Test
	public void testSingleMapScan() {

		try {

			// see also the annotations at the top of this class

			// this section makes sure that any scans created via the factory method
			// ScannableCommands.createConcurrentScan() are a mocked version, so this unit test is not running a real
			// scan: we only want to unit test the class not the scan it runs.
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

			

			SampleEnvironmentIterator it = PowerMockito.mock(SampleEnvironmentIterator.class);
			Mockito.when(it.getNumberOfRepeats()).thenReturn(1);
			Mockito.when(it.getNextSampleName()).thenReturn("My sample");
			Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());
			Mockito.when(samplePreparer.createIterator("Fluorescence")).thenReturn(it);

			mapscan.doCollection(sampleParams, mapscanParams, detParams, outputParams, xspressConfigurationParameters,
					experimentalFullPath, 1);

			// check that the the correct order of preparers and scan were called
			InOrder inorder = Mockito.inOrder(beamlinepreparer,detectorPreparer,samplePreparer,outputPreparer,it,outputParams,mockScan);
			
			inorder.verify(beamlinepreparer).configure(mapscanParams, detParams, sampleParams, outputParams,
					experimentalFullPath);
			inorder.verify(detectorPreparer).configure(mapscanParams, detParams, outputParams, experimentalFullPath);
			inorder.verify(samplePreparer).configure(mapscanParams, sampleParams);
			inorder.verify(outputPreparer).configure(outputParams, mapscanParams, detParams);

			inorder.verify(samplePreparer).createIterator("Fluorescence");
			inorder.verify(beamlinepreparer).prepareForExperiment();

			// iterator is always called, even if it only does one repetition
			inorder.verify(it).resetIterator();
			inorder.verify(it).next();
			inorder.verify(it).getNextSampleName();
			inorder.verify(it).getNextSampleDescriptions();

			inorder.verify(outputParams).getBeforeScriptName();

			inorder.verify(detectorPreparer).beforeEachRepetition();
			inorder.verify(outputPreparer).beforeEachRepetition();

			// need to test that the args given to the scan were correct
			Object[] args = mapscan.createScanArguments("sample 1", new ArrayList<String>());
			org.junit.Assert.assertTrue(args[0] instanceof Scannable);
			org.junit.Assert.assertTrue(y_scannable.getName().equals(((Scannable) args[0]).getName()));
			org.junit.Assert.assertTrue(args[4] instanceof Scannable);
			org.junit.Assert.assertTrue(x_scannable.getName().equals(((Scannable) args[4]).getName()));
			org.junit.Assert.assertTrue(args[8] instanceof Scannable);
			org.junit.Assert.assertTrue(z_scannable.getName().equals(((Scannable) args[8]).getName()));
			org.junit.Assert.assertTrue(args[9] instanceof Detector);
			org.junit.Assert.assertTrue(ionchambers.getName().equals(((Scannable) args[9]).getName()));

//			inorder.verify(mockScan).runScan();

			inorder.verify(outputParams).getAfterScriptName();
			inorder.verify(detectorPreparer).completeCollection();
			inorder.verify(beamlinepreparer).completeExperiment();

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

}
