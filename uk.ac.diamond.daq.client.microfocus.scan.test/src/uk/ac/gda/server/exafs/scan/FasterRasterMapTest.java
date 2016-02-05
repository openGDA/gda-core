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

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import gda.device.CounterTimer;
import gda.device.scannable.RealPositionReader;
import gda.jython.scriptcontroller.ScriptControllerBase;
import uk.ac.gda.client.microfocus.scan.MapFactory;

public class FasterRasterMapTest extends RasterMapTest {

	@Override
	protected void createMapScan() {

		MapFactory theFactory = new MapFactory();

		theFactory.setBeamlinePreparer(testHelper.getBeamlinepreparer());
		theFactory.setDetectorPreparer(testHelper.getDetectorPreparer());
		theFactory.setSamplePreparer(testHelper.getSamplePreparer());
		theFactory.setOutputPreparer(testHelper.getOutputPreparer());
		theFactory.setLoggingScriptController(testHelper.getXASLoggingScriptController());
		theFactory.setDatawriterconfig(testHelper.getDatawriterconfig());
		theFactory.setEnergyScannable(testHelper.getEnergy_scannable());
		theFactory.setEnergyNoGapScannable(testHelper.getEnergy_scannable());
		theFactory.setEnergyWithGapScannable(testHelper.getEnergy_scannable());
		theFactory.setMetashop(testHelper.getMetashop());
		theFactory.setIncludeSampleNameInNexusName(true);
		theFactory.setScanName("mapScan");

		theFactory.setCounterTimer(Mockito.mock(CounterTimer.class));
		theFactory.setxScan(x_traj_scannable);
		theFactory.setyScan(testHelper.getY_scannable());
		theFactory.setzScan(testHelper.getZ_scannable());
		theFactory.setElementListScriptController(Mockito.mock(ScriptControllerBase.class));

		theFactory.setRasterMapDetectorPreparer(testHelper.getDetectorPreparer());
		theFactory.setTrajectoryMotor(x_traj_scannable);
		theFactory.setPositionReader(PowerMockito.mock(RealPositionReader.class));

		mapscan = theFactory.createFasterRasterMap();
	}

}
