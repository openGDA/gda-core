package org.opengda.detector.electronanalyser.client;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.ExternalIOViewExtensionFactory;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.ImageViewExtensionFactory;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.ProgressViewExtensionFactory;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.RegionViewExtensionFactory;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.SequenceViewExtensionFactory;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.SlicesViewExtensionFactory;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.SpectrumViewExtensionFactory;

import uk.ac.gda.client.CommandQueueViewFactory;

public class SESPerspective implements IPerspectiveFactory {
	private static final String COMMANDQUEUEVIEW = CommandQueueViewFactory.ID;
	private static final String PROGRESSVIEW = ProgressViewExtensionFactory.ID;
	private static final String SLICEVIEW = SlicesViewExtensionFactory.ID;
	private static final String IMAGEVIEW = ImageViewExtensionFactory.ID;
	private static final String EXTERNALIOVIEW = ExternalIOViewExtensionFactory.ID;
	private static final String SPECTRUMVIEW =SpectrumViewExtensionFactory.ID;
	private static final String REGIONEDITOR = RegionViewExtensionFactory.ID;
	private static final String SEQUENCEEDITOR = SequenceViewExtensionFactory.ID;
	public static final String ID = "org.opengda.detector.electronanalyser.client.ses.perspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		layout.addView(SEQUENCEEDITOR, IPageLayout.LEFT, 0.75f, layout.getEditorArea());

		layout.addView(REGIONEDITOR, IPageLayout.RIGHT, 0.5f, layout.getEditorArea());

		IFolderLayout plotFolder = null;

		String plotLayoutString = ElectronAnalyserClientPlugin.getDefault().getPreferenceStore().getString(ElectronAnalyserClientPlugin.PLOT_LAYOUT);
		if (plotLayoutString == null
				|| ElectronAnalyserClientPlugin.STACK_PLOT.equals(plotLayoutString)) {
			plotFolder = layout.createFolder("PlotFolder", IPageLayout.BOTTOM, 0.5f, SEQUENCEEDITOR);

			plotFolder.addView(SPECTRUMVIEW);
			plotFolder.addView(EXTERNALIOVIEW);
			plotFolder.addView(IMAGEVIEW);
			plotFolder.addView(SLICEVIEW);

		} else if (ElectronAnalyserClientPlugin.TILE_QUAD.equals(plotLayoutString)) {
			layout.addView(SPECTRUMVIEW, IPageLayout.BOTTOM, 0.5f, SEQUENCEEDITOR);
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.5f, SPECTRUMVIEW);
			layout.addView(EXTERNALIOVIEW, IPageLayout.BOTTOM, 0.5f, SPECTRUMVIEW);
			layout.addView(SLICEVIEW, IPageLayout.BOTTOM, 0.5f, IMAGEVIEW);

		} else if (ElectronAnalyserClientPlugin.TILE_VERTICAL.equals(plotLayoutString)) {
			layout.addView(SPECTRUMVIEW, IPageLayout.BOTTOM, 0.5f, SEQUENCEEDITOR);
			layout.addView(IMAGEVIEW, IPageLayout.BOTTOM, 0.25f, SPECTRUMVIEW);
			layout.addView(EXTERNALIOVIEW, IPageLayout.BOTTOM, 0.33f, IMAGEVIEW);
			layout.addView(SLICEVIEW, IPageLayout.BOTTOM, 0.5f, EXTERNALIOVIEW);
			
		} else if (ElectronAnalyserClientPlugin.TILE_HORIZONTAL.equals(plotLayoutString)) {
			layout.addView(SPECTRUMVIEW, IPageLayout.BOTTOM, 0.5f, SEQUENCEEDITOR);
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.25f, SPECTRUMVIEW);
			layout.addView(EXTERNALIOVIEW, IPageLayout.RIGHT, 0.33f, IMAGEVIEW);
			layout.addView(SLICEVIEW, IPageLayout.RIGHT, 0.5f, EXTERNALIOVIEW);
			
		}

		layout.addView(PROGRESSVIEW, IPageLayout.BOTTOM, 0.5f, REGIONEDITOR);
		layout.addView(COMMANDQUEUEVIEW, IPageLayout.BOTTOM, 0.25f, REGIONEDITOR);
		layout.setEditorAreaVisible(false);
	}

}
