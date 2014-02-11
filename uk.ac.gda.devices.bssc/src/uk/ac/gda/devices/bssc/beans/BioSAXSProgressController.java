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
import gda.factory.FactoryException;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;
import uk.ac.gda.devices.bssc.ispyb.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;
import uk.ac.gda.devices.bssc.ispyb.SimpleUDPServerScannable;

public class BioSAXSProgressController implements IObservable {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressController.class);
	private BioSAXSISPyB bioSAXSISPyB;
	private IObservableList bioSAXSProgressModel;
	private String visit;
	private long blSessionId;
	private List<ISAXSDataCollection> saxsDataCollections;
	private List<ISAXSProgress> progressList;
	private SimpleUDPServerScannable simpleUDPServer;
	ObservableComponent obsComp = new ObservableComponent();
	private boolean stopPolling;

	public BioSAXSProgressController() throws FactoryException {
		simpleUDPServer = new SimpleUDPServerScannable();
		simpleUDPServer.setName("simpleUDPServer");
		simpleUDPServer.setRunning(true);
		simpleUDPServer.setPort(9877);
		simpleUDPServer.setPrefix("simpleUDPServer");
		simpleUDPServer.configure();
		System.out.println("SimpleUDPReceiver added as an observer");
		simpleUDPServer.addIObserver(new SimpleUDPReceiver(this));
	}

	public void setISpyBAPI(BioSAXSISPyB bioSAXSISPyB) throws FactoryException {
		this.bioSAXSISPyB = bioSAXSISPyB;
		try {
			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
			blSessionId = bioSAXSISPyB.getSessionForVisit(/* visit */"cm4977-1");
			progressList = new ArrayList<ISAXSProgress>();
		} catch (DeviceException e) {
			logger.error("DeviceEXception getting visit", e);
		} catch (SQLException e) {
			logger.error("SQLEXception getting session id", e);
		}
	}

	public void startPolling() {
		if (bioSAXSProgressModel == null)
			throw new RuntimeException("Model is null");
		stopPolling = false;
		final Job pollingJob = new Job("Polling ISpyB") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					saxsDataCollections = getDataCollectionsFromISPyB();
					loadModel(saxsDataCollections);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							bioSAXSProgressModel.clear();
							bioSAXSProgressModel.addAll(progressList);
						}
					});
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					return Status.OK_STATUS;
				} catch (Exception e) {
					logger.error("Error polling ispyb", e);
					return Status.CANCEL_STATUS;
				} finally {
					// start job again after specified time has elapsed
					if (!stopPolling)
						schedule(90000);
				}
			}
		};
		/*
		 * job.addJobChangeListener(new JobChangeAdapter() {
		 * @Override public void done(IJobChangeEvent event) { if (event.getResult().isOK())
		 * System.out.println("Job completed successfully"); else
		 * System.out.println("Job did not complete successfully"); } }); start as soon as possible
		 */pollingJob.schedule();
	}

	public List<ISAXSDataCollection> getDataCollectionsFromISPyB() {
		try {
			saxsDataCollections = bioSAXSISPyB.getSAXSDataCollections(blSessionId);
		} catch (SQLException e) {
			logger.error("SQL EXception getting data collections from ISpyB", e);
		}
		return saxsDataCollections;
	}

	public void getStatusInfoFromISpyB(long dataCollectionId) {
		try {
			ISpyBStatusInfo collectionStatusInfo = bioSAXSISPyB.getDataCollectionStatus(dataCollectionId);
			ISpyBStatusInfo reductionStatusInfo = bioSAXSISPyB.getDataReductionStatus(dataCollectionId);
			ISpyBStatusInfo analysisStatusInfo = bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId);

			updateModel(dataCollectionId, collectionStatusInfo, reductionStatusInfo, analysisStatusInfo);
		} catch (SQLException e) {
			logger.error("SQLException", e);
		}
	}
	
	public List<ISAXSProgress> loadModel(List<ISAXSDataCollection> saxsDataCollections) {
		for (ISAXSDataCollection saxsDataCollection : saxsDataCollections) {
			ISAXSProgress progress = new BioSAXSProgress(saxsDataCollection.getId(),
					saxsDataCollection.getSampleName(), saxsDataCollection.getCollectionStatus(),
					saxsDataCollection.getReductionStatus(), saxsDataCollection.getAnalysisStatus());
			progressList.add(progress);
		}

		return progressList;
	}

	public void updateModel(long dataCollectionId, ISpyBStatusInfo collectionStatusInfo,
			ISpyBStatusInfo reductionStatusInfo, ISpyBStatusInfo analysisStatusInfo) {
		int dataCollectionIdIntValue = ((Long) dataCollectionId).intValue();
		ISAXSProgress progressItem = progressList.get(dataCollectionIdIntValue);
		progressItem.setCollectionProgress(collectionStatusInfo);
		progressItem.setReductionProgress(reductionStatusInfo);
		progressItem.setAnalysisProgress(analysisStatusInfo);
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		obsComp.addIObserver(anIObserver);
		if (obsComp.IsBeingObserved()) {
			 startPolling();
		}
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obsComp.deleteIObserver(anIObserver);
		if (!obsComp.IsBeingObserved()) {
			stopPolling = true;
		}
	}

	@Override
	public void deleteIObservers() {
		obsComp.deleteIObservers();
		if (!obsComp.IsBeingObserved()) {
			stopPolling = true;
		}
	}

	public void setModel(IObservableList model) {
		bioSAXSProgressModel = model;

	}

	public void disconnectFromISpyB() {
		try {
			bioSAXSISPyB.disconnect();
		} catch (SQLException e) {
			logger.error("Error disconnecting from ISpyB", e);
		}
	}

	public IObservableList getModel() {
		return bioSAXSProgressModel;
	}
}

class SimpleUDPReceiver implements IObserver {
	private BioSAXSProgressController controller;

	public SimpleUDPReceiver(BioSAXSProgressController controller) {
		this.controller = controller;
	}

	@Override
	public void update(Object theObserved, Object dataCollectionId) {
		long saxsDataCollectionId = Long.valueOf((String) dataCollectionId);
		System.out.println("SAXSDataCollection " + saxsDataCollectionId + " has been updated");
		controller.getStatusInfoFromISpyB(saxsDataCollectionId);
	}
}
