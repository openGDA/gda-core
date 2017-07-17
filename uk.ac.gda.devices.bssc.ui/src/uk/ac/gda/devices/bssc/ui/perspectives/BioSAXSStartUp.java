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

package uk.ac.gda.devices.bssc.ui.perspectives;

import gda.jython.JythonServerFacade;

import org.dawnsci.plotting.tools.profile.RadialProfileTool;
import org.dawnsci.plotting.views.ToolPageView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanEditor;

public class BioSAXSStartUp implements IStartup {
	
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSStartUp.class);
	
	@Override
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

				// Need add listener to workbench to always have at least one editor available
				IPartService service = (IPartService) window.getService(IPartService.class);
				service.addPartListener(new IPartListener() {
					@Override
					public void partActivated(IWorkbenchPart part) {
					}

					@Override
					public void partBroughtToTop(IWorkbenchPart part) {
					}
					
					@Override
					public void partClosed(IWorkbenchPart part) {
						IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
						IWorkbenchPage page = window.getActivePage();
						IPerspectiveDescriptor persp = page.getPerspective();
						if (persp != null && BioSAXSSetupPerspective.ID.equals(persp.getId())) {
							if (page.getEditorReferences().length == 0) {
								logger.debug("All editors closed");
								PlatformUI.getWorkbench().getDisplay().asyncExec(BioSAXSStartUp::openEditorWithDefaultSamples);
							}
						}
					}

					@Override
					public void partDeactivated(IWorkbenchPart part) {
					}

					@Override
					public void partOpened(IWorkbenchPart part) {
					}
				});

				window.addPerspectiveListener(new PerspectiveAdapter() {
					@Override
					public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
						String activePerspectiveID = perspective.getId();
						
						if (activePerspectiveID.equals(BioSAXSSetupPerspective.ID)) {
							if (page.getEditorReferences().length == 0) {
								logger.debug("Perspective opened with no editors present");
								openEditorWithDefaultSamples();
							}
						} else if (activePerspectiveID.equals(BioSAXSProgressPerspective.ID)) {
							try {
								page.showView("uk.ac.gda.client.ncd.saxsview");
								IViewReference radialProfileView = page.findViewReference("org.dawb.workbench.plotting.views.toolPageView.fixed", "org.dawb.workbench.plotting.tools.radialProfileTool");
								ToolPageView radialProfile = (ToolPageView)radialProfileView.getPart(true);
								((RadialProfileTool)radialProfile.getActiveTool()).getToolPlottingSystem().getAxes().get(1).setLog10(true);
								JythonServerFacade.getInstance().runCommand("import loadProfiles\nloadProfiles.load()");
							} catch (PartInitException e) {
								//worse things have happened
							} catch (ClassCastException cce) {
								// Non radial plot?
							}
						}
					}

					@Override
					public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
							IPerspectiveDescriptor newPerspective) {
					}

					@Override
					public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
					}
				});
			}});
	}

	private static void openEditorWithDefaultSamples() {
		logger.debug("Opening editor with default samples");
		BSSCSessionBeanEditor editor = new BSSCSessionBeanEditor();
		editor.openEditorWithDefaultSamples();
	}
}
