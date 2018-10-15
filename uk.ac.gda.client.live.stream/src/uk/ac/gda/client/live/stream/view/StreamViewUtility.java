/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

public class StreamViewUtility {

	private static Text errorText;

	private StreamViewUtility() {
	    throw new IllegalStateException("Utility class");
	  }

	public static void displayAndLogError(Logger logger, final Composite parent, final String errorMessage) {
		displayAndLogError(logger, parent, errorMessage, null);
	}

	public static void displayAndLogError(Logger logger, final Composite parent, final String errorMessage, final Throwable throwable) {
		logger.error(errorMessage, throwable);
		if (errorText == null) {
			errorText = new Text(parent, SWT.LEFT | SWT.WRAP | SWT.BORDER);
			errorText.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					errorText.dispose();
					parent.layout(true);
					errorText = null;
				}
			});
			errorText.setToolTipText("Double click this message to remove it.");
			parent.layout(true);
		}
		final StringBuilder s = new StringBuilder(errorText.getText());
		s.append("\n").append(errorMessage);
		if (throwable != null) {
			s.append("\n\t").append(throwable.getMessage());
		}
		errorText.setText(s.toString());
	}
}
