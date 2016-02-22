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

package uk.ac.gda.client.experimentdefinition.ui.handlers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentBeanDescription;
import uk.ac.gda.client.experimentdefinition.IXMLCommandHandler;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;
import uk.ac.gda.util.io.FileUtils;

public class XMLCommandHandler extends AbstractExperimentCommandHandler implements IXMLCommandHandler {

	private final static Logger logger = LoggerFactory.getLogger(XMLCommandHandler.class);

	public XMLCommandHandler() {
		// Does nothing but allow the extension point to call the class.
	}

	public XMLCommandHandler(final String templateFileName) {
		this.templateFileName = templateFileName;
	}

	private String templateFileName;

	@Override
	public void run(IAction action) {
		doCopyAndSelection(action.getActionDefinitionId());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doCopyAndSelection(event.getCommand().getId());
	}

	protected Object doCopyAndSelection(final String id) {
		final IFolder runFolder = getEditorManager().getSelectedFolder();
		Collection<IExperimentBeanDescription> exafsBeanDescriptions = ExperimentBeanManager.INSTANCE
				.getBeanDescriptions();
		for (IExperimentBeanDescription exafsBeanDescription : exafsBeanDescriptions) {
			if (id.equals(exafsBeanDescription.getCommandId())) {
				XMLCommandHandler xmlCommandHandler = exafsBeanDescription.getXmlCommandHander();
				final IFile to = xmlCommandHandler.doCopy(runFolder);
				doSelection(to);
				return to.getName();
			}
		}
		// This is a fatal configuration error
		throw new RuntimeException("Command ID '" + id + "' not found in ExafsBeanManager");
	}

	@Override
	public Object doCopyAndSelection() {

		final IFolder runFolder = getEditorManager().getSelectedFolder();
		final IFile to = doCopy(runFolder);
		doSelection(to);
		return to.getName();
	}

	@Override
	public IFile doCopy(final IFile to) {
		if (to.exists())
			return to;
		return doCopy(to.getParent(), to);
	}

	/**
	 * @param runFolder
	 * @return name copied.
	 */
	@Override
	public IFile doCopy(final IFolder runFolder) {
		final File to = FileUtils.getUnique(runFolder.getLocation().toFile(), templateFileName, "xml");
		return doCopy(runFolder, runFolder.getFile(to.getName()));
	}

	/**
	 * @param runFolder
	 * @param to
	 * @return name copied.
	 */
	private IFile doCopy(final IContainer runFolder, final IFile to, final File finalTemplate) {

		try {
			try {
				FileUtils.copy(finalTemplate, to.getLocation().toFile());
				to.refreshLocal(IResource.DEPTH_ZERO, null);
			} catch (Exception e) {
				throw new InvocationTargetException(e);
			}

		} catch (Exception e) {
			logger.error("Cannot copy file to '" + to.getLocation() + "'", e);
			return null;
		}

		try {
			ExperimentFactory.fireRunObjectListeners(runFolder);
		} catch (Exception e) {
			logger.error("Cannot notify all run object listeners to '" + to.getLocation() + "'", e);
		}

		return to;
	}

	/**
	 * @param runFolder
	 * @param to
	 * @return name copied.
	 */
	private IFile doCopy(final IContainer runFolder, final IFile to) {

		File template = getTemplatePath();
		if (template == null || !template.exists()) {
			final URL templateUrl = getClass().getResource("/templates/" + templateFileName + ".xml");
			template = new File(EclipseUtils.getAbsoluteUrl(templateUrl).getFile());
		}
		if (!template.exists()) {
			logger.error("Template: '" + template + "' does not exist and cannot be copied to the file system.");
			return null;
		}
		return doCopy(runFolder, to, template);
	}

	@Override
	public IFile doTemplateCopy(final IFolder runFolder, String fromFileName) {
		File template = new File(ExperimentFactory.getTemplatesFolderPath() + fromFileName);
		if (!template.exists()) {
			logger.error("Template: '" + template + "' does not exist and cannot be copied to the file system.");
			return null;
		}
		final File to = FileUtils.getUnique(runFolder.getLocation().toFile(),
				fromFileName.substring(0, fromFileName.indexOf(".xml")), "xml");
		return doCopy(runFolder, runFolder.getFile(to.getName()), template);

	}

	private void doSelection(IFile to) {
		final ExperimentFolderEditor ed = getEditorManager().getActiveFolderEditor();
		if (ed != null)
			ed.setSelected(to);

		getEditorManager().openEditor(to, false);

		if (ed != null) {
			EclipseUtils.getActivePage().activate(ed);
			ed.editSelectedElement();
		}

	}

//	public String getTemplateDirPath() {
////		change this to be configurable, and keep config dir to be the default
////
////		and merge this method and the one below!!!
////		return LocalProperties.getConfigDir() + File.separator + "templates" + File.separator;
//
//
//
//	}

	@Override
	public File getTemplatePath() {
		return new File(ExperimentFactory.getTemplatesFolderPath() + templateFileName + ".xml");
	}

	/**
	 * @return Returns the templateFileName.
	 */
	@Override
	public String getTemplateFileName() {
		return templateFileName + ".xml";
	}

	/**
	 * @return bean
	 * @throws Exception
	 */
	@Override
	public XMLRichBean getTemplateParameters() throws Exception {
		return XMLHelpers.getBean(getTemplatePath());
	}

}
