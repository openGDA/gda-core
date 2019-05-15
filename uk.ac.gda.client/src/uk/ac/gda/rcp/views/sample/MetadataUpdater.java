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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.Device;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.jython.IJythonServerStatusObserver;
import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.jython.commandinfo.CommandThreadEvent;
import gda.jython.commandinfo.CommandThreadEventType;
import gda.jython.commandinfo.CommandThreadInfo;
import gda.jython.commandinfo.ICommandThreadObserver;
import gda.observable.IObserver;
import gda.scan.Scan.ScanStatus;
import gda.scan.ScanDataPoint;
import gda.scan.ScanEvent;

/**
 * A system status display panel updater, this contains all the logic, so the GUI class be edited by graphical SWT
 * layout editors without breaking functionality.
 */
public class MetadataUpdater implements IObserver, IScanDataPointObserver, IJythonServerStatusObserver, ICommandThreadObserver {

	private static final Logger logger = LoggerFactory.getLogger(MetadataUpdater.class);

	private SampleMetadataView client;
	private Metadata metadata;
	private JythonServerFacade jsf;
	private List<Integer> scandimensions;
	private String scanstring;
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

			try {
				widget.setText(metadata.getMetadataValue(metadataName));
			} catch (DeviceException e1) {
				widget.setText("");
			}
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
			try {
				widget.setText(metadata.getMetadataValue(metadataName));
			} catch (DeviceException e1) {
				widget.setText("");
			}
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
					client.currentDirectory.setText(PathConstructor.createFromDefaultProperty());
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

			new MetadataListener(client.subDirectory, "subdirectory", (Device) Finder.getInstance().find("observableSubdirectory"));
			new MetadataListener(client.sampleName, "samplename", (Device) Finder.getInstance().find("observableSamplename"));

		} catch (Exception e) {
			logger.warn("could not find required metadata", e);
		}

		// get events from two observables: JythonServerFacade and Analyser
		jsf = JythonServerFacade.getInstance();
		jsf.addIObserver(this);
		jsf.addScanEventObserver(this);
		jsf.addCommandThreadObserver(this);

		client.currentDirectory.setText(PathConstructor.createFromDefaultProperty());
	}

	@Override
	public void update(Object iObservable, Object arg) {
		if (client.scanStatus.isDisposed()) {
			jsf.deleteIObserver(this);
			return;
		}
		Display.getDefault().asyncExec(new Updater(arg));
	}

	private class Updater implements Runnable {
		private Object arg;
		private int currentPointNumber;

		public Updater(Object arg) {
			this.arg = arg;
		}

		private Integer multiply(Collection<Integer> c) {
			int a = 1;
			for (Integer integer : c) {
				a = a * integer;
			}
			return a;
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
			long timeRemaining = ((totalPoints - currentPointNumber - 1) * elapsed / currentPointNumber);  // converging estimate: points remaing X current average time per point
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
			if (arg != null) {
				logger.debug("run() Observable arg Class="+arg.getClass().getName());

				if (arg instanceof CommandThreadEvent) {               // use Thread Status Information to deduce when command line scan has started and stopped
					CommandThreadEvent cte = (CommandThreadEvent) arg;
					CommandThreadInfo  cti = (CommandThreadInfo) cte.getInfo();
					if (cti != null) {                                 // only command line scans have CommandThreadEventInfo
						if (cte.getEventType()== CommandThreadEventType.START) {
							clockStart();
						} else if (cte.getEventType()== CommandThreadEventType.TERMINATE) {
							clockStop();
						}
					}
				} else	if (arg instanceof ScanEvent) {             // update view related to all scans: analyser fixed/ analyser swept/command line
					ScanEvent se = (ScanEvent) arg;
					currentPointNumber = se.getCurrentPointNumber() + 1; // CurrentPointNumbers is a zero-based index
					totalScanPoints = se.getLatestInformation().getNumberOfPoints();
					ScanStatus ss = se.getLatestStatus();

					client.scanPoint.setText(String.format("%d / %d", currentPointNumber, totalScanPoints));
					client.progressBar.setSelection(10000 * currentPointNumber / totalScanPoints);
					client.scanStatus.setText(ss.toString());      // n.b. a *different* notion of scan status, the JythonServerStatus.scanStatus one, also writes into this field, see jss below
					if (ss == ScanStatus.COMPLETED_OKAY) {          // use ScanEvent to access items to update at end-of-scan
						client.scanFile.setText(lastFileName);
						client.scanNumber.setText(Integer.toString(lastScanNumber + 1)); // indicate number in file name that will be used for next scan
					}

				} else if (arg instanceof ScanDataPoint) {
					ScanDataPoint sdp = (ScanDataPoint) arg;
					lastFileName = sdp.getCurrentFilename();        // store and delay displaying until JythonServerStatus next becomes IDLE
					lastScanNumber = sdp.getScanIdentifier();
					totalScanPoints = sdp.getNumberOfPoints();
					currentPointNumber = sdp.getCurrentPointNumber();
					//only start timing from first point, or remaining time is (even more) inaccurate
					//when there is a long delay getting to the first point
					if (currentPointNumber == 0) { clockStart(); }
					logger.debug("updateElapsedTime from ScanDataPoint");
					updateElapsedTime();

				} else if (arg instanceof JythonServerStatus) {     // Running Scripts and (deprecated) CommandQueue trigger these events, Jython Console scan commands do not
					JythonServerStatus jss = (JythonServerStatus) arg;
					logger.debug("run(): JythonServerStatus {}", jss);

					switch (jss.scriptStatus) {
					case RUNNING:
						clockStart();
						break;
					case IDLE:
						clockStop();
						break;
					}

					switch (jss.scanStatus) {                       // deprecated? currently only scan=IDLE occurs
					case IDLE:
						client.scanStatus.setText("IDLE");
						client.scanPoint.setText("[0] / [0]");
						client.progressBar.setSelection(10000);
						break;
					case PAUSED:
						client.scanStatus.setText("PAUSED");
						break;
					case RUNNING:
						client.scanStatus.setText("RUNNING");
						started = new Date();
						client.elapsedTime.setText("00:00:00");
						int[] dimensions = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getDimensions();
						scandimensions.clear();
						for (Integer dim : dimensions) {
							scandimensions.add(dim);
						}
						totalScanPoints = multiply(scandimensions);
						break;
					default:
						client.scanStatus.setText("UNKNOWN");
						break;
					}
				}
			}
		}
	}
}
