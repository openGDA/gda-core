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

package uk.ac.diamond.daq.experiment.api.structure;

import java.net.URL;
import java.util.List;

/**
 * An experiment represents a collection of acquisitions performed during a
 * defined period of time. When called, {@link #startExperiment(String)} defines
 * or creates a location where all the following acquisitions will be stored.
 */
public interface ExperimentController {


	/**
	 * Starts a new named experiment
	 *
	 * @param experimentName A user-friendly identifier for the experiment
	 *
	 * @return the experiment file URL;
	 * 		   created at the end of the experiment ({@link #stopExperiment()}
	 *
	 * @throws ExperimentControllerException if methods fails to create the
	 *                                       experiment location
	 */
	URL startExperiment(String experimentName) throws ExperimentControllerException;


	/**
	 * Returns the experiment name, or {@code null} if no experiment is running
	 */
	String getExperimentName();


	/**
	 * Closes the active experiment. Closes also any open multipart acquisition.
	 *
	 * @throws ExperimentControllerException if methods fails or
	 *                                       {@link #isExperimentInProgress()} returns
	 *                                       {@code false}
	 */
	void stopExperiment() throws ExperimentControllerException;



	/**
	 * Returns the state of the experiment
	 *
	 * @return {@code true} if the experiment is in progress, otherwise {@code false}
	 */
	boolean isExperimentInProgress();



	/**
	 * Creates a new location within the experiment structure.
	 *
	 * @param acquisitionName A user-friendly identifier for the acquisition
	 *
	 * @return The URL for the acquisition file
	 *
	 * @throws ExperimentControllerException if methods fails to create the
	 *                                       acquisition location
	 */
	URL prepareAcquisition(String acquisitionName) throws ExperimentControllerException;


	/**
	 * Prepares the controller for an acquisition composed of multiple parts.
	 * Each part should then be given a URL by calling {@link #prepareAcquisition(String)},
	 * and the overall acquisition should be ended with {@link #stopMultipartAcquisition()}
	 *
	 * @param acquisitionName A user-friendly identifier for the acquisition
	 *
	 * @return the URL of the acquisition file,
	 * 		   created when the multipart acquisition is stopped
	 *
	 * @throws ExperimentControllerException
	 */
	URL startMultipartAcquisition(String acquisitionName) throws ExperimentControllerException;


	/**
	 * Closes the current multipart acquisition is complete.
	 *
	 * @throws ExperimentControllerException if no open multipart acquisition exists
	 */
	void stopMultipartAcquisition() throws ExperimentControllerException;


	/**
	 * A collection of path to the closed experiment index files
	 *
	 * @return a collection of {@code URL}s, eventually {@code empty}
	 *
	 * @throws ExperimentControllerException if there are error reading the experiments storage location
	 */
	List<URL> closedExperiments() throws ExperimentControllerException;
}
