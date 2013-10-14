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

package uk.ac.gda.arpes.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import uk.ac.gda.arpes.beans.ARPESScanBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class ARPESWizardUtils {

	public static InputStream sessionBeanToStream(ARPESScanBean sessionBean) {
		try {
			File tempFile = File.createTempFile("arpes-", ".xml");
			tempFile.deleteOnExit();
			XMLHelpers.writeToXML(ARPESScanBean.mappingURL, sessionBean, tempFile);
			return new FileInputStream(tempFile);
		} catch (Exception e) {
		}
		return null;
	}
}