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

import java.io.IOException;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.richbeans.components.data.DataWrapper;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;

import com.swtdesigner.SWTResourceManager;

public class Acquire {
	private static final Logger logger = LoggerFactory.getLogger(Acquire.class);
	protected Button acquireBtn;
	protected boolean saveMcaOnAcquire;
	protected Button autoSave;
	protected Label acquireFileLabel;
	private Thread continuousThread;
	private boolean continuousAquire = false;
	protected Button live;
	protected Display display;
	protected ScaleBox acquireTime;
	protected Data plotData;
	protected boolean writeToDisk = LocalProperties.check("gda.detectors.save.single.acquire");
	
	public Acquire(Display display) {
		this.display = display;
	}
	
	public void acquire(double collectionTimeValue) throws Exception {
		
	}
	
	public void plotData(DataWrapper dataWrapper, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex) {
			
	}
		
	
	public void writeToDisk() throws IOException{
		
	}
	
	public void continuousAcquire(final DataWrapper dataWrapper, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex) {
		try {
			continuousThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (continuousAquire) {
						display.asyncExec(new Runnable() {
							@Override
							public void run() {								
								try {
									acquire(acquireTime.getNumericValue());
								} catch (Exception e) {
									logger.error("Error acquiring vortex data", e);
								}
								plotData(dataWrapper, detectorList, detectorElementComposite, currentSelectedElementIndex);
							}
						});
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					logger.error("Continuous acquire problem with detector.", e);
				} catch (Throwable e) {
					logger.error("Continuous acquire problem with detector.", e);
				}
			}
		}, "Detector Live Runner");
		continuousThread.start();
		} 
		catch (Exception e) {
			logger.error("Internal errror process continuous data from detector.", e);
		}
	}
	
	public void addAcquireListener(final DataWrapper dataWrapper, final int currentSelectedElementIndex, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite){
		acquireBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					if (!live.getSelection()){
						acquire(acquireTime.getNumericValue());
						plotData(dataWrapper, detectorList, detectorElementComposite, currentSelectedElementIndex);
						if(writeToDisk)
							writeToDisk();
						else
							acquireFileLabel.setText("										");
					}
					else{
						continuousAquire=!continuousAquire;
						if(continuousAquire)
							continuousAcquire(dataWrapper, detectorList, detectorElementComposite, currentSelectedElementIndex);
					}
				} catch (Exception e1) {
					logger.error("Cannot acquire xmap data", e1);
				}
			}
		});
	}
	
	public void acquireStarted() {
		acquireBtn.setText("Stop");
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/stop.png"));
		autoSave.setEnabled(false);
		acquireFileLabel.setText("										");
		//live.setEnabled(false);
	}

	public void acquireFinished() {
		acquireBtn.setText("Acquire");
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/application_side_expand.png"));
		autoSave.setEnabled(false);
		//live.setEnabled(true);
	}
	
}