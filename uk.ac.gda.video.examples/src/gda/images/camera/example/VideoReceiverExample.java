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

package gda.images.camera.example;

import java.util.Map;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.images.camera.ImageListener;
import gda.images.camera.VideoReceiver;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Demonstrates the use of a {@link VideoReceiver} and {@link ImageListener}s.
 */
public class VideoReceiverExample {

	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("gda/images/camera/example/beans.xml");
		configureAllConfigurablesInApplicationContext(context);
		startAndOpenAllImageListenerWindowsInContext(context);
		openAllVideoReceiverControlWindows(context);
	}
	
	private static void configureAllConfigurablesInApplicationContext(ApplicationContext context) throws FactoryException {
		Map<String, Configurable> configurables = context.getBeansOfType(Configurable.class);
		for (Configurable c : configurables.values()) {
			c.configure();
		}
	}
	
	private static void startAndOpenAllImageListenerWindowsInContext(ApplicationContext context) {
		Map<String, ImageListenerWindow> listeners = context.getBeansOfType(ImageListenerWindow.class);
		for (ImageListenerWindow w : listeners.values()) {
			new Thread(w).start();
			w.setVisible(true);
		}
	}
	
	private static void openAllVideoReceiverControlWindows(ApplicationContext context) {
		Map<String, VideoReceiverControlWindow> windows = context.getBeansOfType(VideoReceiverControlWindow.class);
		for (VideoReceiverControlWindow w : windows.values()) {
			w.setVisible(true);
		}
	}
	
}
