/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.meta;

import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.NXDetectorData;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionCalibrationReader;
import uk.ac.gda.server.ncd.msg.NcdMetaType;

public class NcdCalibration extends PerVisitExternalNcdMetadata {
	private static final Logger logger = LoggerFactory.getLogger(NcdCalibration.class);
	// "/entry/instrument/detector/"

	public NcdCalibration() {
		setMetaType(NcdMetaType.CALIBRATION);
	}

	@Override
	protected void checkFile(String file, String internal) {
		super.checkFile(file, internal);
		try {
			IDiffractionMetadata md = NexusDiffractionCalibrationReader.getDiffractionMetadataFromNexus(file, null);
			if (md == null) {
				throw new IllegalArgumentException("Could not get diffraction metadata from file");
			}
			DetectorProperties dp = md.getDetector2DProperties();
			if (dp == null) {
				throw new IllegalArgumentException("Could not get detector properties from file");
			}
		} catch (DatasetException e) {
			logger.error("Could not get diffraction metadata from file", e);
			throw new IllegalArgumentException("Could not get metadata from file");
		}
	}

	@Override
	public void write(NXDetectorData nxdata, String treeName) {
		logger.debug("Writing calibration from {}", getFilepath());
		INexusTree detTree = nxdata.getDetTree(treeName);

		try {
			IDiffractionMetadata md = NexusDiffractionCalibrationReader.getDiffractionMetadataFromNexus(getFilepath(), null);
			DetectorProperties dp = md.getDetector2DProperties();

			//distance
			nxdata.addData(treeName, "distance", NexusGroupData.createFromDataset(DatasetFactory.createFromObject(new double[] {dp.getBeamCentreDistance()})), "mm", null, null, false);

			//beam center
			double[] bc = dp.getBeamCentreCoords();
			nxdata.addData(treeName, "beam_center_x", NexusGroupData.createFromDataset(DatasetFactory.createFromObject(new double[] {bc[0]})), null, null, null, false);
			nxdata.addData(treeName, "beam_center_y", NexusGroupData.createFromDataset(DatasetFactory.createFromObject(new double[] {bc[1]})), null, null, null, false);

			// pixel size
			nxdata.addData(treeName, "x_pixel_size", NexusGroupData.createFromDataset(DatasetFactory.createFromObject(new double[] {dp.getHPxSize()})), "mm", null, null, false);
			nxdata.addData(treeName, "y_pixel_size", NexusGroupData.createFromDataset(DatasetFactory.createFromObject(new double[] {dp.getVPxSize()})), "mm", null, null, false);

			//calibration file note
			NexusTreeNode groupTreeNode = new NexusTreeNode("calibration_file", "NXnote", detTree.getNode(treeName), NexusGroupData.createFromDataset(DatasetFactory.createFromObject(new String[] {getFilepath()})));
			detTree.addChildNode(groupTreeNode);
		} catch (DatasetException e) {
			logger.error("Could not read Detector properties from calibration file", e);
			InterfaceProvider.getTerminalPrinter().print("Error writing calibration data to file: " + e.getMessage());
		}
	}
}