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

package uk.ac.diamond.daq.detectors.addetector;

import java.io.File;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.sequencer.nexus.SolsticeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.addetector.filewriter.MultipleImagesPerHDF5FileWriter;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.factory.Finder;
import uk.ac.diamond.daq.detectors.addetector.api.AreaDetectorRunnableDeviceModel;

/**
 * <p>
 * This is an implementation of a new style GDA detector that can connects to an area detector.
 * </p>
 * <p>
 * The area detector directly writes its own HDF5 file which is linked to the GDA NeXus file.
 * </p>
 *
 * @author James Mudd
 */
public abstract class AbstractAreaDetectorWritingFilesRunnableDevice extends AreaDetectorRunnableDevice {

	private static final Logger logger = LoggerFactory.getLogger(AbstractAreaDetectorWritingFilesRunnableDevice.class);

	// This is the path within the HDF5 file that the AD writes to the data block
	private static final String PATH_TO_DATA_NODE = "/entry/instrument/detector/data";
	private static final String DETECTOR_FILE_EXTENSION = ".hdf5";
	private static final String FIELD_NAME_STATS_TOTAL = "total";
	private static final String PATH_TO_STATS_TOTAL_NODE = "/entry/instrument/NDAttributes/StatsTotal";
	private static final String PATH_TO_UNIQUE_KEYS_NODE = "/entry/instrument/NDAttributes/NDArrayUniqueId";

	private String fileName;
	private ScanInformation information;

	@PreConfigure
	public void preConfigure(ScanInformation info) {
		this.information = info;
	}

	protected void configureAdDetector(AreaDetectorRunnableDeviceModel model) throws ScanningException {
		// Get the detector by name defined in the model
		adDetector = Finder.getInstance().find(model.getName());
		if (adDetector == null) {
			throw new ScanningException("Could not find detector: " + model.getName());
		}
		try {
			adDetector.setCollectionTime(model.getExposureTime());
			adDetector.atScanStart();
		} catch (DeviceException e) {
			final String message = "Configuring ADDetector failed";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}
		// FIXME Need to configure the plugin chain here (or in the collection strategy)
	}

	protected void configureFileWriting() throws ScanningException {
		try {
			// Setup the file writing
			final NDFile ndFile = adDetector.getNdFile();
			// TODO this is a bit unsafe just cast it. Maybe ADDetector should hold a NdFileHDF5 itself?
			final NDFileHDF5 ndFileHDF5 = ((MultipleImagesPerHDF5FileWriter) adDetector.getFileWriter()).getNdFileHDF5();

			// Setup the HDF plugin
			// File template just merge path and file name (ignore file numbering in area detector)
			final String filetemplate = "%s%s";
			ndFileHDF5.setFileTemplate(filetemplate);
			// Just use a File object for nice methods
			final File scanFile = new File(getBean().getFilePath());
			// Set path
			ndFileHDF5.setFilePath(scanFile.getParent());

			// For the name remove the .nxs and add the detector name and file extension
			fileName = scanFile.getName().replace(".nxs", "") + "-" + getName() + DETECTOR_FILE_EXTENSION;
			ndFileHDF5.setFileName(fileName);

			// Store attributes allows stats output to be written to file
			ndFileHDF5.setStoreAttr(1);
			ndFileHDF5.setStoreAttributesByDimension(true);
			// Lazy open must be true so that the file doesn't exist when the link is made
			// Also it allows the first frame received to be used to setup the dimensions
			ndFileHDF5.setLazyOpen(true);
			ndFile.setFileWriteMode(FileWriteMode.STREAM);

			// FIXME This only handles raster type scans (not snake) and ones in a line square or cube.
			// Think the solution is to use the POS plugin to tell AD in advance where the frames are going
			final int[] scanShape = information.getShape();
			switch (information.getRank()) {
			case 1: // 1D Scan
				ndFileHDF5.setNumExtraDims(0); // 1D scan so no extra dims
				ndFileHDF5.setNumCapture(scanShape[0]);
				break;
			case 2: // 2D Scan (like a map)
				ndFileHDF5.setNumExtraDims(1);
				ndFileHDF5.setExtraDimSizeN(scanShape[1]);
				ndFileHDF5.setExtraDimSizeX(scanShape[0]);
				break;
			case 3: // 3D Scan (like a map at multiple temperatures)
				ndFileHDF5.setNumExtraDims(2);
				ndFileHDF5.setExtraDimSizeN(scanShape[2]);
				ndFileHDF5.setExtraDimSizeX(scanShape[1]);
				ndFileHDF5.setExtraDimSizeY(scanShape[0]);
				break;
			default:
				throw new DeviceException(
						"Area Detector can't handle file writing when scan is >3D. Scan dimensions = " + scanShape.length) ;
			}

			// Start capture and check it worked.
			ndFile.startCapture();
			Thread.sleep(100); // Give a chance for it to happen!
			if (ndFile.getCapture_RBV() != 1) {
				throw new DeviceException("Capture not ready!");
			}
		} catch (Exception e) {
			final String message = "Error configuring file writing";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}
	}

	@PostConfigure
	public void postConfigure() {
		this.information = null; // Just to avoid memory leaks
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo scanInfo) {
		final NXdetector nxDetector = NexusNodeFactory.createNXdetector();

		nxDetector.setCount_timeScalar(model.getExposureTime());

		// The link is relative and relies on the AD file and the NeXus being in the same directory
		nxDetector.addExternalLink(NXdetector.NX_DATA, fileName, PATH_TO_DATA_NODE);

		// Add the link for the total
		nxDetector.addExternalLink(FIELD_NAME_STATS_TOTAL, fileName, PATH_TO_STATS_TOTAL_NODE);

		// Add the link to the unique keys
		nxDetector.addExternalLink(SolsticeConstants.PROPERTY_NAME_UNIQUE_KEYS_PATH, fileName, PATH_TO_UNIQUE_KEYS_NODE);

		// Get the NexusOjbectWrapper wrapping the detector
		final NexusObjectWrapper<NXdetector> nexusObjectWrapper = new NexusObjectWrapper<>(getName(), nxDetector);

		final int scanRank = scanInfo.getRank();

		// Set the external file written by this detector which will be linked to
		nexusObjectWrapper.setDefaultExternalFileName(fileName);

		// Setup the primary NXdata. Add 2 to the scan rank as AD returns 2D data
		nexusObjectWrapper.setPrimaryDataFieldName(NXdetector.NX_DATA);
		nexusObjectWrapper.setExternalDatasetRank(NXdetector.NX_DATA, scanRank + 2);

		// Add an additional NXData for the stats total. This is also scanRank + 2 as AD writes [y,x,1,1]
		nexusObjectWrapper.addAdditionalPrimaryDataFieldName(FIELD_NAME_STATS_TOTAL);
		nexusObjectWrapper.setExternalDatasetRank(FIELD_NAME_STATS_TOTAL, scanRank + 2);

		// There is no NXData for the unique keys as it isn't something users should want to plot, but the path
		// property needs to be set, so the unique keys are added to the solstice_scan unique keys

		nexusObjectWrapper.setPropertyValue(SolsticeConstants.PROPERTY_NAME_UNIQUE_KEYS_PATH, PATH_TO_UNIQUE_KEYS_NODE);

		return nexusObjectWrapper;
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {
		// Doesn't need to do anything the area detector is writing the file which is already linked.
		return true;
	}

}
