/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;
import gda.factory.Finder;

public class NcdMarDetector extends NcdSubDetector implements LastImageProvider {

	String marName;
	private final int[] dims = new int[] { 2048, 1024, 512 };
	private FloatDataset lastData;

	@Override
	public void configure(){
		if (detector == null && marName != null)
			detector = (Detector) Finder.getInstance().find(marName);
	}


	@Override
	public List<DataDimension> getSupportedDimensions() throws DeviceException {
        ArrayList<DataDimension> supportedDimensions = new ArrayList<DataDimension>();
        for (int i=0; i<dims.length; i++) {
            supportedDimensions.add(new DataDimension(dims[i], dims[i]));
        }
        return supportedDimensions;
	}

	@Override
	public void setDataDimensions(int[] detectorSize) throws DeviceException {
		int mode = 2048/detectorSize[0]*2;
		detector.setAttribute("binning mode", mode);
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		if (detector == null) {
			return new int[] {2048, 2048};
		}
		return detector.getDataDimensions();
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		int[] datadims = getDataDimensions();
		float[] data = (float[]) detector.readout();

		lastData = DatasetFactory.createFromObject(FloatDataset.class, data, datadims);

		NexusGroupData ngd = new NexusGroupData(ArrayUtils.add(datadims, 0, 1), data);
		ngd.isDetectorEntryData = true;
		dataTree.addData(getName(), ngd, "counts", 1);

		addMetadata(dataTree);
	}

	@Override
	protected void addMetadata(NXDetectorData nxdata) throws DeviceException {
		NexusGroupData ngd;
		INexusTree detTree = nxdata.getDetTree(getTreeName());

		if (detectorType != null) {
			ngd = new NexusGroupData(detectorType);
			ngd.isDetectorEntryData = false;

			NexusTreeNode type_node = new NexusTreeNode("sas_type", NexusExtractor.SDSClassName, null, ngd);
			type_node.setIsPointDependent(false);

			detTree.addChildNode(type_node);
		}

		if (getPixelSize() != 0.0) {
			ngd = new NexusGroupData(getPixelSize());
			ngd.isDetectorEntryData = false;


			for(String label: new String[]{"x_pixel_size", "y_pixel_size"}) {
				NexusTreeNode type_node = new NexusTreeNode(label, NexusExtractor.SDSClassName, null, ngd);
				type_node.setIsPointDependent(false);
				type_node.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, type_node, new NexusGroupData("m")));

				detTree.addChildNode(type_node);
			}
		}
	}

	public String getMarName() {
		return marName;
	}

	public void setMarName(String marName) {
		this.marName = marName;
	}

	@Override
	public double getPixelSize() throws DeviceException {
		return pixelSize / getDataDimensions()[0];
	}

	@Override
	public void setPixelSize(double pixelsize) throws DeviceException {
		this.pixelSize = pixelsize * getDataDimensions()[0];
	}

	@Override
	public Dataset readLastImage() throws DeviceException {
		return lastData;
	}
}