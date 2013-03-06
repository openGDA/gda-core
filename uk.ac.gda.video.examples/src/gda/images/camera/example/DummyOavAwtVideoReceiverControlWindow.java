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

import java.awt.GridLayout;

import gda.images.camera.DummyOavAwtVideoReceiver;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DummyOavAwtVideoReceiverControlWindow extends JFrame {
	
	protected DummyOavAwtVideoReceiver videoReceiver;
	
	public void setVideoReceiver(DummyOavAwtVideoReceiver videoReceiver) {
		this.videoReceiver = videoReceiver;
	}
	
	public DummyOavAwtVideoReceiverControlWindow() {
		createComponents();
		pack();
		wireEvents();
	}
	
	private JSpinner zoomSpinner;
	
	private JSpinner gtabxSpinner;
	private JSpinner gtabySpinner;
	private JSpinner gtabzSpinner;
	
	private JSpinner gonioySpinner;
	private JSpinner goniozSpinner;
	
	private JSpinner omegaSpinner;
	
	private void createComponents() {
		setLayout(new GridLayout(7, 2, 5, 5));
		
		zoomSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
		
		gtabxSpinner = new JSpinner(new SpinnerNumberModel(0f, -10, 10, 1));
		gtabySpinner = new JSpinner(new SpinnerNumberModel(0f, -10, 10, 1));
		gtabzSpinner = new JSpinner(new SpinnerNumberModel(0f, -10, 10, 1));
		
		gonioySpinner = new JSpinner(new SpinnerNumberModel(0f, -1000, 1000, 10));
		goniozSpinner = new JSpinner(new SpinnerNumberModel(0f, -1000, 1000, 10));
		
		omegaSpinner = new JSpinner(new SpinnerNumberModel(0f, 0, 360, 1));
		
		add(new JLabel("Zoom:"));
		add(zoomSpinner);
		add(new JLabel("GTABX:"));
		add(gtabxSpinner);
		add(new JLabel("GTABY:"));
		add(gtabySpinner);
		add(new JLabel("GTABZ:"));
		add(gtabzSpinner);
		add(new JLabel("GONIOY:"));
		add(gonioySpinner);
		add(new JLabel("GONIOZ:"));
		add(goniozSpinner);
		add(new JLabel("Omega:"));
		add(omegaSpinner);
	}
	
	private void wireEvents() {
		zoomSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int zoom = (Integer) zoomSpinner.getValue();
				videoReceiver.setZoomLevel(zoom);
			}
		});
		
		gtabxSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double gtabx = (Double) gtabxSpinner.getValue();
				videoReceiver.setGtabx(gtabx);
			}
		});
		
		gtabySpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double gtaby = (Double) gtabySpinner.getValue();
				videoReceiver.setGtaby(gtaby);
			}
		});
		
		gtabzSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double gtabz = (Double) gtabzSpinner.getValue();
				videoReceiver.setGtabz(gtabz);
			}
		});
		
		gonioySpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double gtaby = (Double) gonioySpinner.getValue();
				videoReceiver.setGonioy(gtaby);
			}
		});
		
		goniozSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double gtabz = (Double) goniozSpinner.getValue();
				videoReceiver.setGonioz(gtabz);
			}
		});
		
		omegaSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double omega = (Double) omegaSpinner.getValue();
				videoReceiver.setOmega(omega);
			}
		});
	}

}
