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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.NumPyFileSaver;

public class TestCommandRunner implements ITomographyCommandRunner {

	public TestCommandRunner() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Integer> makeReduced(File filename, File outputFilename, int sliceToEvaluate) {
		List<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < 100; i+=10) {
			list.add(i);
		}
		return list;
	}

	@Override
	public IDataset mapPreviewRecon(File filename, File configFilename) {
		return Random.rand(new int[] {10,120,140});
	}

	@Override
	public File fullRecon(File filename, File configfilename) {
		DataHolder dh = new DataHolder();
		DoubleDataset data = Random.rand(new int[] {100,120,140});
		dh.addDataset("reconstruction", data);
		File tempFile = null;
		try {
			tempFile = File.createTempFile("Reconstruction-", ".npy");
			new NumPyFileSaver(tempFile.toString()).saveFile(dh);
		} catch (Exception e) {
			// XXX This should probably throw?
			System.out.println(e);
		}
		return tempFile;
	}

	@Override
	public IDataset parameterRecon(ITomographyParameter parameter, File filename, int slicenumber,
			double[] listOfParametersToEvaluate, File configFilename) {
		return Random.rand(new int[] {listOfParametersToEvaluate.length,120,140});
	}

	@Override
	public ITomographyParameter[] getTomographyParameters() {
		DoubleTomographyParameter centreParam = new DoubleTomographyParameter("Center Of Rotation");
		centreParam.setValue(60);
		centreParam.setMax(120);
		centreParam.setMin(0);
		centreParam.setCoarse_step(5);
		centreParam.setFine_step(1);
		centreParam.setVery_fine_step(0.1);

		StringListTomographyParameter filterParam = new StringListTomographyParameter("Filter");
		ArrayList<String> list = new ArrayList<String>();
		list.add("Gaussian Filter");
		list.add("Square Filter");
		list.add("Happy Filter");
		filterParam.setValueLocation(0);

		return new ITomographyParameter[] {centreParam, filterParam};
	}

}
