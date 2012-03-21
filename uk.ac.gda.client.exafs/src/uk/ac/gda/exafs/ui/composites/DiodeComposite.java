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

import gda.device.CurrentAmplifier;
import gda.device.Scannable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.components.wrappers.FindableNameWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

/**
 * @author fcp94556
 *
 */
public class DiodeComposite extends Composite  {
	
	private TextWrapper deviceName;
	private TextWrapper name;
	private TextWrapper currentAmplifierName;
	private SpinnerWrapper channel;
	private TextWrapper gain;
	private ExpansionAdapter expansionListener;
	private ExpandableComposite advancedExpandableComposite;

	/**
	 * @param parent
	 * @param style
	 */
	public DiodeComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());
		
		final Composite main = new Composite(this, SWT.NONE);
		main.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		main.setLayout(new FillLayout());
		
		final Composite left = new Composite(main, SWT.NONE);
		left.setLayout(new ColumnLayout());

		final Composite gainProperties = new Composite(left, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gainProperties.setLayout(gridLayout);
		
		final Label nameLabel = new Label(gainProperties, SWT.NONE);
		nameLabel.setText("Name");

		name = new FindableNameWrapper(gainProperties, SWT.BORDER, Scannable.class);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label gainLabel = new Label(gainProperties, SWT.NONE);
		gainLabel.setText("Gain");

		gain = new TextWrapper(gainProperties, SWT.BORDER);
		gain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Composite right = new Composite(main, SWT.NONE);
		right.setLayout(new FillLayout());

		this.advancedExpandableComposite = new ExpandableComposite(right, SWT.NONE);
		advancedExpandableComposite.setText("Advanced");

		final Composite advanced = new Composite(advancedExpandableComposite, SWT.NONE);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		advanced.setLayout(gridLayout_2);

		final Label deviceNameLabel = new Label(advanced, SWT.NONE);
		deviceNameLabel.setText("Device Name");

		deviceName = new FindableNameWrapper(advanced, SWT.BORDER, Scannable.class);
		deviceName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label channelLabel = new Label(advanced, SWT.NONE);
		channelLabel.setText("Channel");

		channel = new SpinnerWrapper(advanced, SWT.BORDER);
		channel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label currentAmplifierNameLabel = new Label(advanced, SWT.NONE);
		currentAmplifierNameLabel.setText("Current Amplifier Name");

		currentAmplifierName = new FindableNameWrapper(advanced, SWT.BORDER, CurrentAmplifier.class);
		currentAmplifierName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		

		advancedExpandableComposite.setClient(advanced);
		this.expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				getParent().layout();
			}
		};
		advancedExpandableComposite.addExpansionListener(expansionListener);

	}
	
	@Override
	public void dispose() {
		advancedExpandableComposite.removeExpansionListener(expansionListener);
		super.dispose();
	}

	/**
	 * @return variable
	 */
	public TextWrapper getGain() {
		return gain;
	}
	/**
	 * @return variable
	 */
	public SpinnerWrapper getChannel() {
		return channel;
	}
	/**
	 * @return variable
	 */
	public TextWrapper getCurrentAmplifierName() {
		return currentAmplifierName;
	}
	/**
	 * @return variable
	 */
	@SuppressWarnings("all")
	public TextWrapper getName() {
		return name;
	}
	/**
	 * @return variable
	 */
	public TextWrapper getDeviceName() {
		return deviceName;
	}
	

	
}

	