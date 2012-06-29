/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen;

import gda.data.fileregistrar.FileRegistrarHelper;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.mythen.data.MythenRawDataset;

import org.nexusformat.NexusFile;

public class MythenNexusImpl extends MythenDetectorImpl implements NexusDetector {
	
	
	public MythenNexusImpl(){
		this.inputNames = new String[]{};
		this.extraNames = new String[]{"Mythen"};
		this.outputFormat = new String[]{"%5.5g"};
	}
	
	/**
	 * Holds the processed data in memory instead of writing it to a new file 
	 */
	@Override
	protected void afterCollectData() {
		// read data and process it
		rawData = new MythenRawDataset(rawFile);
		processedData = dataConverter.process(rawData, delta);
		processedFile = null;
		
		FileRegistrarHelper.registerFiles(new String[] {
			rawFile.getAbsolutePath(),
		});
		
		status = IDLE;
	}
	
	@Override
	public NexusTreeProvider readout() throws DeviceException{
		NXDetectorData thisFrame = new NXDetectorData(this);
		thisFrame.addNote(getName(), "Raw file:" + rawFile.getName());
		thisFrame.addData(getName(), new int[] { processedData.getLines().size() }, NexusFile.NX_FLOAT64, processedData.getCountArray(),
				"counts", 1);
		thisFrame.addAxis(getName(), "position", new int[] {processedData.getLines().size()}, NexusFile.NX_FLOAT64, processedData.getAngleArray(), 2, 1, "degrees", false);
		thisFrame.setPlottableValue(getName(), 0.0);
		thisFrame.setDoubleVals(new Double[]{0.0});
		
		return thisFrame;
	}

}
