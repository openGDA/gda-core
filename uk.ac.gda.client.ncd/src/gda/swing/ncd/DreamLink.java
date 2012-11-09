/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.swing.ncd;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides access to classes from the (external) Dream package
 * <p>
 * We only want to use its file reading.
 * <p>
 * The full extend of Dream's ugliness should be hidden from the rest of the innocent NCD files with the help of this
 * wrapper class.
 * <p>
 * We use reflection so that dream is not required for the build and also absolutely optional for operation. With any
 * luck this code is already broken due to incompatible changes made to Dream recently.
 */

public class DreamLink {
	private static final Logger logger = LoggerFactory.getLogger(DreamLink.class);

	private boolean configured = false;

	@SuppressWarnings("rawtypes")
	private Class headerfileClass, filenameClass;

	/**
	 * As Dream is an external package, its jar may not be available This method checks if we can use Dream.
	 * <p>
	 * You are required to call this method before using anything else in here. Otherwise you will see
	 * RuntimeExceptions. This should encourage you to write code that has the Dream access as an option only.
	 * 
	 * @return Dream status
	 */
	public boolean isAvailable() {
		if (configured) {
			return true;
		}
		configure();
		return configured;
	}

	private void configure() {

		if (configured) {
			return;
		}

		try {
			headerfileClass = Class.forName("ncd.dream.data.file.definition.NcdDataFile");
			filenameClass = Class.forName("ncd.dream.utilities.file.Filename");
		} catch (Exception e) {
			return;
		}

		logger.debug("Dream seems to be found");

		configured = true;
	}

	/**
	 * @param filename
	 * @return double[][]
	 * @throws IOException
	 */
	public double[][] getWaxsFromFile(String filename) throws IOException {
		return getSomethingFromFile("Waxs", filename);
	}

	/**
	 * @param filename
	 * @return double[][]
	 * @throws IOException
	 */
	public double[][] getSaxsFromFile(String filename) throws IOException {
		return getSomethingFromFile("Saxs", filename);
	}

	@SuppressWarnings("unchecked")
	private double[][] getSomethingFromFile(String type, String filename) throws IOException {
		// This is the reflected Voodoo dance we are trying to perform:
		// HeaderFile hf = new HeaderFile(new FileName(filename));
		// hf.loadData();
		// double[][] de = hf.getSaxsFile().getDataError().getY().getY();

		if (!configured) {
			throw new RuntimeException();
		}

		Constructor<?> cons;
		Method m;

		try {
			cons = filenameClass.getConstructor(String.class);
			Object fn = cons.newInstance(filename);
			cons = headerfileClass.getConstructor(filenameClass);
			Object hf = cons.newInstance(filenameClass.cast(fn));
			// m = headerfileClass.getMethod("loadData");
			// m.invoke(hf);
			m = headerfileClass.getMethod("get" + type + "File");
			Object df = m.invoke(hf);
			m = df.getClass().getMethod("loadData");
			m.invoke(df);
			m = df.getClass().getMethod("getDataError");
			Object de = m.invoke(df);
			m = de.getClass().getMethod("getY");
			Object dxyf = m.invoke(de);
			m = dxyf.getClass().getMethod("getY");
			return (double[][]) m.invoke(dxyf);
		} catch (NoSuchMethodException e) {
			// getConstructor( ) couldn't find the constructor we described
			logger.warn("no such method", e);
			throw new IOException("problem reading data in or with Dream", e);

		} catch (InstantiationException e) {
			// the class is abstract
			logger.warn("abtract class", e);
			throw new IOException("problem reading data in or with Dream", e);

		} catch (IllegalAccessException e) {
			// we don't have permission to create an instance
			logger.warn("illegal access", e);
			throw new IOException("problem reading data in or with Dream", e);

		} catch (InvocationTargetException e) {
			// the construct threw an exception
			logger.warn("invocation target", e);
			throw new IOException("problem reading data in or with Dream", e);

		} catch (Exception e) {
			logger.warn("error in dream", e);
			throw new IOException("problem reading data in or with Dream", e);
		}
		
		// we should never reach this
		// throw new IOException("problem reading data in or with Dream");
	}
}
