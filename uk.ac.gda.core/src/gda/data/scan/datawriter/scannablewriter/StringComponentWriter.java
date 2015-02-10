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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringComponentWriter extends DefaultComponentWriter {
	private static final Logger logger = LoggerFactory.getLogger(DefaultComponentWriter.class);

	int stringlength = 0;
	int rank = 0;

	public StringComponentWriter() {
	}

	protected int[] slabsizedimfordim(final int[] dim, final byte[] slab) {
		final int[] onedimfordim = super.slabsizedimfordim(dim);
		if (onedimfordim.length < rank) {
			return ArrayUtils.add(onedimfordim, slab.length);
		}
		onedimfordim[onedimfordim.length - 1] = slab.length;
		return onedimfordim;
	}

	@Override
	protected int[] putslabdimfordim(final int[] dim) {
		return ArrayUtils.add(dim, 0);
	}

	@Override
	protected byte[] getComponentSlab(final Object pos) {
		final byte[] slab = ArrayUtils.add(pos.toString().getBytes(Charset.forName("UTF-8")), (byte) 0);
		return slab;
	}

	@Override
	public Collection<SelfCreatingLink> makeComponent(final NeXusFileInterface file, int[] dim, final String path,
			final String scannableName, final String componentName, final Object pos, final String unit)
					throws NexusException {

		final String name = enterLocation(file, path);

		stringlength = 127;

		final byte[] slab = getComponentSlab(pos);

		if (Arrays.equals(dim, new int[] { 1 })) {
			stringlength = slab.length;
		} else if (slab.length + 10 > stringlength) { // if strings vary more than that we are in trouble
			stringlength = slab.length + 10;
		}

		dim = makedatadimfordim(dim);

		if (dim[dim.length - 1] == 1) {
			dim[dim.length - 1] = stringlength;
		} else {
			dim = ArrayUtils.add(dim, stringlength);
		}
		rank = dim.length;

		file.makedata(name, NexusFile.NX_CHAR, rank, dim);
		file.opendata(name);
		file.putattr("local_name", String.format("%s.%s", scannableName, componentName).getBytes(), NexusFile.NX_CHAR);

		file.putslab(slab, nulldimfordim(dim), slabsizedimfordim(dim));

		file.closedata();

		leaveLocation(file);
		return new Vector<SelfCreatingLink>();
	}

	@Override
	public void writeComponent(final NeXusFileInterface file, final int[] start, final String path,
			final String scannableName, final String componentName, final Object pos) throws NexusException {
		final String name = enterLocation(file, path);

		file.opendata(name);
		try {
			final byte[] slab = getComponentSlab(pos);
			final int[] putslabdim = putslabdimfordim(start);
			final int[] slabsizedim = slabsizedimfordim(start, slab);
			file.putslab(slab, putslabdim, slabsizedim);
		} catch (final Exception e) {
			logger.error("error converting scannable data", e);
		}
		file.closedata();

		leaveLocation(file);
	}
}
