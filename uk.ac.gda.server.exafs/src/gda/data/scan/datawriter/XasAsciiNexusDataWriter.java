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

package gda.data.scan.datawriter;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.util.io.FileUtils;

/**
 * Write to an Xas and a Nexus file simultaneously
 */
public class XasAsciiNexusDataWriter extends DataWriterBase implements DataWriter {

	private static Logger logger = LoggerFactory.getLogger(XasAsciiNexusDataWriter.class);

	protected XasAsciiDataWriter xasAscii;
	private NexusDataWriter nexus;
	
	private String xasDir;
	private String nexusFileTemplate;

	public XasAsciiNexusDataWriter() throws Exception {
		super();
		xasAscii = new XasAsciiDataWriter();
		nexus = new XasNexusDataWriter(xasAscii.getFileNumber());
		determineFilenames();
		nexus.setNexusFileNameTemplate(nexusFileTemplate);
		xasAscii.setNexusFilePath(getCurrentFileName());
	}

	private void storeFilenamesWithRegistar() {
		FileRegistrarHelper.registerFiles(new String[] {
				xasAscii.fileUrl
			});
	}

	@Override
	public String getCurrentFileName() {
		return nexus.getCurrentFileName();
	}
	
	public String getAsciiFileName() {
		return xasAscii.getCurrentFileName();
	}

	@Override
	public String getCurrentScanIdentifier() {
		return nexus.getCurrentScanIdentifier();
	}

	@Override
	public void setHeader(String header) {
		// ignore as they both do their own thing
	}

	@Override
	public void addData(IScanDataPoint newData) throws Exception {
		try {
			nexus.addData(newData);
		} catch (Exception e) {
			// ignore so we don't prevent the xas file from being written
		}
		try {
			xasAscii.addData(newData);
		} catch (Exception e) {
			// ignore so we don't prevent the drop file from being written
		}
		finally {
			try {
				super.addData(this, newData);
			} catch (Exception e) {
				logger.error("exception received from DataWriterBase.addData(...)", e);
			}
		}
	}

	@Override
	public void completeCollection() throws Exception {
		storeFilenamesWithRegistar();
		try {
			nexus.completeCollection();
		} catch (Exception e) {
			// ignore so we don't prevent the xas file from being written
			logger.error("ignored nexus error: ",e);
		}
		xasAscii.completeCollection();
	}

	@Override
	public ArrayList<String> getHeader() {
		return nexus.getHeader();
	}

	private void determineFilenames() {
		String nameFrag = LocalProperties.get("gda.instrument"); 

		//get data directory
		if (XasAsciiDataWriter.group != null && XasAsciiDataWriter.group.getExperimentFolderName() != null) {
			xasDir = PathConstructor.createFromDefaultProperty()+XasAsciiDataWriter.group.getExperimentFolderName();
		} else {
			xasDir = PathConstructor.createFromDefaultProperty();
		}

		// create dir
		final File file = new File(xasDir);
		if (!file.canWrite()) {
			final File newFile = FileUtils.createNewUniqueDir(file.getParentFile(), file.getName());
			logger.info("'" + xasDir + "' is not writable. Using '" + newFile.getName() + "' instead.");
			xasDir = newFile.getName() + "/";
		}
		
		// work out sub-folders and filenames
		try {
			if (XasAsciiDataWriter.group != null && XasAsciiDataWriter.group.getScanNumber() >= 0) {
				final OutputParameters params = (OutputParameters)XasAsciiDataWriter.group.getOutput();
				xasDir += "/"+params.getAsciiDirectory();
				nexusFileTemplate = XasAsciiDataWriter.group.getExperimentFolderName() + "/" + params.getNexusDirectory() + "/%d";
				if (nameFrag != null && !nameFrag.equals("i20")){
					final ISampleParameters sampleParams = XasAsciiDataWriter.group.getSample();
					String sampleName = sampleParams.getName().trim().replaceAll(" ", "_");
					nexusFileTemplate += "_"+ sampleName;
				}
				nexusFileTemplate += "_" + XasAsciiDataWriter.group.getScanNumber() + ".nxs";
			} else {
				xasDir += "/ascii" ;
				nexusFileTemplate = "nexus/%d";
				if (nameFrag != null && !nameFrag.equals("i20") && !nameFrag.equals("i20-1")){
					nexusFileTemplate += "_" + nameFrag;
				}
				nexusFileTemplate += ".nxs";
			}			
		} catch (RuntimeException ne) {
			if (XasAsciiDataWriter.group != null && XasAsciiDataWriter.group.isIncompleteDataAllowed()) {
				return;
			}
			throw ne;
		}
	}
	
	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		nexus.addDataWriterExtender(dataWriterExtender);
		xasAscii.addDataWriterExtender(dataWriterExtender);
	}
	

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		nexus.removeDataWriterExtender(dataWriterExtender);	
		xasAscii.removeDataWriterExtender(dataWriterExtender);
	}
}
