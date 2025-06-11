/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.HEIGHT;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.SECTION_HEIGHT;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.STATE_COMPOSITE_WIDTH;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.TRANSITION_BUTTONS_WIDTH;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.composite;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createButtonsComposite;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createSectionWithLabel;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createSectionWithText;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createTitle;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.formatTextToFitWidth;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.formatWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import uk.ac.gda.core.sampletransfer.State;
import uk.ac.gda.core.sampletransfer.Transition;

public class StatePanel {

	private Composite stateComposite;
	private Label sampleStateLabel;

	private List<Button> transitionButtons;
	private Composite transitionButtonsComposite;

	private Label sequenceNameLabel;
	private Label sequenceStateLabel;

	private ProgressBar progressBar;

	private Text errorMessageText;

	private Consumer<Transition> onTransitionSelected;

	public StatePanel (Composite stateComposite, Consumer<Transition> onTransitionSelected) {
		this.stateComposite = stateComposite;
		this.onTransitionSelected = onTransitionSelected;
		createStateSection();
	}

	/**
	 * Creates a composite to display the sample transfer state.
	 */
	private void createStateSection() {
		var composite = composite(stateComposite, 1, STATE_COMPOSITE_WIDTH, HEIGHT);
		sampleStateLabel = createSectionWithLabel(composite, "Sample state: ");
		transitionButtonsComposite = createButtonsComposite(composite, "Select transition: ");
		sequenceNameLabel = createSectionWithLabel(composite, "Sequence name");
		sequenceStateLabel = createSectionWithLabel(composite, "Sequence state");

		var progressComposite = composite(composite, 1, STATE_COMPOSITE_WIDTH, 200);
		createTitle(progressComposite, "Step state: ");
		createProgressBarControls(progressComposite);
		errorMessageText = createSectionWithText(progressComposite);
	}

	private void createProgressBarControls(Composite parent) {
		var composite = composite(parent, 1, 250, SECTION_HEIGHT);
		progressBar = new ProgressBar(composite, SWT.INDETERMINATE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		progressBar.setVisible(false);
	}

	private void createButton(Transition transition, int index) {
		// if it is not the first button being created, place an empty label for alignment
		if (index != 0) {
			var placeholder = LabelFactory.newLabel(SWT.NONE).create(transitionButtonsComposite);
			placeholder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		// creates a button with the transition information
		var button = ButtonFactory.newButton(SWT.PUSH).create(transitionButtonsComposite);
		button.setData(transition);
		button.setText(formatWord(transition.name()));
		button.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			Transition t = (Transition) e.widget.getData();
            onTransitionSelected.accept(t);
		}));

		// adds a minimum width to the button layout
		var buttonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
	    buttonGridData.minimumWidth = TRANSITION_BUTTONS_WIDTH;
	    button.setLayoutData(buttonGridData);

	    // adds button to list of buttons
		transitionButtons.add(button);
		transitionButtonsComposite.layout(true, true);
	}

	public void updateSampleState(State state) {
		sampleStateLabel.setText(formatWord(state.name()));

		// initialises an empty list of buttons
		transitionButtons = new ArrayList<>();
		// dispose any components that were already created from previous sequences
		if (transitionButtonsComposite.getChildren().length > 0) {
			Arrays.stream(transitionButtonsComposite.getChildren())
			.filter(Button.class::isInstance)
			.forEach(Widget::dispose);
		}

		var allowedTransitions = state.getAllowedTransitions();
		// create a button for each transition that is allowed in the current state
		IntStream.range(0, allowedTransitions.size())
			.forEach(i -> createButton(allowedTransitions.get(i), i));
		transitionButtonsComposite.layout(true, true);

		sequenceStateLabel.setText("Not started");
	}

	public void showErrorMessage(String errorMessage) {
	    errorMessageText.setText(formatTextToFitWidth(errorMessage));
	    errorMessageText.getParent().layout();
	}

	public void updateProgressBar(boolean busy) {
		if (progressBar != null && !progressBar.isDisposed()) progressBar.setVisible(busy);
	}

	public void updateSequenceStateLabel(String stateLabel) {
		sequenceStateLabel.setText(stateLabel);
	}

	public void updateSequenceNameLabel(String sequenceName) {
		sequenceNameLabel.setText(sequenceName);
	}

	public void disableTransitionButtons() {
		transitionButtons.stream()
			.forEach(button -> button.setEnabled(false));
	}

}
