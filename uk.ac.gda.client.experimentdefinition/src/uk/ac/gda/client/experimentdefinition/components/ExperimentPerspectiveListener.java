/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.components;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unlike views, Editors in Eclipse are not bound to perspectives. The {@link ExperimentPerspective} uses
 * editors to give the user control over scan parameters, but these editors make sense in that perspective
 * only.
 *
 * This listener therefore stores references to the open editors when switching perspectives against
 * the previous perspective. If the switch is to or from ExperimentPerspective, open editors are closed
 * and those cached against the new perspective (if any) are restored.
 */
public class ExperimentPerspectiveListener implements IStartup {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentPerspectiveListener.class);

	private Map<String, IEditorReference[]> editorsPerPerspective = new HashMap<>();
	private Map<String, IEditorPart> activeEditorPerPerspective = new HashMap<>();

	@Override
	public void earlyStartup() {
		IEventBroker brokerService = PlatformUI.getWorkbench().getService(IEventBroker.class);
		// subscribe to topic where perspective switches are broadcast
		brokerService.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT, this::handlePerspectiveChange);
	}

	private void handlePerspectiveChange(Event event) {
		Object changedElement = event.getProperty(UIEvents.EventTags.ELEMENT);

		// only interested in a perspective switch
		if (changedElement instanceof MPerspectiveStack) {

			final String previousPerspectiveId = getPreviousPerspectiveId(event);

			storeEditors(previousPerspectiveId);

			final String newPerspectiveId = getNewPerspectiveId(event);

			if (switchingToOrAwayFromExperimentPerspective(previousPerspectiveId, newPerspectiveId)) {
				prepareEditorsForPerspective(newPerspectiveId);
			}
		}
	}

	/**
	 * Extract from OSGi event the ID of perspective we are switching from
	 */
	private String getPreviousPerspectiveId(Event event) {
		return getPerspectiveId(event, UIEvents.EventTags.OLD_VALUE);
	}

	/**
	 * Extract from OSGi event the ID of perspective we are switching to
	 */
	private String getNewPerspectiveId(Event event) {
		return getPerspectiveId(event, UIEvents.EventTags.NEW_VALUE);
	}

	/**
	 * Extract from OSGi event the ID of perspective specified by the given property
	 * ({@code UIEvents.EventTags.OLD_VALUE} or {@code UIEvents.EventTags.NEW_VALUE})
	 */
	private String getPerspectiveId(Event event, String property) {
		MPerspective perspective = (MPerspective) event.getProperty(property);
		return perspective.getElementId();
	}

	/**
	 * Returns {@code true} if either previous or new perspective IDs equal {@code ExperimentPersperctive.ID}
	 */
	private boolean switchingToOrAwayFromExperimentPerspective(String previousPerspectiveId, String newPerspectiveId) {
		return newPerspectiveId.equals(ExperimentPerspective.ID)
				|| previousPerspectiveId.equals(ExperimentPerspective.ID);
	}

	/**
	 * Stores the editors open in the current page against the given perspective ID
	 */
	private void storeEditors(String perspectiveId) {
		editorsPerPerspective.put(perspectiveId, getOpenEditors());
		activeEditorPerPerspective.put(perspectiveId, getActivePage().getActiveEditor());
	}

	/**
	 * Closes the currently open perspectives and restores those cached against the given perspective ID.
	 * Only call this method if {@link #switchingToOrAwayFromExperimentPerspective(String, String)}.
	 */
	private void prepareEditorsForPerspective(String perspectiveId) {
		IWorkbenchPage page = getActivePage();
		page.closeAllEditors(true);
		if (editorsPerPerspective.containsKey(perspectiveId)) {
			for (IEditorReference editor : editorsPerPerspective.get(perspectiveId)) {
				try {
					// we don't specify the 'active' flag because even if true,
					// the next editor to be opened will be the active one...
					page.openEditor(editor.getEditorInput(), editor.getId());
				} catch (PartInitException e) {
					logger.error("Could not open editor '{}'", editor.getId(), e);
				}
			}
			IEditorPart activePart = activeEditorPerPerspective.get(perspectiveId);
			if (activePart == null) return;
			try {
				// ...instead we open the active editor again to make it active
				page.openEditor(activePart.getEditorInput(), activePart.getEditorSite().getId());
			} catch (PartInitException e) {
				logger.error("Could not set active editor '{}'", activePart.getEditorSite().getId(), e);
			}
		}
	}

	private IEditorReference[] getOpenEditors() {
		return getActivePage().getEditorReferences();
	}

	private IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

}
