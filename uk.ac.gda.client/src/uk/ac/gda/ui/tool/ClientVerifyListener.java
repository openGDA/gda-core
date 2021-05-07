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

	public static final VerifyListener verifyOnlyIntegerText = verifyDigitText(true, false);
	public static final VerifyListener verifyOnlyPositiveIntegerText = verifyDigitText(true, true);
	public static final VerifyListener verifyOnlyDoubleText = verifyDigitText(false, false);
	public static final VerifyListener verifyOnlyPositiveDoubleText = verifyDigitText(false, true);


//	private static VerifyListener verifyDigitText(boolean integerOnly) {
//		return verifyDigitText(integerOnly, false);
//	}

	private static VerifyListener verifyDigitText(boolean integerOnly, boolean positiveOnly) {
		return e -> {
			Text widget = Text.class.cast(e.widget);
			String currentText = widget.getText().trim();
			String newText = (currentText.substring(0, e.start) + e.text + currentText.substring(e.end)).trim();

			if (stringIsNumber(newText, integerOnly, positiveOnly)) {
				if (newText.isEmpty()) {
					widget.setText("0.0");
					if (integerOnly) {
						widget.setText("0");
					}
					WidgetUtilities.hideDecorator(widget);
					return;
				}
				return;
			}
			e.doit = false;
		};
	}

	public static final boolean stringIsDoubleNumber(String text) {
		return stringIsNumber(text, false, false);
	}

	private static final boolean stringIsNumber(String text, boolean integerOnly, boolean positiveOnly) {
		var signed = "[+-]?";
		var positiveOnlyRegEx = "[+]?";
		var integerRegEx = "\\d*";
		var doubleRegEx = "(\\d*)?(\\.)?(\\d*)?";

		String matchSign = positiveOnly ? positiveOnlyRegEx : signed;
		String matchType = integerOnly ? integerRegEx : doubleRegEx;

		var regEx = String.format("\\s*?%s(%s)\\s*", matchSign, matchType);

		return text.matches(regEx);
	}
}
