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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.SelfCreatingLink;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

/**
 * At the moment this class should handle well formed scannables returning Doubles or Strings with multiple input or
 * extra names
 */
public class SingleScannableWriter implements ScannableWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SingleScannableWriter.class);

	private String[] paths;
	private String[] units;
	private Set<String> prerequisiteScannableNames = Collections.emptySet();
	private final Map<String, ComponentWriter> cwriter = new HashMap<>();

	protected ComponentWriter getComponentWriter(final String componentName, final Object object, @SuppressWarnings("unused") int index) {

		if (!cwriter.containsKey(componentName)) {
			cwriter.put(componentName, (object instanceof Number) ?  new NumberComponentWriter() : new StringComponentWriter());
		}
		return cwriter.get(componentName);
	}

	protected void resetComponentWriters() {
		getCwriter().clear();
	}

	@Override
	public Collection<? extends SelfCreatingLink> makeScannable(final NexusFile file, GroupNode group, final Scannable s,
			final Object position, final int[] dim, final boolean primary) throws NexusException {

		final List<SelfCreatingLink> sclc = new ArrayList<>();
		resetComponentWriters();

		final String[] arrayNames = ((String[]) ArrayUtils.addAll(s.getInputNames(), s.getExtraNames()));
		final Object[] arrayValues = getArrayObject(position);

		for (int i = 0; i < arrayNames.length; i++) {
			try {
				if (getPaths() == null || getPaths().length <= i || getPaths()[i].isEmpty()) {
					continue;
				}

				final String unit;
				if (getUnits() != null && getUnits().length > i) {
					unit = getUnits()[i];
				} else if (s instanceof ScannableMotionUnits) {
					unit = ((ScannableMotionUnits) s).getUserUnits();
				} else {
					unit = null;
				}

				final ComponentWriter cw = getComponentWriter(arrayNames[i], arrayValues[i], i);
				final Collection<SelfCreatingLink> compLinks = cw.makeComponent(file, group, dim, getPaths()[i], s.getName(), arrayNames[i], arrayValues[i],
						unit, primary);
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

		final String[] arrayNames = ((String[]) ArrayUtils.addAll(s.getInputNames(), s.getExtraNames()));
		final Object[] arrayValues = getArrayObject(position);

		for (int i = 0; i < arrayNames.length; i++) {
			if (getPaths() == null || getPaths().length <= i || getPaths()[i].isEmpty()) {
				continue;
			}
			getCwriter().get(arrayNames[i]).writeComponent(file, group, start, getPaths()[i], s.getName(),
					arrayNames[i], arrayValues[i]);
		}
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

	public final void setPaths(final String... paths) {
		this.paths = paths;
	}

	public String[] getUnits() {
		return units;
	}

	public final void setUnits(final String... units) {
		this.units = units;
	}

	public final void setPrerequisiteScannableNames(final Collection<String> prerequisiteScannableNames) {
		this.prerequisiteScannableNames = prerequisiteScannableNames == null ?
				Collections.emptySet() : new HashSet<>(prerequisiteScannableNames);
	}

	@Override
	public Set<String> getPrerequisiteScannableNames() {
		return prerequisiteScannableNames;
	}

	protected Map<String, ComponentWriter> getCwriter() {
		return cwriter;
	}
}
