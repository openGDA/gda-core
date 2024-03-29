/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.exafs.ui;

import java.net.URL;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.richbeans.widgets.selector.BeanSelectionEvent;
import org.eclipse.richbeans.widgets.selector.BeanSelectionListener;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import gda.jython.JythonServerFacade;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.MetadataComposite;
import uk.ac.gda.exafs.ui.composites.SignalParametersComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class OutputParametersUIEditor extends RichBeanEditorPart {
	private VerticalListEditor signalList;
	private VerticalListEditor metadataList;
	private TextWrapper nexusDirectory;
	private TextWrapper asciiDirectory;
	private TextWrapper afterScanscriptName;
	private TextWrapper beforeScanscriptName;
	private TextWrapper beforeFirstRepetition;

	private ExpandableComposite outputFoldersExpandableComposite;
	private ExpandableComposite jythonExpandableComposite;
	private ExpandableComposite signalExpandableComposite;
	private ExpandableComposite metadataExpandableComposite;

	protected Composite leftColumn;
	protected Composite rightColumn;
	private ScrolledComposite scrolledComposite;
	private OutputParameters bean;

	public OutputParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		bean=(OutputParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Output Parameters";
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		final Composite mainComposite = new Composite(scrolledComposite, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, false));
		mainComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		// create main composite in each column, add widgets to those to have better control of where
		// each set of widgets are put.
		leftColumn = new Composite(mainComposite, SWT.NONE);
		leftColumn.setLayout(new GridLayout(1, false));
		leftColumn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		rightColumn = new Composite(mainComposite, SWT.NONE);
		rightColumn.setLayout(new GridLayout(1, false));
		rightColumn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		createExtraColumns(leftColumn);

		if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.SHOW_METADATA_EDITOR))
			createMetadata(rightColumn);

		createScripts(leftColumn);
		createOutput(rightColumn);

		scrolledComposite.setContent(mainComposite);
		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// Add listener to each expandable composite to set min size of scrolled composite if expansion state changes
		ExpandableComposite[] expandableComposites = {outputFoldersExpandableComposite, jythonExpandableComposite,
				signalExpandableComposite, metadataExpandableComposite};
		for(ExpandableComposite comp : expandableComposites) {
			if (comp != null) {
				comp.addExpansionListener(listenerToSetScrolledCompSize);
			}
		}
	}

	private ExpansionAdapter listenerToSetScrolledCompSize = new ExpansionAdapter() {
		@Override
		public void expansionStateChanged(ExpansionEvent e) {
			scrolledComposite.setMinSize(scrolledComposite.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	};

	public void openScript(final TextWrapper field) {
		FileDialog dialog = getJythonScriptFileBrowser();
		final String filename = dialog.open();
		if (filename != null) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					field.setValue(filename);
				}
			});
		}
	}

	/**
	 * Return a file browser dialog for user to select a jython script.
	 * Starting directory is set to the user script directory.
	 * @return FileDialog
	 */
	public static FileDialog getJythonScriptFileBrowser() {
		FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
		String[] filterNames = new String[] { "Jython Script Files", "All Files (*)" };
		dialog.setFilterNames(filterNames);
		String[] filterExtensions = new String[] { "*.py", "*" };
		dialog.setFilterExtensions(filterExtensions);
		String filterPath = findDefaultFilterPath();
		dialog.setFilterPath(filterPath);
		return dialog;
	}

	private static String findDefaultFilterPath() {
		List<String> jythonProjectFolders = JythonServerFacade.getInstance().getAllScriptProjectFolders();
		String filterPath = System.getenv("user.home");
		for (String path : jythonProjectFolders) {
			if (JythonServerFacade.getInstance().projectIsUserType(path)) {
				filterPath = path;
				continue;
			}
		}
		return filterPath;
	}

	private void createExtraColumns(Composite composite){
		signalExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		signalExpandableComposite.setText("Add extra columns of data");
		signalExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite signalComp = new Composite(signalExpandableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		signalComp.setLayout(gridLayout);

		Group signalParametersGroup = new Group(signalComp, SWT.NONE);

		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 400;
		signalParametersGroup.setLayoutData(gd);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		signalParametersGroup.setLayout(gridLayout);

		signalList = new VerticalListEditor(signalParametersGroup, SWT.NONE);
		signalList.setTemplateName("Signal");
		signalList.setEditorClass(SignalParameters.class);
		final SignalParametersComposite signalComposite = new SignalParametersComposite(signalList, SWT.NONE);
		signalList.setEditorUI(signalComposite);
		signalList.setNameField("Label");
		signalList.setVisible(true);
		signalList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		signalList.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				signalComposite.selectionChanged((SignalParameters) evt.getSelectedBean());
			}
		});
		signalExpandableComposite.setClient(signalComp);

		ExpansionAdapter signalExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(signalList.getListSize()>0)
					signalExpandableComposite.setExpanded(true);
				GridUtils.layoutFull(signalComp.getParent());
			}
		};
		signalExpandableComposite.addExpansionListener(signalExpansionListener);

		if(bean.getSignalList().size()>0)
			signalExpandableComposite.setExpanded(true);
	}

	private void createMetadata(Composite composite){
		metadataExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		metadataExpandableComposite.setText("Add information to Ascii header");
		metadataExpandableComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));

		final Composite metadataComp = new Composite(metadataExpandableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		metadataComp.setLayout(gridLayout);

		Group metadataGroup = new Group(metadataComp, SWT.NONE);

		GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, true);
		gd.widthHint = 400;
		metadataGroup.setLayoutData(gd);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		metadataGroup.setLayout(gridLayout);

		metadataList = new VerticalListEditor(metadataGroup, SWT.NONE);
		metadataList.setTemplateName("Metadata");
		metadataList.setEditorClass(MetadataParameters.class);

		final MetadataComposite metadataComposite = new MetadataComposite(metadataList, SWT.NONE);
		metadataList.setEditorUI(metadataComposite);
		metadataList.setNameField("ScannableName");
		metadataList.setVisible(true);
		metadataList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		metadataList.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				metadataComposite.selectionChanged((MetadataParameters) evt.getSelectedBean());
			}
		});
		metadataExpandableComposite.setClient(metadataComp);

		ExpansionAdapter metadataExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(metadataList.getListSize()>0)
					metadataExpandableComposite.setExpanded(true);
				GridUtils.layoutFull(metadataComp.getParent());
			}
		};
		metadataExpandableComposite.addExpansionListener(metadataExpansionListener);


		if(bean.getMetadataList().size()>0)
			metadataExpandableComposite.setExpanded(true);
	}

	private TextWrapper addRunScriptCommandAndButton(Composite parent) {
		final TextWrapper scriptNameCommandText = new TextWrapper(parent, SWT.BORDER);
		scriptNameCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		scriptNameCommandText.setTextType(TextWrapper.TEXT_TYPE.FREE_TXT);

		Button browseForScript = new Button(parent, SWT.PUSH);
		browseForScript.setText("...");
		browseForScript.setToolTipText("Browse for script file");
		browseForScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showDialog();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				showDialog();
			}
			private void showDialog() {
				openScript(scriptNameCommandText);
			}
		});
		return scriptNameCommandText;
	}

	private void createScripts(Composite composite){
		jythonExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		jythonExpandableComposite.setText("Run commands/scripts before and after a scan");
		jythonExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite jythonComp = new Composite(jythonExpandableComposite, SWT.NONE);
		jythonComp.setLayout(new GridLayout());

		Group jythonScriptGroup = new Group(jythonComp, SWT.NONE);
		jythonScriptGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		jythonScriptGroup.setLayout(new GridLayout(3, false));

		Label beforeScriptNameLabel = new Label(jythonScriptGroup, SWT.NONE);
		beforeScriptNameLabel.setToolTipText("Jython commands/script to run immediately before each scan");
		beforeScriptNameLabel.setText("Before Scan Command/Script Name");
		beforeScanscriptName = addRunScriptCommandAndButton(jythonScriptGroup);

		Label afterScriptNameLabel = new Label(jythonScriptGroup, SWT.NONE);
		afterScriptNameLabel.setToolTipText("Jython commands/script to run immediately after each scan");
		afterScriptNameLabel.setText("After Scan Command/Script Name");
		afterScanscriptName = addRunScriptCommandAndButton(jythonScriptGroup);

		Label beforeFirstRepLabel = new Label(jythonScriptGroup, SWT.NONE);
		beforeFirstRepLabel.setToolTipText("Jython commands/script to run immediately before the first scan only\n(and before the 'Before Scan' command/script).");
		beforeFirstRepLabel.setText("Before First Scan Command/Script Name");
		beforeFirstRepetition = addRunScriptCommandAndButton(jythonScriptGroup);

		jythonExpandableComposite.setClient(jythonComp);

		// This listener prevents the 'run commands' section from being minimised if command/script names have been setup.
		ExpansionAdapter jythonExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(StringUtils.isNotEmpty(beforeScanscriptName.getText()) || StringUtils.isNotEmpty(afterScanscriptName.getText())
						|| StringUtils.isNotEmpty(beforeFirstRepetition.getText())) {
					jythonExpandableComposite.setExpanded(true);
				}
				GridUtils.layoutFull(jythonComp.getParent());
			}
		};
		jythonExpandableComposite.addExpansionListener(jythonExpansionListener);

		if(bean.getBeforeScriptName()!=null || bean.getAfterScriptName()!=null)
			jythonExpandableComposite.setExpanded(true);
	}

	private void createOutput(Composite composite){
		outputFoldersExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		outputFoldersExpandableComposite.setText("Choose where files are saved to");
		outputFoldersExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite outputFoldersComp = new Composite(outputFoldersExpandableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		outputFoldersComp.setLayout(gridLayout);

		Group ouputFilePreferencesGroup = new Group(outputFoldersComp, SWT.NONE);
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 400;
		ouputFilePreferencesGroup.setLayoutData(gd);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		ouputFilePreferencesGroup.setLayout(gridLayout);

		outputFoldersExpandableComposite.setClient(outputFoldersComp);

		ExpansionAdapter outputFoldersListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.layoutFull(outputFoldersComp.getParent());
			}
		};
		outputFoldersExpandableComposite.addExpansionListener(outputFoldersListener);

		final Label asciiFolderLabel = new Label(ouputFilePreferencesGroup, SWT.NONE);
		asciiFolderLabel.setText("Ascii Folder");

		asciiDirectory = new TextWrapper(ouputFilePreferencesGroup, SWT.BORDER);
		asciiDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		asciiDirectory.setToolTipText("The ascii sub-folder that will store ascii output files.");
		asciiDirectory.setTextType(TextWrapper.TEXT_TYPE.FILENAME);

		Label nexusFolderLabel = new Label(ouputFilePreferencesGroup, SWT.NONE);
		nexusFolderLabel.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		nexusFolderLabel.setText("Nexus Folder");

		nexusDirectory = new TextWrapper(ouputFilePreferencesGroup, SWT.BORDER);
		nexusDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nexusDirectory.setToolTipText("The sub-folder that will store nexus output files.");
		nexusDirectory.setTextType(TextWrapper.TEXT_TYPE.FILENAME);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	public TextWrapper getAsciiDirectory() {
		return asciiDirectory;
	}

	public TextWrapper getNexusDirectory() {
		return nexusDirectory;
	}

	public TextWrapper getBeforeScriptName() {
		return beforeScanscriptName;
	}

	public TextWrapper getAfterScriptName() {
		return afterScanscriptName;
	}

	public TextWrapper getBeforeFirstRepetition() {
		return beforeFirstRepetition;
	}

	public VerticalListEditor getSignalList() {
		return signalList;
	}

	public VerticalListEditor getMetadataList() {
		return metadataList;
	}
}