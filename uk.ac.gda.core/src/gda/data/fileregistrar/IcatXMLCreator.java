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
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.factory.Finder;

/**
 * creates an xml file required for the icat xml ingest file registry
 */
public class IcatXMLCreator extends ConfigurableBase implements ArchiveFileCreator {

	private static final Logger logger = LoggerFactory.getLogger(IcatXMLCreator.class);

	/**
	 * When making changes to the output format, update this field to the GDA version in which the change will take
	 * effect.
	 */
	private static final String VERSION = "9.7";

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<icat version=\"1.0 RC6\" xsi:noNamespaceSchemaLocation=\"icatXSD.xsd\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + "<study> <investigation> \n";
	private static final String XML_FOOTER = "</investigation>\n</study>\n</icat>\n";
	private static final String DATASET_START = " <dataset>\n";
	private static final String DATASET_END = " </dataset>\n";

	private static final String TOPLEVEL_DATASET_NAME = "topdir";
	private static final String UNKNOWN = "unknown";

	private String directory;
	private String filePrefix;
	private Metadata metadata;
	protected Writer fileWriter;

	private static class InvestigationInfo {
		private String investigationNumber;
		private String visitId;
		private String instrument;
		private String title;
		private String investigationType = "experiment";

		public InvestigationInfo(Metadata metadata) throws DeviceException {
			instrument = metadata.getMetadataValue("instrument", LocalProperties.GDA_INSTRUMENT, null);
			instrument = xmlSanitize(instrument);
			title = metadata.getMetadataValue("title");
			title = xmlSanitize(title);
			visitId = metadata.getMetadataValue("visit");
			if (visitId == null || visitId.isEmpty()) {
				visitId = "0-0";
			}
			visitId = visitId.toUpperCase();
			investigationNumber = visitId.substring(0, visitId.indexOf('-'));
		}

		public String getVisitId() {
			return visitId;
		}

		/**
		 * @param string
		 * @return string with various delimiters removed
		 */
		private String xmlSanitize(String string) {
			if (string == null || string.isEmpty()) {
				return UNKNOWN;
			}
			return string.replaceAll("[<>&/'\"\\\\]", "");
		}

		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			result.append(" <inv_number>" + investigationNumber + "</inv_number>\n");
			result.append(" <visit_id>" + visitId + "</visit_id>\n");
			result.append(" <instrument>" + instrument + "</instrument>\n");
			result.append(" <title>" + title + "</title>\n");
			result.append(" <inv_type>" + investigationType + "</inv_type>\n");
			return result.toString();
		}
	}

	private static class FileInfo {
		private String name;
		private String location;
		private String description;
		private String datafileVersion;
		private String datafileCreateTime;
		private String datafileModifyTime;
		private String datafileSize;

		private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		public FileInfo(String location) {
			this.description = UNKNOWN;
			final File handle = new File(location);
			this.location = handle.getAbsolutePath();
			this.name = handle.getName();
			Date filedate;
			if (!handle.isFile()) {
				logger.warn("file {} not (yet) found or is not a file", location);
				filedate = new Date();
			} else {
				filedate = new Date(handle.lastModified());
				this.datafileSize = String.valueOf(handle.length());
			}
			final String formattedFileDate = dateFormatter.format(filedate);
			this.datafileVersion = "1.0";
			this.datafileModifyTime = formattedFileDate;
			this.datafileCreateTime = formattedFileDate;
		}

		@Override
		public String toString() {
			final StringBuilder s = new StringBuilder();
			s.append("   <datafile>\n");
			s.append("      <name>" + name + "</name>\n");
			s.append("      <location>" + location + "</location>\n");
			s.append("      <description>" + description + "</description>\n");
			s.append("      <datafile_version>" + datafileVersion + "</datafile_version>\n");
			s.append("      <datafile_create_time>" + datafileCreateTime + "</datafile_create_time>\n");
			s.append("      <datafile_modify_time>" + datafileModifyTime + "</datafile_modify_time>\n");
			if (this.datafileSize != null) {
				s.append("      <file_size>" + this.datafileSize + "</file_size>\n");
			}
			s.append("   </datafile>\n");

			return s.toString();
		}
	}

	private static class DatasetInfo {
		private String name;
		private String datasetType;
		private String description;

		public DatasetInfo(String name) {
			this.name = name;
			this.datasetType = "EXPERIMENT_RAW";
			this.description = UNKNOWN;
		}

		@Override
		public String toString() {
			final StringBuilder s = new StringBuilder();
			s.append("   <name>" + name + "</name>\n");
			s.append("   <dataset_type>" + datasetType + "</dataset_type>\n");
			s.append("   <description>" + description + "</description>\n");

			return s.toString();
		}
	}

	/**
	 * Manages an output stream writer that writes the data to a temporary path, which is then renamed to the "real"
	 * path when the file is closed.
	 * <p>
	 * Always call the create() method - do not call the constructor directly
	 */
	private static class AtomicWriter extends OutputStreamWriter {
		private String finalFileName;
		private File file;

		/**
		 * Factory method to create the writer
		 * @param filePath The final destination of the drop file
		 * @return An AtomicWriter which initially writes to a temporary location
		 * @throws FileNotFoundException
		 */
		public static AtomicWriter create(Path filePath) throws FileNotFoundException {
			final String finalFileName = filePath.toAbsolutePath().toString();
			final File tempFile = new File(finalFileName + ".");
			return new AtomicWriter(tempFile, finalFileName);
		}

		/**
		 * Do not call this constructor directly - call create() instead
		 *
		 * @param file Temporary location of the output file
		 * @param finalFileName The final destination of the drop file
		 * @throws FileNotFoundException
		 */
		private AtomicWriter(File file, String finalFileName) throws FileNotFoundException {
			super(new FileOutputStream(file), StandardCharsets.UTF_8);
			this.file = file;
			this.finalFileName = finalFileName;
		}

		@Override
		public void close() throws IOException {
			super.close();
			final boolean success = file.renameTo(new File(finalFileName));
			if (!success) {
				throw new IOException("could not rename file to final destination");
			}
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (directory == null || directory.isEmpty()) {
			throw new FactoryException("Drop file directory not set");
		}
		if (filePrefix == null || filePrefix.isEmpty()) {
			try {
				if (metadata==null) {
					getMetadataObject();
				}
				filePrefix = metadata.getMetadataValue("instrument", LocalProperties.GDA_INSTRUMENT, null);
			} catch (DeviceException e) {
				throw new FactoryException("File prefix not set and cannot get from metadata", e);
			}
		}
		logger.info("DLSICAT:IcatXMLCreator version {} writing to {}", VERSION, directory);
		setConfigured(true);
	}

	/**
	 * creates an XML file in the configured location with the required information for an ICAT XML ingest with the data
	 * file information
	 *
	 * @param scanId
	 *            identifier of the scan
	 * @param files
	 *            list of absolute paths
	 */
	@Override
	public void registerFiles(String scanId, String[] files) {
		logger.debug("registering {} file(s) for scan {}", files.length, scanId);

		if (metadata == null) {
			try {
				getMetadataObject();
			} catch (DeviceException e) {
				logger.error("Error getting metadata", e);
				return;
			}
		}

		InvestigationInfo investigationInfo = null;
		try {
			investigationInfo = new InvestigationInfo(metadata);
		} catch (DeviceException e) {
			logger.error("Error getting Metadata, will NOT archive files! ", e);
			return;
		}

		try {
			final String fileName = String.format("%s-%d.xml", filePrefix, new Date().getTime());
			final Path outputFilePath = Paths.get(directory, fileName);
			createFile(outputFilePath);
			fileWriter.write(XML_HEADER);
			writeData(investigationInfo);
			// Each file is added as separate <dataset> entry, with dataset directory location
			// providing the dataset name
			for (String file : files) {
				final String datasetName = getDatasetNameFromPath(file, investigationInfo.getVisitId());
				logger.debug("Writing dropfile dataset for file {} : scan = {}, dataset = {}", file,
						getScanNumberFromPath(file), datasetName);
				fileWriter.write(DATASET_START);
				writeData(new DatasetInfo(datasetName));
				writeData(new FileInfo(file));
				fileWriter.write(DATASET_END);
			}
			fileWriter.write(XML_FOOTER);
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
		final String filename = FilenameUtils.getBaseName(fullFilePath);
		final int underScorePos = filename.indexOf('_');
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
		final String fileDirectory = FilenameUtils.getFullPathNoEndSeparator(fullFilePath);

		// Get path to visit folder
		final int visitEndIndex = fullFilePath.toLowerCase().indexOf(visitId.toLowerCase());
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

	private void getMetadataObject() throws DeviceException {
		final Map<String, Metadata> findables = Finder.getInstance().getFindablesOfType(Metadata.class);

		if (findables.size() == 0) {
			logger.error("cannot find a metadata object");
			throw new DeviceException("cannot register files when metadata object cannot be found");
		}
		if (findables.size() != 1) {
			logger.warn("multiple metadata objects found, using first found");
		}
		metadata = findables.values().iterator().next();
	}

	protected void closeFile() {
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

	protected void createFile(Path outputFilePath) throws IOException {
		fileWriter = AtomicWriter.create(outputFilePath);
	}

	/**
	 * the directory to create the XML in
	 *
	 * @param directory
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	protected Metadata getMetadata() {
		return metadata;
	}

	protected void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
}