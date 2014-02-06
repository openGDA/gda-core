/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.editors;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;

public class EditorUtils {

	private static final Logger logger = LoggerFactory.getLogger(EditorUtils.class);

	public static void addSidePlotActivator(final IWorkbenchPart part,
			                                final AbstractPlotWindow     window,
			                                final String         partName) {


		// Add part listener in asyncExec so that does not get fired
		// while part being created. 
		part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				part.getSite().getPage().addPartListener((IPartListener2)new PartAdapter() {

					@Override
					public void partActivated(IWorkbenchPartReference partRef) {

						// activating the view stops the rogue toolbars appearing
						// these could also be avoided by moving the toolbars to
						// eclipse configured things.
						if (partRef.getPartName().equals(partName)) {
							try {
								// Select the respective side plots
								final IWorkbenchPage activePage = EclipseUtils.getActivePage();
								if (activePage!=null) {
									logger.error("The AbstractPlotWindow does not support side plots anymore");
//									final ISidePlotView side = window.getPlotUI().getSidePlotView();
//									if (side instanceof IWorkbenchPart) {
//										part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
//											@Override
//											public void run() {
//												IWorkbenchPart part = (IWorkbenchPart)side;
//												activePage.bringToTop(part);
//											}
//										});
//									}
								}
							} catch (Exception ignored) {
								// We do our best to activate and ignore exceptions.
							} 
						}

					}
				});
			}
		});

	}

}
