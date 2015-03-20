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

import gda.data.nexus.NexusException;
import gda.data.nexus.NexusFileInterface;
import gda.data.nexus.NexusGlobals;
import gda.data.scan.datawriter.SelfCreatingLink;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ArrayUtils;

public class StringComponentWriter extends DefaultComponentWriter {

	private int stringlength = 0;
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
	protected byte[] getComponentSlab(final Object pos) {
		return ArrayUtils.add(pos.toString().getBytes(UTF8), (byte) 0);
	}

	@Override
	public Collection<SelfCreatingLink> makeComponent(final NexusFileInterface file, int[] dim, final String path,
			final String scannableName, final String componentName, final Object pos, final String unit)
					throws NexusException {

		final String name = enterLocation(file, path);

		stringlength = 127;

		final Object slab = getComponentSlab(pos);
		final int slablength = Array.getLength(slab);

		if (Arrays.equals(dim, new int[] { 1 })) {
			stringlength = slablength;
		} else if (slablength + 10 > stringlength) { // if strings vary more than that we are in trouble
			stringlength = slablength + 10;
		}

		dim = makedatadimfordim(dim);

		if (dim[dim.length - 1] == 1) {
			dim[dim.length - 1] = stringlength;
		} else {
			dim = ArrayUtils.add(dim, stringlength);
		}
		rank = dim.length;

		file.makedata(name, NexusGlobals.NX_CHAR, rank, dim);
		file.opendata(name);

		if (componentName != null) {
			file.putattr("local_name", (scannableName + "." + componentName).getBytes(UTF8), NexusGlobals.NX_CHAR);
		}

		addCustomAttributes(file, scannableName, componentName);
		file.putslab(slab, nulldimfordim(dim), slabsizedimfordim(dim));

		file.closedata();
		leaveLocation(file);

		return Collections.emptySet();
	}
}
