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

package uk.ac.gda.client;

import gda.commandqueue.CommandProgress;
import gda.commandqueue.JythonCommandCommandProvider;
import gda.commandqueue.Processor;
import gda.commandqueue.ProcessorCurrentItem;
import gda.commandqueue.QueueChangeEvent;
import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.EclipseWidgetUtils;
import uk.ac.gda.menu.JythonControlsFactory;
import uk.ac.gda.preferences.PreferenceConstants;

public class CommandProcessorComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(CommandProcessorComposite.class);

	final String strPause = "Pause Queue";
	final String strRun = "  Start Queue  ";
	Boolean runPauseIsPaused = false;
	Boolean showText = false;

	private IObserver processorObserver;
	private Processor processor;
	private Label txtCurrentDescription;
	ProgressBar progressBar;
	String progressBarText = "";
	private Label txtState;
	private Action btnSkip;
	private Action btnRunPause;
	private boolean btnRunPause_Run = false;
	private Action btnStopAfterCurrent;
	private Action btnStop;
	private Action btnAddToQueue;
	IWorkbenchPartSite iWorkbenchPartSite;
	protected String lastAddedCommand = "";
	private ImageDescriptor pauseImage;
	private ImageDescriptor runImage;
	private boolean disableJythonControls = false;

	public CommandProcessorComposite(Composite parent, int style, final IViewSite iWorkbenchPartSite,
			final Processor processor) {
		super(parent, style);
		this.iWorkbenchPartSite = iWorkbenchPartSite;

		this.processor = processor;

		showText = gda.rcp.GDAClientActivator.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.GDA_COMMAND_QUEUE_SHOW_TEXT);

		final ImageDescriptor forwardOneImage = GDAClientActivator.getImageDescriptor("icons/control_fastforward_blue.png");
		pauseImage = GDAClientActivator.getImageDescriptor("icons/control_pause_blue.png");
		runImage = GDAClientActivator.getImageDescriptor("icons/control_play_blue.png");
		final ImageDescriptor stopImage = GDAClientActivator.getImageDescriptor("icons/delete.png");
		final ImageDescriptor addImage = GDAClientActivator.getImageDescriptor("icons/add.png");


		createComponents();

		btnRunPause = new Action(null, SWT.NONE) {
			@Override
			public void run() {
				setRun(btnRunPause_Run);
			}
		};
		btnRunPause.setId(CommandQueueViewFactory.ID + ".runpause");
		setRunBtnState(true);
		ToolBarManager toolBarManager = (ToolBarManager) iWorkbenchPartSite.getActionBars().getToolBarManager();
		toolBarManager.add(btnRunPause);

		btnSkip = new Action(null, SWT.NONE) {
			@Override
			public void run() {
				try {
					((IHandlerService) iWorkbenchPartSite.getService(IHandlerService.class)).executeCommand(
							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_SKIP_COMMAND_QUEUE, new Event());
				} catch (Exception ex) {
					logger.error("Error executing command "
							+ CommandQueueContributionFactory.UK_AC_GDA_CLIENT_SKIP_COMMAND_QUEUE);
				}
			}
		};
		btnSkip.setToolTipText("Stop current task and skip to start next - skip");
		if (showText){
			btnSkip.setText("Skip Task");
		} else {
			btnSkip.setImageDescriptor(forwardOneImage);
		}
		btnSkip.setId(CommandQueueViewFactory.ID + ".skip");
		toolBarManager.add(btnSkip);

		btnStop = new Action(null, SWT.NONE) {
			@Override
			public void run() {
				try {
					((IHandlerService) iWorkbenchPartSite.getService(IHandlerService.class)).executeCommand(
							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_STOP_COMMAND_QUEUE, new Event());
				} catch (Exception ex) {
					logger.error("Error executing command "
							+ CommandQueueContributionFactory.UK_AC_GDA_CLIENT_STOP_COMMAND_QUEUE);
				}
			}
		};
		btnStop.setToolTipText("Abort current task and pause queue");
		btnStop.setId(CommandQueueViewFactory.ID + ".abort");
		if (showText){
			btnStop.setText("Stop Queue");
		} else {
			btnStop.setImageDescriptor(stopImage);
		}
		toolBarManager.add(btnStop);

		btnStopAfterCurrent = new Action(null, SWT.NONE) {
			@Override
			public void run() {
				try {
					((IHandlerService) iWorkbenchPartSite.getService(IHandlerService.class)).executeCommand(
							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_STOP_AFTER_CURRENT_COMMAND_QUEUE, new Event());
				} catch (Exception ex) {
					logger.error("Error executing command "
							+ CommandQueueContributionFactory.UK_AC_GDA_CLIENT_STOP_AFTER_CURRENT_COMMAND_QUEUE, ex);
				}
			}
		};
		btnStopAfterCurrent.setToolTipText("Stop the processor after the current command has completed");
		btnStopAfterCurrent.setId(CommandQueueViewFactory.ID + ".stopaftercurrent");
		btnStopAfterCurrent.setText("Insert Stop");
		toolBarManager.add(btnStopAfterCurrent);

		btnAddToQueue = new Action(null, SWT.NONE) {
			@Override
			public void run() {
				try {
					InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Add Command To Queue",
							"Enter a command ( Use \"run scriptname\" to execute a script)", lastAddedCommand, null);
					if (dlg.open() == Window.OK) {
						lastAddedCommand = dlg.getValue();
						CommandQueueViewFactory.getQueue().addToTail(
								new JythonCommandCommandProvider(lastAddedCommand, lastAddedCommand, null));
					}
				} catch (Exception e1) {
					logger.error("Error submitting command to queue");
				}
			}
		};
		btnAddToQueue.setToolTipText("Add Jython command to queue");
		btnAddToQueue.setId(CommandQueueViewFactory.ID + ".addtoqueue");
		btnAddToQueue.setImageDescriptor(addImage);
		toolBarManager.add(btnAddToQueue);

		toolBarManager.update(true);

		txtCurrentDescription.setText("Description");

		progressBar.setMinimum(0);
		progressBar.setMaximum(2000);
		progressBar.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Point point = progressBar.getSize();
				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * progressBarText.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground(iWorkbenchPartSite.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
				e.gc.drawString(progressBarText, (point.x - width) / 2, (point.y - height) / 2, true);
			}
		});

		if (processor != null) {
			processorObserver = new IObserver() {

				@Override
				public void update(Object source, final Object arg) {
					if (arg instanceof Processor.STATE) {
						CommandProcessorComposite.this.updateStateAndDescription((Processor.STATE) arg);
					} else if (arg instanceof QueueChangeEvent) {
						// do nothing
					} else if (arg instanceof CommandProgress) {
						iWorkbenchPartSite.getShell().getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								CommandProgress progress = (CommandProgress) arg;
								progressBarText = progress.getMsg();
								progressBar.setSelection((int) (progress.getPercentDone()*20));
								progressBar.redraw();//this is needed to cope with a change in text but no change in percentage

							}
						});
					}
				}
			};
			processor.addIObserver(processorObserver);
		}

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (processor != null && processorObserver != null) {
					processor.deleteIObserver(processorObserver);
				}
			}
		});

		disableJythonControls = GDAClientActivator.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.GDA_COMMAND_QUEUE_DISABLE_JYTHON_CONTROLS);

		updateStateAndDescription(null);
	}

	private void createComponents() {
		GridLayoutFactory.swtDefaults().applyTo(this);

		final Group statusGroup = new Group(this, SWT.BORDER);
		statusGroup.setText("Queue status");
		GridLayoutFactory.swtDefaults().margins(3, 3).applyTo(statusGroup);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusGroup);

		final Group currentTaskGroup = new Group(this, SWT.BORDER);
		currentTaskGroup.setText("Current task");
		GridLayoutFactory.swtDefaults().applyTo(currentTaskGroup);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(currentTaskGroup);

		txtState = new Label(statusGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtState);

		txtCurrentDescription = new Label(currentTaskGroup, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtCurrentDescription);

		progressBar = new ProgressBar(currentTaskGroup, SWT.SMOOTH | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(progressBar);
	}

	private void setRunBtnState(boolean run){
		btnRunPause_Run = run;
		if( run){
			if (showText){
				btnRunPause.setText(strRun);
			} else {
				btnRunPause.setImageDescriptor(CommandProcessorComposite.this.runImage);
			}
			btnRunPause.setToolTipText("Run current task if paused or start next task");
		} else {
			if (showText){
				btnRunPause.setText(strPause);
			} else {
				btnRunPause.setImageDescriptor(CommandProcessorComposite.this.pauseImage);
			}
			btnRunPause.setToolTipText("Pause current task if possible. Stop queue");
		}
	}

	protected void setRun(boolean run) {
		String commandId = run ? CommandQueueContributionFactory.UK_AC_GDA_CLIENT_START_COMMAND_QUEUE
				: CommandQueueContributionFactory.UK_AC_GDA_CLIENT_PAUSE_COMMAND_QUEUE;
		try {
			((IHandlerService) iWorkbenchPartSite.getService(IHandlerService.class)).executeCommand(commandId,
					new Event());
		} catch (Exception e) {
			logger.error("Error executing command " + commandId, e);
		}
	}

	// update the GUI based on state of processor
	private void updateStateAndDescription(final Processor.STATE stateIn) {
		if (processor == null)
			return;
		iWorkbenchPartSite.getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				Processor.STATE state = stateIn;
				if (state == null)
					state = getProcessorState();
				ProcessorCurrentItem currentItem = getProcessorCurrentItem();
				boolean itemBeingProcessed = currentItem != null;
				txtCurrentDescription.setText(currentItem != null ? currentItem.getDescription() : "No current task");
				txtCurrentDescription.setForeground(currentItem != null ? null : Display.getCurrent().getSystemColor(
						SWT.COLOR_GRAY));
				if (!itemBeingProcessed) {
					progressBarText = ""; //set text first
					progressBar.setSelection(0);
				}

				switch (state) {
				case PROCESSING_ITEMS:
					if (disableJythonControls) {
						JythonControlsFactory.disableUIControls();
					}
					if (showText){
						btnRunPause.setText(strPause);
					} else {
						btnRunPause.setImageDescriptor(CommandProcessorComposite.this.pauseImage);
					}
					btnRunPause.setToolTipText("Pause current task if possible. Stop queue");
					setRunBtnState(false);
					btnRunPause.setEnabled(true);
					btnStop.setEnabled(true);
					btnSkip.setEnabled(true);
					btnStopAfterCurrent.setEnabled(true);
					txtState.setText("Running");

					break;
				case UNKNOWN:
					if (disableJythonControls) {
						JythonControlsFactory.enableUIControls();
					}
					txtState.setText("Unknown");
					break;
				case WAITING_QUEUE:
					if (showText){
						btnRunPause.setText(strPause);
					} else {
						btnRunPause.setImageDescriptor(CommandProcessorComposite.this.pauseImage);
					}
					btnRunPause.setToolTipText("Pause current task if possible. Stop queue");
					setRunBtnState(false);
					btnRunPause.setEnabled(false);
					btnStop.setEnabled(false);
					btnSkip.setEnabled(false);
					btnStopAfterCurrent.setEnabled(true);
					txtState.setText("Queue is empty");
					break;
				case WAITING_START:
					if (disableJythonControls) {
						JythonControlsFactory.disableUIControls();
					}
					if (showText){
						btnRunPause.setText(strRun);
					} else {
						btnRunPause.setImageDescriptor(CommandProcessorComposite.this.runImage);
					}
					btnRunPause.setToolTipText("Run current task if paused or start next task");
					setRunBtnState(true);
					btnRunPause.setEnabled(true);
					btnStop.setEnabled(true);
					btnSkip.setEnabled(currentItem != null);
					btnStopAfterCurrent.setEnabled(true);
					txtState.setText("Paused");
					break;
				}
				EclipseWidgetUtils.forceLayoutOfTopParent(CommandProcessorComposite.this);
			}

			private ProcessorCurrentItem getProcessorCurrentItem() {
				try {
					return processor.getCurrentItem();
				} catch (Exception e) {
					logger.error("Error getting processor current item", e);
				}
				return null;
			}

			private Processor.STATE getProcessorState() {
				try {
					return processor.getState();
				} catch (Exception e) {
					logger.error("Error getting processor state", e);
				}
				return Processor.STATE.UNKNOWN;
			}

		});

	}
}

// Helper class - to find a handler and execute
class ActionSelectionListener implements SelectionListener {

	IServiceLocator serviceLocator;
	String commandId;
	private static final Logger logger = LoggerFactory.getLogger(ActionSelectionListener.class);

	public ActionSelectionListener(IServiceLocator serviceLocator, String commandId) {
		super();
		this.serviceLocator = serviceLocator;
		this.commandId = commandId;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		try {
			((IHandlerService) serviceLocator.getService(IHandlerService.class)).executeCommand(commandId, new Event());
		} catch (Exception ex) {
			logger.error("Error executing command " + commandId);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}
}
