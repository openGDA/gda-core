package uk.ac.gda.client.logpanel;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import uk.ac.gda.client.logpanel.view.LogpanelView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		layout.addStandaloneView(LogpanelView.ID, false, IPageLayout.BOTTOM, IPageLayout.NULL_RATIO, layout.getEditorArea());
//		IViewLayout statusLayout = layout.getViewLayout(LogpanelView.ID);
//		statusLayout.setCloseable(false);
//		statusLayout.setMoveable(false);
	}

}
