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

import org.eclipse.core.resources.IFile;

import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;

public class TestCommandRunner implements ITomographyCommandRunner {

	public TestCommandRunner() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Integer> makeReduced(File filename, File outputFilename, int sliceToEvaluate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LazyDataset mapPreviewRecon(IFile filename, IFile configFilename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File fullRecon(File filename, File configfilename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LazyDataset parameterRecon(ITomographyParameter parameter, IFile filename, int slicenumber,
			double[] listOfParametersToEvaluate, IFile configFilename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITomographyParameter[] getTomographyParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
