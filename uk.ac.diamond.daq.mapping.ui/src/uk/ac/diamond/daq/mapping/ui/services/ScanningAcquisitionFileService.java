package uk.ac.diamond.daq.mapping.ui.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.directory.InvalidAttributesException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.scanning.FilePathService;
import uk.ac.gda.client.exception.AcquisitionControllerException;

/**
 * In future this class may be merged to a more common one. For now it does the work.
 *
 * @author Maurizio Nagni
 */
public class ScanningAcquisitionFileService {

	public static final String TOMO_EXTENSION = "json";
	private FilePathService filePathService;

	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionFileService.class);

	/**
	 * Saves a configuration file
	 * @param text the file content
	 * @param fileName the file name
	 * @param extension the file extent
	 * @param override if {@code true} the file is overwritten, if {@code false} and the file exists throws a {@link FileAlreadyExistsException}
	 * @return the path where the file has been saved
	 * @throws IOException if an I/O exception occurs
	 * @throws InvalidAttributesException if any of the parameters is {@code null}
	 */
	public Path saveTextDocument(String text, String fileName, String extension, boolean override) throws IOException, InvalidAttributesException {
		Path path = buildPathToConfigDir(fileName, extension);
		if (!override) {
			fileExists(path);
		}
		path.toFile().getParentFile().mkdirs();
		Files.write(path, text.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
		return path;
	}

	/**
	 * @param fileName
	 * @param extension
	 * @return the file content
	 * @throws IOException if an I/O exception occours
	 * @throws InvalidAttributesException if any of the parameters is {@code null}
	 */
	public byte[] loadFileAsBytes(String fileName, String extension) throws IOException, InvalidAttributesException {
		return Files.readAllBytes(buildPathToConfigDir(fileName, extension));
	}

	public byte[] loadFileAsBytes(URL url) throws IOException {
		try {
			return Files.readAllBytes(Paths.get(url.toURI()));
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public Set<String> getSavedNames(String extension) throws IOException {
		return getSavedNames(extension, "");
	}

	public Set<String> getSavedNames(String extension, String subdirectory) throws IOException {
		Path path = Paths.get(getBaseDir(), subdirectory);
		if (path.toFile().exists()) {
			try (Stream<Path> paths = Files.list(Paths.get(getBaseDir(), subdirectory))) {
				return paths.map(Path::getFileName).map(Path::toString).filter(fileName -> FilenameUtils.isExtension(fileName, extension))
						.map(FilenameUtils::removeExtension).collect(Collectors.toSet());
			}
		}
		return Collections.emptySet();
	}

	public Stream<Path> getPaths(String subdirectory) {
		try {
			return Files.list(Paths.get(getBaseDir(), subdirectory));
		} catch (IOException e) {
			logger.error("Cannot load subdiractory paths", e);
		}
		return Stream.empty();
	}

	/**
	 * @param fileName
	 * @param extension
	 * @throws IOException if an I/O exception occours
	 * @throws InvalidAttributesException if any of the parameters is {@code null}
	 */
	public void delete(String fileName, String extension) throws IOException, InvalidAttributesException {
		Files.deleteIfExists(buildPathToConfigDir(fileName, extension));
	}

	private FilePathService getFilePathService() {
		if (filePathService == null) {
			filePathService = new FilePathService();
		}
		return filePathService;
	}

	private String getBaseDir() {
		return getFilePathService().getVisitConfigDir();
	}

	/**
	 * @param fileName
	 * @param extension
	 * @return the configuration file appended to the full path to the config dir
	 * @throws InvalidAttributesException if any of the parameters is {@code null}
	 */
	private Path buildPathToConfigDir(String fileName, String extension) throws InvalidAttributesException {
		validateNameAndExtension(fileName, extension);
		return Paths.get(getBaseDir(), String.format("%s.%s", fileName, extension));
	}

	/**
	 * @param fileName
	 * @param extension
	 * @throws AcquisitionControllerException if any of the parameters is {@code null}
	 */
	private void validateNameAndExtension(String fileName, String extension) throws InvalidAttributesException {
		if (fileName == null) {
			throw new InvalidAttributesException("Filename null");
		}
		if (extension == null) {
			throw new InvalidAttributesException("Extension null");
		}
	}

	private void fileExists(Path path) throws FileAlreadyExistsException {
		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			throw new FileAlreadyExistsException("File already exists");
		}
	}
}
