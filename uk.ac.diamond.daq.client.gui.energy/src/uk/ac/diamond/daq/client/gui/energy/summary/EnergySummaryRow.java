/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.energy.summary;

import static uk.ac.gda.client.properties.stage.DefaultManagedScannable.BEAM_SELECTOR;
import static uk.ac.gda.client.properties.stage.DefaultManagedScannable.EH_SHUTTER;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.WidgetUtilities.getDataObject;
import static uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy.getBean;

import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.camera.ValveState;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.properties.stage.ManagedScannable;
import uk.ac.gda.client.properties.stage.ScannablesPropertiesHelper;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.WidgetUtilities;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Displays in tabular form some essential energy properties
 *
 * <p>
 * The actual implementations includes three columns: beam selector position, beam energy
 * and a shutter monitor
 * </p>
 *
 * @author Maurizio Nagni
 */
class EnergySummaryRow {
	private static final Logger logger = LoggerFactory.getLogger(EnergySummaryRow.class);
	private static final String LAYOUT = "Layout";

	private final Table table;
	private final TableItem tableItem;

	private Label beamType;
	private Label energyValue;

	enum ShutterCommand {
		OPEN, CLOSE, RESET
	}

	/**
	 * Pairs of (message, image), per state
	 */
	enum ButtonLayout {
		// command
		OPEN(ClientMessages.OPEN, ClientImages.STATE_ACTIVE),
		CLOSED(ClientMessages.CLOSE, ClientImages.STATE_IDLE),
		CLOSE(ClientMessages.CLOSE, ClientImages.STATE_IDLE),
		UNAVAILABLE(ClientMessages.UNAVAILABLE, ClientImages.STATE_IDLE);

		private final ClientMessages message;
		private final ClientImages image;

		ButtonLayout(ClientMessages message, ClientImages image) {
			this.message = message;
			this.image = image;
		}

		public ClientMessages getMessage() {
			return message;
		}

		public ClientImages getImage() {
			return image;
		}
	}

	private Button shutterMonitor;

	/**
	 * @param table            the {@link Table} where attach the {@link TableItem}
	 * @param cameraProperties the camera properties
	 */
	public EnergySummaryRow(Table table) {
		this.table = table;
		this.tableItem = new TableItem(table, SWT.BORDER);
		addColumns();
	}

	/**
	 * Updates the columns values
	 */
	public void updateColumns() {
		updateBeamTypeColumn();
		updateEnergyValueColumn();
	}

	private void updateBeamTypeColumn() {
		String beamTypeText = "N/A";
		try {
			beamTypeText = getBeamSelector().getPosition();
		} catch (GDAClientException e) {
			logger.error("Cannot retrieve beam type");
		}
		beamType.setText(beamTypeText);
	}

	private void updateEnergyValueColumn() {
		String beamEnergy = "N/A";
		// TBD
		energyValue.setText(beamEnergy);
	}

	private void addColumns() {
		table.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		int columnIndex = 0;
		TableEditor editor = new TableEditor(table);
		beamType = createClientLabel(table, SWT.NONE, "");
		beamType.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		editor.grabHorizontal = true;
		editor.setEditor(beamType, tableItem, columnIndex);
		table.getColumn(columnIndex).setWidth(100);

		columnIndex = 1;
		editor = new TableEditor(table);
		String beamEnergy = "N/A";
		energyValue = createClientLabel(table, SWT.NONE, beamEnergy);
		energyValue.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		editor.grabHorizontal = true;
		editor.setEditor(energyValue, tableItem, columnIndex);
		table.getColumn(columnIndex).setWidth(100);

		columnIndex = 2;
		editor = new TableEditor(table);
		shutterMonitor = createShutterButton(table);
		shutterMonitor.pack();
		editor.grabHorizontal = true;
		editor.minimumWidth = shutterMonitor.getSize ().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor(shutterMonitor, tableItem, columnIndex);

		updateColumns();
		table.layout();
	}




	private Button createShutterButton(Composite parent) {
		ButtonLayout layout = ButtonLayout.UNAVAILABLE;
		try {
			layout = getButtonLayout();
		} catch (GDAClientException e) {
			logger.error("Error reading the shutter position");
		}
		Button button = createClientButton(parent, SWT.NONE, ClientMessages.EMPTY_MESSAGE, layout.getMessage(),
				layout.getImage());
		updateButtonLayoutAndListener(button);
		return button;
	}

	/**
	 * Updates a button layout and its associated listener
	 *
	 * @param button      the button to update
	 * @param name        the camera control name
	 * @param cameraState the camera state to set
	 * @throws GDAClientException
	 */
	private void updateButtonLayoutAndListener(Button button) {
		try {
			button.setData(LAYOUT, getButtonLayout());
			// Layout
			updateButtonLayout(button);

			// Listener
			updateButtonListener(button);
		} catch (GDAClientException e) {
			logger.error("Cannot update button layout and listerner");
		}
	}

	private void updateButtonLayout(final Button button) {
		ButtonLayout buttonLayout = getButtonLayout(button);
		button.setEnabled(true);
		String tooltip = String.format("Shutter - State: %s", buttonLayout.getMessage());
		if (ButtonLayout.UNAVAILABLE.equals(buttonLayout)) {
			tooltip = tooltip + " \n Please request assistance";
			button.setEnabled(false);
		}
		if (ButtonLayout.CLOSED.equals(buttonLayout)) {
			tooltip = tooltip + " \n Push to open shutter";
		}
		if (ButtonLayout.CLOSE.equals(buttonLayout)) {
			button.setEnabled(false);
			tooltip = tooltip + " \n Wait the shutter to close";
		}
		if (ButtonLayout.OPEN.equals(buttonLayout)) {
			tooltip = tooltip + " \n Push to close shutter";
		}
		button.setToolTipText(tooltip);
		button.setImage(ClientSWTElements.getImage(buttonLayout.getImage()));
		tableItem.getParent().layout(true, true);
	}

	/**
	 * Maps between the shutter readout state and the button ButtonLayout
	 *
	 * @param state the camera state
	 * @return the button relative layout
	 * @throws GDAClientException
	 */
	private ButtonLayout getButtonLayout() throws GDAClientException {
		String position = getEHShutter().getPosition();
		ValveState bl = getEHShutter().getScannablePropertiesDocument().getEnumsMap().entrySet().stream()
				.filter(p -> p.getValue().equals(position))
				.findFirst().map(Entry::getKey)
				.map(ValveState::valueOf)
				.orElse(null);

		if (bl == null)
			return ButtonLayout.UNAVAILABLE;

		switch (bl) {
		case OPEN:
			return ButtonLayout.OPEN;
		case CLOSED:
			return ButtonLayout.CLOSED;
		case CLOSE:
			return ButtonLayout.CLOSE;
		default:
			return ButtonLayout.UNAVAILABLE;
		}
	}

	/**
	 * For each state associate the correct selection listener to the button state:
	 * IDLE --> action: startAcquire, state: ACQUIRE --> action: stopAcquire
	 *
	 * @param button
	 * @param state
	 * @throws GDAClientException
	 */
	private void updateButtonListener(Button button) {
		Optional.ofNullable(getButtonListener(button)).ifPresent(a -> WidgetUtilities.removeWidgetDisposableListener(button, a));
		ButtonLayout layout = getButtonLayout(button);
		switch (layout) {
		case OPEN:
			button.setData(LISTENER, SelectionListener.widgetSelectedAdapter(this::closeShutter));
			WidgetUtilities.addWidgetDisposableListener(button, getButtonListener(button));
			break;
		case CLOSED:
			button.setData(LISTENER, SelectionListener.widgetSelectedAdapter(this::openShutter));
			WidgetUtilities.addWidgetDisposableListener(button, getButtonListener(button));
			break;
		case CLOSE:
			break;
		case UNAVAILABLE:
			break;
		default:
			break;
		}
	}

	private void openShutter(SelectionEvent event) {
		try {
			getEHShutter().moveTo(
					getEHShutter().getScannablePropertiesDocument().getEnumsMap().get(ShutterCommand.RESET.name()));
			getEHShutter().moveTo(
					getEHShutter().getScannablePropertiesDocument().getEnumsMap().get(ShutterCommand.OPEN.name()));
			updateButtonLayoutAndListener(shutterMonitor);
		} catch (GDAClientException e) {
			logger.error("Error moving shutter {} ", shutterMonitor);
		}
	}

	private void closeShutter(SelectionEvent event) {
		try {
			getEHShutter().moveTo(
					getEHShutter().getScannablePropertiesDocument().getEnumsMap().get(ShutterCommand.CLOSE.name()));
			updateButtonLayoutAndListener(shutterMonitor);
		} catch (GDAClientException e) {
			logger.error("Error moving shutter {} ", shutterMonitor);
		}
	}

	/**
	 * A key for a CameraControlSpringEvent listener
	 */
	private static final String LISTENER = "listener";

	/**
	 * Utility to cast a LISTENER data object
	 *
	 * @param button
	 * @return the button associated CameraControlSpringEvent listener, otherwise
	 *         {@code null}
	 */
	private static SelectionListener getButtonListener(Button button) {
		return getDataObject(button, SelectionListener.class, LISTENER);
	}

	private static ButtonLayout getButtonLayout(Button button) {
		return getDataObject(button, ButtonLayout.class, LAYOUT);
	}

	private ManagedScannable<String> getEHShutter() {
		return getBean(ScannablesPropertiesHelper.class)
				.getManagedScannable(EH_SHUTTER);
	}

	private ManagedScannable<String> getBeamSelector() {
		return getBean(ScannablesPropertiesHelper.class)
				.getManagedScannable(BEAM_SELECTOR);
	}


}