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

import gda.device.Device;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;
import uk.ac.gda.devices.bssc.ispyb.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;
import uk.ac.gda.devices.bssc.ui.BioSAXSProgressView;

public class BioSAXSProgressController implements IObservable, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressController.class);
	private BioSAXSISPyB bioSAXSISPyB;
	private IObservableList bioSAXSProgressModel;
	private String visit;
	private long blSessionId;
	private List<ISAXSDataCollection> saxsDataCollections;
	private Device simpleUDPServer;
	ObservableComponent obsComp = new ObservableComponent();
	private boolean stopPolling;
	protected BioSAXSProgressView view;
	private String udpListenerName;

	@Override
	public void configure() throws FactoryException {
		if (udpListenerName != null && simpleUDPServer == null) {
			simpleUDPServer = Finder.getInstance().find(udpListenerName);
			simpleUDPServer.addIObserver(new SimpleUDPReceiver(this));
		}
//		try {
//			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
//			blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
			blSessionId = 435;
//		} catch (DeviceException e) {
//			logger.error("DeviceException getting visit", e);
//		} 
//		catch (SQLException e) {
//			logger.error("SQLEXception getting session id", e);
//		}
	}

	public void setISpyBAPI(BioSAXSISPyB bioSAXSISPyB) throws FactoryException {
		this.bioSAXSISPyB = bioSAXSISPyB;
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

					if (saxsDataCollections != null) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								loadModelFromISpyB(saxsDataCollections);
							}
						});
					}
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					return Status.OK_STATUS;
				} catch (Exception e) {
					logger.error("Error polling ispyb", e);
					return Status.CANCEL_STATUS;
				} finally {
					// start job again after specified time has elapsed
					// if (!stopPolling)
					// schedule(90000);
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
			logger.error("SQL Exception getting data collections from ISpyB", e);
		}
		return saxsDataCollections;
	}

	public List<ISAXSProgress> loadModelFromISpyB(List<ISAXSDataCollection> saxsDataCollections) {
		for (ISAXSDataCollection saxsDataCollection : saxsDataCollections) {
			if (saxsDataCollection != null) {
				long experimentId = saxsDataCollection.getExperimentId();
				long dataCollectionId = saxsDataCollection.getId();
				String sampleName = saxsDataCollection.getSampleName();
				ISpyBStatusInfo collectionStatusInfo = saxsDataCollection.getCollectionStatus();
				ISpyBStatusInfo reductionStatusInfo = saxsDataCollection.getReductionStatus();
				ISpyBStatusInfo analysisStatusInfo = saxsDataCollection.getAnalysisStatus();

				ISAXSProgress progress = getProgressItemFromModel(bioSAXSProgressModel, dataCollectionId);
				if (progress != null) {
					updateModel(progress, collectionStatusInfo, reductionStatusInfo, analysisStatusInfo);
				} else {
					
					addToModel(experimentId, dataCollectionId, sampleName, collectionStatusInfo, reductionStatusInfo,
							analysisStatusInfo);
				}
			}
		}
		
		return bioSAXSProgressModel;
	}
	
	public void updateStatusInfoFromISpyB(final long dataCollectionId) {
		try {
			final ISAXSDataCollection dataCollection = bioSAXSISPyB.getSAXSDataCollection(dataCollectionId);
			final long experimentId = dataCollection.getExperimentId();
			final ISpyBStatusInfo collectionStatusInfo = bioSAXSISPyB.getDataCollectionStatus(dataCollectionId);
			final ISpyBStatusInfo reductionStatusInfo = bioSAXSISPyB.getDataReductionStatus(dataCollectionId);
			final ISpyBStatusInfo analysisStatusInfo = bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId);

			ISAXSProgress progress = getProgressItemFromModel(bioSAXSProgressModel, dataCollectionId);
			if (progress != null) {
				updateModel(progress, collectionStatusInfo, reductionStatusInfo, analysisStatusInfo);
			} else {
				addToModel(experimentId, dataCollectionId, dataCollection.getSampleName(), collectionStatusInfo,
						reductionStatusInfo, analysisStatusInfo);
			}

			scrollToUpdatedItem();

		} catch (SQLException e) {
			logger.error("SQLException", e);
		}
	}
	
	private void updateModel(ISAXSProgress progress, ISpyBStatusInfo collectionStatusInfo, ISpyBStatusInfo reductionStatusInfo, ISpyBStatusInfo analysisStatusInfo) {
		progress.setCollectionStatusInfo(collectionStatusInfo);
		progress.setReductionStatusInfo(reductionStatusInfo);
		progress.setAnalysisStatusInfo(analysisStatusInfo);

		scrollToUpdatedItem();
	}

	private void addToModel(long experimentId, long dataCollectionId, String sampleName,
			ISpyBStatusInfo collectionStatusInfo, ISpyBStatusInfo reductionStatusInfo,
			ISpyBStatusInfo analysisStatusInfo) {
		final ISAXSProgress progress = new BioSAXSProgress(experimentId, dataCollectionId, sampleName,
				collectionStatusInfo, reductionStatusInfo, analysisStatusInfo);
		progress.setCollectionStatusInfo(collectionStatusInfo);
		progress.setReductionStatusInfo(reductionStatusInfo);
		progress.setAnalysisStatusInfo(analysisStatusInfo);

		bioSAXSProgressModel.add(progress);

		scrollToUpdatedItem();
	}

	private void scrollToUpdatedItem() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		view = (BioSAXSProgressView) window.getActivePage().findView(BioSAXSProgressView.ID);

		if (view != null) {
			view.reveal();
		}
	}
	
	protected ISAXSProgress getProgressItemFromModel(List<ISAXSProgress> list, long id) {
		for (ISAXSProgress progressItem : list) {
			if (progressItem.getDataCollectionId() == id) {
				return progressItem;
			}
		}
		return null;
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

	public void setModel(final List<ISAXSProgress> list) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				bioSAXSProgressModel = new WritableList(list, ISAXSProgress.class);
			}
		});
	}

	public List<ISAXSProgress> getModel() {
		return bioSAXSProgressModel;
	}

	public void setUDPListenerName(String simpleUDPServer) {
		this.udpListenerName = simpleUDPServer;
	}
	
	public String getUDPListenerName() {
		return udpListenerName;
	}

	public void disconnectFromISpyB() {
		try {
			bioSAXSISPyB.disconnect();
		} catch (SQLException e) {
			logger.error("Error disconnecting from ISpyB", e);
		}
	}
}

class SimpleUDPReceiver implements IObserver {
	private BioSAXSProgressController controller;

	public SimpleUDPReceiver(BioSAXSProgressController controller) {
		this.controller = controller;
	}

	@Override
	public void update(Object theObserved, Object dataCollectionId) {
		final long saxsDataCollectionId = Long.valueOf((String) dataCollectionId);
		System.out.println("UDP update recieved for Sample : " + saxsDataCollectionId);

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				controller.updateStatusInfoFromISpyB(saxsDataCollectionId);
			}
		});

	}
}