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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldBeanComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.exafs.util.WorkingEnergyHelper;
import uk.ac.gda.exafs.util.WorkingEnergyParams;

public class WorkingEnergyComposite extends FieldBeanComposite {

	private static final Logger logger = LoggerFactory.getLogger(WorkingEnergyComposite.class);
	protected ScaleBox workingEnergy;
	protected Button selectDefaultsBtn;
	private Group tabFolder;

	public WorkingEnergyComposite(Composite parent, int style, DetectorParameters abean) {
		super(parent, style);
	}

	protected void createEdgeEnergy(Composite comp, IBeanController control) {

		Label workingELbl = new Label(comp, SWT.NONE);
		workingELbl.setText("Calculate ion chamber gas filling for energy:");

		Composite workingEComp = new Composite(comp, SWT.NONE);
		GridLayout glWorkingE = new GridLayout(3, false);
		glWorkingE.marginHeight = 0;
		glWorkingE.marginWidth = 0;
		workingEComp.setLayout(glWorkingE);
		GridData gdWorkingE = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdWorkingE.widthHint = 490;
		workingEComp.setLayoutData(gdWorkingE);

		workingEnergy = new ScaleBox(workingEComp, SWT.NONE);
		workingEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		workingEnergy.setMaximum(20000.0);
		workingEnergy.setUnit("eV");
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.widthHint = 120;
		workingEnergy.setLayoutData(gd);

		Button workingEnergyBtn = new Button(workingEComp, SWT.NONE);
		workingEnergyBtn.setText("Get Energy From Scan");
		workingEnergyBtn.setToolTipText("Click to take edge energy from currently open scan parameters.");
		workingEnergyBtn.addSelectionListener(SelectionListener.widgetSelectedAdapter(listener-> setWorkingEnergyFromEdge()));

		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.HIDE_DEFAULT_GAS_MIXTURES_BUTTON)) {
			this.selectDefaultsBtn = new Button(workingEComp, SWT.NONE);
			selectDefaultsBtn.setText("Set Default Gas Mixtures");
			selectDefaultsBtn.setToolTipText("Click to set ion chamber gas types to defaults.");
		}

		// Set the working energy value and limits from scan parameters
		// (working energy value is set to correct value from the detector parameters once the widgets have been created)
		setWorkingEnergyFromEdge();

		try {
			control.addBeanFieldValueListener("EdgeEnergy", new ValueAdapter("EdgeEnergyListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateWorkingEnergy(e);
				}

			});
		} catch (Exception ne) {
			logger.error("Cannot add EdgeEnergy listeners.", ne);
		}
	}

	protected void setWorkingEnergyFromEdge() {
		try {
			IEditorPart[] dirtyEditors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getDirtyEditors();

			// Prompt the user to check if 'dirty' editors should be saved.
			if(dirtyEditors.length>0 &&
				MessageDialog.openQuestion(getShell(), "Save Editor",
				"The editors need to be saved to get the edge energy. Would you like to save before proceeding?")) {
				for(int i=0;i<dirtyEditors.length;i++){
					dirtyEditors[i].doSave(new NullProgressMonitor());
				}
			}

			WorkingEnergyParams workingParams = WorkingEnergyHelper.createFromScanParameters();

			logger.debug("Working energy parameters : {}", workingParams);
			workingEnergy.setValue(workingParams.getValue());
			// Only apply the energy limits to the scalebox if they have been set (they are not present if using microfocus scan params)
			if (workingParams.getMin() != null) {
				workingEnergy.setMinimum(workingParams.getMin());
			}
			if (workingParams.getMax() != null) {
				workingEnergy.setMaximum(workingParams.getMax());
			}

		} catch (Exception ne) {
			logger.error("Cannot get edge energy for element.", ne);
		}
	}

	private void updateWorkingEnergy(ValueEvent e) {
		if(!workingEnergy.isDisposed()){
			workingEnergy.off();
			workingEnergy.setValue(e.getValue());
			workingEnergy.on();
		}
	}

	protected Group getTabFolder() {
		if (tabFolder == null) {
			tabFolder = new Group(this, SWT.NONE);
			tabFolder.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
			tabFolder.setLayout(new GridLayout());
		}
		return tabFolder;
	}

	public ScaleBox getWorkingEnergy() {
		return workingEnergy;
	}

}
