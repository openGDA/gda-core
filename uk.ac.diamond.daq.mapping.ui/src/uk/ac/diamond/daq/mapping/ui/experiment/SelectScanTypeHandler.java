/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static java.util.Arrays.asList;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler to show a dialogue that allows the user to change the type of scan submitted to GDA
 * <p>
 * It will only be active if the {@link MappingExperimentView}, defined by the {@link MappingViewConfiguration} bean,
 * contains a {@link SubmitScanSelector} containing more than one choice of {@link SubmitScanSection} (or subclass
 * thereof).
 */
public class SelectScanTypeHandler extends AbstractHandler implements IHandler {
	private static final Logger logger = LoggerFactory.getLogger(SelectScanTypeHandler.class);

	private static final String MAPPING_EXPERIMENT_VIEW_ID = "uk.ac.diamond.daq.mapping.ui.experiment.mappingExperimentView";

	private boolean initialised = false;
	private boolean multipleScanSections = false;
	private SubmitScanSelector submitScanSelector;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<String> descriptions = submitScanSelector.getDescriptions();
		final int currentSelection = submitScanSelector.getCurrentSectionIndex();

		final ListDialog dialog = new ListDialog(Display.getDefault().getActiveShell());
		dialog.setTitle("Choose the scan type");
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setLabelProvider(new LabelProvider());
		dialog.setInput(submitScanSelector.getDescriptions());
		// Select the name of the section that is currently visible
		dialog.setInitialElementSelections(asList(descriptions.get(currentSelection)));

		if (dialog.open() == Window.OK) {
			final Object[] result = dialog.getResult();
			submitScanSelector.showSection(descriptions.indexOf(result[0]));
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		initialise();
		return multipleScanSections;
	}

	/**
	 * Initialise the handler (primarily to determine whether there is a {@link SubmitScanSelector}), if it is not yet
	 * successfully initialised.
	 * <p>
	 * As this function is first called early on in the start-up of the workbench, the first few calls may not succeed,
	 * and the workbench window and/or mapping view may not have been created.
	 */
	private void initialise() {
		if (!initialised) {
			final MPart mappingViewPart = getViewPart(MAPPING_EXPERIMENT_VIEW_ID);
			if (mappingViewPart != null && mappingViewPart.getObject() != null) {
				final MappingExperimentView mappingExperimentView = (MappingExperimentView) mappingViewPart.getObject();
				submitScanSelector = mappingExperimentView.getSection(SubmitScanSelector.class);
				if (submitScanSelector != null && submitScanSelector.getNumberOfSections() > 1) {
					multipleScanSections = true;
				}
				initialised = true;
				logger.info("Scan Type button enable state initialised");
			}
		}
	}

	private MPart getViewPart(String viewId) {
		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (workbenchWindow == null) {
			return null;
		}
		final EPartService partService = workbenchWindow.getService(EPartService.class);
		if (partService == null) {
			return null;
		}
		return partService.findPart(viewId);
	}
}
