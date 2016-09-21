/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator.example;

import java.util.Random;

import org.eclipse.richbeans.api.generator.IListenableProxyFactory;

import uk.ac.diamond.daq.guigenerator.ListenableProxyFactory;

/**
 * An example demonstrating two-way data binding. The test object is a bean with property change support. When it is
 * created, a background thread is started which checks the value of the "update" boolean. When <code>true</code>
 * (caused by the user selecting the check box), it periodically updates the X and Y values in the bean, and the
 * data binding automatically propagates the changes to the GUI.
 */
public class UpdatingExample extends GuiGeneratorRunnerBase<UpdatingBean> {
	private final IListenableProxyFactory proxyFactory = new ListenableProxyFactory();

	public static void main(String[] args) {
		new UpdatingExample().run();
	}

	@Override
	public UpdatingBean createTestObject() {
		UpdatingBean updatingBean = proxyFactory.createProxyFor(new UpdatingBeanImpl(), UpdatingBean.class);
		updatingBean.setX(Double.POSITIVE_INFINITY);
		updatingBean.setY(Double.POSITIVE_INFINITY);
		new UpdaterThread(updatingBean).start();
		return updatingBean;
	}

	@Override
	protected String getWindowTitle() {
		return "Two-way data binding example";
	}

	private class UpdaterThread extends Thread {

		private UpdatingBean updatingBean;

		public UpdaterThread(UpdatingBean updatingBean) {
			super();
			this.updatingBean = updatingBean;
			setDaemon(true);
		}

		@Override
		public void run() {
			Random random = new Random();
			while (true) {
				try {
					sleep(1000);
					if (updatingBean.isUpdate()) {
						updatingBean.setX(random.nextGaussian() * 1000.0);
						updatingBean.setY(random.nextGaussian() * 1000.0);
					}
				} catch (InterruptedException e) {
					interrupt();
					return;
				}

			}
		}
	}
}
