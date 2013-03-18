/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.jython.JythonServerFacade;

import java.net.URL;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.exafs.i20.I20OutputParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.MetadataComposite;
import uk.ac.gda.exafs.ui.composites.SignalParametersComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class I20OutputParametersUIEditor extends RichBeanEditorPart {

	private TextWrapper asciiFileName;
	private VerticalListEditor signalList;
	private VerticalListEditor metadataList;
	private TextWrapper nexusDirectory;
	private TextWrapper asciiDirectory;
	private TextWrapper afterScanscriptName;
	private TextWrapper beforeScanscriptName;
	
	private BooleanWrapper vortexSaveRawSpectrum;
	private BooleanWrapper xspressOnlyShowFF;
	private BooleanWrapper xspressShowDTRawValues;
	private BooleanWrapper xspressSaveRawSpectrum;


	ExpandableComposite outputFoldersExpandableComposite;
	ExpandableComposite jythonExpandableComposite;
	ExpandableComposite signalExpandableComposite;
	ExpandableComposite metadataExpandableComposite;
	ExpandableComposite detectorsExpandableComposite;
	
	I20OutputParameters bean;

	public I20OutputParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.bean=(I20OutputParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Output Parameters";
	}

	@Override
	public void createPartControl(Composite parent) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);
		
		final Composite left = new Composite(parent, SWT.NONE);
		left.setLayout(new GridLayout(2, false));
		left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		createExtraColumns(left);

		if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.SHOW_METADATA_EDITOR)) {
			createMetadata(left);
		}

		createScripts(left);
		createOutput(left);
		createDetectorOptions(left);
	}

	public void openScript(final TextWrapper field) {
		FileDialog dialog = new FileDialog(getSite().getShell(), SWT.OPEN);
		String[] filterNames = new String[] { "Jython Script Files", "All Files (*)" };
		dialog.setFilterNames(filterNames);
		String[] filterExtensions = new String[] { "*.py", "*" };
		dialog.setFilterExtensions(filterExtensions);
		String filterPath = findDefaultFilterPath();
		dialog.setFilterPath(filterPath);
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

	private String findDefaultFilterPath() {
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

		final Group signalParametersGroup = new Group(signalComp, SWT.NONE);

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
		metadataExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite metadataComp = new Composite(metadataExpandableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		metadataComp.setLayout(gridLayout);

		final Group metadataGroup = new Group(metadataComp, SWT.NONE);

		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
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
	
	private void createScripts(Composite composite){
		jythonExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		jythonExpandableComposite.setText("Run scripts before and after a scan");
		jythonExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite jythonComp = new Composite(jythonExpandableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		jythonComp.setLayout(gridLayout);

		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 400;
		gd.heightHint = 80;

		final Group jythonScriptGroup = new Group(jythonComp, SWT.NONE);
		jythonScriptGroup.setLayoutData(gd);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		jythonScriptGroup.setLayout(gridLayout);

		Label beforeScriptNameLabel = new Label(jythonScriptGroup, SWT.NONE);
		beforeScriptNameLabel.setToolTipText("A Jython script to run immediately before each scan");
		beforeScriptNameLabel.setText("Before Scan Script Name");

		beforeScanscriptName = new TextWrapper(jythonScriptGroup, SWT.BORDER);
		beforeScanscriptName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		beforeScanscriptName.setTextType(TextWrapper.TEXT_TYPE.FILENAME);

		Button beforeScanscriptButton = new Button(jythonScriptGroup, SWT.PUSH);
		beforeScanscriptButton.setText("...");
		beforeScanscriptButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showDialog();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				showDialog();
			}

			private void showDialog() {
				openScript(beforeScanscriptName);
			}
		});

		Label afterScriptNameLabel = new Label(jythonScriptGroup, SWT.NONE);
		afterScriptNameLabel.setToolTipText("A Jython script to run immediately after each scan");
		afterScriptNameLabel.setText("After Scan Script Name");

		afterScanscriptName = new TextWrapper(jythonScriptGroup, SWT.BORDER);
		afterScanscriptName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		afterScanscriptName.setTextType(TextWrapper.TEXT_TYPE.FILENAME);

		Button afterScanscriptButton = new Button(jythonScriptGroup, SWT.PUSH);
		afterScanscriptButton.setText("...");
		afterScanscriptButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showDialog();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				showDialog();
			}

			private void showDialog() {
				openScript(afterScanscriptName);
			}
		});

		jythonExpandableComposite.setClient(jythonComp);

		ExpansionAdapter jythonExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(!beforeScanscriptName.getText().equals("") || !afterScanscriptName.getText().equals(""))
					jythonExpandableComposite.setExpanded(true);
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
		
		final Label nexusFolderLabel = new Label(ouputFilePreferencesGroup, SWT.NONE);
		nexusFolderLabel.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		nexusFolderLabel.setText("Nexus Folder");

		nexusDirectory = new TextWrapper(ouputFilePreferencesGroup, SWT.BORDER);
		nexusDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nexusDirectory.setToolTipText("The sub-folder that will store nexus output files.");
		nexusDirectory.setTextType(TextWrapper.TEXT_TYPE.FILENAME);
	}
	
	private void createDetectorOptions(Composite left) {
		detectorsExpandableComposite = new ExpandableComposite(left, SWT.NONE);
		detectorsExpandableComposite.setText("Fluorescence detectors output");
		detectorsExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite detFoldersComp = new Composite(detectorsExpandableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		detFoldersComp.setLayout(gridLayout);

		Group vortexPreferencesGroup = new Group(detFoldersComp, SWT.NONE);
		vortexPreferencesGroup.setText("Vortex (Si)");
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 400;
		vortexPreferencesGroup.setLayoutData(gd);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		vortexPreferencesGroup.setLayout(gridLayout);

		Group xspressPreferencesGroup = new Group(detFoldersComp, SWT.NONE);
		xspressPreferencesGroup.setText("Xspress (Ge)");
		xspressPreferencesGroup.setLayoutData(gd);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		xspressPreferencesGroup.setLayout(gridLayout);

		detectorsExpandableComposite.setClient(detFoldersComp);

		ExpansionAdapter detFoldersListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.layoutFull(detFoldersComp.getParent());
			}
		};
		detectorsExpandableComposite.addExpansionListener(detFoldersListener);
		
		this.vortexSaveRawSpectrum = new BooleanWrapper(vortexPreferencesGroup, SWT.NONE);
		vortexSaveRawSpectrum.setText("Save raw spectrum to file");
		vortexSaveRawSpectrum.setValue(false);
		
		this.xspressOnlyShowFF = new BooleanWrapper(xspressPreferencesGroup, SWT.NONE);
		xspressOnlyShowFF.setText("Hide individual elements");
		xspressOnlyShowFF
				.setToolTipText("In ascii output, only display the total in-window counts (FF) from the Xspress detector");
		xspressOnlyShowFF.setValue(Boolean.FALSE);

		this.xspressShowDTRawValues = new BooleanWrapper(xspressPreferencesGroup, SWT.NONE);
		xspressShowDTRawValues.setText("Show DT values");
		xspressShowDTRawValues
				.setToolTipText("Add the raw scaler values used in deadtime (DT) calculations to ascii output");
		xspressShowDTRawValues.setValue(Boolean.FALSE);

		this.xspressSaveRawSpectrum = new BooleanWrapper(xspressPreferencesGroup, SWT.NONE);
		xspressSaveRawSpectrum.setText("Save raw spectrum to file");
		xspressSaveRawSpectrum.setValue(false);
		
		detectorsExpandableComposite.setClient(detFoldersComp);

		ExpansionAdapter detExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(vortexSaveRawSpectrum.getValue() || xspressOnlyShowFF.getValue() || xspressShowDTRawValues.getValue() || xspressSaveRawSpectrum.getValue())
					detectorsExpandableComposite.setExpanded(true);
				GridUtils.layoutFull(detFoldersComp.getParent());
			}
		};
		detectorsExpandableComposite.addExpansionListener(detExpansionListener);
		
		if(bean.isVortexSaveRawSpectrum() || bean.isXspressOnlyShowFF() || bean.isXspressSaveRawSpectrum() || bean.isXspressShowDTRawValues())
			detectorsExpandableComposite.setExpanded(true);
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

	public VerticalListEditor getSignalList() {
		return signalList;
	}

	public VerticalListEditor getMetadataList() {
		return metadataList;
	}

	public TextWrapper getAsciiFileName() {
		return asciiFileName;
	}	

	public BooleanWrapper getVortexSaveRawSpectrum() {
		return vortexSaveRawSpectrum;
	}

	public BooleanWrapper getXspressOnlyShowFF() {
		return xspressOnlyShowFF;
	}

	public BooleanWrapper getXspressShowDTRawValues() {
		return xspressShowDTRawValues;
	}

	public BooleanWrapper getXspressSaveRawSpectrum() {
		return xspressSaveRawSpectrum;
	}
}
