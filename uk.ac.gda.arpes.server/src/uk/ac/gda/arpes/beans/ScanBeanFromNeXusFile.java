/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.beans;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.StringDataset;

import uk.ac.diamond.daq.pes.api.AcquisitionMode;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;

public class ScanBeanFromNeXusFile {

	public static ARPESScanBean read(String filename) throws Exception {

		ARPESScanBean shinynewbean = new ARPESScanBean();

		HDF5Loader hl = new HDF5Loader(filename);
		try {
			Tree tree = hl.loadTree();
			DataNode nodeLink;

			nodeLink = (DataNode) tree.findNodeLink("/entry1/instrument/analyser/acquisition_mode").getDestination();
			setAcquisitionMode(shinynewbean, nodeLink.getDataset());

			nodeLink = (DataNode) tree.findNodeLink("/entry1/instrument/analyser/lens_mode").getDestination();
			setLensMode(shinynewbean, nodeLink.getDataset());

			nodeLink = (DataNode) tree.findNodeLink("/entry1/instrument/analyser/pass_energy").getDestination();
			setPassEnergy(shinynewbean, nodeLink.getDataset());

			nodeLink = (DataNode) tree.findNodeLink("/entry1/instrument/analyser/energies").getDestination();
			setEnergies(shinynewbean, nodeLink.getDataset());

			nodeLink = (DataNode) tree.findNodeLink("/entry1/instrument/analyser/time_for_frames").getDestination();
			setTimePerStep(shinynewbean, nodeLink.getDataset());

			nodeLink = (DataNode) tree.findNodeLink("/entry1/instrument/analyser/number_of_iterations").getDestination();
			setIterations(shinynewbean, nodeLink.getDataset());
						
		} finally {

		}
		return shinynewbean;
	}

	private static void setAcquisitionMode(ARPESScanBean bean, ILazyDataset acqmode) {
		String mode = ((StringDataset) acqmode).get(0).toLowerCase();
		
		if (mode.contains("fixed")) {
			bean.setAcquisitionMode(AcquisitionMode.FIXED);
		} else if (mode.contains("swe")) {
			// try to catch "sweep" or "swept"
			bean.setAcquisitionMode(AcquisitionMode.SWEPT);
		} else if (mode.contains("dither")) {
			bean.setAcquisitionMode(AcquisitionMode.DITHER);
		}
	}
	
	private static void setLensMode(ARPESScanBean bean, ILazyDataset lensmode) {
		String mode = ((StringDataset) lensmode).get(0);
		bean.setLensMode(mode);
	}
	
	private static void setPassEnergy(ARPESScanBean bean, ILazyDataset pe) {
		short passe = (short) ((IDataset)pe).getInt(0);
		bean.setPassEnergy(passe);
	}
	
	private static void setEnergies(ARPESScanBean bean, ILazyDataset lazyenergies) throws DatasetException {
		IDataset energies = lazyenergies.getSlice((Slice) null);
		double start = energies.getDouble(0, 0);
		bean.setStartEnergy(start);
		int size = energies.getSize();
		double end = energies.getDouble(0, size-1);
		bean.setEndEnergy(end);
		if (bean.getAcquisitionMode() == AcquisitionMode.SWEPT) {
			double step = energies.getDouble(0, 1) - start;
			bean.setStepEnergy(step*1000);
		}
	}
	
	private static void setTimePerStep(ARPESScanBean bean, ILazyDataset tps) throws DatasetException {
		IDataset energies = tps.getSlice((Slice) null);
		double start = energies.getDouble(0);
		bean.setTimePerStep(start);
	}
	
	private static void setIterations(ARPESScanBean bean, ILazyDataset iter) {
		int iterations = ((IDataset)iter).getInt(0);
		bean.setIterations(iterations);
	}
}