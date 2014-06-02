/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.hrpd.views;

import gda.observable.IObserver;
import gov.aps.jca.event.MonitorListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;

/**
 * A control for showing progress feedback for a long running operation in EPICS. 
 * This control supports determinate SWT progress bar. Progress values are updated 
 * from EPICS {@link MonitorListener} instances.
 */
public class EpicsProgressIndicator extends Composite implements IObserver {
	private final static int PROGRESS_MAX = 1000; // value to use for max in
	private ProgressBar progressBar;
	private StackLayout layout;
	private double workedSoFar;
	private double totalWork;
	private EpicsIntegerDataListener totalWorkListener;
	private EpicsIntegerDataListener workedSoFarListener;

	/**
	 * Create a ProgressIndicator as a child under the given parent.
	 * 
	 * @param parent
	 *            The widgets parent
	 */
	public EpicsProgressIndicator(Composite parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Create a ProgressIndicator as a child under the given parent.
	 * 
	 * @param parent
	 *            The widgets parent
	 * @param style
	 *            the SWT style constants for progress monitors created by the receiver.
	 */
	public EpicsProgressIndicator(Composite parent, int style) {
		super(parent, SWT.NULL);

		// Enforce horizontal only if vertical isn't set
		if ((style & SWT.VERTICAL) == 0)
			style |= SWT.HORIZONTAL;

		progressBar = new ProgressBar(this, style);
		layout = new StackLayout();
		setLayout(layout);
		if (totalWorkListener!= null) {
			totalWorkListener.addIObserver(this);
		}
		if (workedSoFarListener!=null) {
			workedSoFarListener.addIObserver(this);
		}
	}

	/**
	 * Initialize the progress bar.
	 * 
	 * @param max
	 *            The maximum value.
	 */
	public void beginTask(int max) {
		done();
		this.totalWork = max;
		this.workedSoFar = 0;
		progressBar.setMinimum(0);
		progressBar.setMaximum(PROGRESS_MAX);
		progressBar.setSelection(0);
		layout.topControl = progressBar;
		layout();
	}

	/**
	 * Progress is done.
	 */
	public void done() {
		progressBar.setMinimum(0);
		progressBar.setMaximum(0);
		progressBar.setSelection(0);
		layout.topControl = null;
		layout();
	}
    /**
     * Moves the progress indicator to the end.
     */
    public void sendRemainingWork() {
        worked(totalWork - workedSoFar);
    }
    /**
     * Moves the progress indicator to the given amount of work done so far.
     * @param work the amount of work done so far.
     */
    public void worked(double work) {
        if (work == 0) {
            return;
        }
        workedSoFar = work;
        if (workedSoFar > totalWork) {
            workedSoFar = totalWork;
        }
        if (workedSoFar < 0) {
            workedSoFar = 0;
        }
        int value = (int) (workedSoFar / totalWork * PROGRESS_MAX);
        if (progressBar.getSelection() < value) {
            progressBar.setSelection(value);
        }
    }
    /**
	 * Show the receiver as showing an error.
	 */
	public void showError() {
		progressBar.setState(SWT.ERROR);
	}
	
	/**
	 * Show the receiver as being paused.
	 */
	public void showPaused() {
		progressBar.setState(SWT.PAUSED);
	}

	/**
	 * Reset the progress bar to it's normal style.
	 */
	public void showNormal() {
		progressBar.setState(SWT.NORMAL);
	}

	public EpicsIntegerDataListener getTotalWorkListener() {
		return totalWorkListener;
	}

	public void setTotalWorkListener(EpicsIntegerDataListener totalWorkListener) {
		this.totalWorkListener = totalWorkListener;
	}

	public EpicsIntegerDataListener getWorkedSoFarListener() {
		return workedSoFarListener;
	}

	public void setWorkedSoFarListener(EpicsIntegerDataListener workedSoFarListener) {
		this.workedSoFarListener = workedSoFarListener;
	}

	@Override
	public void update(Object source, Object arg) {
		if (source == totalWorkListener && arg instanceof Integer) {
			this.totalWork=((Integer)arg).doubleValue();
		} else if (source == workedSoFarListener && arg instanceof Integer) {
			this.workedSoFar=((Integer)arg).doubleValue();
			worked(workedSoFar);
		}
		
	}

}
