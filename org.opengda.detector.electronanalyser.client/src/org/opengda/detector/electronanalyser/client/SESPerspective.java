package org.opengda.detector.electronanalyser.client;

import gda.rcp.views.JythonTerminalView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.opengda.detector.electronanalyser.client.views.ExternalIOView;
import org.opengda.detector.electronanalyser.client.views.ImageView;
import org.opengda.detector.electronanalyser.client.views.RegionView;
import org.opengda.detector.electronanalyser.client.views.SequenceView;
import org.opengda.detector.electronanalyser.client.views.SlicesView;
import org.opengda.detector.electronanalyser.client.views.SpectrumView;

import uk.ac.gda.client.liveplot.LivePlotView;
import uk.ac.gda.client.scripting.JythonPerspective;

public class SESPerspective implements IPerspectiveFactory {
	private static final String TERMINAL_FOLDER = "terminalFolder";
	private static final String STATUS_FOLDER = "statusFolder";
	private static final String SEQUENCE_EDITOR_FOLDER = "sequenceEditorFolder";
	private static final String REGION_EDITOR_FOLDER = "regionEditorFolder";
	private static final String PLOT_FOLDER = "plotFolder";

	private static final String SLICEVIEW = SlicesView.ID;
	private static final String IMAGEVIEW = ImageView.ID;
	private static final String EXTERNALIOVIEW = ExternalIOView.ID;
	private static final String SPECTRUMVIEW =SpectrumView.ID;
	private static final String REGIONEDITOR = RegionView.ID;
	private static final String SEQUENCEEDITOR = SequenceView.ID;
	private static final String JYTHONCONSOLE=JythonTerminalView.ID;
	private static final String SCAN_PLOT_VIEW_ID = LivePlotView.ID;
//	private static final String STATUS_VIEW_ID = "uk.ac.gda.beamline.i09.views.statusView";
	public static final String ID = "org.opengda.detector.electronanalyser.client.ses.perspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		defineLayout(layout);
		defineActions(layout);
	}

	private void defineActions(IPageLayout layout) {
		// TODO Auto-generated method stub

	}

	private void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
//		IFolderLayout statusFolder =  layout.createFolder(STATUS_FOLDER, IPageLayout.BOTTOM, (float)0.85, editorArea);
//		statusFolder.addView(STATUS_VIEW_ID);

		IFolderLayout plotFolder = layout.createFolder(PLOT_FOLDER, IPageLayout.LEFT, 0.75f, editorArea);
		plotFolder.addView(SPECTRUMVIEW);

        IFolderLayout regionEditorFolder=layout.createFolder(REGION_EDITOR_FOLDER, IPageLayout.RIGHT, (float)0.48, PLOT_FOLDER); //$NON-NLS-1$
        regionEditorFolder.addView(REGIONEDITOR);

        IFolderLayout sequenceEditorFolder=layout.createFolder(SEQUENCE_EDITOR_FOLDER, IPageLayout.RIGHT, (float)0.5, REGION_EDITOR_FOLDER); //$NON-NLS-1$
        sequenceEditorFolder.addView(SEQUENCEEDITOR);

        IFolderLayout terminalFolder=layout.createFolder(TERMINAL_FOLDER, IPageLayout.BOTTOM, (float)0.65, PLOT_FOLDER); //$NON-NLS-1$
        terminalFolder.addView(JYTHONCONSOLE);
        terminalFolder.addView(SCAN_PLOT_VIEW_ID);

        String plotLayoutString = ElectronAnalyserClientPlugin.getDefault().getPreferenceStore().getString(ElectronAnalyserClientPlugin.PLOT_LAYOUT);
		if (plotLayoutString == null || plotLayoutString.isEmpty()
				|| ElectronAnalyserClientPlugin.STACK_PLOT.equals(plotLayoutString)) {
			plotFolder.addView(EXTERNALIOVIEW);
			plotFolder.addView(IMAGEVIEW);
			plotFolder.addView(SLICEVIEW);
		} else if (ElectronAnalyserClientPlugin.TILE_QUAD.equals(plotLayoutString)) {
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.5f, SPECTRUMVIEW);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(IMAGEVIEW);
			view.setFocus();
			layout.addView(SLICEVIEW, IPageLayout.BOTTOM, 0.5f, IMAGEVIEW);
			view = page.findView(SLICEVIEW);
			view.setFocus();
			layout.addView(EXTERNALIOVIEW, IPageLayout.BOTTOM, 0.5f, SPECTRUMVIEW);
			view = page.findView(EXTERNALIOVIEW);
			view.setFocus();
		} else if (ElectronAnalyserClientPlugin.TILE_VERTICAL.equals(plotLayoutString)) {
			layout.addView(IMAGEVIEW, IPageLayout.BOTTOM, 0.5f, SPECTRUMVIEW);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(IMAGEVIEW);
			view.setFocus();
			layout.addView(SLICEVIEW, IPageLayout.BOTTOM, 0.5f, IMAGEVIEW);
			view = page.findView(SLICEVIEW);
			view.setFocus();
			layout.addView(EXTERNALIOVIEW, IPageLayout.BOTTOM, 0.5f, SPECTRUMVIEW);
			view = page.findView(EXTERNALIOVIEW);
			view.setFocus();
		} else if (ElectronAnalyserClientPlugin.TILE_HORIZONTAL.equals(plotLayoutString)) {
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.25f, SPECTRUMVIEW);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(IMAGEVIEW);
			view.setFocus();
			layout.addView(EXTERNALIOVIEW, IPageLayout.RIGHT, 0.33f, IMAGEVIEW);
			view = page.findView(SLICEVIEW);
			view.setFocus();
			layout.addView(SLICEVIEW, IPageLayout.RIGHT, 0.5f, EXTERNALIOVIEW);
			view = page.findView(EXTERNALIOVIEW);
			view.setFocus();
		}

        layout.addPerspectiveShortcut(JythonPerspective.ID);

		layout.addShowViewShortcut(SEQUENCEEDITOR);
		layout.addShowViewShortcut(REGIONEDITOR);
		layout.addShowViewShortcut(SPECTRUMVIEW);
		layout.addShowViewShortcut(IMAGEVIEW);
		layout.addShowViewShortcut(EXTERNALIOVIEW);
		layout.addShowViewShortcut(SLICEVIEW);
		layout.addShowViewShortcut(SCAN_PLOT_VIEW_ID);
		layout.addShowViewShortcut(JYTHONCONSOLE);
	}
}
