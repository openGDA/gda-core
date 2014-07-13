package org.opengda.lde.ui;

import gda.rcp.views.JythonTerminalView;

import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;
import org.opengda.lde.ui.views.LiveImageView;
import org.opengda.lde.ui.views.ReducedDataPlotView;
import org.opengda.lde.ui.views.SampleGroupView;
import org.python.pydev.ui.wizards.files.PythonModuleWizard;
import org.python.pydev.ui.wizards.files.PythonPackageWizard;
import org.python.pydev.ui.wizards.files.PythonSourceFolderWizard;
import org.python.pydev.ui.wizards.project.PythonProjectWizard;

import uk.ac.gda.beamline.i11.views.DetectorFilePlotView;
import uk.ac.gda.client.liveplot.LivePlotView;
import uk.ac.gda.client.scripting.JythonPerspective;
import uk.ac.gda.epics.client.views.StatusView;

public class LDEPerspective implements IPerspectiveFactory {

	public static final String ID="org.opengda.lde.ui.perspective";
	
	private static final String TERMINAL_FOLDER = "terminalFolder";
	private static final String PROJ_FOLDER = "projectFolder";
	private static final String STATUS_FOLDER = "statusFolder";
	private static final String SAMPLE_TABLE_FOLDER = "sampleTableFolder";
	private static final String DETECTOR_PLOT_FOLDER = "detectorPlotFolder";
	private static final String DETECTOR_STATUS_FOLDER = "detectorStatusFolder";
	
	private static final String SAMPLE_GROUP_VIEW_ID = SampleGroupView.ID;
	private static final String PIXIUM_IMAGE_VIEW_ID = LiveImageView.ID;
	private static final String PIXIUM_PLOT_VIEW_ID = ReducedDataPlotView.ID;
	private static final String DETECTOR_PLOT_VIEW_ID = DetectorFilePlotView.ID;
	private static final String SCAN_PLOT_VIEW_ID = LivePlotView.ID;
	private static final String GDA_NAVIGATOR_VIEW_ID = "uk.ac.gda.client.navigator";
	private static final String STATUS_VIEW_ID = "uk.ac.gda.beamline.i11.views.statusView";
	private static final String DETECTOR_STATUS_VIEW_ID = StatusView.ID;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		defineLayout(layout);
		defineActions(layout);
	}

	private void defineActions(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		
		IFolderLayout statusFolder =  layout.createFolder(STATUS_FOLDER, IPageLayout.BOTTOM, (float)0.85, editorArea);
		statusFolder.addView(STATUS_VIEW_ID);

		IFolderLayout topLeft = layout.createFolder(PROJ_FOLDER, IPageLayout.LEFT, (float)0.17, editorArea); //$NON-NLS-1$
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        topLeft.addPlaceholder(GDA_NAVIGATOR_VIEW_ID);
        
        IFolderLayout sampleTableFolder=layout.createFolder(SAMPLE_TABLE_FOLDER, IPageLayout.LEFT, (float)0.7, editorArea); //$NON-NLS-1$
        sampleTableFolder.addView(SAMPLE_GROUP_VIEW_ID);

        IFolderLayout detectorPlotFolder=layout.createFolder(DETECTOR_PLOT_FOLDER, IPageLayout.BOTTOM, (float)0.5, SAMPLE_TABLE_FOLDER); //$NON-NLS-1$
        detectorPlotFolder.addView(PIXIUM_IMAGE_VIEW_ID);
        detectorPlotFolder.addView(PIXIUM_PLOT_VIEW_ID);
        detectorPlotFolder.addView(DETECTOR_PLOT_VIEW_ID);
        detectorPlotFolder.addView(SCAN_PLOT_VIEW_ID);

        IFolderLayout statusPlotFolder=layout.createFolder(DETECTOR_STATUS_FOLDER, IPageLayout.RIGHT, (float)0.6, SAMPLE_TABLE_FOLDER); //$NON-NLS-1$
        statusPlotFolder.addView(DETECTOR_STATUS_VIEW_ID);
        
        IFolderLayout terminalfolder= layout.createFolder(TERMINAL_FOLDER, IPageLayout.BOTTOM, (float)0.5, DETECTOR_STATUS_FOLDER); //$NON-NLS-1$
        terminalfolder.addView(JythonTerminalView.ID);
        terminalfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        terminalfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
        terminalfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
        terminalfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
        terminalfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
        
        // add status here
        //IFolderLayout sideStatusFolder = layout.createFolder("statusFolder", IPageLayout.RIGHT, 0.5f, Scan_PLOT_FOLDER);

        layout.addPerspectiveShortcut(JythonPerspective.ID);
        
        layout.addShowViewShortcut(SAMPLE_GROUP_VIEW_ID);
        layout.addShowViewShortcut(PIXIUM_IMAGE_VIEW_ID);
        layout.addShowViewShortcut(PIXIUM_PLOT_VIEW_ID);
        layout.addShowViewShortcut(DETECTOR_PLOT_VIEW_ID);
        layout.addShowViewShortcut(LivePlotView.ID);
        layout.addShowViewShortcut(JythonTerminalView.ID);
        layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
	}

	private void defineLayout(IPageLayout layout) {
        layout.addNewWizardShortcut(PythonProjectWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonSourceFolderWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonPackageWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonModuleWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$

        layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
        layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(GDA_NAVIGATOR_VIEW_ID);
        layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
        layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
        layout.addShowViewShortcut("org.python.pydev.views.PyRefactorView");
        layout.addShowViewShortcut("org.python.pydev.views.PyCodeCoverageView");
        layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");
        
        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);	
    }

}
