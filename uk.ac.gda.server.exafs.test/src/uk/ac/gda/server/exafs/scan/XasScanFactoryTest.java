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

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gda.data.metadata.NXMetaDataProvider;
import gda.device.Scannable;
import gda.device.scannable.ContinuouslyScannable;
import gda.jython.scriptcontroller.logging.LoggingScriptController;

public class XasScanFactoryTest {

	private BeamlinePreparer beamlinePreparer;
	private DetectorPreparer detectorPreparer;
	private OutputPreparer outputPreparer;
	private SampleEnvironmentPreparer samplePreparer;
	private LoggingScriptController loggingScriptController;
	private Scannable energyScannable;
	private NXMetaDataProvider metashop;
	private QexafsDetectorPreparer qexafsDetectorPreparer;
	private ContinuouslyScannable qexafsEnergyScannable;

	@Before
	public void setup() {
		// mock all the objects which would be used to create the XasScan objects

		// have not mocked ArrayList<AsciiMetadataConfig> original_header
		beamlinePreparer = mock(BeamlinePreparer.class);
		detectorPreparer = mock(DetectorPreparer.class);
		qexafsDetectorPreparer = mock(QexafsDetectorPreparer.class);
		samplePreparer = mock(SampleEnvironmentPreparer.class);
		outputPreparer = mock(OutputPreparer.class);
		loggingScriptController = mock(LoggingScriptController.class);
		energyScannable = mock(Scannable.class);
		qexafsEnergyScannable = mock(ContinuouslyScannable.class);
		metashop = mock(NXMetaDataProvider.class);
	}

	@Test
	public void testCanCreateEnergyScan() {

		XasScanFactory theFactory = new XasScanFactory();

		theFactory.setBeamlinePreparer(beamlinePreparer);
		theFactory.setDetectorPreparer(detectorPreparer);
		theFactory.setSamplePreparer(samplePreparer);
		theFactory.setOutputPreparer(outputPreparer);
		theFactory.setLoggingScriptController(loggingScriptController);
		theFactory.setEnergyScannable(energyScannable);
		theFactory.setMetashop(metashop);
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("energyScan");

		EnergyScan energyScan = theFactory.createEnergyScan();

		if (energyScan == null) {
			Assert.fail("Null returned from factory");
		}
	}

	@Test
	public void testCannotCreateQexafsScanWithoutExtraParameters() {

		XasScanFactory theFactory = new XasScanFactory();

		theFactory.setBeamlinePreparer(beamlinePreparer);
		theFactory.setDetectorPreparer(detectorPreparer);
		theFactory.setSamplePreparer(samplePreparer);
		theFactory.setOutputPreparer(outputPreparer);
		theFactory.setLoggingScriptController(loggingScriptController);
		theFactory.setEnergyScannable(energyScannable);
		theFactory.setMetashop(metashop);
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("energyScan");

		try {
			QexafsScan scan = theFactory.createQexafsScan();

			if (scan != null) {
				Assert.fail("Scan returned from factory when it was not given the correct parameters");
			}
		} catch (IllegalArgumentException e) {
			// this is what we are expecting
			return;
		} catch (Exception e) {
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test
	public void createIncompleteEnergyScan() {
		XasScanFactory theFactory = new XasScanFactory();

		theFactory.setBeamlinePreparer(beamlinePreparer);
		theFactory.setDetectorPreparer(detectorPreparer);
		theFactory.setSamplePreparer(samplePreparer);
		theFactory.setOutputPreparer(outputPreparer);
		theFactory.setLoggingScriptController(loggingScriptController);
		theFactory.setMetashop(metashop);
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("energyScan");

		try {
			theFactory.createEnergyScan();
		} catch (Exception e) {
			// this is what we are expecting
			return;
		}
		Assert.fail("Expected exception was not caught.");
	}

	@Test
	public void createQexafsScan() {
		XasScanFactory theFactory = new XasScanFactory();

		theFactory.setBeamlinePreparer(beamlinePreparer);
		theFactory.setQexafsDetectorPreparer(qexafsDetectorPreparer);
		theFactory.setSamplePreparer(samplePreparer);
		theFactory.setOutputPreparer(outputPreparer);
		theFactory.setLoggingScriptController(loggingScriptController);
		theFactory.setQexafsEnergyScannable(qexafsEnergyScannable);
		theFactory.setMetashop(metashop);
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("Qexafs");

		QexafsScan scan = theFactory.createQexafsScan();

		if (scan == null) {
			Assert.fail("Null returned from factory");
		}
	}

	@Test
	public void createIncompleteQexafsScan() {
		XasScanFactory theFactory = new XasScanFactory();

		theFactory.setBeamlinePreparer(beamlinePreparer);
		theFactory.setQexafsDetectorPreparer(qexafsDetectorPreparer);
		theFactory.setSamplePreparer(samplePreparer);
		theFactory.setOutputPreparer(outputPreparer);
		theFactory.setLoggingScriptController(loggingScriptController);
		theFactory.setMetashop(metashop);
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("Qexafs");

		try {
			theFactory.createEnergyScan();
		} catch (Exception e) {
			// this is what we are expecting
			return;
		}
		Assert.fail("Expected exception was not caught.");
	}

}
