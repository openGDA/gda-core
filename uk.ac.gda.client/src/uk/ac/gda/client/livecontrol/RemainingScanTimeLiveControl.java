/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.observable.IObserver;

public class RemainingScanTimeLiveControl extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(RemainingScanTimeLiveControl.class);
	private String scannableName;
	private Text positionText;

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	@Override
	public void createControl(Composite composite) {
		new Label(composite, SWT.NONE).setText("Scan time remaining");

		positionText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);

		IObserver updater = (source, arg) -> {
			if (source instanceof Scannable) {
				displayPosition(formatPosition(arg.toString()));
			}
		};

		Scannable scannable = Finder.find(scannableName);
		scannable.addIObserver(updater);
		composite.addDisposeListener(dispose -> scannable.deleteIObserver(updater));

		// set initial state
		displayPosition(getFormattedPosition(scannable));

	}

	private void displayPosition(String position) {
		if (!Thread.currentThread().equals(Display.getDefault().getThread())) {
			Display.getDefault().syncExec(() -> displayPosition(position));
			return;
		}
		positionText.setText(position);
	}

	private String getFormattedPosition(Scannable scannable) {
		try {
			return formatPosition(scannable.getPosition());
		} catch (DeviceException e) {
			logger.error("{} position could not be read", scannableName, e);
			return "Error";
		}
	}

	/**
	 * If position can be parsed to double, it is interpreted as time in seconds,
	 * and represented in {days} HH:mm:ss.
	 */
	private String formatPosition(Object rawPosition) {
		var position = rawPosition.toString();
		try {
			var etaSeconds = (long) Double.parseDouble(position);
			var eta = Duration.of(etaSeconds, ChronoUnit.SECONDS);

			String days = eta.toDaysPart() > 0 ? String.valueOf(eta.toDaysPart()) + "d " : "";
			return days + String.format("%02d:%02d:%02d", eta.toHoursPart(), eta.toMinutesPart(), eta.toSecondsPart());

		} catch (NumberFormatException nfe) {
			return position;
		}
	}

}
