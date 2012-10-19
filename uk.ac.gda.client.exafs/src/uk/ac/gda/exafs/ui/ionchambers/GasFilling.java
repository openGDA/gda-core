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
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

public class GasFilling extends Composite{
	private Text textEnergy;
	private Text text;
	private Text textTotalPressure;
	private Text textIonChamberLength;
	private Text textFill1Period;
	private Text textFill2Period;

	public GasFilling(Composite parent, int style, boolean inEditor) {
		super(parent, style);
		setLayout(new FormLayout());
		Group grpGasFilling = new Group(this, SWT.NONE);
		FormData fd_grpGasFilling = new FormData();
		fd_grpGasFilling.right = new FormAttachment(0, 487);
		fd_grpGasFilling.top = new FormAttachment(0);
		fd_grpGasFilling.left = new FormAttachment(0);
		grpGasFilling.setLayoutData(fd_grpGasFilling);
		grpGasFilling.setText("Gas Filling");
		grpGasFilling.setBounds(10, 10, 559, 233);
		grpGasFilling.setLayout(new GridLayout(1, false));

		Composite compositeEnergy = new Composite(grpGasFilling, SWT.NONE);
		compositeEnergy.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		compositeEnergy.setLayout(new GridLayout(4, false));

		Label lblEnergy = new Label(compositeEnergy, SWT.NONE);
		lblEnergy.setText("Energy");

		textEnergy = new Text(compositeEnergy, SWT.BORDER);
		textEnergy.setText("10500ev");

		if (inEditor) {
			Button btnGetEnergy = new Button(compositeEnergy, SWT.NONE);
			btnGetEnergy.setText("Get from Scan");
		}

		Button btnSetDefaultMix = new Button(compositeEnergy, SWT.NONE);
		btnSetDefaultMix.setText("Set Default Mixture");
			
		Composite composite = new Composite(grpGasFilling, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginWidth = 0;
		composite.setLayout(gl_composite);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_composite.heightHint = 160;
		gd_composite.widthHint = 455;
		composite.setLayoutData(gd_composite);

		Composite compositeGas = new Composite(composite, SWT.NONE);
		GridData gd_compositeGas = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_compositeGas.widthHint = 274;
		compositeGas.setLayoutData(gd_compositeGas);
		compositeGas.setLayout(new GridLayout(2, false));

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
		GridData gd_lblPressureVal = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblPressureVal.heightHint = 25;
		gd_lblPressureVal.widthHint = 125;
		lblPressureVal.setLayoutData(gd_lblPressureVal);
		lblPressureVal.setText("0.053976 bar");
		
		Button checkFlush = new Button(compositeGas, SWT.CHECK);
		checkFlush.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		checkFlush.setText("Flush before Fill");
		
		if (!inEditor) {
			Button btnFill = new Button(compositeGas, SWT.NONE);
			btnFill.setText("Run Fill Sequence");
		}
		
		if (inEditor) {
			Button checkFillBeforeScan = new Button(compositeGas, SWT.CHECK);
			checkFillBeforeScan.setText("Fill before Scan");
		}

		ExpandBar expandAdvanced = new ExpandBar(composite, SWT.NONE);
		GridData gd_expandAdvanced = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_expandAdvanced.heightHint = 152;
		gd_expandAdvanced.widthHint = 170;
		expandAdvanced.setLayoutData(gd_expandAdvanced);

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
		lblNewLabel.setText("Chamber Length");

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
}
