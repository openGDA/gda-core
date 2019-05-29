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

package uk.ac.gda.client.live.stream.view.customui.histogram;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class DoubleVerifyListener implements VerifyListener {

	@Override
	public void verifyText(VerifyEvent e) {
		if (e.character == SWT.BS || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT
				|| e.keyCode == SWT.DEL || e.character == '.') {
			e.doit = true;
			return;
		}

		if (e.character == '\0') {
			e.doit = true;
			return;
		}

		if (e.character == '-') {
			e.doit = true;
			return;
		}
		// for scientific notation
		if (e.character == 'e' || e.character == 'E') {
			e.doit = true;
			return;
		}

		if (!('0' <= e.character && e.character <= '9')) {
			e.doit = false;
		}
	}
}
