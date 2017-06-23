/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hplc.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.richbeans.xml.XMLBeanContentDescriber;

import uk.ac.gda.devices.hplc.beans.HplcSessionBean;

public final class HplcSessionBeanDescriber extends XMLBeanContentDescriber {

	@Override
	public String getBeanName() {
		return HplcSessionBean.class.getName();
	}
	
	@Override
	public String getEditorId() {
		return "uk.ac.gda.devices.hplc.beans.HplcSessionBeanEditor";
	}
	
	@Override
	public int describe(Reader contents, IContentDescription description) throws IOException {

		final BufferedReader reader = new BufferedReader(contents);
		try {
			// TODO Use castor to read the file and use instanceof
			// on the bean type returned.
			@SuppressWarnings("unused")
			final String titleLine = reader.readLine(); // unused.
			final String tagLine = reader.readLine();
			
			String beanName = getBeanName();
			final String tagName = beanName.substring(beanName.lastIndexOf(".")+1);

			if (tagLine != null) {
				if (tagLine.trim().equalsIgnoreCase("<" + tagName + ">")
						|| tagLine.trim().equalsIgnoreCase("<" + tagName + "/>")) {
					return IContentDescriber.VALID;
				}
			} else {
				return IContentDescriber.VALID;
			}
			return IContentDescriber.INVALID;
		} finally {
			reader.close();
		}
	}
}
