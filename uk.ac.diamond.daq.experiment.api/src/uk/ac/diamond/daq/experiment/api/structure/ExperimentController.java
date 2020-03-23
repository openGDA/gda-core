package uk.ac.diamond.daq.experiment.api.structure;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;

/**
 * An experiment represents a collection of acquisitions performed dURLng a
 * defined period of time. When called, {@link #startExperiment(String)} defines
 * or creates a location where all the following acquisitions will be stored.
 */
public interface ExperimentController {


	/**
	 * Starts a new named experiment
	 *
	 * @param experimentName A user-friendly identifier for the experiment. If
	 *                       {@code null} {@link #getDefaultExperimentName()} is
	 *                       used
	 *
	 * @return the experiment root URL
	 * @throws ExperimentControllerException if methods fails to create the
	 *                                       experiment location
	 */
	URL startExperiment(String experimentName) throws ExperimentControllerException;


	/**
	 * Creates a new location within the experiment structure.
	 *
	 * @param acquisitionName A user-friendly identifier for the acquisition
	 *
	 * @return a valid URL if {@link #isStarted()} is {@code true}, {@code null}
	 *         otherwise
	 * @throws ExperimentControllerException if methods fails to create the
	 *                                       acquisition location
	 */
	URL createAcquisitionLocation(String acquisitionName) throws ExperimentControllerException;


	/**
	 * Closes the active experiment.
	 *
	 * @throws ExperimentControllerException if methods fails or
	 *                                       {@link #isStarted()} returns
	 *                                       {@code false}
	 */
	void stopExperiment() throws ExperimentControllerException;


	/**
	 * Returns the state of the experiment
	 *
	 * @return {@code true} if the experiment is started, otherwise {@code false}
	 */
	boolean isStarted();


	/**
	 * Generates a URL for a future acquisition without creating the file itself.
	 * The <code>acquisitionName</code> is used both for the folder name and the main acquisition file.
	 *
	 * The default implementation creates a URL for NeXus file.
	 *
	 * @param acquisitionName the acquisition name
	 * @return the path of the acquisition file
	 * @throws ExperimentControllerException if the experiment has not started or
	 *                                       problem occur creating the folder or
	 *                                       the file URL
	 */
	default URL createAcquisitionUrl(String acquisitionName) throws ExperimentControllerException {
		if (!isStarted()) {
			throw new ExperimentControllerException("You have to start first the experiment before ask URLs for it");
		}

		try {
			URL acquisitionFolder = createAcquisitionLocation(acquisitionName);
			String fileName = FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(acquisitionFolder.getPath()));
			return new URL(acquisitionFolder, fileName + ".nxs");
		} catch (MalformedURLException e) {
			throw new ExperimentControllerException("Could not create URL for acquisition output", e);
		}
	}

}
