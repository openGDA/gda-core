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

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.SelfCreatingLink;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;

public class StringComponentWriter extends DefaultComponentWriter {

	public StringComponentWriter() {
		// no op
	}

	@Override
	protected String getComponentSlab(final Object pos) {
		return pos.toString();
	}

	@Override
	public Collection<SelfCreatingLink> makeComponent(final NexusFile file, GroupNode group, int[] scanDims, final String path,
			final String scannableName, final String componentName, final Object pos, final String unit)
					throws NexusException {

		String aPath = file.getPath(group) + path;
		final String name = NexusUtils.getName(aPath);

		final String slab = getComponentSlab(pos);

		int[] dim = makedatadimfordim(scanDims);

		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(name, Dataset.STRING, dim, null, null);
		GroupNode lGroup = file.getGroup(aPath.substring(0, aPath.length() - name.length()), true);
		int[] chunk;
		if (LocalProperties.check("gda.nexus.writeSwmr", false)) {
			// Temporary hack to avoid HDF5 bug
			chunk = new int[scanDims.length];
			if (scanDims.length == 1) {
				chunk[0] = 1024;
			} else {
				chunk[chunk.length - 1] = 256;
				chunk[chunk.length - 2] = 4;
			}
		} else {
			chunk = NexusUtils.estimateChunking(scanDims, 8);
		}
		lazy.setChunking(chunk);
		DataNode data = file.createData(lGroup, lazy);

		int[] sstart = nulldimfordim(dim);
		int[] sshape = slabsizedimfordim(dim);
		int[] sstop = sstart.clone();
		for (int i = 0; i < sstop.length; i++) {
			sstop[i] += sshape[i];
		}
		Dataset sdata = DatasetFactory.createFromObject(slab).reshape(sshape);

		try {
			lazy.setSlice(null, sdata, SliceND.createSlice(lazy, sstart, sstop));
		} catch (Exception e) {
			throw new NexusException("Problem writing data", e);
		}

		if (componentName != null) {
			NexusUtils.writeStringAttribute(file, data, "local_name", scannableName + "." + componentName);
		}

		addCustomAttributes(file, lGroup, scannableName, componentName);

		return Collections.emptySet();
	}

}
