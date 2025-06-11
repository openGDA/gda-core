/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.COLOUR_GREY;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.HEIGHT;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.SECTION_HEIGHT;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.STATE_COMPOSITE_WIDTH;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.STEPS_COMPOSITE_WIDTH;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.STEP_HEIGHT;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.TRANSITION_BUTTONS_WIDTH;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.WIDTH;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.comboViewer;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.composite;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createButtonWithLayoutData;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createButtonsComposite;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createSectionWithLabel;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createSectionWithText;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createSeparator;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createStartStopButton;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createTitle;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.formatTextToFitWidth;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.formatWord;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.gridData;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.icon;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.diamond.daq.mapping.ui.MappingImageConstants;
import uk.ac.gda.core.sampletransfer.SampleSelection;
import uk.ac.gda.core.sampletransfer.Sequence;
import uk.ac.gda.core.sampletransfer.SequenceCommand;
import uk.ac.gda.core.sampletransfer.SequenceRequest;
import uk.ac.gda.core.sampletransfer.State;
import uk.ac.gda.core.sampletransfer.StepStatus;
import uk.ac.gda.core.sampletransfer.Transition;

public class SampleTransferComposite extends Composite implements StepStatusListener {
	private static final Logger logger = LoggerFactory.getLogger(SampleTransferComposite.class);

	// state ui components
	private Composite stateComposite;
	private Label sampleStateLabel;

	private List<Button> transitionButtons;
	private Composite transitionButtonsComposite;

	private Label sequenceNameLabel;
	private Label sequenceStateLabel;

	private boolean busy = false;
	private ProgressBar progressBar;

	private Text errorMessageText;

	// sequence ui components
	private Composite sequenceComposite;

	private Button startButton;
	private Button stopButton;

	private ComboViewer sampleCombo;
	private SampleSelection sampleSelected;

	// step labels components
	private Composite stepsComposite;
	private Label indicatorIcon;
	private Label stepLabel;
	private Label warningIcon;
	private Button continueButton;

	// retry button components
	private Button retryButton;
	private Composite retryComposite;

	// current state, transition and sequence
	private Transition currentTransition;
	private Sequence currentSequence;

	private SampleTransferController controller;

	public SampleTransferComposite(Composite parent) {
		super(parent, SWT.NONE);
		controller = new SampleTransferController(this);
		controller.connect();
		configureCompositeLayout();
		createStateSection();
		updateSampleState(State.IN_HOTEL);
	}

	/**
	 * Defines two columns for the composite and the overall width and height.
	 * Creates a composite to display the sample transfer state and another
	 * composite to display the sequence controls and sequence steps.
	 */
	private void configureCompositeLayout() {
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);
		GridDataFactory.swtDefaults().hint(WIDTH, HEIGHT).applyTo(this);

	    stateComposite = composite(this, 1, STATE_COMPOSITE_WIDTH, HEIGHT);
	    stateComposite.setLayoutData(gridData(STATE_COMPOSITE_WIDTH));

	    sequenceComposite = composite(this, 1, STEPS_COMPOSITE_WIDTH, HEIGHT);
	    sequenceComposite.setLayoutData(gridData(STEPS_COMPOSITE_WIDTH));
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
		button.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleTransitionSelection));

		// adds a minimum width to the button layout
		var buttonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
	    buttonGridData.minimumWidth = TRANSITION_BUTTONS_WIDTH;
	    button.setLayoutData(buttonGridData);

	    // adds button to list of buttons
		transitionButtons.add(button);
		transitionButtonsComposite.layout(true, true);
	}

	private void createSequenceComponents() {
		var composite = composite(sequenceComposite, 6, STEPS_COMPOSITE_WIDTH, SECTION_HEIGHT);
		createStartButton(composite);
		createStopButton(composite);
		createStepsControls();
		createSampleControls(composite);
		sequenceComposite.layout(true, true);
		sequenceComposite.redraw();
		createSeparator(sequenceComposite);
		prepareSequenceForStart();
	}

	private void createStartButton(Composite composite) {
		icon(composite, MappingImageConstants.IMG_PLAY);
		startButton = createStartStopButton(composite, "Start");
		startButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleStartButtonSelection));
	}

	private void createStopButton(Composite composite) {
		icon(composite, MappingImageConstants.IMG_STOP);
		stopButton = createStartStopButton(composite, "Stop");
		stopButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleStopButtonSelection));
	}

	private void createStepsControls() {
		stepsComposite = composite(sequenceComposite, 1, STEPS_COMPOSITE_WIDTH, HEIGHT);
		stepsComposite.layout(true, true);
	}

	private void createSampleControls(Composite composite) {
		if (currentSequence.requiresSampleSelection()) {
			createSampleSelectionDropdown(composite);
		} else if (currentSequence.requiresSample()) {
			createSampleLabel(composite);
		}
	}

	private void createSampleSelectionDropdown(Composite composite) {
		var selectSampleLabel = LabelFactory.newLabel(SWT.NONE).create(composite);
		selectSampleLabel.setText("Select sample: ");
		sequenceComposite.redraw();
		sampleCombo = comboViewer(composite);
		sampleCombo.addSelectionChangedListener(e -> handleSampleSelection(sampleCombo.getStructuredSelection()));
	}

	private void createSampleLabel(Composite composite) {
		var label = LabelFactory.newLabel(SWT.NONE).create(composite);
		label.setText("Selected sample: ");
		var sampleSelectedText = text(composite);
		sampleSelectedText.setText(sampleSelected.name());
		sampleSelectedText.setEnabled(false);
	}

	private void createStep(String label, boolean isClientAction) {
		var composite = composite(stepsComposite, 3, STEPS_COMPOSITE_WIDTH - 100, STEP_HEIGHT);

		indicatorIcon = icon(composite, MappingImageConstants.IMG_YELLOW);

		stepLabel = LabelFactory.newLabel(SWT.NONE).create(composite);
		stepLabel.setText(label);

		var gridData = new GridData(SWT.RIGHT, SWT.RIGHT, true, false);
		gridData.heightHint = STEP_HEIGHT;

		if (isClientAction) {
			createContinueButton(composite, gridData);
		} else {
			// create filler
			retryComposite = composite(composite, 2);
			retryComposite.setLayoutData(gridData);
		}
		stepsComposite.layout(true, true);
	}

	private void createContinueButton(Composite composite, GridData gridData) {
		var continueComposite = composite(composite, 2);
		continueComposite.setLayoutData(gridData);

		warningIcon = icon(continueComposite, MappingImageConstants.IMG_WARNING);
		continueButton = createButtonWithLayoutData(continueComposite, "Continue");
		continueButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(selection -> sendResumeRequest(continueButton)));
	}

	private void createRetryButton() {
		warningIcon = icon(retryComposite, MappingImageConstants.IMG_WARNING);
		retryButton = createButtonWithLayoutData(retryComposite, "Retry");
		retryButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(selection -> sendRetryRequest()));
		stepsComposite.layout(true, true);
	}

	private void handleStartButtonSelection(@SuppressWarnings("unused") SelectionEvent selection) {
		if (currentSequence.requiresSampleSelection() && sampleSelected == null) {
		    MessageDialog.openInformation(getShell(), "Info", "No sample has been selected");
		    return;
		}

		sendStartRequest();

		startButton.setEnabled(false);
		stopButton.setEnabled(true);
		if (currentSequence.requiresSampleSelection())  sampleCombo.getCombo().setEnabled(false);
	}

	private void handleStopButtonSelection(@SuppressWarnings("unused") SelectionEvent selection) {
		sendStopRequest();
		stopButton.setEnabled(false);
		if (continueButton != null && !continueButton.isDisposed()) continueButton.setEnabled(false);
	}

	private void handleSampleSelection(IStructuredSelection selection) {
		sampleSelected = (SampleSelection) selection.getFirstElement();
	}

	/**
	 * Handles the selection of a transition.
	 * Updates the current transition and sequence based on the selection,
	 * then creates the components for the selected sequence and updates the UI.
	 *
	 * @param selection the event triggered by the user selecting a transition
	 */
	private void handleTransitionSelection(SelectionEvent selection) {
		currentTransition = (Transition) selection.widget.getData();
		// set transition's initial sequence
		currentSequence = currentTransition.getSequences().get(0);
		createSequenceComponents();
	}

	private void handleStepRunning(StepStatus stepStatus) {
		updateStepStatus(!stepStatus.isClientAction(), MappingImageConstants.IMG_GREEN);
		createStep(stepStatus.getDescription(), stepStatus.isClientAction());
	}

	private void handleSequenceTerminated() {
		updateStepStatus(false, MappingImageConstants.IMG_RED);
		stopButton.setEnabled(false);
	}

	private void handleSequenceError(String errorMessage) {
		handleSequenceTerminated();

	    createRetryButton();
	    errorMessageText.setText(formatTextToFitWidth(errorMessage));
	    errorMessageText.getParent().layout();
	}

	/**
	 * Handles the completion of the current sequence.
	 * It updates the UI based on whether the current sequence is the last one in the transition:
	 * - If it is the last sequence, the sample state is updated and the next state is triggered.
	 * - If it is not the last sequence, the UI is updated to prepare for the next sequence.
	 */
	private void handleSequenceCompletion() {
		updateStepStatus(false, MappingImageConstants.IMG_GREEN);
		// clear sequence composite contents
        clearContents(sequenceComposite);
        layout(true, true);

		var sequences = currentTransition.getSequences();
		var lastSequence = sequences.get(sequences.size() - 1);
		boolean isLastSequence = currentSequence.equals(lastSequence);
		// if current sequence is the last one in the transition, go to next state
		if (isLastSequence) {
			var nextState = currentTransition.getNextState();
			updateSampleState(nextState);
		// if not, go to next sequence
		} else {
			updateSequence(sequences);
			createSequenceComponents();
		}
	}


	/**
	 * Update sequence and transition labels, disables the transition buttons,
	 * and enables the start button to start sequence.
	 */
	private void prepareSequenceForStart() {
		sequenceNameLabel.setText(formatWord(currentSequence.name()));
		transitionButtons.stream()
			.forEach(button -> button.setEnabled(false));
		startButton.setEnabled(true);
	}

	private void updateSampleState(State state) {
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

	private void updateSequence(List<Sequence> sequences) {
		// get index of current sequence
		var currentIndex = sequences.indexOf(currentSequence);
		// go to next sequence in the transition
		currentSequence = sequences.get(currentIndex + 1);
		sequenceNameLabel.setText(formatWord(currentSequence.name()));
		sequenceStateLabel.setText("Not started");
	}

	private void updateStepStatus(boolean busy, String iconName) {
		this.busy = busy;
		if (indicatorIcon != null && !indicatorIcon.isDisposed()) indicatorIcon.setImage(Activator.getImage(iconName));
		if (stepLabel != null && !stepLabel.isDisposed()) stepLabel.setForeground(COLOUR_GREY);
		if (warningIcon != null && !warningIcon.isDisposed()) warningIcon.setText("");
		if (progressBar != null && !progressBar.isDisposed()) progressBar.setVisible(isBusy());
	}

	private void sendStartRequest() {
		sendCommand(SequenceCommand.START);
	}

	private void sendResumeRequest(Button continueButton) {
		sendCommand(SequenceCommand.RESUME);
		continueButton.setEnabled(false);
	}

	private void sendRetryRequest() {
		retryButton.setEnabled(false);
		sendCommand(SequenceCommand.RETRY);
	}

	private void sendStopRequest() {
		sendCommand(SequenceCommand.STOP);
	}

	@Override
	public void dispose() {
		sendStopRequest();
		controller.disconnect();
		clearContents(this);
	}

	private boolean isBusy() {
		return busy;
	}

	private void sendCommand(SequenceCommand command) {
		controller.broadcast(new SequenceRequest(currentSequence, sampleSelected, command));
	}

	private void clearContents(Composite composite) {
		if (!composite.isDisposed()) {
	        for (Control control : composite.getChildren()) {
	            control.dispose();
	        }
		}
    }

	@Override
	public void onStepRunning(StepStatus status) {
		Display.getDefault().asyncExec(() -> handleStepRunning(status));
	}

	@Override
	public void onSequenceTerminated(StepStatus status) {
		Display.getDefault().asyncExec(() -> handleSequenceTerminated());
	}

	@Override
	public void onSequenceCompleted(StepStatus status) {
		Display.getDefault().asyncExec(() -> handleSequenceCompletion());
	}

	@Override
	public void onSequenceFailed(StepStatus status) {
		Display.getDefault().asyncExec(() -> handleSequenceError(status.getMessage()));

	}

	@Override
	public void onSequenceStatusUpdate(StepStatus status) {
		sequenceStateLabel.setText(formatWord(status.getStatus().name()));
	}
}
