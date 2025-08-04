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

package uk.ac.diamond.daq.mapping.ui.xanes;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.PolarisationParameters;
import uk.ac.diamond.daq.mapping.api.PolarisationParameters.Phase;
import uk.ac.diamond.daq.mapping.api.PolarisationParameters.Polarisation;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;

public class PolarisationSection extends AbstractHideableMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(PolarisationSection.class);

	private static final int COMBO_WIDTH = 60;
	private static final int TEXT_WIDTH = 40;

	private int selectedDegree;
	private ComboViewer degreesCombo;
	private List<Integer> degreesList = List.of(0, 10, 20, 30, 40, 50, 60, 70, 80, 90);

	private Label phaseLabel;
	private Text phaseText;
	private List<Phase> phaseList;
	private ComboViewer phaseCombo;

	private List<Button> radioButtons;
	private Composite buttonComposite;

	private PolarisationParameters scanParameters;

	private Map<String, Double> edgeToPhase = Collections.emptyMap();
	private Map<Polarisation, Double> polarisationToPhase = Collections.emptyMap();

	private List<String> linearArbitraryEdges;
	private String selectedLinearArbitraryEdge;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		if (edgeToPhase == null || edgeToPhase.isEmpty()) {
			logger.error("Element and edges have not been defined");
			return;
		}

		if (polarisationToPhase == null || polarisationToPhase.isEmpty()) {
			logger.error("Phases position for linear polarisation have not been defined");
			return;
		}

		if (linearArbitraryEdges == null || linearArbitraryEdges.isEmpty()) {
			logger.error("Elements for linear arbitrary polarisation have no been defined");
			return;
		}

		if (scanParameters == null) {
			scanParameters = new PolarisationParameters();
		}

		content = createComposite(parent, 1, false);
		GridDataFactory.swtDefaults().applyTo(content);
		GridLayoutFactory.swtDefaults().applyTo(content);

		LabelFactory.newLabel(SWT.NONE).text("Polarisation").create(createComposite(content, 1, true));

		createPolarisationControls(content);
		createPhaseControls(content);

		updateControls();

		setContentVisibility();
	}

	private void createPolarisationControls(Composite parent) {
		buttonComposite = createComposite(parent, 5, true);
		radioButtons = Stream.of(Polarisation.values()).map(this::createButton).toList();

		var polarisationModel = BeanProperties.value("polarisation", Polarisation.class).observe(scanParameters);
		var polarisationWidget = new SelectObservableValue<>();
		radioButtons.stream()
		.forEach(b -> polarisationWidget.addOption(
				b.getData(),
				WidgetProperties.buttonSelection().observe(b)));
		getDataBindingContext().bindValue(polarisationWidget, polarisationModel);
	}


	private void createPhaseControls(Composite parent) {
		var comboComposite = createComposite(parent, 6, true);
		LabelFactory.newLabel(SWT.NONE).text("Element/edge").create(comboComposite);

		phaseList = edgeToPhase.entrySet().stream()
				.sorted(Comparator.comparing(Map.Entry::getValue))
				.map(o -> new Phase(o.getKey(), o.getValue())).toList();

		phaseCombo = new ComboViewer(comboComposite);
		phaseCombo.setContentProvider(ArrayContentProvider.getInstance());
		phaseCombo.setInput(phaseList);
		phaseCombo.setLabelProvider(new LabelProvider () {
			@Override
			public String getText(Object element) {
				if (element instanceof Phase phase) {
					return phase.getElement(); // for circular mode
				} else if (element instanceof String edge) {
					return edge; // for arbitrary mode
				}
				return super.getText(element);
			}
		});
		phaseCombo.addSelectionChangedListener(this::handleEdgeSelectionChanged);
		GridDataFactory.swtDefaults().hint(COMBO_WIDTH, SWT.DEFAULT).applyTo(phaseCombo.getCombo());

		LabelFactory.newLabel(SWT.NONE).text("Degrees").create(comboComposite);
		degreesCombo = new ComboViewer(comboComposite);
		degreesCombo.setContentProvider(ArrayContentProvider.getInstance());
		degreesCombo.setInput(degreesList);
		degreesCombo.setLabelProvider(new LabelProvider () {
			@Override
			public String getText(Object degree) {
				return String.valueOf(degree);
			}
		});
		degreesCombo.addSelectionChangedListener(this::handleDegreesSelectionChanged);
		GridDataFactory.swtDefaults().hint(COMBO_WIDTH, SWT.DEFAULT).applyTo(degreesCombo.getCombo());

		phaseLabel = LabelFactory.newLabel(SWT.NONE).text("Phase: ").create(comboComposite);
		GridDataFactory.swtDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(phaseLabel);
		phaseText = TextFactory.newText(SWT.NONE).enabled(false).create(comboComposite);
		phaseText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		GridDataFactory.swtDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(phaseText);
		// the phase will change depending on the polarisation setting selected
		radioButtons.stream()
			.forEach(b -> b.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleButtonSelectionChanged)));

		var phaseWidget = WidgetProperties.text(SWT.Modify).observe(phaseText);
		var phaseModel = BeanProperties.value("phase", Double.class).observe(scanParameters);
		getDataBindingContext().bindValue(phaseWidget, phaseModel);

		// select first option and notify listeners to handle the selection event
		radioButtons.stream().findFirst().ifPresent(button -> {
			button.setSelection(true);
			button.notifyListeners(SWT.Selection, new Event());
		});
	}

	private void handleDegreesSelectionChanged(SelectionChangedEvent selection) {
		var degree = selection.getStructuredSelection().getFirstElement();
		selectedDegree = ((Integer) degree).intValue();
	}

	private void setComboEmpty(ComboViewer comboViewer) {
		comboViewer.setInput(Collections.emptyList());
		comboViewer.getCombo().setEnabled(false);
	}

	private void setComboInput(ComboViewer comboViewer, List<?> inputList) {
		comboViewer.setInput(inputList);
		comboViewer.setSelection(new StructuredSelection(inputList.get(0)));
		comboViewer.getCombo().setEnabled(true);
	}

	private void handleButtonSelectionChanged(SelectionEvent event) {
		var button = (Button) event.getSource();
		var polarisation = (Polarisation) button.getData();

		switch (polarisation.getDirection()) {
			case LINEAR -> {
				setComboEmpty(degreesCombo);
				setComboEmpty(phaseCombo);
				phaseLabel.setText("Phase: ");
				phaseText.setText(String.valueOf(polarisationToPhase.get(polarisation)));
			}
			case CIRCULAR -> {
				setComboEmpty(degreesCombo);
				setComboInput(phaseCombo, phaseList);
				phaseLabel.setText("Phase: ");
			}
			case DEGREES -> {
				setComboInput(degreesCombo, degreesList);
				setComboInput(phaseCombo, linearArbitraryEdges);
				phaseLabel.setText("");
				phaseText.setText("");
			}
		}
	}

	private Button createButton(Polarisation polarisation) {
		return ButtonFactory.newButton(SWT.RADIO).data(polarisation).text(polarisation.getLabel()).create(buttonComposite);
	}

	/**
	 * Sets the phase value according to the Element/edge selected
	 * If the current Polarisation is Right, the value will be positive.
	 * If the current Polarisation is Left, the value will be negative.
	 * @param selection element from combo list
	 */
	private void handleEdgeSelectionChanged(SelectionChangedEvent selection) {
		var selected = selection.getStructuredSelection().getFirstElement();

		if (selected instanceof Phase element) {
			var phase = element.getPosition();
			var df = new DecimalFormat("#.0#");

			if (getScanParameters().getPolarisation().equals(Polarisation.CL)) {
				phase *= -1;
			}

			phaseText.setText(df.format(phase));
		} else if (selected instanceof String edge) {
			selectedLinearArbitraryEdge = edge;
			logger.debug("Selected arbitrary edge: {}", edge);
		}
	}

	public PolarisationParameters getScanParameters() {
		return scanParameters;
	}

	public void setEdgeToPhase(Map<String, Double> edgeToPhase) {
		this.edgeToPhase = edgeToPhase;
	}

	public void setPolarisationToPhase(Map<Polarisation, Double> polarisationToPhase) {
		this.polarisationToPhase = polarisationToPhase;
	}

	public void setLinearArbitraryEdges(List<String> linearArbitraryEdges) {
	    this.linearArbitraryEdges = linearArbitraryEdges;
	}

	public int getSelectedDegree() {
		return selectedDegree;
	}

	public List<Integer> getDegreesList() {
		return degreesList;
	}

	public String getSelectedLinearArbitraryEdge() {
		return selectedLinearArbitraryEdge;
	}

	public void setSelectedLinearArbitraryEdge(String selectedLinearArbitraryEdge) {
		this.selectedLinearArbitraryEdge = selectedLinearArbitraryEdge;
	}
}