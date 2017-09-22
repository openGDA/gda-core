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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import gda.commandqueue.Processor;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.ScannableMotor;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import uk.ac.gda.beans.DetectorROI;
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
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;

/**
 * Supplies common attributes for map test classes.
 */
public class MicrofocusMapTestComponent {

	private BeamlinePreparer beamlinepreparer;
	private DetectorPreparer detectorPreparer;
	private SampleEnvironmentPreparer samplePreparer;
	private OutputPreparer outputPreparer;
	private Processor commandQueueProcessor;
	private NXMetaDataProvider metashop;
	private AsciiDataWriterConfiguration datawriterconfig;
	private ScannableMotor energy_scannable;
	private LoggingScriptController XASLoggingScriptController;
	private MicroFocusScanParameters mapscanParams;
	private DetectorParameters detParams;
	private ISampleParameters sampleParams;
	private IOutputParameters outputParams;
	private final String experimentalFullPath = "/scratch/test/xml/path/";
	private ScannableMotor y_scannable;
	private ScannableMotor z_scannable;
	private XspressParameters xspressConfigurationParameters;
	private JythonServer jythonserver;
	private TfgScalerWithFrames ionchambers;
	private ScannableMotor energy_nogap_scannable;

	public void setup() throws DeviceException {

		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");

		DetectorROI roi1 = new DetectorROI("Fe_k", 500, 520);
		ArrayList<DetectorROI> regionList = new ArrayList<DetectorROI>();
		regionList.add(roi1);

		xspressConfigurationParameters = new XspressParameters();
		xspressConfigurationParameters.setDetectorName("xspress2system");
		xspressConfigurationParameters.addDetectorElement(new DetectorElement("element0", 0, 500, 510, false,
				regionList));

		ClientDetails details = Mockito.mock(ClientDetails.class);
		Mockito.when(details.getVisitID()).thenReturn("0-0");

		ionchambers = PowerMockito.mock(TfgScalerWithFrames.class);
		Mockito.when(ionchambers.getName()).thenReturn("ionchambers");
		Mockito.when(ionchambers.readout()).thenReturn(new double[] { 1.0, 2.0, 3.0 });
		Mockito.when(ionchambers.getExtraNames()).thenReturn(new String[] { "i0", "it", "iref" });
		Mockito.when(ionchambers.getInputNames()).thenReturn(new String[] { "time" });
		Mockito.when(ionchambers.getOutputFormat()).thenReturn(new String[] { "%.2f", "%.2f", "%.2f", "%.2f" });

		JythonServerFacade jythonserverfacade = Mockito.mock(JythonServerFacade.class);
		Mockito.when(jythonserverfacade.getBatonHolder()).thenReturn(details);
		InterfaceProvider.setTerminalPrinterForTesting(jythonserverfacade);
		InterfaceProvider.setAuthorisationHolderForTesting(jythonserverfacade);
		InterfaceProvider.setBatonStateProviderForTesting(jythonserverfacade);
		InterfaceProvider.setJythonNamespaceForTesting(jythonserverfacade);
		InterfaceProvider.setScanStatusHolderForTesting(jythonserverfacade);
		Mockito.when(jythonserverfacade.getFromJythonNamespace("ionchambers")).thenReturn(ionchambers);

		jythonserver = Mockito.mock(JythonServer.class);
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
		energy_nogap_scannable = createMockMotorScannable("energy_nogap_scannable");

		y_scannable = createMockMotorScannable("y_scannable");
		z_scannable = createMockMotorScannable("z_scannable");

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
		mapscanParams.setCollectionTime(1.);
		mapscanParams.setZValue(6.0);
		mapscanParams.setRowTime(10.0);

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

	public ScannableMotor createMockMotorScannable(String name) throws DeviceException {
		return (ScannableMotor) createMockScannable(name, ScannableMotor.class);
	}

	public ContinuouslyScannable createMockContinuosulyScannableMotor(String name) throws DeviceException {
		return (ContinuouslyScannable) createMockScannable(name, ContinuouslyScannable.class);
	}

	public Scannable createMockScannable(String name, Class<? extends Scannable> clazz) throws DeviceException {
		Scannable newMock = PowerMockito.mock(clazz);
		Mockito.when(newMock.getName()).thenReturn(name);
		Mockito.when(newMock.getInputNames()).thenReturn(new String[] { name });
		Mockito.when(newMock.getExtraNames()).thenReturn(new String[] {});
		Mockito.when(newMock.getOutputFormat()).thenReturn(new String[] { "%.2f" });
		Mockito.when(newMock.getPosition()).thenReturn(7000.0);
		return newMock;
	}

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

	public BeamlinePreparer getBeamlinepreparer() {
		return beamlinepreparer;
	}

	public DetectorPreparer getDetectorPreparer() {
		return detectorPreparer;
	}

	public SampleEnvironmentPreparer getSamplePreparer() {
		return samplePreparer;
	}

	public OutputPreparer getOutputPreparer() {
		return outputPreparer;
	}

	public Processor getCommandQueueProcessor() {
		return commandQueueProcessor;
	}

	public NXMetaDataProvider getMetashop() {
		return metashop;
	}

	public AsciiDataWriterConfiguration getDatawriterconfig() {
		return datawriterconfig;
	}

	public ScannableMotor getEnergy_scannable() {
		return energy_scannable;
	}

	public LoggingScriptController getXASLoggingScriptController() {
		return XASLoggingScriptController;
	}

	public MicroFocusScanParameters getMapscanParams() {
		return mapscanParams;
	}

	public DetectorParameters getDetParams() {
		return detParams;
	}

	public ISampleParameters getSampleParams() {
		return sampleParams;
	}

	public IOutputParameters getOutputParams() {
		return outputParams;
	}

	public String getExperimentalFullPath() {
		return experimentalFullPath;
	}

	public ScannableMotor getY_scannable() {
		return y_scannable;
	}

	public ScannableMotor getZ_scannable() {
		return z_scannable;
	}

	public XspressParameters getXspressConfigurationParameters() {
		return xspressConfigurationParameters;
	}

	// public ScanPlotSettings getMockPlotSettings() {
	// return mockPlotSettings;
	// }

	public JythonServer getJythonserver() {
		return jythonserver;
	}

	public TfgScalerWithFrames getIonchambers() {
		return ionchambers;
	}

	public void setDetectorPreparer(DetectorPreparer mock) {
		detectorPreparer = mock;
	}
}
