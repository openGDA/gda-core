/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
import java.util.LinkedHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores refs to the open editors. Make sure that the editors are closed when the perspective is swictched and restores
 * them when the user returns to the experiment perspective.
 */
public class ExperimentPerspectiveListener implements IPerspectiveListener4, IStartup {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentPerspectiveListener.class);

	private static HashMap<IEditorReference, IEditorInput> storedEditorRefs;
	private static boolean ignoreEditorEvents = false;

	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		if (!ignoreEditorEvents) {
			if (perspective.getId().equals(ExperimentPerspective.ID)) {
				if (changeId.equalsIgnoreCase("editorOpen") || changeId.equalsIgnoreCase("editorClose")) {
					storeMapOfEditors(page);
				}
			}
		}
	}

	protected void storeMapOfEditors(IWorkbenchPage page) {
		storedEditorRefs = new LinkedHashMap<IEditorReference, IEditorInput>();
		for (IEditorReference thisRef : page.getEditorReferences()) {
			try {
				storedEditorRefs.put(thisRef, thisRef.getEditorInput());
			} catch (PartInitException e) {
				// ignore as we are not doing any work here, simply recording what is open
			}
		}
	}

	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

		if (perspective.getId().equals(ExperimentPerspective.ID)) {
			try {
				ignoreEditorEvents = true;

				if (storedEditorRefs == null) {
					page.closeAllEditors(true);
				} else {
					page.saveAllEditors(true);
					openStoredEditors(page);
				}

			} finally {
				ignoreEditorEvents = false;
			}
		}
	}

	protected void openStoredEditors(IWorkbenchPage page) {
		IEditorReference[] liveRefs = page.getEditorReferences();
		for (IEditorReference thisRef : liveRefs) {
			if (!storedEditorRefs.keySet().contains(thisRef)) {
				page.closeEditor(thisRef.getEditor(false), false);
			}
		}
		for (IEditorReference thisRef : storedEditorRefs.keySet()) {
			if (!ArrayUtils.contains(liveRefs, thisRef)) {
				try {
					page.openEditor(storedEditorRefs.get(thisRef), thisRef.getId());
				} catch (PartInitException e) {
					// fail silently - user can correct easily by clicking on the desired scan in Experiment Explorer
					logger.warn("Exception trying to open stored editor", e);
				}
			}
		}
	}

	@Override
	public void perspectiveClosed(IWorkbenchPage arg0, IPerspectiveDescriptor arg1) {
	}

	@Override
	public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		// too late: editors would be closed by this point
	}

	@Override
	public void perspectiveOpened(IWorkbenchPage arg0, IPerspectiveDescriptor arg1) {
	}

	@Override
	public void perspectiveSavedAs(IWorkbenchPage arg0, IPerspectiveDescriptor arg1, IPerspectiveDescriptor arg2) {
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage arg0, IPerspectiveDescriptor arg1, IWorkbenchPartReference arg2,
			String arg3) {
	}

	@Override
	public void perspectivePreDeactivate(IWorkbenchPage arg0, IPerspectiveDescriptor arg1) {
		if (arg1.getId().equals(ExperimentPerspective.ID)) {
			storeMapOfEditors(arg0);
			ignoreEditorEvents = true;
			arg0.closeAllEditors(true);
			ignoreEditorEvents = false;
		}
	}

	@Override
	public void earlyStartup() {
		// Make sure we always have a listener for the perspective.
		// Cannot add to the perspective class as its not always called when Workbench is started.
		PlatformUI.getWorkbench().getWorkbenchWindows()[0].addPerspectiveListener(this);
	}

}
