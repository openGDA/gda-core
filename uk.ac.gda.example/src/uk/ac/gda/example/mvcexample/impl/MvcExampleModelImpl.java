/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.example.mvcexample.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.ScannableStatus;
import gda.device.scannable.ScannableUtils;
import gda.observable.IObserver;
import uk.ac.gda.beans.ObservableModel;
import uk.ac.gda.client.observablemodels.ScannableWrapper;
import uk.ac.gda.example.mvcexample.MvcExampleItem;
import uk.ac.gda.example.mvcexample.MvcExampleModel;

public class MvcExampleModelImpl  extends ObservableModel  implements MvcExampleModel , InitializingBean{
	private static final double SELECTED_BOUNDARY = 100.;
	private static final Logger logger = LoggerFactory.getLogger(MvcExampleModelImpl.class);
	ScannableWrapper wrapper;

	@Override
	public boolean isSelected() throws DeviceException {
		return getPosition()>SELECTED_BOUNDARY;
	}

	@Override
	public void setSelected(boolean selected) throws DeviceException {
		setPosition(selected?(SELECTED_BOUNDARY+1.) : 0.);//selected means > SELECTED_BOUNDARY
	}

	@Override
	public double getPosition() throws DeviceException {
		return ScannableUtils.getCurrentPositionArray(scannable)[0];
	}

	@Override
	public void setPosition(double position) throws DeviceException {
		scannable.asynchronousMoveTo(position);
	}
	ScannableMotionUnits scannable;



	public ScannableMotionUnits getScannable() {
		return scannable;
	}

	public void setScannable(ScannableMotionUnits scannable) {
		this.scannable = scannable;
	}

	@Override
	public ScannableWrapper getScannableWrapper() throws Exception {
		if (wrapper == null) {
			wrapper = new ScannableWrapper(scannable);
		}
		return wrapper;
	}
	WritableList<MyMvcExampleItem> items;
	@Override
	public WritableList<MyMvcExampleItem> getItems() {
		if( items == null) {
			ArrayList<MyMvcExampleItem> list = new ArrayList<>();
			items = new WritableList<>(list, MvcExampleItem.class);
			Timer timer = new Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if(items.size()< 20)
								items.add(new MyMvcExampleItem( (new Date()).toString(), 0.));
							for( int i=0; i < items.size(); i++) {
								MyMvcExampleItem item = items.get(i);
								double value = item.getValue();
								if( value > 15) {
									item.setValue(value+1);
								}
							}
					}});
				}
			};
			timer.scheduleAtFixedRate(task, 0, 1000);

		}
		return items;
	}

	@Override
	public void afterPropertiesSet() {
		scannable.addIObserver(new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				if( arg instanceof ScannablePositionChangeEvent) {

					Serializable newPosition = ((ScannablePositionChangeEvent)arg).newPosition;
					firePropertyChange(MvcExampleModel.POSITION_PROPERTY_NAME,
							newPosition, newPosition);
				}
				else if( arg instanceof ScannableStatus) {

					try {
						Double newPosition;
						newPosition = getPosition();
						firePropertyChange(MvcExampleModel.POSITION_PROPERTY_NAME,
								null, newPosition);
						firePropertyChange(MvcExampleModel.SELECTED_PROPERTY_NAME,
								null, (newPosition > SELECTED_BOUNDARY));
					} catch (DeviceException e) {
						logger.error("Error handling Scannable Status from "+scannable.getName() , e);
					}
				}
			}
		});
	}



}



class MyMvcExampleItem extends ObservableModel implements MvcExampleItem {

	double value;
	@Override
	public double getValue() {
		return value;
	}


	public void setValue(double newVal){
		double oldVal = this.value;
		this.value = newVal;
		firePropertyChange(MvcExampleItem.VALUE_PROPERTY_NAME, oldVal, newVal);
	}

	String name;
	@Override
	public String getName() {
		return name;
	}


	public MyMvcExampleItem(String name, double value) {
		super();
		this.value = value;
		this.name = name;
	}
}

