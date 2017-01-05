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

package uk.ac.gda.epics.dxp.client.views;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EDXDAlignmentPlotView extends ViewPart {

	public static final String ID = "uk.ac.gda.epics.dxp.client.alignment.plot";
	private Composite rootComposite;
	private IPlottingSystem plottingSystem;

	private final static Logger logger = LoggerFactory.getLogger(EDXDAlignmentPlotView.class);

	@Override
	public void createPartControl(Composite parent) {
		rootComposite = new Composite(parent, SWT.None);
		rootComposite.setLayout(new FillLayout());
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Unable to create plotting system", e);
		}
		plottingSystem.createPlotPart(rootComposite, "EDXDAlignment", null, PlotType.XY_STACKED, null);
	}

	@Override
	public void setFocus() {
		rootComposite.setFocus();
	}

}
