/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.configuration.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;

public class AdditionalInfoDialogCellEditor extends DialogCellEditor {

	private final RESOLUTION resolution;

	public AdditionalInfoDialogCellEditor(Table table, RESOLUTION resolution) {
		super(table);
		this.resolution = resolution;
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		AdditionalInfoDialog dialog = new AdditionalInfoDialog(cellEditorWindow.getShell(), "Additional Information",
				resolution);
		return dialog.open() == Window.OK ? null : null;
	}

	public static class AdditionalInfoDialog extends Dialog {

		private final String header;
		private final RESOLUTION resolution;

		protected AdditionalInfoDialog(Shell parentShell, String header, RESOLUTION resolution) {
			super(parentShell);
			this.header = header;
			this.resolution = resolution;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(header);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			Label lbl = new Label(composite, SWT.None);

			lbl.setText("Hello");
			return composite;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}

	}

}
