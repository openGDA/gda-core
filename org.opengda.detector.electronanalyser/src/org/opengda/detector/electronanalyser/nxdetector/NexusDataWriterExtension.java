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

import gda.configuration.properties.LocalProperties;
import gda.data.PathChanged;
import gda.data.PathConstructor;
import gda.data.nexus.NexusFileFactory;
import gda.data.scan.datawriter.NexusDataWriter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.nexusformat.NeXusFileInterface;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusDataWriterExtension extends NexusDataWriter {
	private static final Logger logger = LoggerFactory
			.getLogger(NexusDataWriterExtension.class);

	public NexusDataWriterExtension(Long scannumber) {
		super(scannumber);
	}

	@Override
	public void configureScanNumber(Long _scanNumber) throws Exception {
		super.configureScanNumber(_scanNumber);
		files.clear();
	}

	@Override
	public void releaseFile() {
		if (!files.isEmpty()) {
			for (Entry<String, NeXusFileInterface> entry : files.entrySet()) {
				try {
					entry.getValue().flush();
					entry.getValue().finalize();
				} catch (Throwable et) {
					String error = "Error closing NeXus file" + entry.getKey();
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
	public void completeCollection() throws Exception {
		releaseFile();
		super.completeCollection();
	};

	Map<String, NeXusFileInterface> files = new HashMap<String, NeXusFileInterface>();

	public Map<String, NeXusFileInterface> getFiles() {
		return files;
	}

	public void setFiles(Map<String, NeXusFileInterface> files) {
		this.files = files;
	}

	/**
	 * 
	 * @param regionName
	 * @param nexusFileNameTemplate
	 *            the template must contain "%d_%s"
	 * @return NeXusFileInterface
	 * @throws Exception
	 */
	public NeXusFileInterface createFile(String regionName,
			Sequence sequence) throws Exception {
		if (!files.isEmpty() && files.containsKey(regionName)) {
			return files.get(regionName);
		}
		// set the entry name
		// this.entryName = "scan_" + run;
		this.entryName = "entry1";

		// construct filename
		if (sequence == null) {
			throw new IllegalArgumentException(
					"Sequence data model must not be null.");
		}
		String regionNexusFileName;
		String filenameFormat = sequence.getSpectrum().getFilenameFormat();
		String filenamePrefix = sequence.getSpectrum().getFilenamePrefix();
		if (filenamePrefix!=null) {
			regionNexusFileName = String.format(filenameFormat, filenamePrefix,scanNumber, regionName) + ".nxs";
		} else {
			regionNexusFileName = String.format("%05d_%s", scanNumber, regionName) + ".nxs";
		}
		if (LocalProperties.check(GDA_NEXUS_BEAMLINE_PREFIX)) {
			regionNexusFileName = beamline + "_" + regionNexusFileName;
		}
		if (dataDir==null) {
			dataDir=PathConstructor.createFromDefaultProperty();
		}
		if (!dataDir.endsWith(File.separator)) {
			dataDir += File.separator;
		}
		String sampleName = sequence.getSpectrum().getSampleName();
		if(sampleName !=null) {
			dataDir=dataDir+sampleName+File.separator;
		}
		File dir=new File(dataDir);
		if (!dir.exists()) {
			dir.mkdir();
		}
		String regionNexusFileUrl = dataDir + regionNexusFileName;
		NeXusFileInterface regionNexusfile = NexusFileFactory.createFile(
				regionNexusFileUrl, defaultNeXusBackend,
				LocalProperties.check(GDA_NEXUS_INSTRUMENT_API));
		files.put(regionName, regionNexusfile);
		return regionNexusfile;
	}

}
