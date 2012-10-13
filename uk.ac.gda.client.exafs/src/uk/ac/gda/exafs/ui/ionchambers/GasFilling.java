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

package uk.ac.gda.exafs.ui.ionchambers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.layout.RowData;

public class GasFilling extends Composite implements ViewEditor {
	private Text textEnergy;
	private Text text;
	private Text textTotalPressure;
	private Text textIonChamberLength;
	private Text textFill1Period;
	private Text textFill2Period;

	public GasFilling(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		Group grpGasFilling = new Group(this, SWT.NONE);
		grpGasFilling.setText("Gas Filling");
		grpGasFilling.setBounds(10, 10, 559, 233);
		grpGasFilling.setLayout(new GridLayout(1, false));
		
		Composite compositeEnergy = new Composite(grpGasFilling, SWT.NONE);
		compositeEnergy.setLayout(new GridLayout(4, false));
		
		Label lblEnergy = new Label(compositeEnergy, SWT.NONE);
		lblEnergy.setText("Energy");
		
		textEnergy = new Text(compositeEnergy, SWT.BORDER);
		textEnergy.setText("10500ev");
		
		Button btnGetEnergy = new Button(compositeEnergy, SWT.NONE);
		btnGetEnergy.setText("Get from Scan");
		
		Button btnSetDefaultMix = new Button(compositeEnergy, SWT.NONE);
		btnSetDefaultMix.setText("Set Default Mixture");
		
		Composite composite = new Composite(grpGasFilling, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.heightHint = 160;
		gd_composite.widthHint = 543;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Composite compositeGas = new Composite(composite, SWT.NONE);
		compositeGas.setLayout(new GridLayout(2, false));
		
		Button checkFillBeforeScan = new Button(compositeGas, SWT.CHECK);
		checkFillBeforeScan.setText("Fill before Scan");
		
		Button checkFlush = new Button(compositeGas, SWT.CHECK);
		checkFlush.setText("Flush before Fill");
		
		Label lblGasType = new Label(compositeGas, SWT.NONE);
		lblGasType.setText("Gas Type");
		
		Combo comboGasType = new Combo(compositeGas, SWT.NONE);
		GridData gd_comboGasType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboGasType.widthHint = 124;
		comboGasType.setLayoutData(gd_comboGasType);
		comboGasType.setText("He + Ar");
		
		Label lblAbsorption = new Label(compositeGas, SWT.NONE);
		lblAbsorption.setText("Absorption");
		
		text = new Text(compositeGas, SWT.BORDER);
		text.setText("15.00%");
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 112;
		text.setLayoutData(gd_text);
		
		Label lblPressure = new Label(compositeGas, SWT.NONE);
		lblPressure.setText("Pressure");
		
		Label lblPressureVal = new Label(compositeGas, SWT.NONE);
		lblPressureVal.setText("0.053976 bar");
		
		Button btnFill = new Button(compositeGas, SWT.NONE);
		btnFill.setText("Peform Fill Sequence");
		new Label(compositeGas, SWT.NONE);
		
		ExpandBar expandAdvanced = new ExpandBar(composite, SWT.NONE);
		expandAdvanced.setLayoutData(new RowData(250, 151));
		
		ExpandItem expandItemAdvanced = new ExpandItem(expandAdvanced, SWT.NONE);
		expandItemAdvanced.setText("Advanced");
		expandItemAdvanced.setHeight(300);
		
		Composite compositeAdvanced = new Composite(expandAdvanced, SWT.NONE);
		compositeAdvanced.setLayout(new GridLayout(2, false));
		
		Label lblTotalPressure = new Label(compositeAdvanced, SWT.NONE);
		lblTotalPressure.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTotalPressure.setText("Total Pressure");
		
		textTotalPressure = new Text(compositeAdvanced, SWT.BORDER);
		textTotalPressure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel = new Label(compositeAdvanced, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Ion Chamber Length");
		
		textIonChamberLength = new Text(compositeAdvanced, SWT.BORDER);
		textIonChamberLength.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_1 = new Label(compositeAdvanced, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Fill 1 Period");
		
		textFill1Period = new Text(compositeAdvanced, SWT.BORDER);
		textFill1Period.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblFillPeriod = new Label(compositeAdvanced, SWT.NONE);
		lblFillPeriod.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFillPeriod.setText("Fill 2 Period");
		
		textFill2Period = new Text(compositeAdvanced, SWT.BORDER);
		textFill2Period.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		expandItemAdvanced.setControl(compositeAdvanced);
	}

	@Override
	public boolean isInEditor() {
		return false;
	}

	@Override
	public void setInEditor() {		
	}
}
