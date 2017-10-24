/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import java.util.Random;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

public class UIHelper {
	private UIHelper() {}

	public static void showError(final String message, final Exception cause) {
		showMessage(MessageDialog.ERROR, message, cause.getMessage());
	}

	public static void showError(final String message, final String reason) {
		showMessage(MessageDialog.ERROR, message, reason);
	}

	public static void showWarning(final String message, final String reason) {
		showMessage(MessageDialog.WARNING, message, reason);
	}

	public static void showWarning(final String message, final Exception cause) {
		showMessage(MessageDialog.WARNING, message, cause.getMessage());
	}

	private static void showMessage(final int messageDialogType, final String message, final String reason) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				StringBuilder messageString = new StringBuilder();
				messageString.append(message);
				if (reason != null) {
					messageString.append("\n\nReason:\n" + reason);
				}
				if (messageDialogType == MessageDialog.ERROR) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", messageString.toString());
				} else if (messageDialogType == MessageDialog.WARNING) {
					MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Warning", messageString.toString());
				}
			}
		});
	}

	public static GridLayout createGridLayoutWithNoMargin(int columns, boolean equal) {
		GridLayout layout = new GridLayout(columns, equal);
		layout.marginBottom = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginTop = 0;
		return layout;
	}

	public static void revalidateLayout(Control control) {
		Control c = control;
		do {
			if (c instanceof ExpandBar) {
				ExpandBar expandBar = (ExpandBar) c;
				for (ExpandItem expandItem : expandBar.getItems()) {
					expandItem
					.setHeight(expandItem.getControl().computeSize(expandBar.getSize().x, SWT.DEFAULT, true).y);
				}
			}
			c = c.getParent();
		} while (c != null && c.getParent() != null && !(c instanceof ScrolledComposite));
		if (c instanceof ScrolledComposite) {
			ScrolledComposite scrolledComposite = (ScrolledComposite) c;
			if (scrolledComposite.getExpandHorizontal() || scrolledComposite.getExpandVertical()) {
				scrolledComposite
				.setMinSize(scrolledComposite.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
			} else {
				scrolledComposite.getContent().pack(true);
			}
		}
		if (c instanceof Composite) {
			Composite composite = (Composite) c;
			composite.layout(true, true);
		}
	}

	public static Color convertHexadecimalToColor(String hexadecimal, Display display) throws NumberFormatException {
		java.awt.Color col=null;
		try{
			col=java.awt.Color.decode(hexadecimal);
		}
		catch (Exception e) {
			col=java.awt.Color.WHITE;
		}
		int red=col.getRed();
		int blue=col.getBlue();
		int green=col.getGreen();

		return new Color(display, new RGB(red, green, blue));
	}

	public static String convertRGBToHexadecimal(RGB rgb) {
		int red = rgb.red;
		int green = rgb.green;
		int blue = rgb.blue;
		String redHexadecimal = Integer.toHexString(red);
		String greenHexadecimal = Integer.toHexString(green);
		String blueHexadecimal = Integer.toHexString(blue);
		if (redHexadecimal.length() == 1) {
			redHexadecimal = "0" + redHexadecimal;
		}
		if (greenHexadecimal.length() == 1) {
			greenHexadecimal = "0" + greenHexadecimal;
		}
		if (blueHexadecimal.length() == 1) {
			blueHexadecimal = "0" + blueHexadecimal;
		}
		return "#" + redHexadecimal + greenHexadecimal + blueHexadecimal;
	}

	public static String getRandomColor() {
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		String hex = Integer.toHexString(new java.awt.Color(r,g,b).getRGB() & 0xffffff);
		if (hex.length() < 6) {
		    hex = "0" + hex;
		}
		hex = "#" + hex;
		return hex;
	}
}