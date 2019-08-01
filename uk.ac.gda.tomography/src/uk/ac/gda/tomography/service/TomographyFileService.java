package uk.ac.gda.tomography.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.scanning.FilePathService;

public class TomographyFileService {

	private FilePathService filePathService;

	private final String baseDirectory;

	private static final Logger logger = LoggerFactory.getLogger(TomographyFileService.class);

	/**
	 * The base directory you give me will live in GDA's persistance directory
	 */
	public TomographyFileService(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public void saveTextDocument(String text, String fileName, String extension) throws IOException {
		Path path = Paths.get(getBaseDir(), fileName + "." + extension);
		path.toFile().getParentFile().mkdirs();
		Files.write(path, text.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
	}

	public byte[] loadFileAsBytes(String fileName, String extension) throws IOException {
		return Files.readAllBytes(loadFile(fileName, extension));
	}

	public Path loadFile(String fileName, String extension) {
		return Paths.get(getBaseDir(), fileName + "." + extension);
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
		Files.deleteIfExists(Paths.get(getBaseDir(), fileName + "." + extension));
	}

	private FilePathService getFilePathService() {
		if (Objects.isNull(filePathService)) {
			filePathService = new FilePathService();
		}
		return filePathService;
	}

	private String getBaseDir() {
		return Paths.get(getFilePathService().getPersistenceDir(), baseDirectory).toString();
	}
}
