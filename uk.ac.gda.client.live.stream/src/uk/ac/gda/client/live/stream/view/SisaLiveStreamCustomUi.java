/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.view.customui.AbstractLiveStreamViewCustomUi;

public class SisaLiveStreamCustomUi extends AbstractLiveStreamViewCustomUi {

	private final Logger logger = LoggerFactory.getLogger(SisaLiveStreamCustomUi.class);

	final Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	int[] streamShape = null;

	@Override
	public void createUi(Composite composite) {
		addCentreLineButton();
	}

	private void addCentreLineButton() {

		getActionBars().getToolBarManager().add(new Action() {
			@Override
			public void run() {
				try {
					streamShape = getLiveStreamConnection().getStream().getDataset().getShape();
				} catch (Exception e) {
					logger.warn("Could not get Live Stream Connection: ", e);
				}
				IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
				Collection<ILineTrace> traces = plottingSystem.getTracesByClass(ILineTrace.class);
				if (!traces.isEmpty()) {
					traces.forEach(plottingSystem::removeTrace);
				}
				else {
					// Draw horizontal line across centre of plotting system
					List<Integer> xList = Arrays.asList(0, streamShape[1]);
					List<Integer> yList = Arrays.asList(streamShape[0]/2, streamShape[0]/2);
					IDataset xData = DatasetFactory.createFromList(xList);
					IDataset yData = DatasetFactory.createFromList(yList);
					ILineTrace centreLine = plottingSystem.createLineTrace("Centre Line");
					centreLine.setData(xData, yData);
					centreLine.setTraceColor(red);
					centreLine.setLineWidth(3);
					plottingSystem.addTrace(centreLine);
					plottingSystem.setShowIntensity(true);
					plottingSystem.setShowLegend(false);
				}
			}

			@Override
			public String getText() {
				return "Add/remove centre line";
			}
		});

		getActionBars().updateActionBars();
	}

}
