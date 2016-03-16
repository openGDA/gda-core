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

import java.io.File;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.nexus.extractor.NexusExtractor;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

public class NcdPilatusDetector extends NcdSubDetector implements LastImageProvider {
	private static final Logger logger = LoggerFactory.getLogger(NcdPilatusDetector.class);
	private String nexusFileUrl = null;
	private String nexusFileName;
	private NexusFile file;
	private int scanDataPoint;
	private GroupNode group;

	@Override
	public Dataset readLastImage() throws DeviceException {
		double[] data = (double[])detector.getAttribute("ReadLastImage");
		int[] dims = getDataDimensions();
		DoubleDataset ds = new DoubleDataset(data, dims);
		return ds;
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		try {
			setupNexusFile(getDetectorType().toLowerCase());
			String filename = nexusFileName;
			dataTree.addScanFileLink(getTreeName(), "nxfile://" + filename + "#entry/instrument/detector/data");
			writeSubFile(frames);
		} catch (Exception e) {
			throw new DeviceException("error getting HDF file name", e);
		}

		addMetadata(dataTree);
	}

	@Override
	public void atScanStart() throws DeviceException {
		detector.atScanStart();
		try {
			scanDataPoint = 0;
			file = NexusFileHDF5.createNexusFile(setupNexusFile(getDetectorType().toLowerCase()));
			file.setDebug(true);
			StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), "entry", NexusExtractor.NXEntryClassName);
			NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
			NexusUtils.addToAugmentPath(path, "detector", NexusExtractor.NXDetectorClassName);
			group = file.getGroup(path.toString(), true);
		} catch (Exception e) {
			logger.error("{} - Unable to create nexus file {}", getName(), nexusFileUrl);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		try {
			file.close();
		} catch (NexusException e) {
			logger.error("{} - Error closing hdf5 file {}", getName(), nexusFileUrl, e);
		}
	}

	private void writeSubFile(int frames) {
		try {
			int[] dims = detector.getDataDimensions();
			int[] datadims = new int[] {ILazyWriteableDataset.UNLIMITED , frames, dims[0], dims[1] };
			// Open data array.
			int rank = datadims.length;
			int[] slabdatadims = new int[] { 1, 1, dims[0], dims[1] };

			ILazyWriteableDataset lazy;
			DataNode data;
			if (scanDataPoint == 0) {
				lazy = NexusUtils.createLazyWriteableDataset("data", Dataset.INT32, slabdatadims, datadims, slabdatadims);
				data = file.createData(group, lazy);
			} else {
				data = file.getData(group, "data");
				lazy = data.getWriteableDataset();
			}
			int[] startPos = new int[rank];
			int[] stop = slabdatadims.clone();

			for (int i = 0; i < frames; i++) {
				startPos[0] = scanDataPoint;
				stop[0] = startPos[0] + 1;
				startPos[1] = i;
				stop[1] = startPos[1] + 1;
				detector.setAttribute("ImageToReadout", (scanDataPoint*frames + i));
				Dataset d = DatasetFactory.createFromObject(detector.readout()).reshape(slabdatadims);
				lazy.setSlice(null, d, SliceND.createSlice(lazy, startPos, stop));
			}
			scanDataPoint++;
		} catch (Exception e) {
			logger.error("{} - Error writing hdf5 file '{}'", getName(), nexusFileUrl, e);
		}
	}

	private String setupNexusFile(String type) {
		String beamline;
		String dataDir;
		try {
			Metadata metadata = GDAMetadataProvider.getInstance();
			beamline = metadata.getMetadataValue("instrument", "gda.instrument", null);
			// If the beamline name isn't set then default to 'base'.
			if (beamline == null) {
				// If the beamline name is not set then use 'base'
				beamline = "base";
			}
			NumTracker numTracker = new NumTracker(beamline);
			long scanNumber = numTracker.getCurrentFileNumber();

			// Check to see if the data directory has been defined.
			dataDir = PathConstructor.createFromDefaultProperty();
			if (dataDir == null) {
				// this java property is compulsory - stop the scan
				throw new InstantiationException("cannot work out data directory - cannot create a new data file.");
			}
			// construct filename
				nexusFileName = beamline + "-" + scanNumber + "-" + type + ".nxs";

			if (!dataDir.endsWith(File.separator)) {
				dataDir += File.separator;
			}
			nexusFileUrl = dataDir + nexusFileName;
		} catch (Exception ex) {
			logger.error("{} - Failed to create file ({})", getName(), nexusFileUrl, ex);
		}
		return nexusFileUrl;
	}

	public boolean isDetectorConfigured() {
		boolean reply;
		reply = ((DeviceBase)detector).isConfigured();
		logger.debug("Detector {} is configured {}", detector.getName(), reply);
		return reply;
	}
}