/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.event.ui.view;

import static java.util.stream.Collectors.toList;

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
import org.eclipse.scanning.api.event.status.AdministratorMessage;
import org.eclipse.scanning.api.event.status.OpenRequest;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.IModifyHandler;
import org.eclipse.scanning.api.ui.IRerunHandler;
import org.eclipse.scanning.api.ui.IResultHandler;
import org.eclipse.scanning.event.ui.Activator;
import org.eclipse.scanning.event.ui.ServiceHolder;
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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final String MODIFY_HANDLER_EXTENSION_POINT_ID = "org.eclipse.scanning.api.modifyHandler";

	private static final String RESULTS_HANDLER_EXTENSION_POINT_ID = "org.eclipse.scanning.api.resultsHandler";

	public static final String ID = "org.eclipse.scanning.event.ui.queueView";

	private static final Logger logger = LoggerFactory.getLogger(StatusQueueView.class);

	private static final String DEFAULT_QUEUE_ARGUMENTS = createId(
					"org.eclipse.scanning.api",
					StatusBean.class.getSimpleName(),
					EventConstants.STATUS_TOPIC,
					EventConstants.SUBMISSION_QUEUE);

	private static final String SUSPEND_QUEUE = "Suspend queueing of upcoming jobs\nDoes not pause current job";
	private static final String UNSUSPEND_QUEUE = "Unsuspend queueing of upcoming jobs\nDoes not undefer upcoming job(s)";
	private static final String SUSPEND_QUEUE_ICON = "icons/switch-queue-on.png";
	private static final String UNSUSPEND_QUEUE_ICON = "icons/switch-queue-off.png";

	// UI
	private TableViewer viewer;
	private DelegatingSelectionProvider selectionProvider;
	private Job queueJob;
	private boolean queueJobAgain = false;

	// Data
	private Map<String, StatusBean> queue;
	private List<StatusBean> runList = new ArrayList<>();
	private List<StatusBean> submittedList =  new ArrayList<>();
	private boolean hideOtherUsersResults = false;

	private ISubscriber<IBeanListener<StatusBean>> statusTopicSubscriber;
	private ISubscriber<IBeanListener<AdministratorMessage>> adminTopicSubscriber;
	private IJobQueue<StatusBean> jobQueueProxy;

	// Actions on a specific item (StatusBean) in the queue
	private Action openResultsAction;
	private Action rerunAction;
	private Action editAction;
	private Action removeAction;
	private Action upAction;
	private Action downAction;
	private Action pauseAction;
	private Action deferAction;
	private Action stopAction;
	private Action openAction;
	private Action detailsAction;

	// Actions on the whole queue
	private Action clearQueueAction;
	private Action suspendQueueAction;

	private IEventService service;

	private List<IResultHandler<StatusBean>> resultsHandlers = null;

	protected IBeanSummariser toolTipTextProvider;

	public StatusQueueView() {
		this.service = ServiceHolder.getEventService();

		toolTipTextProvider = ServiceHolder.getBeanSummariser();
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
			logger.error("Cannot create proxy to queue {}", getSubmissionQueueName(), e);
		}

		try {
			updateQueue();

			String name = getSecondaryIdAttribute("partName");
			if (name!=null) setPartName(name);

			createActions();

			// We just use this submitter to read the queue
			createTopicListener(getUri());

		} catch (Exception e) {
			logger.error("Cannot listen to topic of command server!", e);
		}

		selectionProvider = new DelegatingSelectionProvider(viewer);
		getViewSite().setSelectionProvider(selectionProvider);
		viewer.addSelectionChangedListener(event -> updateStatusBeanActions());
	}

	private void updateStatusBeanActions() {
		final List<StatusBean> selection = getSelection();
		final List<String> selectedUniqueIds = selection.stream().map(StatusBean::getUniqueId).collect(toList());
		final List<StatusBean> selectedInSubmittedList = submittedList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.collect(toList());
		final List<StatusBean> selectedInRunList = runList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.collect(toList());

		final boolean activeScanSelected = selectedInRunList.stream().anyMatch(st -> st.getStatus().isActive());
		final boolean activeScanPaused = selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isPaused());
		final boolean anyFinalSelectedInRunList = selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isFinal());
		final boolean anySelectedInSubmittedList = !selectedInSubmittedList.isEmpty();
		final boolean anyNonFinalInSubmittedList = selectedInSubmittedList.stream().anyMatch(sb -> !sb.getStatus().isFinal());
		final boolean allSelectedSubmittedDeferred = selectedInSubmittedList.stream().allMatch(
				sb -> sb.getStatus().isPaused() || sb.getStatus().isFinal());

		removeActionUpdate(selectedInSubmittedList, selectedInRunList);
		rerunActionUpdate(selection);
		upActionUpdate(anySelectedInSubmittedList);
		editActionUpdate(anySelectedInSubmittedList);
		downActionUpdate(anySelectedInSubmittedList);

		stopActionUpdate(selectedInRunList);
		pauseActionUpdate(activeScanSelected, activeScanPaused);
		deferActionUpdate(anyNonFinalInSubmittedList, anyNonFinalInSubmittedList && allSelectedSubmittedDeferred);

		openResultsActionUpdate(anyFinalSelectedInRunList);
		openActionUpdate(selection);
		detailsActionUpdate(selectedInSubmittedList, selectedInRunList);

		// Some sanity checks
		warnIfListContainsStatus("null status found in selection:       ", selection, null);
		warnIfListContainsStatus("RUNNING status found in submittedList: ", submittedList, org.eclipse.scanning.api.event.status.Status.RUNNING);
		warnIfListContainsStatus("SUBMITTED status found in runList:       ", runList, org.eclipse.scanning.api.event.status.Status.SUBMITTED);
		warnIfListContainsStatus("DEFERRED status found in runList:       ", runList, org.eclipse.scanning.api.event.status.Status.DEFERRED);
	}

	private void updateQueueStatusActions(QueueStatus status) {
		suspendQueueAction.setChecked(status == QueueStatus.PAUSED);
	}

	private void warnIfListContainsStatus(String description, List<StatusBean> list, org.eclipse.scanning.api.event.status.Status status) {
		List<StatusBean> matchingStatusList = list.stream()
				.filter(x -> x.getStatus()==status)
				.collect(Collectors.toList());

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

					adminTopicSubscriber = service.createSubscriber(uri, EventConstants.ADMIN_MESSAGE_TOPIC);
					adminTopicSubscriber.addListener(evt -> {
							final AdministratorMessage bean = evt.getBean();
							getSite().getShell().getDisplay().syncExec(() -> {
								MessageDialog.openError(getViewSite().getShell(), bean.getTitle(), bean.getMessage());
								viewer.refresh();
							});
						});
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
			if (adminTopicSubscriber!=null) adminTopicSubscriber.disconnect();
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
		final IContributionManager toolMan  = getViewSite().getActionBars().getToolBarManager();
		final IContributionManager dropDown = getViewSite().getActionBars().getMenuManager();
		final MenuManager          menuMan = new MenuManager();

		openResultsAction = openResultsActionCreate();
		addActionTo(toolMan, menuMan, dropDown, openResultsAction);

		addSeparators(toolMan, menuMan, dropDown);

		upAction = upActionCreate();
		addActionTo(toolMan, menuMan, dropDown, upAction);

		downAction = downActionCreate();
		addActionTo(toolMan, menuMan, dropDown, downAction);

		deferAction = deferActionCreate();
		addActionTo(toolMan, menuMan, dropDown, deferAction);

		pauseAction = pauseActionCreate();
		addActionTo(toolMan, menuMan, dropDown, pauseAction);

		stopAction = stopActionCreate();
		addActionTo(toolMan, menuMan, dropDown, stopAction);

		suspendQueueAction = suspendQueueActionCreate();
		addActionTo(toolMan, menuMan, dropDown, suspendQueueAction);

		removeAction = removeActionCreate();
		addActionTo(toolMan, menuMan, dropDown, removeAction);

		rerunAction = rerunActionCreate();
		addActionTo(toolMan, menuMan, dropDown, rerunAction);

		openAction = openActionCreate();
		addActionTo(toolMan, menuMan, dropDown, openAction);

		editAction = editActionCreate();
		addActionTo(toolMan, menuMan, dropDown, editAction);

		detailsAction = detailsActionCreate();
		addActionTo(toolMan, menuMan, dropDown, detailsAction);

		addSeparators(toolMan, menuMan, dropDown);

		final Action hideOtherUsersResultsAction = hideOtherUsersResultsActionCreate();
		addActionTo(toolMan, menuMan, dropDown, hideOtherUsersResultsAction);

		addSeparators(toolMan, menuMan, dropDown);

		final Action refreshAction = refreshActionCreate();
		addActionTo(toolMan, menuMan, dropDown, refreshAction);
		jobQueueProxy.addQueueStatusListener(newStatus -> {
			if (newStatus.equals(QueueStatus.MODIFIED)) {
				refreshAction.run();
			}
		});

		final Action configureAction = configureActionCreate();
		addActionTo(toolMan, menuMan, dropDown, configureAction);

		addSeparators(null, menuMan, dropDown);

		clearQueueAction = clearQueueActionCreate();
		addActionTo(null, menuMan, dropDown, clearQueueAction);

		viewer.getControl().setMenu(menuMan.createContextMenu(viewer.getControl()));
	}

	private void addActionTo(IContributionManager toolMan, MenuManager menuMan, IContributionManager dropDown, Action action) {
		if (toolMan != null)  toolMan.add(action);
		if (menuMan != null)  menuMan.add(action);
		if (dropDown != null) dropDown.add(action);
	}

	private void addSeparators(IContributionManager toolMan, MenuManager menuMan, IContributionManager dropDown) {
		if (toolMan != null)  toolMan.add(new Separator());
		if (menuMan != null)  menuMan.add(new Separator());
		if (dropDown != null) dropDown.add(new Separator());
	}

	private void upActionUpdate(boolean anySelectedInSubmittedList) {
		upAction.setEnabled(anySelectedInSubmittedList);
	}

	private Action upActionCreate() {
		Action action = new Action("Move backward (run later)", Activator.getImageDescriptor("icons/arrow-090.png")) {
			@Override
			public void run() {
				for(StatusBean bean : getSelection()) {
					try {
						if (bean.getStatus() == org.eclipse.scanning.api.event.status.Status.SUBMITTED) {
							jobQueueProxy.moveBackward(bean);
						} else {
							logger.info("Cannot move {} up as it's status ({}) is not SUBMITTED", bean.getName(), bean.getStatus());
						}
					} catch (EventException e) {
						ErrorDialog.openError(getViewSite().getShell(), "Cannot move "+bean.getName()+" up",
							"'"+bean.getName()+"' cannot be moved in the submission queue.",
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
					}
				}
				refresh();
			}

		};
		action.setEnabled(false);
		return action;
	}

	private void downActionUpdate(boolean anySelectedInSubmittedList) {
		downAction.setEnabled(anySelectedInSubmittedList);
	}

	private Action downActionCreate() {
		final Action action = new Action("More forward (run earlier)", Activator.getImageDescriptor("icons/arrow-270.png")) {
			@Override
			public void run() {
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
						ErrorDialog.openError(getViewSite().getShell(), "Cannot move "+bean.getName()+" down",
								"'"+bean.getName()+"' cannot be moved in the submission queue.",
								new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
					}
				}
				refresh();
			}
		};
		action.setEnabled(false);
		return action;
	}

	private Action suspendQueueActionCreate() {
		final boolean isChecked = jobQueueProxy.isPaused();
		final Action action = new Action(isChecked ? UNSUSPEND_QUEUE : SUSPEND_QUEUE, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				suspendQueueActionRun(this);
			}
		};
		action.setImageDescriptor(Activator.getImageDescriptor(isChecked ? UNSUSPEND_QUEUE_ICON  : SUSPEND_QUEUE_ICON));
		action.setChecked(isChecked);

		jobQueueProxy.addQueueStatusListener(this::updateQueueStatusActions);
		return action;
	}

	private void suspendQueueActionRun(IAction suspendQueue) {

		// The button can get out of sync if two clients are used.
		final boolean queueSuspended = jobQueueProxy.isPaused();
		try {
			suspendQueue.setChecked(!queueSuspended); // We are toggling it.
			suspendQueue.setText(queueSuspended ? SUSPEND_QUEUE : UNSUSPEND_QUEUE);
			suspendQueue.setImageDescriptor(Activator.getImageDescriptor(queueSuspended ? SUSPEND_QUEUE_ICON : UNSUSPEND_QUEUE_ICON));
			if (queueSuspended) {
				jobQueueProxy.resume();
			} else {
				jobQueueProxy.pause();
			}
		} catch (Exception e) {
			ErrorDialog.openError(getViewSite().getShell(), "Cannot pause queue "+getSubmissionQueueName(),
				"Cannot pause queue "+getSubmissionQueueName()+"\n\nPlease contact your support representative.",
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		suspendQueue.setChecked(jobQueueProxy.isPaused());
	}

	private void pauseActionUpdate(boolean activeScanSelected, boolean activeScanPaused) {
		pauseAction.setEnabled(activeScanSelected);
		pauseAction.setChecked(activeScanPaused);
		pauseAction.setText(activeScanPaused ? "Resume job" :"Pause job");
	}

	private Action pauseActionCreate() {
		final Action action = new Action("Pause job", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				pauseActionRun();
			}
		};
		action.setImageDescriptor(Activator.getImageDescriptor("icons/control-pause.png"));
		action.setEnabled(false);
		action.setChecked(false);
		return action;
	}

	private void pauseActionRun() {
		pauseAction.setEnabled(false);

		final List<String> selectedUniqueIds= getSelection().stream().map(StatusBean::getUniqueId).collect(toList());
		final List<StatusBean> selectedInRunList = runList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.collect(toList());

		for(StatusBean bean : selectedInRunList) {
			if (bean.getStatus().isFinal()) continue;

			try {
				// Invert as JFace bindings toggle state first...
				if (!pauseAction.isChecked()) {
					jobQueueProxy.resumeJob(bean);
				} else {
					jobQueueProxy.pauseJob(bean);
				}
			} catch (Exception e) {
				ErrorDialog.openError(getViewSite().getShell(), "Cannot pause "+bean.getName(),
					"Cannot pause "+bean.getName()+"\n\nPlease contact your support representative.",
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}
		}
	}

	private void deferActionUpdate(boolean deferableSelectedInSubmittedList, boolean allSelectedSubmittedDeferred) {
		deferAction.setEnabled(deferableSelectedInSubmittedList);
		deferAction.setChecked(allSelectedSubmittedDeferred);
		deferAction.setText(allSelectedSubmittedDeferred ? "Undefer job(s)" :"Defer job(s)");
	}

	private Action deferActionCreate() {
		final Action action = new Action("Defer submitted scan(s) until undefered", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				deferActionRun();
			}
		};
		action.setImageDescriptor(Activator.getImageDescriptor("icons/alarm-clock-select.png"));
		action.setEnabled(false);
		action.setChecked(false);
		return action;
	}

	private void deferActionRun() {
		final List<String> selectedUniqueIds = getSelection().stream().map(StatusBean::getUniqueId).collect(toList());
		final List<StatusBean> selectedInSubmittedList = submittedList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.collect(toList());

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
				ErrorDialog.openError(getViewSite().getShell(), "Cannot defer "+bean.getName(),
					"Cannot defer "+bean.getName()+"\n\nPlease contact your support representative.",
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}
		}

	}

	private void removeActionUpdate(List<StatusBean> selectedInSubmittedList, List<StatusBean> selectedInRunList) {
		final boolean anySelectedInSubmittedList = !selectedInSubmittedList.isEmpty();
		final boolean anySelectedInRunListAreFinal = selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isFinal());

		removeAction.setEnabled(anySelectedInSubmittedList || anySelectedInRunListAreFinal);
	}

	private Action removeActionCreate() {
		final Action action = new Action("Remove job", PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE)) {
			@Override
			public void run() {
				removeActionRun();
			}
		};
		action.setEnabled(false);
		return action;
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
					ErrorDialog.openError(getViewSite().getShell(), "Cannot delete "+bean.getName(),
						"Cannot delete "+bean.getName()+"\n\nIt might have changed state at the same time and being remoted.",
						new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			} else {
				MessageDialog.openWarning(getViewSite().getShell(), "Cannot remove running job",
						"Cannot delete "+bean.getName()+" as it is currently active.");
			}
		}
	}

	private void stopActionUpdate(List<StatusBean> selectedInRunList) {
		stopAction.setEnabled(selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isActive()));
	}

	private Action stopActionCreate() {
		final Action action = new Action("Stop job", Activator.getImageDescriptor("icons/control-stop-square.png")) {
			@Override
			public void run() {
				stopActionRun();
			}
		};
		action.setEnabled(false);
		return action;
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
				ErrorDialog.openError(getViewSite().getShell(), "Cannot terminate "+bean.getName(), "Cannot terminate "+bean.getName()+"\n\nPlease contact your support representative.",
						new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}
			} else {
				MessageDialog.openWarning(getViewSite().getShell(), "Cannot stop inactive job",
						"Cannot stop "+bean.getName()+" as it is not currently active.");
			}
		}
	}

	private void clearQueueActionUpdate() {
		clearQueueAction.setEnabled(!submittedList.isEmpty() || !runList.isEmpty());
	}

	private Action clearQueueActionCreate() {
		return new Action("Clear Queue", PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_REMOVEALL)) {
			@Override
			public void run() {
				try {
					clearQueueActionRun();
				} catch (EventException e) {
					logger.error("Canot purge queues", e);
				}
			}
		};
	}

	private void clearQueueActionRun() throws EventException {

		boolean ok = MessageDialog.openQuestion(getSite().getShell(), "Confirm Clear Queues",
			"Are you sure you would like to remove all items from the queue "+getQueueName()+" and "+
			getSubmissionQueueName()+"?\n\nThis could abort or disconnect runs of other users.");
		if (!ok) return;
		boolean terminateRunningScan = runList.stream().noneMatch(b -> b.getStatus().isActive()) ||
				MessageDialog.openQuestion(getSite().getShell(),
				"Confirm scan termination", "Would you like to terminate the currently active scan?");

		jobQueueProxy.clearQueue();
		jobQueueProxy.clearRunningAndCompleted(terminateRunningScan);

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
					ErrorDialog.openError(getSite().getShell(), "Internal Error",
							"Could not create results handler for class " + configElement.getAttribute("class") +
							".\n\nPlease contact your support representative.",
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
			handlers.add(new DefaultResultsHandler());
			resultsHandlers = handlers;
		}
		return resultsHandlers;
	}

	private void openResultsActionUpdate(boolean anyFinalSelectedInRunList) {
		openResultsAction.setEnabled(anyFinalSelectedInRunList);
	}

	private Action openResultsActionCreate() {
		return new Action("Open results for selected run", Activator.getImageDescriptor("icons/results.png")) {
			@Override
			public void run() {
				for (StatusBean bean : getSelection()) {
					openResultsActionRun(bean);
				}
			}
		};
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
					ErrorDialog.openError(getSite().getShell(), "Internal Error", handler.getErrorMessage(null),
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
		}
	}

	private void openActionUpdate(List<StatusBean> selection) {
		// Enable only if only one job is selected (we don't care about this job's status)
		openAction.setEnabled(selection.size() == 1);
	}

	private Action openActionCreate() {
		return new Action("Open...", Activator.getImageDescriptor("icons/application-dock-090.png")) {
			@Override
			public void run() {
				openActionRun();
			}
		};
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

	private void editActionUpdate(boolean anySelectedInSubmittedList) {
		editAction.setEnabled(anySelectedInSubmittedList);
	}

	private Action editActionCreate() {
		final Action action = new Action("Edit...", Activator.getImageDescriptor("icons/modify.png")) {
			@Override
			public void run() {
				editActionRun();
			}
		};
		action.setEnabled(false);
		return action;
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

	private Action detailsActionCreate() {
		return new Action("Show details...", Activator.getImageDescriptor("icons/clipboard-list.png")) {
			@Override
			public void run() {
				final ScanDetailsDialog detailsDialog = new ScanDetailsDialog(getSite().getShell(), getSelection().get(0));
				detailsDialog.setBlockOnOpen(true);
				detailsDialog.open();
			}
		};
	}

	/**
	 * Edits a not run yet selection
	 */
	private void editActionRun() {
		for (StatusBean bean : getSelection()) {
			if (bean.getStatus() != org.eclipse.scanning.api.event.status.Status.SUBMITTED) {
				MessageDialog.openConfirm(getSite().getShell(), "Cannot Edit '"+bean.getName()+"'",
					"The run '"+bean.getName()+"' cannot be edited because it is not waiting to run.");
			} else {
				try {
					final IConfigurationElement[] c = Platform.getExtensionRegistry().getConfigurationElementsFor(MODIFY_HANDLER_EXTENSION_POINT_ID);
					boolean edited = false;
					for (IConfigurationElement i : c) {
						@SuppressWarnings("unchecked")
						final IModifyHandler<StatusBean> handler = (IModifyHandler<StatusBean>) i.createExecutableExtension("class");
						handler.init(service, createJobQueueConfiguration());
						if (handler.isHandled(bean)) {
							edited = handler.modify(bean);
							break;
						}
					}
					if (!edited) {
						MessageDialog.openConfirm(getSite().getShell(), "Cannot Edit '"+bean.getName()+"'",
								"There are no editors registered for '"+bean.getName()+"'\n\nPlease contact your support representative.");
					}
				} catch (Exception ne) {
					final String err = "Cannot modify "+bean.getRunDirectory()+" normally.\n\nPlease contact your support representative.";
					logger.error(err, ne);
					ErrorDialog.openError(getSite().getShell(), "Internal Error", err, new Status(IStatus.ERROR, Activator.PLUGIN_ID, ne.getMessage()));
				}
			}
		}
	}

	private void rerunActionUpdate(List<StatusBean> selection) {
		rerunAction.setEnabled(!selection.isEmpty());
	}

	private Action rerunActionCreate() {
		final Action action = new Action("Rerun...", Activator.getImageDescriptor("icons/rerun.png")) {
			@Override
			public void run() {
				rerunActionRun();
			}
		};
		action.setEnabled(false);
		return action;
	}

	private void rerunActionRun() {
		for (StatusBean bean : getSelection()) {
			boolean handled = false;
			try {
				final IConfigurationElement[] c = Platform.getExtensionRegistry().getConfigurationElementsFor(RERUN_HANDLER_EXTENSION_POINT_ID);
				for (IConfigurationElement i : c) {
					@SuppressWarnings("unchecked")
					final IRerunHandler<StatusBean> handler = (IRerunHandler<StatusBean>) i.createExecutableExtension("class");
					handler.init(service, createJobQueueConfiguration());
					if (handler.isHandled(bean)) {
						final StatusBean copy = bean.getClass().getDeclaredConstructor().newInstance();
						copy.merge(bean);
						copy.setUniqueId(UUID.randomUUID().toString());
						copy.setStatus(org.eclipse.scanning.api.event.status.Status.SUBMITTED);
						copy.setSubmissionTime(System.currentTimeMillis());
						handled = handler.run(copy);
						if (handled) break;
					}
				}
			} catch (Exception ne) {
				final String err = "Cannot rerun "+bean.getRunDirectory()+" normally.\n\nPlease contact your support representative.";
				logger.error(err, ne);
				ErrorDialog.openError(getSite().getShell(), "Internal Error", err, new Status(IStatus.ERROR, Activator.PLUGIN_ID, ne.getMessage()));
				continue;
			}
			if (!handled) {
				// If we have not already handled this rerun, it is possible to call a generic one.
				rerun(bean);
			}
		}
	}

	private JobQueueConfiguration createJobQueueConfiguration() throws Exception {
		return new JobQueueConfiguration(getUri(), getSubmissionQueueName(), getTopicName(), getQueueName());
	}

	private void rerun(StatusBean bean) {

		try {
			final DateFormat format = DateFormat.getDateTimeInstance();
			boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm resubmission "+bean.getName(),
					  "Are you sure you want to rerun "+bean.getName()+" submitted on "+format.format(new Date(bean.getSubmissionTime()))+"?");

			if (!ok) return;

			final StatusBean copy = bean.getClass().getDeclaredConstructor().newInstance();
			copy.merge(bean);
			copy.setUniqueId(UUID.randomUUID().toString());
			copy.setMessage("Rerun of "+bean.getName());
			copy.setStatus(org.eclipse.scanning.api.event.status.Status.SUBMITTED);
			copy.setPercentComplete(0.0);
			copy.setSubmissionTime(System.currentTimeMillis());

			jobQueueProxy.submit(copy);

			// Do not need to reconnect, just add to our local list
			mergeBean(copy);

		} catch (Exception e) {
			ErrorDialog.openError(getViewSite().getShell(), "Cannot rerun "+bean.getName(), "Cannot rerun "+bean.getName()+"\n\nPlease contact your support representative.",
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
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
		return action;
	}

	private Action refreshActionCreate() {
		return new Action("Refresh", Activator.getImageDescriptor("icons/arrow-circle-double-135.png")) {
			@Override
			public void run() {
				reconnect();
			}
		};
	}

	public void refresh() {
		reconnect();
		updateStatusBeanActions();
	}

	private Action configureActionCreate() {
		return new Action("Configure...", Activator.getImageDescriptor("icons/document--pencil.png")) {
			@Override
			public void run() {
				PropertiesDialog dialog = new PropertiesDialog(getSite().getShell(), idProperties);

				int ok = dialog.open();
				if (ok == Window.OK) {
					idProperties.clear();
					idProperties.putAll(dialog.getProps());
					reconnect();
				}
			}
		};
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
			.collect(toList());
	}

	/**
	 * Read Queue and return in submission order.
	 */
	private synchronized void updateQueue() {
		if (logger.isTraceEnabled()) {
			logger.trace("updateQueue() called from {} (abridged)",
				Arrays.stream(Thread.currentThread().getStackTrace()).skip(2).limit(4).collect(Collectors.toList()));
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
		if (statusBean instanceof ScanBean) return ((ScanBean)statusBean).getFilePath();
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
					getDisplay().asyncExec(() -> updateQueueStatusActions(queueStatus)); // update actions in UI thread
				}).schedule();

				// Given that these lists could be large, only summarise the count of beans with each status in each list.
				logger.info("updateQueue Job run({}) completed, submittedList beans {}, runningList beans {}", monitor,
						submittedList.stream().collect(Collectors.groupingBy(StatusBean::getStatus, Collectors.counting())),
							  runList.stream().collect(Collectors.groupingBy(StatusBean::getStatus, Collectors.counting())));
				monitor.done();

				return Status.OK_STATUS;
			} catch (final Exception e) {
				monitor.done();
				logger.error("Updating changed bean from topic", e);
				getSite().getShell().getDisplay().syncExec(() ->
						ErrorDialog.openError(getViewSite().getShell(), "Cannot connect to queue", "The server is unavailable at "+getUriString()+".\n\nPlease contact your support representative.",
								new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()))
					);
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

	public String getToolTipText(StatusBean statusBean) {
		return toolTipTextProvider.summarise(statusBean);
	}
}
