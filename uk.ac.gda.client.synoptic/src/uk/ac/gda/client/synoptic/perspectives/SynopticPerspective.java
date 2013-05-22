package uk.ac.gda.client.synoptic.perspectives;

import gda.configuration.properties.LocalProperties;

import org.csstudio.sds.ui.runmode.RunModeService;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class SynopticPerspective implements IPerspectiveFactory {
	
	public static String ID = "uk.ac.gda.beamline.client.synoptic.synopticperspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);

		String path = LocalProperties.get("gda.client.synopticpath");
		Path sdsDisplay = new Path(path);
		RunModeService.getInstance().openDisplayViewInRunMode(sdsDisplay);
	}

}
