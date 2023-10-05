/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.services.numtracker;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import gda.data.NumTracker;

@RestController
public class NumTrackerService {
	private final NumTracker numTracker;

	public NumTrackerService() throws IOException {
		super();
		numTracker = new NumTracker();
	}

	/**
	 * Creates a new data collection identifier
	 * @return The new identifier
	 */
	@PostMapping("/numtracker")
	public DataCollectionIdentifier createNewCollection() {
		final var scanNumber = nextScanNumber();
		return new DataCollectionIdentifier(scanNumber);
	}

	/**
	 * Provides the current data collection details
	 * @return A unique identifier for the current data collection
	 */
	@GetMapping("/numtracker")
	public DataCollectionIdentifier getCurrentCollection() {
		return new DataCollectionIdentifier(getCurrentScanNumber());
	}

	private synchronized int nextScanNumber() {
		numTracker.incrementNumber();
		return numTracker.getCurrentFileNumber();
	}

	private synchronized int getCurrentScanNumber() {
		return numTracker.getCurrentFileNumber();
	}

}
