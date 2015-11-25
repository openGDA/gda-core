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

import gda.data.scan.datawriter.SelfCreatingLink;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.exception.NotANumberException;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberComponentWriter extends DefaultComponentWriter {

	/** Logger */
	private static final Logger logger = LoggerFactory.getLogger(NumberComponentWriter.class);

	public NumberComponentWriter() {
		// no op
	}

	@Override
	protected double[] getComponentSlab(final Object pos) {
		if (!(pos instanceof Number)) {
			throw new NotANumberException();
		}
		return new double[] { ((Number) pos).doubleValue() };
	}

	@Override
	public Collection<SelfCreatingLink> makeComponent(final NexusFile file, GroupNode group, final int[] dim,
			final String path, final String scannableName, final String componentName, final Object pos,
			final String unit) throws NexusException {

		DataNode data;

		String aPath = file.getPath(group) + path;

		if (file.isPathValid(aPath)) {
			logger.info("found dataset {} exists already when trying to create it for {}. This may not be a problem provided the data written is the same",
					aPath, scannableName);
			return null;
		}

		String name = NexusUtils.getName(aPath);

		final int[] makedatadim = makedatadimfordim(dim);
		final int[] initialdims = new int[makedatadim.length];
		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(name, Dataset.FLOAT64, initialdims, makedatadim, null);
		lazy.setChunking(chunkfordim(dim, 8));
		GroupNode lGroup = file.getGroup(aPath.substring(0, aPath.length() - name.length()), true);
		data = file.createData(lGroup, lazy);

		int[] sstart = nulldimfordim(dim);
		int[] sshape = slabsizedimfordim(dim);
		int[] sstop = sstart.clone();
		for (int i = 0; i < sstop.length; i++) {
			sstop[i] += sshape[i];
		}
		Dataset sdata = DatasetFactory.createFromObject(getComponentSlab(pos)).reshape(sshape);

		try {
			lazy.setSlice(null, sdata, SliceND.createSlice(lazy, sstart, sstop));
		} catch (Exception e) {
			logger.error("Problem writing data: {}", aPath, e);
		}

		if (componentName != null) {
			NexusUtils.writeStringAttribute(file, data, "local_name", scannableName + "." + componentName);
		}

		final StringBuilder axislist = new StringBuilder(dim.length * 3 + 1).append('1');
		for (int j = 2; j <= dim.length; j++) {
			axislist.append(',').append(j);
		}
		NexusUtils.writeStringAttribute(file, data, "axis", axislist.toString());

		if (StringUtils.isNotBlank(unit)) {
			NexusUtils.writeStringAttribute(file, data, "units", unit);
		}

		addCustomAttributes(file, data, scannableName, componentName);
		final SelfCreatingLink scl = new SelfCreatingLink(data);

		return Collections.singleton(scl);
	}

}
