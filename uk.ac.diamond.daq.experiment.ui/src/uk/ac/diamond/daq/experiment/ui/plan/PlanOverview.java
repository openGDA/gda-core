package uk.ac.diamond.daq.experiment.ui.plan;

import static org.eclipse.scanning.api.event.EventConstants.STATUS_TOPIC;
import static uk.ac.diamond.daq.experiment.api.plan.event.EventConstants.EXPERIMENT_PLAN_TOPIC;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.plan.event.EventConstants;
import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.ui.plan.tree.PlanTree;
import uk.ac.diamond.daq.experiment.ui.plan.tree.PlanTreeNode;
import uk.ac.diamond.daq.experiment.ui.plan.tree.SegmentNode;
import uk.ac.diamond.daq.experiment.ui.plan.tree.TriggerNode;

public class PlanOverview extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(PlanOverview.class);

	private static final int INITIAL_COLUMN_WIDTH = 150;
	private static final int COLUMN_PACK_MARGIN = 10;
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_TIME;
	private static final ZoneId ZONE = ZoneId.of("Europe/London");
	private static final String SEGMENT_ICON_PATH = "/icons/segment.png";
	private static final String TRIGGER_ICON_PATH = "/icons/trigger.png";
	private static final String TIMER_NAME = "System timer";

	private ISubscriber<IBeanListener<PlanStatusBean>> planSubscriber;
	private ISubscriber<IBeanListener<StatusBean>> scanSubscriber;
	private static IEventService eventService;

	private TreeViewer viewer;
	
	private String idOfCurrentPlan;
	private Map<String, TriggerNode> triggersInCurrentTree = new HashMap<>();
	
	private Color running;
	private Color completed;
	private Color error;

	@Override
	public void createPartControl(Composite parent) {

		try {
			createSubscribers();
		} catch (Exception e) {
			logger.error("Could not create subscriber, rendering this view useless. Giving up...", e);
			return;
		}
		
		// colours:
		running = new Color(Display.getDefault(), 171, 204, 201);
		completed = new Color(Display.getDefault(), 217, 222, 171);
		error = new Color(Display.getDefault(), 204, 125, 65);

		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new PlanTreeContentProvider());
		viewer.getTree().setHeaderVisible(true);

		addColumn("Name", 
			textFunctionCreator(PlanTreeNode::getName),
			imageFunctionCreator(segment -> SEGMENT_ICON_PATH, trigger -> TRIGGER_ICON_PATH));
		
		addColumn("Status", textFunctionCreator(node -> {
			Status status = node.getStatus();
			if (status != null) {
				return status.toString();
			} else {
				return null;
			}
		}));

		addColumn("Time", textFunctionCreator(node -> formatTimestamp(node.getTime())));

		addColumn("Driver", textFunctionCreator(PlanTreeNode::getSevName));

		addColumn("Value",
			textFunctionCreator(node -> {
				if (node instanceof SegmentNode && !((SegmentNode) node).hasFinished()) {
					return null; // will display its limiting signal when it has finished
				}
				if (node.getSevName().equals(TIMER_NAME)) {
					return getDurationString(node.getRelativeStart(), (long) node.getSignificantSignal() * 1000);
				} else {
					return String.valueOf(node.getSignificantSignal());
				}
			}));
		
		viewer.getTree().setEnabled(false);

		GridLayoutFactory.fillDefaults().generateLayout(parent);
		
		viewer.addDoubleClickListener(event -> {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Object selectedNode = selection.getFirstElement();
			viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
			resizeColumns();
		});
		
		viewer.getTree().addListener(SWT.Expand, e -> resizeColumns());
	}
	
	/**
	 * essentially column.pack() + a constant margin 
	 */
	private void resizeColumns() {
		final TreeColumn[] treeColumns = viewer.getTree().getColumns();
		Display.getDefault().asyncExec(() -> {
			for (TreeColumn column : treeColumns) {
				column.pack();
				column.setWidth(column.getWidth()+COLUMN_PACK_MARGIN);
			}
		});
	}

	/**
	 * This view subscribes to the {@link EventConstants#EXPERIMENT_PLAN_TOPIC experiment plan topic}
	 * and the {@link org.eclipse.scanning.api.event.EventConstants#STATUS_TOPIC status topic}.
	 * </p>
	 * Remember to {@link #dispose() dispose} the subscribers!
	 */
	private void createSubscribers() throws URISyntaxException, EventException {
		Objects.requireNonNull(eventService, "Event service not set! Check OSGi configuration.");
		URI activeMqUri = new URI(LocalProperties.getActiveMQBrokerURI());
		planSubscriber = eventService.createSubscriber(activeMqUri, EXPERIMENT_PLAN_TOPIC);
		planSubscriber.addListener(this::planListener);
		
		scanSubscriber = eventService.createSubscriber(activeMqUri, STATUS_TOPIC);
		scanSubscriber.addListener(this::scanListener);
	}
	
	/**
	 * With each plan update we create a new PlanTree structure
	 * and set it as the viewer input.
	 */
	private void planListener(BeanEvent<PlanStatusBean> event) {
		Display.getDefault().syncExec(()->{
			if (!viewer.getTree().isEnabled()) {
				viewer.getTree().setEnabled(true);
			}
			final PlanStatusBean bean = event.getBean();
			if (newPlan(bean)) {
				logger.debug("Registering new Plan bean {}", bean.getName());
				idOfCurrentPlan = bean.getUniqueId();
				triggersInCurrentTree.clear();
			}
			
			// preserve the expanded state
			Object[] expanded = viewer.getExpandedElements();
			
			// this prevents 'flashing' in the viewer while updating
			viewer.getTree().setRedraw(false);
			
			PlanTree tree = new PlanTree(bean, triggersInCurrentTree);
			
			// we restore the 'status' field of past/ongoing triggers
			tree.getTriggers().forEach(trigger -> {
				if (!triggersInCurrentTree.containsKey(trigger.getId())) {
					triggersInCurrentTree.put(trigger.getId(), trigger);
				}
			});
			
			viewer.setInput(tree);
			viewer.setExpandedElements(expanded);
			viewer.getTree().setRedraw(true);
		});
	}
	
	/**
	 * We listen to scan updates in order to display the current status in the appropriate
	 * tree node.
	 * 
	 * A special consideration is that a plan can end but a scan started by it is still running.
	 * When this final scan ends its trigger node should be updated (if the tree is still in the viewer)
	 */
	private void scanListener(BeanEvent<StatusBean> event) {
		StatusBean bean = event.getBean();
		
		final String scanId = bean.getUniqueId();
		final Status scanStatus = bean.getStatus();
		
		if (triggersInCurrentTree.containsKey(scanId) // this is a scan that's relevant to the plan
			&& triggersInCurrentTree.get(scanId).getStatus() != scanStatus) { // the status of the scan has changed
			
			triggersInCurrentTree.get(bean.getUniqueId()).setStatus(bean.getStatus());

			Display.getDefault().asyncExec(()->viewer.update(triggersInCurrentTree.get(scanId), new String[] {"status"}));
		}
	}

	private boolean newPlan(PlanStatusBean bean) {
		return idOfCurrentPlan == null || !idOfCurrentPlan.equals(bean.getUniqueId());
	}

	/**
	 * For when we don't need an image.
	 * {@code textFunction} can return {@code null} but cannot be {@code null} itself.
	 */
	private void addColumn(String name, Function<Object, String> textFunction) {
		addColumn(name, textFunction, null);
	}

	private void addColumn(String name, Function<Object, String> textFunction, Function<Object, Image> imageFunction) {
		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.CENTER);
		column.getColumn().setText(name);
		column.getColumn().setWidth(INITIAL_COLUMN_WIDTH);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (textFunction != null) {
					return textFunction.apply(element);
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (imageFunction != null) {
					return imageFunction.apply(element);
				}
				return super.getImage(element);
			}
			
			@Override
			public Color getBackground(Object element) {
				if (element instanceof PlanTreeNode) {
					return getBackgroundForPlanTreeNode((PlanTreeNode) element);
				}
				return super.getBackground(element);
			}
		});
	}
	
	private Color getBackgroundForPlanTreeNode(PlanTreeNode node) {
		Status status = node.getStatus();
		if (status == null) {
			// a submission could have failed (status would not have been set)
			if (node instanceof TriggerNode && ((TriggerNode) node).hasFailed()) {
				return error;
			}
			return null;
		}
		if (status.isActive()) return running;
		
		// careful here:
		// we check explicitly for FAILED first
		// because status.isFinal() includes FAILED also
		else if (status == Status.FAILED) return error;
		else if (status.isFinal()) return completed;
		return null;
	}

	private Function<Object, String> textFunctionCreator(Function<PlanTreeNode, String> nodeToText) {
		return element -> {
			if (element instanceof PlanTreeNode) {
				return nodeToText.apply((PlanTreeNode) element);
			}
			return null;
		};
	}

	private Function<Object, Image> imageFunctionCreator(Function<SegmentNode, String> segmentToImagePath, Function<TriggerNode, String> triggerToImagePath) {
		return element -> {
			String path = null;
			if (element instanceof SegmentNode) {
				path = segmentToImagePath.apply((SegmentNode) element);
			} else if (element instanceof TriggerNode) {
				path = triggerToImagePath.apply((TriggerNode) element);
			}
			if (path == null) {
				return null;
			} else {
				return getIcon(path);
			}
		};
	}

	private String formatTimestamp(long epoch) {
		return Instant.ofEpochMilli(epoch).atZone(ZONE).format(TIME_FORMAT);
	}

	private String getDurationString(long earlier, long later) {
		return DurationFormatUtils.formatDuration(later-earlier, "HH:mm:ss", true);
	}

	@Override
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	/**
	 * Describes a two-level {@link PlanTree} structure
	 */
	private class PlanTreeContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			PlanTree bean = (PlanTree) inputElement;
			return ArrayContentProvider.getInstance().getElements(bean.getSegments());
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof SegmentNode) {
				return ((SegmentNode) parentElement).getTriggerEvents().toArray();
			} else {			
				return new Object[0];
			}
		}

		@Override
		public Object getParent(Object element) {
			return null; // Not necessary
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof SegmentNode) {
				return !((SegmentNode) element).getTriggerEvents().isEmpty();
			} else {
				return false;
			}
		}

	}

	/**
	 * Creates an {@link Image} using the specified path
	 */
	private Image getIcon(final String path) {
		return new Image(Display.getDefault(), getClass().getResourceAsStream(path));
	}

	@Override
	public void dispose() {
		if (planSubscriber != null) {
			try {
				planSubscriber.disconnect();
			} catch (EventException e) {
				logger.error("Error disconnecting plan subscriber", e);
			}
		}
		if (scanSubscriber != null) {
			try {
				scanSubscriber.disconnect();
			} catch (EventException e) {
				logger.error("Error disconnecting scan subscriber", e);
			}
		}
		running.dispose();
		completed.dispose();
		error.dispose();
		super.dispose();
	}

	// for OSGi use only!
	public void setEventService(IEventService eventService) {
		PlanOverview.eventService = eventService; // NOSONAR used by OSGi only
	}

}
 