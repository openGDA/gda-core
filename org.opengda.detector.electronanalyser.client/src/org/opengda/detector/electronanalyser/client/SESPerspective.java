package org.opengda.detector.electronanalyser.client;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class SESPerspective implements IPerspectiveFactory {
	private static final String SLICEVIEW = "org.opengda.detector.electronanalyser.client.plot.sliceview";
	private static final String IMAGEVIEW = "org.opengda.detector.electronanalyser.client.plot.imageview";
	private static final String EXTERNALIOVIEW = "org.opengda.detector.electronanalyser.client.plot.externalioview";
	private static final String SPECTRUMVIEW = "org.opengda.detector.electronanalyser.client.plot.spectrumview";
	private static final String REGIONEDITOR = "org.opengda.detector.electronanalyser.client.regioneditor";
	private static final String SEQUENCE_EDITOR = "org.opengda.detector.electronanalyser.client.sequenceeditor";
	public static final String ID = "org.opengda.detector.electronanalyser.client.ses.perspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		layout.addView(SEQUENCE_EDITOR, IPageLayout.LEFT, 0.75f, layout.getEditorArea());

		layout.addView(REGIONEDITOR, IPageLayout.RIGHT, 0.5f, layout.getEditorArea());

		IFolderLayout plotFolder = null;

		String plotLayoutString = ElectronAnalyserClientPlugin.getDefault().getPreferenceStore().getString(ElectronAnalyserClientPlugin.PLOT_LAYOUT);
		if (plotLayoutString == null
				|| ElectronAnalyserClientPlugin.STACK_PLOT.equals(plotLayoutString)) {
			plotFolder = layout.createFolder("PlotFolder", IPageLayout.BOTTOM, 0.5f, SEQUENCE_EDITOR);

			plotFolder.addView(SPECTRUMVIEW);
			plotFolder.addView(EXTERNALIOVIEW);
			plotFolder.addView(IMAGEVIEW);
			plotFolder.addView(SLICEVIEW);

		} else if (ElectronAnalyserClientPlugin.TILE_QUAD.equals(plotLayoutString)) {
			layout.addView(SPECTRUMVIEW, IPageLayout.BOTTOM, 0.5f, SEQUENCE_EDITOR);
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.5f, SPECTRUMVIEW);
			layout.addView(EXTERNALIOVIEW, IPageLayout.BOTTOM, 0.5f, SPECTRUMVIEW);
			layout.addView(SLICEVIEW, IPageLayout.BOTTOM, 0.5f, IMAGEVIEW);

		} else if (ElectronAnalyserClientPlugin.TILE_VERTICAL.equals(plotLayoutString)) {
			layout.addView(SPECTRUMVIEW, IPageLayout.BOTTOM, 0.5f, SEQUENCE_EDITOR);
			layout.addView(IMAGEVIEW, IPageLayout.BOTTOM, 0.25f, SPECTRUMVIEW);
			layout.addView(EXTERNALIOVIEW, IPageLayout.BOTTOM, 0.33f, IMAGEVIEW);
			layout.addView(SLICEVIEW, IPageLayout.BOTTOM, 0.5f, EXTERNALIOVIEW);
			
		} else if (ElectronAnalyserClientPlugin.TILE_HORIZONTAL.equals(plotLayoutString)) {
			layout.addView(SPECTRUMVIEW, IPageLayout.BOTTOM, 0.5f, SEQUENCE_EDITOR);
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.25f, SPECTRUMVIEW);
			layout.addView(EXTERNALIOVIEW, IPageLayout.RIGHT, 0.33f, IMAGEVIEW);
			layout.addView(SLICEVIEW, IPageLayout.RIGHT, 0.5f, EXTERNALIOVIEW);
			
		}

		layout.addView("uk.ac.gda.client.CommandQueueViewFactory", IPageLayout.BOTTOM, 0.5f, REGIONEDITOR);
		layout.addView("org.opengda.detector.electronanalyser.client.plot.progress", IPageLayout.BOTTOM, 0.8f, REGIONEDITOR);
		layout.setEditorAreaVisible(false);
	}

}
