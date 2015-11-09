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

package uk.ac.gda.exafs.ui.detector.wizards;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.richbeans.reflection.BeansFactory;
import org.eclipse.richbeans.widgets.selector.ListEditor;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;

/**
 * This is the Import Wizard that allows Regions Of Interest to be imported into the current DetectorEditor from another
 * bean xml file.
 * <p>
 * The way to use it is as follows:
 * <p>
 * <ol>
 * <li>From within (e.g.) Vortex Parameters Editor UI, press the Import Regions Of Interest button.</li>
 * <li>Select another Vortect Parameters file in the "Select Detector File to Import" box</li>
 * <li>Now (assuming a valid file) the imported file is on the left, and the bean currently being edited is on the
 * right.</li>
 * <li>(If more than 1 element) Choose which element to import from by clicking on it in the GridListEditor</li>
 * <li>Click on the region to import in the left list</li>
 * <li>Press Copy Selected</li>
 * <li>The region has now been copied into the current list</li>
 * <li>Press Finish to accept the changes, or Cancel to return to the Editor</li>
 * </ol>
 * <p>
 * Some notes:
 * <ul>
 * <li>Unnamed Regions Of Interest do not maintain their display name in the list when copied. (e.g. ROI 2 when copied
 * may become ROI 4) But it will still be added to the end of the list. Recommend to always have explicit names on
 * source files.</li>
 * <li>If you want to pre-fill a detector file to import (e.g. as I18 wants to have a common one to import from),
 * override {@link ImportROIWizardPage#getInitialSourceValue()}</li>
 * <li>To achieve having a common file that can be imported from, create the file initially in the Xspress or Vortex
 * editor and then copy/move the xml file to a common location.</li>
 * <li>It may be desirable to have more ROIs in the common file than the GUI will let you add. However, you can add more
 * regions by editing the XML directly. Once the regions have been added in the XML file, you can edit the contents in
 * the GUI editor</li>
 * </ul>
 * <p>
 */
public class ImportFluoDetROIWizard extends Wizard {

	private ImportFluoDetROIWizardPage mainPage;
	private ListEditor regionList;
	private ListEditor elementList;
	private Class<? extends FluorescenceDetectorParameters> detectorParametersClass;

	public ImportFluoDetROIWizard(ListEditor regionList, ListEditor elementList,
			Class<? extends FluorescenceDetectorParameters> detectorParametersClass) {
		super();
		this.regionList = regionList;
		this.elementList = elementList;
		this.detectorParametersClass = detectorParametersClass;
	}

	@Override
	public boolean performFinish() {
		regionList.setValue(mainPage.getBeansToAdd());
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		double maximum = 4096.0;

		// the region list must store the right type
		@SuppressWarnings("unchecked")
		List<? extends DetectorROI> value = (List<? extends DetectorROI>) regionList.getValue();
		List<? extends DetectorROI> clonedValue;
		try {
			clonedValue = BeansFactory.deepClone(value);
		} catch (Exception e) {
			clonedValue = null;
		}
		mainPage = new ImportFluoDetROIWizardPage(elementList.getListSize(), clonedValue, maximum,
				detectorParametersClass);
		mainPage.setListEditor(elementList);
		addPage(mainPage);
	}

}
