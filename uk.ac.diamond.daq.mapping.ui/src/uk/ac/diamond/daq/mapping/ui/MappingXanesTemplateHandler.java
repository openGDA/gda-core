/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView;

public class MappingXanesTemplateHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(MappingXanesTemplateHandler.class);
	private static final String[] FILE_FILTER_NAMES = new String[] { "Mapping Scan Files", "All Files (*.*)" };
	private static final String[] FILE_FILTER_EXTENSIONS = new String[] { "*.map", "*.*" };

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String templatePath = chooseTemplate();
		if (templatePath != null) { // null if 'Cancel' pressed
			loadTemplate(templatePath);
		}
		logger.debug("Selected mapping XANES template: '{}'", templatePath);
		return null;
	}

	private String chooseTemplate() {
		final FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
		dialog.setFilterNames(FILE_FILTER_NAMES);
		dialog.setFilterExtensions(FILE_FILTER_EXTENSIONS);
		final String visitConfigDir = getService(IFilePathService.class).getVisitConfigDir();
		dialog.setFilterPath(visitConfigDir);
		dialog.setOverwrite(true);

		return dialog.open();
	}

	private void loadTemplate(String templatePath) {
		final IMappingExperimentBean template;

		try {
			final byte[] bytes = Files.readAllBytes(Paths.get(templatePath));
			final String json = new String(bytes, "UTF-8");

			final IMarshallerService marshaller = getService(IMarshallerService.class);
			template = marshaller.unmarshal(json, MappingExperimentBean.class);
		} catch (Exception e) {
			logger.error("Error loading template", e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Mapping template",
					"Could not load map template. Map parameters have not been changed.");
			return;
		}

		final IMappingExperimentBeanProvider beanProvider = getService(IMappingExperimentBeanProvider.class);
		final IMappingExperimentBean currentBean = beanProvider.getMappingExperimentBean();
		template.getScanDefinition().setMappingScanRegion(currentBean.getScanDefinition().getMappingScanRegion());

		beanProvider.setMappingExperimentBean(template);

		final MappingExperimentView view = (MappingExperimentView) getService(EPartService.class).findPart(MappingExperimentView.ID).getObject();
		view.updateControls();
	}

	private <T> T getService(Class<T> serviceClass) {
		return PlatformUI.getWorkbench().getService(serviceClass);
	}
}
