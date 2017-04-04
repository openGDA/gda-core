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

package uk.ac.gda.arpes.ui.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Device;
import gda.device.MotorStatus;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.gda.arpes.widgets.ProgressBarWithText;
import uk.ac.gda.devices.vgscienta.FrameUpdate;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;
import uk.ac.gda.devices.vgscienta.SweptProgress;

public class AnalyserProgressView extends ViewPart implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserProgressView.class);

	private Text csweep;
	private IVGScientaAnalyserRMI analyser;
	private Device sweepUpdater;
	private Spinner sweepSpinner;
	private int scheduledIterations = -1;
	private int compSweep = -1;
	private ProgressBarWithText progressBar;

	private Color idleColor = new Color(Display.getCurrent(), 150, 150, 150);
	private Color preColor = new Color(Display.getCurrent(), 250, 0, 0);
	private Color postColor = new Color(Display.getCurrent(), 0, 250, 0);
	private boolean running;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_parent = new GridLayout(4, false);
		gl_parent.verticalSpacing = 15;
		gl_parent.marginTop = 5;
		gl_parent.marginRight = 5;
		gl_parent.marginLeft = 5;
		gl_parent.marginBottom = 5;
		parent.setLayout(gl_parent);

		progressBar = new ProgressBarWithText(parent, SWT.FILL);
		progressBar.setSelection(1000);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		progressBar.setText("IDLE");

		Label lblCurrentSweep = new Label(parent, SWT.NONE);
		lblCurrentSweep.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblCurrentSweep.setText("Completed Iterations");

		csweep = new Text(parent, SWT.BORDER | SWT.RIGHT);
		csweep.setEditable(false);
		csweep.setEnabled(false);
		csweep.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		Label lblNewMaximum = new Label(parent, SWT.NONE);
		GridData gd_lblNewMaximum = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_lblNewMaximum.horizontalIndent = 15;
		lblNewMaximum.setLayoutData(gd_lblNewMaximum);
		lblNewMaximum.setText("Scheduled Iterations");

		sweepSpinner = new Spinner(parent, SWT.BORDER);
		sweepSpinner.setIncrement(1);
		sweepSpinner.setMinimum(1);
		sweepSpinner.setMaximum(1000);
		sweepSpinner.setSelection(1);
		sweepSpinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Enter pressed in spinner box try to change scheduled iterations
				changeScheduledIterations(sweepSpinner.getSelection());
			}
		});
		sweepSpinner.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// Reset the value to the actual scheduled iterations
				sweepSpinner.setSelection(scheduledIterations);
			}
		});
		sweepSpinner.setToolTipText("The total number of iterations required. Press enter to confim changes");

		List<IVGScientaAnalyserRMI> analysers = Finder.getInstance().listFindablesOfType(IVGScientaAnalyserRMI.class);
		if (analysers.size() != 1) {
			logger.error("Didn't find 1 analyser");
		}
		else {
			analysers.get(0).addIObserver(this);
		}

		sweepUpdater = (Device) Finder.getInstance().find("sweepupdater");
		if (sweepUpdater != null) {
			sweepUpdater.addIObserver(this);
		}
	}

	@Override
	public void setFocus() {
		sweepSpinner.setFocus();
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof FrameUpdate) {
			final FrameUpdate fu = (FrameUpdate) arg;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					compSweep = fu.cFrame;
					csweep.setText(String.valueOf(compSweep));
					if (fu.mFrame != scheduledIterations) {
						scheduledIterations = fu.mFrame;
						sweepSpinner.setSelection(scheduledIterations);
						logger.debug("Updated scheduled iterations to {}", scheduledIterations);
					}
				}
			});
			return;
		}
		if (arg instanceof MotorStatus) {
			running = MotorStatus.BUSY.equals(arg);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (running) {
						progressBar.setText("RUNNING");
						progressBar.setSelection(progressBar.getMinimum());
						progressBar.setForeground(preColor);
					} else {
						progressBar.setText("IDLE");
						progressBar.setSelection(progressBar.getMaximum());
						progressBar.setMinimum(0);
						progressBar.setBackground(idleColor);
					}
				}
			});
			return;
		}
		if (arg instanceof SweptProgress) {
			logger.trace("updated with " + arg.toString());
			final SweptProgress sp = (SweptProgress) arg;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					progressBar.setText(running ? String.format("Running: %d%% completed", sp.pct) : "IDLE");
					progressBar.setSelection(sp.pct);
					if (sp.current > 0)
						progressBar.setBackground(postColor);
					else
						progressBar.setBackground(idleColor);
				}
			});
			return;
		}
	}

	/**
	 * Allow increasing or reducing the number of scheduled iterations while the scan is running
	 *
	 * @param newScheduledIterations
	 */
	private void changeScheduledIterations(int newScheduledIterations) {
		logger.debug("About to change scheduled iterations to: {}", newScheduledIterations);
		try {
			analyser.changeRequestedIterations(newScheduledIterations);
			logger.info("Changed scheduled iterations to: {}", newScheduledIterations);
		} catch (IllegalArgumentException e) {
			logger.error("Scheduled iteratons could not be set to {}", newScheduledIterations, e);
			// Reset the scheduled iterations GUI to reflect the unsuccessful change
			sweepSpinner.setSelection(scheduledIterations);
		}
	}

	@Override
	public void dispose() {
		// Remove the listeners
		analyser.deleteIObserver(this);
		sweepUpdater.deleteIObserver(this);
		super.dispose();
	}
}
