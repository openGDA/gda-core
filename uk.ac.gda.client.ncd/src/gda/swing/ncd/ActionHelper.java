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

package gda.swing.ncd;

import gda.device.Timer;
import gda.device.TimerStatus;
import gda.observable.IObserver;
import gda.rcp.ncd.NcdController;

import java.awt.Toolkit;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionHelper implements IObserver {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ActionHelper.class);
	private static ActionHelper instance = new ActionHelper();
	private List<NcdButtonPanel> updateList = new Vector<NcdButtonPanel>();
	private Boolean oldIdle = null;
	
	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return NcdController
	 */
	public static ActionHelper getInstance() {
		return instance;
	}

	/**
	 * private constructor to ensure we are a singleton
	 */
	private ActionHelper() {
		Timer tfg = NcdController.getInstance().getTfg();
		if (tfg != null) {
			tfg.addIObserver(this);
		}
	}

	/**
	 * NcdBase panels register with us
	 * 
	 * @param ncdBasePanel
	 */
	public void register(NcdButtonPanel ncdBasePanel) {
		updateList.add(ncdBasePanel);
	}

	/**
	 * NcdBase panels will hardly ever unregister
	 * 
	 * @param ncdBasePanel
	 */
	public void unregister(NcdButtonPanel ncdBasePanel) {
		updateList.remove(ncdBasePanel);
	}

	/**
	 * disables the collection buttons in the NcdBasePanels
	 */
	public void disableCollection() {
		for (NcdButtonPanel ncdBasePanel : updateList) {
			ncdBasePanel.disableCollection();
		}
	}

	/**
	 * enables the collection buttons in the NcdBasePanels
	 */
	public void enableCollection() {
		for (NcdButtonPanel ncdBasePanel : updateList) {
			ncdBasePanel.collectionComplete();
		}
	}

	/**
	 * sets the buttons in NcdBasePanel to reflect the outputting state
	 */
	public void outputStart() {
		for (NcdButtonPanel ncdBasePanel : updateList) {
			ncdBasePanel.outputStart();
		}
	}

	/**
	 * sets the buttons in NcdBasePanel to reflect the state of a complete output
	 */
	public void outputComplete() {
		for (NcdButtonPanel ncdBasePanel : updateList) {
			ncdBasePanel.outputComplete();
		}
	}

	/**
	 * @param observer
	 */
	public void addObservability(IObserver observer) {
		for (NcdButtonPanel ncdBasePanel : updateList) {
			ncdBasePanel.addObservability(observer);
		}
	}

	@Override
	public void update(Object iObservable, Object arg) {
		if (arg != null && arg instanceof TimerStatus) {
			Boolean newIdle = ((TimerStatus) arg).getCurrentStatus().equals("IDLE");
			
			if (newIdle.equals(oldIdle)) return;
			
			if (newIdle) {

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Toolkit.getDefaultToolkit().beep();
						for (NcdButtonPanel ncdBasePanel : updateList) {
							ncdBasePanel.collectionComplete();
						}
					}
				});

			} else {
				// Tfg state not idle - disable GUI collection

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (NcdButtonPanel ncdBasePanel : updateList) {
							ncdBasePanel.collectionStart();
						}
					}
				});
			}
			
			oldIdle = newIdle;
		}
	}
}