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

import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.device.CounterTimer;
import gda.device.Scannable;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.RealPositionReader;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import junit.framework.Assert;
import uk.ac.gda.client.microfocus.scan.MapFactory;
import uk.ac.gda.client.microfocus.scan.StepMap;

public class MapFactoryTest {

	private DetectorPreparer detectorPreparer;
	private BeamlinePreparer beamlinePreparer;
	private SampleEnvironmentPreparer samplePreparer;
	private OutputPreparer outputPreparer;
	private LoggingScriptController XASLoggingScriptController;
	private AsciiDataWriterConfiguration datawriterconfig;
//	private ArrayList<AsciiMetadataConfig> original_header;
	private Scannable energyScannable;
	private NXMetaDataProvider metashop;
	private Scannable xScan;
	private Scannable yScan;
	private Scannable zScan;
	private ScriptControllerBase elementListScriptController;
	private CounterTimer counterTimer;
	private ContinuouslyScannable trajectoryMotor;
	private RealPositionReader positionReader;

	@Before
	public void setup() {
		// mock all the objects which would be used to create the XasScan objects
		beamlinePreparer = PowerMockito.mock(BeamlinePreparer.class);
		detectorPreparer = PowerMockito.mock(DetectorPreparer.class);
		samplePreparer = PowerMockito.mock(SampleEnvironmentPreparer.class);
		outputPreparer = PowerMockito.mock(OutputPreparer.class);
		XASLoggingScriptController = PowerMockito.mock(LoggingScriptController.class);
		datawriterconfig = PowerMockito.mock(AsciiDataWriterConfiguration.class);
//		original_header = new ArrayList<AsciiMetadataConfig>();
		energyScannable = PowerMockito.mock(Scannable.class);
		metashop = PowerMockito.mock(NXMetaDataProvider.class);
		counterTimer = PowerMockito.mock(CounterTimer.class);
		xScan = PowerMockito.mock(Scannable.class);
		yScan = PowerMockito.mock(Scannable.class);
		zScan = PowerMockito.mock(Scannable.class);
		elementListScriptController = PowerMockito.mock(ScriptControllerBase.class);

		trajectoryMotor = PowerMockito.mock(ContinuouslyScannable.class);
		positionReader = PowerMockito.mock(RealPositionReader.class);
	}

	@Test
	public void testCanCreatStepMap() {

		MapFactory theFactory = new MapFactory();

		theFactory.setBeamlinePreparer(beamlinePreparer);
		theFactory.setDetectorPreparer(detectorPreparer);
		theFactory.setSamplePreparer(samplePreparer);
		theFactory.setOutputPreparer(outputPreparer);
		theFactory.setLoggingScriptController(XASLoggingScriptController);
		theFactory.setDatawriterconfig(datawriterconfig);
		theFactory.setEnergyNoGapScannable(energyScannable);
		theFactory.setEnergyWithGapScannable(energyScannable);
		theFactory.setMetashop(metashop);
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("mapScan");

		theFactory.setCounterTimer(counterTimer);
		theFactory.setxScan(xScan);
		theFactory.setyScan(yScan);
		theFactory.setzScan(zScan);
		theFactory.setElementListScriptController(elementListScriptController);

		StepMap theScan = theFactory.createStepMap();

		if (theScan == null) {
			Assert.fail("Null returned from factory");
		}

	}

	@Test
	public void testIncompleteStepMapFails() {
		// do not add enough objects to factory so an exception should be thrown

		MapFactory theFactory = new MapFactory();

		theFactory.setBeamlinePreparer(beamlinePreparer);
		theFactory.setDetectorPreparer(detectorPreparer);
		theFactory.setSamplePreparer(samplePreparer);
		theFactory.setOutputPreparer(outputPreparer);
		theFactory.setLoggingScriptController(XASLoggingScriptController);
		theFactory.setDatawriterconfig(datawriterconfig);
		theFactory.setEnergyScannable(energyScannable);
		theFactory.setMetashop(metashop);
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("mapScan");

		theFactory.setCounterTimer(counterTimer);
		theFactory.setxScan(xScan);
		// theFactory.setyScan(yScan);
		theFactory.setzScan(zScan);
		theFactory.setElementListScriptController(elementListScriptController);

		try {
			theFactory.createStepMap();
		} catch (IllegalArgumentException e) {
			// this is what we are expecting
			return;
		} catch (Exception e) {
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}

}
