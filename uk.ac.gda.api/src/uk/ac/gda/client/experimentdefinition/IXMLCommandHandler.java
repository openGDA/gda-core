/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import uk.ac.gda.util.beans.xml.XMLRichBean;

public interface IXMLCommandHandler {

	Object doCopyAndSelection();

	IFile doCopy(final IFile to);

	/**
	 * @param runFolder
	 * @return name copied.
	 */
	IFile doCopy(final IFolder runFolder);

	IFile doTemplateCopy(final IFolder runFolder, String fromFileName);

	File getTemplatePath();

	/**
	 * @return Returns the templateFileName.
	 */
	String getTemplateFileName();

	/**
	 * @return bean
	 * @throws Exception
	 */
	XMLRichBean getTemplateParameters() throws Exception;
}
