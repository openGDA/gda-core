/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditSampleMetadataDialog extends Dialog {

	private String name;
	private String description;
	private Text nameWidget;
	private Text descriptionWidget;

	protected EditSampleMetadataDialog(Shell parentShell, String name, String description) {
		super(parentShell);
		setShellStyle(SWT.RESIZE);
		this.name = name;
		this.description = description;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite mainComposite = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(mainComposite);

		new Label(mainComposite, SWT.NONE).setText("Sample name");
		nameWidget = new Text(mainComposite, SWT.BORDER);
		nameWidget.setText(name);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameWidget);

		Label descriptionLabel = new Label(mainComposite, SWT.NONE);
		descriptionLabel.setText("Description");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.TOP).applyTo(descriptionLabel);
		descriptionWidget = new Text(mainComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		descriptionWidget.setText(description);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(descriptionWidget);

		return mainComposite;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(350, 170);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit metadata");
	}

	@Override
	protected void okPressed() {
		name = nameWidget.getText();
		description = descriptionWidget.getText();
		super.okPressed();
	}

	protected String getName() {
		return name;
	}

	protected String getDescription() {
		return description;
	}

}
