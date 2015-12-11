/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.nxdetector;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.jython.InterfaceProvider;

public class NexusDataWriterExtension extends NexusDataWriter {
	private static final Logger logger = LoggerFactory
			.getLogger(NexusDataWriterExtension.class);

	public NexusDataWriterExtension(int scannumber) {
		super(scannumber);
	}

	@Override
	public void configureScanNumber(int _scanNumber) throws Exception {
		super.configureScanNumber(_scanNumber);
		files.clear();
	}

	@Override
	public void releaseFile() {
		if (!files.isEmpty()) {
			for (Entry<String, RegionFileMapper> entry : files.entrySet()) {
				try {
					entry.getValue().getNxFile().flush();
				} catch (Throwable et) {
					String error = "Error closing NeXus file " + entry.getValue().getURL();
					logger.error(error + et.getMessage());
					terminalPrinter.print(error);
					terminalPrinter.print(et.getMessage());
				} finally {
					files.clear();
				}
			}
		}

//		super.releaseFile();
	}
	@Override
	public void completeCollection() throws Exception {
		releaseFile();
//		super.completeCollection();
	};

	Map<String, RegionFileMapper> files = new ConcurrentHashMap<String, RegionFileMapper>();

	public Map<String, RegionFileMapper> getFiles() {
		return files;
	}

	public void setFiles(Map<String, RegionFileMapper> files) {
		this.files = files;
	}

	/**
	 *
	 * @param regionName
	 * @param nexusFileNameTemplate
	 *            the template must contain "%d_%s"
	 * @return Nexus file
	 * @throws Exception
	 */
	public NexusFile createFile(String regionName,	Sequence sequence) throws Exception {
		this.entryName = "entry1";

		if (sequence == null) {
			throw new IllegalArgumentException("Sequence data model must not be null.");
		}
		String regionNexusFileName;
		String filenamePrefix = sequence.getSpectrum().getFilenamePrefix();
		if (scanNumber <= 0) {
			scanNumber=new NumTracker(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)).getCurrentFileNumber();
		}
		if (filenamePrefix!=null && !filenamePrefix.isEmpty()) {
			regionNexusFileName = String.format("%s_%05d_%s", filenamePrefix,scanNumber, regionName) + ".nxs";
		} else {
			regionNexusFileName = String.format("%05d_%s", scanNumber, regionName) + ".nxs";
		}
		if (LocalProperties.check(GDA_NEXUS_BEAMLINE_PREFIX)) {
			if (beamline==null) {
				beamline=LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
			}
			regionNexusFileName = beamline + "_" + regionNexusFileName;
		}
		if (dataDir==null) {
			dataDir=PathConstructor.createFromDefaultProperty();
		}
		if (!dataDir.endsWith(File.separator)) {
			dataDir += File.separator;
		}
		String sampleName = sequence.getSpectrum().getSampleName();
		File dir = new File(dataDir + sampleName.trim());
		if (!dir.exists()) {
			dir.mkdir();
		}

		String regionNexusFileUrl = dir.getAbsolutePath()+File.separator + regionNexusFileName;
		InterfaceProvider.getTerminalPrinter().print("Region '" + regionName + "' data will write to: "+ regionNexusFileUrl);
		NexusFile regionNexusfile = NexusFileHDF5.createNexusFile(regionNexusFileUrl);
		regionNexusfile.setDebug(LocalProperties.check(GDA_NEXUS_INSTRUMENT_API));

		RegionFileMapper regionFileMapper = new RegionFileMapper(regionName,regionNexusFileUrl,regionNexusfile );
		files.put(regionName, regionFileMapper);
		return regionNexusfile;
	}

	public NexusFile getNXFile(String regionName, int scanDataPoint) {
		if (!files.isEmpty() && files.containsKey(regionName)) {
			RegionFileMapper mapper = files.get(regionName);
//			InterfaceProvider.getTerminalPrinter().print("scan point: "+scanDataPoint+"\t-\tCollecting region '" + regionName + "' data to file : "+ mapper.getURL());
			return mapper.getNxFile();
		}
		return null;
	}

	class RegionFileMapper {
		private String regionName;
		private String URL;
		private NexusFile nxFile;
		public RegionFileMapper(String regionName, String regionNexusFileUrl,
				NexusFile regionNexusfile) {
			this.regionName=regionName;
			this.URL=regionNexusFileUrl;
			this.nxFile=regionNexusfile;
		}
		public String getRegionName() {
			return regionName;
		}
		public void setRegionName(String regionName) {
			this.regionName = regionName;
		}
		public String getURL() {
			return URL;
		}
		public void setURL(String uRL) {
			URL = uRL;
		}
		public NexusFile getNxFile() {
			return nxFile;
		}
		public void setNxFile(NexusFile nxFile) {
			this.nxFile = nxFile;
		}
	}

}
