package uk.ac.diamond.tomography.reconstruction;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.diamond.tomography.reconstruction.views.ParameterView;
import uk.ac.diamond.tomography.reconstruction.views.ProjectionsView;

public class PerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {

		layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f,
				IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.diamond.scisoft.analysis.rcp.plotView1",
				IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(ProjectionsView.ID, IPageLayout.BOTTOM, 0.3f,
				IPageLayout.ID_PROJECT_EXPLORER);
		layout.addView(ParameterView.ID, IPageLayout.BOTTOM, 0.5f,
				ProjectionsView.ID);
		layout.addPlaceholder(
				"org.dawb.workbench.plotting.views.toolPageView.1D_and_2D",
				IPageLayout.RIGHT, 0.7f,
				"uk.ac.diamond.scisoft.analysis.rcp.plotView1");
		layout.setEditorAreaVisible(false);
	}

}
