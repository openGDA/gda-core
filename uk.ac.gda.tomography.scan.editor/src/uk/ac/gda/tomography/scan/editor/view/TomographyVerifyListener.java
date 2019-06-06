/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view;

import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

public final class TomographyVerifyListener {

	/**
	 * Hides public constructor
	 */
	private TomographyVerifyListener() {
	}

	public static final VerifyListener verifyOnlyDigitText = e -> {
		Text widget = Text.class.cast(e.widget);
		String currentText = widget.getText();
		String newText = (currentText.substring(0, e.start) + e.text + currentText.substring(e.end));
		if (stringIsNumber(newText)) {
			widget.setToolTipText("");
			WidgetUtilities.hideDecorator(widget);
			return;
		}
		widget.setToolTipText(TomographyMessagesUtility.getMessage(TomographyMessages.ONLY_NUMBERS_ALLOWED));
		WidgetUtilities.addErrorDecorator(widget,
				TomographyMessagesUtility.getMessage(TomographyMessages.ONLY_NUMBERS_ALLOWED)).show();
		e.doit = false;
	};

	public static final boolean stringIsNumber(String text) {
		if (text == null) {
			return false;
		}
		return (text.isEmpty() || text.matches("\\d*\\.\\d*") || text.matches("\\d*"));
	}
}
