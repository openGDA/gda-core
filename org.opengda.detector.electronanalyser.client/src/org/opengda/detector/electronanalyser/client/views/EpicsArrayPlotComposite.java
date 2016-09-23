package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;
import org.opengda.detector.electronanalyser.client.IEnergyAxis;
import org.opengda.detector.electronanalyser.client.IPlotCompositeInitialiser;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cosylab.epics.caj.CAJChannel;
import com.google.common.util.concurrent.RateLimiter;

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

public class EpicsArrayPlotComposite extends Composite implements InitializationListener, MonitorListener, IEnergyAxis, IPlotCompositeInitialiser {

	private static final Logger logger = LoggerFactory.getLogger(EpicsArrayPlotComposite.class);
	protected String updatePV;
	protected IVGScientaAnalyser analyser;
	protected IPlottingSystem<Composite> plottingSystem;
	protected ILineTrace profileLineTrace;
	protected UpdateListener updateListener;
	protected Channel updateChannel;
	protected EpicsChannelManager controller;
	protected boolean first = false;
	protected Channel startChannel;
	protected boolean channelCreated = false;
	protected Dataset dataset;
	protected double[] xdata = null;
	private boolean newRegion = true;
	private volatile boolean displayBindingEnergy = false;
	protected IDataset xAxis;
	private RateLimiter rateLimiter;
	private double updatesPerSecond = 5; // Hz, the fastest the plots will try to update

	public EpicsArrayPlotComposite(Composite parent, int style) {
		super(parent, style);
		controller = new EpicsChannelManager(this);
	}

	@Override
	public void initialise() {
		if (getAnalyser() == null || getUpdatePV() == null) {
			throw new IllegalStateException("required parameters for 'analyser' and/or 'arrayPV' are missing.");
		}
		updateListener = new UpdateListener();
		createChannels();
		// Initialise the rate limiter
		rateLimiter = RateLimiter.create(updatesPerSecond);
	}

	private void createChannels() {
		if (!channelCreated) {
			first = true;
			try {
				updateChannel = controller.createChannel(updatePV, updateListener, MonitorType.NATIVE, false);
				String[] split = getUpdatePV().split(":");
				startChannel = controller.createChannel(split[0] + ":" + split[1] + ":" + ADBase.Acquire, this, MonitorType.NATIVE, false);
				controller.creationPhaseCompleted();
				controller.tryInitialize(100);
				logger.debug("Data channel {} is created", updateChannel.getName());
				channelCreated = true;
			} catch (CAException e) {
				logger.error("Failed to create channel {}", updateChannel.getName());
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
				plottingSystem.dispose();
			}
		// comment out see BLIX-144 for info.
		// dataChannel.dispose();
		// startChannel.dispose();
			super.dispose();
		}

	private class UpdateListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			if (first) {
				first = false;
				logger.debug("Monitor listener is added to channel {}.", ((Channel) arg0.getSource()).getName());
				return;
			}

			DBR dbr = arg0.getDBR();
			final long value = Math.round((((DBR_Double) dbr).getDoubleValue()[0]));

			// Check if we are in the prescan.
			if (value < 1) {
				logger.trace("Update ignored: In prescan");
				return;
			}
			// Check if we can update without exceeding the update rate
			if (rateLimiter.tryAcquire()) {
				if (!getDisplay().isDisposed()) {
					// Do update in UI thread
					getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							logger.trace("Doing update for point: {}", value);
							updatePlot(new NullProgressMonitor());
						}

					});
				}
			} else {
				logger.trace("Update ignored: Update rate in excess of limit");
			}
		}
	}

	/**
	 * subclass must override this method to provide actual data and plotting.
	 * The override method must call this method first to set X-Axis.
	 */
	protected synchronized void updatePlot(final IProgressMonitor monitor) {
		xAxis = createXAxis();
	}

	private synchronized IDataset createXAxis() {
		// Get the axis data from the analyser
		try {
			xdata = getAnalyser().getEnergyAxis();
		} catch (Exception e) {
			logger.error("Cannot get energy axis fron the analyser", e);
		}
		// Create a dataset of the axis
		Dataset xAxis = DatasetFactory.createFromObject(xdata);
		if (isDisplayInBindingEnergy()) {
			// Convert to binding energy
			xdata = convertToBindingEnergy(xdata);
			xAxis.setName("Binding Energies (eV)");
		} else {
			xAxis.setName("Kinetic Energies (eV)");
		}
		return xAxis;
	}

	/**
	 * subclass must override this method to provide actual data and plotting.
	 * The override method must call this method first to set X-Axis.
	 */
	@Override
	public synchronized void updatePlot() {
		xAxis = createXAxis();
	}

	protected synchronized void reverseXAxis() {
		IAxis xAxis = plottingSystem.getSelectedXAxis();
		// Flip the direction of the x axis
		double upper = xAxis.getUpper();
		double lower = xAxis.getLower();
		xAxis.setRange(Math.max(lower, upper), Math.min(lower, upper));
	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	@Override
	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	@Override
	public void initializationCompleted() throws InterruptedException,
			DeviceException, TimeoutException, CAException {
				logger.info("EPICS Channel {} initialisation completed!", updateChannel.getName());

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

	private double[] convertToBindingEnergy(double[] xdata) {
		try {
			double excitationEnergy = getAnalyser().getExcitationEnergy();
			for (int i = 0; i < xdata.length; i++) {
				xdata[i] = excitationEnergy - xdata[i];
			}
		} catch (Exception e) {
			logger.error("Cannot get excitation energy from the analyser", e);
		}
		return xdata;
	}

	@Override
	public void displayInBindingEnergy(boolean b) {
		this.displayBindingEnergy=b;
	}

	@Override
	public boolean isDisplayInBindingEnergy() {
		return displayBindingEnergy;
	}

	public String getUpdatePV() {
		return updatePV;
	}

	@Override
	public void setUpdatePV(String updatePV) {
		this.updatePV = updatePV;
	}

	public double getUpdatesPerSecond() {
		return updatesPerSecond;
	}

	@Override
	public void setUpdatesPerSecond(double updatesPerSecond) {
		this.updatesPerSecond = updatesPerSecond;
	}
}
