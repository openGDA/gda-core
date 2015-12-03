/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.mythen.visualisation.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.dawnsci.plotting.api.jreality.impl.SurfPlotStyles;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.histogram.functions.AbstractMapFunction;
import uk.ac.diamond.scisoft.analysis.histogram.functions.ConstMapFunction;
import uk.ac.diamond.scisoft.analysis.histogram.functions.SquareRootMapFunction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;

public class MythenDataView extends ViewPart {

	public static final String ID = MythenDataView.class.getName();

	public static MythenDataView getInstance() {
		return (MythenDataView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ID);
	}

	private static AbstractMapFunction ZERO_MAP_FUNCTION = new ConstMapFunction(0, "0");

	private static AbstractMapFunction ONE_MAP_FUNCTION = new ConstMapFunction(1, "1");

	private Composite parent;

	private DataSetPlotter plotter;

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new FormLayout());
	}

	public void plot1D(List<IDataset> data) {
		replacePlotter(PlottingMode.ONED);

		try {
			plotter.replaceAllPlots(data);
			refreshPlotter();
		} catch (PlotException e1) {
			e1.printStackTrace();
		}
	}

	public void plot2D(DoubleDataset data) {
		replacePlotter(PlottingMode.TWOD);

		try {
			plotter.replaceAllPlots(Collections.singleton(data));
			refreshPlotter();

			plotter.applyColourCast(
					new SquareRootMapFunction(),
					new SquareRootMapFunction(),
					ZERO_MAP_FUNCTION,
					ONE_MAP_FUNCTION,
					false, false, false, false,
					0, 600);

		} catch (PlotException e1) {
			e1.printStackTrace();
		}
	}

	public void plot3D(IDataset data, @SuppressWarnings("unused") int yScaleFactor) {
		replacePlotter(PlottingMode.SURF2D);
//		((DataSet3DPlot3D) plotter.getPlotter()).setYScaleFactor(yScaleFactor);

		try {
			plotter.replaceAllPlots(Collections.singleton(data));
			plotter.setPlot2DSurfStyle(SurfPlotStyles.FILLED); // or WIREFRAME
			refreshPlotter();

			plotter.applyColourCast(
				new SquareRootMapFunction(),
				ZERO_MAP_FUNCTION,
				ZERO_MAP_FUNCTION,
				ONE_MAP_FUNCTION,
				false, false, false, false,
				0, 200);

		} catch (PlotException e1) {
			e1.printStackTrace();
		}
	}

	private void replacePlotter(PlottingMode plotMode) {
		removeExistingPlotter();
		addPlotter(plotMode);
	}

	private void removeExistingPlotter() {
		if (plotter != null) {
			plotter.cleanUp();
			plotter = null;
		}
	}

	private void addPlotter(PlottingMode plotMode) {
		plotter = new DataSetPlotter(plotMode, parent);
		FormData formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(0, 5);
		formData.bottom = new FormAttachment(100, -5);
		formData.right = new FormAttachment(100, -5);
		plotter.getComposite().setLayoutData(formData);
	}

	private void refreshPlotter() {
		parent.layout();
	}

	@Override
	public void dispose() {
		removeExistingPlotter();
	}

	@Override
	public void setFocus() {
		// do nothing
	}

}
