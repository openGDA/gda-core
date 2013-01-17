/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.composites;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.TomoClientActivator;

public class TomoAlignmentToolbarComposite extends WorkbenchWindowControlContribution {

	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentToolbarComposite.class);
	public static final String RESET_DET_COMMAND = "uk.ac.gda.client.tomo.alignment.detector.reset";
	public static final String OPEN_PREF_COMMAND = "uk.ac.gda.client.tomo.alignment.pref.open";

	public TomoAlignmentToolbarComposite() {
	}

	public TomoAlignmentToolbarComposite(String id) {
		super(id);
	}

	@Override
	protected Control createControl(Composite parent) {
		Composite cmp = new Composite(parent, SWT.None);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		cmp.setLayout(layout);

		ToolBar toolbar = new ToolBar(cmp, SWT.FLAT | SWT.RIGHT);
		ToolItem openPrefToolItem = new ToolItem(toolbar, SWT.None);
		openPrefToolItem.setText("Open Preferences");
		openPrefToolItem.setImage(TomoClientActivator.getDefault().getImageRegistry()
				.get(ImageConstants.ICON_OPEN_PREF));
		final Object handlerServiceObj = PlatformUI.getWorkbench().getService(IHandlerService.class);

		openPrefToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (handlerServiceObj instanceof IHandlerService) {
					IHandlerService iHandlerService = (IHandlerService) handlerServiceObj;
					try {
						Event event = getEvent(e);
						iHandlerService.executeCommand(OPEN_PREF_COMMAND, event);
					} catch (ExecutionException e1) {
						logger.error("Command problem with execution", e1);
					} catch (NotDefinedException e1) {
						logger.error("Command not defined", e1);
					} catch (NotEnabledException e1) {
						logger.error("Command is not enabled", e1);
					} catch (NotHandledException e1) {
						logger.error("Command is not handled", e1);
					}
				}

			}
		});

		ToolItem resetDetectorToolItem = new ToolItem(toolbar, SWT.None);
		resetDetectorToolItem.setText("Reset Detector");
		resetDetectorToolItem.setImage(TomoClientActivator.getDefault().getImageRegistry()
				.get(ImageConstants.ICON_RESET_DETECTOR));

		resetDetectorToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (handlerServiceObj instanceof IHandlerService) {
					IHandlerService iHandlerService = (IHandlerService) handlerServiceObj;
					try {
						Event event = getEvent(e);
						iHandlerService.executeCommand(RESET_DET_COMMAND, event);
					} catch (ExecutionException e1) {
						logger.error("Command problem with execution", e1);
					} catch (NotDefinedException e1) {
						logger.error("Command not defined", e1);
					} catch (NotEnabledException e1) {
						logger.error("Command is not enabled", e1);
					} catch (NotHandledException e1) {
						logger.error("Command is not handled", e1);
					}
				}

			}
		});
		toolbar.setLayoutData(new GridData(GridData.FILL_BOTH));
		return cmp;
	}

	private Event getEvent(SelectionEvent e) {
		Event event = new Event();
		event.widget = e.widget;
		event.time = e.time;
		event.display = e.display;
		event.text = e.text;
		event.item = e.item;
		return event;
	}

}
