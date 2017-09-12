/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Helper methods for persisting SPECS sequences to JSON files.
 * <p>
 * You can not not make instances of this class.
 *
 * @author James Mudd
 */
public final class SpecsPhoibosSequenceHelper {

	private static final Logger logger = LoggerFactory.getLogger(SpecsPhoibosSequenceHelper.class);

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private SpecsPhoibosSequenceHelper() {
		// Don't allow instances to be created
	}

	/**
	 * Saves a SPECS sequence to JSON file.
	 *
	 * @param sequence
	 *            The sequence to save to JSON
	 * @param path
	 *            The file to save into.
	 * @throws RuntimeException
	 *             If an {@link IOException} occurs during the save
	 */
	public static void saveSequence(final SpecsPhoibosSequence sequence, final String path) {
		logger.debug("About to save sequence to file: {}", path);

		try (Writer writer = new FileWriter(path)) {
			// Save out the JSON file
			GSON.toJson(sequence, writer);
		} catch (IOException e) {
			final String msg = "Failed to save sequence file to: " + path;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Loads a SPECS sequence from XML file.
	 *
	 * @param path
	 *            The path to the sequence file to load
	 * @return The loaded sequence
	 * @throws RuntimeException
	 *             If an {@link IOException} occurs during the loading
	 * @throws NullPointerException
	 *             If path is null
	 * @throws IllegalArgumentException
	 *             If path is empty
	 */
	public static SpecsPhoibosSequence loadSequence(final String path) {
		// Parameter validation
		Objects.requireNonNull(path);
		if (path.isEmpty()) {
			throw new IllegalArgumentException("path was empty");
		}

		logger.debug("About to load sequence from file: {}", path);

		try (Reader reader = new FileReader(path)) {
			final SpecsPhoibosSequence sequence = GSON.fromJson(reader, SpecsPhoibosSequence.class);
			sequence.updateRegionListeners(); // This recreates the required PCS on the sequence
			return sequence;
		} catch (IOException e) {
			final String msg = "Failed to load sequence file from: " + path;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

}
