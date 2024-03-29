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

import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Device;
import gda.device.MotorStatus;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import uk.ac.diamond.daq.pes.api.IElectronAnalyser;
import uk.ac.diamond.daq.pes.api.SweptProgress;
import uk.ac.gda.arpes.widgets.ProgressBarWithText;

public class AnalyserProgressView extends ViewPart implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserProgressView.class);

	private Text completedIterTxt;
	private Text scheduledIterTxt;
	private IElectronAnalyser analyser;
	private Device sweepUpdater;
	private int scheduledIterations;
	private int completedIterations;
	private ProgressBarWithText progressBar;

	private Color idleColor = new Color(Display.getCurrent(), 150, 150, 150);
	private Color preColor = new Color(Display.getCurrent(), 250, 0, 0);
	private Color postColor = new Color(Display.getCurrent(), 0, 250, 0);
	private boolean running;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentGridLayout = new GridLayout(4, false);
		parentGridLayout.verticalSpacing = 15;
		parentGridLayout.marginTop = 5;
		parentGridLayout.marginRight = 5;
		parentGridLayout.marginLeft = 5;
		parentGridLayout.marginBottom = 5;
		parent.setLayout(parentGridLayout);

		progressBar = new ProgressBarWithText(parent, SWT.FILL);
		progressBar.setSelection(1000);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		progressBar.setText("IDLE");

		Label lblCurrentSweep = new Label(parent, SWT.NONE);
		lblCurrentSweep.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblCurrentSweep.setText("Completed Iterations:");

		completedIterTxt = new Text(parent, SWT.BORDER | SWT.RIGHT);
		completedIterTxt.setEditable(false);
		completedIterTxt.setEnabled(false);
		completedIterTxt.setToolTipText("The number of iterations completed");
		completedIterTxt.setLayoutData(new GridData(40, SWT.DEFAULT));

		Label lblNewMaximum = new Label(parent, SWT.NONE);
		GridData lblNewMaximumGD = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		lblNewMaximumGD.horizontalIndent = 15;
		lblNewMaximum.setLayoutData(lblNewMaximumGD);
		lblNewMaximum.setText("Scheduled Iterations:");

		scheduledIterTxt = new Text(parent, SWT.BORDER | SWT.RIGHT);
		scheduledIterTxt.setEditable(false);
		scheduledIterTxt.setEnabled(false);
		scheduledIterTxt.setToolTipText("The number of iterations requested");
		scheduledIterTxt.setLayoutData(new GridData(40, SWT.DEFAULT));

		Optional<IElectronAnalyser> analyserRmiProxy = Finder.listLocalFindablesOfType(IElectronAnalyser.class)
				.stream()
				.findFirst();

		if (analyserRmiProxy.isPresent()) {
			this.analyser = analyserRmiProxy.get();
			analyser.addIObserver(this);
		} else {
			logger.error("Could not find analyser over RMI");
		}

		Button btnStopNextIter = new Button(parent, SWT.NONE);
		btnStopNextIter.setText("Stop after current iteration!");
		btnStopNextIter.setToolTipText("Stop acquisiton after current iteration finishes and save file");
		btnStopNextIter.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false, 2, 1));
		btnStopNextIter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isBatonHeld()) { // Baton is held, stop the analyser after the next iteration
					logger.debug("About to stop after next iteration");
					try {
						analyser.stopAfterCurrentIteration();
					} catch (Exception e1) {
						logger.error("Could not stop after next iteration", e1);
					}
				}
			}
		});

		Button btnStopExperiment = new Button(parent, SWT.NONE);
		btnStopExperiment.setText("Stop now!");
		btnStopExperiment.setToolTipText("Stop acquisition now and save data from last completed iteration");
		btnStopExperiment.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false, 2, 1));
		btnStopExperiment.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isBatonHeld()) { // Baton is held, stop the analyser after the next iteration
					logger.debug("About to stop after next iteration");
					try {
						analyser.zeroSupplies();
					} catch (Exception e1) {
						logger.error("Could not stop analyser!", e1);
					}
				}
			}
		});

		sweepUpdater = Finder.find("sweepupdater");
		if (sweepUpdater != null) {
			sweepUpdater.addIObserver(this);
		}
	}

	@Override
	public void setFocus() {
		completedIterTxt.setFocus();
	}

	boolean isBatonHeld() {
		boolean batonHeld = JythonServerFacade.getInstance().amIBatonHolder();
		if (!batonHeld) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Baton not held",
					"You do not hold the baton, please take the baton using the baton manager.");
		}
		return batonHeld;
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof MotorStatus) {
			running = MotorStatus.BUSY.equals(arg);
			if (running) {
				try {
					scheduledIterations = analyser.getIterations();
				} catch (Exception e) {
					logger.error("Could not get iterations from analyser.", e);
				}

				Display.getDefault().asyncExec(() -> {
					progressBar.setText("RUNNING");
					progressBar.setSelection(progressBar.getMinimum());
					progressBar.setForeground(preColor);
					scheduledIterTxt.setText(String.valueOf(scheduledIterations));
				});
			}
			else {
				try {
					completedIterations = analyser.getCompletedIterations();
				} catch (Exception e) {
					logger.error("Could not get completed iterations from analyser.", e);
				}
				Display.getDefault().asyncExec(() -> {
					progressBar.setText("IDLE");
					progressBar.setSelection(progressBar.getMaximum());
					progressBar.setMinimum(0);
					progressBar.setBackground(idleColor);
					completedIterTxt.setText(String.valueOf(completedIterations));
				});
			}
		}
		if (arg instanceof SweptProgress) {
			logger.trace("updated with {}", arg);
			final SweptProgress sp = (SweptProgress) arg;
			Display.getDefault().asyncExec(() -> {
				progressBar.setText(running ? String.format("Running: %d%% completed", sp.getPercent()) : "IDLE");
				progressBar.setSelection(sp.getPercent());
				completedIterTxt.setText(String.valueOf(sp.getCurrentIter()));
				if (sp.getCurrentPoint() > 0)
					progressBar.setBackground(postColor);
				else
					progressBar.setBackground(idleColor);
			});
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
