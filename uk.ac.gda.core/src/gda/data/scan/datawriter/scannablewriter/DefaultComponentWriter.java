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
package gda.data.scan.datawriter.scannablewriter;

import java.lang.reflect.Array;
import java.nio.charset.Charset;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultComponentWriter implements ComponentWriter {
	private static final Logger logger = LoggerFactory.getLogger(DefaultComponentWriter.class);

	protected static final Charset UTF8 = Charset.forName("UTF-8");

	public DefaultComponentWriter() {
		// no op
	}

	protected int[] nulldimfordim(final int[] dim) {
		return dimForDimForValue(dim, 0);
	}

	protected int[] slabsizedimfordim(final int[] dim) {
		return dimForDimForValue(dim, 1);
	}

	protected int[] makedatadimfordim(final int[] dim) {
		return dimForDimForValue(dim, -1);
	}

	protected int[] chunkfordim(final int[] dim, final int dataByteSize) {
		return NexusUtils.estimateChunking(dim, dataByteSize);
	}

	private int[] dimForDimForValue(final int[] dim, final int value) {
		final int[] mdim = (dim != null) ? new int[dim.length] : new int[] {};
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = value;
		}

		return mdim;
	}

	protected int[] putslabdimfordim(final int[] dim) {
		return dim;
	}

	protected abstract Object getComponentSlab(final Object pos);

	protected int[] slabSizeForWriting(final int[] dim, @SuppressWarnings("unused") final int length) {
		// default: ignore length
		return slabsizedimfordim(dim);
	}

	/**
	 * To allow overriding classes to add random attributes if required the file will have the data set "open" ready to
	 * receive putattr calls
	 *
	 * @param file
	 *            nexus file
	 * @param scannableName
	 *            name of Scannable
	 * @param componentName
	 *            extra or input name being written
	 * @throws NexusException
	 */
	@SuppressWarnings("unused")
	// Overriding classes will use the arguments
	protected void addCustomAttributes(final NexusFile file, Node group, final String scannableName, final String componentName) throws NexusException {
		// Default no operation
	}

	@Override
	public void writeComponent(final NexusFile file, GroupNode group, final int[] start, final String path,
			final String scannableName, final String componentName, final Object pos) throws NexusException {

		String aPath = file.getPath(group) + path;
		DataNode data = file.getData(aPath);
		ILazyWriteableDataset lazy = data.getWriteableDataset();
		final Object slab = getComponentSlab(pos);
		final int slablength = (slab.getClass().isArray()) ? Array.getLength(slab) : 0;
		int[] sstart = putslabdimfordim(start);
		int[] sshape = slabSizeForWriting(start, slablength);
		int[] sstop = sstart.clone();
		for (int i = 0; i < sstop.length; i++) {
			sstop[i] += sshape[i];
		}
		Dataset sd = DatasetFactory.createFromObject(slab).reshape(sshape);
		try {
			lazy.setSlice(null, sd, SliceND.createSlice(lazy, sstart, sstop));
		} catch (Exception e) {
			logger.error("Could not write slab: {}", aPath, e);
			throw new NexusException("Could not write slab", e);
		}
	}
}
