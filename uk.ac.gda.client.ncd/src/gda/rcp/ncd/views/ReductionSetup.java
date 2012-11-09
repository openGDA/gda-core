/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.rcp.ncd.views;

import java.io.File;

import gda.data.PathConstructor;
import gda.jython.JythonServerFacade;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;

public class ReductionSetup extends ViewPart {
	public ReductionSetup() {
	}

	public static final String ID = "gda.rcp.ncd.views.ReductionSetup"; //$NON-NLS-1$
	private Text bgFile;
	private Spinner normChan;
	private Button btnNorm;
	private Button btnBG;
	private Button btnSect;
	private Button btnAver;
	private Button btnInv;
	private Button btnMask;

	private static String pyBoolStringFor(Button btn) {
		return btn.getSelection() ? "True" : "False";
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout(1, false));			
		
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label lblSetupFor = new Label(group, SWT.SHADOW_NONE);
		lblSetupFor.setText("Setup for");
		
		final Combo combo = new Combo(group, SWT.NONE);
		combo.setItems(new String[] {"Saxs", "Waxs"});
		
		Button btnConfigure = new Button(group, SWT.NONE);
		btnConfigure.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder command = new StringBuilder("ncdredux.ncdredconf(");
				command.append('"');
				command.append(combo.getText());
				command.append('"');
				if (btnNorm.getSelection()) {
					command.append(String.format(",norm=%d",normChan.getSelection()));
				} else {
					command.append(",norm=False");
				}
				if (btnBG.getSelection()) {
					command.append(String.format(",bg=\"%s\"",bgFile.getText().replaceAll("[\\\"]", "")));
				} else {
					command.append(",bg=False");
				}
				if (btnInv.getSelection()) {
					command.append(",inv=True");
				}
				
				command.append(",aver="+pyBoolStringFor(btnAver));
				command.append(",sect="+pyBoolStringFor(btnSect));
				command.append(",mask="+pyBoolStringFor(btnMask));

				command.append(")");
				JythonServerFacade.getInstance().runCommand(command.toString());
			}
		});
		btnConfigure.setText("Configure");
		
		Group grpNormalisation = new Group(parent, SWT.BORDER);
		grpNormalisation.setText("Normalisation");
		GridLayout gl_grpNormalisation = new GridLayout(3, false);
		gl_grpNormalisation.horizontalSpacing = 15;
		grpNormalisation.setLayout(gl_grpNormalisation);
		grpNormalisation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnNorm = new Button(grpNormalisation, SWT.CHECK);
		btnNorm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnNorm.setText("enabled");
		
		Label lblChannel = new Label(grpNormalisation, SWT.NONE);
		lblChannel.setText("Channel");
		
		normChan = new Spinner(grpNormalisation, SWT.BORDER);
		normChan.setMinimum(2);
		normChan.setMaximum(9);
		normChan.setSelection(2);
		
		Group grpBackgroundSubtraction = new Group(parent, SWT.BORDER);
		grpBackgroundSubtraction.setText("Background Subtraction");
		GridLayout gl_grpBackgroundSubtraction = new GridLayout(4, false);
		gl_grpBackgroundSubtraction.horizontalSpacing = 15;
		grpBackgroundSubtraction.setLayout(gl_grpBackgroundSubtraction);
		grpBackgroundSubtraction.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnBG = new Button(grpBackgroundSubtraction, SWT.CHECK);
		btnBG.setText("enabled");
		
		Label lblFile = new Label(grpBackgroundSubtraction, SWT.NONE);
		lblFile.setText("File");
		
		bgFile = new Text(grpBackgroundSubtraction, SWT.BORDER);
		GridData gd_bgFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_bgFile.widthHint = 185;
		bgFile.setLayoutData(gd_bgFile);
		
		Button btnBrowse = new Button(grpBackgroundSubtraction, SWT.NONE);
		btnBrowse.setText("Browse");
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
				Shell shell = new Shell(parent.getDisplay());
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterPath(PathConstructor.createFromDefaultProperty());
				fd.setFilterNames(new String[] { "NeXus files", "All Files"});
				fd.setFilterExtensions(new String[] {"*.nxs", "*.*"});
				String filename = fd.open();
				File f = new File(filename);
				if (f.exists()) 
					bgFile.setText(f.getAbsolutePath());
				} catch (Exception ex) {
					// it should teach the user for now that there is no update to the text field
				}
			}
		});

		Group grpMask = new Group(parent, SWT.NONE);
		grpMask.setText("Masking");
		GridLayout gl_grpMask = new GridLayout(1, false);
		gl_grpMask.horizontalSpacing = 15;
		grpMask.setLayout(gl_grpMask);
		grpMask.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnMask = new Button(grpMask, SWT.CHECK);
		btnMask.setText("use GUI defined mask");
		
		Group grpSectorIntegration = new Group(parent, SWT.NONE);
		grpSectorIntegration.setText("Sector Integration");
		GridLayout gl_grpSectorIntegration = new GridLayout(1, false);
		gl_grpSectorIntegration.horizontalSpacing = 15;
		grpSectorIntegration.setLayout(gl_grpSectorIntegration);
		grpSectorIntegration.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnSect = new Button(grpSectorIntegration, SWT.CHECK);
		btnSect.setText("enabled");
		
		Group grpInvariant = new Group(parent, SWT.NONE);
		grpInvariant.setText("Invariant");
		grpInvariant.setLayout(new GridLayout(1, false));
		grpInvariant.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnInv = new Button(grpInvariant, SWT.BORDER | SWT.CHECK);
		btnInv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnInv.setText("enabled");
		
		Group grpAverage = new Group(parent, SWT.BORDER);
		grpAverage.setText("Average");
		grpAverage.setLayout(new GridLayout(1, false));
		grpAverage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnAver = new Button(grpAverage, SWT.CHECK);
		btnAver.setText("enabled");
	}

	@Override
	public void setFocus() {
	}
}