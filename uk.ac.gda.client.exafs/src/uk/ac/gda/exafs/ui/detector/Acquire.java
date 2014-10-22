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

package uk.ac.gda.exafs.ui.detector;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;

public class Acquire {
	private static final Logger logger = LoggerFactory.getLogger(Acquire.class);
	protected Button acquireBtn;
	protected boolean saveMcaOnAcquire;
	protected Button autoSave;
	protected Label acquireFileLabel;
	private boolean continuousAquire = false;
	protected Button live;
	protected Display display;
	protected ScaleBox acquireTime;
	protected boolean writeToDisk = LocalProperties.check("gda.detectors.save.single.acquire");
	private Job continiousAcquireJob;
	private double acquireTimeValue;
	
	public Acquire(Display display) {
		this.display = display;
	}
	
	public void acquire(double collectionTime) throws DeviceException, InterruptedException{
		
	}
	
	public void plotData(final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex) throws DeviceException, InterruptedException {
			
	}
	
	public void writeToDisk() throws IOException{
		
	}
	
	public void updateStats(final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex){
		
	}
	
	public void acquireAndPlotAndUpdateStats(double acquireTime, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex){
		try {
			acquire(acquireTime);
		} catch (DeviceException e) {
			logger.error("Cannot acquire", e);
		} catch (InterruptedException e) {
			logger.error("Cannot acquire", e);
		}
		try {
			plotData(detectorList, detectorElementComposite, currentSelectedElementIndex);
		} catch (DeviceException e) {
			logger.error("Cannot plot", e);
		} catch (InterruptedException e) {
			logger.error("Cannot plot", e);
		}
		updateStats(detectorList, detectorElementComposite, currentSelectedElementIndex);
	}
	
	public void createContinuousAcquireJob(final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex) {
//		Display.getDefault().asyncExec(new Runnable() {
//			@Override
//			public void run() {
//				acquireTimeValue = acquireTime.getNumericValue();
//			}
//		});
		continiousAcquireJob = new Job("updateScanStatus") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				while(continuousAquire){
					acquireAndPlotAndUpdateStats(acquireTimeValue, detectorList, detectorElementComposite, currentSelectedElementIndex);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
				return Status.OK_STATUS;
			}
		};	
	}
	
	public void addAcquireListener(final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite){
		final int currentSelectedElementIndex = detectorList.getSelectedIndex();
		acquireBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				
				acquireTimeValue = acquireTime.getNumericValue();
				
				createContinuousAcquireJob(detectorList, detectorElementComposite, currentSelectedElementIndex);
				try {
					if (live==null || !live.getSelection()){
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {	
								acquireBtn.setText("Stop");
							}
						});
						acquireAndPlotAndUpdateStats(acquireTimeValue, detectorList, detectorElementComposite, currentSelectedElementIndex);
						if(writeToDisk)
							writeToDisk();
						else
							acquireFileLabel.setText("										");
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								acquireBtn.setText("Acquire");
							}
						});
					}
					else{
						continuousAquire=!continuousAquire;
						if(continuousAquire){
							continiousAcquireJob.schedule();
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {	
									acquireBtn.setText("Stop");
								}
							});
						}
						else{
							continiousAcquireJob.cancel();
							continiousAcquireJob.schedule();
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									acquireBtn.setText("Acquire");
								}
							});
						}
					}
				} catch (Exception e1) {
					logger.error("Cannot acquire xmap data", e1);
				}
			}
		});
	}
	
}