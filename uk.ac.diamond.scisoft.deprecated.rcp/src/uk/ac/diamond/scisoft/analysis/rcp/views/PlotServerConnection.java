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

package uk.ac.diamond.scisoft.analysis.rcp.views;

import gda.observable.IObservable;
import gda.observable.IObserver;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.plotclient.IUpdateNotificationListener;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiUpdate;
import uk.ac.diamond.scisoft.analysis.plotserver.IBeanScriptingManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotConsumer;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotJob;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotJobType;

/**
 * This class deals with the connection between a PlotWindow and the PlotServer
 * 
 * Copied straight out of PlotView but can be used anywhere.
 */
public class PlotServerConnection implements IObserver, 
										     IObservable, 
										     IBeanScriptingManager,
											 IUpdateNotificationListener  {

	// Adding in some logging to help with getting this running
	private static final Logger logger = LoggerFactory.getLogger(PlotServerConnection.class);


	private       AbstractPlotWindow      plotWindow;
	private final PlotServer      plotServer;
	private final ExecutorService execSvc;
	private final String          plotName;
	private final UUID            plotId;
	
	private PlotConsumer    plotConsumer  = null;
	private GuiBean         guiBean       = null;
	private Set<IObserver>  dataObservers = Collections.synchronizedSet(new LinkedHashSet<IObserver>());
	private List<IObserver> observers     = Collections.synchronizedList(new LinkedList<IObserver>());

	/**
	 * Default Constructor of the plot view
	 */
	public PlotServerConnection(final String plotName) {
		
		this.plotName   = plotName;
		this.plotId     = UUID.randomUUID();
		
		plotServer = PlotServerProvider.getPlotServer();
		plotServer.addIObserver(this);
		execSvc = Executors.newFixedThreadPool(2);
		
		plotConsumer = new PlotConsumer(plotServer, plotName);

		execSvc.execute(plotConsumer);
		plotConsumer.addJob(new PlotJob(PlotJobType.Data));

	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved.equals(plotConsumer)) {
			if (changeCode instanceof DataBean) {
				plotWindow.processPlotUpdate((DataBean) changeCode);
				notifyDataObservers((DataBean)changeCode);
			} else if (changeCode instanceof GuiBean) {
				plotWindow.processGUIUpdate((GuiBean) changeCode);
			}
		} else {
			if (changeCode instanceof String && changeCode.equals(plotName)) {
				plotConsumer.addJob(new PlotJob(PlotJobType.Data));
			}
			if (changeCode instanceof GuiUpdate) {
				GuiUpdate gu = (GuiUpdate) changeCode;
				if (gu.getGuiName().contains(plotName)) {
					GuiBean bean = gu.getGuiData();
					UUID id = (UUID) bean.get(GuiParameters.PLOTID);
					if (id == null || plotId.compareTo(id) != 0) { // filter out own beans
						if (guiBean == null)
							guiBean = bean.copy(); // cache a local copy
						else
							guiBean.merge(bean);   // or merge it
						PlotJob job = new PlotJob(PlotJobType.GUI);
						job.setGuiBean(bean);
						plotConsumer.addJob(job);
					}
				}
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
		observers.clear();

	}

	/**
	 * Allow another observer to see plot data.
	 * <p>
	 * A data observer gets an update with a data bean.
	 * @param observer
	 */
	public void addDataObserver(IObserver observer) {
		dataObservers.add(observer);
	}

	/**
	 * Remove a data observer
	 * 
	 * @param observer
	 */
	public void deleteDataObserver(IObserver observer) {
		dataObservers.remove(observer);
	}

	/**
	 * Remove all data observers
	 */
	public void deleteDataObservers() {
		dataObservers.clear();
	}

	private void notifyDataObservers(DataBean bean) {
		Iterator<IObserver> iter = dataObservers.iterator();
		while (iter.hasNext()) {
			IObserver ob = iter.next();
			ob.update(this, bean);
		}
	}

	/**
	 * Get gui information from plot server
	 */
	@Override
	public GuiBean getGUIInfo() {
		getGUIState();
		return guiBean;
	}

	private void getGUIState() {
		if (guiBean == null) {
			try {
				guiBean = plotServer.getGuiState(plotName);
			} catch (Exception e) {
				logger.warn("Problem with getting GUI data from plot server");
			}
			if (guiBean == null)
				guiBean = new GuiBean();
		}
	}

	/**
	 * Push gui information back to plot server
	 * @param key 
	 * @param value 
	 */
	@Override
	public void putGUIInfo(GuiParameters key, Serializable value) {
		getGUIState();

		guiBean.put(key, value);

		sendGUIInfo(guiBean);
	}

	/**
	 * Remove gui information from plot server
	 * @param key
	 */
	@Override
	public void removeGUIInfo(GuiParameters key) {
		getGUIState();

		guiBean.remove(key);

		sendGUIInfo(guiBean);
	}

	
	/**
	 * Does not dispose the plot window
	 */
	public void dispose() {
	   plotConsumer.stop();
	   execSvc.shutdown();
	   deleteIObservers();
	   deleteDataObservers();
	   System.gc();
	}

	@Override
	public void sendGUIInfo(GuiBean guiBean) {
		guiBean.put(GuiParameters.PLOTID, plotId); // put plotID in bean
		try {
			plotServer.updateGui(plotName, guiBean);
		} catch (Exception e) {
			logger.warn("Problem with updating plot server with GUI data");
			e.printStackTrace();
		}
	}

	@Override
	public void updateProcessed() {
		if (plotConsumer != null)
			plotConsumer.dataUpdateFinished();
	}

	public AbstractPlotWindow getPlotWindow() {
		return plotWindow;
	}

	public void setPlotWindow(AbstractPlotWindow plotWindow) {
		this.plotWindow = plotWindow;
		plotConsumer.addIObserver(this);
	}
}
