package uk.ac.gda.client.synoptic.perspectives;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

import gda.jython.authenticator.UserAuthentication;
import gda.jython.authoriser.AuthoriserProvider;
import gda.dal.DALStartup;


import org.csstudio.sds.ui.runmode.RunModeService;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class SynopticPerspective implements IPerspectiveFactory {
	
	public static final String ID = "uk.ac.gda.beamline.client.synoptic.synopticperspective";
	public static final String STAFF_PATH = "gda.client.i20.synopticpath.staff";
	public static final String USER_PATH = "gda.client.i20.synopticpath.user";
	
	private static final Logger logger = LoggerFactory.getLogger(SynopticPerspective.class);
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		new DALStartup().earlyStartup();
		String path = getSynopticPath();
		Path sdsDisplay = new Path(path);
		RunModeService.getInstance().openDisplayViewInRunMode(sdsDisplay);
	}

	
	public static String getSynopticPath() {
		try {
			// find our permission level
			String user = UserAuthentication.getUsername();
			boolean isStaff = AuthoriserProvider.getAuthoriser().isLocalStaff(user);

			if (isStaff) {
				return LocalProperties.get(STAFF_PATH);
			} 
			return LocalProperties.get(USER_PATH);
		} catch (ClassNotFoundException e) {
			logger.error("Exception trying to identify if user is staff", e);
			return LocalProperties.get(STAFF_PATH);
		}
	}
}
