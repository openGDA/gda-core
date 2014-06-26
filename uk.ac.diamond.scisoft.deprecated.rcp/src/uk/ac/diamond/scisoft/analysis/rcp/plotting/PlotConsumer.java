/*
 * Copyright 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IObservable;
import gda.observable.IObserver;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;

public class PlotConsumer implements Runnable, IObservable {

	// Adding in some logging to help with getting this running
	transient private static final Logger logger = LoggerFactory.getLogger(PlotConsumer.class);
	
	private PlotServer plotServer;
	private String viewName;
	private boolean terminate = false;
	private boolean updateFinished = true;
	private int numberOfJobs = 0;
	private Set<IObserver> observers = Collections.synchronizedSet(new LinkedHashSet<IObserver>());
	private List<PlotJob> jobList = Collections.synchronizedList(new LinkedList<PlotJob>());
	private final static int TIMEOUTTILLNEXTJOB = 100; // in milliseconds

	/**
	 * Create a PlotConsumer
	 * 
	 * @param server
	 *            plotServer object
	 * @param viewName
	 *            name of the view
	 */
	public PlotConsumer(PlotServer server, String viewName) {
		plotServer = server;
		this.viewName = viewName;
	}

	/**
	 * Add a job to consume
	 * 
	 * @param newJob
	 *            job object to add to the queue
	 */
	public synchronized void addJob(PlotJob newJob) {
		if (newJob.getType() == PlotJobType.GUI) {
			jobList.add(newJob);
			numberOfJobs++;
		} else {
			boolean foundOldJob = false;
			synchronized(jobList) {
				Iterator<PlotJob> iter = jobList.iterator();
				while (iter.hasNext()) {
					PlotJob job = iter.next();
					if (job.getType() == PlotJobType.Data) {
						jobList.remove(job);
						foundOldJob = true;
						break;
					}
				}
				jobList.add(newJob);
			}
			if (!foundOldJob) {
				numberOfJobs++;
			}
		}
		notify();
	}

	/**
	 * Stop the plot consumer
	 */
	public void stop() {
		terminate = true;
		updateFinished = true;
	}

	@Override
	public void run() {
		while (!terminate) {
			try {
				synchronized (this) {
					while (numberOfJobs == 0) {
						wait();
					}
				}
			} catch (InterruptedException ex) {
			}
			try {
				PlotJob currentJob = null;
				synchronized(jobList) {
					 currentJob = jobList.get(0);
					jobList.remove(0);
				}
				numberOfJobs--;
				switch (currentJob.getType()) {
				case Data: {
					DataBean dbPlot = plotServer.getData(viewName);
					// check if there is actually a data bean available
					// if yes notify observers that there is a new one
					// available
					if (dbPlot != null) {
						logger.debug("Consuming a job for "+viewName);
						Iterator<IObserver> iter = observers.iterator();
						updateFinished = false;
						while (iter.hasNext()) {
							IObserver ob = iter.next();
							ob.update(this, dbPlot);
						}
						synchronized(this) {
							while (!updateFinished) {
								wait();
							}
						}						
					}
					// to prevent the UI from being hammered since it all
					// is asynchronous and just will return at once
					// restrict the update rate to 10hz
					try {
						Thread.sleep(TIMEOUTTILLNEXTJOB);
					} catch (InterruptedException ex) {
					}
				}
					break;
				case GUI: {
					GuiBean guiBean = currentJob.getGuiBean();
					if (guiBean != null) {
						Iterator<IObserver> iter = observers.iterator();
						while (iter.hasNext()) {
							IObserver ob = iter.next();
							ob.update(this, guiBean);
						}
					}
				}
					break;
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observers.add(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observers.remove(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observers.removeAll(observers);
	}
	
	public synchronized void dataUpdateFinished() {
		updateFinished = true;
		notify();
	}
	
}
