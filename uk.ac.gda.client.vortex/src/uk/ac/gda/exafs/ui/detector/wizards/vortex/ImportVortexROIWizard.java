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

package uk.ac.gda.exafs.ui.detector.wizards.vortex;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.exafs.ui.detector.wizards.ImportROIWizardPage;
import uk.ac.gda.exafs.ui.detectorviews.DetectorElementComposite;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;

/**
 * This is the Import Wizard that allows Regions Of Interest to be imported into the
 * current DetectorEditor from another bean xml file. 
 */
public class ImportVortexROIWizard extends Wizard {
	private ImportROIWizardPage mainPage;
	private int elementListSize;
	private DetectorElementComposite detectorElementComposite;
	private GridListEditor detectorList;
	
	public ImportVortexROIWizard(int elementListSize, DetectorElementComposite detectorElementComposite, GridListEditor detectorList) {
		super();
		this.elementListSize = elementListSize;
		this.detectorElementComposite = detectorElementComposite;
		this.detectorList = detectorList;
	}

	@Override
	public boolean performFinish() {
		VerticalListEditor regionList = detectorElementComposite.getRegionList();
		regionList.setValue(mainPage.getBeansToAdd());
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		VerticalListEditor regionList = detectorElementComposite.getRegionList();
		double maximum = detectorElementComposite.getEnd().getMaximum();
		// the region list must store the right type
		@SuppressWarnings("unchecked")
		List<? extends DetectorROI> value = (List<? extends DetectorROI>)regionList.getValue();
		List<? extends DetectorROI> clonedValue;
		try {
			clonedValue = BeansFactory.deepClone(value);
		} catch (Exception e) {
			clonedValue = null;
		}
		mainPage = new ImportVortexROIWizardPage(elementListSize, clonedValue, maximum);			
		mainPage.setListEditor(detectorList);
		addPage(mainPage);
	}

}