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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GasFilling extends Composite {
	private Text textEnergy;
	private Text text;
	private Text textTotalPressure;
	private Text textIonChamberLength;
	private Text textFill1Period;
	private Text textFill2Period;

	public GasFilling(Composite parent, int style, boolean inEditor) {
		
		super(parent, style);
		setLayout(new GridLayout(1, false));
		Group grpGasFilling = new Group(this, SWT.NONE);
		grpGasFilling.setText("Gas Filling");
		grpGasFilling.setBounds(10, 10, 559, 233);
		GridLayout gl_grpGasFilling = new GridLayout(1, false);
		gl_grpGasFilling.horizontalSpacing = 0;
		gl_grpGasFilling.marginWidth = 0;
		gl_grpGasFilling.marginHeight = 0;
		gl_grpGasFilling.verticalSpacing = 0;
		grpGasFilling.setLayout(gl_grpGasFilling);

		Composite composite_2 = new Composite(grpGasFilling, SWT.NONE);
		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_2.widthHint = 485;
		composite_2.setLayoutData(gd_composite_2);
		GridLayout gl_composite_2 = new GridLayout(2, false);
		gl_composite_2.marginHeight = 0;
		gl_composite_2.verticalSpacing = 0;
		gl_composite_2.marginWidth = 0;
		gl_composite_2.horizontalSpacing = 0;
		composite_2.setLayout(gl_composite_2);

		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		GridLayout gl_composite_3 = new GridLayout(1, false);
		gl_composite_3.horizontalSpacing = 0;
		gl_composite_3.marginHeight = 0;
		gl_composite_3.verticalSpacing = 0;
		gl_composite_3.marginWidth = 0;
		composite_3.setLayout(gl_composite_3);

		Composite compositeEnergy = new Composite(composite_3, SWT.NONE);
		GridData gd_compositeEnergy = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_compositeEnergy.widthHint = 276;
		compositeEnergy.setLayoutData(gd_compositeEnergy);
		GridLayout gl_compositeEnergy = new GridLayout(4, false);
		gl_compositeEnergy.marginHeight = 0;
		compositeEnergy.setLayout(gl_compositeEnergy);

		if (inEditor) {
			Button btnGetEnergy = new Button(compositeEnergy, SWT.NONE);
			btnGetEnergy.setText("Get from Scan");
		}

		Label lblEnergy = new Label(compositeEnergy, SWT.NONE);
		lblEnergy.setText("Energy");

		textEnergy = new Text(compositeEnergy, SWT.BORDER);
		GridData gd_textEnergy = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_textEnergy.widthHint = 73;
		textEnergy.setLayoutData(gd_textEnergy);
		textEnergy.setText("10500ev");

		Button btnSetDefaultMix = new Button(compositeEnergy, SWT.NONE);
		GridData gd_btnSetDefaultMix = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnSetDefaultMix.widthHint = 125;
		btnSetDefaultMix.setLayoutData(gd_btnSetDefaultMix);
		btnSetDefaultMix.setText("Set Default Mix");
		new Label(compositeEnergy, SWT.NONE);

		Composite compositeGas = new Composite(composite_3, SWT.NONE);
		GridLayout gl_compositeGas = new GridLayout(2, false);
		gl_compositeGas.verticalSpacing = 3;
		compositeGas.setLayout(gl_compositeGas);

		if (inEditor) {
			Button checkFillBeforeScan = new Button(compositeGas, SWT.CHECK);
			checkFillBeforeScan.setText("Fill before Scan");
		}

		Label lblGasType = new Label(compositeGas, SWT.NONE);
		lblGasType.setText("Gas Type");

		Combo comboGasType = new Combo(compositeGas, SWT.NONE);
		GridData gd_comboGasType = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gd_comboGasType.widthHint = 124;
		comboGasType.setLayoutData(gd_comboGasType);
		comboGasType.setText("He + Ar");

		Label lblAbsorption = new Label(compositeGas, SWT.NONE);
		lblAbsorption.setText("Absorption");

		text = new Text(compositeGas, SWT.BORDER);
		text.setText("15.00%");

		GridData gd_text = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gd_text.widthHint = 113;
		text.setLayoutData(gd_text);

		Label lblPressure = new Label(compositeGas, SWT.NONE);
		lblPressure.setText("Pressure");

		Label lblPressureVal = new Label(compositeGas, SWT.NONE);
		GridData gd_lblPressureVal = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblPressureVal.widthHint = 125;
		lblPressureVal.setLayoutData(gd_lblPressureVal);
		lblPressureVal.setText("0.053976 bar");

		Button checkFlush = new Button(compositeGas, SWT.CHECK);
		checkFlush.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		checkFlush.setText("Flush before Fill");

		// if (!inEditor) {
		Button btnFill = new Button(compositeGas, SWT.NONE);
		btnFill.setText("Run Fill Sequence");

		Composite composite = new Composite(composite_2, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_composite.widthHint = 207;
		composite.setLayoutData(gd_composite);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.marginHeight = 7;
		gl_composite.marginWidth = 0;
		composite.setLayout(gl_composite);

		ExpandBar expandAdvanced = new ExpandBar(composite, SWT.NONE);
		GridData gd_expandAdvanced = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		gd_expandAdvanced.heightHint = 152;
		gd_expandAdvanced.widthHint = 205;
		expandAdvanced.setLayoutData(gd_expandAdvanced);

		ExpandItem expandItemAdvanced = new ExpandItem(expandAdvanced, SWT.NONE);
		expandItemAdvanced.setText("Advanced");
		expandItemAdvanced.setHeight(300);

		Composite compositeAdvanced = new Composite(expandAdvanced, SWT.NONE);
		GridLayout gl_compositeAdvanced = new GridLayout(2, false);
		gl_compositeAdvanced.marginHeight = 8;
		gl_compositeAdvanced.verticalSpacing = 3;
		compositeAdvanced.setLayout(gl_compositeAdvanced);

		Label lblTotalPressure = new Label(compositeAdvanced, SWT.NONE);
		lblTotalPressure.setText("Total Pressure");

		textTotalPressure = new Text(compositeAdvanced, SWT.BORDER);
		textTotalPressure.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel = new Label(compositeAdvanced, SWT.NONE);
		lblNewLabel.setText("Chamber Length");

		textIonChamberLength = new Text(compositeAdvanced, SWT.BORDER);
		textIonChamberLength.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_1 = new Label(compositeAdvanced, SWT.NONE);
		lblNewLabel_1.setText("Fill 1 Period");

		textFill1Period = new Text(compositeAdvanced, SWT.BORDER);
		textFill1Period.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		Label lblFillPeriod = new Label(compositeAdvanced, SWT.NONE);
		lblFillPeriod.setText("Fill 2 Period");

		textFill2Period = new Text(compositeAdvanced, SWT.BORDER);
		textFill2Period.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		expandItemAdvanced.setControl(compositeAdvanced);
	}
}
