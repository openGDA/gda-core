/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.wizards;

import gda.data.PathConstructor;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class TargetFolderPage extends WizardPage {
	
	protected String targetDir = "";
	private Text txtFilenames;

	protected TargetFolderPage(String pageName) {
		super(pageName);
		setTitle("Choose target folder");
		setDescription("Please choose the XML folder you wish to place the XML files");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		setControl(composite);
		new Label(composite, SWT.NONE).setText("Target folder:");
		txtFilenames = new Text(composite, SWT.SINGLE | SWT.BORDER);
		txtFilenames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Button button = new Button(composite, SWT.NONE);
		button.setText("...");

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// open file dialog at the beamline's data folder
				DirectoryDialog  dialog = new DirectoryDialog (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.OPEN);
				dialog.setFilterPath(PathConstructor.createFromRCPProperties() + "/xml");
				
				targetDir = dialog.open();
				
				txtFilenames.setText(targetDir);
			}
		});
		
		Label warning = new Label(composite, SWT.NONE);
		warning.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,3,1));
		warning.setText("NOTE: If the target folder contains files with the same names\nas the originals then the new files will be renamed by adding\nan integer to the end of the name.");
	}

}
