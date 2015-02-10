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
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.math3.exception.NotANumberException;
import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultComponentWriter implements ComponentWriter {
	private static final Logger logger = LoggerFactory.getLogger(DefaultComponentWriter.class);

	private int levels = 0;

	public DefaultComponentWriter() {
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
		final int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = 0;
		}
		return mdim;
	}

	protected int[] slabsizedimfordim(final int[] dim) {
		final int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = 1;
		}
		return mdim;
	}

	protected int[] makedatadimfordim(final int[] dim) {
		final int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = -1;
		}
		return mdim;
	}

	protected int[] putslabdimfordim(final int[] dim) {
		return dim;
	}

	protected Object getComponentSlab(final Object pos) {
		if (!(pos instanceof Number)) {
			throw new NotANumberException();
		}
		return new double[] { ((Number) pos).doubleValue() };
	}

	@Override
	public Collection<SelfCreatingLink> makeComponent(final NeXusFileInterface file, final int[] dim,
			final String path, final String scannableName, final String componentName, final Object pos,
			final String unit) throws NexusException {
		final Vector<SelfCreatingLink> sclc = new Vector<SelfCreatingLink>();

		final String name = enterLocation(file, path);

		try {
			file.opendata(name);
			logger.info("found dataset " + path + " exists already when trying to create it for " + scannableName
					+ ". This may not be a problem provided the data written is the same");
			return sclc;
		} catch (final NexusException e) {
			// this is normal case!
			final int[] makedatadim = makedatadimfordim(dim);
			file.makedata(name, NexusFile.NX_FLOAT64, makedatadim.length, makedatadim);
			file.opendata(name);
			if (componentName != null) {
				file.putattr("local_name", String.format("%s.%s", scannableName, componentName).getBytes(),
						NexusFile.NX_CHAR);
			}

			String axislist = "1";
			for (int j = 2; j <= dim.length; j++) {
				axislist = axislist + String.format(",%d", j);
			}
			file.putattr("axis", axislist.getBytes(), NexusFile.NX_CHAR);
			if (unit != null && !unit.isEmpty()) {
				file.putattr("units", unit.getBytes(Charset.forName("UTF-8")), NexusFile.NX_CHAR);
			}
			addCustomAttributes(file, scannableName, componentName);
			file.putslab(getComponentSlab(pos), nulldimfordim(dim), slabsizedimfordim(dim));

			sclc.add(new SelfCreatingLink(file.getdataID()));
			file.closedata();
		} finally {
			leaveLocation(file);
		}

		return sclc;
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
	protected void addCustomAttributes(final NeXusFileInterface file, final String scannableName,
			final String componentName) throws NexusException {
	}

	@Override
	public void writeComponent(final NeXusFileInterface file, final int[] start, final String path,
			final String scannableName, final String componentName, final Object pos) throws NexusException {
		final String name = enterLocation(file, path);

		file.opendata(name);
		try {
			file.putslab(getComponentSlab(pos), putslabdimfordim(start), slabsizedimfordim(start));
		} catch (final Exception e) {
			logger.error("error converting scannable data", e);
		}
		file.closedata();

		leaveLocation(file);
	}
}