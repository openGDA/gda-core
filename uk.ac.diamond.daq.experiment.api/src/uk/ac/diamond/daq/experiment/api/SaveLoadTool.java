package uk.ac.diamond.daq.experiment.api;

import java.io.IOException;
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
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.scanning.api.scan.IFilePathService;

public class SaveLoadTool {

	private IMarshallerService marshaller;
	private IFilePathService filePathService;

	private final String baseDirectory;

	/**
	 * The base directory you give me will live in GDA's persistance directory
	 */
	public SaveLoadTool(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public void saveObject(Object object, String fileName, String extension) {
		try {
			String json = getMarshallerService().marshal(object);

			Path path = Paths.get(getBaseDir(), fileName + "." + extension);
			path.toFile().getParentFile().mkdirs();
			Files.write(path, json.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
		} catch (Exception e) {
			throw new ExperimentException("Could not save " + fileName, e);
		}
	}

	public <T> T loadObject(Class<T> objectClass, String fileName, String extension) {
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(getBaseDir(), fileName + "." + extension));
			String json = new String(bytes, "UTF-8");
			return getMarshallerService().unmarshal(json, objectClass);
		} catch (Exception e) {
			throw new ExperimentException("Could not load " + fileName, e);
		}
	}

	public Set<String> getSavedNames(String extension) {
		return getSavedNames(extension, "");
	}

	public Set<String> getSavedNames(String extension, String subdirectory) {
		Path path = Paths.get(getBaseDir(), subdirectory);
		if (path.toFile().exists()) {
			try (Stream<Path> paths = Files.list(Paths.get(getBaseDir(), subdirectory))) {
				return paths.map(Path::getFileName).map(Path::toString)
						.filter(fileName -> FilenameUtils.isExtension(fileName, extension))
						.map(FilenameUtils::removeExtension)
						.collect(Collectors.toSet());
			} catch (IOException e) {
				throw new ExperimentException("Could not retrieve saved elements", e);
			}
		} else {
			return Collections.emptySet();
		}
	}

	public void delete(String fileName, String extension) {
		try {
			Files.deleteIfExists(Paths.get(getBaseDir(), fileName + "." + extension));
		} catch (IOException e) {
			throw new ExperimentException("Could not delete " + fileName, e);
		}
	}

	private IMarshallerService getMarshallerService() {
		if (marshaller == null) {
			marshaller = Activator.getService(IMarshallerService.class);
		}
		return marshaller;
	}

	private IFilePathService getFilePathService() {
		if (filePathService == null) {
			filePathService = Activator.getService(IFilePathService.class);
		}
		return filePathService;
	}

	private String getBaseDir() {
		return Paths.get(getFilePathService().getPersistenceDir(), baseDirectory).toString();
	}
}
