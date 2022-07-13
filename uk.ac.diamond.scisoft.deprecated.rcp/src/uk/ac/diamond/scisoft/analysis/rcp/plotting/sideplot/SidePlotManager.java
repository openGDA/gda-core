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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;

/**
 * SidePlotManager is implemented as a singleton. 
 * It is responsible for managing the side plot extension point
 */
@Deprecated
public class SidePlotManager {

	private static final Logger logger = LoggerFactory.getLogger(SidePlotManager.class);
	private static final String SIDEPLOT_EXTENSION_ID = "uk.ac.diamond.scisoft.analysis.rcp.sidePlot"; //$NON-NLS-1$
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$
	private static final String ATT_MODE= "plotMode"; //$NON-NLS-1$

	private static SidePlotManager SIDEPLOT_MANAGER;
	private Map<String,Map<GuiPlotMode, List<ISidePlot>>> sidePlotsMap;

	private SidePlotManager(){
		sidePlotsMap = new HashMap<String,Map<GuiPlotMode,List<ISidePlot>>>();
	}

	/**
	 * create the singleton instance, check for null condition of
	 * SIDEPLOT_MANAGER. If SIDEPLOT_MANAGER is null create the SidePlotManager.
	 * instance assign it to SIDEPLOT_MANAGER. There is no need to have
	 * synchronized here.
	 * 
	 * @return SIDEPLOT_MANAGER, instance of TemplateConfigAssocManager.

	 */
	public static SidePlotManager getDefault(){
		if (SIDEPLOT_MANAGER == null){
			SIDEPLOT_MANAGER = new SidePlotManager();
		}
		return SIDEPLOT_MANAGER;
	}

	/**
	 * Discover and create all the side plot classes
	 * based on the side plot extension point
	 */
	private void initializeSidePlots(String viewName){
		
		final Map<GuiPlotMode,List<ISidePlot>> sidePlotsMapForView = new HashMap<GuiPlotMode, List<ISidePlot>>();
		final IExtension[] extensions = getExtensions(SIDEPLOT_EXTENSION_ID);
		
		for(int i=0; i<extensions.length; i++) {
			
			IExtension extension = extensions[i];
			IConfigurationElement[] configElements = extension.getConfigurationElements();	
			
			for(int j=0; j<configElements.length; j++) {
				IConfigurationElement config = configElements[j];
				try {
					ISidePlot side1 = (ISidePlot) config.createExecutableExtension(ATT_CLASS);
					String mode = config.getAttribute(ATT_MODE);
					GuiPlotMode pMode = mode == null ? GuiPlotMode.TWOD : getMode(mode);
					
					if (!sidePlotsMapForView.containsKey(pMode))
						sidePlotsMapForView.put(pMode, new ArrayList<ISidePlot>());
					sidePlotsMapForView.get(pMode).add(side1);
				} catch (CoreException e) {
					logger.error("Unable to instantiate side plot" + e.getMessage());
				}
			}
		}
		sidePlotsMap.put(viewName, sidePlotsMapForView);
	}

	/**
	 * Discover extensions for the given extensionPointId
	 * 
	 * @param extensionPointId the extension point id
	 * @return an array of discovered extensions
	 */
	private IExtension[] getExtensions(String extensionPointId) {
		IExtensionRegistry registry = Platform. getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(extensionPointId);
		IExtension[] extensions = point.getExtensions();
		return extensions;
	}

	/**
	 * Return list of discovered side plots
	 * <p>
	 * <b>Do not modify the returned list, ever!</b>
	 * @param mode gui plot mode
	 * @return discovered side plots
	 */
	public List<ISidePlot> getSidePlots(GuiPlotMode mode, String viewName){
		if (sidePlotsMap.get(viewName) == null)
			initializeSidePlots(viewName);
		// Ensure that no-one changes the list once it is read from the extensions.
		return sidePlotsMap.get(viewName).get(mode);
	}	

	private static Map<String, GuiPlotMode> plotModeMap = null;

	/**
	 * 
	 * @param mode
	 * @return Gui plot mode
	 */
	public static GuiPlotMode getMode(String mode) {
		if (plotModeMap == null) {
			plotModeMap = new HashMap<String, GuiPlotMode>();
			plotModeMap.put("1D", GuiPlotMode.ONED);
			plotModeMap.put("1D_stack", GuiPlotMode.ONED_THREED);
			plotModeMap.put("2D_image", GuiPlotMode.TWOD);
			plotModeMap.put("2D_surface", GuiPlotMode.SURF2D);
			plotModeMap.put("2D_multi", GuiPlotMode.MULTI2D);
			plotModeMap.put("3D_volume", GuiPlotMode.VOLUME);
		}
		return plotModeMap.get(mode);
	}
}
