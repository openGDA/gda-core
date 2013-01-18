/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.composites;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.TomoClientActivator;

public class ButtonSelectionUtil {

	public static final Color BUTTON_DE_SELECTION_FOREGROUND = ColorConstants.listForeground;
	public static final Color BUTTON_DE_SELECTION_BACKGROUND = ColorConstants.listBackground;

	public static final Color BUTTON_SELECTION_FOREGROUND = ColorConstants.black;
	public static final Color BUTTON_SELECTION_BACKGROUND = ColorConstants.green;

	public static final Color CTRL_BUTTON_DE_SELECTION_FOREGROUND = BUTTON_DE_SELECTION_FOREGROUND;// ColorConstants.white;
	public static final Color CTRL_BUTTON_DE_SELECTION_BACKGROUND = BUTTON_DE_SELECTION_BACKGROUND;// ColorConstants.buttonDarkest;

	public static final Color CTRL_BUTTON_SELECTION_FOREGROUND = BUTTON_SELECTION_FOREGROUND;// ColorConstants.white;
	public static final Color CTRL_BUTTON_SELECTION_BACKGROUND = BUTTON_SELECTION_BACKGROUND;// ColorConstants.lightGray;

	/**
	 * Method to run in the UI thread to set the colors to show the button selected
	 * 
	 * @param btnCntrl
	 */
	public static void setButtonSelected(final Button btnCntrl) {
		btnCntrl.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				btnCntrl.setForeground(BUTTON_SELECTION_FOREGROUND);
				btnCntrl.setBackground(BUTTON_SELECTION_BACKGROUND);

			}
		});
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button de-selected
	 * 
	 * @param btnCntrl
	 */
	public static void setButtonDeselected(final Button btnCntrl) {
		if (btnCntrl != null && !btnCntrl.isDisposed()) {
			btnCntrl.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					btnCntrl.setForeground(BUTTON_DE_SELECTION_FOREGROUND);
					btnCntrl.setBackground(BUTTON_DE_SELECTION_BACKGROUND);
				}
			});
		}
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button selected
	 * 
	 * @param btnCntrl
	 */
	public static void setControlButtonSelected(final Button btnCntrl) {
		btnCntrl.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				btnCntrl.setForeground(CTRL_BUTTON_SELECTION_FOREGROUND);
				btnCntrl.setBackground(CTRL_BUTTON_SELECTION_BACKGROUND);
			}
		});
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button de-selected
	 * 
	 * @param btnCntrl
	 */
	public static void setControlButtonDeselected(final Button btnCntrl) {
		if (btnCntrl != null && !btnCntrl.isDisposed()) {
			btnCntrl.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					btnCntrl.setForeground(CTRL_BUTTON_DE_SELECTION_FOREGROUND);
					btnCntrl.setBackground(CTRL_BUTTON_DE_SELECTION_BACKGROUND);
				}
			});
		}
	}

	public static void decorateControlButton(final Button btnCntrl) {
		btnCntrl.setForeground(CTRL_BUTTON_DE_SELECTION_FOREGROUND);
		btnCntrl.setBackground(CTRL_BUTTON_DE_SELECTION_BACKGROUND);
		btnCntrl.setImage(TomoClientActivator.getDefault().getImageRegistry().get(ImageConstants.ICON_CTRL_BTN));
	}

	/**
	 * Returns <code>true</code> if the colors as set when selected.
	 * 
	 * @param button
	 * @return true when background color is lightgray and foreground color is red - this is what was set when the
	 *         widget was selected.
	 */
	public static boolean isButtonSelected(final Button button) {
		final boolean[] isSelected = new boolean[1];
		if (button != null && !button.isDisposed()) {
			button.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (BUTTON_SELECTION_FOREGROUND.equals(button.getForeground())
							&& BUTTON_SELECTION_BACKGROUND.equals(button.getBackground())) {
						isSelected[0] = true;
					} else {
						isSelected[0] = false;
					}
				}
			});
		}
		return isSelected[0];
	}

	/**
	 * Returns <code>true</code> if the colors as set when selected.
	 * 
	 * @param button
	 * @return true when background color is lightgray and foreground color is red - this is what was set when the
	 *         widget was selected.
	 */
	public static boolean isCtrlButtonSelected(final Button button) {
		final boolean[] isSelected = new boolean[1];
		if (button != null && !button.isDisposed()) {
			button.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (CTRL_BUTTON_SELECTION_FOREGROUND.equals(button.getForeground())
							&& CTRL_BUTTON_SELECTION_BACKGROUND.equals(button.getBackground())) {
						isSelected[0] = true;
					} else {
						isSelected[0] = false;
					}
				}
			});
		}
		return isSelected[0];
	}

}
