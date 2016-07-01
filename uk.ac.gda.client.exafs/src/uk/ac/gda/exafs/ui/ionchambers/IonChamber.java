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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.IonChambersBean;

public class IonChamber {

	private Combo ionChamberListCombo;
	private String[] defaultIonChambers = {"I0", "It", "Iref"};
	IonChambersBean ionChambersBean;
	private AmplifierComposite amplifier;
	private VoltageSupplyComposite voltageSupply;
	private GasFillComposite gasFilling;
	private ArrayList<GasFillComposite> gasFillComposites;

	public IonChamber(Composite parent, IonChambersBean bean) {

		parent.setLayout(new FillLayout());
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		ionChambersBean = bean;

		// Create default ionchambers if not present in template
		if ( bean.getIonChambers().size() == 0 ) {
			for(int i = 0; i< defaultIonChambers.length; i++ ) {
				IonChamberParameters ionChamber = new IonChamberParameters();
				ionChamber.setName(defaultIonChambers[i]);
				ionChambersBean.addIonChamber( ionChamber );
			}
		}

		final Group grpIonChambers = new Group(scrolledComposite, SWT.NONE);

		grpIonChambers.setLayout( new GridLayout(1, false) );
		grpIonChambers.setText("Ion Chambers");

		createEnergyControl( grpIonChambers );
		createGuiAllChambers( grpIonChambers );

		scrolledComposite.setContent(grpIonChambers);
		grpIonChambers.layout();
		scrolledComposite.setMinSize(grpIonChambers.computeSize(200, SWT.DEFAULT));

	}

	/**
	 *  Create composite showing photon energy textbox
	 */
	private void createEnergyControl( Composite parent ) {
		Composite compositeEnergy = new Composite(parent, SWT.NONE);
		compositeEnergy.setLayout(new GridLayout(2, false));
		compositeEnergy.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		( (GridData) compositeEnergy.getLayoutData() ).widthHint=200; // set size, so that textbox is not too wide

		final Text energyTextbox = addLabelAndTextBox( compositeEnergy, "Energy" );
		setTextboxFromDouble( energyTextbox, ionChambersBean.getEnergy() );

		// When photon energy is changed, set new 'working energy' for each ion chamber and recalculate the pressures
		energyTextbox.addModifyListener( new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Double energy = getDoubleFromTextbox(energyTextbox);
				if ( energy != null ) {
					ionChambersBean.setEnergy(energy);
					for( IonChamberParameters params : ionChambersBean.getIonChambers() )
						params.setWorkingEnergy(energy);

				}
				for( GasFillComposite gasFillComp : gasFillComposites ) {
					gasFillComp.updatePressure();
				}
			}
		} );

	}

	/**
	 *  Create composite showing settings for all ion chambers
	 */
	private void createGuiAllChambers( Composite parent ) {

		boolean showAmpVoltageControls = false;

		gasFillComposites = new ArrayList<GasFillComposite>();

		for( IonChamberParameters param :  ionChambersBean.getIonChambers() ) {

			Group grpIonChambers = new Group(parent, SWT.NONE);
			grpIonChambers.setLayout(new GridLayout(2, false));
			grpIonChambers.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			grpIonChambers.setText( "Ion chambers - "+param.getName() );

			if ( showAmpVoltageControls ) {
				AmplifierComposite amp = new AmplifierComposite(grpIonChambers, SWT.NONE);
				amp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
				amp.setGuiFromParameters( param );

				VoltageSupplyComposite voltage = new VoltageSupplyComposite(grpIonChambers, SWT.NONE);
				voltage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			}

			GasFillComposite gas = new GasFillComposite(grpIonChambers, SWT.NONE );
			gas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			param.setWorkingEnergy( ionChambersBean.getEnergy() );
			gas.setGuiFromParameters(param);
			gasFillComposites.add( gas );
		}
	}

	/**
	 * Create composite showing settings for single ion chamber;
	 * ion chamber to be shown is chosen using combo box
	 */
	private void createGuiChamberSelection( Composite parent ) {

		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		createIonchamberSelectionCombo( composite_1 );

		new Label(composite_1, SWT.NONE); // empty space for 2nd column

		amplifier = new AmplifierComposite(composite_1, SWT.NONE);
		amplifier.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		voltageSupply = new VoltageSupplyComposite(composite_1, SWT.NONE);
		voltageSupply.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		gasFilling = new GasFillComposite(composite_1, SWT.NONE );
		gasFilling.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		updateGuiForSelectedIonchamber();
	}

	/**
	 *  Update gui to show parameters for ion chamber currently selected in combo box
	 */
	private void updateGuiForSelectedIonchamber() {
		int index = ionChamberListCombo.getSelectionIndex();

		IonChamberParameters param = ionChambersBean.getIonChambers().get( index );
		amplifier.setGuiFromParameters( param );
		gasFilling.setGuiFromParameters( param );
	}

	/**
	 * Create combo box that can be used to select which ion chamber to show parameters for
	*/
	private void createIonchamberSelectionCombo( Composite parent ) {
		// Combo box with ion chamber names
		ionChamberListCombo = new Combo( parent, SWT.NONE);
		for( IonChamberParameters param :  ionChambersBean.getIonChambers() ) {
			ionChamberListCombo.add( param.getName() );
		}

		// Listener to update gui elements when different ion chamber is selected
		ionChamberListCombo.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateGuiForSelectedIonchamber();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		ionChamberListCombo.select(0);

		GridData gd_list = new GridData(SWT.LEFT, SWT.TOP, false, true, 1, 1);
		gd_list.widthHint = 90;
		ionChamberListCombo.setLayoutData(gd_list);

	}

	/**
	 * Add label and textbox to a composite.
	 * @param parent parent composite
	 * @param label String to use for label
	 * @return TextBox added to composite
	 */
	public static Text addLabelAndTextBox( Composite parent, String label ) {
		Label lblFillPeriod = new Label(parent, SWT.NONE);
		lblFillPeriod.setText(label);
		Text textBox = new Text(parent, SWT.BORDER);
		textBox.setLayoutData( new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1) );
		return textBox;
	}

	/**
	 * Set value in a textbox from floating point value.
	 * @param textbox
	 * @param dblValue
	 */
	public static void setTextboxFromDouble( Text textbox, Double dblValue ) {
		if ( textbox == null || dblValue == null )
			return;

		String strVal = Double.toString(dblValue);
		if ( strVal != null )
			textbox.setText( strVal );
	}

	/**
	 * Return floating point value from a text box.
	 * @param textbox
	 * @return Double representation of text value (may be null)
	 */
	public static Double getDoubleFromTextbox( Text textbox ) {
		if ( textbox == null )
			return null;
		String strVal = textbox.getText();
		if ( strVal != null && strVal.length() > 0 )
			return Double.valueOf( strVal );
		else
			return null;
	}
}