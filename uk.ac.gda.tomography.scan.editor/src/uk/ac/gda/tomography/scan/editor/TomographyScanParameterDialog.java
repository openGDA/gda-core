/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.controller.ITomographyConfigurationController;
import uk.ac.gda.tomography.controller.TomographyControllerException;
import uk.ac.gda.tomography.model.TomographyScanParameters;
import uk.ac.gda.tomography.scan.editor.view.TomographyConfigurationComposite;
import uk.ac.gda.tomography.scan.editor.view.TomographyMessages;
import uk.ac.gda.tomography.scan.editor.view.TomographyMessagesUtility;

/**
 * Allows the user to create/edit tomography scan parameter configuration. This
 * dialog has been created primarily for k11 however has been developed to allow
 * other beamlines migrate to this one.
 *
 * @author Maurizio Nagni
 */
public class TomographyScanParameterDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(TomographyScanParameterDialog.class);

	private final ITomographyConfigurationController<TomographyScanParameters> controller;

	public TomographyScanParameterDialog(Shell parentShell,
			ITomographyConfigurationController<TomographyScanParameters> controller) {
		super(parentShell);
		this.controller = controller;
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setLayout(new GridLayout(1, false));
		super.configureShell(newShell);
		newShell.setText(TomographyMessagesUtility.getMessage(TomographyMessages.TOMO_SCAN_PARMS));
	}

	@Override
	public Control createDialogArea(Composite parent) {
		new TomographyConfigurationComposite(parent, controller.getData());
		customiseDialogArea(parent);
		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);
		Button save = TomographySWTElements.createButton(parent, TomographyMessages.SAVE, SWT.PUSH);
		Button cancel = TomographySWTElements.createButton(parent, TomographyMessages.CANCEL, SWT.PUSH);

		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					controller.saveData();
					arg0.widget.getDisplay().getActiveShell().close();
				} catch (TomographyControllerException e) {
					logger.error("TODO put description of error here", e);
				}
			}
		};
		save.addSelectionListener(listener);

		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				arg0.widget.getDisplay().getActiveShell().close();
			}
		};
		cancel.addSelectionListener(listener);
	}

	protected void customiseDialogArea(Control control) {

	}
}
