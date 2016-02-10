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

package gda.device.displayscaleprovider;

import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import org.springframework.beans.factory.InitializingBean;

public class DisplayScaleProviderImpl implements DisplayScaleProvider, InitializingBean {
	// private static final Logger logger = LoggerFactory.getLogger(DisplayScaleProviderImpl.class);
	// public static final String NEWVAL = "NEWVAL";
	private ObservableComponent obsComp= new ObservableComponent();
	private double pixelsPerMMInX=100;
	private double pixelsPerMMInY=100;

	// private double cameraStagePixelsPerMMInX=50;
	// private double cameraStagePixelsPerMMInY=12.5;

	private DisplayScaleProvider currentProvider = this;

	// private Scannable keyScannable;
	// private Map<String , DisplayScaleProvider> providers;

	@Override
	public void addIObserver(IObserver anIObserver) {
		obsComp.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obsComp.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		obsComp.deleteIObservers();
	}

	@Override
	public double getPixelsPerMMInX() {
		return pixelsPerMMInX;
	}

	public void setPixelsPerMMInX(double pixelsPerMMInX) {
		this.pixelsPerMMInX = pixelsPerMMInX;
	}

	@Override
	public double getPixelsPerMMInY() {
		return pixelsPerMMInY;
	}

	public void setPixelsPerMMInY(double pixelsPerMMInY) {
		this.pixelsPerMMInY = pixelsPerMMInY;
	}

	private void getProvider() {
/*		if( keyScannable != null && providers != null){
			String key = ((String[])keyScannable.getPosition())[0];
			currentProvider= providers.get(key);
		}*/
		if (currentProvider == null)
			currentProvider = this;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		getProvider();
/*		if(keyScannable != null){
			keyScannable.addIObserver(new IObserver() {

				@Override
				public void update(Object source, Object arg) {
					try {
						getProvider();
						obsComp.notifyIObservers(DisplayScaleProviderImpl.this, NEWVAL);
					} catch (DeviceException e) {
						logger.error("Error getting display provider", e);
					}
				}
			});
		}*/
	}
}
