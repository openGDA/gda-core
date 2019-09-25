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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.scan.editor.view.TomographyConfigurationComposite;
import uk.ac.gda.tomography.scan.editor.view.TomographyReconstructionComposite;
import uk.ac.gda.tomography.service.message.TomographyMessages;
import uk.ac.gda.tomography.service.message.TomographyMessagesUtility;
import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController;

import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Allows the user to create/edit tomography scan parameter configuration. This dialog has been created primarily for k11 however has been developed to allow
 * other beamlines migrate to this one.
 *
 * @author Maurizio Nagni
 */
public class TomographyAcquisitionTabsDialog extends Dialog {

	public static final int CANCEL = 0;
	public static final int SAVE = 1;
	public static final int RUN = 3;

	private static final Logger logger = LoggerFactory.getLogger(TomographyAcquisitionTabsDialog.class);

	private final TomographyParametersAcquisitionController controller;

	public TomographyAcquisitionTabsDialog(Shell parentShell, TomographyParametersAcquisitionController controller) {
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
		final TabFolder tabFolder = new TabFolder (parent, SWT.BORDER);
		//Composite container = (Composite) super.createDialogArea(parent);
		TabItem item = new TabItem (tabFolder, SWT.NONE);
		item.setText(TomographyMessagesUtility.getMessage(TomographyMessages.ACQUISITION));
		Composite comp = new TomographyConfigurationComposite(tabFolder, controller);
		item.setControl(comp);

		item = new TabItem (tabFolder, SWT.NONE);
		item.setText(TomographyMessagesUtility.getMessage(TomographyMessages.RECONSTRUCTION));
		comp = new TomographyReconstructionComposite(tabFolder, controller);
		item.setControl(comp);
		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, RUN, ClientMessagesUtility.getMessage(ClientMessages.RUN), false);
		createButton(parent, SAVE, ClientMessagesUtility.getMessage(ClientMessages.SAVE), false);
		createButton(parent, CANCEL, ClientMessagesUtility.getMessage(ClientMessages.CANCEL), false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
	    setReturnCode(buttonId);
	    close();
	}
}
