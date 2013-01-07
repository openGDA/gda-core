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

import gda.util.OSCommandRunner;
import gda.util.OSCommandRunner.LOGOPTION;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.TomoClientActivator;

public abstract class BaseToolbarComposite extends WorkbenchWindowControlContribution {

	private static final String RDESKTOP_COMMAND = "rdesktop 172.23.112.208 -u i12detector -p !i12d3t3ct0r# -d DIAMOND -g 1600x1100 -a 16";
	private static final String IOC_DESKTOP = "IOC Desktop";
	private static final String DETECTOR_IOC_STATUS = "Detector IOC status";
	private static final String RESET_DETECTOR = "Reset Detector";
	private static final String OPEN_PREFERENCES = "Open Preferences";
	private static final Logger logger = LoggerFactory.getLogger(BaseToolbarComposite.class);
	public static final String RESET_DET_COMMAND = "uk.ac.gda.client.tomo.alignment.detector.reset";
	public static final String OPEN_PREF_COMMAND = "uk.ac.gda.client.tomo.alignment.pref.open";

	public BaseToolbarComposite() {
		this(null);
	}

	public BaseToolbarComposite(String id) {
		super(id);
	}

	protected abstract String getIocRunningContext();

	@Override
	protected Control createControl(Composite parent) {
		Composite cmp = new Composite(parent, SWT.None);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		cmp.setLayout(layout);

		btnOpenRemoteDesktop = new Button(cmp, SWT.None);
		btnOpenRemoteDesktop.setLayoutData(new GridData());
		btnOpenRemoteDesktop.setText(IOC_DESKTOP);
		btnOpenRemoteDesktop.setImage(TomoClientActivator.getDefault().getImageRegistry()
				.get(ImageConstants.ICON_REMOTE_DESKTOP));
		btnOpenRemoteDesktop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OSCommandRunner.runNoWait(RDESKTOP_COMMAND, LOGOPTION.ONLY_ON_ERROR, null);
			}
		});

		Composite iocStatusComposite = new Composite(cmp, SWT.None);
		iocStatusComposite.setLayoutData(new GridData());
		iocStatusComposite.setLayout(new GridLayout(2, false));

		Composite borderComposite = new Composite(iocStatusComposite, SWT.None);
		borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginWidth = 2;
		fillLayout.marginHeight = 2;
		borderComposite.setLayout(fillLayout);

		GridData fillHorizontalGD = new GridData();
		fillHorizontalGD.horizontalIndent = 3;
		fillHorizontalGD.widthHint = 15;
		fillHorizontalGD.heightHint = 15;
		borderComposite.setLayoutData(fillHorizontalGD);
		iocState = new Composite(borderComposite, SWT.None);
		iocState.setBackground(ColorConstants.red);

		Label lblTest = new Label(iocStatusComposite, SWT.None);
		lblTest.setText(DETECTOR_IOC_STATUS);
		lblTest.setLayoutData(new GridData());
		final Object handlerServiceObj = PlatformUI.getWorkbench().getService(IHandlerService.class);

		ToolBar toolbar = new ToolBar(cmp, SWT.FLAT | SWT.RIGHT);

		ToolItem resetDetectorToolItem = new ToolItem(toolbar, SWT.None);
		resetDetectorToolItem.setText(RESET_DETECTOR);
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
		ToolItem openPrefToolItem = new ToolItem(toolbar, SWT.None);
		openPrefToolItem.setText(OPEN_PREFERENCES);
		openPrefToolItem.setImage(TomoClientActivator.getDefault().getImageRegistry()
				.get(ImageConstants.ICON_OPEN_PREF));

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

		Object contextService = PlatformUI.getWorkbench().getService(IContextService.class);
		if (contextService instanceof IContextService) {
			IContextService cs = (IContextService) contextService;
			cs.addContextManagerListener(contextManagerListener);
		}

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

	private IContextManagerListener contextManagerListener = new IContextManagerListener() {

		@Override
		public void contextManagerChanged(ContextManagerEvent contextManagerEvent) {
			boolean isIOCRunning = contextManagerEvent.getContextManager().getActiveContextIds()
					.contains(getIocRunningContext());
			logger.debug("ioc running : {}", isIOCRunning);
			setIocState(isIOCRunning);
		}
	};
	private Composite iocState;
	private Button btnOpenRemoteDesktop;

	@Override
	public void dispose() {
		Object contextService = PlatformUI.getWorkbench().getService(IContextService.class);
		if (contextService instanceof IContextService) {
			IContextService cs = (IContextService) contextService;
			cs.removeContextManagerListener(contextManagerListener);
		}
		super.dispose();
	}

	protected void setIocState(final boolean isIOCRunning) {
		if (getWorkbenchWindow().getShell() != null && !getWorkbenchWindow().getShell().isDisposed()) {
			getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (isIOCRunning) {
						iocState.setBackground(ColorConstants.lightGreen);
						btnOpenRemoteDesktop.setEnabled(false);
					} else {
						iocState.setBackground(ColorConstants.red);
						btnOpenRemoteDesktop.setEnabled(true);
					}
				}
			});
		}
	}
}
