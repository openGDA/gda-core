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

import org.eclipse.core.runtime.content.IContentType;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.richbeans.components.cell.IXMLFileListProvider;

public interface IExperimentBeanDescription extends IXMLFileListProvider {

	/**
	 * Get the bean class we are describing
	 * 
	 * @return the bean class, will not be <code>null</code>
	 */
	public Class<? extends IRichBean> getBeanClass();

	/**
	 * Return the command id (org.eclipse.ui.commands) that corresponds to this
	 * describer.
	 * 
	 * @return command id, or null if there is no corresponding command
	 */
	public String getCommandId();

	/**
	 * Get the content type for this bean.
	 * 
	 * @return content type, or <code>null</code> if bean is not registered with
	 *         Eclipse
	 */
	public IContentType getContentType();

	/**
	 * Should return which Exafs type of top level bean this is.
	 * 
	 * @return applicable type, must not return <code>null</code>
	 */
	public String getBeanType();

	/**
	 * Returns the user displayed name for this Bean.
	 * 
	 * @return name, will not be <code>null</code> or empty string
	 */
	public String getName();

	/**
	 * Return an instance of the {@link XMLCommandHandler} the corresponds to
	 * this describer.
	 * <p>
	 * The implementer should cache the result and return it each time.
	 * 
	 * @return XMLCommandHandler. Must not return <code>null</code>
	 */
	public XMLCommandHandler getXmlCommandHander();

	/**
	 * Returns whether the bean described by this description should be included
	 * in a newly created (default) scan
	 * 
	 * @return whether to include in a new scan
	 */
	public boolean includeInNew();

	public String toStringTitleCase();

}