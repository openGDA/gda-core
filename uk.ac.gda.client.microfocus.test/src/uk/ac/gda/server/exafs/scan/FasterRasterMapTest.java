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

import java.util.ArrayList;

import uk.ac.gda.client.microfocus.scan.FasterRasterMap;
import uk.ac.gda.client.microfocus.scan.RasterMapDetectorPreparer;

public class FasterRasterMapTest extends RasterMapTest{
	
	
	@Override
	protected void createMapScan() {
		mapscan = new FasterRasterMap(testHelper.getBeamlinepreparer(),
				(RasterMapDetectorPreparer) testHelper.getDetectorPreparer(), testHelper.getSamplePreparer(),
				testHelper.getOutputPreparer(), testHelper.getCommandQueueProcessor(),
				testHelper.getXASLoggingScriptController(), testHelper.getDatawriterconfig(),
				new ArrayList<AsciiMetadataConfig>(), testHelper.getEnergy_scannable(), testHelper.getMetashop(), true,
				x_traj_scannable, null, testHelper.getY_scannable(), testHelper.getZ_scannable(), null, null);
	}

}
