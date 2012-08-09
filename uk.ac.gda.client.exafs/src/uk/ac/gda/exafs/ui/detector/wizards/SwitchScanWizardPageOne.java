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

package uk.ac.gda.exafs.ui.detector.wizards;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;

public class SwitchScanWizardPageOne extends WizardPage {

	Combo expType;
	Label lblChooseType;
	String scanType;
	
	SwitchScanWizardPageOne() {
		super("Choose scan type");
	}
	
	private String[] getExperimentTypes(){
		
		if (ScanObjectManager.isXESOnlyMode()) {
			return new String[]{"Xes"};
		} 
		
		String[] types = new String[]{ "Xas", "Xanes"};
		
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.common.beans.factory");
		for (IConfigurationElement element : config) {
			if (element.getName().equals("bean")) {
				try {
					IRichBean thisbean = (IRichBean) element.createExecutableExtension("class");
					if (thisbean instanceof MicroFocusScanParameters){
						types = (String[]) ArrayUtils.add(types, "Microfocus");
					} else if (thisbean instanceof QEXAFSParameters){
						types = (String[]) ArrayUtils.add(types, "Qexafs");
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return types;
	}

	@Override
	public void createControl(Composite parent) {
		this.setTitle("Please select the type of scan.");
		
		Composite selectTypeArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(selectTypeArea);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(selectTypeArea);
		
		lblChooseType = new Label(selectTypeArea, 0);
		lblChooseType.setText("Please choose an experiment type");
		
		String[] scanTypes = getExperimentTypes();

		expType = new Combo(selectTypeArea, 0);
		expType.setItems(scanTypes);
		expType.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				scanType=expType.getItem(expType.getSelectionIndex());
				SwitchScanWizardPageOne.this.setPageComplete(true);
			}
		});
		
		setPageComplete(false);
		setErrorMessage(null);
		setMessage(null);
		setControl(selectTypeArea);
	}
	
	public String getScanType(){
		return scanType;
	}
}
