/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detectorviews.wizards;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AddScanWizardPageOne extends WizardPage {
	private Combo expType;
	private Label lblChooseType;
	private String scanType;

	AddScanWizardPageOne() {
		super("Choose scan type");
	}

	@Override
	public void createControl(Composite parent) {
		setTitle("Please select the type of scan.");
		Composite selectTypeArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(selectTypeArea);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(selectTypeArea);
		lblChooseType = new Label(selectTypeArea, 0);
		lblChooseType.setText("Please choose an experiment type");
		String[] scanTypes = { "Xas", "Xanes", "Qexafs" };
		expType = new Combo(selectTypeArea, 0);
		expType.setItems(scanTypes);
		expType.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				scanType = expType.getItem(expType.getSelectionIndex());
			}

		});
		setPageComplete(true);
		setErrorMessage(null);
		setMessage(null);
		setControl(selectTypeArea);
	}
	
	public String getScanType(){
		return scanType;
	}
	
}