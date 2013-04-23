/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.richbeans.ACTIVE_MODE;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

// Apologies for the long name, feel free to rename it if you can come up with something shorter
public class WorkingEnergyWithIonChambersComposite extends WorkingEnergyComposite {
	private final static Logger logger = LoggerFactory.getLogger(WorkingEnergyWithIonChambersComposite.class);

	protected VerticalListEditor ionChamberParameters;
	IonChamberComposite ionChamberComposite;
	private DetectorParameters provider;

	public WorkingEnergyWithIonChambersComposite(Composite parent, int style, DetectorParameters abean) {
		super(parent, style, abean);
		provider = abean;
	}

	public void setExperimentType(String type) {
		if (ionChamberComposite != null) {
			ionChamberComposite.setExperimentType(type);
			ionChamberComposite.calculatePressure();
		}
	}

	protected void createIonChamberSection(DetectorParameters abean) {
		getTabFolder().setText("Ion Chambers");

		this.ionChamberParameters = new VerticalListEditor(getTabFolder(), SWT.BORDER);
		ionChamberParameters.setEditorClass(IonChamberParameters.class);
		ionChamberParameters.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);
		ionChamberParameters.setNameField("name");
		ionChamberParameters.setAdditionalFields(new String[] { "Gain", "GasType", "pressure" });
		ionChamberParameters.setMinItems(3);
		ionChamberParameters.setMaxItems(3);
		ionChamberParameters.setListHeight(75);
		this.ionChamberComposite = new IonChamberComposite(ionChamberParameters, SWT.NONE, ionChamberParameters, abean);
		ionChamberComposite.setWorkingEnergy(workingEnergy);
		ionChamberComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ionChamberParameters.setEditorUI(ionChamberComposite);
		ionChamberParameters.setListEditorUI(ionChamberComposite);
		if (ionChamberComposite.isUseGasProperties()) {
			ionChamberParameters.addBeanSelectionListener(new BeanSelectionListener() {
				@Override
				public void selectionChanged(BeanSelectionEvent evt) {
					ionChamberComposite.calculatePressure();
					try {
						BeanUI.uiToBean(ionChamberComposite, evt.getSelectedBean(), "pressure");
					} catch (Exception e) {
						logger.error("Error sending bean value to bean.", e);
					}
				}
			});	
		}
		workingEnergy.addValueListener(new ValueAdapter("workingEnergyListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				ionChamberComposite.calculatePressure();
			}
		});

		this.selectDefaultsListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				double workingEnergy = 0;
				if (provider.getExperimentType().toString().equals("Transmission")) {
					workingEnergy = provider.getTransmissionParameters().getWorkingEnergy();
				} else if (provider.getExperimentType().toString().equals("Fluorescence")) {
					workingEnergy = provider.getFluorescenceParameters().getWorkingEnergy();
				}

				ionChamberComposite.calculateDefaultGasType(workingEnergy);
			}
		};
		selectDefaultsBtn.addSelectionListener(selectDefaultsListener);
	}

	/**
	 * @return BeanListEditor
	 */
	public VerticalListEditor getIonChamberParameters() {
		return ionChamberParameters;
	}

	public IonChamberComposite getIonChamberComposite() {
		return ionChamberComposite;
	}
}
