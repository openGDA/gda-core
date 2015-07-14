/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.data.scan.datawriter.FindableAsciiDataWriterConfiguration;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.gda.beans.exafs.MetadataParameters;

public class Metadata {
	private static final Logger logger = LoggerFactory.getLogger(Metadata.class);
	AsciiDataWriterConfiguration dataWriterConfig;
	NXMetaDataProvider metashop;
	Finder finder;

	public Metadata(FindableAsciiDataWriterConfiguration dataWriterConfig) {
		this.dataWriterConfig = (AsciiDataWriterConfiguration) dataWriterConfig;
		finder = Finder.getInstance();
		metashop = finder.find("metashop");
	}

	public void add_to_metadata(List<MetadataParameters> metadataList) {
		ArrayList<AsciiMetadataConfig> header = dataWriterConfig.getHeader();

		for (MetadataParameters metadata : metadataList) {
			AsciiMetadataConfig asciiConfig = new AsciiMetadataConfig();
			String name = metadata.getScannableName();
			asciiConfig.setLabel(name + ": %4.1f");
			Scannable scannable = finder.find(name);
			if (scannable == null) {
				scannable = (Scannable) InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name);
			}
			Scannable[] labels = {scannable};
			asciiConfig.setLabelValues(labels);
			header.add(asciiConfig);
			if (scannable != null && metashop != null) {
				metashop.add(scannable);
			} else {
				logger.warn("Failed in add_to_metadata because scannable does not exist: " + name);
			}
		}
		dataWriterConfig.setHeader(header);
	}

	public void removeNexusMetadataList(List<String> removeList) {
		for (String s : removeList) {
			Scannable scannable = finder.find(s);
			if (scannable != null && metashop != null) {
				metashop.remove(scannable);
			}
		}
	}

	public void addNexusMetadataList(List<String> addList) {
		for (String s : addList) {
			Scannable scannable = finder.find(s);
			if (scannable != null && metashop != null) {
				metashop.add(scannable);
			}
		}
	}

	public String clearAlldynamical() {
		NXMetaDataProvider metashop = Finder.getInstance().find("metashop");

		if (metashop == null) {
			return null;
		}
		// clear scannables
		List<Scannable> allMetaScannableList = metashop.getMetaScannables();
		for (Scannable s : allMetaScannableList) {
			metashop.remove(s);
		}
		// clear non-scannables
		metashop.clear();
		Map<String, ScannableWriter> staticLocationMap = NexusDataWriter.getLocationmap();
		HashSet<String> staticMetaScannableList = new HashSet<String>();
		for (String k : staticLocationMap.keySet()) {
			staticMetaScannableList.add(k);
		}
		NexusDataWriter.setMetadatascannables(staticMetaScannableList);
		return metashop.list(false);
	}
}
