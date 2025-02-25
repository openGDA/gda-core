package org.opengda.detector.electronanalyser.client;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.opengda.detector.electronanalyser.client.views.ImageView;
import org.opengda.detector.electronanalyser.client.views.RegionViewLive;
import org.opengda.detector.electronanalyser.client.views.SequenceViewLive;
import org.opengda.detector.electronanalyser.client.views.SpectrumView;

import gda.factory.Finder;
import gda.rcp.views.JythonTerminalView;
import uk.ac.gda.client.livecontrol.ControlSet;
import uk.ac.gda.client.livecontrol.LiveControlsView;
import uk.ac.gda.client.liveplot.LivePlotView;
import uk.ac.gda.client.scripting.JythonPerspective;

public class SESLivePerspective implements IPerspectiveFactory {
	private static final String TERMINAL_FOLDER = "terminalFolder";
	private static final String SEQUENCE_EDITOR_FOLDER = "sequenceEditorFolder";
	private static final String REGION_EDITOR_FOLDER = "regionEditorFolder";
	private static final String PLOT_FOLDER = "plotFolder";

	private static final String IMAGEVIEW = ImageView.ID;
	private static final String SPECTRUMVIEW = SpectrumView.ID;
	private static final String REGIONEDITOR = RegionViewLive.ID;
	private static final String SEQUENCEEDITOR = SequenceViewLive.ID;
	private static final String JYTHONCONSOLE = JythonTerminalView.ID;
	private static final String SCAN_PLOT_VIEW_ID = LivePlotView.ID;
	private static final String SCAN_2D_PLOT_VIEW_ID = "uk.ac.diamond.scisoft.analysis.rcp.plotViewMultiple:2D Scan Plot";
	private static final String LIVE_CONTROLS = LiveControlsView.ID;

	public static final String ID = "org.opengda.detector.electronanalyser.client.ses.perspective";

	public static final String GDA_NAVIGATOR_ID = "uk.ac.gda.client.navigator";

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

		IFolderLayout regionEditorFolder = layout.createFolder(REGION_EDITOR_FOLDER, IPageLayout.RIGHT, 0.55f, PLOT_FOLDER);
		regionEditorFolder.addView(REGIONEDITOR);

		IFolderLayout sequenceEditorFolder = layout.createFolder(SEQUENCE_EDITOR_FOLDER, IPageLayout.RIGHT, 0.55f, REGION_EDITOR_FOLDER);
		sequenceEditorFolder.addView(SEQUENCEEDITOR);

		IFolderLayout terminalFolder = layout.createFolder(TERMINAL_FOLDER, IPageLayout.BOTTOM, 0.5f, PLOT_FOLDER);
		terminalFolder.addView(JYTHONCONSOLE);

		//Only add live controls if there are some configured
		if (!Finder.listLocalFindablesOfType(ControlSet.class).isEmpty()) {
			IFolderLayout liveControls = layout.createFolder(LIVE_CONTROLS, IPageLayout.TOP, 0.16f, TERMINAL_FOLDER);
			liveControls.addView(LIVE_CONTROLS);
		}

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

		IFolderLayout scanPlot = layout.createFolder(SCAN_PLOT_VIEW_ID, IPageLayout.RIGHT, 0.5f, PLOT_FOLDER);
		scanPlot.addView(SCAN_PLOT_VIEW_ID);
		scanPlot.addView(SCAN_2D_PLOT_VIEW_ID);

		IFolderLayout projectExplorer = layout.createFolder(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.RIGHT, 0.3f, editorArea); //$NON-NLS-1$
		projectExplorer.addView(IPageLayout.ID_PROJECT_EXPLORER);
		projectExplorer.addPlaceholder(GDA_NAVIGATOR_ID);

		layout.addPerspectiveShortcut(JythonPerspective.ID);

		layout.addShowViewShortcut(REGIONEDITOR);
		layout.addShowViewShortcut(SPECTRUMVIEW);
		layout.addShowViewShortcut(IMAGEVIEW);
		layout.addShowViewShortcut(SCAN_PLOT_VIEW_ID);
		layout.addShowViewShortcut(JYTHONCONSOLE);
		layout.addShowViewShortcut(LIVE_CONTROLS);
	}
}
