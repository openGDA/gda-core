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

package uk.ac.gda.exafs.ui.detector.xspress;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.exafs.ui.detector.DetectorROIComposite;
import uk.ac.gda.exafs.ui.detector.IDetectorROICompositeFactory;
import uk.ac.gda.exafs.ui.detector.XspressROIComposite;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.selector.GridListEditor.GRID_ORDER;

public enum XspressParametersUIHelper {
	INSTANCE;
	
	public IDetectorROICompositeFactory getDetectorROICompositeFactory() {
		IDetectorROICompositeFactory factory = new IDetectorROICompositeFactory() {
			@Override
			public DetectorROIComposite createDetectorROIComposite(Composite parent, int style) {
				return new XspressROIComposite(parent, style);
			}
		};
		return factory;
	}

	public void setDetectorListGridOrder(GridListEditor gridListEditor) {
		final Map<Integer, Integer> pixelMap = getIndexToElectricalChannelMap();
		GRID_ORDER gridOrder = pixelMap != null ? GRID_ORDER.CUSTOM_MAP : GRID_ORDER.TOP_TO_BOTTOM_RIGHT_TO_LEFT;
		gridListEditor.setGridOrder(gridOrder);
		gridListEditor.setGridMap(pixelMap);
	}
	
	/**
	 * Used where the pixels are mapped differently to the default
	 * @return map of pixels read from extension point or null.
	 */
	public Map<Integer, Integer> getIndexToElectricalChannelMap() {
		
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.gda.exafs.xspress.pixel.map");
		if (config==null||config.length<1) return null; 
			
		final Map<Integer,Integer> ret = new HashMap<Integer, Integer>(64);
		for (IConfigurationElement e : config) {
		    final String pixel = e.getAttribute("pixel");
		    final String[] ps  = pixel.split("=");
		    ret.put(Integer.parseInt(ps[0].trim()), Integer.parseInt(ps[1].trim()));
		}
		return ret;
	}

	public int getMinimumRegions() {
		return 0;
	}

	public int getMaximumRegions() {
		return 8;
	}

}
