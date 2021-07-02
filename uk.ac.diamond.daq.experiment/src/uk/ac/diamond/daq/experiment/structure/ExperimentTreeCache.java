/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.structure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.daq.experiment.api.Activator;

/**
 * Saves and reads an {@link ExperimentTree} to and from a file
 * resolved as {@link IFilePathService#getVisitConfigDir()}/{@link #STATE_FILE}
 */
@Component
public class ExperimentTreeCache {


	/**
	 * Name of file written in {@link IFilePathService#getVisitConfigDir()}
	 * where the experiment tree state is stored
	 */
	public static final String STATE_FILE = "experiment-state.json";

	/**
	 * Get through getter to ensure correct initialisation
	 */
	private IFilePathService filePathService;


	private final ObjectMapper mapper = new ObjectMapper();


	/**
	 * Retrieves a tree previously cached through {@link #store(ExperimentTree)}
	 * wrapped in an optional (empty if no tree is found)
	 */
	public Optional<ExperimentTree> restore() throws IOException {
		File stateCache = getCacheFile();
		if (stateCache.exists() && stateCache.length() != 0) {
			return Optional.ofNullable(mapper.readValue(stateCache, ExperimentTree.class));
		} else return Optional.empty();
	}

	/**
	 * Saves a serialised form of the given tree,
	 * which can later be retrieved through {@link #restore()}
	 */
	public void store(ExperimentTree tree) throws IOException {
		mapper.writeValue(getCacheFile(), tree);
	}

	private IFilePathService getFilePathService() {
		if (filePathService == null) {
			filePathService = Activator.getService(IFilePathService.class);
		}
		return filePathService;
	}

	private File getCacheFile() {
		return Paths.get(getFilePathService().getVisitConfigDir(), STATE_FILE).toFile();
	}

	/** For tests only */
	void setFilePathService(IFilePathService filePathService) {
		this.filePathService = filePathService;
	}

}
