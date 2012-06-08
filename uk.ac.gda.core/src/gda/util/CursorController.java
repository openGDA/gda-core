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

package gda.util;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public final class CursorController {

	/** */
	public final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

	/** */
	public final static Cursor defaultCursor = Cursor.getDefaultCursor();

	private CursorController() {
	}

	/**
	 * @param component
	 * @param mainActionListener
	 * @return the actionListener
	 */
	public static ActionListener createListener(final Component component, final ActionListener mainActionListener) {
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					component.setCursor(busyCursor);
					mainActionListener.actionPerformed(ae);
				} finally {
					component.setCursor(defaultCursor);
				}
			}
		};
		return actionListener;
	}
}
