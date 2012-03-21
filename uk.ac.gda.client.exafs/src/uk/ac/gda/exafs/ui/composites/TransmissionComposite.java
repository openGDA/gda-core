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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

/**
 * @author fcp94556
 */
public class TransmissionComposite extends WorkingEnergyWithIonChambersComposite {

	private BooleanWrapper collectDiffractionImages;
	private ScaleBox mythenEnergy;
	private ScaleBox mythenTime;

	/**
	 * @param parent
	 * @param style
	 */
	public TransmissionComposite(Composite parent, int style, DetectorParameters abean) {
		super(parent, style, abean);
		setLayout(new GridLayout());

		final Composite top = new Composite(this, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		top.setLayout(gridLayout);
		
		if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.SHOW_MYTHEN)) {

			final Label collectDiffImagesLabel = new Label(top, SWT.NONE);
			collectDiffImagesLabel.setText("Diffraction Images");

			final Composite diffractionComp = new Composite(top, SWT.NONE);
			diffractionComp.setLayout(new GridLayout(5, true));

			collectDiffractionImages = new BooleanWrapper(diffractionComp, SWT.NONE);
			collectDiffractionImages.setToolTipText("Collect diffraction data at the start and end of scans");
			collectDiffractionImages.setText("Collect");

			final Label mythenEnergyLabel = new Label(diffractionComp, SWT.NONE);
			mythenEnergyLabel.setText("     Energy");
			mythenEnergy = new ScaleBox(diffractionComp, SWT.NONE);
			mythenEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			mythenEnergy.setMaximum(20000.0);
			mythenEnergy.setUnit("eV");

			final Label mythenTimeLabel = new Label(diffractionComp, SWT.NONE);
			mythenTimeLabel.setText("     Time");
			mythenTime = new ScaleBox(diffractionComp, SWT.NONE);
			mythenTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


					
			collectDiffractionImages.addValueListener(new ValueListener() {

				@Override
				public String getValueListenerName() {
					return null;
				}

				@Override
				public void valueChangePerformed(ValueEvent e) {
					mythenEnergy.setVisible(collectDiffractionImages.getValue());
					mythenTime.setVisible(collectDiffractionImages.getValue());
					mythenEnergyLabel.setVisible(collectDiffractionImages.getValue());
					mythenTimeLabel.setVisible(collectDiffractionImages.getValue());
				}
			});
		}

		createEdgeEnergy(top);

		createIonChamberSection(abean);
	}

	public ScaleBox getMythenEnergy() {
		return mythenEnergy;
	}

	public ScaleBox getMythenTime() {
		return mythenTime;
	}

	public BooleanWrapper getCollectDiffractionImages() {
		return collectDiffractionImages;
	}
}
