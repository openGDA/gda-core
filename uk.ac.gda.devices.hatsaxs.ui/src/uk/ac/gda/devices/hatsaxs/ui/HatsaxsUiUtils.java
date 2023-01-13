package uk.ac.gda.devices.hatsaxs.ui;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.DataProject;
import uk.ac.gda.devices.hatsaxs.HatsaxsUtils;

public class HatsaxsUiUtils {
	private static final Logger logger = LoggerFactory.getLogger(HatsaxsUiUtils.class);
	private HatsaxsUiUtils() {}
	
	public static void refreshXmlDirectory() {
		IProject project = DataProject.getDataProjectIfExists();
		if (project != null) {
			try {
				IWorkspace wksp = project.getWorkspace();
				IWorkspaceRoot root = wksp.getRoot();
				URI xml = new File(HatsaxsUtils.getXmlDirectory()).toURI();
				IContainer[] containers = root.findContainersForLocationURI(xml);
				if (containers.length > 0) {
					containers[0].refreshLocal(2, null);
				}
			} catch (CoreException e) {
				logger.error("Could not refresh XML directory", e);
			}
		}
	}
}
