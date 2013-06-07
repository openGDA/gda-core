/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.scan.ScanInformation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NexusFile;

import static gda.device.detector.addetector.ADDetector.readoutArrayIntoNXDetectorData;

public class PVArrayPlugin extends NullNXPlugin {
	

	private ReadOnlyPV<Float[]> pv;


	public PVArrayPlugin(String pvName) {
		pv = LazyPVFactory.newReadOnlyFloatArrayPV(pvName);
	}

	@Override
	public String getName() {
		return "pv_array_plugin";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}
	
	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {

		float[] floats;
		try {
			floats = ArrayUtils.toPrimitive(pv.get());
		} catch (IOException e) {
			throw new RuntimeException("Failed to get data from " +pv.getPvName(), e);
		}
		
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(new NXDetectorDataArrayAppender(floats));
		return appenders;
	}

}
class NXDetectorDataArrayAppender implements NXDetectorDataAppender {


	final private float[] arrayData;

	NXDetectorDataArrayAppender(float[] arrayData) {
		this.arrayData = arrayData;
		
	}
	
	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException{
		try {
			int[] dims = new int[] {arrayData.length};
			
			int nexusType =  NexusFile.NX_FLOAT64;
			data.addData(detectorName, "data", dims , nexusType, arrayData, "count", 1);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
}