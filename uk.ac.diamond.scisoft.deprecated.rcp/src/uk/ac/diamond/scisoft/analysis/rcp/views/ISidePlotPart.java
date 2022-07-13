/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rcp.views;

import org.eclipse.january.dataset.IMetadataProvider;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
@Deprecated
public interface ISidePlotPart extends IMetadataProvider, IWorkbenchPart{

	/**
	 * Different side plot choices which may be made by the user.
	 */
	public enum SidePlotPreference {
		LINE,
		PROFILE,
		DIFFRACTION_3D,
		IMAGE
	}
	public SidePlotPreference getSidePlotPreference();
	
	/**
	 * No longer implemented: returns null
	 * Returns the plotter used on the part
	 * @return DataSetPlotter
	 */
	public DataSetPlotter getMainPlotter();
}
