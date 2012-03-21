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

import gda.jython.JythonServerFacade;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.beans.exafs.b18.FurnaceParameters;
import uk.ac.gda.beans.exafs.b18.LN2CryoStageParameters;
import uk.ac.gda.beans.exafs.b18.LakeshoreParameters;
import uk.ac.gda.beans.exafs.b18.PulseTubeCryostatParameters;
import uk.ac.gda.beans.exafs.b18.SXCryoStageParameters;
import uk.ac.gda.beans.exafs.b18.SampleWheelParameters;
import uk.ac.gda.beans.exafs.b18.UserStageParameters;
import uk.ac.gda.beans.exafs.b18.XYThetaStageParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ui.composites.B18FurnaceComposite;
import uk.ac.gda.exafs.ui.composites.B18PulseTubeCryostatComposite;
import uk.ac.gda.exafs.ui.composites.LN2CryoStageComposite;
import uk.ac.gda.exafs.ui.composites.LakeshoreComposite;
import uk.ac.gda.exafs.ui.composites.SXCryoStageComposite;
import uk.ac.gda.exafs.ui.composites.SampleWheelParametersComposite;
import uk.ac.gda.exafs.ui.composites.UserStageComposite;
import uk.ac.gda.exafs.ui.composites.XYThetaStageComposite;
import uk.ac.gda.richbeans.ACTIVE_MODE;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 *
 */
public final class B18SampleParametersUIEditor extends RichBeanEditorPart {

	private FieldComposite name;
	private FieldComposite description1;
	private FieldComposite description2;
	private ComboWrapper stage;
	private ComboWrapper sampleEnvironment;

	private XYThetaStageComposite xythetaStageParameters;
	private UserStageComposite userStageComposite;
	private LN2CryoStageComposite ln2CryoStageComposite;
	private SXCryoStageComposite sxCryoStageComposite;

	private B18PulseTubeCryostatComposite pulseTubeCryostatParameters;
	private B18FurnaceComposite furnaceParameters;
	private LakeshoreComposite lakeshoreComposite;
	private Group grpStageParameters;
	private Group grpEnvironmentParameters;
	private Group grpStage;
	private Group grpBeanComposite;
	private Composite composite;
	private StackLayout stageLayout;
	private Composite blankStageComposite;
	private StackLayout environmentLayout;
	private Composite blankEnvironmentComposite;
	private ScrolledComposite topComposite;
	private SampleWheelParametersComposite sampleWheelParametersComposite;
	private Group grpSampleWheel;

	ExpandableComposite sampleStageExpandableComposite;
	ExpandableComposite temperatureExpandableComposite;
	ExpandableComposite wheelExpandableComposite;
	
	
	private TextWrapper afterScanscriptName;
	private TextWrapper beforeScanscriptName;

	private VerticalListEditor signalList;
	private BooleanWrapper signalActive;

	private VerticalListEditor metadataList;
	private BooleanWrapper metadataActive;
	
	B18SampleParameters bean;
	
	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public B18SampleParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		bean = (B18SampleParameters) editingBean;
	}

	@Override
	public String getRichEditorTabText() {
		return "B18SampleParametersEditor";
	}

	/**
	 * 
	 */
	@Override
	public void createPartControl(Composite comp) {
		comp.setLayout(new FillLayout());

		topComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		topComposite.setExpandHorizontal(true);
		topComposite.setExpandVertical(true);

		final Composite container = new Composite(topComposite, SWT.NONE);
		container.setLayout(new GridLayout(2, false));

		composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.NONE);
		label.setSize(37, 17);
		label.setText("Filename");
		this.name = new TextWrapper(composite, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		name.setSize(234, 21);

		new Label(composite, SWT.NONE);

		label = new Label(composite, SWT.NONE);
		label.setSize(72, 17);
		label.setText("Sample description");
		this.description1 = new TextWrapper(composite, SWT.NONE);
		description1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		description1.setSize(234, 21);

		new Label(composite, SWT.NONE);
		
		label = new Label(composite, SWT.NONE);
		label.setSize(72, 17);
		label.setText("Additional comments");
		this.description2 = new TextWrapper(composite, SWT.NONE);
		description2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		description2.setSize(234, 21);

		new Label(composite, SWT.NONE);

		sampleStageExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		sampleStageExpandableComposite.setText("Sample Stage");
		sampleStageExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite stageComp = new Composite(sampleStageExpandableComposite, SWT.NONE);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		stageComp.setLayout(gridLayout_2);

		grpStage = new Group(stageComp, SWT.NONE);
		grpStage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		grpStage.setLayout(gridLayout);

		label = new Label(grpStage, SWT.NONE);
		label.setSize(37, 17);
		label.setText("Stage");
		stage = new ComboWrapper(grpStage, SWT.READ_ONLY);
		stage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		stage.setSize(234, 27);
		stage.setItems(new String[] { "none", "xythetastage", "ln2cryostage", "sxcryostage", "userstage" });
		stage.select(0);

		grpStageParameters = new Group(grpStage, SWT.NONE);
		stageLayout = new StackLayout();
		grpStageParameters.setLayout(stageLayout);
		grpStageParameters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		blankStageComposite = new Composite(grpStageParameters, SWT.NONE);

		xythetaStageParameters = new XYThetaStageComposite(grpStageParameters, SWT.NONE, "sam2x", "sam2y", "sam2rot");
		xythetaStageParameters.setVisible(true);
		xythetaStageParameters.setEditorClass(XYThetaStageParameters.class);
		xythetaStageParameters.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);

		ln2CryoStageComposite = new LN2CryoStageComposite(grpStageParameters, SWT.NONE,
				(B18SampleParameters) editingBean);
		ln2CryoStageComposite.setVisible(true);
		ln2CryoStageComposite.setEditorClass(LN2CryoStageParameters.class);
		ln2CryoStageComposite.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);

		sxCryoStageComposite = new SXCryoStageComposite(grpStageParameters, SWT.NONE, (B18SampleParameters) editingBean);
		sxCryoStageComposite.setVisible(true);
		sxCryoStageComposite.setEditorClass(SXCryoStageParameters.class);
		sxCryoStageComposite.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);

		userStageComposite = new UserStageComposite(grpStageParameters, SWT.NONE, "user2", "user4", "user5", "user6", "user7", "user8");
		userStageComposite.setVisible(true);
		userStageComposite.setEditorClass(UserStageParameters.class);
		userStageComposite.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);
		
		temperatureExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		temperatureExpandableComposite.setText("Temperature Controller");
		temperatureExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite tempComp = new Composite(temperatureExpandableComposite, SWT.NONE);
		final GridLayout gridLayout_3 = new GridLayout();
		gridLayout_3.numColumns = 2;
		tempComp.setLayout(gridLayout_3);

		grpBeanComposite = new Group(tempComp, SWT.NONE);
		grpBeanComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		final GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		grpBeanComposite.setLayout(gridLayout2);

		label = new Label(grpBeanComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("temperatureControl");
		this.sampleEnvironment = new ComboWrapper(grpBeanComposite, SWT.READ_ONLY);
		sampleEnvironment.setItems(new String[] { "none", "pulsetubecryostat", "furnace", "lakeshore" });
		sampleEnvironment.select(0);
		sampleEnvironment.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		grpEnvironmentParameters = new Group(grpBeanComposite, SWT.NONE);
		environmentLayout = new StackLayout();
		grpEnvironmentParameters.setLayout(environmentLayout);
		grpEnvironmentParameters.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));

		wheelExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		wheelExpandableComposite.setText("Sample Wheel");
		wheelExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Composite wheelComp = new Composite(wheelExpandableComposite, SWT.NONE);
		final GridLayout gridLayout_4 = new GridLayout();
		gridLayout_4.numColumns = 2;
		wheelComp.setLayout(gridLayout_4);

		grpSampleWheel = new Group(wheelComp, SWT.NONE);
		grpSampleWheel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		final GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 2;
		grpSampleWheel.setLayout(gridLayout3);

		sampleWheelParametersComposite = new SampleWheelParametersComposite(grpSampleWheel, SWT.NONE,
				(B18SampleParameters) editingBean);
		sampleWheelParametersComposite.setBounds(10, 29, 400, 230);
		sampleWheelParametersComposite.setEditorClass(SampleWheelParameters.class);
		sampleWheelParametersComposite.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);
		sampleWheelParametersComposite.layout();

		blankEnvironmentComposite = new Composite(grpEnvironmentParameters, SWT.NONE);
		pulseTubeCryostatParameters = new B18PulseTubeCryostatComposite(grpEnvironmentParameters, SWT.NONE,
				(B18SampleParameters) editingBean);
		pulseTubeCryostatParameters.setVisible(false);
		pulseTubeCryostatParameters.setEditorClass(PulseTubeCryostatParameters.class);
		pulseTubeCryostatParameters.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);
		new Label(pulseTubeCryostatParameters, SWT.NONE);
		new Label(pulseTubeCryostatParameters.getSetPoint(), SWT.NONE);
		new Label(pulseTubeCryostatParameters.getTolerance(), SWT.NONE);
		new Label(pulseTubeCryostatParameters.getTime(), SWT.NONE);

		furnaceParameters = new B18FurnaceComposite(grpEnvironmentParameters, SWT.NONE);
		furnaceParameters.setVisible(false);
		furnaceParameters.setEditorClass(FurnaceParameters.class);
		furnaceParameters.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);
		new Label(furnaceParameters, SWT.NONE);
		new Label(furnaceParameters.getTolerance(), SWT.NONE);
		new Label(furnaceParameters.getTime(), SWT.NONE);

		lakeshoreComposite = new LakeshoreComposite(grpEnvironmentParameters, SWT.NONE);
		lakeshoreComposite.setVisible(false);
		lakeshoreComposite.setEditorClass(LakeshoreParameters.class);
		lakeshoreComposite.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);
		new Label(lakeshoreComposite, SWT.NONE);
		new Label(lakeshoreComposite.getTolerance(), SWT.NONE);
		new Label(lakeshoreComposite.getTime(), SWT.NONE);
		new Label(container, SWT.NONE);

		topComposite.setContent(container);
		topComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		sampleStageExpandableComposite.setClient(stageComp);
		temperatureExpandableComposite.setClient(tempComp);
		wheelExpandableComposite.setClient(wheelComp);

		ExpansionAdapter stageExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(!stage.getValue().toString().equals("none"))
					sampleStageExpandableComposite.setExpanded(true);
				GridUtils.layoutFull(stageComp.getParent());
			}
		};
		sampleStageExpandableComposite.addExpansionListener(stageExpansionListener);

		if(!bean.getStage().toString().equals("none"))
			sampleStageExpandableComposite.setExpanded(true);
		
		ExpansionAdapter tempExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(!sampleEnvironment.getValue().toString().equals("none"))
					temperatureExpandableComposite.setExpanded(true);
				GridUtils.layoutFull(tempComp.getParent());
			}
		};
		temperatureExpandableComposite.addExpansionListener(tempExpansionListener);

		if(!bean.getTemperatureControl().toString().equals("none"))
			temperatureExpandableComposite.setExpanded(true);
		
		ExpansionAdapter wheelExpansionAdapter = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(sampleWheelParametersComposite.getWheelEnabled().getValue())
					wheelExpandableComposite.setExpanded(true);
				GridUtils.layoutFull(wheelComp.getParent());
			}
		};
		wheelExpandableComposite.addExpansionListener(wheelExpansionAdapter);

		if(bean.getSampleWheelParameters().isWheelEnabled())
			wheelExpandableComposite.setExpanded(true);
	}

	@Override
	public void linkUI(final boolean isPageChange) {

		sampleEnvironment.addValueListener(new ValueAdapter("sampleEnvironment") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateEnvironmentType();
			}
		});

		stage.addValueListener(new ValueAdapter("stage") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateStageType();
			}
		});

		try {
			GridUtils.startMultiLayout(topComposite);
			super.linkUI(isPageChange);

			// Now the data will have one of the complex types so we can init the envType
			initOptions();
			updateEnvironmentType();
			updateStageType();
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	private void initOptions() {
		final B18SampleParameters params = (B18SampleParameters) editingBean;

		final List<String> envItems = Arrays.asList(sampleEnvironment.getItems());
		final int envIndex = envItems.indexOf(params.getTemperatureControl());
		sampleEnvironment.select(envIndex);

		final List<String> stageItems = Arrays.asList(stage.getItems());
		final int stageIndex = stageItems.indexOf(params.getStage());
		stage.select(stageIndex);
	}

	private void updateStageType() {
		switch (stage.getSelectionIndex()) {
		case 0:
			stageLayout.topControl = blankStageComposite;
			break;
		case 1:
			stageLayout.topControl = xythetaStageParameters;
			break;
		case 2:
			stageLayout.topControl = ln2CryoStageComposite;
			break;
		case 3:
			stageLayout.topControl = sxCryoStageComposite;
			break;
		case 4:
			stageLayout.topControl = userStageComposite;
			break;
		}
		GridUtils.layoutFull(grpStageParameters);
	}

	private void updateEnvironmentType() {
		switch (sampleEnvironment.getSelectionIndex()) {
		case 0:
			environmentLayout.topControl = blankEnvironmentComposite;
			break;
		case 1:
			environmentLayout.topControl = pulseTubeCryostatParameters;
			break;
		case 2:
			environmentLayout.topControl = furnaceParameters;
			break;
		case 3:
			environmentLayout.topControl = lakeshoreComposite;
			break;
		}
		GridUtils.layoutFull(grpEnvironmentParameters);
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

	@Override
	public void setFocus() {
	}

	public FieldComposite getName() {
		return name;
	}

	public FieldComposite getDescription1() {
		return description1;
	}
	
	public FieldComposite getDescription2() {
		return description2;
	}

	public FieldComposite getTemperatureControl() {
		return sampleEnvironment;
	}

	public FieldComposite getStage() {
		return stage;
	}

	public XYThetaStageComposite getXYThetaStageParameters() {
		return xythetaStageParameters;
	}

	public LN2CryoStageComposite getLN2CryoStageParameters() {
		return ln2CryoStageComposite;
	}

	public SXCryoStageComposite getSXCryoStageParameters() {
		return sxCryoStageComposite;
	}

	public UserStageComposite getUserStageParameters() {
		return userStageComposite;
	}
	
	public B18PulseTubeCryostatComposite getPulseTubeCryostatParameters() {
		return pulseTubeCryostatParameters;
	}

	public B18FurnaceComposite getFurnaceParameters() {
		return furnaceParameters;
	}

	public LakeshoreComposite getLakeshoreParameters() {
		return lakeshoreComposite;
	}

	public SampleWheelParametersComposite getSampleWheelParameters() {
		return sampleWheelParametersComposite;
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

	public BooleanWrapper getSignalActive() {
		return signalActive;
	}
	
	public BooleanWrapper getMetadataActive() {
		return metadataActive;
	}

	public VerticalListEditor getMetadataList(){
		return metadataList;
	}
}
