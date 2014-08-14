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

import gda.device.DeviceException;
import gda.device.DeviceBase;
import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.nexus.NexusFileWrapper;
import gda.device.detector.NXDetectorData;
import java.io.File;

//import org.nexusformat.NXlink;
import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NcdPilatusDetector extends NcdSubDetector implements LastImageProvider {
	private static final Logger logger = LoggerFactory.getLogger(NcdPilatusDetector.class);
	private String nexusFileUrl = null;
	private String nexusFileName;
	private NeXusFileInterface file;
	private NexusFileWrapper nfw;
	private int scanDataPoint;

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
			file = new NexusFile(setupNexusFile(getDetectorType().toLowerCase()), NexusFile.NXACC_CREATE5);
			nfw = new NexusFileWrapper(file);
			nfw.makegroup("entry", "NXentry");
			nfw.opengroup("entry", "NXentry");
			nfw.makegroup("instrument", "NXinstrument");
			nfw.opengroup("instrument", "NXinstrument");
			nfw.makegroup("detector", "NXdetector");
			nfw.opengroup("detector", "NXdetector");
		} catch (Exception e) {
			logger.error("Unable to create nexus file " + nexusFileUrl);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		try {
			nfw.closedata(); // Close data
			nfw.closegroup(); // Close NXdetector
			nfw.closegroup(); // close NXinstrument
			nfw.closegroup(); // Close NXentry
			nfw.close();
		} catch (NexusException e) {
			logger.error("Error closing hdf5 file "+ nexusFileUrl + " : " + e.getMessage());
		} 
	}
	
	private void writeSubFile(int frames) {
		try {
			int[] dims = detector.getDataDimensions();
			int[] datadims = new int[] {NexusFile.NX_UNLIMITED , frames, dims[0], dims[1] };
			// Open data array.
			int rank = datadims.length;
			if (scanDataPoint == 0) {
				nfw.makedata("data", NexusFile.NX_INT32, rank, datadims);
				nfw.opendata("data");
			}
			int[] startPos = new int[rank];
			int[] slabdatadims = new int[] { 1, 1, dims[0], dims[1] };

			for (int i = 0; i < frames; i++) {
				startPos[0] = scanDataPoint;
				startPos[1] = i;
				detector.setAttribute("ImageToReadout", (scanDataPoint*frames + i));
				nfw.putslab(detector.readout(), startPos, slabdatadims);
			}
			scanDataPoint++;
		} catch (Exception e) {
			logger.error("Error writing hdf5 file "+ nexusFileUrl + " : " + e.getMessage());
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
			String error = "Failed to create file (" + nexusFileUrl;
			error += ")";
			logger.error(error, ex);
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