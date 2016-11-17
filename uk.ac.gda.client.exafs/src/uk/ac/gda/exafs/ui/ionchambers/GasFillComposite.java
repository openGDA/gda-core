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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.exafs.mucal.PressureBean;
import gda.exafs.mucal.PressureCalculation;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.common.rcp.util.GridUtils;

public class GasFillComposite extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(GasFillComposite.class);

	private Text energyTextbox;
	private Text absorptionTextbox;
	private Text totalPressureTextbox;
	private Text ionChamberLengthTextbox;
	private Text fill1PeriodTextbox;
	private Text fill2PeriodTextbox;

	private Label pressureValueLabel;
	private Button runFillSequenceButton;
	private Combo gasTypeCombo;
	private Map<String, String> gasTypeMap;
	private Button flushBeforeFillCheckbox;

	private IonChamberParameters currentParameters;
	private boolean settingGuiFromParameters;


	public GasFillComposite(Composite parent, int style ) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		// Main group has two columns : one for energy, gas type settings; one for 'advanced' settings
		Group grpGasFilling = new Group(this, SWT.NONE);
		grpGasFilling.setText("Gas Filling");
		grpGasFilling.setLayout(new GridLayout(2, false));
		grpGasFilling.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// --- Energy, gas type, absorption setting ---
		Composite energyGasTypeComposite = new Composite(grpGasFilling, SWT.NONE);
		energyGasTypeComposite.setLayout(new GridLayout(2, false));
		energyGasTypeComposite.setLayoutData( new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1) );

		// Create map between gas type and description
		gasTypeMap = new LinkedHashMap<String, String>();
		gasTypeMap.put("He","He" );
		gasTypeMap.put("N", "He + N\u2082");
		gasTypeMap.put("Ar", "He + Ar");
		gasTypeMap.put("Kr", "He + Kr");

		Label lblGasType = new Label(energyGasTypeComposite, SWT.NONE);
		lblGasType.setText("Gas Type");
		gasTypeCombo = new Combo(energyGasTypeComposite, SWT.NONE);
		gasTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		// Setup gas selection combo box - each entry uses gas description
		for( Entry<String,String> val : gasTypeMap.entrySet() ) {
			gasTypeCombo.add( val.getValue() );
		}

		absorptionTextbox = IonChamber.addLabelAndTextBox(energyGasTypeComposite, "Absorption (%)" );

		Label lblPressure = new Label(energyGasTypeComposite, SWT.NONE);
		lblPressure.setText("Pressure");
		pressureValueLabel = new Label(energyGasTypeComposite, SWT.NONE);
		pressureValueLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pressureValueLabel.setText("0.053976 bar");

		flushBeforeFillCheckbox = new Button(energyGasTypeComposite, SWT.CHECK);
		flushBeforeFillCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1));
		flushBeforeFillCheckbox.setText("Flush before Fill");

		runFillSequenceButton = new Button(energyGasTypeComposite, SWT.NONE);
		runFillSequenceButton.setText("Run Fill Sequence");
		runFillSequenceButton.setToolTipText("Click to fill the gas into the ion chamber.");

		flushBeforeFillCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));


		// --- 'Advanced' settings ---

		ExpandableComposite expandableComp = new ExpandableComposite(grpGasFilling, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		expandableComp.setLayoutData(layoutData);
		expandableComp.setText("Advanced");
		// set minimum width to match length of title
		layoutData.minimumWidth = expandableComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		Composite compositeAdvanced = new Composite(expandableComp, SWT.NONE);
		compositeAdvanced.setLayout( new GridLayout(2, false) );
		compositeAdvanced.setLayoutData( new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1) );

		totalPressureTextbox = IonChamber.addLabelAndTextBox(compositeAdvanced, "Total Pressure (bar)");
 		ionChamberLengthTextbox = IonChamber.addLabelAndTextBox(compositeAdvanced, "Chamber length (cm)");
		fill1PeriodTextbox = IonChamber.addLabelAndTextBox(compositeAdvanced, "Fill 1 Period (sec)");
		fill2PeriodTextbox = IonChamber.addLabelAndTextBox(compositeAdvanced, "Fill 2 Period (sec)");

		expandableComp.setSize(compositeAdvanced.computeSize(SWT.DEFAULT,  SWT.DEFAULT));
		expandableComp.setClient(compositeAdvanced);

		expandableComp.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.layoutFull(getParent());
				// also layout ionchamber view, to avoid having unnecessary gaps between groups of ionchamber widgets
				GridUtils.layoutFull(getParent().getParent());
			}
		});
		expandableComp.setExpanded(false);

		addListeners();
	}

	public void setGuiFromParameters(IonChamberParameters param) {
		currentParameters = param;
		settingGuiFromParameters = true; // set to true when updating gui to avoid listener triggered events from trying to update params from gui at same time.

		IonChamber.setTextboxFromDouble( totalPressureTextbox, param.getTotalPressure() );
		IonChamber.setTextboxFromDouble( ionChamberLengthTextbox, param.getIonChamberLength() );
		IonChamber.setTextboxFromDouble( fill1PeriodTextbox, param.getGas_fill1_period_box() );
		IonChamber.setTextboxFromDouble( fill2PeriodTextbox, param.getGas_fill2_period_box() );
		IonChamber.setTextboxFromDouble( absorptionTextbox, param.getPercentAbsorption() );
		IonChamber.setTextboxFromDouble( energyTextbox, param.getWorkingEnergy());

		flushBeforeFillCheckbox.setSelection( param.getFlush() );

		// set gas type combo box index for current gas type
		String gasDesc = gasTypeMap.get( param.getGasType() ); //Get combo description from gas type (Ar, Kr, He, N)
		int index = gasTypeCombo.indexOf( gasDesc );
		if ( index > -1 )
			gasTypeCombo.select( index );

		updatePressure();

		settingGuiFromParameters = false;
	}

	public void updateParametersFromGui() {
		if ( currentParameters == null )
			return;

		currentParameters.setTotalPressure( IonChamber.getDoubleFromTextbox(totalPressureTextbox) );
		currentParameters.setIonChamberLength( IonChamber.getDoubleFromTextbox(ionChamberLengthTextbox) );
		currentParameters.setGas_fill1_period_box( IonChamber.getDoubleFromTextbox(fill1PeriodTextbox) );
		currentParameters.setGas_fill2_period_box( IonChamber.getDoubleFromTextbox(fill2PeriodTextbox) );
		currentParameters.setPercentAbsorption( IonChamber.getDoubleFromTextbox(absorptionTextbox) );

		// Loop over gasType map and look for gas type matching description currently in combo box
		String gasTypeDescription = gasTypeCombo.getText();
		for( Entry<String,String> entry: gasTypeMap.entrySet() ) {
			if ( entry.getValue().equals( gasTypeDescription ) ) {
				currentParameters.setGasType( entry.getKey() );
			}
		}
		currentParameters.setFlush( flushBeforeFillCheckbox.getSelection() );

		updatePressure();

	}

	private void addListeners() {

		ModifyListener updateParameterListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if ( !settingGuiFromParameters ) {
					updateParametersFromGui();
				}
			}
		};

		totalPressureTextbox.addModifyListener(updateParameterListener);
		ionChamberLengthTextbox.addModifyListener( updateParameterListener );
		fill1PeriodTextbox.addModifyListener( updateParameterListener );
		fill2PeriodTextbox.addModifyListener( updateParameterListener );
		absorptionTextbox.addModifyListener( updateParameterListener );
		gasTypeCombo.addModifyListener( updateParameterListener );

		// Listener to update flush before fill property
		flushBeforeFillCheckbox.addSelectionListener( new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentParameters.setFlush( flushBeforeFillCheckbox.getSelection() );
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		} );

		// Listener to run gas fill sequence
		runFillSequenceButton.addSelectionListener( new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateParametersFromGui();
				GasFill.runGasFill( currentParameters, runFillSequenceButton);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		} );

	}

	/**
	 * Calculate fill pressure for current set of IonChamberParameters and update gui.
	 */
	public void updatePressure() {
		calculatePressure();
		pressureValueLabel.setText( String.format("%.5g bar", currentParameters.getPressure()) );
	}

	/**
	 * Calculate and set pressure for current set of IonChamberParameters.
	 */
	public void calculatePressure() {
		PressureBean pressureCalcResults;
		try {
			pressureCalcResults = PressureCalculation.getPressure(currentParameters);
			double pressure = pressureCalcResults.getPressure();
			currentParameters.setPressure( pressure );
		} catch (Exception e) {
			logger.error("Problem running mucal for absorption calculation ", e);
		}
	}

}