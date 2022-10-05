/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * A listener that can be added as a listener to a subscriber to the beans
 * published on a topic.
 */
class TestBeanListener implements IBeanListener<StatusBean> {

	private List<StatusBean> beansReceived = Collections.synchronizedList(new ArrayList<>());

	private List<String> listenerStartEndEvents = Collections.synchronizedList(new ArrayList<>());

	private final CountDownLatch countdown;

	long sleepTime = 50;

	public TestBeanListener(int numBeansExpected) {
		countdown = new CountDownLatch(numBeansExpected);
	}

	@Override
	public void beanChangePerformed(BeanEvent<StatusBean> event) {
		beansReceived.add(event.getBean());
		listenerStartEndEvents.add(event.getBean().getName() + " start");
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			listenerStartEndEvents.add(event.getBean().getName() + " end");
			countdown.countDown();
		}
	}

	public List<StatusBean> getBeansReceived() {
		return beansReceived;
	}

	public List<String> getListenerStartEndEvents() {
		return listenerStartEndEvents;
	}

	public void awaitBeans() throws InterruptedException {
		countdown.await(1, TimeUnit.SECONDS);
		Thread.sleep(100); // extra sleep to check there aren't more beans than expected
	}

	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

}