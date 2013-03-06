/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import java.awt.Dimension;

import gda.images.camera.DummyOavAwtVideoReceiver;

public class DummyOavAwtVideoReceiverTest {

	public static void main(String[] args) throws Exception {
		DummyOavAwtVideoReceiver videoReceiver = new DummyOavAwtVideoReceiver();
		
		VideoReceiverControlWindow receiverControlWindow = new VideoReceiverControlWindow();
		receiverControlWindow.setVideoReceiver(videoReceiver);
		receiverControlWindow.setLocation(700, 0);
		
		ImageListenerWindow imageDisplayWindow = new ImageListenerWindow();
		imageDisplayWindow.setVideoReceiver(videoReceiver);
		imageDisplayWindow.setSize(new Dimension(660, 526));
		
		DummyOavAwtVideoReceiverControlWindow videoReceiverParametersControlWindow = new DummyOavAwtVideoReceiverControlWindow();
		videoReceiverParametersControlWindow.setVideoReceiver(videoReceiver);
		videoReceiverParametersControlWindow.setLocation(700, 100);
		
		videoReceiver.configure();
		imageDisplayWindow.configure();
		
		new Thread(imageDisplayWindow).start();
		
		videoReceiverParametersControlWindow.setVisible(true);
		imageDisplayWindow.setVisible(true);
		receiverControlWindow.setVisible(true);
	}

}
