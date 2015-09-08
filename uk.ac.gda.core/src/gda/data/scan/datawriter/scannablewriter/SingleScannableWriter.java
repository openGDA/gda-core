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
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * At the moment this class should handle well formed scannables returning Doubles or Strings with multiple input or
 * extra names
 */
public class SingleScannableWriter implements ScannableWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SingleScannableWriter.class);

	private String[] paths;
	private String[] units;
	private Collection<String> prerequisiteScannableNames;
	private final Map<String, ComponentWriter> cwriter = new HashMap<String, ComponentWriter>();

	private final int componentsFor(final Scannable s) {
		final int i = (s.getInputNames() != null) ? s.getInputNames().length : 0;
		final int e = (s.getExtraNames() != null) ? s.getExtraNames().length : 0;
		return i + e;
	}

	private final String componentNameFor(final Scannable s, final int i) {
		return ArrayUtils.addAll((s.getInputNames() != null) ? s.getInputNames() : new String[] {},
				(s.getExtraNames() != null) ? s.getExtraNames() : new String[] {})[i].toString();
	}

	protected static int indexForComponentName(final Scannable s, final String component) {
		// FIXME : ArrayUtils.addAll(s.getInputNames(), s.getExtraNames()) should yield same result
		final String[] all = (String[]) ArrayUtils.addAll((s.getInputNames() != null) ? s.getInputNames()
				: new String[] {}, (s.getExtraNames() != null) ? s.getExtraNames() : new String[] {});

		if (all != null) {
			for (int i = 0; i < all.length; i++) {
				if (component.equals(all[i])) {
					return i;
				}
			}
		}

		throw new ArrayIndexOutOfBoundsException();
	}

	protected ComponentWriter getComponentWriter(@SuppressWarnings("unused") final Scannable s, final String componentName, final Object object) {

		final Map<String, ComponentWriter> cwmap = getCwriter();

		if (cwmap.containsKey(componentName)) {
			return cwmap.get(componentName);
		}

		final ComponentWriter cw;
		if (object instanceof Number) {
			cw = new NumberComponentWriter();
		} else {
			cw = new StringComponentWriter();
		}

		cwmap.put(componentName, cw);
		return cw;
	}

	protected void resetComponentWriters() {
		getCwriter().clear();
	}

	@Override
	public Collection<? extends SelfCreatingLink> makeScannable(final NexusFile file, GroupNode group, final Scannable s,
			final Object position, final int[] dim) throws NexusException {

		final Vector<SelfCreatingLink> sclc = new Vector<SelfCreatingLink>();
		resetComponentWriters();

		for (int i = 0; i < componentsFor(s); i++) {
			try {
				if (getPaths() == null || getPaths().length <= i || getPaths()[i].isEmpty()) {
					continue;
				}
				final String componentName = componentNameFor(s, i);

				final String unit;
				if (getUnits() != null && getUnits().length > i) {
					unit = getUnits()[i];
				} else if (s instanceof ScannableMotionUnits) {
					unit = ((ScannableMotionUnits) s).getUserUnits();
				} else {
					unit = null;
				}

				final Object componentObject = getComponentObject(s, position, i);
				final ComponentWriter cw = getComponentWriter(s, componentName, componentObject);
				final Collection<SelfCreatingLink> compLinks = cw.makeComponent(file, group, dim, getPaths()[i], s.getName(), componentName, componentObject,
						unit);
				sclc.addAll(compLinks);

			} catch (final Exception e) {
				LOGGER.error("error converting scannable data", e);
			}
		}

		return sclc;
	}

	@Override
	public void writeScannable(final NexusFile file, GroupNode group, final Scannable s, final Object position,
			final int[] start) throws NexusException {

		for (int i = 0; i < componentsFor(s); i++) {
			if (getPaths() == null || getPaths().length <= i || getPaths()[i].isEmpty()) {
				continue;
			}
			final Object slab = getComponentObject(s, position, i);
			getCwriter().get(componentNameFor(s, i)).writeComponent(file, group, start, getPaths()[i], s.getName(),
					componentNameFor(s, i), slab);
		}
	}

	protected Object getComponentObject(@SuppressWarnings("unused") final Scannable s, final Object position, final int i) {
		return getArrayObject(position)[i];
	}

	private final Object[] getArrayObject(final Object position) {

		if (position.getClass().isArray()) {
			final Object[] outputArray;

			if (position.getClass().getComponentType().isPrimitive()) {
				final int arrlength = Array.getLength(position);
				outputArray = new Object[arrlength];
				for (int i = 0; i < arrlength; ++i) {
					outputArray[i] = Array.get(position, i);
				}

			} else {
				outputArray = (Object[]) position;
			}

			return outputArray;

		} else {
			return new Object[] { position };
		}
	}

	public String[] getPaths() {
		return paths;
	}

	public final void setPaths(final String[] paths) {
		this.paths = paths;
	}

	public String[] getUnits() {
		return units;
	}

	public final void setUnits(final String[] units) {
		this.units = units;
	}

	public final void setPrerequisiteScannableNames(final Collection<String> prerequisiteScannableNames) {
		this.prerequisiteScannableNames = prerequisiteScannableNames;
	}

	@Override
	public Collection<String> getPrerequisiteScannableNames() {
		return prerequisiteScannableNames;
	}

	protected Map<String, ComponentWriter> getCwriter() {
		return cwriter;
	}
}
