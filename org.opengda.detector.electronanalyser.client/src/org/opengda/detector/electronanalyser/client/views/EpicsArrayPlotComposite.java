package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.swt.widgets.Composite;
import org.opengda.detector.electronanalyser.client.IEnergyAxis;
import org.opengda.detector.electronanalyser.client.IPlotCompositeInitialiser;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cosylab.epics.caj.CAJChannel;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

public class EpicsArrayPlotComposite extends Composite implements InitializationListener, MonitorListener, IEnergyAxis,IPlotCompositeInitialiser {

	private static final Logger logger = LoggerFactory.getLogger(EpicsArrayPlotComposite.class);
	protected String arrayPV;
	protected IVGScientaAnalyser analyser;
	protected IPlottingSystem plottingSystem;
	protected ILineTrace profileLineTrace;
	protected DataListener dataListener;
	protected Channel dataChannel;
	protected EpicsChannelManager controller;
	protected boolean first = false;
	protected Channel startChannel;
	protected boolean channelCreated = false;
	protected Dataset dataset;
	protected double[] xdata = null;
	private boolean newRegion = true;
	private boolean displayBindingEnergy = false;
	protected Dataset xAxis;

	public EpicsArrayPlotComposite(Composite parent, int style) {
		super(parent, style);
		controller = new EpicsChannelManager(this);
	}

	@Override
	public void initialise() {
		if (getAnalyser() == null || getArrayPV() == null) {
			throw new IllegalStateException("required parameters for 'analyser' and/or 'arrayPV' are missing.");
		}
		dataListener = new DataListener();
		createChannels();
	}

	private void createChannels() {
		if (!channelCreated) {
			first = true;
			try {
				dataChannel = controller.createChannel(arrayPV,dataListener, MonitorType.NATIVE, false);
				String[] split = getArrayPV().split(":");
				startChannel = controller.createChannel(split[0] + ":" + split[1] + ":" + ADBase.Acquire, this, MonitorType.NATIVE, false);
				controller.creationPhaseCompleted();
				controller.tryInitialize(100);
				logger.debug("Data channel {} is created", dataChannel.getName());
				channelCreated = true;
			} catch (CAException e) {
				logger.error("Failed to create channel {}", dataChannel.getName());
			}
		}
	}

	@Override
	public boolean setFocus() {
		if (!channelCreated) {
			createChannels();
		}
		return super.setFocus();
	}

	@Override
	public void dispose() {
			if (!plottingSystem.isDisposed()) {
				plottingSystem.clear();
			}
			//comment out see BLIX-144 for info.
	//		dataChannel.dispose();
	//		startChannel.dispose();
			super.dispose();
		}

	public void clearPlots() {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				plottingSystem.setTitle("");
				plottingSystem.reset();
			}
		});

	}

	protected class DataListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			if (first) {
				first = false;
				logger.debug("Monitor listener is added to channel {}.", ((Channel)arg0.getSource()).getName());
				return;
			}

			if (!getDisplay().isDisposed()) {
				getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
//						boolean visible = EpicsArrayPlotComposite.this.isVisible();
//						if (visible) {
							DBR dbr = arg0.getDBR();
							double[] value = null;
							if (dbr.isDOUBLE()) {
								value = ((DBR_Double) dbr).getDoubleValue();
							}
							updatePlot(new NullProgressMonitor(), value);
//						}
					}
				});
			}
		}
	}
	/**
	 * subclass must override this method to provide actual data and plotting.
	 * The override method must call this method first to set X-Axis.
	 */
	protected void updatePlot(final IProgressMonitor monitor, double[] value) {
		xdata = getXData();
		xAxis = createXAxis();
		plottingSystem.clear();
		plottingSystem.reset();
		plottingSystem.getSelectedXAxis().setRange(xdata[0], xdata[xdata.length-1]);
	}

	private Dataset createXAxis() {
		Dataset xAxis = DatasetFactory.createFromObject(xdata);
		if (isDisplayBindingEnergy()) {
			xAxis.setName("Binding Energies (eV)");
		} else {
			xAxis.setName("Kinetic Energies (eV)");
		}
		return xAxis;
	}

	private double[] getXData() {
		double[] xdata=null;
		try {
			xdata = getAnalyser().getEnergyAxis();
			if (isDisplayBindingEnergy()) {
				xdata=convertToBindingENergy(xdata);
			}
		} catch (Exception e) {
			logger.error("cannot get enegery axis fron the analyser", e);
		}
		return xdata;
	}
	/**
	 * subclass must override this method to provide actual data and plotting.
	 * The override method must call this method first to set X-Axis.
	 */
	@Override
	public void updatePlot() {
		if (xdata==null) return;
		xdata=convertToBindingENergy(xdata);
		xAxis = createXAxis();
		plottingSystem.clear();
		plottingSystem.reset();
		plottingSystem.getSelectedXAxis().setRange(xdata[0], xdata[xdata.length-1]);
	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	@Override
	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public String getArrayPV() {
		return arrayPV;
	}

	@Override
	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}

	@Override
	public void initializationCompleted() throws InterruptedException,
			DeviceException, TimeoutException, CAException {
				logger.info("EPICS Channel {} initialisation completed!", dataChannel.getName());

			}

	public void setNewRegion(boolean b) {
		this.newRegion = b;

	}

	public boolean isNewRegion() {
		return newRegion;
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		if (((CAJChannel) arg0.getSource()).getName().endsWith(ADBase.Acquire)) {
			setNewRegion(true);
		}
	}

	private double[] convertToBindingENergy(double[] xdata) {
		try {
			double excitationEnergy = getAnalyser().getExcitationEnergy();
			for (int i = 0; i < xdata.length; i++) {
				xdata[i] = excitationEnergy - xdata[i];
			}
		} catch (Exception e) {
			logger.error("cannot get enegery axis fron the analyser", e);
		}
		return xdata;
	}

	@Override
	public void displayInBindingEnergy(boolean b) {
		this.displayBindingEnergy=b;
	}

	public boolean isDisplayBindingEnergy() {
		return displayBindingEnergy;
	}

}
