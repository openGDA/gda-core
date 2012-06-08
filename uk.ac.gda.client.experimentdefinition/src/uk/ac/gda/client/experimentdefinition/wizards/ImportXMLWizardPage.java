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

import gda.configuration.properties.LocalProperties;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ImportXMLWizardPage extends WizardPage {

	protected String[] selectedFilenames;
	protected String selectedFolder;
	private Text txtFilenames;

	protected ImportXMLWizardPage(String pageName) {
		super(pageName);
		setTitle("Choose file to copy into project");
		setDescription("Please choose the XML file(s) you wish to copy into you current visit's XML folder");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		setControl(composite);
		new Label(composite, SWT.NONE).setText("File:");
		txtFilenames = new Text(composite, SWT.SINGLE | SWT.BORDER);
		txtFilenames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Button button = new Button(composite, SWT.NONE);
		button.setText("...");

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// open file dialog at the beamline's data folder
				FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.MULTI);
			    dialog
			        .setFilterNames(new String[] { "XML Files", "All Files (*.*)" });
			    dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
			    dialog.setFilterPath(LocalProperties.getBaseDataDir()); 
			    dialog.open();
				selectedFilenames = dialog.getFileNames();
				selectedFolder = dialog.getFilterPath();
				String filenamesString = "";
				for (String filename : selectedFilenames){
					filenamesString += filename + ";";
				}
				txtFilenames.setText(filenamesString);
			}
		});
	}

}
