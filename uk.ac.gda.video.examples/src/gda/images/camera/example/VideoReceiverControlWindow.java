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

import gda.images.camera.VideoReceiver;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * Window that allows a {@link VideoReceiver} to be stopped and started.
 */
public class VideoReceiverControlWindow extends JFrame {
	
	public VideoReceiverControlWindow() {
		super("VideoReceiverControlWindow");
		createComponents();
		wireEvents();
		startButton.setEnabled(false);
	}
	
	protected VideoReceiver<?> videoReceiver;
	
	public void setVideoReceiver(VideoReceiver<?> videoReceiver) {
		this.videoReceiver = videoReceiver;
	}
	
	protected JButton startButton;
	protected JButton stopButton;
	
	private void createComponents() {
		setLayout(new FlowLayout());
		startButton = new JButton("Start");
		stopButton = new JButton("Stop");
		add(startButton);
		add(stopButton);
		pack();
	}
	
	private void wireEvents() {
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startButton.setEnabled(false);
				videoReceiver.createConnection();
				stopButton.setEnabled(true);
			}
		});
		
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopButton.setEnabled(false);
				videoReceiver.closeConnection();
				startButton.setEnabled(true);
			}
		});
	}
	
	@Override
	public void setLocation(Point p) {
		super.setLocation(p);
	}
	
}
