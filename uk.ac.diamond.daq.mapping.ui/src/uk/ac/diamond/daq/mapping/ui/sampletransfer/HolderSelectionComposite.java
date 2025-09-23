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
import java.util.Arrays;
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

public class HolderSelectionComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HolderSelectionComposite.class);

	public static final int LABEL_WIDTH = 100;
	public static final int STATUS_WIDTH = 40;
	public static final int TEXT_WIDTH = 200;

    public enum Position { POSITION_3, POSITION_5, POSITION_7, SPACE_4, SPIGOT }

    private Map<Position, HolderState> holderStates = new EnumMap<>(Position.class);

    private boolean loadFromFile = false;

	public HolderSelectionComposite(Composite parent, int style) {
        super(parent, style);
    	setLayout(new GridLayout(1, false));

        var title = LabelFactory.newLabel(SWT.NONE).create(this);
        title.setText("Assign samples to holders and spigot");
        title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        createColumnHeaders(this);

        // confirmation dialog at initialisation
        if (confirmPopulateFromFile()) {
            loadStateFromFile();
        } else {
        	initHolderStates();
        }

        Arrays.stream(Position.values())
        	.forEach(value -> createSampleHolderComposite(this, value));
    }

	private boolean confirmPopulateFromFile() {
        Shell shell = getShell();
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBox.setMessage("Do you want to load sample holder information from file?");
        messageBox.setText("Load Data");

        int response = messageBox.open();
        return response == SWT.YES;
    }

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

    private void createSampleHolderComposite(Composite parent, Position pos) {
    	var composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(4, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createPositionLabel(composite, pos);
        createStatusCheckBox(composite, pos);
    }

    public static void createPositionLabel(Composite parent, Position pos) {
    	var positionLabel = new Label(parent, SWT.NONE);
        positionLabel.setText(pos.toString().toLowerCase());

        GridData positionLabelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        positionLabelData.widthHint = LABEL_WIDTH;
        positionLabel.setLayoutData(positionLabelData);
    }

    private void createStatusCheckBox(Composite parent, Position pos) {
    	var checkBox = new Button(parent, SWT.CHECK);
        var indicatorIcon = icon(parent, MappingImageConstants.IMG_GREEN);

        var sampleNameText = new Text(parent, SWT.BORDER);
        GridData sampleNameTextData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        sampleNameTextData.widthHint = TEXT_WIDTH;
        sampleNameText.setLayoutData(sampleNameTextData);
        sampleNameText.setEnabled(false);
        sampleNameText.setTextLimit(30);

        if (loadFromFile && holderStates.containsKey(pos)) {
        	restoreUI(pos, checkBox, indicatorIcon, sampleNameText);
        }

        // Add listeners after the UI has been restored
        checkBox.addSelectionListener(SelectionListener.widgetSelectedAdapter(e ->
    		updateState(indicatorIcon, sampleNameText, checkBox.getSelection(), pos)));

        sampleNameText.addModifyListener(e -> saveHolderState(pos, true, sampleNameText.getText()));
    }

    private void updateState(Label indicatorIcon, Text sampleNameText, boolean selected, Position pos) {
    	updateUI(indicatorIcon, sampleNameText, selected);
    	if (selected) {
        	sampleNameText.setText("unnamed_sample");
    	}
		saveHolderState(pos, selected,sampleNameText.getText());
    }

    private void restoreUI(Position pos, Button checkBox, Label indicatorIcon, Text sampleNameText) {
    	var holderState = holderStates.get(pos);
        logger.debug("Restoring UI for pos={}, sampleName='{}'", pos, holderState.getSampleName());
    	checkBox.setSelection(holderState.isBusy());
    	updateUI(indicatorIcon, sampleNameText, holderState.isBusy());
    	sampleNameText.setText(holderState.getSampleName());
    }

    private void updateUI(Label indicatorIcon, Text sampleNameText, boolean selected) {
    	var iconName = "";
		if (selected) {
			iconName = MappingImageConstants.IMG_RED;
			sampleNameText.setEnabled(true);
		} else {
			iconName = MappingImageConstants.IMG_GREEN;
			sampleNameText.setEnabled(false);
			// Clearing name if the sample holder has been unselected
			sampleNameText.setText("");
		}
		if (indicatorIcon != null && !indicatorIcon.isDisposed()) indicatorIcon.setImage(Activator.getImage(iconName));
    }

    private void saveHolderState(Position pos, boolean selected, String sampleName) {
    	// Get the existing state or create a new one if it doesn't exist yet.
    	// At initialisation we populate the map so all entries should exist already
        HolderState state = holderStates.computeIfAbsent(pos, p -> new HolderState(p, false, ""));

        state.setPosition(pos);
        state.setBusy(selected);
        state.setSampleName(sampleName);

        saveStateToFile();
    }

    private void saveStateToFile() {
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
			holderStates.put(pos, new HolderState(pos, false, ""));
		}
		saveStateToFile();
		loadFromFile = false;
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
}
