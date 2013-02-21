package org.opengda.detector.electronanalyser.client;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class SESPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		IFolderLayout mainFolder = layout.createFolder("MAIN_FOLDER", IPageLayout.TOP, IPageLayout.RATIO_MAX, null);
	}

}
