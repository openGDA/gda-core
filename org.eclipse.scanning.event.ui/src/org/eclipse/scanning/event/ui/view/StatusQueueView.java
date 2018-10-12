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
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ConsumerConfiguration;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.QueueViews;
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
import org.osgi.framework.Bundle;
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
@SuppressWarnings({"squid:S1192", "squid:S1659"})
public class StatusQueueView extends EventConnectionView {

	private static final String RERUN_HANDLER_EXTENSION_POINT_ID = "org.eclipse.scanning.api.rerunHandler";

	private static final String MODIFY_HANDLER_EXTENSION_POINT_ID = "org.eclipse.scanning.api.modifyHandler";

	private static final String RESULTS_HANDLER_EXTENSION_POINT_ID = "org.eclipse.scanning.api.resultsHandler";

	public static final String ID = "org.eclipse.scanning.event.ui.queueView";

	private static final Logger logger = LoggerFactory.getLogger(StatusQueueView.class);

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

	private ISubscriber<IBeanListener<StatusBean>> topicMonitor;
	private ISubscriber<IBeanListener<AdministratorMessage>> adminMonitor;
	private ISubmitter<StatusBean> submitter;
	private IPublisher<StatusBean> statusTopicPublisher;
	private IConsumer<StatusBean> consumerProxy;

	private Action openResultsAction;
	private Action rerunAction;
	private Action editAction;
	private Action removeAction;
	private Action upAction;
	private Action downAction;
	private Action pauseAction;
	private Action stopAction;
	private Action openAction;
	private Action detailsAction;
	private Action clearQueueAction;

	private IEventService service;

	private List<IResultHandler> resultsHandlers = null;

	public StatusQueueView() {
		this.service = ServiceHolder.getEventService();
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
			consumerProxy = service.createConsumerProxy(getUri(), getSubmissionQueueName(), EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		} catch (Exception e) {
			logger.error("Cannot create proxy to queue {}", getSubmissionQueueName(), e);
		}

		try {
			submitter = service.createSubmitter(getUri(), getSubmissionQueueName());
			submitter.setStatusTopicName(getTopicName());
			updateQueue();

			String name = getSecondaryIdAttribute("partName");
			if (name!=null) setPartName(name);

			createActions();

			// We just use this submitter to read the queue
			createTopicListener(getUri());

		} catch (Exception e) {
			logger.error("Cannot listen to topic of command server!", e);
		}

		try {
			statusTopicPublisher = service.createPublisher(new URI(Activator.getJmsUri()), EventConstants.STATUS_TOPIC);
		} catch (Exception e) {
			logger.error("Cannot create publisher to status topic", e);
		}


		selectionProvider = new DelegatingSelectionProvider(viewer);
		getViewSite().setSelectionProvider(selectionProvider);
		viewer.addSelectionChangedListener(event -> updateActions() );
	}

	private void updateActions() {
		List<StatusBean> selection = getSelection();

		List<String> selectedUniqueIds= selection.stream().map(sb -> sb.getUniqueId()).collect(Collectors.toList());

		List<StatusBean> selectedInSubmittedList = submittedList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.collect(Collectors.toList());

		List<StatusBean> selectedInRunList = runList.stream()
				.filter(sb -> selectedUniqueIds.contains(sb.getUniqueId()))
				.collect(Collectors.toList());

		List<StatusBean> activeInRunList = runList.stream()
				.filter(sb -> sb.getStatus().isActive())
				.collect(Collectors.toList());

		boolean anyFinalSelectedInRunList = selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isFinal());
		boolean anySelectedInSubmittedList = !selectedInSubmittedList.isEmpty();

		removeActionUpdate(selectedInSubmittedList, selectedInRunList);
		rerunActionUpdate(selection);
		upActionUpdate(anySelectedInSubmittedList);
		editActionUpdate(anySelectedInSubmittedList);
		downActionUpdate(anySelectedInSubmittedList);

		boolean anyRunning = queue.values().stream().anyMatch(x -> x.getStatus().isRunning());
		boolean anyPaused = queue.values().stream().anyMatch(x -> x.getStatus().isPaused() );

		stopActionUpdate(selectedInRunList);
		pauseActionUpdate(anyRunning, anyPaused, anySelectedInSubmittedList);

		openResultsActionUpdate(anyFinalSelectedInRunList);
		openActionUpdate(selection);
		detailsActionUpdate(selectedInSubmittedList, selectedInRunList);

		// Some sanity checks
		warnIfListContainsStatus("null status found in selection:       ", selection,     null);
		warnIfListContainsStatus("queued status found in submittedList: ", submittedList, org.eclipse.scanning.api.event.status.Status.QUEUED);
		warnIfListContainsStatus("queued status found in runList:       ", runList,       org.eclipse.scanning.api.event.status.Status.QUEUED);
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
					topicMonitor = service.createSubscriber(uri, getTopicName());
					topicMonitor.addListener(evt -> {
							final StatusBean bean = evt.getBean();
							try {
								mergeBean(bean);
							} catch (Exception e) {
								logger.error("Cannot merge changed bean!", e);
							}
						});

					adminMonitor = service.createSubscriber(uri, EventConstants.ADMIN_MESSAGE_TOPIC);
					adminMonitor.addListener(evt -> {
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
			if (topicMonitor!=null) topicMonitor.disconnect();
			if (adminMonitor!=null) adminMonitor.disconnect();
		} catch (Exception ne) {
			logger.warn("Problem stopping topic listening for "+getTopicName(), ne);
		}
		try {
			if (statusTopicPublisher != null) statusTopicPublisher.disconnect();
		} catch (Exception e) {
			logger.warn("Problem disconnecting publisher from status topic", e);
		}
		try {
			if (consumerProxy != null) consumerProxy.disconnect();
		} catch (Exception e) {
			logger.warn("Problem disconnecting publisher from command topic", e);
		}

		try {
			if (submitter != null) submitter.disconnect();
		} catch (EventException e) {
			logger.warn("Problem disconnecting from queue connection", e);
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
					logger.trace("mergeBean(id={}) Merging existing bean: {}", bean.getUniqueId(), bean);
					queue.get(bean.getUniqueId()).merge(bean);
					warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "merge complete");
				} else {
					logger.trace("mergeBean(id={}) Adding new bean:       {}", bean.getUniqueId(), bean);
					queue.put(bean.getUniqueId(), bean);
					warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "bean added");
					reconnect();
					warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "reconnect request complete");
				}
				viewer.refresh();
				warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "refresh complete");
				updateActions();
				warnIfDelayed(jobStartTime, "mergeBean() asyncExec()", "updateActions complete");
			});
	}

	private void createActions() throws Exception {

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

		pauseAction = pauseActionCreate();
		addActionTo(toolMan, menuMan, dropDown, pauseAction);

		stopAction = stopActionCreate();
		addActionTo(toolMan, menuMan, dropDown, stopAction);

		final Action pauseConsumerAction = pauseConsumerActionCreate();
		addActionTo(toolMan, menuMan, dropDown, pauseConsumerAction);

		ISubscriber<IBeanListener<QueueCommandBean>> pauseMonitor = service.createSubscriber(getUri(), EventConstants.CMD_TOPIC);
		pauseMonitor.addListener(evt -> {
			final QueueCommandBean.Command command = evt.getBean().getCommand();
			if (command == Command.PAUSE || command == Command.RESUME) {
				pauseConsumerAction.setChecked(consumerProxy.isQueuePaused());
			}
		});

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
		Action action = new Action("Less urgent (-1)", Activator.getImageDescriptor("icons/arrow-090.png")) {
			@Override
			public void run() {
				for(StatusBean bean : getSelection()) {
					try {
						if (bean.getStatus() == org.eclipse.scanning.api.event.status.Status.SUBMITTED) {
							consumerProxy.reorder(bean, 1);
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
		Action action = new Action("More urgent (+1)", Activator.getImageDescriptor("icons/arrow-270.png")) {
			@Override
			public void run() {
				// When moving items down, if we move an item down before moving down an adjacent item below it, we end
				// up with both items in the same position. To avoid this, we iterate the selection list in reverse.
				List<StatusBean> selection = getSelection();
				Collections.reverse(selection);
				for (StatusBean bean : selection) {
					try {
						if (bean.getStatus() == org.eclipse.scanning.api.event.status.Status.SUBMITTED) {
							consumerProxy.reorder(bean, -1);
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

	private Action pauseConsumerActionCreate() {
		Action action = new Action("Pause "+getPartName()+". Does not pause running job.", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				pauseConsumerActionRun(this);
			}
		};
		action.setImageDescriptor(Activator.getImageDescriptor("icons/control-pause-red.png"));
		action.setChecked(consumerProxy.isQueuePaused());
		return action;
	}

	private void pauseConsumerActionRun(IAction pauseConsumer) {

		// The button can get out of sync if two clients are used.
		final boolean queuePaused = consumerProxy.isQueuePaused();
		try {
			pauseConsumer.setChecked(!queuePaused); // We are toggling it.
			if (queuePaused) {
				consumerProxy.resume();
			} else {
				consumerProxy.pause();
			}
		} catch (Exception e) {
			ErrorDialog.openError(getViewSite().getShell(), "Cannot pause queue "+getSubmissionQueueName(),
				"Cannot pause queue "+getSubmissionQueueName()+"\n\nPlease contact your support representative.",
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		pauseConsumer.setChecked(consumerProxy.isQueuePaused());
	}

	private void pauseActionUpdate(boolean anyRunning, boolean anyPaused, boolean anySelectedSubmitted) {
		pauseAction.setEnabled(anyRunning || anyPaused || anySelectedSubmitted);
		pauseAction.setChecked(anyPaused);
		pauseAction.setText(anyPaused?"Resume job":"Pause job");
	}

	private Action pauseActionCreate() {
		Action action = new Action("Pause job.\nPauses a running job.", IAction.AS_CHECK_BOX) {
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

		for(StatusBean bean : getSelection()) {

			if (bean.getStatus().isFinal()) {
				MessageDialog.openInformation(getViewSite().getShell(), "Run '"+bean.getName()+"' inactive",
					"Run '"+bean.getName()+"' is inactive and cannot be paused.");
				continue;
			}

			try {
				if (bean.getStatus().isPaused()) {
					bean.setStatus(org.eclipse.scanning.api.event.status.Status.REQUEST_RESUME);
					bean.setMessage("Resume of "+bean.getName());
				} else {
					bean.setStatus(org.eclipse.scanning.api.event.status.Status.REQUEST_PAUSE);
					bean.setMessage("Pause of "+bean.getName());
				}

				statusTopicPublisher.broadcast(bean);
			} catch (Exception e) {
				ErrorDialog.openError(getViewSite().getShell(), "Cannot pause "+bean.getName(),
					"Cannot pause "+bean.getName()+"\n\nPlease contact your support representative.",
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}
		}
	}

	private void removeActionUpdate(List<StatusBean> selectedInSubmittedList, List<StatusBean> selectedInRunList) {
		boolean anySelectedInSubmittedList = !selectedInSubmittedList.isEmpty();
		boolean anySelectedInRunListAreFinal = selectedInRunList.stream().anyMatch(sb -> sb.getStatus().isFinal());

		removeAction.setEnabled(anySelectedInSubmittedList || anySelectedInRunListAreFinal);
	}

	private Action removeActionCreate() {
		Action action = new Action("Remove job", PlatformUI.getWorkbench().getSharedImages()
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
						consumerProxy.remove(bean);
					} else {
						// only ask the user to confirm is the queue is in the status set not the submit queue
						boolean ok = MessageDialog.openQuestion(getSite().getShell(), "Confirm Remove '"+bean.getName()+"'",
							"Are you sure you would like to remove '"+bean.getName()+"'?");
						if (ok) {
							consumerProxy.removeCompleted(bean);
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
		Action action = new Action("Stop job", Activator.getImageDescriptor("icons/control-stop-square.png")) {
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
				final DateFormat format = DateFormat.getDateTimeInstance();
				boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm terminate "+bean.getName(),
						  "Are you sure you want to terminate "+bean.getName()+" submitted on "+format.format(new Date(bean.getSubmissionTime()))+"?");

				if (!ok) continue;

				bean.setStatus(org.eclipse.scanning.api.event.status.Status.REQUEST_TERMINATE);
				bean.setMessage("Termination of "+bean.getName());

				statusTopicPublisher.broadcast(bean);
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

		consumerProxy.clearQueue();
		consumerProxy.clearRunningAndCompleted();

		// TODO: this temporarily introduces a new race condition, as the consumer doesn't notify us when this
		// is completed. A subsequent change fixes this by introducing an acknowledgement topic
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			logger.error("Thread interrupted", e);
			Thread.currentThread().interrupt();
			throw new EventException(e);
		}

		reconnect();
	}

	private List<IResultHandler> getResultsHandlers() {
		if (resultsHandlers == null) {
			final IConfigurationElement[] configElements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(RESULTS_HANDLER_EXTENSION_POINT_ID);
			final List<IResultHandler> handlers = new ArrayList<>(configElements.length + 1);
			for (IConfigurationElement configElement : configElements) {
				try {
					final IResultHandler handler = (IResultHandler) configElement.createExecutableExtension("class");
					handler.init(service, createConsumerConfiguration());
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

		for (IResultHandler handler : getResultsHandlers()) {
			if (handler.isHandled(bean)) {
				try {
					boolean ok = handler.open(bean);
					if (ok) return;
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
		Action action = new Action("Edit...", Activator.getImageDescriptor("icons/modify.png")) {
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
		detailsAction.setEnabled(selectedInSubmittedList.size() == 0 && selectedInRunList.size() == 1);
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
	@SuppressWarnings({"squid:S3776", "squid:S135"})
	private void editActionRun() {

		for (StatusBean bean : getSelection()) {
			if (bean.getStatus()!=org.eclipse.scanning.api.event.status.Status.SUBMITTED) {
				MessageDialog.openConfirm(getSite().getShell(), "Cannot Edit '"+bean.getName()+"'",
					"The run '"+bean.getName()+"' cannot be edited because it is not waiting to run.");
				continue;
			}

			try {
				final IConfigurationElement[] c = Platform.getExtensionRegistry().getConfigurationElementsFor(MODIFY_HANDLER_EXTENSION_POINT_ID);
				if (c!=null) {
					for (IConfigurationElement i : c) {
						final IModifyHandler handler = (IModifyHandler)i.createExecutableExtension("class");
						handler.init(service, createConsumerConfiguration());
						if (handler.isHandled(bean)) {
							boolean ok = handler.modify(bean);
							if (ok) continue;
						}
					}
				}
			} catch (Exception ne) {
				final String err = "Cannot modify "+bean.getRunDirectory()+" normally.\n\nPlease contact your support representative.";
				logger.error(err, ne);
				ErrorDialog.openError(getSite().getShell(), "Internal Error", err, new Status(IStatus.ERROR, Activator.PLUGIN_ID, ne.getMessage()));
				continue;
			}
			MessageDialog.openConfirm(getSite().getShell(), "Cannot Edit '"+bean.getName()+"'",
				"There are no editers registered for '"+bean.getName()+"'\n\nPlease contact your support representative.");
		}
	}

	private void rerunActionUpdate(List<StatusBean> selection) {
		rerunAction.setEnabled(!selection.isEmpty());
	}

	private Action rerunActionCreate() {
		Action action = new Action("Rerun...", Activator.getImageDescriptor("icons/rerun.png")) {
			@Override
			public void run() {
				rerunActionRun();
			}
		};
		action.setEnabled(false);
		return action;
	}

	@SuppressWarnings("squid:S3776")
	private void rerunActionRun() {

		for (StatusBean bean : getSelection()) {
			try {
				final IConfigurationElement[] c = Platform.getExtensionRegistry().getConfigurationElementsFor(RERUN_HANDLER_EXTENSION_POINT_ID);
				if (c!=null) {
					for (IConfigurationElement i : c) {
						final IRerunHandler handler = (IRerunHandler)i.createExecutableExtension("class");
						handler.init(service, createConsumerConfiguration());
						if (handler.isHandled(bean)) {
							final StatusBean copy = bean.getClass().newInstance();
							copy.merge(bean);
							copy.setUniqueId(UUID.randomUUID().toString());
							copy.setStatus(org.eclipse.scanning.api.event.status.Status.SUBMITTED);
							copy.setSubmissionTime(System.currentTimeMillis());
							boolean ok = handler.run(copy);
							if (ok) continue;
						}
					}
				}
			} catch (Exception ne) {
				final String err = "Cannot rerun "+bean.getRunDirectory()+" normally.\n\nPlease contact your support representative.";
				logger.error(err, ne);
				ErrorDialog.openError(getSite().getShell(), "Internal Error", err, new Status(IStatus.ERROR, Activator.PLUGIN_ID, ne.getMessage()));
				continue;
			}
			// If we have not already handled this rerun, it is possible to call a generic one.
			rerun(bean);
		}
	}

	private ConsumerConfiguration createConsumerConfiguration() throws Exception {
		return new ConsumerConfiguration(getUri(), getSubmissionQueueName(), getTopicName(), getQueueName());
	}

	private void rerun(StatusBean bean) {

		try {
			final DateFormat format = DateFormat.getDateTimeInstance();
			boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm resubmission "+bean.getName(),
					  "Are you sure you want to rerun "+bean.getName()+" submitted on "+format.format(new Date(bean.getSubmissionTime()))+"?");

			if (!ok) return;

			final StatusBean copy = bean.getClass().newInstance();
			copy.merge(bean);
			copy.setUniqueId(UUID.randomUUID().toString());
			copy.setMessage("Rerun of "+bean.getName());
			copy.setStatus(org.eclipse.scanning.api.event.status.Status.SUBMITTED);
			copy.setPercentComplete(0.0);
			copy.setSubmissionTime(System.currentTimeMillis());

			submitter.submit(copy);

			reconnect();

		} catch (Exception e) {
			ErrorDialog.openError(getViewSite().getShell(), "Cannot rerun "+bean.getName(), "Cannot rerun "+bean.getName()+"\n\nPlease contact your support representative.",
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
	}

	private Action hideOtherUsersResultsActionCreate() {
		Action action = new Action("Hide other users results", IAction.AS_CHECK_BOX) {
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
		updateActions();
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

	@SuppressWarnings("squid:S3776")
	private IContentProvider createContentProvider() {
		return new IStructuredContentProvider() {
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
					for (Iterator it = retained.iterator(); it.hasNext();) {
						StatusBean statusBean = (StatusBean) it.next();
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
			.map(sb -> (StatusBean)sb)
			.collect(Collectors.toList());
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
			logger.debug(    "updateQueue()                   queueJob.state={} (0:None, 1:Sleeping, 2:Waiting,4:Running), thread={}, name={} {}", queueJob.getState(), queueJob.getThread(), queueJob.getName(), queueJob);
			if (queueJob.getState() != Job.RUNNING) {
				queueJob.schedule(100);
				logger.debug("updateQueue() schedule() called queueJob.state={} (0:None, 1:Sleeping, 2:Waiting,4:Running), thread={}, name={} {}", queueJob.getState(), queueJob.getThread(), queueJob.getName(), queueJob);
			} else {					// If job is already running then a call to schedule(), as above, would do
				queueJobAgain = true;	// nothing, so instead ask the job to schedule itself again after it completes.
				logger.debug("updateQueue() queueJobAgain set queueJob.state={} (0:None, 1:Sleeping, 2:Waiting,4:Running), thread={}, name={} {}", queueJob.getState(), queueJob.getThread(), queueJob.getName(), queueJob);
			}
			return;
		}
		queueJob = new Job("Connect and read queue") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					logger.debug("updateQueue Job run({})", monitor);

					final Instant jobStartTime = Instant.now();
					monitor.beginTask("Connect to command server", 10);
					updateProgress(jobStartTime, monitor, "Queue connection set");

					runList = consumerProxy.getRunningAndCompleted();
					updateProgress(jobStartTime, monitor, "List of running and completed jobs retrieved");

					submittedList = consumerProxy.getQueue();
					updateProgress(jobStartTime, monitor, "List of submitted jobs retrieved");

					// We leave the list in reverse order so we can insert entries at the start by adding to the end
					final Map<String,StatusBean> ret = new LinkedHashMap<>();
					for (StatusBean bean : runList) {
						ret.put(bean.getUniqueId(), bean);
					}
					updateProgress(jobStartTime, monitor, "Run/running jobs added to view list");

					for (StatusBean bean : submittedList) {
						ret.put(bean.getUniqueId(), bean);
					}
					updateProgress(jobStartTime, monitor, "Submitted jobs added to view list");

					getSite().getShell().getDisplay().syncExec(() -> {
							warnIfDelayed(jobStartTime, "updateQueue Job run() syncExec()", "start");
							viewer.setInput(ret);
							warnIfDelayed(jobStartTime, "updateQueue Job run() syncExec()", "setInput complete");
							viewer.refresh();
							warnIfDelayed(jobStartTime, "updateQueue Job run() syncExec()", "refresh complete");
							updateActions();
							warnIfDelayed(jobStartTime, "updateQueue Job run() syncExec()", "updateActions complete");
						});
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
		};
		queueJob.setPriority(Job.SHORT);
		queueJob.setUser(true);
		queueJob.schedule();
		logger.debug("updateQueue() scheduled as thread {}", queueJob.getThread());
	}

	private Class<StatusBean> getBeanClass() {
		String beanBundleName = getSecondaryIdAttribute("beanBundleName");
		String beanClassName  = getSecondaryIdAttribute("beanClassName");
		try {

			Bundle bundle = Platform.getBundle(beanBundleName);
			return (Class<StatusBean>)bundle.loadClass(beanClassName);
		} catch (Exception ne) {
			logger.error("Cannot get class "+beanClassName+". Defaulting to StatusBean. This will probably not work though.", ne);
			return StatusBean.class;
		}
	}

	@SuppressWarnings("squid:S3776")
	private void createColumns() {

		final TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
		name.getColumn().setText("Name");
		name.getColumn().setWidth(260);
		name.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((StatusBean)element).getName();
			}
		});

		final TableViewerColumn status = new TableViewerColumn(viewer, SWT.LEFT);
		status.getColumn().setText("Status");
		status.getColumn().setWidth(80);
		status.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((StatusBean)element).getStatus().toString();
			}
		});

		final TableViewerColumn pc = new TableViewerColumn(viewer, SWT.CENTER);
		pc.getColumn().setText("Complete");
		pc.getColumn().setWidth(70);
		final NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setRoundingMode(RoundingMode.DOWN);
		pc.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					return percentFormat.format(((StatusBean)element).getPercentComplete()/100d);
				} catch (Exception ne) {
					return "-";
				}
			}
		});

		final TableViewerColumn submittedDate = new TableViewerColumn(viewer, SWT.CENTER);
		submittedDate.getColumn().setText("Date Submitted");
		submittedDate.getColumn().setWidth(120);
		submittedDate.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					return DateFormat.getDateTimeInstance().format(new Date(((StatusBean)element).getSubmissionTime()));
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		});

		final TableViewerColumn message = new TableViewerColumn(viewer, SWT.LEFT);
		message.getColumn().setText("Message");
		message.getColumn().setWidth(150);
		message.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					return ((StatusBean)element).getMessage();
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		});

		final TableViewerColumn location = new TableViewerColumn(viewer, SWT.LEFT);
		location.getColumn().setText("Location");
		location.getColumn().setWidth(300);
		location.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					final StatusBean bean = (StatusBean)element;
					return getLocation(bean);
				} catch (Exception e) {
					return e.getMessage();
				}
			}

			@Override
			public Color getForeground(Object element) {
				boolean isFinal = ((StatusBean) element).getStatus().isFinal();
				return getSite().getShell().getDisplay().getSystemColor(isFinal ? SWT.COLOR_BLUE : SWT.COLOR_BLACK);
			}
		});

		final TableViewerColumn host = new TableViewerColumn(viewer, SWT.CENTER);
		host.getColumn().setText("Host");
		host.getColumn().setWidth(90);
		host.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					return ((StatusBean)element).getHostName();
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		});

		final TableViewerColumn user = new TableViewerColumn(viewer, SWT.CENTER);
		user.getColumn().setText("User Name");
		user.getColumn().setWidth(80);
		user.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					return ((StatusBean)element).getUserName();
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		});

		final TableViewerColumn startTime = new TableViewerColumn(viewer, SWT.CENTER);
		startTime.getColumn().setText("Start Time");
		startTime.getColumn().setWidth(120);
		startTime.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					long statusStartTime = ((StatusBean)element).getStartTime();
					if (statusStartTime == 0) return "";
					return DateFormat.getTimeInstance().format(new Date(statusStartTime));
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		});

		final TableViewerColumn estimatedEndTime = new TableViewerColumn(viewer, SWT.CENTER);
		estimatedEndTime.getColumn().setText("E. End Time");
		estimatedEndTime.getColumn().setWidth(120);
		estimatedEndTime.setLabelProvider(new StatusQueueColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					long statusEstimatedEndTime = ((StatusBean)element).getStartTime() + ((StatusBean)element).getEstimatedTime();
					if (statusEstimatedEndTime == 0) return "";
					return DateFormat.getTimeInstance().format(new Date(statusEstimatedEndTime));
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		});

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

		MouseAdapter mouseClick = new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Point pt = new Point(e.x, e.y);
				TableItem item = viewer.getTable().getItem(pt);
				if (item == null) return;
				Rectangle rect = item.getBounds(5);
				if (rect.contains(pt)) {
					final StatusBean bean = (StatusBean)item.getData();
					if (bean.getStatus().isFinal())
						openResultsActionRun(bean);
				}
			}
		};
		viewer.getTable().addMouseListener(mouseClick);
	}

	@Override
	public void setFocus() {
		if (!viewer.getTable().isDisposed()) {
			viewer.getTable().setFocus();
		}
	}

	public static String createId(final String beanBundleName, final String beanClassName, final String queueName, final String topicName, final String submissionQueueName) {

		final StringBuilder buf = new StringBuilder();
		buf.append(ID);
		buf.append(":");
		buf.append(QueueViews.createSecondaryId(beanBundleName, beanClassName, queueName, topicName, submissionQueueName));
		return buf.toString();
	}

	public static String createId(final String uri, final String beanBundleName, final String beanClassName, final String queueName, final String topicName, final String submissionQueueName) {

		final StringBuilder buf = new StringBuilder();
		buf.append(ID);
		buf.append(":");
		buf.append(QueueViews.createSecondaryId(uri, beanBundleName, beanClassName, queueName, topicName, submissionQueueName));
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
	 * Extend ColumnLabelProvider to provide a tool tip for each column
	 * <p>
	 * There is a bug in Eclipse Mars which means that the tool tip flickers and disappears if it extends beyond the
	 * edge of the screen. We believe that this is fixed in later versions of the Eclipse platform. For the time being,
	 * wrap the tool tip to make this less likely to happen.
	 */
	private static class StatusQueueColumnLabelProvider extends ColumnLabelProvider {
		private static final int WRAP_LENGTH = 40;

		@Override
		public String getToolTipText(Object element) {
			return WordUtils.wrap(getText(element), WRAP_LENGTH, null, true);
		}
	}
}
