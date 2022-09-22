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

package uk.ac.diamond.tomography.reconstruction.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * A dialog that allows us to set the nexus path that will be used to sort nexus elements e.g. /entry1/entry_identifier
 */
public class BaseNexusPathDialog extends SelectionDialog {
	private Combo nexusPathCombo;

	private String initialPath;
	private String[] recentlyUsedPaths;

	/**
	 * Create a new dialog for providing a nexus sort path
	 *
	 * @param parentShell
	 *            the containing shell
	 * @param initialPath
	 *            an initialPath to set of <code>null</code> if not specified
	 * @param ruPaths
	 *            an array of recently used Paths to populate the combo box or <code>null</code>
	 */
	public BaseNexusPathDialog(Shell parentShell, String initialPath, String[] ruPaths) {
		super(parentShell);
		if (initialPath != null)
			this.initialPath = initialPath;
		if (ruPaths != null)
			this.recentlyUsedPaths = ruPaths;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(composite, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true).create());

		Label sortByLabel = new Label(container, SWT.NULL);
		sortByLabel.setText("NeXus Path to use:");
		nexusPathCombo = new Combo(container, SWT.NULL);
		nexusPathCombo.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());

		initializePaths();

		return composite;
	}

	private void initializePaths() {
		if (nexusPathCombo != null) {
			if (recentlyUsedPaths != null) {
				for (int i = 0; i < recentlyUsedPaths.length; i++) {
					nexusPathCombo.add(recentlyUsedPaths[i], i);
				}
			}
			if (initialPath != null)
				nexusPathCombo.setText(initialPath);
		}
	}

	/**
	 * Returns the nexus Path
	 *
	 * @return the nexus Path string
	 */
	protected String getNexusPath() {
		int selectionIndex = nexusPathCombo.getSelectionIndex();
		final String newPath;
		if (selectionIndex != -1) {
			newPath = nexusPathCombo.getItem(selectionIndex);
		} else {
			newPath = nexusPathCombo.getText();
		}
		return newPath;
	}

}
