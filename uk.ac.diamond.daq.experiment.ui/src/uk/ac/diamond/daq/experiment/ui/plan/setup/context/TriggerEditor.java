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

import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.SCAN_PROPERTY;
import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;
import static uk.ac.gda.ui.tool.ClientSWTElements.numericTextBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import tec.units.indriya.unit.Units;
import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.PlanTreeComponent;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.ui.plan.ScannableMotionNamesCombo;
import uk.ac.diamond.daq.experiment.ui.plan.setup.ComponentEditor;
import uk.ac.gda.client.NumberAndUnitsComposite;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.rest.ConfigurationsRestServiceClient;

public class TriggerEditor implements ComponentEditor {

	private TriggerDescriptor trigger;

	private DataBindingContext dbc;
	private List<ISideEffect> sideEffects;

	private Composite triggerControls;
	private Composite triggerDetails;

	private ScannableMotionNamesCombo scannables;
	private Set<String> prioritySignals;

	private Map<Pair<SignalSource, ExecutionPolicy>, Runnable> triggerDetailControls = Map.of(
			Pair.of(SignalSource.TIME, ExecutionPolicy.SINGLE), this::singleTimeBasedTrigger,
			Pair.of(SignalSource.TIME, ExecutionPolicy.REPEATING), this::repeatingTimeBasedTrigger,
			Pair.of(SignalSource.POSITION, ExecutionPolicy.SINGLE), this::singleSignalBasedTrigger,
			Pair.of(SignalSource.POSITION, ExecutionPolicy.REPEATING), this::repeatingSignalBasedTrigger);


	/** access via {@link #getService()} */
	private ConfigurationsRestServiceClient service;

	@Override
	public void createControls(Composite parent, PlanTreeComponent component) {
		clearState();

		trigger = (TriggerDescriptor) component;
		triggerControls = composite(parent, 1);

		nameControls();

		measurementControls();

		triggerSourceControls();

		triggerModeControls();

	}

	private void clearState() {
		if (dbc != null) {
			for (var binding : new ArrayList<>(dbc.getBindings())) {
				binding.dispose();
			}
			dbc.dispose();
		}

		dbc = new DataBindingContext();

		if (sideEffects != null) {
			sideEffects.forEach(ISideEffect::dispose);
			sideEffects.clear();
		} else {
			sideEffects = new ArrayList<>();
		}

	}

	private void nameControls() {
		var nameComposite = composite(triggerControls, 2);

		label(nameComposite, "Name:");
		var name = new Text(nameComposite, SWT.BORDER);
		STRETCH.applyTo(name);

		name.setText(trigger.getName());
		name.addListener(SWT.Modify, event -> trigger.setName(name.getText()));
	}

	private void measurementControls() {
		var controls = composite(triggerControls, 2);
		label(controls, "Measurement:");

		var scanCombo = new ComboViewer(controls, SWT.DROP_DOWN);
		scanCombo.setContentProvider(ArrayContentProvider.getInstance());
		scanCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Document document) {
					return document.getName();
				}
				return super.getText(element);
			}
		});

		var input = getScansList();
		scanCombo.setInput(input);

		var scanNames = input.stream().map(Document::getName).toList().toArray(new String[0]);
		new AutoCompleteField(scanCombo.getControl(), new ComboContentAdapter(), scanNames);

		STRETCH.applyTo(scanCombo.getControl());

		var scanInWidget = ViewerProperties.singleSelection(Document.class).observe(scanCombo);
		var scanInModel = BeanProperties.value(SCAN_PROPERTY, UUID.class).observe(trigger);

		var docToIdConverter = IConverter.create(Document.class, UUID.class, document -> documentToId((Document) document));
		var idToDocConverter = IConverter.create(UUID.class, Document.class, id -> idToDocument((UUID) id));

		dbc.bindValue(scanInWidget, scanInModel, UpdateValueStrategy.create(docToIdConverter), UpdateValueStrategy.create(idToDocConverter));

	}

	private UUID documentToId(Document doc) {
		if (doc == null) return null;
		return doc.getUuid();
	}

	private Document idToDocument(UUID id) {
		if (id == null) return null;
		try {
			return getService().getDocument(id.toString());
		} catch (GDAClientRestException e) {
			return null;
		}
	}

	private List<Document> getScansList() {
		try {
			return getService().getDocuments();
		} catch (GDAClientRestException e) {
			return Collections.emptyList();
		}
	}

	private ConfigurationsRestServiceClient getService() {
		if (service == null) {
			service = SpringApplicationContextFacade.getBean(ConfigurationsRestServiceClient.class);
		}
		return service;
	}

	private void triggerSourceControls() {
		var sourceComposite = composite(triggerControls, 1);
		label(sourceComposite, "Source:");

		var buttons = composite(sourceComposite, 2);
		STRETCH.copy().span(2, 1).applyTo(buttons);
		var timeBased = new Button(buttons, SWT.RADIO);
		timeBased.setText("Time-based");

		var signalBased = new Button(buttons, SWT.RADIO);
		signalBased.setText("Signal-based");

		IObservableValue<SignalSource> sourceInModelObservable = BeanProperties.value("signalSource", SignalSource.class).observe(trigger);

		SelectObservableValue<SignalSource> sourceSelection = new SelectObservableValue<>();
		sourceSelection.addOption(SignalSource.POSITION, WidgetProperties.buttonSelection().observe(signalBased));
		sourceSelection.addOption(SignalSource.TIME, WidgetProperties.buttonSelection().observe(timeBased));

		dbc.bindValue(sourceSelection, sourceInModelObservable);

		sideEffects.add(ISideEffect.create(sourceSelection::getValue, source -> createDetailComposite()));
	}

	private void triggerModeControls() {
		var modeComposite = composite(triggerControls, 1);
		label(modeComposite, "Mode:");

		var buttons = composite(modeComposite, 2);
		STRETCH.copy().span(2, 1).applyTo(buttons);
		var single = new Button(buttons, SWT.RADIO);
		single.setText("Single");

		var periodic = new Button(buttons, SWT.RADIO);
		periodic.setText("Periodic");

		IObservableValue<ExecutionPolicy> modeInModelObservable = BeanProperties.value("executionPolicy", ExecutionPolicy.class).observe(trigger);

		SelectObservableValue<ExecutionPolicy> modeSelection = new SelectObservableValue<>();
		modeSelection.addOption(ExecutionPolicy.SINGLE, WidgetProperties.buttonSelection().observe(single));
		modeSelection.addOption(ExecutionPolicy.REPEATING, WidgetProperties.buttonSelection().observe(periodic));

		dbc.bindValue(modeSelection, modeInModelObservable);

		sideEffects.add(ISideEffect.create(modeSelection::getValue, mode -> createDetailComposite()));
	}

	private void createDetailComposite() {
		if (triggerDetails != null) {
			triggerDetails.dispose();
			triggerDetails = null;
		}
		triggerDetails = composite(triggerControls, 1);

		triggerDetailControls.get(Pair.of(trigger.getSignalSource(), trigger.getExecutionPolicy())).run();

		triggerControls.getParent().layout(true, true);
	}

	private void singleTimeBasedTrigger() {
		var details = composite(triggerDetails, 2);

		label(details, "Time (from segment start):");

		var target = new NumberAndUnitsComposite<>(details, SWT.NONE, Units.SECOND, Set.of(Units.SECOND, Units.MINUTE, Units.HOUR));
		STRETCH.applyTo(target);

		target.setValue(trigger.getTarget());
		target.addListener(SWT.Modify, event -> trigger.setTarget(target.getValue()));
	}

	private void singleSignalBasedTrigger() {
		var details = composite(triggerDetails, 2);

		label(details, "Scannable:");
		scannables = new ScannableMotionNamesCombo(details);
		scannables.setPriorityItems(prioritySignals);
		STRETCH.applyTo(scannables.getControl());

		label(details, "Target:");
		var target = numericTextBox(details);

		label(details, "Tolerance:");
		var tolerance = numericTextBox(details);

		IViewerObservableValue<Object> sevControlObservable = ViewerProperties.singleSelection().observe(scannables);
		IObservableValue<String> sevInModelObservable = BeanProperties.value("sampleEnvironmentVariableName", String.class).observe(trigger);
		dbc.bindValue(sevControlObservable, sevInModelObservable);

		if (trigger.getSampleEnvironmentVariableName() == null) {
			scannables.setSelection(new StructuredSelection(scannables.getElementAt(0)), true);
		}

		target.setText(String.valueOf(trigger.getTarget()));
		target.addListener(SWT.Modify, event -> trigger.setTarget(Double.parseDouble(target.getText())));

		tolerance.setText(String.valueOf(trigger.getTolerance()));
		tolerance.addListener(SWT.Modify, event -> trigger.setTolerance(Double.parseDouble(tolerance.getText())));
	}

	private void repeatingTimeBasedTrigger() {
		var details = composite(triggerDetails, 2);

		label(details, "Period:");
		var period = new NumberAndUnitsComposite<>(details, SWT.NONE, Units.SECOND, Set.of(Units.SECOND, Units.MINUTE, Units.HOUR));
		STRETCH.applyTo(period);

		label(details, "Offset:");
		var offset = new NumberAndUnitsComposite<>(details, SWT.NONE, Units.SECOND, Set.of(Units.SECOND, Units.MINUTE, Units.HOUR));
		STRETCH.applyTo(offset);

		period.setValue(trigger.getInterval());
		period.addListener(SWT.Modify, event -> trigger.setInterval(period.getValue()));

		offset.setValue(trigger.getOffset());
		offset.addListener(SWT.Modify, event -> trigger.setOffset(offset.getValue()));
	}

	private void repeatingSignalBasedTrigger() {
		var details = composite(triggerDetails, 2);

		label(details, "Scannable:");
		scannables = new ScannableMotionNamesCombo(details);
		if (prioritySignals != null) {
			scannables.setPriorityItems(prioritySignals);
		}
		STRETCH.applyTo(scannables.getControl());

		label(details, "Period:");
		var period = numericTextBox(details);

		label(details, "Offset:");
		var offset = numericTextBox(details);

		IViewerObservableValue<Object> sevControlObservable = ViewerProperties.singleSelection().observe(scannables);
		IObservableValue<String> sevInModelObservable = BeanProperties.value("sampleEnvironmentVariableName", String.class).observe(trigger);
		dbc.bindValue(sevControlObservable, sevInModelObservable);

		if (trigger.getSampleEnvironmentVariableName() == null) {
			scannables.setSelection(new StructuredSelection(scannables.getElementAt(0)), true);
		}

		period.setText(String.valueOf(trigger.getInterval()));
		period.addListener(SWT.Modify, event -> trigger.setInterval(Double.parseDouble(period.getText())));

		offset.setText(String.valueOf(trigger.getOffset()));
		offset.addListener(SWT.Modify, event -> trigger.setOffset(Double.parseDouble(offset.getText())));
	}

	@Override
	public void setPrioritySignals(Set<String> signals) {
		prioritySignals = signals;
		if (scannables != null && !scannables.getControl().isDisposed()) {
			scannables.setPriorityItems(signals);
		}
	}


}
