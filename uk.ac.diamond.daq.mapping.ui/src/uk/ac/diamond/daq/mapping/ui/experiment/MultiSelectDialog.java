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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog wrapping a List widget that allows the user to select one of more Strings
 */
public class MultiSelectDialog extends Dialog {
	private String[] listItems;
	private String title;
	private String header;
	private List<String> selected;

	// List box containing names that user can select
	private org.eclipse.swt.widgets.List stringList;

	/**
	 * Constructor
	 *
	 * @param parentShell
	 * @param title
	 *            Description to be shown in the title bar
	 * @param header
	 *            Description to be shown above the list of names
	 * @param listItems
	 *            Strings to be shown in the list
	 */
	public MultiSelectDialog(Shell parentShell, String title, String header, Collection<String> listItems) {
		super(parentShell);
		this.title = title;
		this.header = header;
		this.listItems = listItems.toArray(new String[listItems.size()]);
		Arrays.sort(this.listItems);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(composite);

		final Label lblHeader = new Label(composite, SWT.NONE);
		lblHeader.setText(header);
		GridDataFactory.swtDefaults().applyTo(lblHeader);

		stringList = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		stringList.setItems(listItems);
		GridDataFactory.swtDefaults().hint(250, 200).applyTo(stringList);

		return composite;
	}

	@Override
	protected void okPressed() {
		selected = Arrays.asList(stringList.getSelection());
		super.okPressed();
	}

	public List<String> getSelected() {
		return selected;
	}
}