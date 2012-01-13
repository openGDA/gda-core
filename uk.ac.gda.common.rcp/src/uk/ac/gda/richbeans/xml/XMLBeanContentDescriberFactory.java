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

package uk.ac.gda.richbeans.xml;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.ui.IStorageEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.IStorageUtils;

/**
 * @author fcp94556
 *
 */
public class XMLBeanContentDescriberFactory {
	private static final Logger logger = LoggerFactory.getLogger(XMLBeanContentDescriberFactory.class);

	private static XMLBeanContentDescriberFactory factory = new XMLBeanContentDescriberFactory();
	
	private Collection<XMLBeanContentDescriber> contentList;
	private XMLBeanContentDescriberFactory() { 
		this.contentList = Collections.newSetFromMap(new ConcurrentHashMap<XMLBeanContentDescriber,Boolean>());
	}
	
	/**
	 * @return XMLBeanContentDescriberFactory
	 */
	public static XMLBeanContentDescriberFactory getInstance() {
		return factory;
	}
	
	/**
	 * @param beanContentDescriber
	 */
	public void addDescriber(final XMLBeanContentDescriber beanContentDescriber) {
		try {
			contentList.add(beanContentDescriber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Map<String,String> extensionMap;
	/**
	 * Called to register editors by file extension if they are not
	 * XMLBean files.
	 * 
	 * @param extension
	 * @param id
	 */
	public void addFileExtension(final String extension, final String id) {
		// Quite a heavy weight class to be using here. Intentionally chosen, this method 
		// must be thread safe.
		if (extensionMap==null) extensionMap = new ConcurrentHashMap<String,String>(3);
		extensionMap.put(extension, id);
	}
	
	/**
	 * Thread Safe, the collections accessed here are concurrent implementations.
	 * Required to get the system to work without errors.
	 * 
	 * @param file
	 * @return String
	 */
	public String getId(final IFile file) {
		
		if (extensionMap!=null&&file!=null) {
			for (String extension : extensionMap.keySet()) {
				if (file.getName().endsWith(extension)) return extensionMap.get(extension);
			}
		}
		
		// Look at the xml tag
		for (XMLBeanContentDescriber beanDes : contentList) {
			try {
				InputStream contents = IStorageUtils.getContents(file);
				if (beanDes.describe(contents, null) == IContentDescriber.VALID) {
					return beanDes.getEditorId();
				}
			} catch (Exception e) {
				logger.error("Unexpected exception attempting to obtain Id", e);
			}
		}
		return ""; // Null causes fatal exception
	}
	
	/**
	 * Reads the editor input a determines the editor id.
	 * @param file
	 * @return the id or "" if it does not exist.
	 */
	public String getId(IStorageEditorInput file) {
		// Look at the xml tag
		for (XMLBeanContentDescriber beanDes : contentList) {
			try {
				InputStream contents = IStorageUtils.getContents(file.getStorage(), null);
				if (beanDes.describe(contents, null)==IContentDescriber.VALID) {
					return beanDes.getEditorId();
				}
			} catch (Exception e) {
				logger.error("Unexpected exception attempting to obtain Id", e);

			}
		}
		return ""; // Null causes fatal exception
	}

	
	/**
	 * True if the id is one of these describers.
	 * @param id
	 * @return true if id
	 */
	public boolean isId(final String id) {
		if (id==null) return false;
		for (XMLBeanContentDescriber d : contentList) {
			if (id.equals(d.getEditorId())) return true;
		}
		return false;
	}

}

	