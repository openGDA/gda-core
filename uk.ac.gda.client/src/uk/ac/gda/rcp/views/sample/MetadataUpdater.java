/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.rcp.views.sample;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.Device;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.scan.Scan.ScanStatus;
import gda.scan.ScanEvent;

/**
 * A system status display panel updater, this contains all the logic, so the GUI class be edited by graphical SWT
 * layout editors without breaking functionality.
 */
public class MetadataUpdater implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(MetadataUpdater.class);

	private SampleMetadataView client;
	private Metadata metadata;
	private Integer totalScanPoints;
	private Date started;
	private String lastFileName;
	private int lastScanNumber;

	private class MetadataListener extends KeyAdapter implements FocusListener, IObserver {
		private Text widget;
		private String metadataName;
		private Device blaster;

		public MetadataListener(Text widget, String metadataName, Device blaster) {
			this.widget = widget;
			this.metadataName = metadataName;
			this.blaster = blaster;

			widget.addFocusListener(this);
			widget.addKeyListener(this);

			blaster.addIObserver(this);

			widget.setText(metadata.getMetadataValue(metadataName));
		}

		@Override
		public void keyReleased(KeyEvent e) {
			super.keyReleased(e);
			if (e.character == SWT.CR) {
				try {
					metadata.setMetadataValue(metadataName, widget.getText().trim());
				} catch (Exception e1) {
					widget.setText("");
				}
			}
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void focusLost(FocusEvent e) {
			widget.setText(metadata.getMetadataValue(metadataName));
		}

		private void unobserve() {
			blaster.deleteIObserver(this);
		}

		@Override
		public void update(Object source, final Object arg) {
			if (widget.isDisposed()) {
				unobserve();
				return;
			}
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					widget.setText(arg.toString());
					client.currentDirectory.setText(InterfaceProvider.getPathConstructor().createFromDefaultProperty());
				}
			});
		}
	}

	/**
	 * Constructor
	 *
	 * @param client {@link SampleMetadataView}
	 *            who to update
	 */
	@SuppressWarnings("unused") //compiler thinks neither MetadataListener is used
	public MetadataUpdater(final SampleMetadataView client) {
		this.client = client;

		try {
			metadata = GDAMetadataProvider.getInstance();

			new MetadataListener(client.subDirectory, "subdirectory", (Device) Finder.find("observableSubdirectory"));
			new MetadataListener(client.sampleName, "samplename", (Device) Finder.find("observableSamplename"));

		} catch (Exception e) {
			logger.warn("could not find required metadata", e);
		}
		InterfaceProvider.getScanDataPointProvider().addScanEventObserver(this);
		client.currentDirectory.setText(InterfaceProvider.getPathConstructor().createFromDefaultProperty());
	}

	@Override
	public void update(Object iObservable, Object arg) {
		if (client.scanStatus.isDisposed()) {
			InterfaceProvider.getScanDataPointProvider().deleteScanEventObserver(this);
			return;
		}
		if (arg == null) return;
		Display.getDefault().asyncExec(new Updater(arg));
	}

	private class Updater implements Runnable {
		private Object arg;
		private int currentPointNumber;

		public Updater(Object arg) {
			this.arg = arg;
		}

		private String hms4millis(long millis) {
			if (millis == 0) {
				return "--:--:--";
			}
			int h = (int) (millis / (3600 * 1000));
			int m = (int) (millis / (60 * 1000) % 60);
			int s = (int) (millis / 1000 % 60);
			return String.format("%02d:%02d:%02d", h, m, s);
		}

		private long etaPrediction(int currentpoint, int totalPoints, long elapsed) {
			if (currentpoint == 0) {
				return 0;
			}
			long timeRemaining = ((totalPoints - currentPointNumber) * elapsed / currentPointNumber);  // converging estimate: points remaing X current average time per point
			logger.debug(String.format("Time remainning: %d",timeRemaining));
			logger.debug("etaPrediction:"+String.format("totalPoints=%d, currentPointNumber=%d, elapsed=%s remaining=%s", totalPoints, currentPointNumber, hms4millis(elapsed), hms4millis(timeRemaining)));
			return timeRemaining;
		}

		private void updateElapsedTime () {
			if (started != null){ // non-null indicates clock has started
				logger.debug(String.format("updateElapsedTime, currentPointNumber=%d", currentPointNumber));
				long elapsed = ((new Date()).getTime()) - started.getTime();
				client.elapsedTime.setText(hms4millis(elapsed));
				if (totalScanPoints != null ) {
					client.remainingTime.setText(hms4millis(etaPrediction(currentPointNumber, totalScanPoints, elapsed)));
				}
			}
		}

		private void clockStart() {
			started = new Date();
		}

		private void clockStop() {
			started = null;
		}

		@Override
		public void run() {
			if (arg instanceof ScanEvent se) {
				currentPointNumber = se.getCurrentPointNumber() + 1; // CurrentPointNumbers is a zero-based index

				lastScanNumber = se.getLatestInformation().getScanNumber();
				totalScanPoints = se.getLatestInformation().getNumberOfPoints();

				client.scanPoint.setText(String.format("%d / %d", currentPointNumber, totalScanPoints));
				client.progressBar.setSelection(10000 * currentPointNumber / totalScanPoints);

				ScanStatus ss = se.getLatestStatus();
				client.scanStatus.setText(ss.toString());
				if (ss == ScanStatus.NOTSTARTED) {
					clockStart();
				} else if (ss == ScanStatus.COMPLETED_OKAY) {          // use ScanEvent to access items to update at end-of-scan
					clockStop();
					lastFileName = se.getLatestInformation().getFilename();
					client.scanFile.setText(lastFileName);
					client.scanNumber.setText(Integer.toString(lastScanNumber + 1)); // indicate number in file name that will be used for next scan
				}
				updateElapsedTime();
			}
		}
	}
}
