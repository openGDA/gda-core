/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.fileregistrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.Metadata;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;

/**
 * creates an xml file required for the icat xml ingest file registry
 */
public class IcatXMLCreator implements ArchiveFileCreator, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(IcatXMLCreator.class);

	/**
	 * When making changes to the output format, update this field to the GDA version in which the change will take
	 * effect.
	 */
	private static final String VERSION = "9.7";

	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<icat version=\"1.0 RC6\" xsi:noNamespaceSchemaLocation=\"icatXSD.xsd\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + "<study> <investigation> \n";
	private static final String xmlFooter = "</investigation>\n</study>\n</icat>\n";
	private static final String datasetStart = " <dataset>\n";
	private static final String datasetEnd = " </dataset>\n";

	private static final String TOPLEVEL_DATASET_NAME = "topdir";

	private String directory = "/tmp/bar-";
	private Metadata metadata;
	private Writer fileWriter;

	private boolean useDirForDatasetName = true;

	private class IvestigationInfo {
		private String inv_number, visit_id, instrument, title;
		private String inv_type = "experiment";

		public IvestigationInfo() throws DeviceException {
			instrument = metadata.getMetadataValue("instrument", LocalProperties.GDA_INSTRUMENT, null);
			instrument = xmlSanitize(instrument);
			title = metadata.getMetadataValue("title");
			title = xmlSanitize(title);
			visit_id = metadata.getMetadataValue("visit");
			if (visit_id == null || visit_id.isEmpty()) {
				visit_id = "0-0";
			}
			visit_id = visit_id.toUpperCase();
			inv_number = visit_id.substring(0, visit_id.indexOf('-'));
		}

		public String getVisitId() {
			return visit_id;
		}

		/**
		 * @param string
		 * @return string with various delimiters removed
		 */
		private String xmlSanitize(String string) {
			if (string == null || string.isEmpty())
				return "unknown";
			string = string.replace(">", "");
			string = string.replace("<", "");
			string = string.replace("&", "");
			string = string.replace("/", "");
			string = string.replace("'", "");
			string = string.replace("\"", "");
			string = string.replace("\\", "");
			return string;
		}

		@Override
		public String toString() {
			String s = "";
			s += " <inv_number>" + inv_number + "</inv_number>\n";
			s += " <visit_id>" + visit_id + "</visit_id>\n";
			s += " <instrument>" + instrument + "</instrument>\n";
			s += " <title>" + title + "</title>\n";
			s += " <inv_type>" + inv_type + "</inv_type>\n";

			return s;
		}
	}

	private class FileInfo {
		private String name, location, description, datafile_version, datafile_create_time, datafile_modify_time, datafile_size;

		public FileInfo(String location) {
			this.description = "unknown";
			File handle = new File(location);
			this.location = handle.getAbsolutePath();
			this.name = handle.getName();
			Date filedate;
			if (!handle.isFile()) {
				logger.warn("file " + location + " not (yet) found or is no file");
				filedate = new Date();
			} else {
				filedate = new Date(handle.lastModified());
				this.datafile_size = String.valueOf(handle.length());
			}
			String formattedFileDate = dateFormatter.format(filedate);
			this.datafile_version = "1.0";
			this.datafile_modify_time = formattedFileDate;
			this.datafile_create_time = formattedFileDate;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("   <datafile>\n");
			s.append("      <name>" + name + "</name>\n");
			s.append("      <location>" + location + "</location>\n");
			s.append("      <description>" + description + "</description>\n");
			s.append("      <datafile_version>" + datafile_version + "</datafile_version>\n");
			s.append("      <datafile_create_time>" + datafile_create_time + "</datafile_create_time>\n");
			s.append("      <datafile_modify_time>" + datafile_modify_time + "</datafile_modify_time>\n");
            if (this.datafile_size != null) s.append("      <file_size>" + this.datafile_size + "</file_size>\n");
			s.append("   </datafile>\n");

			return s.toString();
		}
	}

	private class DatasetInfo {
		private String name, dataset_type, description;

		public DatasetInfo(String name) {
			this.name = name;
			this.dataset_type = "EXPERIMENT_RAW";
			this.description = "unknown";
		}

		@Override
		public String toString() {
			String s = "";
			s += "   <name>" + name + "</name>\n";
			s += "   <dataset_type>" + dataset_type + "</dataset_type>\n";
			s += "   <description>" + description + "</description>\n";

			return s;
		}
	}

	private class AtomicWriter extends OutputStreamWriter {
		private String finalFileName;
		private File file;

		public AtomicWriter(String fileName) throws UnsupportedEncodingException, FileNotFoundException {
			this(new File(fileName+"."));
			this.finalFileName = fileName;
		}

		private AtomicWriter(File file) throws UnsupportedEncodingException, FileNotFoundException {
			super(new FileOutputStream(file), "UTF-8");
			this.file = file;
		}

		@Override
		public void close() throws IOException {
			super.close();
			boolean success = file.renameTo(new File(finalFileName));
			if (!success) throw new IOException("could not rename file to final destination");
		}
	}

	@Override
	public void configure() throws FactoryException {
		logger.info("DLSICAT:IcatXMLCreator version {} writing to {}", VERSION, directory);
	}

	/**
	 * creates an XML file in the configured location with the required information for an ICAT XML ingest with the data
	 * file information
	 *
	 * @param datasetId
	 *            name that will allow to group related files
	 * @param files
	 *            list of absolute paths
	 */
	@Override
	public void registerFiles(String datasetId, String[] files) {
		logger.debug("registering {} file(s) for dataset {}", files.length, datasetId);

		if (metadata == null) {
			getMetadataObject();
		}

		IvestigationInfo investigationInfo = null;
		try {
			investigationInfo = new IvestigationInfo();
		} catch (DeviceException e) {
			logger.error("Error getting Metadata, will NOT archive files! ", e);
			return;
		}

		try {
			createFile();
			writeData(investigationInfo);
			if (useDirForDatasetName) {
				// Each file is added as separate <dataset> entry, with dataset directory location providing the dataset name
				for (String file : files) {
					String datasetName = getDatasetNameFromPath(file, investigationInfo.getVisitId());
					logger.debug("Writing dropfile dataset for file {} : scan = {}, dataset = {}", file, getScanNumberFromPath(file), datasetName);
					writeData(datasetStart);
					writeData(new DatasetInfo(datasetName));
					writeData(new FileInfo(file));
					writeData(datasetEnd);
				}
			} else {
				writeData(datasetStart);
				writeData(new DatasetInfo(datasetId));
				for (String file : files) {
					logger.debug("Writing info for file " + file);
					writeData(new FileInfo(file));
				}
				writeData(datasetEnd);
			}
		} catch (Exception e) {
			logger.error("Cannot write XML drop file ", e);
		} finally {
			closeFile();
		}
	}

	/**
	 * Extract scan number from full file path. Format of scan file is assumed to be : <scan_number>_*.*
	 * @param fullFilePath
	 * @return Scan number
	 */
	private String getScanNumberFromPath(String fullFilePath) {
		String filename = FilenameUtils.getBaseName(fullFilePath);
		int underScorePos = filename.indexOf("_");
		if (underScorePos > 0) {
			return filename.substring(0, underScorePos);
		} else {
			return filename;
		}
	}

	/**
	 * @param fullFilePath
	 * @param visitId
	 * @return Icat dataset name (sub directory in visit/commissioning directory where file is located)
	 */
	private String getDatasetNameFromPath(String fullFilePath, String visitId) {
		// Get path to folder containing data
		String fileDirectory = FilenameUtils.getFullPathNoEndSeparator(fullFilePath);

		// Get path to visit folder
		int visitEndIndex = fullFilePath.toLowerCase().indexOf(visitId.toLowerCase());
		if (visitEndIndex == -1 ) {
			logger.warn("Path for file {} does not contain visit id {}. Using directory {} instead", fullFilePath, visitId, fileDirectory);
			return fileDirectory;
		}
		String dataDirectory = fullFilePath.substring(0, visitEndIndex + visitId.length() + 1);
		dataDirectory = StringUtils.stripEnd(dataDirectory, File.separator).trim();

		// Subfolder within visit directory
		String relativePath = fileDirectory.replace(dataDirectory, "");
		relativePath = StringUtils.strip(relativePath, File.separator).trim();
		if (relativePath.isEmpty()) {
			return TOPLEVEL_DATASET_NAME;
		} else {
			return relativePath;
		}
	}

	/**
	 * creates an XML file in the configured location with the required information for an ICAT XML ingest with the data file information
	 *
	 * @param files
	 *            list of absolute paths
	 */
	public void registerFiles(String[] files) {

		String datasetId = "scan-" + getScanNumberFromPath(files[0]);

		if (metadata == null) {
			getMetadataObject();
		}

		IvestigationInfo investigationInfo = null;
		try {
			investigationInfo = new IvestigationInfo();
		} catch (DeviceException e) {
			logger.error("Error getting Metadata, will NOT archive files! ", e);
			return;
		}

		try {
			createFile();
			writeData(investigationInfo);
			writeData(datasetStart);
			writeData(new DatasetInfo(datasetId));
			for (String file : files) {
				logger.debug("Writing info for file {}", file);
				writeData(new FileInfo(file));
			}
			writeData(datasetEnd);
		} catch (Exception e) {
			logger.error("Cannot write XML drop file", e);
		} finally {
			closeFile();
		}
	}

	private void getMetadataObject() {
		Map<String, Metadata> findables = Finder.getInstance().getFindablesOfType(Metadata.class);

		if (findables.size() == 0) {
			logger.error("cannot find a metadata object");
			throw new RuntimeException("cannot register files when metadata object cannot be found");
		}
		if (findables.size() != 1) {
			logger.warn("multiple metadata objects found, using first found");
		}
		metadata = findables.values().iterator().next();
	}

	private void closeFile() {
		if (fileWriter == null) {
			return;
		}
		try {
			fileWriter.write(xmlFooter);
		} catch (IOException e) {
			logger.error("Cannot write the very last bit.", e);
		}
		try {
			fileWriter.close();
		} catch (IOException e) {
			logger.error("Cannot XML close file.", e);
		}
		fileWriter = null;
	}

	private void writeData(Object info) throws IOException {
		try {
			fileWriter.write(info.toString());
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			logger.error("error getting data to write to xml", e);
		}
	}

	private void createFile() throws IOException {
		fileWriter = new AtomicWriter(directory + new Date().getTime() + ".xml");
		fileWriter.write(xmlHeader);
	}

	/**
	 * the directory to create the XML in
	 *
	 * @param directory
	 */
	@Override
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	protected Metadata getMetadata() {
		return metadata;
	}

	protected void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public boolean getUseDirForDatasetName() {
		return useDirForDatasetName;
	}

	public void setUseDirForDatasetName(boolean useDirForDatasetName) {
		this.useDirForDatasetName = useDirForDatasetName;
	}
}