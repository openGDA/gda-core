/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.progress.UIJob;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DConsumer;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.OverlayProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.AreaSelectEvent;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

public class EdgePlotSelectionListener implements Overlay1DConsumer {
	private volatile boolean graphUpdateJobInProgress = false;
	private int selectedLine = 0;
	private XasScanParametersUIEditor editor;
	private Overlay1DProvider oProvider;
	private Double beanMin = null;
	private Double beanMax = null;

	public EdgePlotSelectionListener(XasScanParametersUIEditor editor) {
		this.editor = editor;
	}

	@Override
	public void areaSelected(AreaSelectEvent event) {
		if (event.getMode() == 1) {
			// movement while button held
			if (editor.suspendGraphUpdate) {
				refreshGraph(event, false);
			}
		} else if (event.getMode() == 2) {
			// mouse released after a single click or a drag
			if (editor.suspendGraphUpdate) {
				refreshGraph(event, true);
			}
			editor.suspendGraphUpdate = false;
		} else if (event.getMode() == 0) {
			// mouse button pressed down
			if (!editor.suspendGraphUpdate) {
				// not busy so pick up initial event
				doMousePressed(event);
			}
		}
	}

	protected void refreshGraph(final AreaSelectEvent event, boolean queue) {

		if (graphUpdateJobInProgress && !queue) {
			return;
		}

		final double[] lines = editor.edgeOverlay.xValues;
		final double latestTargetValue = event.getX();
		UpdateEditorJob job = null;
		if (newValueAcceptable(lines, latestTargetValue)) {
			lines[selectedLine] = latestTargetValue;
			job = runGraphUpdateJob(lines, latestTargetValue);
		} else if (queue) {
			job = runGraphUpdateJob(lines, lines[selectedLine]);
		}

		if (queue && job != null) {
			// always do a final update after the mouse button lifted
			try {
				job.join();
				editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						editor.linkUI(true);
					}
				});
			} catch (InterruptedException e) {
				// skip the asyncExec line
			}
		}
	}

	protected UpdateEditorJob runGraphUpdateJob(final double[] lines, final double latestTargetValue) {
		UpdateEditorJob job;
		job = new UpdateEditorJob("XAS editor graph", lines, latestTargetValue);
		job.setSystem(true);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				graphUpdateJobInProgress = false;
			}
		});
		graphUpdateJobInProgress = true;
		job.schedule();
		return job;
	}

	private class UpdateEditorJob extends UIJob {

		private double[] lines;
		private double latestTargetValue;

		public UpdateEditorJob(String name, double[] lines, double latestTargetValue) {
			super(name);
			this.lines = lines;
			this.latestTargetValue = latestTargetValue;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			beanMin = editor.getInitialEnergy().getNumericValue();
			beanMax = editor.getFinalEnergy().getNumericValue();
			if (newValueAcceptable(lines, latestTargetValue)) {
				switch (selectedLine) {
				case 1: // a
					double gaf1 = editor.calcGaf1or2(latestTargetValue);
					editor.getGaf1().setValue(gaf1);
					break;
				case 2: // b
					editor.getGaf2().setValue(editor.calcGaf1or2(latestTargetValue));
					if (ExafsActivator.getDefault().getPreferenceStore()
							.getBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK)) {
						editor.getGaf3().setValue(editor.calcGaf3(latestTargetValue));
					}
					break;
				case 3: // c
					editor.getGaf3().setValue(editor.calcGaf3(latestTargetValue));
					break;
				default:
					break;
				}
				editor.edgeOverlay.xValues = this.lines;
				editor.plotter.refresh(true);
			}
			return Status.OK_STATUS;
		}
	}

	private boolean newValueAcceptable(double[] lines, double newValue) {

		if (beanMax == null || beanMin == null)
			return true;
		if (newValue < beanMin || newValue > beanMax)
			return false;

		// compare lines to each other
		switch (selectedLine) {
		case 1: // a < b
			return newValue < lines[2];
		case 2: // a < b < edge
			return lines[1] < newValue && newValue < lines[0];
		case 3: // c > edge
			return newValue > lines[0];
		default:
			return false;
		}
	}

	private void doMousePressed(AreaSelectEvent event) {
		// is a zoom mode selected?
		if (!editor.plotter.isZoomEnabled() && !editor.suspendGraphUpdate) {
			// what are the current lines?
			double[] lines = editor.edgeOverlay.xValues;

			double x = event.getX();

			int[] linesCanMove = new int[] { 1, 2, 3 };
			if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK)) {
				linesCanMove = new int[] { 1, 2 };
			}

			// is the click near the lines?
			for (int line : linesCanMove)
				if (Math.abs(lines[line] - x) < 5) {
					selectedLine = line;
					editor.suspendGraphUpdate = true;
				}
		}
	}

	@Override
	public void registerProvider(OverlayProvider provider) {
		oProvider = (Overlay1DProvider) provider;
	}

	@Override
	public void unregisterProvider() {
		if (oProvider != null) {
			oProvider = null;
		}
	}

	@Override
	public void removePrimitives() {
		// not used
	}
}
