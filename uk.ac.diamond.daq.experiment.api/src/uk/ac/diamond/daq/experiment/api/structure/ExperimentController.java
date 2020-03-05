package uk.ac.diamond.daq.experiment.api.structure;

import java.net.URL;

/**
 * An experiment represents a collection of acquisitions performed during a defined period of time.
 * When called, {@link #startExperiment(String)} defines or creates a location where all the following acquisitions will be stored.
 */
public interface ExperimentController {

	/**
	 * Starts a new named experiment
	 *
	 * @param experimentName
	 *            A user-friendly identifier for the experiment.
	 *            If {@code null} {@link #getDefaultExperimentName()} is used
	 *
	 * @return the experiment root URL
	 * @throws ExperimentControllerException
	 *             if methods fails to create the experiment location
	 */
	URL startExperiment(String experimentName) throws ExperimentControllerException;

	/**
	 * Creates a new location within {@link #getExperimentLocation()}
	 *
	 * @param acquisitionName
	 *            A user-friendly identifier for the acquisition
	 *
	 * @return a valid URL if {@link #isStarted()} is {@code true}, {@code null} otherwise
	 * @throws ExperimentControllerException
	 *             if methods fails to create the acquisition location
	 */
	URL createAcquisitionLocation(String acquisitionName) throws ExperimentControllerException;

	/**
	 * Closes the active experiment.
	 *
	 * @throws ExperimentControllerException
	 *             if methods fails or {@link #isStarted()} returns {@code false}
	 */
	void stopExperiment() throws ExperimentControllerException;

	/**
	 * Returns the state of the experiment
	 *
	 * @return {@code true} if the experiment is started, otherwise {@code false}
	 */
	boolean isStarted();

	/**
	 * Returns the controller root location. All calls to {@link #startExperiment(String)} create sub-locations of this URL.
	 *
	 * @return a valid URL for the root location
	 * @throws ExperimentControllerException if the root location doesn't exist and it cannot be created
	 */
	URL getControllerRootLocation() throws ExperimentControllerException;

	/**
	 * Returns the {@link URL} created by the last {@link #startExperiment(String)}
	 *
	 * @return a valid URL if {@link #isStarted()} is {@code true}, {@code null} otherwise
	 */
	URL getExperimentLocation();

	/**
	 * Returns the {@link URL} created by the last {@link #createAcquisitionLocation(String)}
	 *
	 * @return a valid URL if {@link #isStarted()} is {@code true}, {@code null} otherwise
	 */
	URL getLastAcquisitionLocation();

	/**
	 * Returns the default experiment name
	 */
	String getDefaultExperimentName();

	/**
	 * Returns the default acquisition name
	 */
	String getDefaultAcquisitionName();

}
