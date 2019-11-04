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

package uk.ac.gda.ui.tool;

import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

public final class ClientVerifyListener {

	/**
	 * Hides public constructor
	 */
	private ClientVerifyListener() {
	}

	public static final VerifyListener verifyOnlyIntegerText = verifyDigitText(true);
	public static final VerifyListener verifyOnlyDoubleText = verifyDigitText(false);


	private static VerifyListener verifyDigitText(boolean integerOnly) {
		return e -> {
			Text widget = Text.class.cast(e.widget);
			String currentText = widget.getText().trim();
			String newText = (currentText.substring(0, e.start) + e.text + currentText.substring(e.end));
			if (stringIsNumber(newText.trim(), integerOnly)) {
				if (newText.trim().isEmpty()) {
					widget.setText("0");
				}
				WidgetUtilities.hideDecorator(widget);
				return;
			}
//			widget.setToolTipText(TomographyMessagesUtility.getMessage(TomographyMessages.ONLY_NUMBERS_ALLOWED));
//			WidgetUtilities.addErrorDecorator(widget,
//					TomographyMessagesUtility.getMessage(TomographyMessages.ONLY_NUMBERS_ALLOWED)).show();
			e.doit = false;
		};
	}

	private static final boolean stringIsNumber(String text, boolean integerOnly) {
		if (text == null) {
			return false;
		}
		boolean isNumber = integerOnly ? stringIsIntegerNumber(text) : stringIsDoubleNumber(text);
		return text.isEmpty() || isNumber;
	}

	public static final boolean stringIsDoubleNumber(String text) {
		return text.matches("\\d*\\.\\d*") || stringIsIntegerNumber(text);
	}

	private static final boolean stringIsIntegerNumber(String text) {
		return text.matches("\\d+");
	}
}
