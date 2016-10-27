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

import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanEditor;

public class BioSAXSStartUp implements IStartup {

	private static int runcount = 0;
	private IWorkbenchWindow window;
	private IWorkbenchPage page;
	private String activePerspectiveID = BioSAXSSetupPerspective.ID;
	
	@Override
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				page = window.getActivePage();
				activePerspectiveID = page.getPerspective().getId();

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
						if (activePerspectiveID.equals(BioSAXSSetupPerspective.ID)) {
							if (page.getEditorReferences().length == 0) {
								openEditorWithDefaultSamples();
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
						activePerspectiveID = perspective.getId();
						
						if (activePerspectiveID.equals(BioSAXSSetupPerspective.ID)) {
							if (page.getEditorReferences().length == 0) {
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

	private void openEditorWithDefaultSamples() {
		BSSCSessionBeanEditor editor = new BSSCSessionBeanEditor();
		editor.openEditorWithDefaultSamples();
	}
}
