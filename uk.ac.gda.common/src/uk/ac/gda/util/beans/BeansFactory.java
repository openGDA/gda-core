/*-
 *******************************************************************************
 * Copyright (c) 2011, 2014 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/

package uk.ac.gda.util.beans;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;

/**
 * Has a series of utilities for interacting with the beans and the
 * xml files in which they are persisted.
 * <p>
 * The list of classes is either populated through a Spring instantiated instance of this class (server-side) or via the
 * uk.ac.common.beans.factory extension point (RCP client-side).
 */
public class BeansFactory {

	private static Class<? extends Object>[] CLASSES;

	/**
	 * Check that CLASSES is init and throw an exception if it hasn't been yet. Using CLASSES in this file would
	 * generally through a {@link NullPointerException} anyway, this is just a more informative check that is less
	 * likely to be caught by over aggressive try/catchs that exist in this file.
	 *
	 * @throws NullPointerException
	 *             thrown if CLASSES has not been initialised
	 */
	private static void checkInit() throws NullPointerException {
		if (CLASSES == null)
			throw new NullPointerException(
					"BeansFactory.CLASSES is null, therefore BeansFactory has not been initialized properly");
	}



	/**
	 * Can inject classes from spring or with static method. The classes are needed to define which XML files have GDA
	 * bean files with them.
	 */
	public BeansFactory() {
	}

	/**
	 * Fast way to determine if XML file is a bean in the system which can be read by castor and control things on the
	 * server.
	 *
	 * @param beanFile
	 * @return true if bean file.
	 * @throws Exception
	 */
	public static boolean isBean(File beanFile) throws Exception {
		checkInit();
		for (int i = 0; i < CLASSES.length; i++) {
			if (BeansFactory.isBean(beanFile, CLASSES[i]))
				return true;
		}
		return false;
	}



	/**
	 * Find the first filename of an xml file persisting a bean of the given class
	 *
	 * @param folder
	 * @param clazz
	 * @return String - filename
	 * @throws Exception
	 */
	public static String getFirstFileName(File folder, Class<? extends Object> clazz) throws Exception {
		final File[] fa = folder.listFiles();
		for (int i = 0; i < fa.length; i++) {
			if (BeansFactory.isBean(fa[i], clazz)) {
				return fa[i].getName();
			}
		}
		return null;
	}

	/**
	 * Checks if the XML file is a Castor persistence file of a bean in the given list
	 *
	 * @param beanFile
	 * @return true if file is a saved version of this bean
	 * @throws Exception
	 */
	public static boolean isBean(final File beanFile, final Class<? extends Object> beanClass) throws Exception {
		final BufferedReader reader = new BufferedReader(new FileReader(beanFile));
		return isBean(reader, beanClass);
	}

	public static boolean isBean(final InputStream bean, final Class<? extends Object> beanClass) throws Exception {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(bean, "UTF-8"));
		return isBean(reader, beanClass);
	}

	private static boolean isBean(BufferedReader reader, Class<? extends Object> beanClass) throws Exception {
		try {
			@SuppressWarnings("unused")
			final String titleLine = reader.readLine(); // unused.
			final String tagLine = reader.readLine();
			if (tagLine == null)
				return false;
			final String tagName = beanClass.getName().substring(beanClass.getName().lastIndexOf(".") + 1);
			if (tagName == null)
				return false;
			if (tagLine.trim().equalsIgnoreCase("<" + tagName + ">")
					|| tagLine.trim().equalsIgnoreCase("<" + tagName + "/>")) {
				return true;
			}
			return false;
		} finally {
			reader.close();
		}
	}


	public static Class<? extends Object>[] getClasses() {
		checkInit();

		return CLASSES;
	}

	public static void setClasses(Class<? extends Object>[] cLASSES) {
		if (cLASSES == null)
			throw new NullPointerException("cLASSES must be-non null to initialzied BeansFactory");
		CLASSES = cLASSES;
	}

	/**
	 * Can be used to test if a given class is a class defined by the extension point.
	 *
	 * @param clazz
	 * @return true if class.
	 */
	public static boolean isClass(Class<? extends Object> clazz) {
		checkInit();

		for (int i = 0; i < CLASSES.length; i++) {
			if (CLASSES[i].equals(clazz))
				return true;
		}
		return false;
	}

	/**
	 * Deep copy using serialization. All objects in the graph must serialize to use this method or an exception will be
	 * thrown.
	 *
	 * @param fromBean
	 * @return deeply cloned bean
	 */
	public static <T> T deepClone(final T fromBean) throws Exception {
		return deepClone(fromBean, fromBean.getClass().getClassLoader());
	}

	/**
	 * Creates a clone of any serializable object. Collections and arrays may be cloned if the entries are serializable.
	 * Caution super class members are not cloned if a super class is not serializable.
	 */
	private static <T> T deepClone(T toClone, final ClassLoader classLoader) throws Exception {
		if (null == toClone)
			return null;

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		ObjectOutputStream oOut = new ObjectOutputStream(bOut);
		oOut.writeObject(toClone);
		oOut.close();
		ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		bOut.close();
		ObjectInputStream oIn = new ObjectInputStream(bIn) {
			/**
			 * What we are saying with this is that either the class loader or any of the beans added using extension
			 * points classloaders should be able to find the class.
			 */
			@Override
			protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
				try {
					return Class.forName(desc.getName(), false, classLoader);
				} catch (Exception ne) {
					for (int i = 0; i < CLASSES.length; i++) {
						try {
							return CLASSES[i].getClassLoader().loadClass(desc.getName());
						} catch (Exception e) {
							continue;
						}
					}
				}
				return null;
			}
		};
		bIn.close();
		// the whole idea is to create a clone, therefore the readObject must
		// be the same type in the toClone, hence of T
		@SuppressWarnings("unchecked")
		T copy = (T) oIn.readObject();
		oIn.close();

		return copy;
	}



	/**
	 * Creates a new list of cloned beans (deep).
	 *
	 * @param beans
	 * @return list of cloned beans.
	 * @throws Exception
	 */
	public static List<?> cloneBeans(final List<?> beans) throws Exception {
		final List<Object> ret = new ArrayList<Object>(beans.size());
		for (Object bean : beans)
			ret.add(BeansFactory.deepClone(bean));
		return ret;
	}

}
