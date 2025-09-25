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

import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createColumnHeader;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.icon;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.diamond.daq.mapping.ui.MappingImageConstants;
import uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleSetupPanel.Position;

public class HolderSelectionComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HolderSelectionComposite.class);

	public static final int LABEL_WIDTH = 100;
	public static final int STATUS_WIDTH = 40;
	public static final int TEXT_WIDTH = 200;

    private boolean loadFromFile = false;

    private final Map<Position, HolderState> holderStates;
    private Map<Position, HolderUI> holderUIs = new EnumMap<>(Position.class);

	public HolderSelectionComposite(Composite parent, int style, Map<Position, HolderState> holderStates) {
        super(parent, style);
        this.holderStates = holderStates;
    	setLayout(new GridLayout(1, false));

        var title = LabelFactory.newLabel(SWT.NONE).create(this);
        title.setText("Assign samples to holders and spigot");
        title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        createUI();
    }

	private void createUI() {
        createColumnHeaders(this);

        // confirmation dialog at initialisation to init sample holders
        if (confirmPopulateFromFile()) {
            loadStateFromFile();
        } else {
        	initHolderStates();
        }

		for (Position pos : Position.values()) {
			createSampleHolderComposite(this, pos);
		}
	}

	public void updateUI() {
		for (Position pos : Position.values()) {
			restoreUI(pos);
		}
	}

	/**
	 * Creates the column headers for a UI layout, above the table of sample holders.
	 * This includes labels for "Holder", "Status", and "Sample Name".
	 *
	 * @param parent the parent composite to which the header row will be added
	 */
	public static void createColumnHeaders(Composite parent) {
		var headerComposite = new Composite(parent, SWT.NONE);
        headerComposite.setLayout(new GridLayout(4, false));

        var holderLabel = createColumnHeader(headerComposite, "Holder", LABEL_WIDTH);
        holderLabel.setToolTipText("Holder position.");
        var statusLabel = createColumnHeader(headerComposite, "Status", STATUS_WIDTH);
        statusLabel.setToolTipText("Indicates if the holder is currently in use (checked = busy).");
        var sampleNameLabel = createColumnHeader(headerComposite, "Sample Name", TEXT_WIDTH);
        sampleNameLabel.setToolTipText("Name of the sample assigned to this holder position.");
	}

	/**
	 * Creates the composite UI element for a single sample holder.
	 * This includes the holder's label, a checkbox for the usage status and a text for the sample name.
	 *
	 * @param parent the parent composite that will contain the holder composite
	 * @param pos the logical position of the sample holder (e.g., position_3, position_5, etc.)
	 */
    private void createSampleHolderComposite(Composite parent, Position pos) {
    	var composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(4, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createPositionLabel(composite, pos);
        createHolderCheckboxRow(composite, pos);
    }

    private void createHolderCheckboxRow(Composite parent, Position pos) {
    	var checkBox = new Button(parent, SWT.CHECK);
        var icon = icon(parent, MappingImageConstants.IMG_GREEN);
        var text = createSampleNameText(parent);
        holderUIs.put(pos, new HolderUI(icon, text, checkBox));

        // If we are loading from file, we update the UI components
        if (loadFromFile) {
        	restoreUI(pos);
        }

        // Add listeners to checkbox and text
        checkBox.addSelectionListener(SelectionListener.widgetSelectedAdapter(e ->
        	holderUIs.get(pos).updateUI(checkBox.getSelection(), true)));

        text.addModifyListener(e -> saveHolderMapState(pos, checkBox.getSelection(), text.getText()));
    }

    private void restoreUI(Position pos) {
    	var holderState = holderStates.get(pos);
    	var holderUI = holderUIs.get(pos);
        logger.debug("Restoring UI for pos={}, sampleName='{}', busy='{}'", pos, holderState.getSampleName(), holderState.isBusy());
        var checkBox = holderUI.getButton();
        checkBox.setSelection(holderState.isBusy());
    	holderUI.updateUI(checkBox.getSelection(), false);
    	holderUI.getText().setText(holderState.getSampleName());
    }

    private void saveHolderMapState(Position pos, boolean selected, String sampleName) {
    	// Get the existing state or create a new one if it doesn't exist yet.
    	// At initialisation we populate the map so all entries should exist already
        HolderState state = holderStates.computeIfAbsent(pos, p -> new HolderState(p, false, ""));

        state.setPosition(pos);
        state.setBusy(selected);
        state.setSampleName(sampleName);

        saveHolderMapStateToFile();
    }

    private void saveHolderMapStateToFile() {
        try {
        	// Save the updated map's values to the file
        	HolderStateManager.saveStateToFile(new ArrayList<>(holderStates.values()));
        } catch (Exception e) {
        	logger.error("Failed to save holder states to file", e);
        	displaySaveErrorMessage();
        }
    }

    private void loadStateFromFile() {
    	try {
    		List<HolderState> loadedStates = HolderStateManager.loadStateFromFile();
    		for (HolderState state : loadedStates) {
    			logger.debug("Loaded HolderState: pos={}, sampleName='{}', busy={}",
    			        state.getPosition(), state.getSampleName(), state.isBusy());
            	Position pos = state.getPosition();
                holderStates.put(pos, state);
            }
            loadFromFile = true;
    	} catch (Exception e) {
        	logger.error("Could not read the states from the file system");
        	displayErrorMessage();
        	initHolderStates();
    	}
    }

	private void initHolderStates() {
		for (Position pos : Position.values()) {
			saveHolderMapState(pos, false, "");
		}
		loadFromFile = false;
	}

    public static void createPositionLabel(Composite parent, Position pos) {
    	var positionLabel = new Label(parent, SWT.NONE);
        positionLabel.setText(pos.toString().toLowerCase());

        GridData positionLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        positionLabelData.widthHint = LABEL_WIDTH;
        positionLabel.setLayoutData(positionLabelData);
    }

    public static Text createSampleNameText(Composite parent) {
        var text = new Text(parent, SWT.BORDER);
        GridData sampleNameTextData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        sampleNameTextData.widthHint = TEXT_WIDTH;
        text.setLayoutData(sampleNameTextData);
        text.setEnabled(false);
        text.setTextLimit(30);
        return text;
    }

	private boolean confirmPopulateFromFile() {
        Shell shell = getShell();
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBox.setMessage("Do you want to load sample holder information from file?");
        messageBox.setText("Load Data");

        int response = messageBox.open();
        return response == SWT.YES;
    }

	private void displaySaveErrorMessage() {
	    Shell shell = getShell();
	    MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
	    String errorMessage = String.join(System.lineSeparator(),
	    	    "The application was unable to save the holder states to the file system.",
	    	    "Please check that the file path exists and is writable.",
	    	    "Your recent changes may not be saved."
	    	);
	    messageBox.setMessage(errorMessage);
	    messageBox.setText("Save Failed");
	    messageBox.open();
	}

	private void displayErrorMessage() {
		Shell shell = getShell();
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		String errorMessage = String.join(System.lineSeparator(),
			    "The application could not read the states from the file system.",
			    "All holders' positions will be set to empty.",
			    "Please check the camera and re-enter the values."
			);
		messageBox.setMessage(errorMessage);
		messageBox.setText("Error");
		messageBox.open();
	}

	private class HolderUI {
	    private final Label indicatorIcon;
	    private final Text sampleNameText;
	    private final Button button;

	    HolderUI(Label indicatorIcon, Text sampleNameText, Button button) {
	        this.indicatorIcon = indicatorIcon;
	        this.sampleNameText = sampleNameText;
	        this.button = button;
	    }

	    void updateUI(boolean busy, boolean updateText) {
	    	String icon = busy ? MappingImageConstants.IMG_RED : MappingImageConstants.IMG_GREEN;
	        if (!indicatorIcon.isDisposed()) indicatorIcon.setImage(Activator.getImage(icon));

	        sampleNameText.setEnabled(busy);

	        if (updateText) {
		        // listener for text will save the holder map state
		        sampleNameText.setText(busy ? "unnamed_sample" : "");
	        }
	    }

		public Button getButton() {
			return button;
		}

		public Text getText() {
			return sampleNameText;
		}
	}
}
