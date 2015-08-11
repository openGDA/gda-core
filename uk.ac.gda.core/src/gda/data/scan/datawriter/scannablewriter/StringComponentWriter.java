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

import gda.data.nexus.NexusUtils;
import gda.data.scan.datawriter.SelfCreatingLink;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;

public class StringComponentWriter extends DefaultComponentWriter {

//	private int stringlength = 0;
	private int rank = 0;

	public StringComponentWriter() {
		// no op
	}

	@Override
	protected int[] slabSizeForWriting(final int[] dim, final int slablength) {

		final int[] onedimfordim = slabsizedimfordim(dim);

		if (onedimfordim.length < rank) {
			return ArrayUtils.add(onedimfordim, slablength);

		} else {
			onedimfordim[onedimfordim.length - 1] = slablength;
			return onedimfordim;
		}
	}

	@Override
	protected int[] putslabdimfordim(final int[] dim) {
		return ArrayUtils.add(dim, 0);
	}

	@Override
	protected String getComponentSlab(final Object pos) {
		return pos.toString();
	}

	@Override
	public Collection<SelfCreatingLink> makeComponent(final NexusFile file, GroupNode group, int[] dim, final String path,
			final String scannableName, final String componentName, final Object pos, final String unit)
					throws NexusException {

		String aPath = file.getPath(group) + path;
		final String name = NexusUtils.getName(aPath);

		final String slab = getComponentSlab(pos);

		dim = makedatadimfordim(dim);

		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(name, Dataset.STRING, dim, null, null);
		GroupNode lGroup = file.getGroup(aPath.substring(0, aPath.length() - name.length()), true);
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
