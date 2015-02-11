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
import java.util.StringTokenizer;

import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultComponentWriter implements ComponentWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComponentWriter.class);

	protected static final Charset UTF8 = Charset.forName("UTF-8");

	private int levels = 0;

	public DefaultComponentWriter() {
		// no op
	}

	/**
	 * Set the file into the position to write the data
	 *
	 * @param file
	 * @return name of trailing component
	 * @throws NexusException
	 */
	protected String enterLocation(final NeXusFileInterface file, final String path) throws NexusException {
		levels = 0;
		final StringTokenizer st = new StringTokenizer(path, "/");
		while (st.hasMoreTokens()) {
			final String[] split = st.nextToken().split(":");
			final String name = split[0];
			if (split.length == 1) {
				// no class, write data
				return name;
			}
			final String clazz = split[1];
			try {
				file.makegroup(name, clazz);
			} catch (final NexusException ne) {
				// ignore, it might be there already
			}
			file.opengroup(name, clazz);
			levels++;
		}

		throw new IllegalArgumentException("configured path is not well formed (suspect it has no trailing component)");
	}

	protected void leaveLocation(final NeXusFileInterface file) throws NexusException {
		for (int i = 0; i < levels; i++) {
			file.closegroup();
		}
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

	protected int[] slabSizeForWriting(final int[] dim, final int length) {
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
	protected void addCustomAttributes(final NeXusFileInterface file, final String scannableName,
			final String componentName) throws NexusException {
		// Default no operation
	}

	@Override
	public void writeComponent(final NeXusFileInterface file, final int[] start, final String path,
			final String scannableName, final String componentName, final Object pos) throws NexusException {

		final String name = enterLocation(file, path);

		file.opendata(name);
		try {
			final Object slab = getComponentSlab(pos);
			final int slablength = (slab.getClass().isArray()) ? Array.getLength(slab) : 0;
			file.putslab(slab, putslabdimfordim(start), slabSizeForWriting(start, slablength));

		} catch (final Exception e) {
			LOGGER.error("error converting scannable data", e);
		}
		file.closedata();

		leaveLocation(file);
	}
}
