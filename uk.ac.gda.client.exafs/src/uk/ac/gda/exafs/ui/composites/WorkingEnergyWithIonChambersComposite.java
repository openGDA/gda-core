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

import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.binding.IBeanService;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.ActiveMode;
import org.eclipse.richbeans.widgets.scalebox.IntegerBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.selector.BeanSelectionEvent;
import org.eclipse.richbeans.widgets.selector.BeanSelectionListener;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

public class WorkingEnergyWithIonChambersComposite extends WorkingEnergyComposite {
	private final static Logger logger = LoggerFactory.getLogger(WorkingEnergyWithIonChambersComposite.class);

	protected VerticalListEditor ionChamberParameters;
	IonChamberComposite ionChamberComposite;
	private DetectorParameters provider;

	// diffraction section
	private BooleanWrapper collectDiffractionImages;
	private ScaleBox mythenEnergy;
	private ScaleBox mythenTime;
	private ScaleBox mythenFrames;
	private Label mythenEnergyLabel;
	private Label mythenTimeLabel;
	private Label mythenFramesLabel;

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

	protected void createIonChamberSection(DetectorParameters abean, IBeanController control) {
		getTabFolder().setText("Ion Chambers");

		this.ionChamberParameters = new VerticalListEditor(getTabFolder(), SWT.BORDER);
		ionChamberParameters.setEditorClass(IonChamberParameters.class);
		ionChamberParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);
		ionChamberParameters.setNameField("name");
		ionChamberParameters.setAdditionalFields(new String[] { "Gain", "GasType", "pressure" });
		ionChamberParameters.setMinItems(3);
		ionChamberParameters.setMaxItems(3);
		ionChamberParameters.setListHeight(75);
		this.ionChamberComposite = new IonChamberComposite(ionChamberParameters, SWT.NONE, ionChamberParameters, abean, control);
		ionChamberComposite.setWorkingEnergy(workingEnergy);
		ionChamberComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ionChamberParameters.setEditorUI(ionChamberComposite);
		ionChamberParameters.setListEditorUI(ionChamberComposite);
		if (!ExafsActivator.getDefault().getPreferenceStore().getDefaultBoolean(ExafsPreferenceConstants.HIDE_WORKING_ENERGY)) {
			if (ionChamberComposite.isUseGasProperties()) {
				ionChamberParameters.addBeanSelectionListener(new BeanSelectionListener() {
					@Override
					public void selectionChanged(BeanSelectionEvent evt) {
						ionChamberComposite.calculatePressure();
						try {
							IBeanService service = ExafsActivator.getService(IBeanService.class);
							IBeanController control = service.createController(ionChamberComposite, evt.getSelectedBean());
							control.uiToBean("pressure");
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
		}

		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.HIDE_DEFAULT_GAS_MIXTURES_BUTTON)) {
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
	}

	protected void createDiffractionSection(final Composite top) {
		boolean collectDiffractionData = ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.SHOW_MYTHEN);
		boolean diffractionCollectedWithFluoData = ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DIFFRACTION_COLLECTED_CONCURRENTLY);

		if (collectDiffractionData) {
			Label collectDiffImagesLabel = new Label(top, SWT.NONE);
			collectDiffImagesLabel.setText("Diffraction Images");
			collectDiffImagesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			Composite diffractionComp = new Composite(top, SWT.NONE);
			diffractionComp.setLayout(new GridLayout(7, true));
			collectDiffractionImages = new BooleanWrapper(diffractionComp, SWT.NONE);
			collectDiffractionImages.setToolTipText("Collect diffraction data");
			collectDiffractionImages.setText("Collect");

			// extra options when collecting diffraction data separately from fluo data
			if (!diffractionCollectedWithFluoData){
				mythenEnergyLabel = new Label(diffractionComp, SWT.NONE);
				mythenEnergyLabel.setText("     Energy");
				mythenEnergy = new ScaleBox(diffractionComp, SWT.NONE);
				mythenEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				mythenEnergy.setMaximum(20000.0);
				mythenEnergy.setUnit("eV");
				mythenTimeLabel = new Label(diffractionComp, SWT.NONE);
				mythenTimeLabel.setText("     Time");
				mythenTime = new ScaleBox(diffractionComp, SWT.NONE);
				mythenTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				mythenFramesLabel = new Label(diffractionComp, SWT.NONE);
				mythenFramesLabel.setText("     Frames");
				mythenFrames = new IntegerBox(diffractionComp, SWT.NONE);
				mythenFrames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				showMythenParameters(collectDiffractionImages.getValue());

				collectDiffractionImages.addValueListener(new ValueListener() {

					@Override
					public String getValueListenerName() {
						return null;
					}

					@Override
					public void valueChangePerformed(ValueEvent e) {
						showMythenParameters(collectDiffractionImages.getValue());
					}
				});
			}
		}
	}

	void showMythenParameters(boolean show) {
		mythenEnergy.setVisible(show);
		mythenTime.setVisible(show);
		mythenFrames.setVisible(show);
		mythenEnergyLabel.setVisible(show);
		mythenTimeLabel.setVisible(show);
		mythenFramesLabel.setVisible(show);
	}

	public ScaleBox getMythenEnergy() {
		return mythenEnergy;
	}

	public ScaleBox getMythenTime() {
		return mythenTime;
	}

	public ScaleBox getMythenFrames() {
		return mythenFrames;
	}

	/**
	 * Return whether or not to collect diffraction images
	 *
	 * @return true if we should collect them, else false
	 */
	public BooleanWrapper getCollectDiffractionImages() {
		return collectDiffractionImages;
	}

	public VerticalListEditor getIonChamberParameters() {
		return ionChamberParameters;
	}

	public IonChamberComposite getIonChamberComposite() {
		return ionChamberComposite;
	}
}
