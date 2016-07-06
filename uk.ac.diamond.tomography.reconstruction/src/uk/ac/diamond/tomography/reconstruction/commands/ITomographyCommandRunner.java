/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.commands;

import java.io.File;
import java.util.List;

import org.eclipse.january.dataset.IDataset;

public interface ITomographyCommandRunner {

	/**
	 * Takes a filename, and an output filename, it puts the reduced data in the location of the outputfilename, and
	 * returns the slices which will be evaluated in the list. Clients may set a specific sliceToEvaluate.(this could be
	 * different to the input, as it may be a sequence of slices)
	 *
	 * @param filename
	 *            input file
	 * @param outputFilename
	 *            output file
	 * @param sliceToEvaluate
	 *            int of the slice to evaluate
	 * @return a list of integers
	 */
	List<Integer> makeReduced(File filename, File outputFilename, int sliceToEvaluate);

	/**
	 * Given a filename and a configuration file, return a lazy dataset containing the images for all the slices
	 * described in the list from makeReduced
	 *
	 * @param filename
	 *            input file
	 * @param configFilename
	 *            configuration file
	 * @return a lazydataset containing images for all slices
	 */
	IDataset mapPreviewRecon(File filename, File configFilename);

	/**
	 * Given a filename and a configuration file, return the filename of the resulting full reconstruction
	 *
	 * @param filename
	 *            input file
	 * @param configfilename
	 *            configuration file
	 * @return reconstruction file
	 */
	File fullRecon(File filename, File configfilename);

	/**
	 * Run a reconstruction for the specific parameter
	 *
	 * @param parameter
	 *            tomography parameter
	 * @param filename
	 *            input file
	 * @param slicenumber
	 * @param listOfParametersToEvaluate
	 *            Must not be null
	 * @param configFilename
	 *            configuration file
	 * @return stack of images
	 */
	IDataset parameterRecon(ITomographyParameter parameter, File filename, int slicenumber,
			double[] listOfParametersToEvaluate, File configFilename);

	/**
	 * Returns the modifiable parameters for this command runner
	 *
	 * @return list of modifiable parameters
	 */
	ITomographyParameter[] getTomographyParameters();
}
