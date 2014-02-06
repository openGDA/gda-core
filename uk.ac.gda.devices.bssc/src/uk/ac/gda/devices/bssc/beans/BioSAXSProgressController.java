/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.beans;

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;

public class BioSAXSProgressController implements IObservable{
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressController.class);
	private BioSAXSISPyB bioSAXSISPyB;
	private IProgressModel bioSAXSProgressModel;
	private String visit;
	private long blSessionId;
	private List<ISAXSDataCollection> saxsDataCollections = null;
	
	ObservableComponent obsComp = new ObservableComponent();
	private boolean stopPolling;

	public BioSAXSProgressController() {
	}

	public void setISpyBAPI(BioSAXSISPyB bioSAXSISPyB) {
		this.bioSAXSISPyB = bioSAXSISPyB;
		try {
			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
			blSessionId = bioSAXSISPyB.getSessionForVisit(/*visit*/"cm4977-1");
		} catch (DeviceException e) {
			logger.error("DeviceEXception getting visit", e);
		} catch (SQLException e) {
			logger.error("SQLEXception getting session id", e);
		}
	}

	public void startPolling() {
		if( bioSAXSProgressModel == null)
			throw new RuntimeException("Model is null");
		stopPolling = false;
		final Job pollingJob = new Job("Polling ISpyB") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// bioSAXSProgressModel.clearItems();
					saxsDataCollections = loadModelFromISPyB();
					Display.getDefault().asyncExec(new Runnable(){
						@Override
						public void run() {
							bioSAXSProgressModel.clearItems();
							bioSAXSProgressModel.addItems(saxsDataCollections);
						}});
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					return Status.OK_STATUS;
				} 
				catch(Exception e){
					logger.error("Error polling ispyb", e);
					return Status.OK_STATUS;
				}
				finally {
					// start job again after specified time has elapsed
					if(!stopPolling)
						schedule(90000);
				}
			}
		};
/*		 job.addJobChangeListener(new JobChangeAdapter() {
		 @Override
		 public void done(IJobChangeEvent event) {
		 if (event.getResult().isOK())
		 System.out.println("Job completed successfully");
		 else
		 System.out.println("Job did not complete successfully");
		 }
		 });
		 start as soon as possible
*/		pollingJob.schedule();
	}

	public List<ISAXSDataCollection> loadModelFromISPyB() {
		List<ISAXSDataCollection> saxsDataCollections = null;
		
		try {
			saxsDataCollections = bioSAXSISPyB.getSAXSDataCollections(blSessionId);
		} catch (SQLException e) {
			logger.error("SQL EXception getting data collections from ISpyB", e);
		}
		
		return saxsDataCollections;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		obsComp.addIObserver(anIObserver);
		if (obsComp.IsBeingObserved()){
			startPolling();
		}
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obsComp.deleteIObserver(anIObserver);
		if (!obsComp.IsBeingObserved()){
			stopPolling = true;
		}
	}


	@Override
	public void deleteIObservers() {
		obsComp.deleteIObservers();
		if (!obsComp.IsBeingObserved()){
			stopPolling = true;
		}
	}

	public void setModel(BioSAXSProgressModel model) {
		bioSAXSProgressModel = model;
		
	}
	
	
	
}
