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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;


public enum ExperimentBeanManager {
	/**
	 * The singleton instance of {@link ExperimentBeanManager}
	 */
	INSTANCE;

	public static final String EXTENSION_ID = "uk.ac.gda.richbeans.beantypes";
	public static final String ATT_NAME = "name";
	public static final String ATT_TYPE = "type";
	public static final String ATT_TEMPLATE = "template";
	public static final String ATT_CONTENT_TYPE = "contentType";
	public static final String ATT_COMMAND_ID = "commandId";
	public static final String ATT_BEAN = "bean";
	public static final String ATT_INCLUDE_IN_NEW = "includeInNew";

	List<IExperimentBeanDescription> descriptions;

	/**
	 * Retrieve a collection of all the Bean Descriptions known
	 * @return all Bean Descriptions as an unmodifiable collection
	 */
	@SuppressWarnings("unchecked")
	public List<IExperimentBeanDescription> getBeanDescriptions() {
		if (descriptions == null) {
			IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_ID).getExtensions();

			List<IExperimentBeanDescription> descList = new LinkedList<IExperimentBeanDescription>();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configElements = extension.getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					IConfigurationElement config = configElements[j];
					String name = config.getAttribute(ATT_NAME);
					String typeId = config.getAttribute(ATT_TYPE);
					String template = config.getAttribute(ATT_TEMPLATE);
					String contentTypeId = config.getAttribute(ATT_CONTENT_TYPE);
					String commandId = config.getAttribute(ATT_COMMAND_ID);
					String includeInNewString = config.getAttribute(ATT_INCLUDE_IN_NEW);
					
					
					Class<? extends IRichBean> beanClass = null;
					try {
						beanClass = (Class<? extends IRichBean>) config.createExecutableExtension(ATT_BEAN).getClass();
					} catch (CoreException e) {
						throw new RuntimeException("Cannot create required bean", e);
					}
					IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
					XMLCommandHandler handler = new XMLCommandHandler(template);
					boolean includeInNew = Boolean.parseBoolean(includeInNewString);
					
					IExperimentBeanDescription exafsBeanDescription = new ExperimentBeanDescription(name, typeId, commandId,
							handler, beanClass, contentType, includeInNew);
					
					descList.add(exafsBeanDescription);

				}
			}

			descriptions = descList;
		}
		return descriptions;
	}

	/**
	 * Retrieve the XMLCommandHandler for the given class if there is an {@link IExperimentBeanDescription}
	 * containing a reference to it.
	 * @param clazz the class to query
	 * @return command handler, or <code>null</code> id no matching class found
	 */
	public XMLCommandHandler getXmlCommandHandler(Class<?> clazz) {
		for (IExperimentBeanDescription desc : getBeanDescriptions()) {
			if (desc.getBeanClass().equals(clazz)) {
				return desc.getXmlCommandHander();
			}
		}
		return null;
	}
}
