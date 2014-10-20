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

import gda.data.PathConstructor;
import gda.jython.InterfaceProvider;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySequence;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.microfocus.scan.datawriter.MicroFocusWriterExtender;

/**
 * The single point of access in the jython environment for map scans. This redirects calls based on its configuration
 * and attributes.
 * <p>
 * Currently only used by I18. This could be applied to other mapping beamlines with a bit of refactoring. It would be
 * better to refactor this than for other beamlines to use another way to keep consistency.
 */
public class MapSelector {

	private final MapScan non_raster;
	private final MapScan raster;
	private final MapScan raster_return_write;

	private MapScan raster_mode;
	private boolean currentMapIsRaster = false;

	public MapSelector(MapScan non_raster, MapScan raster, MapScan raster_return_write) {
		this.non_raster = non_raster;
		this.raster = raster;
		this.raster_return_write = raster_return_write;
		raster_mode = raster;
	}

	public PyObject __call__(PyObject pyArgs) throws Exception {
		String sampleFileName = ((PySequence) pyArgs).__finditem__(0).asString();
		String scanFileName = ((PySequence) pyArgs).__finditem__(1).asString();
		String detectorFileName = ((PySequence) pyArgs).__finditem__(2).asString();
		String outputFileName = ((PySequence) pyArgs).__finditem__(3).asString();
		String folderName = ((PySequence) pyArgs).__finditem__(4).asString()+ "/";
		int numRepetitions = ((PySequence) pyArgs).__finditem__(5).asInt();

//		String datadir = PathConstructor.createFromDefaultProperty() + "/xml/";
//		String xmlFolderName = datadir + folderName + "/";

		// it has to be a MicroFocusScanParameters object or its not a map
		MicroFocusScanParameters scanBean = (MicroFocusScanParameters) BeansFactory.getBeanObject(folderName,
				scanFileName);
		currentMapIsRaster = scanBean.isRaster();
		if (currentMapIsRaster) {
			raster_mode.doCollection(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName,
					numRepetitions);
		} else {
			non_raster.doCollection(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName,
					numRepetitions);
		}
		return new PyInteger(0); 
	}

	public MicroFocusWriterExtender getMFD() {
		if (currentMapIsRaster) {
			return raster_mode.getMFD();
		}
		return non_raster.getMFD();
	}

	public void enableFasterRaster() {
		raster_mode = raster_return_write;
	}

	public void disableFasterRaster() {
		raster_mode = raster;
	}

	public void setStage(int stageNumber) {

		if (stageNumber != 1 && stageNumber != 3) {
			InterfaceProvider.getTerminalPrinter().print("only stages 1 or 3 may be selected");
		}

		// TODO once the map scan class has been ported
//		raster.setStage(stageNumber);
//		raster_return_write.setStage(stageNumber);
	}

}
