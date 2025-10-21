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

import static uk.ac.diamond.daq.mapping.ui.sampletransfer.HolderSelectionComposite.createColumnHeaders;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.HolderSelectionComposite.createPositionLabel;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.HolderSelectionComposite.createSampleNameText;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.createButton;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.holderComboViewer;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.icon;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
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


public class HolderTransferComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HolderTransferComposite.class);

    private Map<Position, HolderState> holderStates;
    private Map<Position, HolderUI> holderUIs = new EnumMap<>(Position.class);

    private ComboViewer comboViewer;
    private Position holderSelected;

	public HolderTransferComposite(Composite parent, int style, Map<Position, HolderState> holderStates) {
		super(parent, style);
		this.holderStates = holderStates;
    	setLayout(new GridLayout(1, false));

    	var title = LabelFactory.newLabel(SWT.NONE).create(this);
        title.setText("Transfer samples to holders and spigot");
        title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        createUI();
        updateUI();
    }

	private void createUI() {
		createColumnHeaders(this);

		var holders = holderStates.values().stream()
			    .filter(state -> !state.getPosition().equals(Position.SPIGOT)).toList();
		holders.forEach(holder -> {
			createSampleHolderComposite(this, holder.getPosition(), false);
			var button = holderUIs.get(holder.getPosition()).getButton();
		    button.addSelectionListener(SelectionListener.widgetSelectedAdapter(e ->
    			transferSampleFrom(holder.getPosition())));
		});

		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		var spigot = holderStates.get(Position.SPIGOT);
		GridLayout spigotLayout = new GridLayout(5, false);
		GridData spigotLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		createSampleHolderComposite(this, spigot.getPosition(), true, spigotLayout, spigotLayoutData);
		var button = holderUIs.get(spigot.getPosition()).getButton();
		button.addSelectionListener(SelectionListener.widgetSelectedAdapter(e ->
			unloadToHolder(holderSelected)));
	}

	public void updateUI() {
        updateHoldersUI();
    	updateSpigotUI();
	}

    private void createSampleHolderComposite(Composite parent, Position pos, boolean createComboViewer, GridLayout layout, GridData layoutData) {
    	var composite = new Composite(parent, SWT.NONE);
        composite.setLayout(layout);
        composite.setLayoutData(layoutData);

        createPositionLabel(composite, pos);
        createHolderRow(composite, pos, createComboViewer);
    }

    private void createSampleHolderComposite(Composite parent, Position pos, boolean createComboViewer) {
        createSampleHolderComposite(parent, pos, createComboViewer, new GridLayout(5, false), new GridData(SWT.FILL, SWT.TOP, true, false));
    }


    private void createHolderRow(Composite parent, Position pos, boolean createComboViewer) {
    	var icon = icon(parent, MappingImageConstants.IMG_GREEN);
        var text = createSampleNameText(parent);
        if (createComboViewer) {
        	comboViewer = holderComboViewer(parent);
        	comboViewer.addSelectionChangedListener(event -> {
        		var selection = (IStructuredSelection) event.getSelection();
        		holderSelected = (Position) selection.getFirstElement();
        	    holderUIs.get(Position.SPIGOT).updateButton(true);
        	});
        }
        Button button = createButton(parent, "Transfer");
        button.setEnabled(false);
        holderUIs.put(pos, new HolderUI(icon, text, button));
    }

    private void unloadToHolder(Position toPos) {
    	// after transfer has been completed
    	updateState(Position.SPIGOT, toPos);
    	holderUIs.get(Position.SPIGOT).updateButton(false);
    	updateHoldersUI();
    	updateSpigotUI();
    }

    private void transferSampleFrom(Position fromPos) {
    	// after transfer has been completed
    	updateState(fromPos, Position.SPIGOT);
    	updateHoldersUI();
    	updateSpigotUI();
    }

    private void updateState(Position fromPos, Position toPos) {
    	HolderState fromState = holderStates.get(fromPos);
        HolderState toState = holderStates.get(toPos);

        var fromSampleName = fromState.getSampleName();
        fromState.setSampleName("");
        fromState.setBusy(false);

        toState.setSampleName(fromSampleName);
        toState.setBusy(true);

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

    private void updateHoldersUI() {
    	boolean spigotBusy = holderStates.get(Position.SPIGOT).isBusy();
        holderUIs.forEach((pos, ui) -> {
        	if (!pos.equals(Position.SPIGOT)) {
        		HolderState state = holderStates.get(pos);
        		ui.updateUI(state.isBusy(), state.getSampleName());
        		ui.updateButton(state.isBusy() && !spigotBusy);
        	}
        });
    }

    private void updateSpigotUI() {
    	var state = holderStates.get(Position.SPIGOT);
    	boolean spigotBusy = state.isBusy();
    	holderUIs.get(Position.SPIGOT).updateUI(spigotBusy, state.getSampleName());
    	List<Position> freeHolders = holderStates.entrySet().stream()
                .filter(e -> e.getKey() != Position.SPIGOT && !e.getValue().isBusy())
                .map(Map.Entry::getKey)
                .toList();
    	boolean hasFreeHolder = !freeHolders.isEmpty();
    	comboViewer.getCombo().setEnabled(spigotBusy && hasFreeHolder);
        comboViewer.setInput(freeHolders);
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

	private class HolderUI {
	    private final Label indicatorIcon;
	    private final Text sampleNameText;
	    private final Button button;

	    HolderUI(Label indicatorIcon, Text sampleNameText, Button button) {
	        this.indicatorIcon = indicatorIcon;
	        this.sampleNameText = sampleNameText;
	        this.button = button;
	    }

	    void updateUI(boolean busy, String sampleName) {
	        String icon = busy ? MappingImageConstants.IMG_RED : MappingImageConstants.IMG_GREEN;
	        if (!indicatorIcon.isDisposed()) indicatorIcon.setImage(Activator.getImage(icon));
	        sampleNameText.setText(sampleName);
	    }

	    void updateButton(boolean isEnabled) {
	    	button.setEnabled(isEnabled);
	    }

		public Button getButton() {
			return button;
		}
	}
}
