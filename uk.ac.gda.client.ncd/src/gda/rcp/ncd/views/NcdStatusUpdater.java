/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp.ncd.views;

import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.Device;
import gda.device.TimerStatus;
import gda.factory.Finder;
import gda.jython.IAllScanDataPointsObserver;
import gda.jython.InterfaceProvider;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.rcp.ncd.ExptDataModel;
import gda.rcp.ncd.NcdController;
import gda.scan.ScanDataPoint;

import java.text.NumberFormat;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;
import uk.ac.gda.server.ncd.plotting.DetectorRates;

/**
 * A system status display panel
 */
public class NcdStatusUpdater implements IObserver, IAllScanDataPointsObserver {

	private static final Logger logger = LoggerFactory.getLogger(NcdStatusUpdater.class);

	private IObservable tfg;
	private ExptDataModel exptDataModel;
	private NcdStatus client;
	private String subdirectory;
	private Metadata metadata;

	/**
	 * Constructor
	 * 
	 * @param client
	 *            who to update
	 */
	public NcdStatusUpdater(final NcdStatus client) {
		this.client = client;

		exptDataModel = ExptDataModel.getInstance();
		if (exptDataModel != null) {
			exptDataModel.addIObserver(this);
		}
		tfg = NcdController.getInstance().getNcdDetectorSystem();
		if (tfg != null) {
			tfg.addIObserver(this);
		}

		try {
			metadata = GDAMetadataProvider.getInstance();
			subdirectory = metadata.getMetadataValue("subdirectory");

			Device blaster = Finder.getInstance().find("observableSubdirectory");
			blaster.addIObserver(this);

			meUpdate();
		} catch (Exception e) {
			logger.warn("could not find subdirectory metadata", e);
		}

		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);

		client.subDirectory.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (e.character == SWT.CR) {

					try {
						metadata.setMetadataValue("subdirectory", client.subDirectory.getText().trim());
					} catch (Exception e1) {
						client.subDirectory.setText("");
					}
				}
			}
		});
	}

	@Override
	public void update(Object iObservable, Object arg) {
		Display.getDefault().asyncExec(new Updater(iObservable, arg));
	}

	private void meUpdate() {
		client.subDirectory.setText(subdirectory);
		client.currentDirectory.setText(PathConstructor.createFromDefaultProperty());
		exptDataModel.setDirectory(client.currentDirectory.getText());
	}

	private class Updater implements Runnable {
		private Object iObservable;

		private Object arg;

		/**
		 * @param iObservable
		 * @param arg
		 */
		public Updater(Object iObservable, Object arg) {
			this.iObservable = iObservable;
			this.arg = arg;
		}

		@Override
		public void run() {
			if (arg != null) {
				if (arg instanceof TimerStatus) {
					TimerStatus ts = (TimerStatus) arg;

					long diff = ts.getElapsedTime();
					NumberFormat nf = NumberFormat.getInstance();
					nf.setMinimumIntegerDigits(2);
					long hour = diff / 3600000;
					diff -= hour * 3600000;
					long min = diff / 60000;
					diff -= min * 60000;
					long sec = diff / 1000;

					String text;
					text = nf.format(hour) + ":" + nf.format(min) + ":" + nf.format(sec);
					client.elapsedTime.setText(text);

					client.frameStatus.setText(ts.getCurrentStatus());
					text = "" + ts.getCurrentFrame();
					client.frameNumber.setText(text);
					text = "" + ts.getCurrentCycle();
					client.cycleNumber.setText(text);
					text = "" + ts.getTotalCycles();
					client.totalCycleCount.setText(text);

					client.progressBar.setSelection(ts.getPercentComplete());
				} else if (iObservable instanceof Device) {
					if (arg instanceof Collection<?>) {
						for (Object odr : (Collection<?>) arg) {
							if (odr instanceof DetectorRates) {
								DetectorRates dr = (DetectorRates) odr;
								if (NcdDetectorSystem.WAXS_DETECTOR.equalsIgnoreCase(dr.detType)) {
									client.waxsCountRate.setText(String.format("%,6.5G cps", dr.integratedCounts / dr.countingTime));
									client.waxsCount.setText(String.format("%,6.6G cps", dr.integratedCounts));
									client.waxsPeakRate.setText(String.format("%,6.5G cps", dr.maxCounts / dr.countingTime));
									client.waxsPeak.setText(String.format("%,6.6G counts", dr.maxCounts));
								} else if (NcdDetectorSystem.SAXS_DETECTOR.equalsIgnoreCase(dr.detType)) {
									client.saxsCountRate.setText(String.format("%,6.5G cps", dr.integratedCounts / dr.countingTime));
									client.saxsCount.setText(String.format("%,6.6G cps", dr.integratedCounts));
									client.saxsPeakRate.setText(String.format("%,6.5G cps", dr.maxCounts / dr.countingTime));
									client.saxsPeak.setText(String.format("%,6.6G counts", dr.maxCounts));
								}
							}
						}
					} else if (arg instanceof String) {
						subdirectory = arg.toString();
						meUpdate();
					}
				} else if (arg instanceof ScanDataPoint) {
					String filename = ((ScanDataPoint) arg).getCurrentFilename();
					client.scanFile.setText(filename);
				}
			} else if (iObservable instanceof ExptDataModel) {
				client.parameterFile.setText(exptDataModel.getFileName());
				client.currentDirectory.setText(exptDataModel.getDirectory());
			}
		}
	}
}