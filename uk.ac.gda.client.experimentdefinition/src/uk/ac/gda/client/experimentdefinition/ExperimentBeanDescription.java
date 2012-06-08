/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.common.rcp.util.ISortingUtils;
import uk.ac.gda.richbeans.xml.XMLBeanContentDescriberFactory;

/**
 * Immutable class representing an Exafs Specialisation of a Bean Description
 */
public class ExperimentBeanDescription implements IExperimentBeanDescription {

	private String type;
	private String commandId;
	private XMLCommandHandler handler;
	private String name;
	private Class<? extends IRichBean> beanClass;
	private IContentType contentType;
	private boolean includeInNew;

	public ExperimentBeanDescription(String name, String type, String commandId, XMLCommandHandler handler,
			Class<? extends IRichBean> beanClass, IContentType contentType, boolean includeInNew) {
		if (name == null || beanClass == null) {
			throw new NullPointerException();
		}
		if ("".equals(name)) {
			throw new IllegalArgumentException("name cannot be empty string");
		}
		this.name = name;
		this.type = type;
		this.commandId = commandId;
		this.handler = handler;
		this.beanClass = beanClass;
		this.contentType = contentType;
		this.includeInNew = includeInNew;
	}

	@Override
	public String toString() {
		return this.name + " :" + this.type + ":" + this.commandId + ":" + this.beanClass;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<? extends IRichBean> getBeanClass() {
		return beanClass;
	}

	@Override
	public boolean includeInNew() {
		return includeInNew;
	}

	@Override
	public IContentType getContentType() {
		return contentType;
	}

	@Override
	public String getBeanType() {
		return type;
	}

	@Override
	public String getCommandId() {
		return commandId;
	}

	@Override
	public XMLCommandHandler getXmlCommandHander() {
		return handler;
	}

	/**
	 * Formats the toString output in Title Case
	 * 
	 * @return title case of the enum name
	 */
	@Override
	public String toStringTitleCase() {
		StringBuilder stringBuilder = new StringBuilder(toString().toLowerCase());
		stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
		return stringBuilder.toString();
	}

	@Override
	public List<IFile> getSortedFileList(IFolder dir) {

		List<IFile> filesList;
		try {
			filesList = ISortingUtils.getSortedFileList(dir, ".xml");
		} catch (CoreException e) {
			filesList = new LinkedList<IFile>();
		}

		List<String> editorTypes = new ArrayList<String>();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"org.eclipse.ui.editors");
		for (IConfigurationElement editorConfig : config) {
			IConfigurationElement[] children = editorConfig.getChildren("contentTypeBinding");
			for (IConfigurationElement child : children) {
				String contentTypeName = child.getAttribute("contentTypeId");
				if (contentTypeName.equals(contentType.getId())) {
					editorTypes.add(editorConfig.getAttribute("class"));
				}
			}
		}

		List<IFile> filesToShow = new ArrayList<IFile>();
		for (IFile file : filesList) {
			String editorID = XMLBeanContentDescriberFactory.getInstance().getId(file);
			if (editorTypes.contains(editorID)) {
				filesToShow.add(file);
			}
		}
		return filesToShow;
	}

	private Collection<IExperimentBeanDescription> exafsDescriptions;

	/**
	 * Retrieve all the Bean Descriptions provided by {@link ExperimentBeanManager#getBeanDescriptions()} filtered
	 * by receivers bean type
	 * 
	 * @return unmodifiable collection of matching descriptions
	 */
	public Collection<IExperimentBeanDescription> getBeanDescriptions() {
		if (exafsDescriptions == null) {
			Set<IExperimentBeanDescription> set = new HashSet<IExperimentBeanDescription>();
			for (IExperimentBeanDescription desc : ExperimentBeanManager.INSTANCE.getBeanDescriptions()) {
				if (desc.getBeanType().equals(this.type)) {
					set.add(desc);
				}
			}
			exafsDescriptions = Collections.unmodifiableCollection(set);
		}
		return exafsDescriptions;
	}

	private Collection<IContentType> conetentTypes;

	/**
	 * Retrieve all content types provided by {@link #getBeanDescriptions()}
	 * 
	 * @return unmodifiable collection of content types
	 */
	public Collection<IContentType> getContentTypes() {
		if (conetentTypes == null) {
			Set<IContentType> set = new HashSet<IContentType>();
			for (IExperimentBeanDescription desc : getBeanDescriptions()) {
				set.add(desc.getContentType());
			}
			conetentTypes = Collections.unmodifiableCollection(set);
		}
		return conetentTypes;
	}

}
