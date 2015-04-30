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
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ILazyLoader;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.IndexIterator;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NAPILazyLoader implements ILazyLoader, Serializable {
	protected static final Logger logger = LoggerFactory.getLogger(NAPILazyLoader.class);
	protected URI source;
	protected String path;
	protected String name;
	protected int[] trueShape;
	protected String filename;
	protected int dtype;

	/**
	 * @param tree
	 * @param path group where dataset will reside
	 * @param name
	 * @param shape
	 * @param dtype
	 */
	public NAPILazyLoader(Tree tree, String path, String name, int[] shape, int dtype) {
		source = tree.getSourceURI();
		if (tree instanceof TreeFile) {
			filename = ((TreeFile) tree).getFilename();
		}
		this.path = path;
		this.name = name;
		trueShape = shape;
		this.dtype = dtype;
	}

	protected boolean checkHost() {
		try {
			String host = source.getHost();
			if (host != null && host.length() > 0 && !host.equals(InetAddress.getLocalHost().getHostName()))
				return false;
		} catch (UnknownHostException e) {
			logger.warn("Problem finding local host so ignoring check", e);
		}
		return true;
	}

	@Override
	public boolean isFileReadable() {
		if (!checkHost())
			return false;

		return new File(filename != null ? filename : source.getPath()).canRead();
	}

	@Override
	public IDataset getDataset(IMonitor mon, SliceND slice) throws Exception {
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

		Dataset d = null;
		NexusFile file = null;
		int textLength = -1;

		try {
			file = new NexusFile(filename, NexusFile.NXACC_READ);
			file.openpath(path);
			file.opendata(name);
			if (dtype == Dataset.STRING) {
				int[] infoDims = new int[10];
				int[] infoArgs = new int[10];
				file.getinfo(infoDims, infoArgs);
				if (infoArgs[1] != NexusFile.NX_CHAR) {
					throw new ScanFileHolderException("Dataset was expected to contain strings"); 
				}
				int trank = infoArgs[0] - 1;
				if (trank != trueShape.length) {
					throw new ScanFileHolderException("Dataset has wrong rank"); 
				}
				textLength = infoDims[trank];
				int[] tsize = Arrays.copyOf(size, trank + 1);
				tsize[trank] = textLength;
				d = DatasetFactory.zeros(tsize, Dataset.INT8);
			} else {
				d = DatasetFactory.zeros(size, dtype);
			}
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
				if (textLength > 0) {
					d = readSlabAsStrings(file, textLength, tstart, tsize, (byte[]) d.getBuffer());
				} else {
					file.getslab(tstart, tsize, d.getBuffer());
				}
				d.setShape(size); // squeeze shape back
			} else {
				if (textLength > 0) {
					d = readSlabAsStrings(file, textLength, lstart, size, (byte[]) d.getBuffer());
				} else {
					file.getslab(lstart, size, d.getBuffer());
				}
			}
			if (useSteps)
				d = d.getSlice(null, null, lstep); // reduce dataset to requested elements
			d.setName(name);
		} catch (NexusException e) {
			logger.error("Problem with NeXus library: {}", e);
		} finally {
			if (file != null)
				file.close();
		}
		return d;
	}

	private static Dataset readSlabAsStrings(NexusFile file, int length, int[] start, int[] size, byte[] buffer) throws NexusException {
		int[] bstart = Arrays.copyOf(start, start.length + 1);
		int[] bsize = Arrays.copyOf(size, size.length + 1);
		bsize[bsize.length - 1] = length;
		if (AbstractDataset.calcSize(bsize) != buffer.length) {
			throw new IllegalArgumentException("Buffer of incorrect length");
		}
		file.getslab(bstart, bsize, buffer);
		byte[] text = new byte[length];
		StringDataset data = new StringDataset(size);
		IndexIterator it = data.getIterator();
		String[] strings = data.getData();
		int k = 0;
		while (it.hasNext()) {
			System.arraycopy(buffer, k, text, 0, length);
			k += length;
			try {
				strings[it.index] = new String(text, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				strings[it.index] = new String(text);
			}
		}
		return data;
	}
}
