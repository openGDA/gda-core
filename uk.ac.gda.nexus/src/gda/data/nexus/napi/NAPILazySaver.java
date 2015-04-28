/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.data.nexus.napi;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ILazySaver;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.nexusformat.NexusFile;

public class NAPILazySaver extends NAPILazyLoader implements ILazySaver, Serializable {

	/**
	 * @param tree
	 * @param path group where dataset will reside
	 * @param name
	 * @param shape
	 * @param dtype
	 */
	public NAPILazySaver(Tree tree, String path, String name, int[] shape, int dtype) {
		super(tree, path, name, shape, dtype);
	}

	@Override
	public void initialize() throws Exception {
		NexusFile file = null;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_READ);
			file.openpath(path);
			file.makedata(name, dtype, trueShape.length, trueShape);
		} catch (NexusException e) {
			logger.error("Problem with NeXus library: {}", e);
		} finally {
			if (file != null)
				file.close();
		}
	}

	@Override
	public boolean isFileWriteable() {
		if (!checkHost())
			return false;

		return new File(filename != null ? filename : source.getPath()).canWrite();
	}

	@Override
	public void setSlice(IMonitor mon, IDataset data, SliceND slice) throws Exception {
		int[] lstart = slice.getStart();
		int[] lstep  = slice.getStep();
		int[] newShape = slice.getShape();
		int rank = newShape.length;

		boolean useSteps = false;
		int[] size;

		for (int i = 0; i < rank; i++) {
			if (lstep[i] != 1) {
				useSteps = true;
				break;
			}
		}

		if (useSteps) { // have to get superset of slice as NeXus API's getslab doesn't allow steps
			size = new int[rank];
			for (int i = 0; i < rank; i++) {
				int last = lstart[i] + (newShape[i]-1)*lstep[i]; // last index
				if (lstep[i] < 0) {
					size[i] = lstart[i] - last + 1;
					lstart[i] = last;
				} else {
					size[i] = last - lstart[i] + 1;
				}
			}
		} else {
			size = newShape;
		}

		
		NexusFile file = null;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_RDWR);
			file.openpath(path);
			file.opendata(name);
			if (!Arrays.equals(trueShape, slice.getSourceShape())) { // if shape was squeezed then need to translate to true slice
				final int trank = trueShape.length;
				int[] tstart = new int[trank];
				int[] tsize = new int[trank];

				int j = 0;
				for (int i = 0; i < trank; i++) {
					if (trueShape[i] == 1) {
						tstart[i] = 0;
						tsize[i] = 1;
					} else {
						tstart[i] = lstart[j];
						tsize[i] = size[j];
						j++;
					}
				}
				
				file.putslab(DatasetUtils.serializeDataset(data), tstart, tsize);
			} else {
				file.putslab(DatasetUtils.serializeDataset(data), lstart, size);
			}
		} catch (NexusException e) {
			logger.error("Problem with NeXus library: {}", e);
		} finally {
			if (file != null)
				file.close();
		}
	}
}
