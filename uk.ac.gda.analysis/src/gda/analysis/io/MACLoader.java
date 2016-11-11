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

package gda.analysis.io;

import java.io.File;
import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;

/**
 * This class should be used to load MAC (Multi-channel Analyser Crystal) data files into the ScanFileHolder object
 */
public class MACLoader extends uk.ac.diamond.scisoft.analysis.io.SRSLoader {
	private static String[] savedKeys = {
		"CarouselNo",
		"SampleID",
		"SampleName",
		"Description",
		"Title",
		"Comment",
		"RunNumber",
		"ScanTime",
		"MonitorAverageCount",
		"Date",
		"Time",
		"Beamline",
		"Project",
		"Experiment",
		"Wavelength",
		"Temperature",
		"SamplePosition",
	};

	/**
	 * The constructor for the loader which requires the filename of the data file to load.
	 * @param fileName
	 */
	public MACLoader(String fileName) {
		super(fileName);
		if (!fileName.startsWith( File.separator )) {
			// default to the current data directory.
			fileName = getDataDir() + File.separator + fileName;

		}
		this.fileName = fileName;
		logger.info("Load MAC data file from {}", fileName);
	}

	/**
	 * Constructor for MAC file loader using relative (negative value) and absolute file number without the full path,
	 * default data directory will be applied. This method can only handle externally rebinned data in "processing" directory.
	 * @param fileNumber
	 */
	public MACLoader(int fileNumber) {
		if (fileNumber <= 0) {
			// relative file number
			this.fileName = getDataDir() + File.separator + "processing" + File.separator + (getCurrentFileNumber()+fileNumber)+"_red.dat";
		} else {
			this.fileName = getDataDir() + File.separator + "processing" + File.separator + (fileNumber)+"_red.dat";
		}
	}
	/** Constructor for load file with name in the pattern of <code> fileNumber-mac-collectionNumber.dat</code>.
	 *
	 * @param fileNumber
	 * @param collectionNumber
	 */
	public MACLoader(int fileNumber, int collectionNumber) {
		if (fileNumber <= 0) {
			// relative file number
			this.fileName = getDataDir() + File.separator + (getCurrentFileNumber()+fileNumber)
			+ LocalProperties.get("gda.data.file.suffix", "-mac") + "-" + collectionNumber+ "."
			+ LocalProperties.get("gda.data.file.extension.rebinned", "dat");
		} else {
			this.fileName = getDataDir() + File.separator + (fileNumber)
			+ LocalProperties.get("gda.data.file.suffix", "-mac") + "-" + collectionNumber+ "."
			+ LocalProperties.get("gda.data.file.extension.rebinned", "dat");
		}
	}

	/**
	 * Function that loads in the MAC data format
	 *
	 * @return The package which contains the data that has been loaded
	 */
	@Override
	public DataHolder loadFile() throws ScanFileHolderException {
		DataHolder result = super.loadFile();

		// overwrite dataset names
		if (result.size()>1) {
			result.getDataset(1).setName(fileName);
		}

		return result;

	}

	/**
	 * @return data directory
	 */
	public String getDataDir() {
		return PathConstructor.createFromDefaultProperty();
	}

	/**
	 * Using the GDA rule to get get the current file name, without incrementing file number
	 *
	 * @return current file name
	 */
	@SuppressWarnings("null")
	public long getCurrentFileNumber() {
		NumTracker runs = null;

		try {
			runs = new NumTracker(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME));

		} catch (IOException e) {
			logger.error("ERROR: Could not instantiate NumTracker.",e);
		}
		return runs.getCurrentFileNumber();
	}

	@Override
	protected String[] getKeysToSave() {
		return savedKeys;
	}
}
