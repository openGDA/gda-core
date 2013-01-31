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

package uk.ac.gda.exafs.ui.views.scalersmonitor;

import gda.device.scannable.IQexafsScannableState;
import gda.factory.Finder;

import org.eclipse.ui.PlatformUI;


public class B18ScalersMonitorView extends ScalersMonitorView {
	@SuppressWarnings("hiding")
	public static final String ID = "uk.ac.gda.exafs.ui.views.B18ScalersMonitor"; //$NON-NLS-1$
	private String qexafsState;
	//private IQexafsScannableState qexafsScannableState;

	
	@Override
	public void run() {
		//qexafsScannableState = Finder.getInstance().find("qexafs_energy");
		while (keepOnTrucking) {
			if (!runMonitoring || !amVisible) {
				try {
					if (!keepOnTrucking) {
						return;
					}
					Thread.sleep(1000);
					if (!keepOnTrucking) {
						return;
					}
				} catch (InterruptedException e) {
					// end the thread
					return;
				}
			} else {

				//qexafsState = qexafsScannableState.getState();
				if (qexafsState.equals("idle")) {
					final Double[] values;
					final Double[] xspressStats;
					try {
						values = getIonChamberValues();
						xspressStats = getFluoDetectorCountRatesAndDeadTimes();
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
						runMonitoring = false;
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								btnRunPause.setImageDescriptor(runImage);
							}
						});
						continue;
					}

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							displayData.setI0(values[0]);
							displayData.setIt(values[1]);
							displayData.setIref(values[2]);
							double ItI0 = Math.log(values[0] / values[1]);
							displayData.setItI0(ItI0);
							double IrefIt = Math.log(values[2] / values[1]);
							displayData.setIrefIt(IrefIt);
							updateXspressGrid(xspressStats, values);
						}

					});
				}

				try {
					if (!keepOnTrucking) {
						return;
					}
					Thread.sleep((long) (refreshRate * 1000));
					if (!keepOnTrucking) {
						return;
					}
				} catch (InterruptedException e) {
					// end the thread
					return;
				}

			}
		}
	}
}
