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
import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * At the moment this class should handle well formed scannables returning Doubles or Strings 
 * with multiple input or extra names
 */
public class SingleScannableWriter implements ScannableWriter {
	private static final Logger logger = LoggerFactory.getLogger(SingleScannableWriter.class);

	protected String[] paths;
	protected String[] units;
	protected Collection<String> prerequisiteScannableNames;
	protected Map<String, ComponentWriter> cwriter = new HashMap<String, ComponentWriter>();

	protected static int componentsFor(Scannable s) {
		int i = s.getInputNames() != null ? s.getInputNames().length : 0;
		int e = s.getExtraNames() != null ? s.getExtraNames().length : 0;
		return i + e;
	}

	protected static String componentNameFor(Scannable s, int i) {
		return ArrayUtils.addAll(s.getInputNames() != null ? s.getInputNames() : new String[] {},
				s.getExtraNames() != null ? s.getExtraNames() : new String[] {})[i].toString();
	}

	protected static int indexForcomponentName(Scannable s, String component) {
		String[] all = (String[]) ArrayUtils.addAll(s.getInputNames() != null ? s.getInputNames() : new String[] {},
				s.getExtraNames() != null ? s.getExtraNames() : new String[] {});
		for (int i = 0; i < all.length; i++) {
			if (component.equals(all[i]))
				return i;
		}
		throw new ArrayIndexOutOfBoundsException();
	}
	
	protected ComponentWriter getComponentWriter(Scannable s, String componentName, Object object) {
		if (cwriter.containsKey(componentName))
			return cwriter.get(componentName);
		DefaultComponentWriter cw = null;
		if (object instanceof String)
			cw = new StringComponentWriter();
		if (cw == null) // default
			cw = new DefaultComponentWriter(); // Doubles
		cwriter.put(componentName, cw);
		return cw;
	}
	
	protected void resetComponentWriters() {
		cwriter = new HashMap<String, ComponentWriter>();
	}

	@Override
	public Collection<? extends SelfCreatingLink> makeScannable(NeXusFileInterface file, Scannable s, Object position,
			int[] dim) throws NexusException {
		Vector<SelfCreatingLink> sclc = new Vector<SelfCreatingLink>();
		resetComponentWriters();

		for (int i = 0; i < componentsFor(s); i++) {
			try {
				if (paths[i].isEmpty())
					continue;
				String componentName = componentNameFor(s, i);
				String unit = null;
				if (s instanceof ScannableMotionUnits)
					unit = ((ScannableMotionUnits) s).getUserUnits();
				if (units != null && units.length > i)
					unit = units[i];
				Object componentObject = getComponentObject(s, position, i);
				ComponentWriter cw = getComponentWriter(s, componentName, componentObject);
				sclc.addAll(cw.makeComponent(file, dim, paths[i], s.getName(), componentName, componentObject, unit));
			} catch (Exception e) {
				logger.error("error converting scannable data", e);
			}
		}
		return sclc;
	}
	
	@Override
	public void writeScannable(NeXusFileInterface file, Scannable s, Object position, int[] start)
			throws NexusException {
		for (int i = 0; i < componentsFor(s); i++) {
			if (paths[i].isEmpty())
				continue;
			Object slab;
			slab = getComponentObject(s, position, i);
			cwriter.get(componentNameFor(s, i)).writeComponent(file, start, paths[i], s.getName(),
					componentNameFor(s, i), slab);
		}
	}

	protected Object getComponentObject(@SuppressWarnings("unused") Scannable s, Object position, int i) {
		return getArrayObject(position)[i];
	}

	private final Class<?>[] ARRAY_PRIMITIVE_TYPES = { int[].class, float[].class, double[].class, boolean[].class,
			byte[].class, short[].class, long[].class, char[].class };

	private Object[] getArrayObject(Object foo) {
		if (foo.getClass().isAssignableFrom(Object[].class))
			return (Object[]) foo;
		if (foo.getClass().isArray()) {
			Class<?> valKlass = foo.getClass();
			Object[] outputArray = null;

			for (Class<?> arrKlass : ARRAY_PRIMITIVE_TYPES) {
				if (valKlass.isAssignableFrom(arrKlass)) {
					int arrlength = Array.getLength(foo);
					outputArray = new Object[arrlength];
					for (int i = 0; i < arrlength; ++i) {
						outputArray[i] = Array.get(foo, i);
					}
					break;
				}
			}
			if (outputArray == null) // not primitive type array
				outputArray = (Object[]) foo;

			return outputArray;
		}
		return new Object[] { foo };
	}

	public String[] getPaths() {
		return paths;
	}

	public void setPaths(String[] paths) {
		this.paths = paths;
	}

	public String[] getUnits() {
		return units;
	}

	public void setUnits(String[] units) {
		this.units = units;
	}

	public void setPrerequisiteScannableNames(Collection<String> prerequisiteScannableNames) {
		this.prerequisiteScannableNames = prerequisiteScannableNames;
	}

	@Override
	public Collection<String> getPrerequisiteScannableNames() {
		return prerequisiteScannableNames;
	}
}