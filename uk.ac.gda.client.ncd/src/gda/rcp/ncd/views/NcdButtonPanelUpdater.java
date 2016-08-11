/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.Device;
import gda.device.DeviceException;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.scan.Scan.ScanStatus;

//Keep title as stored in metadata in sync with title input in NcdButtonPanel
//and sample thickness
public class NcdButtonPanelUpdater implements IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(NcdStatusUpdater.class);
	private NcdButtonPanelView client;
	private String title;
	private Metadata metadata;
	private Device blaster;
	
	public NcdButtonPanelUpdater(final NcdButtonPanelView client) {
		this.client = client;
		try {
			metadata = GDAMetadataProvider.getInstance();
			title = metadata.getMetadataValue("title");
			blaster = Finder.getInstance().find("observableScanTitle");
			blaster.addIObserver(this);
			client.thicknessScannable.addIObserver(this);
			update(new Object(), new Object());
		} catch (DeviceException e) {
			logger.error("Could not read metadata (scan title)", e);
		}
	}
	
	@Override
	public void update(Object source, final Object arg) {
		updateTitle();
		if (arg instanceof ScannablePositionChangeEvent) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!client.thicknessPositionLabel.isDisposed()) {
						client.thicknessPositionLabel.setText(String.valueOf(((ScannablePositionChangeEvent) arg).newPosition));
					}
				}
			});
		} else if (arg instanceof ScanStatus) {
			ScanStatus ss = (ScanStatus) arg;
			boolean nowIdle = ss.isComplete();
			if (nowIdle) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!client.retainMetadata.isDisposed() && !client.thicknessPositionLabel.isDisposed()) {
							if (client.retainMetadata.getSelection()) {
								client.thicknessPositionLabel.setText(client.thicknessString);
							}
							client.retainMetadata.setSelection(false);
						}
					}
				});
			}
		}
	}
		
	public void updateTitle() {
		if (client.titleEntry.isDisposed()) {
			blaster.deleteIObserver(this);
			return;
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					title = metadata.getMetadataValue("title");
					if (title.equals("") && client.hasTitleString()) {
						metadata.setMetadataValue("title", client.getTitleString());
						client.setTitleString("");
					} else {
						client.titleEntry.setText(title);
					}
				} catch (DeviceException e) {
					logger.error("Could not access title metadata", e);
				}
			}
		});
		
	}

}
