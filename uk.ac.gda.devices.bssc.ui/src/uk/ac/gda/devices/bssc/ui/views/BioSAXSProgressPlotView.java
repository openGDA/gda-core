/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.ui.views;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BioSAXSProgressPlotView extends ViewPart {
	public static String ID = "uk.ac.gda.devices.bssc.views.BioSAXSProgressPlotView";
	private IPlottingSystem plotting;

	private Logger logger = LoggerFactory.getLogger(BioSAXSProgressPlotView.class);

	public BioSAXSProgressPlotView() {
		try {
			this.plotting = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Cannot create a plotting system!", e);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		plotting.createPlotPart(parent, "My Plot Name", getViewSite().getActionBars(), PlotType.IMAGE, this);
	}

	@Override
	public void setFocus() {
		plotting.setFocus();
	}

	@Override
	public Object getAdapter(final Class clazz) {

		if (IPlottingSystem.class == clazz)
			return plotting;

		if (IToolPageSystem.class == clazz)
			return plotting;

		return super.getAdapter(clazz);
	}
}