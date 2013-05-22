package org.opengda.detector.electronanalyser.client;

import gda.rcp.views.JythonTerminalView;

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
	private static final String JYTHONCONSOLE=JythonTerminalView.ID;
	public static final String ID = "org.opengda.detector.electronanalyser.client.ses.perspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);

		String editorArea = layout.getEditorArea();
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.75, editorArea); //$NON-NLS-1$
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        
		layout.addView(SEQUENCEEDITOR, IPageLayout.RIGHT, 0.15f, "topLeft");
        

		IFolderLayout plotFolder = null;

		String plotLayoutString = ElectronAnalyserClientPlugin.getDefault().getPreferenceStore().getString(ElectronAnalyserClientPlugin.PLOT_LAYOUT);
		if (plotLayoutString == null || plotLayoutString.isEmpty() 
				|| ElectronAnalyserClientPlugin.STACK_PLOT.equals(plotLayoutString)) {
			layout.addView(REGIONEDITOR, IPageLayout.RIGHT, 0.7f, SEQUENCEEDITOR);
			plotFolder = layout.createFolder("PlotFolder", IPageLayout.BOTTOM, 0.5f, SEQUENCEEDITOR);
			layout.addView(JYTHONCONSOLE, IPageLayout.BOTTOM, 0.5f, REGIONEDITOR);

			plotFolder.addView(SPECTRUMVIEW);
			plotFolder.addView(EXTERNALIOVIEW);
			plotFolder.addView(IMAGEVIEW);
			plotFolder.addView(SLICEVIEW);

			layout.addView(PROGRESSVIEW, IPageLayout.BOTTOM, 0.875f, "PlotFolder");
			
		} else if (ElectronAnalyserClientPlugin.TILE_QUAD.equals(plotLayoutString)) {
			layout.addView(REGIONEDITOR, IPageLayout.RIGHT, 0.7f, SEQUENCEEDITOR);
			layout.addView(SPECTRUMVIEW, IPageLayout.BOTTOM, 0.5f, SEQUENCEEDITOR);
			layout.addView(PROGRESSVIEW, IPageLayout.BOTTOM, 0.875f, SPECTRUMVIEW);
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.5f, SPECTRUMVIEW);
			layout.addView(SLICEVIEW, IPageLayout.BOTTOM, 0.5f, IMAGEVIEW);
			layout.addView(EXTERNALIOVIEW, IPageLayout.BOTTOM, 0.5f, SPECTRUMVIEW);
			layout.addView(JYTHONCONSOLE, IPageLayout.BOTTOM, 0.5f, REGIONEDITOR);

		} else if (ElectronAnalyserClientPlugin.TILE_VERTICAL.equals(plotLayoutString)) {
			layout.addView(REGIONEDITOR, IPageLayout.RIGHT, 0.7f, SEQUENCEEDITOR);
			IFolderLayout topPlotfolder = layout.createFolder("topPlot", IPageLayout.BOTTOM, 0.5f, SEQUENCEEDITOR);
			topPlotfolder.addView(SPECTRUMVIEW);
			topPlotfolder.addView(IMAGEVIEW);
			IFolderLayout bottomPlotFolder= layout.createFolder("bottomPlot", IPageLayout.BOTTOM, 0.45f, "topPlot");
			bottomPlotFolder.addView(EXTERNALIOVIEW);
			bottomPlotFolder.addView(SLICEVIEW);
			layout.addView(PROGRESSVIEW, IPageLayout.BOTTOM, 0.8f, "bottomPlot");
			layout.addView(JYTHONCONSOLE, IPageLayout.BOTTOM, 0.5f, REGIONEDITOR);

		} else if (ElectronAnalyserClientPlugin.TILE_HORIZONTAL.equals(plotLayoutString)) {
			layout.addView(REGIONEDITOR, IPageLayout.RIGHT, 0.7f, SEQUENCEEDITOR);
			layout.addView(SPECTRUMVIEW, IPageLayout.BOTTOM, 0.5f, SEQUENCEEDITOR);
			layout.addView(PROGRESSVIEW, IPageLayout.BOTTOM, 0.875f, SPECTRUMVIEW);
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.25f, SPECTRUMVIEW);
			layout.addView(EXTERNALIOVIEW, IPageLayout.RIGHT, 0.33f, IMAGEVIEW);
			layout.addView(SLICEVIEW, IPageLayout.RIGHT, 0.5f, EXTERNALIOVIEW);
			layout.addView(JYTHONCONSOLE, IPageLayout.BOTTOM, 0.5f, REGIONEDITOR);
		}

		//layout.addView(COMMANDQUEUEVIEW, IPageLayout.BOTTOM, 0.5f, "topLeft");
		layout.setEditorAreaVisible(false);
		
		layout.addShowViewShortcut(SEQUENCEEDITOR);
		layout.addShowViewShortcut(REGIONEDITOR);
		layout.addShowViewShortcut(SPECTRUMVIEW);
		layout.addShowViewShortcut(IMAGEVIEW);
		layout.addShowViewShortcut(EXTERNALIOVIEW);
		layout.addShowViewShortcut(SLICEVIEW);
		layout.addShowViewShortcut(PROGRESSVIEW);
	}

}
