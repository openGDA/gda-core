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

package uk.ac.diamond.daq.experiment.ui.plan.setup.context;

import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;
import static uk.ac.gda.ui.tool.ClientSWTElements.numericTextBox;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import tec.units.indriya.unit.Units;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.Inequality;
import uk.ac.diamond.daq.experiment.api.remote.PlanTreeComponent;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.ui.plan.ScannableMotionNamesCombo;
import uk.ac.diamond.daq.experiment.ui.plan.setup.ComponentEditor;
import uk.ac.gda.client.NumberAndUnitsComposite;

public class SegmentEditor implements ComponentEditor {

	private Composite segmentControls;
	private Composite endConditionControls;

	private ScannableMotionNamesCombo scannables;
	private Set<String> prioritySignals;
	private SegmentDescriptor segment;

	private DataBindingContext dbc;
	private ISideEffect sourceListener;
	@Override
	public void createControls(Composite parent, PlanTreeComponent component) {
		clearState();

		segment = (SegmentDescriptor) component;

		segmentControls = composite(parent, 1);
		nameControls();

		endConditionType();
	}

	private void clearState() {
		if (dbc != null) {
			for (var binding : new ArrayList<>(dbc.getBindings())) {
				binding.dispose();
			}
			dbc.dispose();
		}

		dbc = new DataBindingContext();

		if (sourceListener != null) {
			sourceListener.dispose();
		}
	}

	private void nameControls() {
		var nameComposite = composite(segmentControls, 2);

		label(nameComposite, "Name:");
		var name = new Text(nameComposite, SWT.BORDER);
		STRETCH.applyTo(name);

		name.setText(segment.getName());
		name.addListener(SWT.Modify, event -> segment.setName(name.getText()));
	}

	private void endConditionType() {
		var signalSource = composite(segmentControls, 1);
		label(signalSource, "End condition:");

		var buttons = composite(signalSource, 2);
		STRETCH.copy().span(2, 1).applyTo(buttons);
		var timeBased = new Button(buttons, SWT.RADIO);
		timeBased.setText("Time-based");

		var signalBased = new Button(buttons, SWT.RADIO);
		signalBased.setText("Signal-based");

		IObservableValue<SignalSource> sourceInModelObservable = BeanProperties.value("signalSource", SignalSource.class).observe(segment);

		SelectObservableValue<SignalSource> limitingSourceSelection = new SelectObservableValue<>();
		limitingSourceSelection.addOption(SignalSource.POSITION, WidgetProperties.buttonSelection().observe(signalBased));
		limitingSourceSelection.addOption(SignalSource.TIME, WidgetProperties.buttonSelection().observe(timeBased));

		dbc.bindValue(limitingSourceSelection, sourceInModelObservable);

		sourceListener = ISideEffect.create(limitingSourceSelection::getValue, this::updateLimitControls);

	}

	private void updateLimitControls(SignalSource signalSource) {
		resetEndConditionControls();
		if (signalSource == SignalSource.TIME) {
			timeBasedControls();
		} else if (signalSource == SignalSource.POSITION) {
			signalBasedControls();
		}
		segmentControls.getParent().layout(true, true);
	}

	private void resetEndConditionControls() {
		if (endConditionControls != null) {
			endConditionControls.dispose();
			endConditionControls = null;
		}

		endConditionControls = composite(segmentControls, 1);
	}

	private void timeBasedControls() {
		var timeBasedControls = composite(endConditionControls, 1);
		label(timeBasedControls, "Duration:");
		var controls = composite(timeBasedControls, 1);
		var duration = new NumberAndUnitsComposite<>(controls, SWT.NONE, Units.SECOND, Set.of(Units.SECOND, Units.MINUTE, Units.HOUR));
		STRETCH.applyTo(duration);

		duration.setValue(segment.getDuration());
		duration.addListener(SWT.Modify, event -> segment.setDuration(duration.getValue()));
	}

	private void signalBasedControls() {
		var signalBasedControls = composite(endConditionControls, 1);
		label(signalBasedControls, "Active until:");

		var condition = composite(signalBasedControls, 3, false);
		scannables = new ScannableMotionNamesCombo(condition);
		if (prioritySignals != null) {
			scannables.setPriorityItems(prioritySignals);
		}
		STRETCH.applyTo(scannables.getControl());

		var inequality = new ComboViewer(condition, SWT.DROP_DOWN | SWT.READ_ONLY);
		inequality.setContentProvider(ArrayContentProvider.getInstance());

		Set<Inequality> pred = EnumSet.allOf(Inequality.class);
		inequality.setInput(pred.toArray());

		var predicateArgument = numericTextBox(condition);

		IViewerObservableValue<Object> sevControlObservable = ViewerProperties.singleSelection().observe(scannables);
		IObservableValue<String> sevInModelObservable = BeanProperties.value("sampleEnvironmentVariableName", String.class).observe(segment);
		dbc.bindValue(sevControlObservable, sevInModelObservable);

		if (segment.getSampleEnvironmentVariableName() == null) {
			scannables.setSelection(new StructuredSelection(scannables.getElementAt(0)), true);
		}

		IViewerObservableValue<Object> selectedInequalityObservable = ViewerProperties.singleSelection().observe(inequality);
		IObservableValue<SignalSource> inequalityInModelObservable = BeanProperties.value("inequality", SignalSource.class).observe(segment);
		dbc.bindValue(selectedInequalityObservable, inequalityInModelObservable);

		IObservableValue<String> ineqArgControlObservable = WidgetProperties.text(SWT.Modify).observe(predicateArgument);
		IObservableValue<Double> ineqArgInModelObservable = BeanProperties.value("inequalityArgument", double.class).observe(segment);
		dbc.bindValue(ineqArgControlObservable, ineqArgInModelObservable);
	}

	@Override
	public void setPrioritySignals(Set<String> signals) {
		prioritySignals = signals;
		if (scannables != null && !scannables.getControl().isDisposed()) {
			scannables.setPriorityItems(signals);
		}
	}

}
