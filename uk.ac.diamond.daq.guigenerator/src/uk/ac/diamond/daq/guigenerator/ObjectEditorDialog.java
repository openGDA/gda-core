/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ObjectEditorDialog extends Dialog {

	private IGuiGeneratorService guiGenerator;
	private Object objectToEdit;
	private String title;

	public ObjectEditorDialog(Object bean, Shell parent, String title, IGuiGeneratorService guiGenerator) {
		super(parent);
		this.objectToEdit = bean;
		this.title = title != null ? title : "";
		this.guiGenerator = guiGenerator;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control editor = guiGenerator.generateGui(objectToEdit, parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editor);
		return editor;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Only an OK button, no cancel, because data binding means we can't cancel any changes anyway
		// (at least, not without copying the object first)
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
}
