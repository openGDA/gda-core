/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.swing.ncd;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a JPanel to control a beamline shutter or valve (EpicsPneumatic)
 * <p>
 * The JPanel will have a frame with the shutter's findable name, a status JLabel as well as a button to operate the
 * shutter. Only the reasonable action is available to the user, see status2action To use just add to an existing panel
 * and be sure to configure the shutterName via setShutterName and call configure() after that.
 * <p>
 * The implementation is lazy: There is a getPosition() in the update below, to circumvent changes to the Corba
 * interface. Don't be too surprised if you are doing funny things with this class and it breaks. You have been warned
 * now.
 * 
 */

public class ShutterPanel extends JPanel implements IObserver, Runnable,Configurable, Findable {
	private static final Logger logger = LoggerFactory.getLogger(ShutterPanel.class);

	private String shutterName;
	private JButton operateButton;
	private JLabel statusLabel;
	private String status = "Fault";
	private EnumPositioner shutter;
	public EnumPositioner getShutter() {
		return shutter;
	}

	public void setShutter(EnumPositioner shutter) {
		this.shutter = shutter;
	}

	private Timer timer;

	private static final Map<String, String> status2action = new HashMap<String, String>() {
		{
			put("Open", "Close");
			put("Closed", "Open");
			put("Fault", "Reset");
			put("Opening", "Wait");
			put("Closing", "Wait");
		}
	};

	/**
	 * Constructor.
	 */
	public ShutterPanel() {
		timer = new Timer(1000, null);
	}

	/**
	 * 
	 */
	@Override
	public void configure() {
		if (shutter == null) {
			if (shutterName == null)
				return;
		}
		try {
			if (shutter == null) {
				shutter = (EnumPositioner) Finder.getInstance().findNoWarn(shutterName);
				if (shutter == null) {
					return;
				}
			}
			setLayout(new FlowLayout());
			statusLabel = new JLabel("Fault");
			statusLabel.setOpaque(true);
			statusLabel.setBackground(Color.lightGray);
			operateButton = new JButton("Wait");
			operateButton.setToolTipText("Operate Shutter (" + shutter.getName() + ")");
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), shutter.getName(),
					TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

			operateButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					try {
						// do what is says on the tin
						String action = ev.getActionCommand();
						
						logger.debug("shutter action requested: "+action);
						
						if (action.equals("Wait")) {
							return;
						}
						// to refresh the PSS we may need to reset
//						shutter.moveTo("Reset");
//						gda.util.Sleep.sleep(100);
						// don't reset twice
						if (!action.equals("Reset")) {
							shutter.moveTo(action);
						}
					} catch (DeviceException e) {
						logger.warn("could not operate " + shutter.getName() + ": ", e);
					}
				}
			});
			add(statusLabel);
			add(operateButton);

			shutter.addIObserver(this);
			timer.addActionListener(new TimerListener(statusLabel));
			update(this, shutter);
			uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()).start();
		} catch (Exception e) {
			// if the shutter is not found the JPanel will be empty
			logger.warn("exception configuring {}: {}", shutter.getName(), e);
		}
	}

	private class TimerListener implements ActionListener {
		private JLabel label;
		private Color backGround;
		private Color foreGround;
		private boolean isForeGround = true;

		/**
		 * @param label
		 */
		public TimerListener(JLabel label) {
			this.label = label;
			foreGround = label.getForeground();
			backGround = label.getBackground();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isForeGround) {
				label.setForeground(foreGround);
			} else {
				label.setForeground(backGround);
			}
			isForeGround = !isForeGround;
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		try {
			// TODO I agree it is not too good requesting more information in the update
			// we should improve the changeCode
			status = shutter.getPosition().toString();

			statusLabel.setText(status);
			if (!"Open".equals(status)) {
				statusLabel.setBackground(Color.RED);
				if ("i11".equalsIgnoreCase(LocalProperties.get("gda.factory.factoryName"))) {
					timer.start();
				}
			} else {
				timer.stop();
				statusLabel.setBackground(Color.lightGray);
				statusLabel.setForeground(Color.BLACK);
			}

			String tmp = status2action.get(status);
			if (tmp != null) {
				operateButton.setText(tmp);
			} else {
				// something fishy
				operateButton.setText("Reset");
			}

		} catch (DeviceException e) {
			logger.warn("could not get status for " + shutter.getName() + ": ", e);
		}
	}

	/**
	 * @return shutter name
	 */
	public String getShutterName() {
		return shutterName;
	}

	/**
	 * @param shutterName
	 */
	public void setShutterName(String shutterName) {
		this.shutterName = shutterName;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			// update every so long, in case an EPICS update is lost
			try {
				Thread.sleep(12345);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("Thread interrupted while waiting to update ShutterPanel", e);
				return;
			}
			update(null, null);
		}
	}
}