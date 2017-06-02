/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Findable;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

/**
 * Monitor an Area Detector Array plugin for new array frames and draw them to a standard plot view.
 *<p>
 * Start/Stop plot updates using the .start() and .stop() methods.
 * </p>
 * <p>
 * If created in Spring then use the Finder to start/stop:
 * <pre>
 * {@code
 * <bean id="ad_plot_update" class="gda.device.detector.areadetector.v17.ADArrayPlotUpdater">
 * 	<property name="name" value="ad_plot_update" />
 * 	<property name="plotName" value="AD Plot" />
 * 	<property name="arrayPlugin" ref="ndarray" />
 * </bean>
 *
 * Finder.getInstance().find("ad_plot_update").start()
 * ...
 * Finder.getInstance().find("ad_plot_update").stop()
 * }
 * </pre>
 * </p>
 */
public class ADArrayPlotUpdater implements Findable {

	private static Logger logger = LoggerFactory.getLogger(ADArrayPlotUpdater.class);

	private NDArray arrayPlugin;

	private String plotName;

	private String name;

	private double minUpdateTime = 0.1;

	private int lastCount = -1;

	private final ScheduledExecutorService execService = new ScheduledThreadPoolExecutor(1);

	private ScheduledFuture<?> updateFuture;

	public ADArrayPlotUpdater() {
	}

	public ADArrayPlotUpdater(String name, String plotName, NDArray arrayPlugin) {
		this.name = name;
		this.arrayPlugin = arrayPlugin;
		this.plotName = plotName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public NDArray getArrayPlugin() {
		return arrayPlugin;
	}

	public void setArrayPlugin(NDArray arrayPlugin) {
		this.arrayPlugin = arrayPlugin;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public double getMinUpdateTime() {
		return minUpdateTime;
	}

	public void setMinUpdateTime(double minUpdateTime) {
		this.minUpdateTime = minUpdateTime;
	}

	private void update() throws Exception {
		try {
			int arrayCount = arrayPlugin.getPluginBase().getArrayCounter_RBV();
			if (arrayCount != lastCount) {
				lastCount = arrayCount;
				int sizeX = arrayPlugin.getPluginBase().getArraySize0_RBV();
				int sizeY = arrayPlugin.getPluginBase().getArraySize1_RBV();
				short dtype = arrayPlugin.getPluginBase().getDataType_RBV();
				boolean unsigned = dtype == NDPluginBase.UInt8
						|| dtype == NDPluginBase.UInt16
						|| dtype == NDPluginBase.UInt32;
				Object data;
				if (dtype == NDPluginBase.Int8 || dtype == NDPluginBase.UInt8) {
					data = arrayPlugin.getByteArrayData(sizeX * sizeY);
				} else if (dtype == NDPluginBase.Int16 || dtype == NDPluginBase.UInt16) {
					data = arrayPlugin.getShortArrayData(sizeX * sizeY);
				} else if (dtype == NDPluginBase.Int32 || dtype == NDPluginBase.UInt32) {
					data = arrayPlugin.getIntArrayData(sizeX * sizeY);
				} else if (dtype == NDPluginBase.Float32) {
					data = arrayPlugin.getFloatArrayData(sizeX * sizeY);
				} else if (dtype == NDPluginBase.Float64) {
					data = arrayPlugin.getDoubleArrayData(sizeX * sizeY);
				} else {
					data = arrayPlugin.getImageData(sizeX * sizeY);
				}
				IDataset ds = DatasetFactory.createFromObject(unsigned, data);
				ds.setShape(sizeY, sizeX);
				SDAPlotter.imagePlot(plotName, ds);
			}
		} catch (Exception e) {
			logger.error("Could not update array view " + plotName, e);
			throw e;
		}
	}

	public void start() {
		if (updateFuture != null && !updateFuture.isDone()) {
			return;
		}
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					update();
				} catch (Exception e) {
					// RuntimeException will stop further runs of update, which is desirable for the moment
					logger.info("Stopping " + name + " updates to " + plotName + " after error");
					throw new RuntimeException(e);
				}
			}
		};
		updateFuture = execService.scheduleWithFixedDelay(r, 0, (long) (minUpdateTime * 1000), TimeUnit.MILLISECONDS);
	}

	public void stop() {
		if (updateFuture != null) {
			updateFuture.cancel(true);
		}
	}
}
