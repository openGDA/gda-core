/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.ui.plan.setup.tree;

import static uk.ac.diamond.daq.experiment.api.ui.ExperimentUIConstants.REFRESH_PROPERTY;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.PlanTreeComponent;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public class PlanTreeEditor {

	private final ExperimentPlanBean bean;

	private TreeViewer tree;

	private List<ComponentAction> actions;

	private Consumer<PlanTreeComponent> selectionListener;

	private ITreeContentProvider contentProvider;

	public PlanTreeEditor(ExperimentPlanBean bean) {
		this.bean = bean;
	}

	public void create(Composite parent) {

		tree = new TreeViewer(parent);

		contentProvider = new PlanTreeContentProvider(bean);
		tree.setContentProvider(contentProvider);
		tree.setLabelProvider(new PlanTreeLabelProvider());
		tree.setInput(bean);
		tree.expandAll();

		createActions();
		createMenu();

		if (bean.getSegmentRequests().isEmpty()) {
			addNewSegment();
		}

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tree.getControl());

		PropertyChangeListener refreshListener = evt -> {
			if (evt.getPropertyName().equals(REFRESH_PROPERTY)) {
				tree.refresh();
			}
		};

		bean.addPropertyChangeListener(refreshListener);
		parent.addDisposeListener(dispose -> bean.removePropertyChangeListener(refreshListener));

		tree.getControl().setToolTipText("Right-click to add or remove plan components");
	}

	private void createActions() {

		var newSegment = new ComponentAction("New segment", this::addNewSegment, selection -> true);
		var deleteSegment = new ComponentAction("Delete segment", this::deleteSegment, selection -> selection instanceof SegmentRequest && bean.getSegmentRequests().size() > 1);
		var newTrigger = new ComponentAction("New trigger", this::addNewTrigger, SegmentRequest.class::isInstance);
		var deleteTrigger = new ComponentAction("Delete trigger", this::deleteTrigger, TriggerRequest.class::isInstance);

		actions = List.of(newSegment, deleteSegment, newTrigger, deleteTrigger);

		resetMenuActions(getSelection());
	}

	private Object getSelection() {
		return tree.getStructuredSelection().getFirstElement();
	}

	private void addNewSegment() {
		var segments = new ArrayList<>(bean.getSegments());
		var newSegment = new SegmentDescriptor();
		newSegment.setComponentId(UUID.randomUUID());
		newSegment.setParentId(bean.getUuid());

		var segmentName = "Segment " + (segments.size() + 1);

		/* That is the ideal name, but must check other segment names.
		 * Otherwise it would be easy to break e.g.:
		 * 1. have two segments: 'Segment 1' and 'Segment 2'
		 * 2. delete 'Segment 2'
		 * 3. Add new segment, name will be generated as 'Segment 2', which we already have.
		 */
		if (segments.stream().anyMatch(segment -> segment.getName().equals(segmentName))) {
			newSegment.setName(segmentName.concat(" (new)"));
		} else {
			newSegment.setName(segmentName);
		}
		newSegment.setSignalSource(SignalSource.TIME);
		segments.add(newSegment);
		bean.setSegments(segments);
		tree.refresh();

		// select new segment
		tree.setSelection(new StructuredSelection(newSegment));
	}

	private void deleteSegment() {
		var segmentToDelete = (SegmentDescriptor) getSelection();

		if (!segmentToDelete.getTriggerRequests().isEmpty()) {
			boolean confirmed = MessageDialog.openConfirm(tree.getControl().getShell(), "Delete segment", "This segment contains triggers. Are you sure you want to delete it?");
			if (!confirmed) return;
		}

		var segments = new ArrayList<>(bean.getSegments());
		segments.remove(segmentToDelete);
		bean.setSegments(segments);
		tree.refresh();
	}

	private void addNewTrigger() {
		var segment = (SegmentDescriptor) getSelection();
		var triggers = new ArrayList<>(segment.getTriggers());
		var trigger = new TriggerDescriptor();
		trigger.setComponentId(UUID.randomUUID());
		trigger.setParentId(segment.getComponentId());
		trigger.setName("Trigger " + (triggers.size() + 1));
		trigger.setSignalSource(SignalSource.TIME);
		trigger.setExecutionPolicy(ExecutionPolicy.SINGLE);
		triggers.add(trigger);
		segment.setTriggers(triggers);

		// expand parent segment, keeping remaining expansion state
		var expanded = ArrayUtils.addAll(tree.getExpandedElements(), segment);
		tree.setExpandedElements(expanded);

		tree.refresh();

		// select new trigger
		tree.setSelection(new StructuredSelection(trigger));
	}

	private void deleteTrigger() {
		var trigger = (TriggerDescriptor) getSelection();
		var segment = (SegmentDescriptor) contentProvider.getParent(trigger);
		var triggers = new ArrayList<>(segment.getTriggers());
		triggers.remove(trigger);
		segment.setTriggers(triggers);
		tree.refresh();

		// select parent segment
		tree.setSelection(new StructuredSelection(segment));
	}

	private class ComponentAction extends Action {

		private Runnable operation;
		private Predicate<Object> selectionBasedEnablement;

		public ComponentAction(String title, Runnable operation, Predicate<Object> selectionBasedEnablement) {
			super(title);

			this.operation = operation;
			this.selectionBasedEnablement = selectionBasedEnablement;
		}

		@Override
		public void run() {
			operation.run();
		}

		public void computeEnablement(Object selection) {
			setEnabled(selectionBasedEnablement.test(selection));
		}
	}

	private void resetMenuActions(Object selection) {
		actions.forEach(action -> action.computeEnablement(selection));
	}

	private void createMenu() {
		MenuManager contextMenu = new MenuManager(null);
		actions.forEach(contextMenu::add);
		var menu = contextMenu.createContextMenu(tree.getControl());

		tree.getTree().setMenu(menu);

		tree.addSelectionChangedListener(event -> {

			var structuredSelection = event.getStructuredSelection();
			var selection = (PlanTreeComponent) structuredSelection.getFirstElement();

			// disable multiple selection (can't offer meaningful behaviour)
			if (structuredSelection.size() > 1) {
				tree.setSelection(new StructuredSelection(selection));
			}

			resetMenuActions(selection);

			if (selectionListener != null) {
				selectionListener.accept(selection);
			}

		});
	}

	public void setSelectionListener(Consumer<PlanTreeComponent> selectionListener) {
		this.selectionListener = selectionListener;
	}

}
