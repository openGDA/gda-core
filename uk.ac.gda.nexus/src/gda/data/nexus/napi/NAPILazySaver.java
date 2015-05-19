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

import gda.data.nexus.extractor.NexusGroupData;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ILazySaver;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class NAPILazySaver extends NAPILazyLoader implements ILazySaver, Serializable {

	private int textLength;

	/**
	 * @param file 
	 * @param tree
	 * @param path group where dataset will reside
	 * @param name
	 * @param shape
	 * @param dtype
	 */
	public NAPILazySaver(NexusFile file, Tree tree, String path, String name, int[] shape, int dtype, boolean isUnsigned) {
		super(file, tree, path, name, shape, dtype, isUnsigned);
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public boolean isFileWriteable() {
		if (!checkHost())
			return false;

		return new File(filename != null ? filename : source.getPath()).canWrite();
	}

	@Override
	protected NexusFile open() throws NexusException {
		return new NexusFile(filename, NexusFile.NXACC_RDWR);
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

		boolean close = true;
		try {
			if (file != null) {
				try {
					file.openpath(path);
					close = false; // still open
				} catch (NexusException e1) {
					file = null;
				}
			}
			if (file == null) {
				file = open();
				file.openpath(path);
			}
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
				
				saveData(file, data, tstart, tsize);
			} else {
				saveData(file, data, lstart, size);
			}
		} catch (NexusException e) {
			logger.error("Problem with NeXus library: {}", e);
		} finally {
			if (close) {
				file.close();
				file = null;
			}
		}
	}

	/**
	 * Set maximum length to be used for string datasets
	 * @param maxTextLength the maximum number of bytes used to encode any string
	 */
	public void setMaxTextLength(int maxTextLength) {
		textLength = maxTextLength;
	}

	private void saveData(NexusFile file, IDataset data, int[] start, int[] size) throws org.nexusformat.NexusException {
		Dataset cdata = DatasetUtils.cast(data, dtype);
		if (cdata.getRank() == 0) {
			cdata.setShape(1);
		}
		NexusGroupData ngd = NexusGroupData.createFromDataset(cdata);
		ngd.setMaxStringLength(textLength);
		Serializable slab = ngd.getBuffer(true);
		int[] tsize = ngd.getDimensions();
		if (ngd.getDtype() == Dataset.STRING && !Arrays.equals(size, tsize)) {
			if (tsize.length > size.length) {
				start = Arrays.copyOf(start, tsize.length);
			}
			file.putslab(slab, start, tsize);
		} else {
			file.putslab(slab, start, size);
		}
	}
}
