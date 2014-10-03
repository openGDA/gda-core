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
import gda.commandqueue.Processor;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.device.scannable.ScannableMotor;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.scriptcontroller.logging.LoggingScriptController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.beans.exafs.Region;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.server.exafs.scan.beans.SampleParametersTestImpl;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

public class XasScanTest {

	private BeamlinePreparer beamlinepreparer;
	private DetectorPreparer detectorPreparer;
	private SampleEnvironmentPreparer samplePreparer;
	private OutputPreparer outputPreparer;
	private Processor commandQueueProcessor;
	private NXMetaDataProvider metashop;
	private AsciiDataWriterConfiguration datawriterconfig;
	private ScannableMotor energy_scannable;
	private XasScan xasscan;
	private LoggingScriptController XASLoggingScriptController;
	private XanesScanParameters xanesParams;
	private DetectorParameters detParams;
	private SampleParametersTestImpl sampleParams;
	private IOutputParameters outputParams;
	private TfgScalerWithFrames ionchambers;
	private final String experimentalFullPath = "/scratch/test/xml/path";

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
		beamlinepreparer = PowerMockito.mock(BeamlinePreparer.class);
		detectorPreparer = PowerMockito.mock(DetectorPreparer.class);
		samplePreparer = PowerMockito.mock(SampleEnvironmentPreparer.class);
		outputPreparer = PowerMockito.mock(OutputPreparer.class);
		commandQueueProcessor = PowerMockito.mock(Processor.class);
		datawriterconfig = new AsciiDataWriterConfiguration();
		metashop = new NXMetaDataProvider();
		XASLoggingScriptController = PowerMockito.mock(LoggingScriptController.class);

		energy_scannable = PowerMockito.mock(ScannableMotor.class);
		Mockito.when(energy_scannable.getName()).thenReturn("energy_scannable");
		Mockito.when(energy_scannable.getInputNames()).thenReturn(new String[] { "energy_scannable" });
		Mockito.when(energy_scannable.getExtraNames()).thenReturn(new String[] {});
		Mockito.when(energy_scannable.getOutputFormat()).thenReturn(new String[] { "%.2f" });
		Mockito.when(energy_scannable.getPosition()).thenReturn(7000.0);

		// create XasScan object
		xasscan = new XasScan(beamlinepreparer, detectorPreparer, samplePreparer, outputPreparer,
				commandQueueProcessor, XASLoggingScriptController, datawriterconfig,
				new ArrayList<AsciiMetadataConfig>(), energy_scannable, metashop, true);

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

		sampleParams = new SampleParametersTestImpl();

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

			SampleEnvironmentIterator it = PowerMockito.mock(SampleEnvironmentIterator.class);
			Mockito.when(it.getNumberOfRepeats()).thenReturn(1);
			Mockito.when(it.getNextSampleName()).thenReturn("My sample");
			Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());

			Mockito.when(samplePreparer.createIterator("Transmission")).thenReturn(it);

			xasscan.doCollection(sampleParams, xanesParams, detParams, outputParams, experimentalFullPath, 1);

			// check that the the correct order of preparers and scan were called
			Mockito.verify(beamlinepreparer).configure(xanesParams, detParams, sampleParams, outputParams,
					experimentalFullPath);
			Mockito.verify(detectorPreparer).configure(xanesParams, detParams, outputParams, experimentalFullPath);
			Mockito.verify(samplePreparer).configure(sampleParams);
			Mockito.verify(outputPreparer).configure(outputParams, xanesParams, detParams);

			Mockito.verify(samplePreparer).createIterator("Transmission");
			Mockito.verify(beamlinepreparer).prepareForExperiment();

			Mockito.verify(it).resetIterator();
			Mockito.verify(it).next();
			Mockito.verify(it).getNextSampleName();
			Mockito.verify(it).getNextSampleDescriptions();

			Mockito.verify(outputParams).getBeforeScriptName();

			Mockito.verify(detectorPreparer).beforeEachRepetition();
			Mockito.verify(outputPreparer).beforeEachRepetition();
			Mockito.verify(outputPreparer).getPlotSettings();

			Mockito.verify(outputParams).getSignalList();

			for (int i = 0; i < 7; i++) {
				Mockito.verify(energy_scannable).moveTo(7000.0 + (i * 3.0));
				Mockito.verify(ionchambers, Mockito.times(8)).collectData();
				Mockito.verify(ionchambers, Mockito.times(8)).readout();
			}

			Mockito.verify(outputParams).getAfterScriptName();
			Mockito.verify(detectorPreparer).completeCollection();
			Mockito.verify(beamlinepreparer).completeExperiment();

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void testMultipleRepetitionsXanesScan() {
		try {
			SampleEnvironmentIterator it = PowerMockito.mock(SampleEnvironmentIterator.class);
			Mockito.when(it.getNumberOfRepeats()).thenReturn(1);
			Mockito.when(it.getNextSampleName()).thenReturn("My sample");
			Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());

			Mockito.when(samplePreparer.createIterator("Transmission")).thenReturn(it);

			xasscan.doCollection(sampleParams, xanesParams, detParams, outputParams, experimentalFullPath, 3);

			// check that the the correct order of preparers and scan were called

			// performed after repetition loop
			Mockito.verify(beamlinepreparer).configure(xanesParams, detParams, sampleParams, outputParams,
					experimentalFullPath);
			Mockito.verify(detectorPreparer).configure(xanesParams, detParams, outputParams, experimentalFullPath);
			Mockito.verify(samplePreparer).configure(sampleParams);
			Mockito.verify(outputPreparer).configure(outputParams, xanesParams, detParams);

			Mockito.verify(samplePreparer).createIterator("Transmission");
			Mockito.verify(beamlinepreparer).prepareForExperiment();

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

				for (int i = 0; i < 7; i++) {
					Mockito.verify(energy_scannable, Mockito.times(3)).moveTo(7000.0 + (i * 3.0));
					Mockito.verify(ionchambers, Mockito.times(24)).collectData();
					Mockito.verify(ionchambers, Mockito.times(24)).readout();
				}

				Mockito.verify(outputParams, Mockito.times(3)).getAfterScriptName();
			}
			// end of repetition loop

			// performed after repetition loop
			Mockito.verify(detectorPreparer).completeCollection();
			Mockito.verify(beamlinepreparer).completeExperiment();

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void testSampleEnvironmentLoopsXanesScan() {
		try {
			// 2 sample env loops + 3 repetitions, so 6 scans in total

			SampleEnvironmentIterator it = PowerMockito.mock(SampleEnvironmentIterator.class);
			Mockito.when(it.getNumberOfRepeats()).thenReturn(2);
			Mockito.when(it.getNextSampleName()).thenReturn("My sample");
			Mockito.when(it.getNextSampleDescriptions()).thenReturn(new ArrayList<String>());

			Mockito.when(samplePreparer.createIterator("Transmission")).thenReturn(it);

			xasscan.doCollection(sampleParams, xanesParams, detParams, outputParams, experimentalFullPath, 3);

			// check that the the correct order of preparers and scan were called

			// performed after repetition loop
			Mockito.verify(beamlinepreparer).configure(xanesParams, detParams, sampleParams, outputParams,
					experimentalFullPath);
			Mockito.verify(detectorPreparer).configure(xanesParams, detParams, outputParams, experimentalFullPath);
			Mockito.verify(samplePreparer).configure(sampleParams);
			Mockito.verify(outputPreparer).configure(outputParams, xanesParams, detParams);

			Mockito.verify(samplePreparer).createIterator("Transmission");
			Mockito.verify(beamlinepreparer).prepareForExperiment();

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

					for (int i = 0; i < 7; i++) {
						Mockito.verify(energy_scannable, Mockito.times(6)).moveTo(7000.0 + (i * 3.0));
						Mockito.verify(ionchambers, Mockito.times(48)).collectData();
						Mockito.verify(ionchambers, Mockito.times(48)).readout();
					}

					Mockito.verify(outputParams, Mockito.times(6)).getAfterScriptName();
				}
				// end of sam env loop
			}
			// end of repetition loop

			// performed after repetition loop
			Mockito.verify(detectorPreparer).completeCollection();
			Mockito.verify(beamlinepreparer).completeExperiment();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
