/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.views.nexus;

import org.eclipse.ui.IEditorPart;

import uk.ac.gda.common.rcp.util.EclipseUtils;

public class DataSetPlotUtils {

	/**
	 * Tries to find an active DataSetPlotView 
	 * @return DataSetPlotView
	 */
	public static DataSetPlotView getActiveView() {
		
		final DataSetComparisionDialog dialog = DataSetComparisionDialog.getActiveDialog();
		DataSetPlotView sets = dialog!=null ? dialog.getDataSetPlotView() : null;
		
		if (sets==null) {
		    sets = (DataSetPlotView)EclipseUtils.getActivePage().findView(DataSetPlotView.ID);
		}
		if (sets == null) {
			IEditorPart editor = EclipseUtils.getActivePage().getActiveEditor();
			if (editor!=null) {
				if (editor instanceof IDataSetPlotViewProvider) {
					sets = ((IDataSetPlotViewProvider)editor).getDataSetPlotView();
				}
				
			}
		}
		
		return sets; // Might still be null
	}
}
