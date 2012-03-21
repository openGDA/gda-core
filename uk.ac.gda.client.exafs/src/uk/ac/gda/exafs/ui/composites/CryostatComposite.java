/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.scalebox.NumberBox;
import uk.ac.gda.richbeans.components.scalebox.RangeBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 * @author fcp94556
 *
 */
public class CryostatComposite extends FieldBeanComposite {

	private RangeBox     finePosition;
	private RangeBox     position;
	private RangeBox     sampleNumber;
	private ComboWrapper sampleHolder;
	private ComboWrapper profileType;
	private ScaleBox     time;
	private SpinnerWrapper heaterRange;
	private RangeBox     temperature;
	private ScaleBox     p,i,d,ramp;
	private StackLayout  advStack;
	private ScaleBox     tolerance;
	private ExpandableComposite advancedExpandableComposite;
	private Composite rampChoice, pidChoice, advChoice;
	private ExpansionAdapter expansionListener;
	private Label sampleNumberLabel;
	
	/**
	 * @param parent
	 * @param style
	 */
	@SuppressWarnings("unused")
	public CryostatComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.VERTICAL));

		final Composite main = new Composite(this, SWT.NONE);
		main.setLayout(new FillLayout());
		
		final Composite left = new Composite(main, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		left.setLayout(gridLayout);

		final Label temperatureLabel = new Label(left, SWT.NONE);
		temperatureLabel.setText("Set Point");

		temperature = new RangeBox(left, SWT.NONE);
		temperature.setMaximum(300);
		temperature.setUnit("K");
		final GridData gd_temperature = new GridData(SWT.FILL, SWT.CENTER, true, false);
		temperature.setLayoutData(gd_temperature);

		final Label toleranceLabel = new Label(left, SWT.NONE);
		toleranceLabel.setText("Tolerance");

		tolerance = new ScaleBox(left, SWT.NONE);
		final GridData gd_tolerance = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tolerance.setLayoutData(gd_tolerance);
		tolerance.setMaximum(5);

		final Label timeLabel = new Label(left, SWT.NONE);
		timeLabel.setText("Wait Time");

		time = new ScaleBox(left, SWT.NONE);
		final GridData gd_time = new GridData(SWT.FILL, SWT.CENTER, true, false);
		time.setLayoutData(gd_time);
		time.setUnit("s");
		time.setMaximum(400.0);
		
		new Label(left, SWT.NONE);
		new Label(left, SWT.NONE);

		final Label sampleHolderLabel = new Label(left, SWT.NONE);
		sampleHolderLabel.setText("Sample Holder");

		sampleHolder = new ComboWrapper(left, SWT.READ_ONLY);
		sampleHolder.select(0);
		sampleHolder.setItems(new String[] {"2 Samples", "3 Samples", "4 Samples", "Liquid Cell"});
		final GridData gd_sampleHolder = new GridData(SWT.FILL, SWT.CENTER, false, false);
		sampleHolder.setLayoutData(gd_sampleHolder);

		this.sampleNumberLabel = new Label(left, SWT.NONE);
		sampleNumberLabel.setText("Sample Number");

		sampleNumber = new RangeBox(left, SWT.NONE);
		sampleNumber.setMinimum(1);
		sampleNumber.setMaximum(2);
		sampleNumber.setIntegerBox(true);
		sampleNumber.setLayoutData(gd_temperature);
		
		sampleHolder.addValueListener(new ValueAdapter("sampleHolderListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateSampleRange();
			}	
		});

		final Label positionLabel = new Label(left, SWT.NONE);
		positionLabel.setText("Position");

		position = new RangeBox(left, SWT.NONE);
		position.setMinimum(-15);
		position.setMaximum(15);
		position.setUnit("mm");
		final GridData gd_position = new GridData(SWT.FILL, SWT.CENTER, true, false);
		position.setLayoutData(gd_position);

		final Label finePositionLabel = new Label(left, SWT.NONE);
		finePositionLabel.setText("Fine Position");

		finePosition = new RangeBox(left, SWT.NONE);
		finePosition.setDecimalPlaces(4);
		final GridData gd_finePosition = new GridData(SWT.FILL, SWT.CENTER, true, false);
		finePosition.setLayoutData(gd_finePosition);
		finePosition.setUnit("mm");
		finePosition.setMinimum(-1);
		finePosition.setMaximum(1);


		final Composite right = new Composite(main, SWT.NONE);
		right.setLayout(new FillLayout());

		this.advancedExpandableComposite = new ExpandableComposite(right, SWT.NONE);
		advancedExpandableComposite.setText("Advanced");
		
		final Composite advanced = new Composite(advancedExpandableComposite, SWT.NONE);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		advanced.setLayout(gridLayout_2);


		final Label temperatureChangeProfileLabel = new Label(advanced, SWT.NONE);
		temperatureChangeProfileLabel.setText("Temperature Adjust");

		profileType = new ComboWrapper(advanced, SWT.READ_ONLY);
		profileType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		profileType.setItems(new String[] {"PID", "Ramp"});
		profileType.select(0);
		
		new Label(advanced, SWT.NONE);
		
		this.advChoice = new Composite(advanced, SWT.NONE);
		advChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.advStack = new StackLayout();
		advChoice.setLayout(advStack);
		
		this.pidChoice = new Composite(advChoice, SWT.NONE);
		pidChoice.setLayout(gridLayout_2);
		advStack.topControl = pidChoice;
		
		final Label pLabel = new Label(pidChoice, SWT.NONE);
		pLabel.setText("P");
		
		this.p = new ScaleBox(pidChoice,SWT.NONE);
		p.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label iLabel = new Label(pidChoice, SWT.NONE);
		iLabel.setText("I");
		
		this.i = new ScaleBox(pidChoice,SWT.NONE);
		i.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Label dLabel = new Label(pidChoice, SWT.NONE);
		dLabel.setText("D");
		
		this.d = new ScaleBox(pidChoice,SWT.NONE);
		d.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		this.rampChoice = new Composite(advChoice, SWT.NONE);
		rampChoice.setLayout(gridLayout_2);
		
		final Label rampLabel = new Label(rampChoice, SWT.NONE);
		rampLabel.setText("Ramp");
		
		this.ramp = new ScaleBox(rampChoice,SWT.NONE);
		ramp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ramp.setUnit("K/minute");
		ramp.setMinimum(0.1d);
		ramp.setMaximum(100d);
		ramp.setValue(1d);
		
		final Label heaterLabel = new Label(advanced, SWT.NONE);
		heaterLabel.setText("Heater Range");

		heaterRange = new SpinnerWrapper(advanced, SWT.BORDER);
		heaterRange.setMinimum(1);
		heaterRange.setMaximum(5);
		heaterRange.setLayoutData(new GridData());

		advancedExpandableComposite.setClient(advanced);
		this.expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				updatePidLayout();
				getParent().getParent().getParent().layout();
			}
		};
		advancedExpandableComposite.addExpansionListener(expansionListener);
		
		profileType.setNotifyType(NOTIFY_TYPE.ALWAYS);
		profileType.addValueListener(new ValueAdapter("profileTypeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updatePidLayout();
				if (ramp.getNumericValue()<0.1d) {
					ramp.setNumericValue(1d);
				}
			}
		});
	}
	
	protected void updateSampleRange() {
		final int index = sampleHolder.getSelectionIndex();
		if (index<3) {
			sampleNumber.setMaximum(index+2);
			sampleNumberLabel.setVisible(true);
			sampleNumber.setVisible(true);
		} else {
			sampleNumber.setValue(1);
			sampleNumberLabel.setVisible(false);
			sampleNumber.setVisible(false);
		}
	}

	@Override
	public void setValue(Object value) {
        super.setValue(value);
        updateSampleRange();
	}
	
	@Override
	public void dispose() {
		advancedExpandableComposite.removeExpansionListener(expansionListener);
		super.dispose();
	}
	

	private void updatePidLayout() {
		advStack.topControl = profileType.getSelectionIndex()==0
			                ? pidChoice
			                : rampChoice;
		advChoice.layout();
	}

	/**
	 * @return ScaleBox
	 */ 
	public NumberBox getTemperature() {
		return temperature;
	}
	/**
	 * @return ScaleBox
	 */ 
	public ScaleBox getTolerance() {
		return tolerance;
	}
	/**
	 * @return ScaleBox
	 */ 
	public ScaleBox getTime() {
		return time;
	}
	
	/**
	 * 
	 * @return d
	 */
	public ComboWrapper getProfileType() {
		return profileType;
	}
	/**
	 * @return Returns the p.
	 */
	public ScaleBox getP() {
		return p;
	}
	/**
	 * @return Returns the i.
	 */
	public ScaleBox getI() {
		return i;
	}
	/**
	 * @return Returns the d.
	 */
	public ScaleBox getD() {
		return d;
	}
	/**
	 * @return Returns the ramp.
	 */
	public ScaleBox getRamp() {
		return ramp;
	}
	/**
	 * @return Returns the heaterRange.
	 */
	public SpinnerWrapper getHeaterRange() {
		return heaterRange;
	}
	/**
	 * @return g
	 */
	public ComboWrapper getSampleHolder() {
		return sampleHolder;
	}
	/**
	 * @return g
	 */
	public RangeBox getSampleNumber() {
		return sampleNumber;
	}
	/**
	 * @return h
	 */
	public RangeBox getPosition() {
		return position;
	}
	/**
	 * @return h
	 */
	public RangeBox getFinePosition() {
		return finePosition;
	}
	
	/**
	 * Used to show advanced in from test deck.
	 */
	public void _testSetAdvancedActive() {
		advancedExpandableComposite.setExpanded(true);
	}

	/**
	 * Used in testing.
	 * @return true if pid top
	 */
	public boolean _testIsPidTop() {
		return advStack.topControl == pidChoice;
	}
}

	