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

package uk.ac.gda.tomography.scan.editor.view;

import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyIntegerText;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.configuration.MultipleScans;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.ClientVerifyListener;

/**
 * Open a modal dialog to edit the multiple scan configuration. The editing is done on a copy then if the users closes the dialog pressing the {@code OK} button
 * the data are set in the original configuration.
 *
 * @author Maurizio Nagni
 */
public class MultipleScanDialog extends TitleAreaDialog {

	private final Shell parentShell;
	private Rectangle oldPosition;

	private Shell shell;
	private Composite area;

	private Text numberRepetitions;
	private Text waitingTime;

	private Button repeateMultipleScansType;
	private Button switchbackMultipleScansType;

	/**
	 * The internal copy of the multipleScan data
	 */
	private MultipleScans multipleScan;

	/**
	 * The reference to the original parent {@code ScanningConfiguration}
	 */
	private final Supplier<ScanningConfiguration> configurationSupplier;

	/**
	 * @param parentShell the shell where open the dialog
	 * @param configurationSupplier the configuration supplier
	 */
	protected MultipleScanDialog(Shell parentShell, Supplier<ScanningConfiguration> configurationSupplier) {
		super(parentShell);
		this.parentShell = parentShell;
		this.setShellStyle(SWT.PRIMARY_MODAL);
		this.configurationSupplier = configurationSupplier;
		this.multipleScan = MultipleScans.Builder.cloneMultipleScansDocument(configurationSupplier.get().getMultipleScans()).build();
	}

	@Override
	public void create() {
		super.create();
		this.shell = this.getShell();
		this.oldPosition = parentShell.getShell().getBounds();
		setTitle("Multiple Scan Dialog");
		setMessage("Here you can configure multiple repetitions for the defined acquisition", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		Composite container = ClientSWTElements.createClientCompositeWithGridLayout(area, SWT.NONE, 5);

		Label label = ClientSWTElements.createClientLabel(container, SWT.NONE, ClientMessages.NUM_REPETITIONS, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		numberRepetitions = ClientSWTElements.createClientText(container, SWT.NONE, ClientMessages.NUM_REPETITIONS_TOOLTIP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).applyTo(numberRepetitions);

		ClientSWTElements.createClientEmptyCell(container, new Point(50, 10));

		label = ClientSWTElements.createClientLabel(container, SWT.NONE, ClientMessages.WAITING_TIME, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		waitingTime = ClientSWTElements.createClientText(container, SWT.NONE, ClientMessages.WAITING_TIME_TOOLTIP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).applyTo(waitingTime);

		repeateMultipleScansType = ClientSWTElements.createClientButton(container, SWT.RADIO, ClientMessages.REPEATE_SCAN, ClientMessages.REPEATE_SCAN_TOOLTIP,
				Optional.empty());
		repeateMultipleScansType.setData(MultipleScansType.REPEAT_SCAN);
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE)
				.applyTo(repeateMultipleScansType);

		ClientSWTElements.createClientEmptyCell(container, new Point(50, 10));

		switchbackMultipleScansType = ClientSWTElements.createClientButton(container, SWT.RADIO, ClientMessages.SWITCHBACK_SCAN,
				ClientMessages.SWITCHBACK_SCAN_TOOLTIP, Optional.empty());
		switchbackMultipleScansType.setData(MultipleScansType.SWITCHBACK_SCAN);
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(switchbackMultipleScansType);

		bindElements();
		initialiseElements();
		return container;
	}

	private ControlListener dialogPositionListener = new ControlListener() {
		@Override
		public void controlResized(ControlEvent e) {
			// Do nothing cannot resize.
		}

		/**
		 * Binds the dialog to the parent shell. When the shell moves, the dialog follows
		 */
		@Override
		public void controlMoved(ControlEvent e) {
			int diffX = parentShell.getShell().getBounds().x - oldPosition.x;
			int diffY = parentShell.getShell().getBounds().y - oldPosition.y;
			oldPosition = parentShell.getShell().getBounds();
			Rectangle newBound = new org.eclipse.swt.graphics.Rectangle(shell.getBounds().x + diffX, shell.getBounds().y + diffY, shell.getBounds().width,
					shell.getBounds().height);
			shell.setBounds(newBound);
			shell.layout(true, true);
		}
	};

	private SelectionListener multipleScanTypeListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!Button.class.isInstance(event.getSource()))
				return;

			if (!((Button) event.getSource()).getSelection())
				return;

			Button button = Button.class.cast(event.getSource());
			if (button.equals(switchbackMultipleScansType) || button.equals(repeateMultipleScansType)) {
				multipleScan = ConfigurationDataHelper.updateMultipleScanType(getMultipleScans(), MultipleScansType.class.cast(button.getData()));
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// do nothing
		}
	};

	@Override
	protected Point getInitialSize() {
		return new Point(600, 250);
	}

	@Override
	protected void okPressed() {
		// Save the edited multipleScans configuration
		configurationSupplier.get().setMultipleScans(multipleScan);
		super.okPressed();
	}

	private MultipleScans getMultipleScans() {
		return multipleScan;
	}

	private void bindElements() {
		numberRepetitions.addVerifyListener(ClientVerifyListener.verifyOnlyIntegerText);
		numberRepetitions.addModifyListener(
				e -> multipleScan = ConfigurationDataHelper.updateNumberRepetitions(multipleScan, Integer.parseInt(Text.class.cast(e.widget).getText())));

		waitingTime.addVerifyListener(ClientVerifyListener.verifyOnlyIntegerText);
		waitingTime.addModifyListener(
				e -> multipleScan = ConfigurationDataHelper.updateWaitingTime(multipleScan, Integer.parseInt(Text.class.cast(e.widget).getText())));

		switchbackMultipleScansType.addSelectionListener(multipleScanTypeListener);
		repeateMultipleScansType.addSelectionListener(multipleScanTypeListener);

		parentShell.getShell().addControlListener(dialogPositionListener);
	}

	private void initialiseElements() {
		numberRepetitions.setText(Integer.toString(getMultipleScans().getNumberRepetitions()));
		waitingTime.setText(Integer.toString(getMultipleScans().getWaitingTime()));
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream().filter(i -> getMultipleScans().getMultipleScansType().equals(i.getData()))
				.findFirst().ifPresent(b -> b.setSelection(true));
	}
}
