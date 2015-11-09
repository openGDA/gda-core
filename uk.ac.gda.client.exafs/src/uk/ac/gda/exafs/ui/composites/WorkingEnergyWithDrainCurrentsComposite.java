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

import org.eclipse.richbeans.api.reflection.IBeanController;
import org.eclipse.richbeans.api.widget.ActiveMode;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.DrainCurrentParameters;

// Apologies for the long name, feel free to rename it if you can come up with something shorter
public class WorkingEnergyWithDrainCurrentsComposite extends WorkingEnergyComposite {

	protected VerticalListEditor      drainCurrentParameters;
	protected DrainCurrentComposite drainCurrentComposite;


	public WorkingEnergyWithDrainCurrentsComposite(Composite parent, int style, DetectorParameters abean) {
		super(parent, style, abean);
	}

	protected void createDrainCurrentSection(IBeanController control) {

//		final TabItem ionChambersTabItem = new TabItem(getTabFolder(), SWT.NONE);
//		ionChambersTabItem.setText("Drain Currents");
		getTabFolder().setText("Drain Currents");

		drainCurrentParameters = new VerticalListEditor(getTabFolder(), SWT.BORDER);
		drainCurrentParameters.setEditorClass(DrainCurrentParameters.class);
		drainCurrentComposite = new DrainCurrentComposite(drainCurrentParameters, SWT.NONE, control);
		drainCurrentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		drainCurrentParameters.setEditorUI(drainCurrentComposite);
		drainCurrentParameters.setListEditorUI(drainCurrentComposite);
		drainCurrentParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);
		drainCurrentParameters.setNameField("name");
		drainCurrentParameters.setAdditionalFields(new String[]{"Gain"});
		drainCurrentParameters.setMinItems(2);
		drainCurrentParameters.setMaxItems(2);
		drainCurrentParameters.setListHeight(100);
		drainCurrentParameters.setColumnWidths(300,300);
		//ionChambersTabItem.setControl(drainCurrentParameters);


	}

	/**
	 * @return BeanListEditor
	 */
	public VerticalListEditor getDrainCurrentParameters() {
		return drainCurrentParameters;
	}

}
