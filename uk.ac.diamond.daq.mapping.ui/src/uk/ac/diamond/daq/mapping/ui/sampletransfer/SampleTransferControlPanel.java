package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SampleTransferControlPanel class provides the user interface for controlling the
 * sample transfer process. It allows activating and deactivating the sample transfer,
 * setting up sample holder positions, and moving samples between the spigot and holders.
 */
public class SampleTransferControlPanel extends Composite {
    private static final Logger logger = LoggerFactory.getLogger(SampleTransferControlPanel.class);

    private static final String WITH_SAMPLE_LABEL = "With Sample";
    private static final String NO_SAMPLE_LABEL = "No Sample";

    private static final int LABEL_WIDTH = 70;
    private static final int STATE_WIDTH = 80;
    private static final int NAME_TEXT_WIDTH = 120;
    private static final int MOVE_BUTTON_WIDTH = 140;

    private Button deactivateButton;
    private Label statusLabel;
    private boolean isActivated = true;

    public enum Position { POSITION_3, POSITION_5, POSITION_7, SPACE_4, SPIGOT }

    private Map<Position, Button> checkBoxMap = new EnumMap<>(Position.class);
    private Map<Position, Text> stateTextMap = new EnumMap<>(Position.class);
    private Map<Position, Text> sampleNameTextMap = new EnumMap<>(Position.class);
    private Map<Position, Button> moveButtonMap = new EnumMap<>(Position.class);

    private Button spigotMoveButton;
    private ComboViewer positionCombo;
    private Position positionSelected;

    private Button saveButton;
    private Button clearButton;
    private Label saveStatusLabel;
    private boolean isSaved = false;

    public SampleTransferControlPanel(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(1, false));


        createSampleHotelSection(this);
        createSpigotSection(this);
        createSaveButtons(this);
        createDeactivateButton(this);

        // set initial state
        Arrays.stream(Position.values()).forEach(position -> setSelection(position, false));
    }

    public void createDeactivateButton(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        deactivateButton = ButtonFactory.newButton(SWT.PUSH).create(composite);
        deactivateButton.setText("Deactivate Sample Transfer");
        deactivateButton.addListener(SWT.Selection, event -> deactivateSampleTransfer());

        statusLabel = LabelFactory.newLabel(SWT.NONE).create(composite);
        statusLabel.setText("System State: Activated");
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    }

    private void deactivateSampleTransfer() {
    	statusLabel.setText("Deactivating...");
    	logger.info("Deactivating sample transfer...");
    	isActivated = false;
    	deactivateButton.setEnabled(false);
    	statusLabel.setText("System State: Deactivated");
    }

    /**
     * Create section for selecting the hotel and spigot samples
     * @param parent
     */
    private void createSampleHotelSection(Composite parent) {
        var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

        var title = LabelFactory.newLabel(SWT.NONE).create(composite);
        title.setText("Sample Holders");
        title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        // create a section for each position
        Arrays.stream(Position.values())
        	.filter(value -> !value.equals(Position.SPIGOT))
        	.forEach(value -> createSampleHolderComposite(composite, value));
    }

    private void createSampleHolderComposite(Composite parent, Position pos) {
        var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
        composite.setLayout(new GridLayout(5, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

        createPositionLabel(composite, pos);
        createSampleCheckBox(composite, pos);
        createStateIndicatorText(composite, pos);
        createSampleNameField(composite, pos);
        createMoveToSpigotButton(composite, pos);
    }

    private void createPositionLabel(Composite parent, Position pos) {
        var positionLabel = LabelFactory.newLabel(SWT.NONE).create(parent);
        positionLabel.setText(pos.toString().toLowerCase());

        // set layout data
        GridData positionLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        positionLabelData.widthHint = LABEL_WIDTH;
        positionLabel.setLayoutData(positionLabelData);
    }

    private void createSampleCheckBox(Composite parent, Position pos) {
    	var checkBox = ButtonFactory.newButton(SWT.CHECK).create(parent);
    	// add selection listener
        checkBox.addSelectionListener(createCheckBoxSelectionAdapter(pos));
        // add to map
        checkBoxMap.put(pos, checkBox);
    }

    private Text createStateIndicatorText(Composite parent, Position pos) {
        var stateText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);

        // set layout data
        GridData emptyFullTextData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        emptyFullTextData.widthHint = STATE_WIDTH;
        stateText.setLayoutData(emptyFullTextData);

        // add to map
        stateTextMap.put(pos, stateText);

        return stateText;
    }

    private void createSampleNameField(Composite parent, Position pos) {
        var sampleNameText = new Text(parent, SWT.BORDER);

        // set layout data
        GridData sampleNameTextData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        sampleNameTextData.widthHint = NAME_TEXT_WIDTH;
        sampleNameText.setLayoutData(sampleNameTextData);

        // disable editing sample name
        sampleNameText.setEnabled(false);

        // add modify listener
        sampleNameText.addModifyListener(createTextModifiedListener());

        // add to map
        sampleNameTextMap.put(pos, sampleNameText);
    }

    /**
     * Creates a ModifyListener that changes the background color of a text field
     * based on whether it is empty or contains text.
     * White for empty, gray for filled.
     *
     * @return The ModifyListener to be added to the text field
     */
	private ModifyListener createTextModifiedListener() {
	    return event -> {
	        String text = ((Text) event.widget).getText();
	        Text widget = (Text) event.widget;
	        if (text.isEmpty() || text.isBlank()) {
	        	widget.setBackground(widget.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	        } else {
	            widget.setBackground(widget.getDisplay().getSystemColor(SWT.COLOR_GRAY));
	        }
	    };
	}

    private void createMoveToSpigotButton(Composite parent, Position pos) {
        var moveButton = ButtonFactory.newButton(SWT.PUSH).create(parent);
        moveButton.setText("Transfer sample");

        // disable button
        moveButton.setEnabled(false);

        // set layout data
        GridData moveButtonData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        moveButtonData.widthHint = MOVE_BUTTON_WIDTH;
        moveButton.setLayoutData(moveButtonData);

        // add selection listener
        moveButton.addSelectionListener(createTransferSelectionAdapter(pos));

        // add to map
        moveButtonMap.put(pos, moveButton);
    }

    private void createSpigotSection(Composite parent) {
    	Position pos = Position.SPIGOT;

        var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

        var title = LabelFactory.newLabel(SWT.NONE).create(composite);
        title.setText("Measurement Position");
        title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        var mainComposite = CompositeFactory.newComposite(SWT.NONE).create(composite);
        mainComposite.setLayout(new GridLayout(6, false));
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

        createPositionLabel(mainComposite, pos);
        createSampleCheckBox(mainComposite, pos);
        createStateIndicatorText(mainComposite, pos);
        createSampleNameField(mainComposite, pos);

        positionCombo = comboViewer(mainComposite);
        // add selection listener
        positionCombo.addSelectionChangedListener(e -> handlePositionSelection(positionCombo.getStructuredSelection()));

        spigotMoveButton = ButtonFactory.newButton(SWT.PUSH).create(mainComposite);
        spigotMoveButton.setText("Unload to holder");
        spigotMoveButton.setEnabled(false);
        spigotMoveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        // add selection listener
        spigotMoveButton.addSelectionListener(createTransferSelectionAdapter(pos));

        moveButtonMap.put(Position.SPIGOT, spigotMoveButton);
    }

	public static ComboViewer comboViewer(Composite parent) {
		GridData gridData = new GridData();
		gridData.widthHint = STATE_WIDTH;

		var combo = new ComboViewer(parent);
		combo.getCombo().setLayoutData(gridData);
		combo.getCombo().setEnabled(false);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		var values = Arrays.stream(Position.values())
				.filter(position -> !position.equals(Position.SPIGOT))
				.toList();
		combo.setInput(values);
		combo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Position position = (Position) element;
				var name = position.toString();
				if (name.matches(".*\\d.*")) {
	                return name.replaceAll("\\D", "");  // remove non digit characters
	            } else {
	                return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase(); // format string
	            }

			}
		});
		return combo;
	}

    public void createSaveButtons(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        var title = LabelFactory.newLabel(SWT.NONE).create(composite);
        title.setText("Settings Control");
        title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        saveButton = ButtonFactory.newButton(SWT.PUSH).create(composite);
        saveButton.setText("Save Settings");
        saveButton.addListener(SWT.Selection, event -> saveSettings());

        clearButton = ButtonFactory.newButton(SWT.PUSH).create(composite);
        clearButton.setText("Clear Settings");
        clearButton.setEnabled(false);
        clearButton.addListener(SWT.Selection, event -> clearSettings());

        saveStatusLabel = LabelFactory.newLabel(SWT.NONE).create(composite);
        saveStatusLabel.setText("Settings: Unsaved");
        saveStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    }

    private void clearSettings() {
    	saveStatusLabel.setText("Enable editing...");
        logger.info("Enable editing.");
        updateSettingsState(false);
    }

    private void saveSettings() {
    	if (areSampleNamesPresent()) {
    		saveStatusLabel.setText("Saving Settings...");
            logger.info("Saving sample holder positions.");
            updateSettingsState(true);
    	} else {
    		logger.error("Cannot save settings. Some selected positions are missing sample names.");
    		MessageDialog.openError(Display.getDefault().getActiveShell(), "Error saving settings",
    				"Please make sure all selected positions marked as 'With Sample' have a sample name before proceeding.");
    	}
    }

    public boolean areSampleNamesPresent() {
        return checkBoxMap.entrySet().stream()
            .filter(entry -> entry.getValue().getSelection())
            .allMatch(entry -> {
                String sampleName = sampleNameTextMap.get(entry.getKey()).getText();
                return sampleName != null && !sampleName.isEmpty() && !sampleName.isBlank();
            });
    }

	private void updateSettingsState(boolean saved) {
		isSaved = saved;
		saveButton.setEnabled(!saved);
		clearButton.setEnabled(saved);
		saveStatusLabel.setText(saved ? "Settings: Saved" : "Settings: Unsaved");

		if (saved) {
			lockSettings();
		} else {
			clearFields();
		}
	}

	private void lockSettings() {
		checkBoxMap.values().forEach(value -> value.setEnabled(false));
		sampleNameTextMap.values().forEach(value -> value.setEnabled(false));
		moveButtonMap.keySet().forEach(this::enableButton);
	}

	private void clearFields() {
		checkBoxMap.values().forEach(value -> value.setEnabled(true));
		Arrays.stream(Position.values()).forEach(position -> setSelection(position, false));
		sampleNameTextMap.values().forEach(value -> value.setText(""));
		moveButtonMap.values().forEach(value -> value.setEnabled(false));
		positionCombo.getCombo().setEnabled(false);
	}

	private void updatePositionComboViewer() {
		if (positionCombo != null) {
			var emptyPositions = getPositionsBySelection(false);
			var shouldEnableCombo = isSampleInHolder(Position.SPIGOT);
			positionCombo.setInput(emptyPositions);
			positionCombo.getCombo().setEnabled(shouldEnableCombo);
			positionCombo.refresh();
		}
	}

	private boolean isSampleInHolder(Position pos) {
		return checkBoxMap.get(pos).getSelection();
	}

	private List<Position> getPositionsBySelection(boolean selected) {
	    return checkBoxMap.entrySet().stream()
	        .filter(entry -> entry.getValue().getSelection() == selected)
	        .map(Map.Entry::getKey)
	        .toList();
	}

    private SelectionAdapter createCheckBoxSelectionAdapter(Position pos) {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (stateTextMap.containsKey(pos)) {
                    boolean selected = ((Button) e.widget).getSelection();
                    handleCheckBoxSelection(pos, selected);
                }
            }
        };
    }

    private void setSelection(Position position, boolean select) {
    	var button = checkBoxMap.get(position);
    	button.setSelection(select);
		var selectionEvent = new Event();
		selectionEvent.widget = button;
		button.notifyListeners(SWT.Selection, selectionEvent);
    }

    private void transferSample(Position oldPos, Position newPos) {
    	// change sample name fields
    	var currentText = sampleNameTextMap.get(oldPos);
    	var newText = sampleNameTextMap.get(newPos);
    	newText.setText(currentText.getText());
    	currentText.setText(StringUtils.EMPTY);

    	// update check box
    	setSelection(newPos, true);
    	setSelection(oldPos, false);
    }

    //  move to/from position holders listeners
    private SelectionAdapter createTransferSelectionAdapter(Position pos) {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if (pos.equals(Position.SPIGOT)) {
                	transferSample(Position.SPIGOT, positionSelected);
            	} else {
            		transferSample(pos, Position.SPIGOT);
            	}
                ((Button) e.widget).setEnabled(false);
            }
        };
    }

	private void handlePositionSelection(IStructuredSelection selection) {
		positionSelected = (Position) selection.getFirstElement();
		var shouldEnableButton = isSampleInHolder(Position.SPIGOT);
		spigotMoveButton.setEnabled(shouldEnableButton);
	}

	private void enableButton(Position pos) {
		if (pos.equals(Position.SPIGOT)) {
			updatePositionComboViewer();
		} else {
			var shouldEnableButton = isSampleInHolder(pos) && !isSampleInHolder(Position.SPIGOT);
			moveButtonMap.get(pos).setEnabled(shouldEnableButton);
		}
	}

	private void handleCheckBoxSelection(Position pos, boolean selected) {
        var indicatorText = stateTextMap.get(pos);
        indicatorText.setText(selected ? WITH_SAMPLE_LABEL : NO_SAMPLE_LABEL);
        indicatorText.setBackground(indicatorText.getDisplay().getSystemColor(selected ? SWT.COLOR_GRAY : SWT.COLOR_WHITE));
		if (isSaved) {
			moveButtonMap.keySet().forEach(this::enableButton);
		} else {
			var sampleText = sampleNameTextMap.get(pos);
			sampleText.setEnabled(selected);
			// clear text if it was unselected
			if (!selected) sampleText.setText("");
		}
	}

	public boolean isActivated() {
		return isActivated;
	}
}
