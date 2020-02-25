package uk.ac.gda.tomography.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.scanning.FilePathService;

/**
 * In future this class may be merged to a more common one. For now it does the work.
 *
 * @author Maurizio Nagni
 */
public class TomographyFileService {

	public static final String TOMO_EXTENSION = "json";
	private FilePathService filePathService;

	private static final Logger logger = LoggerFactory.getLogger(TomographyFileService.class);

	public Path saveTextDocument(String text, String fileName, String extension) throws IOException {
		Path path = createPath(fileName, extension);
		path.toFile().getParentFile().mkdirs();
		Files.write(path, text.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
		return path;
	}

	public byte[] loadFileAsBytes(String fileName, String extension) throws IOException {
		return Files.readAllBytes(createPath(fileName, extension));
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

	public void delete(String fileName, String extension) throws IOException {
		Files.deleteIfExists(createPath(fileName, extension));
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

	private Path createPath(String fileName, String extension) throws IOException {
		validateNameAndExtension(fileName, extension);
		return Paths.get(getBaseDir(), String.format("%s.%s", fileName, extension));
	}

	private void validateNameAndExtension(String fileName, String extension) throws IOException {
		if (fileName == null) {
			throw new IOException("Filename null");
		}
		if (extension == null) {
			throw new IOException("extension null");
		}
	}
}
