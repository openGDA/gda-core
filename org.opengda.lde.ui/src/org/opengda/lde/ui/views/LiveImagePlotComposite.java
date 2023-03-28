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

package org.opengda.lde.ui.views;

import java.util.Arrays;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * Monitor and plotting live image data from the electron analyser.
 */
public class LiveImagePlotComposite extends Composite implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(LiveImagePlotComposite.class);

	private static final String IMAGE_PLOT = "Image plot";

	private String arrayPV;
	private String arrayEnablePV;
	private String xSizePV;
	private String ySizePV;

	private EpicsChannelManager controller;
	private int xDimension;
	private int yDimension;
	private IPlottingSystem<Composite> plottingSystem;

	private String plotName;

	private ImageDataListener dataListener;
	private XSizeListener xSizeListener = new XSizeListener();
	private YSizeListener ySizeListener = new YSizeListener();

	@SuppressWarnings("unused")
	private Channel dataChannel;
	@SuppressWarnings("unused")
	private Channel enableChanel;
	private Channel xSizeChannel;
	private Channel ySizeChannel;

	private Supplier<String> titleProvider = () -> "Intensity";

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public LiveImagePlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		super(parent, style);
		this.setBackground(ColorConstants.white);

		controller = new EpicsChannelManager(this);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, getPlotName(), part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars() : null,
				PlotType.IMAGE, part);
		plottingSystem.setTitle(IMAGE_PLOT);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("######.#");
	}

	public void initialise() {
		if (getArrayPV() == null) {
			throw new IllegalStateException("required parameters for 'arrayPV' are missing.");
		}
		if (getxDimension() == 0 || getyDimension() == 0) {
			throw new IllegalStateException("Area detector dimension cannot be 0.");
		}
		dataListener = new ImageDataListener();
		try {
			createChannels();
		} catch (CAException e1) {
			logger.error("failed to create required spectrum channels", e1);
		}
	}

	public void createChannels() throws CAException {
		dataChannel = controller.createChannel(arrayPV, dataListener, MonitorType.NATIVE, false);
		enableChanel= controller.createChannel(arrayEnablePV, null, false, 1);
		if (getxSizePV()!=null) {
			xSizeChannel=controller.createChannel(getxSizePV(), xSizeListener, MonitorType.NATIVE, true);
		}
		if (getySizePV()!=null) {
			ySizeChannel=controller.createChannel(getySizePV(), ySizeListener, MonitorType.NATIVE, true);
		}
		controller.creationPhaseCompleted();
		logger.debug("Image channel is created");
	}

	@Override
	public void dispose() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
//		dataChannel.dispose();
		super.dispose();
	}

	public void clearPlots() {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				plottingSystem.reset();
			}
		});
	}

	private abstract class DbrListener implements MonitorListener {
		private boolean first = true;
		private String name;
		protected DbrListener(String name) {
			this.name = name;
		}
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (first) {
				logger.debug("{} listener initialised", name);
				first = false;
				return;
			}
			DBR dbr = arg0.getDBR();
			logger.trace("{} - new update: {}", name, dbr);
			handleDbr(dbr);
		}
		protected abstract void handleDbr(DBR dbr);
	}

	private class XSizeListener extends DbrListener {
		public XSizeListener() {
			super("XSize");
		}
		@Override
		protected void handleDbr(DBR dbr) {
			if (dbr.isINT()) {
				xDimension = ((DBR_Int) dbr).getIntValue()[0];
			}
		}
	}

	private class YSizeListener extends DbrListener {
		public YSizeListener() {
			super("YSize");
		}
		@Override
		protected void handleDbr(DBR dbr) {
			if (dbr.isINT()) {
				yDimension = ((DBR_Int) dbr).getIntValue()[0];
			}
		}
	}

	private class ImageDataListener extends DbrListener {
		public ImageDataListener() {
			super("Image data");
		}
		@Override
		protected void handleDbr(DBR dbr) {
			if (dbr.isDOUBLE()) {
				final double[] value = ((DBR_Double) dbr).getDoubleValue();
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(() -> {
						IProgressMonitor monitor = new NullProgressMonitor();
						try {
							updateImagePlot(monitor, value);
						} catch (Exception e) {
							logger.error("exception caught preparing analyser live plot",e);
						}
					});
				}
			}
		}
	}

	private void updateImagePlot(final IProgressMonitor monitor, final double[] value) {
		try {
			int[] dims = new int[] { getyDimension(), getxDimension() };
			int arraysize = dims[0] * dims[1];
			if (arraysize < 1) {
				return;
			}
			double[] values = Arrays.copyOf(value, arraysize);
			final Dataset ds = DatasetFactory.createFromObject(values, dims);
			ds.setName(titleProvider.get());
			plottingSystem.clear();
			plottingSystem.createPlot2D(ds, null, monitor);
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live image plot", e);
		}
	}

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.info("Image EPICS Channel initialisation completed!");

	}

	public int getxDimension() {
		return xDimension;
	}

	public void setxDimension(int xDimension) {
		this.xDimension = xDimension;
	}

	public int getyDimension() {
		return yDimension;
	}

	public void setyDimension(int yDimension) {
		this.yDimension = yDimension;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public void setArrayEnablePV(String arrayEnablePV) {
		this.arrayEnablePV=arrayEnablePV;

	}

	public String getxSizePV() {
		return xSizePV;
	}

	public void setxSizePV(String xSizePV) {
		this.xSizePV = xSizePV;
	}

	public String getySizePV() {
		return ySizePV;
	}

	public void setySizePV(String ySizePV) {
		this.ySizePV = ySizePV;
	}

	public Supplier<String> getTitleProvider() {
		return titleProvider;
	}

	public void setTitleProvider(Supplier<String> titleProvider) {
		this.titleProvider = titleProvider;
	}
}
