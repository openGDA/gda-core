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

import org.dawnsci.plotting.jreality.impl.DataSet3DPlot1D;
import org.dawnsci.plotting.jreality.tool.PlotActionComplexEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEventListener;
import org.dawnsci.plotting.jreality.tool.PlotRightClickActionTool;
import org.eclipse.dawnsci.plotting.api.jreality.core.ScaleType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.ViewSettingsDialog;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;

/**
 * This is a 1D plotter that is used in side plots and provides context menu options to use log scales
 * and support zooming
 */
@Deprecated
public class SidePlotter1D extends DataSetPlotter implements PlotActionEventListener {
	private Composite parent;
	Display display;
	private String title;

	class ContextDialog extends ViewSettingsDialog {
		boolean logScale = getPreferenceSidePlotterLogY();

		public ContextDialog(Shell parent) {
			super(parent);
			setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
			setBlockOnOpen(false);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(title + " options");
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CLIENT_ID, "Toggle y logscale", false);
			createButton(parent, IDialogConstants.CLIENT_ID+1, "Unzoom", false);
			createButton(parent, IDialogConstants.CLIENT_ID+2, "Reset", false);
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			switch (buttonId) {
			case IDialogConstants.CLIENT_ID:
				logScale = !logScale;
				if (logScale)
					setYAxisScaling(ScaleType.LOG10);
				else
					setYAxisScaling(ScaleType.LINEAR);
				break;
			case IDialogConstants.CLIENT_ID+1:
				undoZoom();
				break;
			case IDialogConstants.CLIENT_ID+2:
				resetZoom();
				break;
			default:
				super.buttonPressed(buttonId);
			}
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					refresh(false);
				}
			});
		}
	}

	private ContextDialog popup;

	public SidePlotter1D(Composite parent, String title) {
		super(PlottingMode.ONED, parent, false);

		this.title = title;
		this.parent = parent;
		display = parent.getDisplay();

		((DataSet3DPlot1D) plotter).addPlotActionEventListener(this);
		setPlotRightClickActionEnabled(true);
		setZoomMode(true);
		setZoomEnabled(true);
		if (getPreferenceSidePlotterLogY())
			setYAxisScaling(ScaleType.LOG10);
		popup = new ContextDialog(parent.getShell());
	}

	@Override
	public void plotActionPerformed(final PlotActionEvent event) {
		if (event instanceof PlotActionComplexEvent && event.getSource() instanceof PlotRightClickActionTool) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					popup.open();
				}
			});
		}
	}

	private boolean getPreferenceSidePlotterLogY() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y) ? 
				preferenceStore.getDefaultBoolean(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y)
				: preferenceStore.getBoolean(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y);
	}

	@SuppressWarnings("unused")
	private void setPreferenceSidePlotterLogY(boolean useLogY) {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		if (!preferenceStore.isDefault(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y)) 
			preferenceStore.setValue(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y, useLogY);
	}

}
