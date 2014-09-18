/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

import gda.device.DeviceException;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;

import uk.ac.diamond.scisoft.analysis.dataset.Nexus;
import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;

public class ReductionDetectorBase  extends NcdSubDetector {

	protected String key;
	protected Dataset qAxis;
	
	public ReductionDetectorBase(String name, String key) {
		super();
		setName(name);
		this.key = key;
	}
	
	@Override
	public void configure(){
		// no configuration required for this class
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return NcdDetectorSystem.REDUCTION_DETECTOR;
	}

	@Override
	public void start() throws DeviceException {
	}

	@Override
	public void stop() throws DeviceException {
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		addMetadata(nxdata);
	}

	@Override
	public List<DataDimension> getSupportedDimensions() throws DeviceException {
		return null;
	}

	@Override
	public void setDataDimensions(int[] detectorSize) throws DeviceException {
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return null;
	}
	
	@Override
	public String toString() {
		return "Reduction Detector "+getName()+" of class "+getClass()+" working on "+key;
	}
	
	public void setqAxis(IDataset qAxis) {
		this.qAxis = DatasetUtils.convertToDataset(qAxis);
	}

	public Dataset getqAxis() {
		return qAxis;
	}
	
	protected void addQAxis(NXDetectorData nxdata, int axisValue) {
		if (qAxis != null) {
			nxdata.addAxis(getName(), "q", Nexus.createNexusGroupData(qAxis), axisValue, 1, "nm^{-1}", false);
		}
	}
}