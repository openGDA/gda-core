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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.exafs.util.GainCalculation;

public class AmplifierComposite extends Composite{
	private Combo sensitivityCombo;
	private IonChamberParameters currentParameters;

	public AmplifierComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		Group grpAmplifier = new Group(this, SWT.NONE);
		grpAmplifier.setLayout( new GridLayout(3, false) );

		grpAmplifier.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1) );
		grpAmplifier.setText("Amplifier");

		Label lblNewLabel = new Label(grpAmplifier, SWT.NONE);
		lblNewLabel.setText("Sensitivity");
		sensitivityCombo = new Combo(grpAmplifier, SWT.NONE);

		for( String val : GainCalculation.getGainNotches() )
			sensitivityCombo.add( val );
		sensitivityCombo.select(0);
		sensitivityCombo.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( currentParameters != null ) {
					currentParameters.setGain( sensitivityCombo.getText() );
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.widthHint = 100;
		sensitivityCombo.setLayoutData(gd_combo);

		Button btnSet = new Button(grpAmplifier, SWT.NONE);
		btnSet.setText("Set");
	}

	public void setGuiFromParameters( IonChamberParameters params ) {
		currentParameters = params;
		String gainStr = params.getGain();
		int index = sensitivityCombo.indexOf( gainStr );
		if ( index > -1 )
			sensitivityCombo.select(index);

	}
}
