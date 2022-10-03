package org.opengda.detector.electronanalyser.client;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.opengda.detector.electronanalyser.client.views.ImageView;
import org.opengda.detector.electronanalyser.client.views.RegionView;
import org.opengda.detector.electronanalyser.client.views.SequenceView;
import org.opengda.detector.electronanalyser.client.views.SpectrumView;

import gda.rcp.views.JythonTerminalView;
import uk.ac.gda.client.liveplot.LivePlotView;
import uk.ac.gda.client.scripting.JythonPerspective;

public class SESPerspective implements IPerspectiveFactory {
	private static final String TERMINAL_FOLDER = "terminalFolder";
	private static final String SEQUENCE_EDITOR_FOLDER = "sequenceEditorFolder";
	private static final String REGION_EDITOR_FOLDER = "regionEditorFolder";
	private static final String PLOT_FOLDER = "plotFolder";

	private static final String IMAGEVIEW = ImageView.ID;
	private static final String SPECTRUMVIEW = SpectrumView.ID;
	private static final String REGIONEDITOR = RegionView.ID;
	private static final String SEQUENCEEDITOR = SequenceView.ID;
	private static final String JYTHONCONSOLE = JythonTerminalView.ID;
	private static final String SCAN_PLOT_VIEW_ID = LivePlotView.ID;

	public static final String ID = "org.opengda.detector.electronanalyser.client.ses.perspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		defineLayout(layout);
	}

	private void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout plotFolder = layout.createFolder(PLOT_FOLDER, IPageLayout.LEFT, 0.75f, editorArea);
		plotFolder.addView(SPECTRUMVIEW);

		IFolderLayout regionEditorFolder = layout.createFolder(REGION_EDITOR_FOLDER, IPageLayout.RIGHT, 0.47f, PLOT_FOLDER);
		regionEditorFolder.addView(REGIONEDITOR);

		IFolderLayout sequenceEditorFolder = layout.createFolder(SEQUENCE_EDITOR_FOLDER, IPageLayout.RIGHT, 0.5f, REGION_EDITOR_FOLDER);
		sequenceEditorFolder.addView(SEQUENCEEDITOR);

		IFolderLayout terminalFolder = layout.createFolder(TERMINAL_FOLDER, IPageLayout.BOTTOM, 0.65f, PLOT_FOLDER);
		terminalFolder.addView(JYTHONCONSOLE);
		terminalFolder.addView(SCAN_PLOT_VIEW_ID);

		String plotLayoutString = ElectronAnalyserClientPlugin.getDefault().getPreferenceStore().getString(ElectronAnalyserClientPlugin.PLOT_LAYOUT);
		if (plotLayoutString == null || plotLayoutString.isEmpty() || ElectronAnalyserClientPlugin.STACK_PLOT.equals(plotLayoutString)) {
			plotFolder.addView(IMAGEVIEW);
		} else if (ElectronAnalyserClientPlugin.TILE_QUAD.equals(plotLayoutString)) {
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.5f, SPECTRUMVIEW);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(IMAGEVIEW);
			view.setFocus();
		} else if (ElectronAnalyserClientPlugin.TILE_VERTICAL.equals(plotLayoutString)) {
			layout.addView(IMAGEVIEW, IPageLayout.BOTTOM, 0.5f, SPECTRUMVIEW);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(IMAGEVIEW);
			view.setFocus();
		} else if (ElectronAnalyserClientPlugin.TILE_HORIZONTAL.equals(plotLayoutString)) {
			layout.addView(IMAGEVIEW, IPageLayout.RIGHT, 0.25f, SPECTRUMVIEW);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(IMAGEVIEW);
			view.setFocus();
		}

		layout.addPerspectiveShortcut(JythonPerspective.ID);

		layout.addShowViewShortcut(SEQUENCEEDITOR);
		layout.addShowViewShortcut(REGIONEDITOR);
		layout.addShowViewShortcut(SPECTRUMVIEW);
		layout.addShowViewShortcut(IMAGEVIEW);
		layout.addShowViewShortcut(SCAN_PLOT_VIEW_ID);
		layout.addShowViewShortcut(JYTHONCONSOLE);
	}
}
