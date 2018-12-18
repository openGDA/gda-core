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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * A popup Dialog
 */
public class SaveDataDialog extends JOptionPane {
	JFrame frame = new JFrame("Save Data");

	private boolean saveRequired = false;

	private boolean clearRequired = false;

	/**
	 * 
	 */
	public SaveDataDialog() {
	}

	/**
	 * show the dialog
	 */
	public void showDialog() {
		int option = JOptionPane.showOptionDialog(frame, "You have not saved the data\nDo you want to output?",
				"Save Data Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] {
						"Save", "Discard", "Continue" }, "Save");

		switch (option) {
		case JOptionPane.CLOSED_OPTION:
			saveRequired = false;
			clearRequired = false;
			break;
		case 0:
			saveRequired = true;
			clearRequired = false;
			break;
		case 1:
			saveRequired = false;
			clearRequired = true;
			break;
		case 2:
		default:
			saveRequired = false;
			clearRequired = false;
			break;
		}
	}

	/**
	 * @return true if save is required
	 */
	public boolean isSaveRequired() {
		return saveRequired;
	}

	/**
	 * @return true if clear is required
	 */
	public boolean isClearRequired() {
		return clearRequired;
	}
}
