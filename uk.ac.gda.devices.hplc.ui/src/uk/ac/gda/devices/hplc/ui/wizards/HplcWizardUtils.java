package uk.ac.gda.devices.hplc.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import uk.ac.gda.devices.hplc.beans.HplcSessionBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class HplcWizardUtils {

	public static InputStream sessionBeanToStream(HplcSessionBean sessionBean) {
		try {
			File tempFile = File.createTempFile("hplc-", ".xml");
			tempFile.deleteOnExit();
			XMLHelpers.writeToXML(HplcSessionBean.mappingURL, sessionBean, tempFile);
			return new FileInputStream(tempFile);
		} catch (Exception e) {
		}
		return null;
	}
}
