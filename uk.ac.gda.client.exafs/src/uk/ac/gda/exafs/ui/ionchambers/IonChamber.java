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

import static uk.ac.gda.beans.exafs.IonChamberParameters.I_0;
import static uk.ac.gda.beans.exafs.IonChamberParameters.I_REF;
import static uk.ac.gda.beans.exafs.IonChamberParameters.I_T;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.GasType;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.IonChambersBean;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.exafs.util.WorkingEnergyHelper;
import uk.ac.gda.exafs.util.WorkingEnergyParams;

/**
 * SWTBot test: uk.ac.gda.exafs.ui.IonChamberUITest
 */
public class IonChamber {
	private static final Logger logger = LoggerFactory.getLogger(IonChamber.class);

	private String[] defaultIonChambers = {I_0, I_T, I_REF};
	private IonChambersBean ionChambersBean;
	private List<GasFillComposite> gasFillComposites = Collections.emptyList();
	private List<Button> allRunFillSequenceButtons = new ArrayList<>();
	private boolean showGetEnergyButton = ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.IONCHAMBERS_SHOW_ENERGY_FROM_SCAN_BUTTON);

	public IonChamber(Composite parent, IonChambersBean bean) {

		parent.setLayout(new FillLayout());
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		ionChambersBean = bean;

		// Create default ionchambers if not present in template
		if(bean.getIonChambers().isEmpty()) {
			for(var defaultParam : defaultIonChambers) {
				IonChamberParameters ionChamber = new IonChamberParameters();
				ionChamber.setName(defaultParam);
				ionChambersBean.addIonChamber( ionChamber );
			}
		}

		final Group mainGroup = new Group(scrolledComposite, SWT.NONE);

		mainGroup.setLayout( new GridLayout(1, false) );
		mainGroup.setText("Ion Chambers");

		createEnergyControl(mainGroup);

		final Composite gasfillComp = new Composite(mainGroup, SWT.NONE);
		gasfillComp.setLayout( new GridLayout(2, false) );
		createFillAllControl(gasfillComp);
		createSetDefaultsControl(gasfillComp);

		createGuiAllChambers(mainGroup);

		// Add the run fill sequence buttons to the list of all buttons
		allRunFillSequenceButtons.addAll(
				gasFillComposites.stream()
				.map(GasFillComposite::getRunFillSequenceButton)
				.collect(Collectors.toList())
		);

		scrolledComposite.setContent(mainGroup);
		mainGroup.layout();
		scrolledComposite.setMinSize(mainGroup.computeSize(200, SWT.DEFAULT));
	}

	/**
	 *  Create composite showing photon energy textbox
	 */
	private void createEnergyControl( Composite parent ) {
		Composite compositeEnergy = new Composite(parent, SWT.NONE);
		compositeEnergy.setLayout(new GridLayout(3, false));
		compositeEnergy.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		final Text energyTextbox = addLabelAndTextBox( compositeEnergy, "Energy (eV)" );
		setTextboxFromDouble( energyTextbox, ionChambersBean.getEnergy() );
		( (GridData) energyTextbox.getLayoutData() ).widthHint=80;

		if (showGetEnergyButton) {
			Button energyFromScanButton = new Button(compositeEnergy, SWT.PUSH);
			energyFromScanButton.setText("Get energy from scan");
			energyFromScanButton.setToolTipText("Get energy from currently open scan parameters");

			energyFromScanButton.addSelectionListener(
					SelectionListener.widgetSelectedAdapter(event -> updateWithEnergyFromScan(energyTextbox)));
		}

		// When photon energy is changed, set new 'working energy' for each ion chamber and recalculate the pressures
		energyTextbox.addModifyListener(e -> {
			Double energy = getDoubleFromTextbox(energyTextbox);
			if (energy != null) {
				ionChambersBean.setEnergy(energy);
				ionChambersBean.getIonChambers().forEach(p -> p.setWorkingEnergy(energy));
				gasFillComposites.forEach(GasFillComposite::updatePressure);
			}
		} );
	}

	private void updateWithEnergyFromScan(Text energyTextBox) {
		try {
			WorkingEnergyParams p = WorkingEnergyHelper.createFromScanParameters();
			setTextboxFromDouble(energyTextBox, p.getValue());
		} catch (Exception e) {
			logger.error("Problem getting energy from scan", e);
		}
	}

	private void createFillAllControl( Composite parent ) {
		Button fillAllButton = new Button(parent, SWT.PUSH);
		fillAllButton.setText("Fill all ionchambers");
		fillAllButton.setToolTipText("Run the fill sequence for each ionchamber, one after another");
		fillAllButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event-> runFillAll()));
		allRunFillSequenceButtons.add(fillAllButton);
	}

	private void runFillAll() {
		gasFillComposites.forEach(GasFillComposite::updateParametersFromGui);
		GasFill.runGasFill(ionChambersBean.getIonChambers(), allRunFillSequenceButtons);
	}

	private void createSetDefaultsControl(Composite parent) {
		Button defaultMixturesButton = new Button(parent, SWT.PUSH);
		defaultMixturesButton.setText("Set default gas mixtures");
		defaultMixturesButton.setToolTipText("Set ion chamber gas fill types to default values for the current energy");
		defaultMixturesButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			setDefaultGasMixtures();
			updateGuiFromParameters();
		}));
	}

	private void setDefaultGasMixtures() {
		double workingEnergy = ionChambersBean.getEnergy();
		GasType gasForI0 = GasType.getI0GasType(workingEnergy);
		GasType gasForItIref = GasType.getItIrefGasType(workingEnergy);
		logger.debug("Setting default gas mixture for {} eV. I0 = {}, It/Iref = {}", workingEnergy, gasForI0.getName(), gasForItIref.getName());
		List<IonChamberParameters> params = ionChambersBean.getIonChambers();

		// Find the IonChamberParameters for I0 and set the I0 gas type
		params.stream()
			.filter(c -> c.getName().equals(I_0))
			.forEach(c -> c.setGasType(gasForI0.getName()));

		// Find the IonChamberParameters for It and Iref and set the It, Iref gas type
		params.stream()
			.filter(c -> c.getName().matches(I_T+"|"+I_REF))
			.forEach(c -> c.setGasType(gasForItIref.getName()));
	}

	/**
	 *  Update gas fill composites to show the latest parameters
	 */
	private void updateGuiFromParameters() {
		for(int i=0; i<ionChambersBean.getIonChambers().size(); i++) {
			IonChamberParameters params = ionChambersBean.getIonChambers().get(i);
			gasFillComposites.get(i).setGuiFromParameters(params);
		}
	}

	/**
	 *  Create composite showing settings for all ion chambers
	 */
	private void createGuiAllChambers( Composite parent ) {

		boolean showAmpVoltageControls = false;

		gasFillComposites = new ArrayList<>();

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