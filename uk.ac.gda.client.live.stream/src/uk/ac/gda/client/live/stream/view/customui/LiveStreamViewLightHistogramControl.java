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

package uk.ac.gda.client.live.stream.view.customui;

import static org.eclipse.jface.widgets.WidgetFactory.label;

import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Slider;

public class LiveStreamViewLightHistogramControl extends AbstractLiveStreamViewCustomUi {
	private Slider maxHistoSlider;
	private Slider minHistoSlider;
	private ITraceListener traceListener = new TraceListener();
	private boolean lockStatus = true;
	private Button lockButton;

	@Override
	public void createUi(Composite composite) {
		Group groupHistoControl = new Group(composite, SWT.NONE);
		groupHistoControl.setText("Histogram control");
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		groupHistoControl.setLayoutData(gridData);
		groupHistoControl.setLayout(new GridLayout(5, false));

		lockButton = new Button(groupHistoControl, SWT.PUSH);
		lockButton.setText("Switch Lock");
		GridData gridDataLB = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		lockButton.setLayoutData(gridDataLB);
		lockButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> switchLock()));

		label(SWT.NULL).text(" Min: ").create(groupHistoControl);

		minHistoSlider = new Slider(groupHistoControl, SWT.HORIZONTAL);
		GridData gridDataMHS = new GridData(SWT.FILL,SWT.CENTER,true,true);
		minHistoSlider.setLayoutData(gridDataMHS);
		minHistoSlider.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> updateMin()));

		label(SWT.NULL).text(" Max: ").create(groupHistoControl);

		maxHistoSlider = new Slider(groupHistoControl, SWT.HORIZONTAL);
		GridData gridDataHS = new GridData(SWT.FILL,SWT.CENTER,true,true);
		maxHistoSlider.setLayoutData(gridDataHS);
		maxHistoSlider.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> updateMax()));

		getPlottingSystem().addTraceListener(traceListener);

		switchLock();
	}

	private void switchLock() {
		getImageTrace().setRescaleHistogram(!getImageTrace().isRescaleHistogram());
		lockStatus = !getImageTrace().isRescaleHistogram();
		lockButton.setText(lockStatus? "Locked":"Unlocked");
	}

	private void updateMax() {
		IImageTrace image = getImageTrace();
		image.setMax(this.maxHistoSlider.getSelection());
		image.setPaletteData(image.getPaletteData());//Workaround to force image to repaint, see DAWNSCI-5834
	}

	private void updateMin() {
		IImageTrace image = getImageTrace();
		image.setMin(this.minHistoSlider.getSelection());
		image.setPaletteData(image.getPaletteData());//Workaround to force image to repaint, see DAWNSCI-5834
	}


	private final class TraceListener implements ITraceListener {
		@Override
		public void traceWillPlot(TraceWillPlotEvent evt) {
		}

		@Override
		public void tracesAdded(TraceEvent evt) {
		}

		@Override
		public void traceCreated(TraceEvent evt) {
		}

		@Override
		public void traceUpdated(TraceEvent evt) {
			if (lockStatus) return;
			IImageTrace tr = getImageTrace();
			int newMax = (int) tr.getMax().doubleValue();
			int newMin = (int) tr.getMin().doubleValue();

			maxHistoSlider.setMaximum(newMax);
			maxHistoSlider.setSelection(newMax);
			maxHistoSlider.setMinimum(newMin);

			minHistoSlider.setMaximum(newMax);
			minHistoSlider.setMinimum(newMin);
			minHistoSlider.setSelection(newMin);
		}

		@Override
		public void traceAdded(TraceEvent evt) {
			traceUpdated(evt);
		}

		@Override
		public void traceRemoved(TraceEvent evt) {
		}

		@Override
		public void tracesUpdated(TraceEvent evt) {
		}

		@Override
		public void tracesRemoved(TraceEvent evet) {
		}
	}
}
