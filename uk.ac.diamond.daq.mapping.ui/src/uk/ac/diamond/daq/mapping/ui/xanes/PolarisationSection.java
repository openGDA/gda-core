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
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.PolarisationParameters;
import uk.ac.diamond.daq.mapping.api.PolarisationParameters.Phase;
import uk.ac.diamond.daq.mapping.api.PolarisationParameters.Polarisation;
import uk.ac.diamond.daq.mapping.api.PolarisationParameters.Polarisation.Direction;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;

public class PolarisationSection extends AbstractHideableMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(PolarisationSection.class);

	private static final int COMBO_WIDTH = 60;
	private static final int TEXT_WIDTH = 40;

	private Text phaseText;
	private List<Phase> phaseList;
	private ComboViewer phaseCombo;
	private Composite buttonComposite;
	private List<Button> radioButtons;

	private PolarisationParameters scanParameters;

	private String scriptFilePath;
	private Map<String, Double> edgeToPhase = Collections.emptyMap();
	private Map<Polarisation, Double> polarisationToPhase = Collections.emptyMap();

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		if (scriptFilePath == null || scriptFilePath.isEmpty()) {
			logger.error("Script file path has not been defined");
			return;
		}

		if (edgeToPhase == null || edgeToPhase.isEmpty()) {
			logger.error("Element and edges have not been defined");
			return;
		}

		if (polarisationToPhase == null || polarisationToPhase.isEmpty()) {
			logger.error("Phases position for linear polarisation have not been defined");
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
		buttonComposite = createComposite(parent, 4, true);
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
		var comboComposite = createComposite(parent, 4, true);
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
				return ((Phase) element).getElement();
			}
		});
		phaseCombo.addSelectionChangedListener(this::handleEdgeSelectionChanged);
		GridDataFactory.swtDefaults().hint(COMBO_WIDTH, SWT.DEFAULT).applyTo(phaseCombo.getCombo());

		LabelFactory.newLabel(SWT.NONE).text("Phase: ").create(comboComposite);
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

	private void handleButtonSelectionChanged(SelectionEvent event) {
		var button = (Button) event.getSource();
		var polarisation = (Polarisation) button.getData();

		if (polarisation.getDirection().equals(Direction.LINEAR)) {
			phaseCombo.setInput(Collections.emptyList());
			phaseCombo.getCombo().setEnabled(false);

			var phasePosition = polarisationToPhase.get(polarisation);
			phaseText.setText(String.valueOf(phasePosition));

		} else {
			phaseCombo.setInput(phaseList);
			phaseCombo.setSelection(new StructuredSelection(phaseList.get(0)));
			phaseCombo.getCombo().setEnabled(true);
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
		var element = (Phase) selection.getStructuredSelection().getFirstElement();
		var phase = element.getPosition();
		var df = new DecimalFormat("#.0#");

		if (getScanParameters().getPolarisation().equals(Polarisation.CL)) {
			phase *= -1;
		}

		phaseText.setText(df.format(phase));
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

	public String getScriptFilePath() {
		return scriptFilePath;
	}

	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}

}
