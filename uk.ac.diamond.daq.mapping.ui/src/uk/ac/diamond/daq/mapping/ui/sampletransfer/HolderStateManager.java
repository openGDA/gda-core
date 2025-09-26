/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import gda.configuration.properties.LocalProperties;

public class HolderStateManager {
	private static final Logger logger = LoggerFactory.getLogger(HolderStateManager.class);

	private static final String GDA_VAR_DIR = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
	private static final String FILE_PATH = Paths.get(GDA_VAR_DIR, "sample_transfer_holder_info.json").toString();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private HolderStateManager() {

	}

	public static void saveStateToFile(List<HolderState> holderStates) throws IOException{
		File file = new File(FILE_PATH);
        objectMapper.writeValue(file, holderStates);
        logger.info("State saved to file: {}", FILE_PATH);
    }

	 public static List<HolderState> loadStateFromFile() throws IOException {
		 File file = new File(FILE_PATH);
	        return objectMapper.readValue(
	            file,
	            objectMapper.getTypeFactory().constructCollectionType(List.class, HolderState.class)
	        );
	 }
}
