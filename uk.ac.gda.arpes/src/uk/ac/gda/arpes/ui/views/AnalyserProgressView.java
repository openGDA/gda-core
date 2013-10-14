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

import gda.device.Device;
import gda.device.MotorStatus;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.arpes.widgets.ProgressBarWithText;
import uk.ac.gda.devices.vgscienta.FlexibleFrameDetector;
import uk.ac.gda.devices.vgscienta.FrameUpdate;
import uk.ac.gda.devices.vgscienta.SweptProgress;

public class AnalyserProgressView extends ViewPart implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserProgressView.class);

	private Text csweep;
	private FlexibleFrameDetector analyser;
	private Spinner sweepSpinner;
	private int oldMax = -1, compSweep = -1;
	private ProgressBarWithText progressBar;

	private Color idleColor = new Color(Display.getCurrent(), 150, 150, 150);
	private Color preColor = new Color(Display.getCurrent(), 250, 0, 0);
	private Color postColor = new Color(Display.getCurrent(), 0, 250, 0);
	private boolean running;

	public AnalyserProgressView() {
	}

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
		sweepSpinner.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				int newMax = sweepSpinner.getSelection();
				if (newMax < compSweep)
					return;
				oldMax = newMax;
				analyser.setMaximumFrame(oldMax);
			}
		});
		
		Button btnStop = new Button(parent, SWT.NONE);
		btnStop.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		btnStop.setText("Abort Scan");
		btnStop.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().haltCurrentScan();
				analyser.setMaximumFrame(analyser.getCurrentFrame());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Button btnCompleteAndStop = new Button(parent, SWT.NONE);
		btnCompleteAndStop.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		btnCompleteAndStop.setText("Finish after current Iteration");
		btnCompleteAndStop.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				analyser.setMaximumFrame(analyser.getCurrentFrame());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		analyser = (FlexibleFrameDetector) Finder.getInstance().find("analyser");
		if (analyser != null) {
			analyser.addIObserver(this);
		}
		
		Device su = (Device) Finder.getInstance().find("sweepupdater");
		if (su != null) {
			su.addIObserver(this);
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(Object source, Object arg) {
		if (csweep.isDisposed()) {
			try {
				((Device) source).deleteIObserver(this);
			} catch (Exception e) {}
			return;
		}
			
		if (arg instanceof FrameUpdate) {
			final FrameUpdate fu = (FrameUpdate) arg;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					compSweep = fu.cFrame;
					csweep.setText(String.valueOf(compSweep));
					if (fu.mFrame != oldMax) {
						sweepSpinner.setSelection(fu.mFrame);
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
			logger.debug("updated with "+arg.toString());
			final SweptProgress sp = (SweptProgress) arg;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					progressBar.setText(getProgressBarText(sp.current, sp.max));
					if (sp.current < progressBar.getMinimum())
						progressBar.setMinimum(sp.current);
					progressBar.setMaximum(sp.max);
					progressBar.setSelection(sp.current);
					if (sp.current > 0)
						progressBar.setBackground(postColor);
					else
						progressBar.setBackground(idleColor);
				}
			});
			return;
		}
	}
	
	private String getProgressBarText(int cur, int max) {
		if (running) {
			String form = "%d";
			if (max > 999)
				form = "%04d";
			else if (max > 99)
				form = "%03d";
			else if (max > 9)
				form = "%02d";
			return String.format("Running  "+form+" / %d", cur, max);
		}
		return "IDLE";
	}
}