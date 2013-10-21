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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple example of a scannable with one input or extra name that needs to record in a specific place
 */
public class SingleScannableWriter implements ScannableWriter {
	private static final Logger logger = LoggerFactory.getLogger(SingleScannableWriter.class);

	protected String[] paths;
	protected String[] units;
	protected Collection<String> prerequisiteScannableNames;
	protected Map<String, ComponentWriter> cwriter = new HashMap<String, SingleScannableWriter.ComponentWriter>();


	protected class ComponentWriter {
		private int levels = 0;
		
		public ComponentWriter() { }

		/**
		 * Set the file into the position to write the data
		 * 
		 * @param file
		 * @return name of trailing component
		 * @throws NexusException
		 */
		protected String enterLocation(NeXusFileInterface file, String path) throws NexusException {
			levels = 0;
			StringTokenizer st = new StringTokenizer(path, "/");
			while (st.hasMoreTokens()) {
				String[] split = st.nextToken().split(":");
				String name = split[0];
				if (split.length == 1) {
					// no class, write data
					return name;
				}
				String clazz = split[1];
				try {
					file.makegroup(name, clazz);
				} catch (NexusException ne) {
					// ignore, it might be there already
				}
				file.opengroup(name, clazz);
				levels++;
			}

			throw new IllegalArgumentException(
					"configured path is not well formed (suspect it has no trailing component)");
		}

		protected void leaveLocation(NeXusFileInterface file) throws NexusException {
			for (int i = 0; i < levels; i++) {
				file.closegroup();
			}
		}

		protected int[] makedimfordim(int[] dim) {
			int[] mdim = new int[dim.length];
			for (int i = 0; i < mdim.length; i++) {
				mdim[i] = -1;
			}
			return mdim;
		}

		protected int[] nulldimfordim(int[] dim) {
			int[] mdim = new int[dim.length];
			for (int i = 0; i < mdim.length; i++) {
				mdim[i] = 0;
			}
			return mdim;
		}

		protected int[] onedimfordim(int[] dim) {
			int[] mdim = new int[dim.length];
			for (int i = 0; i < mdim.length; i++) {
				mdim[i] = 1;
			}
			return mdim;
		}

		protected int[] minusonedimfordim(int[] dim) {
			int[] mdim = new int[dim.length];
			for (int i = 0; i < mdim.length; i++) {
				mdim[i] = -1;
			}
			return mdim;
		}

		protected Object getComponentSlab(Object pos) {
			Object poso = pos;
			return new double[] { (Double) poso };
		}

		protected Collection<SelfCreatingLink> makeComponent(NeXusFileInterface file, int[] dim, String path,
				String scannableName, String componentName, Object pos, String unit) throws NexusException {
			Vector<SelfCreatingLink> sclc = new Vector<SelfCreatingLink>();

			String name = enterLocation(file, path);

			file.makedata(name, NexusFile.NX_FLOAT64, dim.length, minusonedimfordim(dim));
			file.opendata(name);
			file.putattr("local_name", String.format("%s.%s", scannableName, componentName).getBytes(),
					NexusFile.NX_CHAR);

			String axislist = "1";
			for (int j = 2; j <= dim.length; j++) {
				axislist = axislist + String.format(",%d", j);
			}
			file.putattr("axis", axislist.getBytes(), NexusFile.NX_CHAR);
			if (unit != null && !unit.isEmpty())
				file.putattr("units", unit.getBytes(Charset.forName("UTF-8")), NexusFile.NX_CHAR);
			file.putslab(getComponentSlab(pos), nulldimfordim(dim), onedimfordim(dim));

			sclc.add(new SelfCreatingLink(file.getdataID()));
			file.closedata();

			leaveLocation(file);

			return sclc;
		}

		protected void writeComponent(NeXusFileInterface file, int[] start, String path, String scannableName,
				String componentName, Object pos) throws NexusException {
			String name = enterLocation(file, path);

			file.opendata(name);
			try {

				file.putslab(getComponentSlab(pos), start, onedimfordim(start));
			} catch (Exception e) {
				logger.error("error converting scannable data", e);
			}
			file.closedata();

			leaveLocation(file);
		}
	}

	protected class StringComponentWriter extends ComponentWriter {
		int stringlength = 0;
		int rank = 0;

		public StringComponentWriter() { }
		
		@Override
		protected int[] onedimfordim(int[] dim) {
			int[] onedimfordim = super.onedimfordim(dim);
			if (onedimfordim.length < rank)
				return ArrayUtils.add(onedimfordim, stringlength);
			onedimfordim[onedimfordim.length - 1] = stringlength;
			return onedimfordim;
		}

		@Override
		protected Object getComponentSlab(Object pos) {
			byte[] slab = ArrayUtils.add(pos.toString().getBytes(Charset.forName("UTF-8")), (byte) 0);
			return slab;
		}
		
		@Override
		protected Collection<SelfCreatingLink> makeComponent(NeXusFileInterface file, int[] dim, String path,
				String scannableName, String componentName, Object pos, String unit)
				throws NexusException {

			String name = enterLocation(file, path);

			stringlength = 127;

			byte[] slab = ArrayUtils.add(pos.toString().getBytes(Charset.forName("UTF-8")), (byte) 0);

			if (Arrays.equals(dim, new int[] { 1 })) {
				stringlength = slab.length;
			} else if (slab.length + 10 > stringlength) { // if strings vary more than that we are in trouble
				stringlength = slab.length + 10;
			}

			dim = minusonedimfordim(dim);

			if (dim[dim.length - 1] == 1) {
				dim[dim.length - 1] = stringlength;
			} else {
				dim = ArrayUtils.add(dim, stringlength);
			}
			rank = dim.length;

			file.makedata(name, NexusFile.NX_CHAR, rank, dim);
			file.opendata(name);
			file.putattr("local_name", String.format("%s.%s", scannableName, componentName).getBytes(),
					NexusFile.NX_CHAR);

			file.putslab(slab, nulldimfordim(dim), onedimfordim(dim));

			file.closedata();

			leaveLocation(file);
			return new Vector<SelfCreatingLink>();
		}
		
		@Override
		protected void writeComponent(NeXusFileInterface file, int[] start, String path, String scannableName,
				String componentName, Object pos) throws NexusException {
			String name = enterLocation(file, path);

			file.opendata(name);
			try {
				file.putslab(getComponentSlab(pos), start, onedimfordim(start));
			} catch (Exception e) {
				logger.error("error converting scannable data", e);
			}
			file.closedata();

			leaveLocation(file);
		}
	}

	protected int componentsFor(Scannable s) {
		int i = s.getInputNames() != null ? s.getInputNames().length : 0;
		int e = s.getExtraNames() != null ? s.getExtraNames().length : 0;
		return i + e;
	}

	protected String componentNameFor(Scannable s, int i) {
		return ArrayUtils.addAll(s.getInputNames() != null ? s.getInputNames() : new String[] {},
				s.getExtraNames() != null ? s.getExtraNames() : new String[] {})[i].toString();
	}

	protected ComponentWriter getComponentWriter(String componentName, Object object) {
		if (cwriter.containsKey(componentName))
			return cwriter.get(componentName);
		ComponentWriter cw = null;
		if (object instanceof String)
			cw = new StringComponentWriter();
		if (cw == null) // default
			cw = new ComponentWriter(); // Doubles
		cwriter.put(componentName, cw);
		return cw;
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
				ComponentWriter cw = getComponentWriter(componentName, componentObject);
				sclc.addAll(cw.makeComponent(file, dim, paths[i], s.getName(), componentName, componentObject, unit));
			} catch (Exception e) {
				logger.error("error converting scannable data", e);
			}
		}
		return sclc;
	}

	protected void resetComponentWriters() {
		cwriter = new HashMap<String, SingleScannableWriter.ComponentWriter>();
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