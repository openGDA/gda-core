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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;

import gda.data.scan.datawriter.SelfCreatingLink;

public class StringComponentWriter extends DefaultComponentWriter<String> {

	public StringComponentWriter() {
		// no op
	}

	@Override
	protected String getComponentSlab(final Object pos) {
		return pos.toString();
	}

	@Override
	public Collection<SelfCreatingLink> makeComponent(final NexusFile file, GroupNode group, int[] scanDims, final String path,
			final String scannableName, final String componentName, final Object pos, final String unit, final boolean primary)
					throws NexusException {

		String aPath = file.getPath(group) + path;
		final String name = NexusUtils.getName(aPath);

		final String slab = getComponentSlab(pos);

		int[] dim = makedatadimfordim(scanDims);

		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(name, String.class, dim, null, null);
		GroupNode lGroup = file.getGroup(aPath.substring(0, aPath.length() - name.length()), true);
		lazy.setChunking(NexusUtils.estimateChunking(scanDims, 8));
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
			throw new NexusException("Problem writing data: " + aPath, e);
		}

		if (componentName != null) {
			NexusUtils.writeStringAttribute(file, data, "local_name", scannableName + "." + componentName);
		}

		if (primary) {
			NexusUtils.writeAttribute(file, data, "primary", 1);
		}

		addCustomAttributes(file, lGroup, scannableName, componentName);

		return Collections.emptySet();
	}

}
