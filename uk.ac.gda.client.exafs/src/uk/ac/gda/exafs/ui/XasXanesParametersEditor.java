/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public abstract class XasXanesParametersEditor extends ExafsBeanFileSelectionEditor implements DirtyContainer {

	@Override
	public void doSaveAs() {
		// store the current filename and then do the regular save as process
		String initialFileName = this.getPartName();
		super.doSaveAs();

		// if we are in XES mode (I20) and we need to ensure that the XES editor's xas/xanes scan file name is updated
		if (this.getPartName() != initialFileName) {

			// are we in xes mode? In that case inform the XES editor that its associated scan file has been renamed
			try {
				IExperimentEditorManager man = ExperimentFactory.getExperimentEditorManager();
				IScanParameters scanParams = ((ScanObject) man.getSelectedScan()).getScanParameters();

				if (scanParams != null && scanParams instanceof XesScanParameters) {
					XesScanParameters xesParams = (XesScanParameters) scanParams;
					if (xesParams.getScanType() == XesScanParameters.FIXED_XES_SCAN_XANES
							|| xesParams.getScanType() == XesScanParameters.FIXED_XES_SCAN_XAS) {
						if (xesParams.getScanFileName().equals(initialFileName)) {
							RichBeanMultiPageEditorPart editor = man.getEditor(man.getSelectedScan().getFile(
									ScanObject.SCANBEANTYPE));
							XesScanParametersUIEditor ed = (XesScanParametersUIEditor) editor.getRichBeanEditor();
							ed.getScanFileName().setValue(this.getPartName());
							editor.setDirty(true);
						}
					}
				}
			} catch (Exception e) {
				// any problems, simply ignore as this will be a problem in the XafsEditorManager which will be picked
				// up elsewhere
			}
		}
	}
}
