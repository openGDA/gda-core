/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.event.ui.view;

import java.math.RoundingMode;
import java.net.URI;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.widgets.menu.MenuAction;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.core.JobQueueConfiguration;
import org.eclipse.scanning.api.event.queue.QueueStatus;
import org.eclipse.scanning.api.event.queues.QueueViews;
import org.eclipse.scanning.api.event.scan.IBeanSummariser;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanBeanSummariser;
import org.eclipse.scanning.api.event.status.OpenRequest;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.IRerunHandler;
import org.eclipse.scanning.api.ui.IResultHandler;
import org.eclipse.scanning.event.ui.Activator;
import org.eclipse.scanning.event.ui.dialog.PropertiesDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * A view for which the secondary id MUST be set and provides the queueName
 * and optionally the queue view name if a custom one is required. Syntax of
 * these parameters in the secondary id are key1=value1;key2=value2...
 *
 * The essential keys are: beanBundleName, beanClassName, queueName, topicName, submissionQueueName
 * You can use createId(...) to generate a legal id from them.
 *
 * The optional keys are: partName,
 *                        uri (default CommandConstants.JMS_URI),
 *                        userName (default is user.name system property)
 *
 * Example id for this view would be:
 * org.eclipse.scanning.event.ui.queueView:beanClassName=org.dawnsci.commandserver.mx.beans.ProjectBean;beanBundleName=org.dawnsci.commandserver.mx
 *
 * You can optionally extend this class to provide a table which is displayed for your
 * queue of custom objects. For instance for a queue showing xia2 reruns, the
 * extra columns for this could be defined. However by default the
 *
 * @author Matthew Gerring
 *
 */
public class StatusQueueView extends EventConnectionView {

	private static final String RERUN_HANDLER_EXTENSION_POINT_ID = "org.eclipse.scanning.api.rerunHandler";

	private static final String RESULTS_HANDLER_EXTENSION_POINT_ID = "org.eclipse.scanning.api.resultsHandler";

	public static final String ID = "org.eclipse.scanning.event.ui.queueView";

	private static final Logger logger = LoggerFactory.getLogger(StatusQueueView.class);

	private static final String DEFAULT_QUEUE_ARGUMENTS = createId(
					"org.eclipse.scanning.api",
					StatusBean.class.getSimpleName(),
					EventConstants.STATUS_TOPIC,
					EventConstants.SUBMISSION_QUEUE);

	private static final String SUSPEND_QUEUE_TOOLTIP = "Suspend queueing of upcoming jobs\nDoes not pause current job";
	private static final String UNSUSPEND_QUEUE_TOOLTIP = "Unsuspend queueing of upcoming jobs\nDoes not undefer upcoming job(s)";
	private static final String SUSPEND_QUEUE_ICON = "icons/switch-queue-on.png";
	private static final String UNSUSPEND_QUEUE_ICON = "icons/switch-queue-off.png";
	private static final String SUSPEND_QUEUE_PART_NAME = " - suspended";

	private static final String CLEAR_ENTIRE_QUEUE_CONFIRMATION = "Are you sure you would like to remove all items from the queue?";
	private static final String CLEAR_SUBMITTED_JOBS_CONFIRMATION = "Are you sure you would like to remove all pending items from the queue?";

	// UI
	private TableViewer viewer;
	private DelegatingSelectionProvider selectionProvider;
	private Job queueJob;
	private boolean queueJobAgain = false;
	private String partName;

	// Data
	private Map<String, StatusBean> queue;
	private List<StatusBean> runList = new ArrayList<>();
	private List<StatusBean> submittedList =  new ArrayList<>();
	private boolean hideOtherUsersResults = false;

	private ISubscriber<IBeanListener<StatusBean>> statusTopicSubscriber;

	private IJobQueue<StatusBean> jobQueueProxy;

	// Actions on a specific item (StatusBean) in the queue
	private Action openResultsAction;
	private Action rerunAction;
	private Action removeAction;
	private Action upAction;
	private Action downAction;
	private Action pauseAction;
	private Action deferAction;
	private Action stopAction;
	private Action openAction;
	private Action detailsAction;

	private IContributionManager toolMan;
	private IContributionManager dropDown;
	private MenuManager menuMan;

	// Actions on the whole queue
	private Action suspendQueueAction;

	private IEventService service;

	private List<IResultHandler<StatusBean>> resultsHandlers = null;

	protected IBeanSummariser toolTipTextProvider;

	private WatchdogWatcher watchdogWatcher;

	public StatusQueueView() {
		this.service = ServiceProvider.getService(IEventService.class);

		toolTipTextProvider = ServiceProvider.getServiceOrRegisterNew(IBeanSummariser.class, ScanBeanSummariser::new);
	}

	@Override
	public void createPartControl(Composite content) {
		content.setLayout(new GridLayout(1, false));
		Util.removeMargins(content);

		this.viewer   = new TableViewer(content, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		viewer.setUseHashlookup(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ColumnViewerToolTipSupport.enableFor(viewer);

		createColumns();
		viewer.setContentProvider(createContentProvider());

		try {
			jobQueueProxy = service.createJobQueueProxy(getUri(), getSubmissionQueueName(), EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		} catch (Exception e) {
			logAndDisplayError("Cannot connect to queue", "Could not create proxy to queue " + getSubmissionQueueName(), e);
		}

		try {
			updateQueue();

			String name = getSecondaryIdAttribute("partName");
			if (name!=null) setPartName(name);
			partName = getPartName();

			createActions();

			// We just use this submitter to read the queue
			createTopicListener(getUri());
		} catch (Exception e) {
			logAndDisplayError("Cannot connect to status topic", "Could not create listener to status topic", e);
		}

		selectionProvider = new DelegatingSelectionProvider(viewer);
		getViewSite().setSelectionProvider(selectionProvider);
		viewer.addSelectionChangedListener(event -> updateStatusBeanActions());
	}

	private void updateStatusBeanActions() {
		final List<StatusBean> selection = getSelection();
		final List<String> selectedUniqueIds = selection.stream().map(StatusBean::getUniqueId).toList();
		final List<StatusBean> selectedInSubmittedList = submittedList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.toList();
		final List<StatusBean> selectedInRunList = runList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.toList();

		final boolean activeScanSelected = selectedInRunList.stream().anyMatch(st -> st.getStatus().isActive());
		final boolean activeScanPaused = selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isPaused());
		final boolean anyFinalSelectedInRunList = selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isFinal());
		final boolean anySelectedInSubmittedList = !selectedInSubmittedList.isEmpty();
		final boolean anyNonFinalInSubmittedList = selectedInSubmittedList.stream().anyMatch(sb -> !sb.getStatus().isFinal());
		final boolean allSelectedSubmittedDeferred = selectedInSubmittedList.stream().allMatch(
				sb -> sb.getStatus().isPaused() || sb.getStatus().isFinal());

		removeActionUpdate(selectedInSubmittedList, selectedInRunList);
		rerunAction.setEnabled(!selection.isEmpty());
		upAction.setEnabled(anySelectedInSubmittedList);
		downAction.setEnabled(anySelectedInSubmittedList);

		stopAction.setEnabled(selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isActive()));
		pauseActionUpdate(activeScanSelected, activeScanPaused);
		deferActionUpdate(anyNonFinalInSubmittedList, anyNonFinalInSubmittedList && allSelectedSubmittedDeferred);

		openResultsAction.setEnabled(anyFinalSelectedInRunList);
		// Enable only if only one job is selected (we don't care about this job's status)
		openAction.setEnabled(selection.size() == 1);
		detailsActionUpdate(selectedInSubmittedList, selectedInRunList);

		// Some sanity checks
		warnIfListContainsStatus("null status found in selection:       ", selection, null);
		warnIfListContainsStatus("RUNNING status found in submittedList: ", submittedList, org.eclipse.scanning.api.event.status.Status.RUNNING);
		warnIfListContainsStatus("SUBMITTED status found in runList:       ", runList, org.eclipse.scanning.api.event.status.Status.SUBMITTED);
		warnIfListContainsStatus("DEFERRED status found in runList:       ", runList, org.eclipse.scanning.api.event.status.Status.DEFERRED);
	}

	private void warnIfListContainsStatus(String description, List<StatusBean> list, org.eclipse.scanning.api.event.status.Status status) {
		List<StatusBean> matchingStatusList = list.stream()
				.filter(x -> x.getStatus()==status)
				.toList();

			if (!matchingStatusList.isEmpty()) {
				logger.warn("{} {}", description, matchingStatusList);
			}
	}

	/**
	 * Listens to a topic
	 */
	private void createTopicListener(final URI uri) {

		// Use job because connection might timeout.
		final Job topicJob = new Job("Create topic listener") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					statusTopicSubscriber = service.createSubscriber(uri, getTopicName());
					statusTopicSubscriber.addListener(evt -> {
							final StatusBean bean = evt.getBean();
							try {
								mergeBean(bean);
							} catch (Exception e) {
								logger.error("Cannot merge changed bean!", e);
							}
						});

					watchdogWatcher = new WatchdogWatcher(uri);
					return Status.OK_STATUS;

				} catch (Exception ne) {
					logger.error("Cannot listen to topic changes because command server is not there", ne);
					return Status.CANCEL_STATUS;
				}
			}
		};

		topicJob.setPriority(Job.INTERACTIVE);
		topicJob.setSystem(true);
		topicJob.setUser(false);
		topicJob.schedule();
	}

	@Override
	public void dispose() {
		super.dispose();
		try {
			if (statusTopicSubscriber!=null) statusTopicSubscriber.disconnect();
			if (watchdogWatcher != null) watchdogWatcher.dispose();
		} catch (Exception ne) {
			logger.warn("Problem stopping topic listening for "+getTopicName(), ne);
		}
		try {
			if (jobQueueProxy != null) jobQueueProxy.disconnect();
		} catch (Exception e) {
			logger.warn("Problem disconnecting publisher from command topic", e);
		}
	}

	/**
	 * Updates the bean if it is found in the list, otherwise
	 * refreshes the whole list because a bean we are not reporting
	 * has been(bean?) encountered.
	 *
	 * @param bean
	 */
	private void mergeBean(final StatusBean bean) {
		getSite().getShell().getDisplay().asyncExec(() -> {
				final Instant jobStartTime = Instant.now();

				if (queue.containsKey(bean.getUniqueId())) {
					StatusBean oldBean = queue.get(bean.getUniqueId());
					if (bean.getStatus().isActive() && !runList.contains(oldBean)) {
						runList.add(oldBean);
						submittedList.remove(oldBean);
					}
					logger.trace("mergeBean(id={}) Merging existing bean: {}", bean.getUniqueId(), bean);
					oldBean.merge(bean);

					/*
					 * Not reliable without viewer.refresh()
					 * Ought to update just relevant row, but seems to work intermittently: every ~40 points?
					 * If this worked would prevent having to redraw whole queue.
					 */
//					viewer.update(queue.get(bean.getUniqueId()), null);
					warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "merge complete");
				} else if (bean.getStatus().equals(org.eclipse.scanning.api.event.status.Status.SUBMITTED)) {
					logger.trace("mergeBean(id={}) Adding new bean:       {}", bean.getUniqueId(), bean);
					queue.put(bean.getUniqueId(), bean);
					// Necessary for toolbar actions to work
					submittedList.add(bean);
					/*
					 * If viewer.update() worked above, could potentially move refresh here
					 * But in testing sometimes update is applied to wrong status bean, in
					 * addition to the irregular and spaced out update rate.
					 */
//					viewer.refresh();
					warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "bean added");
				}
				viewer.refresh();
				warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "refresh complete");
				updateStatusBeanActions();
				warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "updateStatusBeanActions complete");
			});
	}

	private void createActions() {
		toolMan  = getViewSite().getActionBars().getToolBarManager();
	    dropDown = getViewSite().getActionBars().getMenuManager();
		menuMan = new MenuManager();

		suspendQueueAction = suspendQueueActionCreate();

		addSeparators();

		openResultsAction = createAction(QueueAction.OPEN_RESULTS, this::openResultsActionRun);

		addSeparators();

		upAction = createAction(QueueAction.UP, this::upActionRun);
		downAction = createAction(QueueAction.DOWN, this::downActionRun);
		deferAction = createAction(QueueAction.DEFER, this::deferActionRun, IAction.AS_CHECK_BOX);
		pauseAction = createAction(QueueAction.PAUSE, this::pauseActionRun, IAction.AS_CHECK_BOX);
		stopAction = createAction(QueueAction.STOP, this::stopActionRun);

		addSeparators();

		removeAction = createAction(QueueAction.REMOVE, this::removeActionRun);
		rerunAction = createAction(QueueAction.RERUN, this::rerunActionRun);
		openAction = createAction(QueueAction.OPEN, this::openActionRun);
		detailsAction = createAction(QueueAction.DETAILS, this::detailsActionRun);

		addSeparators();

		hideOtherUsersResultsActionCreate();

		addSeparators();

		final Action refreshAction = createAction(QueueAction.REFRESH, this::reconnect);
		refreshAction.setEnabled(true);
		jobQueueProxy.addQueueStatusListener(newStatus -> {
			if (newStatus.equals(QueueStatus.MODIFIED)) {
				refreshAction.run();
			}
		});

		final Action configureAction = createAction(QueueAction.CONFIGURE, this::configureActionRun);
		configureAction.setEnabled(true);
		menuMan.add(new Separator());
		dropDown.add(new Separator());

		createClearQueueMenuActions();

		viewer.getControl().setMenu(menuMan.createContextMenu(viewer.getControl()));
	}

	private void createClearQueueMenuActions() {
		var menuAction = new MenuAction("Clear queue...");
		menuAction.setImageDescriptor(Activator.getImageDescriptor(QueueAction.CLEAR_QUEUE.getImageName()));
		var clearQueueAction = new RunnableAction(QueueAction.CLEAR_QUEUE.getLabel(),
				QueueAction.CLEAR_QUEUE.getImageName(), this::clearQueueActionRun);

		var clearSubmissionQueueAction = new RunnableAction(QueueAction.CLEAR_SUBMISSION_QUEUE.getLabel(),
				QueueAction.CLEAR_SUBMISSION_QUEUE.getImageName(), this::clearSubmittedQueueActionRun);

		menuAction.add(clearQueueAction);
		menuAction.add(clearSubmissionQueueAction);

		addAction(menuAction);
	}

	private void addAction(Action action) {
		if (toolMan != null)  toolMan.add(action);
		if (menuMan != null)  menuMan.add(action);
		if (dropDown != null) dropDown.add(action);
	}

	private void addSeparators() {
		if (toolMan != null)  toolMan.add(new Separator());
		if (menuMan != null)  menuMan.add(new Separator());
		if (dropDown != null) dropDown.add(new Separator());
	}

	private void upActionRun() {
		for(StatusBean bean : getSelection()) {
			try {
				if (bean.getStatus() == org.eclipse.scanning.api.event.status.Status.SUBMITTED) {
					jobQueueProxy.moveBackward(bean);
				} else {
					logger.info("Cannot move {} up as it's status ({}) is not SUBMITTED", bean.getName(), bean.getStatus());
				}
			} catch (EventException e) {
				logAndDisplayError("Cannot move "+bean.getName()+" up",
						"'"+bean.getName()+"' cannot be moved in the submission queue.", e);
			}
		}
		refresh();
	}

	private void downActionRun() {
		// When moving items down, if we move an item down before moving down an adjacent item below it, we end
		// up with both items in the same position. To avoid this, we iterate the selection list in reverse.
		List<StatusBean> selection = getSelection();
		Collections.reverse(selection);
		for (StatusBean bean : selection) {
			try {
				if (bean.getStatus() == org.eclipse.scanning.api.event.status.Status.SUBMITTED) {
					jobQueueProxy.moveForward(bean);
				} else {
					logger.info("Cannot move {} down as it's status ({}) is not SUBMITTED", bean.getName(), bean.getStatus());
				}
			} catch (EventException e) {
				logAndDisplayError("Cannot move "+bean.getName()+" down",
						"'"+bean.getName()+"' cannot be moved in the submission queue.", e);
			}
		}
		refresh();
	}

	private void suspendQueueActionUpdate(QueueStatus status) {
		final boolean queueSuspended = (status == QueueStatus.PAUSED);
		suspendQueueAction.setChecked(queueSuspended);
		suspendQueueAction.setText(queueSuspended ? UNSUSPEND_QUEUE_TOOLTIP : SUSPEND_QUEUE_TOOLTIP);
		suspendQueueAction.setImageDescriptor(Activator.getImageDescriptor(queueSuspended ? UNSUSPEND_QUEUE_ICON  : SUSPEND_QUEUE_ICON));
		// Grey out the table viewer when the queue is suspended
		viewer.getControl().setForeground(getDisplay().getSystemColor(queueSuspended ? SWT.COLOR_GRAY : SWT.COLOR_BLACK));
		// Append suspended to the view name when the queue is suspended
		setPartName(queueSuspended ? partName + SUSPEND_QUEUE_PART_NAME : partName);
	}

	private Action suspendQueueActionCreate() {
		final boolean queueSuspended = jobQueueProxy.isPaused();
		final String actionLabel = queueSuspended ? UNSUSPEND_QUEUE_TOOLTIP : SUSPEND_QUEUE_TOOLTIP;
		final String imageName = queueSuspended ? UNSUSPEND_QUEUE_ICON  : SUSPEND_QUEUE_ICON;
		final Action action = new RunnableAction(actionLabel, imageName, this::suspendQueueActionRun, IAction.AS_CHECK_BOX);
		action.setChecked(queueSuspended);
		// This should be called from the UI thread
		jobQueueProxy.addQueueStatusListener(queueStatus -> Display.getDefault().asyncExec(() -> suspendQueueActionUpdate(queueStatus)));
		addAction(action);
		return action;
	}

	private void suspendQueueActionRun() {
		try {
			if (jobQueueProxy.isPaused()) {
				jobQueueProxy.resume();
			} else {
				jobQueueProxy.pause();
			}
		} catch (Exception e) {
			logAndDisplayError("Cannot pause queue "+getSubmissionQueueName(),
					"Cannot pause queue "+getSubmissionQueueName()+"\n\nPlease contact your support representative.", e);
		}
		suspendQueueActionUpdate(jobQueueProxy.getQueueStatus());
	}

	private void pauseActionUpdate(boolean activeScanSelected, boolean activeScanPaused) {
		pauseAction.setEnabled(activeScanSelected);
		pauseAction.setChecked(activeScanPaused);
		pauseAction.setText(activeScanPaused ? "Resume job" :"Pause job");
	}

	private void pauseActionRun() {
		pauseAction.setEnabled(false);

		final List<String> selectedUniqueIds= getSelection().stream().map(StatusBean::getUniqueId).toList();
		final List<StatusBean> selectedInRunList = runList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.toList();

		for(StatusBean bean : selectedInRunList) {
			if (bean.getStatus().isFinal()) continue;

			try {
				// Invert as JFace bindings toggle state first...
				if (!pauseAction.isChecked()) {
					if (watchdogWatcher != null) {
						resumeJob(bean);
					} else {
						jobQueueProxy.resumeJob(bean);
					}
				} else {
					jobQueueProxy.pauseJob(bean);
				}
			} catch (Exception e) {
				logAndDisplayError("Cannot pause "+bean.getName(),
						"Cannot pause "+bean.getName()+"\n\nPlease contact your support representative.", e);
			}
		}
	}

	private void resumeJob(StatusBean bean) throws Exception {
		if (!watchdogWatcher.isWatchdogPausing()) {
			jobQueueProxy.resumeJob(bean);
		} else {
			boolean ok = MessageDialog.openQuestion(getSite().getShell(),
					"Confirm Resume '"+ bean.getName()+ "'",
					"Are you sure you would like to resume '"+bean.getName()+ "?" + "\n\n"+
					"The following watchdogs are pausing:" + "\n\n" + watchdogWatcher.getWatchdogPausingNames());

			// if user confirms and the watchdog is still pausing by the time it confirms
			// as it will not request to resume again if watchdog has already resume
			if (ok && watchdogWatcher.isWatchdogPausing()) {
				jobQueueProxy.resumeJob(bean);
			} else {
				pauseAction.setEnabled(true);
			}
		}
	}

	private void deferActionUpdate(boolean deferableSelectedInSubmittedList, boolean allSelectedSubmittedDeferred) {
		deferAction.setEnabled(deferableSelectedInSubmittedList);
		deferAction.setChecked(allSelectedSubmittedDeferred);
		deferAction.setText(allSelectedSubmittedDeferred ? "Undefer job(s)" :"Defer job(s)");
	}

	private void deferActionRun() {
		final List<String> selectedUniqueIds = getSelection().stream().map(StatusBean::getUniqueId).toList();
		final List<StatusBean> selectedInSubmittedList = submittedList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.toList();

		for(StatusBean bean : selectedInSubmittedList) {
			if (bean.getStatus().isFinal()) continue;

			try {
				// Invert as JFace bindings toggle state first...
				if (!deferAction.isChecked()) {
					jobQueueProxy.undefer(bean);
				} else {
					jobQueueProxy.defer(bean);
				}
			} catch (Exception e) {
				logAndDisplayError("\"Cannot defer \"+bean.getName()",
						"Cannot defer "+bean.getName()+"\n\nPlease contact your support representative.", e);
			}
		}

	}

	private void removeActionUpdate(List<StatusBean> selectedInSubmittedList, List<StatusBean> selectedInRunList) {
		final boolean anySelectedInSubmittedList = !selectedInSubmittedList.isEmpty();
		final boolean anySelectedInRunListAreFinal = selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isFinal());

		removeAction.setEnabled(anySelectedInSubmittedList || anySelectedInRunListAreFinal);
	}

	private void removeActionRun() {
		for(StatusBean bean : getSelection()) {
			if (!bean.getStatus().isActive()) {
				try {
					if (bean.getStatus() == org.eclipse.scanning.api.event.status.Status.SUBMITTED) {
						// It is submitted and not running. We can probably delete it.
						jobQueueProxy.remove(bean);
					} else {
						// only ask the user to confirm is the queue is in the status set not the submit queue
						boolean ok = MessageDialog.openQuestion(getSite().getShell(), "Confirm Remove '"+bean.getName()+"'",
							"Are you sure you would like to remove '"+bean.getName()+"'?");
						if (ok) {
							jobQueueProxy.removeCompleted(bean);
						}
					}
					refresh();
				} catch (EventException e) {
					logAndDisplayError("Cannot delete "+bean.getName(),
							"Cannot delete "+bean.getName()+"\n\nIt might have changed state at the same time and being remoted.", e);
				}
			} else {
				MessageDialog.openWarning(getViewSite().getShell(), "Cannot remove running job",
						"Cannot delete "+bean.getName()+" as it is currently active.");
			}
		}
	}

	private void stopActionRun() {
		for(StatusBean bean : getSelection()) {
			if (bean.getStatus().isActive()) {
			try {
				final String submissionTime = DateFormat.getDateTimeInstance().format(new Date(bean.getSubmissionTime()));
				final boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm terminate "+bean.getName(),
						  "Are you sure you want to terminate "+bean.getName()+" submitted on "+submissionTime+"?");
				if (!ok) continue;

				jobQueueProxy.terminateJob(bean);
				refresh();
				logger.info("Requesting termination of {} submitted on {}", bean.getName(), submissionTime);
			} catch (Exception e) {
				logAndDisplayError("Cannot terminate "+bean.getName(),
						"Cannot terminate "+bean.getName()+"\n\nPlease contact your support representative.", e);
			}
			} else {
				MessageDialog.openWarning(getViewSite().getShell(), "Cannot stop inactive job",
						"Cannot stop "+bean.getName()+" as it is not currently active.");
			}
		}
	}

	private Predicate<StatusBean> isActive = s -> s.getStatus().isActive();

	private void clearQueueActionRun() {
		boolean ok = MessageDialog.openQuestion(getSite().getShell(), "Confirm clear queues",
				CLEAR_ENTIRE_QUEUE_CONFIRMATION + "\nThis could abort or disconnect runs of other users");
		if (!ok) return;

		boolean terminateRunningScan = false;

		if (runList.stream().anyMatch(isActive)) {
			terminateRunningScan = MessageDialog.openQuestion(getSite().getShell(),
					"Confirm scan termination",
					"Would you like to terminate the currently active scan?");
		}

		try {
			jobQueueProxy.clearQueue();
			jobQueueProxy.clearRunningAndCompleted(terminateRunningScan);
		} catch(EventException e) {
			logger.error("Cannot purge queues", e);
		}

		// Still reconnect to ensure that queue reflects state of server queue, is empty
		reconnect();
	}

	private void clearSubmittedQueueActionRun() {
		boolean ok = MessageDialog.openQuestion(getSite().getShell(), "Confirm clear submitted jobs queue",
				CLEAR_SUBMITTED_JOBS_CONFIRMATION + "\nThis could abort or disconnect runs of other users");
		if (!ok) return;

		try {
			jobQueueProxy.clearQueue();
		} catch(EventException e) {
			logger.error("Cannot purge queues", e);
		}

		// Still reconnect to ensure that queue reflects state of server queue, is empty
		reconnect();
	}

	private List<IResultHandler<StatusBean>> getResultsHandlers() {
		if (resultsHandlers == null) {
			final IConfigurationElement[] configElements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(RESULTS_HANDLER_EXTENSION_POINT_ID);
			final List<IResultHandler<StatusBean>> handlers = new ArrayList<>(configElements.length + 1);
			for (IConfigurationElement configElement : configElements) {
				try {
					@SuppressWarnings("unchecked")
					final IResultHandler<StatusBean> handler = (IResultHandler<StatusBean>) configElement.createExecutableExtension("class");
					handler.init(service, createJobQueueConfiguration());
					handlers.add(handler);
				} catch (Exception e) {
					logAndDisplayError("Internal Error", "Could not create results handler for class " + configElement.getAttribute("class") +
							".\n\nPlease contact your support representative.", e);
				}
			}
			handlers.add(new DefaultResultsHandler());
			resultsHandlers = handlers;
		}
		return resultsHandlers;
	}

	private void openResultsActionRun() {
		for (StatusBean bean : getSelection()) {
			openResultsActionRun(bean);
		}
	}

	/**
	 * You can override this method to provide custom opening of
	 * results if required.
	 *
	 * @param bean
	 */
	private void openResultsActionRun(StatusBean bean) {
		if (bean == null) return;
		for (IResultHandler<StatusBean> handler : getResultsHandlers()) {
			if (handler.isHandled(bean)) {
				try {
					boolean ok = handler.open(bean);
					if (ok) break;
				} catch (Exception e) {
					logAndDisplayError("Internal Error", handler.getErrorMessage(null), e);
				}
			}
		}
	}

	/**
	 * Pushes any previous run back into the UI
	 */
	private void openActionRun() {
		final List<StatusBean> beans = getSelection();

		// TODO FIXME Change to IScanBuilderService not selections so that it works with e4.
		// We fire a special object into the selection mechanism with the data for this run.
		// It is then up to parts to respond to this selection and update their contents.
		// We call fireSelection as the openRequest isn't in the table. This sets the workb
		for (StatusBean bean : beans) {
			selectionProvider.fireSelection(new StructuredSelection(new OpenRequest(bean)));
		}
	}

	private void detailsActionUpdate(List<StatusBean> selectedInSubmittedList, List<StatusBean> selectedInRunList) {
		/* If more than one job is selected, only the first is considered, and even then it is only shown if it is in
		 * the run list (i.e. the job is either running or has completed running) so only enable when only a single
		 * item is selected in the run list.
		 *
		 * This prevents Unhandled event loop exceptions when the button is pressed with no selection or when the first
		 * selection is in the submitted list.
		 */
		detailsAction.setEnabled(selectedInSubmittedList.isEmpty() && selectedInRunList.size() == 1);
	}

	private void detailsActionRun() {
		final ScanDetailsDialog detailsDialog = new ScanDetailsDialog(getSite().getShell(), getSelection().get(0));
		detailsDialog.setBlockOnOpen(true);
		detailsDialog.open();
	}

	private void rerunActionRun() {
		boolean handled = false;
		var selection = getSelection();

		/* We copy the selection for safety (we cannot guarantee handler behaviour).
		 * We also reverse it: this is because getSelection() returns beans ordered from top to bottom,
		 * but this action will resubmit items and execution occurs from bottom to top */
		var beansToBeRerun = new ArrayList<>(selection.stream().map(this::copyBean).toList());
		Collections.reverse(beansToBeRerun);

		try {
			final IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(RERUN_HANDLER_EXTENSION_POINT_ID);
			for (var extension : extensions) {
				@SuppressWarnings("unchecked")
				final IRerunHandler<StatusBean> handler = (IRerunHandler<StatusBean>) extension.createExecutableExtension("class");
				handler.init(service, createJobQueueConfiguration());
				if (selection.stream().allMatch(handler::isHandled)) {
					handled = handler.handleRerun(beansToBeRerun);
					if (handled) break;
				}
			}
		} catch (Exception ne) {
			logAndDisplayError("Internal Error", "Cannot rerun selected scan(s) normally.\n\nPlease contact your support representative.", ne);
		}
		if (!handled) {
			// If we have not already handled this rerun,
			// we provide a default implementation.
			rerun(beansToBeRerun);
		}
	}

	private JobQueueConfiguration createJobQueueConfiguration() throws Exception {
		return new JobQueueConfiguration(getUri(), getSubmissionQueueName(), getTopicName(), getQueueName());
	}

	private StatusBean copyBean(StatusBean bean) {
		try {
			final StatusBean copy = bean.getClass().getDeclaredConstructor().newInstance();
			copy.merge(bean);
			copy.setUniqueId(UUID.randomUUID().toString());
			copy.setMessage("Rerun of " + bean.getName());
			copy.setStatus(org.eclipse.scanning.api.event.status.Status.SUBMITTED);
			copy.setPercentComplete(0.0);
			return copy;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void submit(StatusBean bean) throws EventException {
		bean.setSubmissionTime(System.currentTimeMillis());
		jobQueueProxy.submit(bean);
		mergeBean(bean);
	}

	/**
	 * Default rerun implementation asks for user confirmation and resubmits once
	 */
	private void rerun(List<StatusBean> beans) {
		try {

			final DateFormat format = DateFormat.getDateTimeInstance();
			var selection = beans.stream()
								.map(bean -> String.format("%s submitted on %s",
										bean.getName(), format.format(new Date(bean.getSubmissionTime()))))
								.collect(Collectors.joining("\n"));

			boolean confirmed = MessageDialog.openQuestion(getViewSite().getShell(),
					"Confirm resubmission of selected scan(s)",
					"Are you sure you want to rerun the following?\n\n" + selection);

			if (!confirmed) return;

			for (var bean : beans) {
				submit(copyBean(bean));
			}

		} catch (Exception e) {
			logAndDisplayError("Cannot rerun selection",
					"Cannot rerun selection\n\nPlease contact your support representative.", e);
		}
	}

	private Action hideOtherUsersResultsActionCreate() {
		final Action action = new Action("Hide other users results", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				hideOtherUsersResults = isChecked();
				viewer.refresh();
			}
		};
		action.setImageDescriptor(Activator.getImageDescriptor("icons/spectacle-lorgnette.png"));
		addAction(action);
		return action;
	}

	public void refresh() {
		reconnect();
		updateStatusBeanActions();
	}

	private void configureActionRun() {
		PropertiesDialog dialog = new PropertiesDialog(getSite().getShell(), idProperties);

		int ok = dialog.open();
		if (ok == Window.OK) {
			idProperties.clear();
			idProperties.putAll(dialog.getProps());
			reconnect();
		}
	}

	private void reconnect() {
		try {
			updateQueue();
		} catch (Exception e) {
			logger.error("Cannot resolve uri for activemq server of "+getSecondaryIdAttribute("uri"), e);
		}
	}

	private IContentProvider createContentProvider() {
		return new IStructuredContentProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				queue = (Map<String, StatusBean>)newInput;
			}

			@Override
			public void dispose() {
				if (queue!=null) queue.clear();
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (queue==null) return new StatusBean[]{StatusBean.EMPTY};
				final List<StatusBean> retained = new ArrayList<>(queue.values());

				// This preference is not secure people could hack DAWN to do this.
				if (!Boolean.getBoolean("org.dawnsci.commandserver.ui.view.showWholeQueue")) {
					// Old fashioned loop. In Java8 we will use a predicate...
					final String userName = getUserName();
					for (Iterator<StatusBean> it = retained.iterator(); it.hasNext();) {
						StatusBean statusBean = it.next();
						if (statusBean.getUserName()==null) continue;
						if (hideOtherUsersResults && !userName.equals(statusBean.getUserName())) it.remove();
					}
					// This form of filtering is not at all secure because we
					// give the full list of the queue to the clients.
				}
				// We want the newest items at the top, so reverse the list.
				Collections.reverse(retained);
				return retained.toArray(new StatusBean[retained.size()]);
			}
		};
	}

	private List<StatusBean> getSelection() {
		return Arrays.stream(((IStructuredSelection)viewer.getSelection()).toArray())
			.map(StatusBean.class::cast)
			.toList();
	}

	/**
	 * Read Queue and return in submission order.
	 */
	private synchronized void updateQueue() {
		if (logger.isTraceEnabled()) {
			logger.trace("updateQueue() called from {} (abridged)",
				Arrays.stream(Thread.currentThread().getStackTrace()).skip(2).limit(4).toList());
		}
		if (queueJob != null) {
			logger.debug("updateQueue()                   queueJob.state={} (0:None, 1:Sleeping, 2:Waiting,4:Running), thread={}, name={} {}", queueJob.getState(), queueJob.getThread(), queueJob.getName(), queueJob);
			if (queueJob.getState() != Job.RUNNING) {
				queueJob.schedule(100);
				logger.debug("updateQueue() schedule() called queueJob.state={} (0:None, 1:Sleeping, 2:Waiting,4:Running), thread={}, name={} {}", queueJob.getState(), queueJob.getThread(), queueJob.getName(), queueJob);
			} else {					// If job is already running then a call to schedule(), as above, would do
				queueJobAgain = true;	// nothing, so instead ask the job to schedule itself again after it completes.
				logger.debug("updateQueue() queueJobAgain set queueJob.state={} (0:None, 1:Sleeping, 2:Waiting,4:Running), thread={}, name={} {}", queueJob.getState(), queueJob.getThread(), queueJob.getName(), queueJob);
			}
			return;
		}

		queueJob = new UpdateQueueJob("Connect and read queue");
		queueJob.setPriority(Job.SHORT);
		queueJob.setUser(true);
		queueJob.schedule();
		logger.debug("updateQueue() scheduled as thread {}", queueJob.getThread());
	}

	private void createColumns() {
		final TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
		name.getColumn().setText("Name");
		name.getColumn().setWidth(260);
		name.setLabelProvider(new StatusQueueColumnLabelProvider(element -> ((StatusBean)element).getName(),
																 element -> getToolTipText((StatusBean)element)));

		final TableViewerColumn status = new TableViewerColumn(viewer, SWT.LEFT);
		status.getColumn().setText("Status");
		status.getColumn().setWidth(80);
		status.setLabelProvider(new StatusQueueColumnLabelProvider(element -> ((StatusBean)element).getStatus().toString()));

		final TableViewerColumn pc = new TableViewerColumn(viewer, SWT.CENTER);
		pc.getColumn().setText("Complete");
		pc.getColumn().setWidth(70);
		final NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setRoundingMode(RoundingMode.DOWN);
		pc.setLabelProvider(new StatusQueueColumnLabelProvider(element -> percentFormat.format(((StatusBean)element).getPercentComplete()/100d)));

		final TableViewerColumn submittedDate = new TableViewerColumn(viewer, SWT.CENTER);
		submittedDate.getColumn().setText("Date Submitted");
		submittedDate.getColumn().setWidth(120);
		submittedDate.setLabelProvider(new StatusQueueColumnLabelProvider(element -> DateFormat.getDateTimeInstance().format(new Date(((StatusBean)element).getSubmissionTime()))));

		final TableViewerColumn message = new TableViewerColumn(viewer, SWT.LEFT);
		message.getColumn().setText("Message");
		message.getColumn().setWidth(150);
		message.setLabelProvider(new StatusQueueColumnLabelProvider(element -> ((StatusBean)element).getMessage()));

		final TableViewerColumn location = new TableViewerColumn(viewer, SWT.LEFT);
		location.getColumn().setText("Location");
		location.getColumn().setWidth(300);
		location.setLabelProvider(new StatusQueueColumnLabelProvider(element -> getLocation((StatusBean)element)) {

			@Override
			public Color getForeground(Object element) {
				boolean isFinal = ((StatusBean) element).getStatus().isFinal();
				return getSite().getShell().getDisplay().getSystemColor(isFinal ? SWT.COLOR_BLUE : SWT.COLOR_BLACK);
			}
		});

		final TableViewerColumn host = new TableViewerColumn(viewer, SWT.CENTER);
		host.getColumn().setText("Host");
		host.getColumn().setWidth(90);
		host.setLabelProvider(new StatusQueueColumnLabelProvider(element -> ((StatusBean)element).getHostName()));

		final TableViewerColumn user = new TableViewerColumn(viewer, SWT.CENTER);
		user.getColumn().setText("User Name");
		user.getColumn().setWidth(80);
		user.setLabelProvider(new StatusQueueColumnLabelProvider(element -> ((StatusBean)element).getUserName()));

		final TableViewerColumn startTime = new TableViewerColumn(viewer, SWT.CENTER);
		startTime.getColumn().setText("Start Time");
		startTime.getColumn().setWidth(120);
		startTime.setLabelProvider(new StatusQueueColumnLabelProvider(element -> {
			long statusStartTime = ((StatusBean)element).getStartTime();
			return statusStartTime == 0 ? "" : DateFormat.getTimeInstance().format(new Date(statusStartTime));
		}));

		final TableViewerColumn estimatedEndTime = new TableViewerColumn(viewer, SWT.CENTER);
		estimatedEndTime.getColumn().setText("E. End Time");
		estimatedEndTime.getColumn().setWidth(120);
		estimatedEndTime.setLabelProvider(new StatusQueueColumnLabelProvider(element -> {
			long statusEstimatedEndTime = ((StatusBean)element).getStartTime() + ((StatusBean)element).getEstimatedTime();
			return statusEstimatedEndTime == 0 ? "" : DateFormat.getTimeInstance().format(new Date(statusEstimatedEndTime));
		}));

		MouseMoveListener cursorListener = e -> {
				Point pt = new Point(e.x, e.y);
				TableItem item = viewer.getTable().getItem(pt);

				Cursor cursor = null;
				if (item != null && item.getBounds(5).contains(pt)) {
					StatusBean statusBean = (StatusBean) item.getData();
					if (statusBean != null && getLocation(statusBean) != null && statusBean.getStatus().isFinal()) {
						cursor = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);
					}
				}
				viewer.getTable().setCursor(cursor);
			};

		viewer.getTable().addMouseMoveListener(cursorListener);

		// This is the adaptor used to open scans by clicking on the location link
		viewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) { // Primary mouse click (usually left)
					// Get the item the mouse is over
					Point pt = new Point(e.x, e.y);
					TableItem item = viewer.getTable().getItem(pt);
					// The item will be null if you click somewhere on the table but not on an item
					if (item != null) {
						// Now check if the click is in the 5th "location" column
						Rectangle rect = item.getBounds(5);
						if (rect.contains(pt)) {
							// It is in the column so get the item
							final StatusBean bean = (StatusBean) item.getData();
							// Only open it if its final i.e scan finished, opening SWMR files locally
							// from non-POSIX file systems is not good
							if (bean.getStatus().isFinal()) {
								openResultsActionRun(bean);
							}
						}
					}
				}
			}
		});
	}

	@Override
	public void setFocus() {
		if (!viewer.getTable().isDisposed()) {
			viewer.getTable().setFocus();
		}
	}

	public static String createId(final String beanBundleName, final String beanClassName, final String topicName, final String submissionQueueName) {
		final StringBuilder buf = new StringBuilder();
		buf.append(ID);
		buf.append(":");
		buf.append(QueueViews.createSecondaryId(beanBundleName, beanClassName, topicName, submissionQueueName));
		return buf.toString();
	}

	public static String createId(final String uri, final String beanBundleName, final String beanClassName, final String topicName, final String submissionQueueName) {
		final StringBuilder buf = new StringBuilder();
		buf.append(ID);
		buf.append(":");
		buf.append(QueueViews.createSecondaryId(uri, beanBundleName, beanClassName, topicName, submissionQueueName));
		return buf.toString();
	}

	private String getLocation(final StatusBean statusBean) {
		if (statusBean instanceof ScanBean scanBean) return scanBean.getFilePath();
		return statusBean.getRunDirectory();
	}

	private void warnIfDelayed(Instant jobStartTime, String beforeInterval, String afterInterval) {
		Instant timeNow = Instant.now();
		if (Duration.between(jobStartTime, timeNow).toMillis() > 100) {
			logger.warn("{} took {}ms to {}", beforeInterval, Duration.between(jobStartTime, timeNow).toMillis(), afterInterval);
		}
	}

	/**
	 * A {@link Job} to fetch the queue from the server and update the viewer in the UI.
	 */
	private final class UpdateQueueJob extends Job {

		private UpdateQueueJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				logger.debug("updateQueue Job run({})", monitor);

				final Instant jobStartTime = Instant.now();
				monitor.beginTask("Connect to command server", 10);
				updateProgress(jobStartTime, monitor, "Queue connection set");

				runList = jobQueueProxy.getRunningAndCompleted(idProperties.getProperty("maxQueueSize"));
				updateProgress(jobStartTime, monitor, "List of running and completed jobs retrieved");

				submittedList = jobQueueProxy.getSubmissionQueue();
				updateProgress(jobStartTime, monitor, "List of submitted jobs retrieved");

				// We leave the list in reverse order so we can insert entries at the start by adding to the end
				final Map<String,StatusBean> statusQueue = new LinkedHashMap<>();
				for (StatusBean bean : runList) {
					statusQueue.put(bean.getUniqueId(), bean);
				}
				updateProgress(jobStartTime, monitor, "Run/running jobs added to view list");

				for (StatusBean bean : submittedList) {
					if (bean.getStatus().isPaused()) bean.setStatus(org.eclipse.scanning.api.event.status.Status.DEFERRED);
					if (bean.getStatus().isRequest()) bean.setStatus(org.eclipse.scanning.api.event.status.Status.SUBMITTED);
					statusQueue.put(bean.getUniqueId(), bean);
				}
				updateProgress(jobStartTime, monitor, "Submitted jobs added to view list");

				getDisplay().syncExec(() -> {
					warnIfDelayed(jobStartTime, "updateQueue Job run() syncExec()", "start");
					viewer.setInput(statusQueue);
					warnIfDelayed(jobStartTime, "updateQueue Job run() syncExec()", "setInput complete");
					viewer.refresh();
					warnIfDelayed(jobStartTime, "updateQueue Job run() syncExec()", "refresh complete");
					updateStatusBeanActions();
					warnIfDelayed(jobStartTime, "updateQueue Job run() syncExec()", "updateStatusBeanActions complete");
				});

				// update actions that require the queue status
				Job.create("Getting queue status", mon -> {
					// get the status first in a non-UI thread as it can take time, potentially causing the UI to freeze
					final QueueStatus queueStatus = jobQueueProxy.getQueueStatus();
					getDisplay().asyncExec(() -> suspendQueueActionUpdate(queueStatus)); // update actions in UI thread
				}).schedule();

				// Given that these lists could be large, only summarise the count of beans with each status in each list.
				logger.info("updateQueue Job run({}) completed, submittedList beans {}, runningList beans {}", monitor,
						submittedList.stream().collect(Collectors.groupingBy(StatusBean::getStatus, Collectors.counting())),
							  runList.stream().collect(Collectors.groupingBy(StatusBean::getStatus, Collectors.counting())));
				monitor.done();

				return Status.OK_STATUS;
			} catch (final Exception e) {
				monitor.done();
				logAndDisplayError("Cannot connect to queue",
						"The server is unavailable at "+getUriString()+".\n\nPlease contact your support representative.",
						e, "Updating changed bean from topic");
				return Status.CANCEL_STATUS;
			} finally {
				if (queueJobAgain) {
					queueJobAgain = false;
					schedule(100);
				}
			}
		}

		private void updateProgress(Instant jobStartTime, IProgressMonitor monitor, String subTask) {
			monitor.subTask(subTask);
			monitor.worked(1);
			warnIfDelayed(jobStartTime, "updateQueue Job progress", subTask);
		}
	}

	private Display getDisplay() {
		return getSite().getShell().getDisplay();
	}

	/**
	 * Extend ColumnLabelProvider to provide a tool tip for each column
	 * <p>
	 * There is a bug in Eclipse Mars which means that the tool tip flickers and disappears if it extends beyond the
	 * edge of the screen. We believe that this is fixed in later versions of the Eclipse platform. For the time being,
	 * wrap the tool tip to make this less likely to happen.
	 */
	private class StatusQueueColumnLabelProvider extends ColumnLabelProvider {
		private static final int WRAP_LENGTH = 200;
		private Function<Object, String> getTextMethod;
		private Function<Object, String> getToolTipTextMethod;

		public StatusQueueColumnLabelProvider(Function<Object, String> getTextMethod) {
			this(getTextMethod, null);
		}

		public StatusQueueColumnLabelProvider(Function<Object, String> getTextMethod, Function<Object, String> getToolTipTextMethod) {
			super();
			this.getTextMethod = getTextMethod;
			this.getToolTipTextMethod = getToolTipTextMethod;
		}

		@Override
		public String getText(Object element)
		{
			String text = null;
			if (getTextMethod != null) {
				try {
					text =  getTextMethod.apply(element);
				} catch (Exception e) {
					text = e.getMessage();
				}
			}
			return text;
		}

		@Override
		public String getToolTipText(Object element) {
			String toolTipText = "";

			if (getToolTipTextMethod != null) {
				toolTipText =  getToolTipTextMethod.apply(element);
			}
			else {
				toolTipText =  getTextMethod.apply(element);
			}

			return WordUtils.wrap(toolTipText, WRAP_LENGTH, null, true);
		}
	}

	@Override
	protected String getSecondaryIdAttribute(String key) {
		if (idProperties == null) idProperties = parseString(DEFAULT_QUEUE_ARGUMENTS);
		if (super.getSecondaryIdAttribute(key) == null) {
			// If secondaryId doesn't contain field/secondaryId is null, return default
			return (String) parseString(DEFAULT_QUEUE_ARGUMENTS).get(key);
		}
		return super.getSecondaryIdAttribute(key);
	}

	private Action createAction(QueueAction actionElement, Runnable runnable) {
		var action = new RunnableAction(actionElement.getLabel(), actionElement.getImageName(), runnable);
		action.setEnabled(false);
		addAction(action);
		return action;
	}

	private Action createAction(QueueAction actionElement, Runnable runnable, int style) {
		var action = new RunnableAction(actionElement.getLabel(), actionElement.getImageName(), runnable, style);
		action.setEnabled(false);
		action.setChecked(false);
		addAction(action);
		return action;
	}

	public String getToolTipText(StatusBean statusBean) {
		return toolTipTextProvider.summarise(statusBean);
	}

	private void logAndDisplayError(String dialogTitle, String errorMessage, Exception e) {
		logAndDisplayError(dialogTitle, errorMessage, e, errorMessage);
	}

	private void logAndDisplayError(String dialogTitle, String errorMessage, Exception e, String logMessage) {
		logger.error(logMessage, e);
		getDisplay().asyncExec(() ->
			ErrorDialog.openError(getViewSite().getShell(), dialogTitle, errorMessage,
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage())));
	}
}
