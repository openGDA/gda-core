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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.ActiveMode;
import org.eclipse.richbeans.widgets.FieldBeanComposite;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper.TEXT_TYPE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class B18SampleParametersUIEditor extends RichBeanEditorPart {

	private FieldComposite name;
	private FieldComposite description1;
	private FieldComposite description2;
	private ComboWrapper stage;
	private ComboWrapper sampleEnvironment;
	private List<String> selectedSampleStages;

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
	private Composite topComposite;
	private Composite bottomComposite;
	private StackLayout stageLayout;
	private Composite blankStageComposite;
	private StackLayout environmentLayout;
	private Composite blankEnvironmentComposite;
	private ScrolledComposite scrolledComposite;
	private Composite scrolledContents;
	private SampleWheelParametersComposite sampleWheelParametersComposite;
	private Group grpSampleWheel;

	private ExpandableComposite sampleStageExpandableComposite;
	private ExpandableComposite temperatureExpandableComposite;
	private ExpandableComposite wheelExpandableComposite;

	private B18SampleParameters bean;
	private Composite wheelComp;
	private Composite stageComp;
	private Composite tempComp;

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

	@Override
	public void createPartControl(Composite comp) {
		comp.setLayout(new FillLayout());

		scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		scrolledContents = new Composite(scrolledComposite, SWT.NONE);
		scrolledContents.setLayout(new GridLayout(1, false));

		topComposite = new Composite(scrolledContents, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(topComposite);

		Label nameLabel = new Label(topComposite, SWT.NONE);
		nameLabel.setText("Sample name");

		name = new TextWrapper(topComposite, SWT.BORDER | SWT.SINGLE);
		((TextWrapper) name).setTextType(TEXT_TYPE.FILENAME);
		GridDataFactory.fillDefaults().hint(294, SWT.DEFAULT).applyTo(name);

		Label descriptionLabel = new Label(topComposite, SWT.NONE);
		descriptionLabel.setText("Sample description");
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(descriptionLabel);

		description1 = new TextWrapper(topComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 53).applyTo(description1);

		Label commentsLabel = new Label(topComposite, SWT.NONE);
		commentsLabel.setText("Additional comments");
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(commentsLabel);

		description2 = new TextWrapper(topComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 53).applyTo(description2);

		bottomComposite = new Composite(scrolledContents, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(bottomComposite);

		sampleStageExpandableComposite = new ExpandableComposite(bottomComposite, SWT.NONE);
		sampleStageExpandableComposite.setText("Sample Stage");
		sampleStageExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		temperatureExpandableComposite = new ExpandableComposite(bottomComposite, SWT.NONE);
		temperatureExpandableComposite.setText("Temperature Controller");
		temperatureExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		wheelExpandableComposite = new ExpandableComposite(bottomComposite, SWT.NONE);
		wheelExpandableComposite.setText("Sample Wheel");
		wheelExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		// createStage();
		createStageShowAll();

		// stage
		ExpansionAdapter stageExpansionListener = getStageExpansionListener(sampleStageExpandableComposite);

		sampleStageExpandableComposite.addExpansionListener(stageExpansionListener);

//		if (!bean.getStage().toString().equals("none")) {
//			sampleStageExpandableComposite.setExpanded(true);
//			linkuiForDynamicLoading(false);
//			updateStageType();
//		}

		createTemp();

		// temp
		ExpansionAdapter tempExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if (!sampleEnvironment.getValue().toString().equals("none"))
					temperatureExpandableComposite.setExpanded(true);
				updateExpandable(temperatureExpandableComposite, null);
			}
		};
		temperatureExpandableComposite.addExpansionListener(tempExpansionListener);

		if (!bean.getTemperatureControl().toString().equals("none")) {
			temperatureExpandableComposite.setExpanded(true);
			linkuiForDynamicLoading(false);
			updateEnvironmentType();
		}

		// wheel
		createWheel();
		ExpansionAdapter wheelExpansionAdapter = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if (bean.getSampleWheelParameters().isWheelEnabled())
					wheelExpandableComposite.setExpanded(true);
				else
					wheelExpandableComposite.setExpanded(e.getState());
				updateExpandable(wheelExpandableComposite, null);
			}
		};
		wheelExpandableComposite.addExpansionListener(wheelExpansionAdapter);

		if (bean.getSampleWheelParameters().isWheelEnabled())
			wheelExpandableComposite.setExpanded(true);

		refreshScrolledContentsSize();

		name.addValueListener(listenerToUpdateBeanFromUI);
		description1.addValueListener(listenerToUpdateBeanFromUI);
		description2.addValueListener(listenerToUpdateBeanFromUI);
	}

	private ValueListener listenerToUpdateBeanFromUI = new ValueListener() {
		@Override
		public void valueChangePerformed(ValueEvent e) {
			try {
				if (isDirty()) {
					controller.uiToBean();
				}
			} catch (Exception e1) {
				logger.error("Problem updating UI from bean", e1);
			}
		}

		@Override
		public String getValueListenerName() {
			return null;
		}
	};

	private ExpansionAdapter getStageExpansionListener(final ExpandableComposite expandableComposite) {
		return getStageExpansionListener(expandableComposite, null);
	}

	private ExpansionAdapter getStageExpansionListener(final ExpandableComposite expandableComposite, final Button useSampleStageButton) {
		ExpansionAdapter extAdapter = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				// layout the expanded composite first...
				ExpandableComposite comp = (ExpandableComposite) e.getSource();
				GridUtils.layoutFull(comp);
				// Layout the sample stage composite...
				sampleStageExpandableComposite.setExpanded(true);
				updateExpandable(expandableComposite, useSampleStageButton);
			}
		};
		return extAdapter;
	}

	private void updateExpandable(ExpandableComposite expComposite, Button useSampleStageButton) {
		// First, get bean settings from ui
		try {
			controller.uiToBean();
			logger.debug("Sample name {}, Sample description {}, Sample comments {}", bean.getName(), bean.getDescription1(), bean.getDescription2());
		} catch (Exception e) {
			logger.error("Problem converting UI to bean {}", e);
		}
		// Make sure composite is always expanded if the sample stage is currently selected
		boolean forceExpanded = useSampleStageButton!=null && useSampleStageButton.getSelection()==true;
		if (forceExpanded) {
			expComposite.setExpanded(true);
		}

		GridUtils.layoutFull(expComposite);
		refreshScrolledContentsSize();
		linkuiForDynamicLoading(false);

		// Try to set focus on first element in the expandable composite (the twistie toggle at top of composite)
		Control[] childWidgets = expComposite.getChildren();
		if (childWidgets != null && childWidgets.length > 0) {
			logger.debug("Trying to set focus to {}. Has focus = {}", expComposite.getText(), childWidgets[0].setFocus());
		}
	}

	private void refreshScrolledContentsSize() {
		scrolledComposite.setContent(scrolledContents);
		scrolledComposite.setMinSize(scrolledContents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public void linkuiForDynamicLoading(@SuppressWarnings("unused") final boolean isPageChange) {
		try {
			controller.switchState(false);
			controller.beanToUI();
			controller.switchState(true);
		} catch (Exception e) {
			logger.error("Cannot save bean!", e);
		}
	}

	private ExpandableComposite addExpandableComposite( Composite parent, String label ) {
		ExpandableComposite expandableComposite = new ExpandableComposite(parent, SWT.NONE);
		expandableComposite.setText(label);
		expandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 2));
		return expandableComposite;
	}

	private Group addGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		group.setLayout(gridLayout);
		return group;
	}

	/**
	 * Add button to parent composite to control 'used' status of sample stage.
	 * @param parent
	 * @param stageType
	 * @return
	 */
	private Button addSampleStageIsActiveButton( Composite parent , final SAMPLESTAGE_TYPE stageType ) {
		final Button sampleStageSelected = new Button(parent, SWT.CHECK); // add checkbox
		sampleStageSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 2));
		sampleStageSelected.setText("Use sample stage");
		sampleStageSelected.setVisible( true );

		// Set checked status to correct value
		if ( selectedSampleStages.contains( stageType.getTypeString() ) )
			sampleStageSelected.setSelection( true );
		else
			sampleStageSelected.setSelection( false );

		sampleStageSelected.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// Update (string) list of selected sample stages
				if ( sampleStageSelected.getSelection() == true ) {
					selectedSampleStages.add( stageType.getTypeString() );
				} else
					while( selectedSampleStages.remove(stageType.getTypeString()) ); // remove all occurrences from list

				logger.debug("Selected sample stages {}", ArrayUtils.toString(selectedSampleStages));

				// update model with new list
				bean.setSelectedSampleStages(selectedSampleStages);

				// Notify editor that change has occurred (so the file can be saved after checkbox status changes)
				ValueEvent evt = new ValueEvent(sampleStageSelected, sampleStageSelected.getText() );
				valueChangePerformed(evt);
			}
		});

		return sampleStageSelected;
	}

	private void setupExpandableComposite( ExpandableComposite expandableComposite, Composite childComposite, final SAMPLESTAGE_TYPE stageType) {
		expandableComposite.setClient( childComposite );
		expandableComposite.setExpanded(false);
		Button useSampleStageButton = (Button)childComposite.getChildren()[0];
		ExpansionAdapter expansionListener = getStageExpansionListener(expandableComposite, useSampleStageButton);
		expandableComposite.addExpansionListener( expansionListener );
		GridUtils.layoutFull(expandableComposite);
		// Expand composite if stage is selected
		if ( selectedSampleStages.contains( stageType.getTypeString() ) )
				expandableComposite.setExpanded( true );
	}

	/**
	 * New sample stage selection widget to allow multiple stages to be selected and used in a scan.
	 * @since 27/4/2016
	 */
	private void createStageShowAll() {
		if (stageComp == null) {

			selectedSampleStages =  this.bean.getSelectedSampleStages();

			stageComp = new Composite(sampleStageExpandableComposite, SWT.NONE);
			GridLayout gridLayout_2 = new GridLayout();
			gridLayout_2.numColumns = 1;
			stageComp.setLayout(gridLayout_2);

			grpStage = new Group(stageComp, SWT.NONE);
			grpStage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 1;
			grpStage.setLayout(gridLayout);


			ExpandableComposite expComp = addExpandableComposite( grpStage, "xy theta stage" );
			Group grp = addGroup( expComp );
			addSampleStageIsActiveButton(grp, SAMPLESTAGE_TYPE.XY_THETA);
			xythetaStageParameters = new XYThetaStageComposite(grp, SWT.NONE, "sam2x", "sam2y", "sam2rot");
			xythetaStageParameters.setEditorClass(XYThetaStageParameters.class);
			setupExpandableComposite( expComp, grp , SAMPLESTAGE_TYPE.XY_THETA );


			expComp = addExpandableComposite( grpStage, "ln2 cryostage");
			grp = addGroup( expComp );
			addSampleStageIsActiveButton(grp, SAMPLESTAGE_TYPE.LN2_CRYO);
			ln2CryoStageComposite = new LN2CryoStageComposite(grp, SWT.NONE, bean );
			ln2CryoStageComposite.setEditorClass(LN2CryoStageParameters.class);
			setupExpandableComposite( expComp, grp, SAMPLESTAGE_TYPE.LN2_CRYO);


			expComp = addExpandableComposite( grpStage, "sx cryostage");
			grp = addGroup( expComp );
			addSampleStageIsActiveButton(grp, SAMPLESTAGE_TYPE.SX_CRYO);
			sxCryoStageComposite = new SXCryoStageComposite(grp, SWT.NONE, bean);
			sxCryoStageComposite.setEditorClass(SXCryoStageParameters.class);
			sxCryoStageComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);
			setupExpandableComposite( expComp, grp, SAMPLESTAGE_TYPE.SX_CRYO);


			expComp = addExpandableComposite( grpStage, "user stage");
			grp = addGroup( expComp );
			addSampleStageIsActiveButton(grp, SAMPLESTAGE_TYPE.USER);
			userStageComposite = new UserStageComposite(grp, SWT.NONE, "user2", "user4", "user5", "user6", "user7", "user8");
			userStageComposite.setEditorClass(UserStageParameters.class);
			userStageComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);
			setupExpandableComposite( expComp, grp, SAMPLESTAGE_TYPE.USER);

			sampleStageExpandableComposite.setClient(stageComp);

			if ( selectedSampleStages != null && selectedSampleStages.size() > 0 )
				sampleStageExpandableComposite.setExpanded(true);

		}
	}

	public void createStage() {
		if (stageComp == null) {

			stageComp = new Composite(sampleStageExpandableComposite, SWT.NONE);
			GridLayout gridLayout_2 = new GridLayout();
			gridLayout_2.numColumns = 2;
			stageComp.setLayout(gridLayout_2);

			grpStage = new Group(stageComp, SWT.NONE);
			grpStage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 2;
			grpStage.setLayout(gridLayout);

			Label label = new Label(grpStage, SWT.NONE);
			label.setSize(37, 17);
			label.setText("Stage");

			stage = new ComboWrapper(grpStage, SWT.READ_ONLY);
			stage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			stage.setSize(234, 27);
			stage.setItems(new String[] { "none", "xythetastage", "ln2cryostage", "sxcryostage", "userstage" });

			grpStageParameters = new Group(grpStage, SWT.NONE);
			stageLayout = new StackLayout();
			grpStageParameters.setLayout(stageLayout);
			grpStageParameters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			blankStageComposite = new Composite(grpStageParameters, SWT.NONE);

			xythetaStageParameters = new XYThetaStageComposite(grpStageParameters, SWT.NONE, "sam2x", "sam2y",
					"sam2rot");
			xythetaStageParameters.setVisible(true);
			xythetaStageParameters.setEditorClass(XYThetaStageParameters.class);
			xythetaStageParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);


			ln2CryoStageComposite = new LN2CryoStageComposite(grpStageParameters, SWT.NONE, bean);
			ln2CryoStageComposite.setVisible(true);
			ln2CryoStageComposite.setEditorClass(LN2CryoStageParameters.class);
			ln2CryoStageComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);

			sxCryoStageComposite = new SXCryoStageComposite(grpStageParameters, SWT.NONE, bean);
			sxCryoStageComposite.setVisible(true);
			sxCryoStageComposite.setEditorClass(SXCryoStageParameters.class);
			sxCryoStageComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);

			userStageComposite = new UserStageComposite(grpStageParameters, SWT.NONE, "user2", "user4", "user5", "user6", "user7", "user8");
			userStageComposite.setVisible(true);
			userStageComposite.setEditorClass(UserStageParameters.class);
			userStageComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);

			Control[] children = grpStageParameters.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control control = children[i];
				if (control instanceof FieldBeanComposite) {
					((FieldBeanComposite) control).addValueListener(new ValueListener() {
						@Override
						public void valueChangePerformed(ValueEvent e) {
							Object source = e.getSource();
							if (!(source instanceof ScaleBox)) {
								bean.setStage(stage.getItem(stage.getSelectionIndex()));
								updateStageType();
								linkUI(false);
							}
						}

						@Override
						public String getValueListenerName() {
							return null;
						}
					});
				}
			}

			stage.addValueListener(new ValueListener() {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					bean.setStage(stage.getItem(stage.getSelectionIndex()));
					updateStageType();
					linkUI(false);
				}

				@Override
				public String getValueListenerName() {
					return "stage";
				}
			});

			sampleStageExpandableComposite.setClient(stageComp);
		}
	}

	@SuppressWarnings("unused")
	public void createTemp() {
		if (tempComp == null) {

			tempComp = new Composite(temperatureExpandableComposite, SWT.NONE);
			GridLayout gridLayout_3 = new GridLayout();
			gridLayout_3.numColumns = 2;
			tempComp.setLayout(gridLayout_3);

			grpBeanComposite = new Group(tempComp, SWT.NONE);
			grpBeanComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			GridLayout gridLayout2 = new GridLayout();
			gridLayout2.numColumns = 2;
			grpBeanComposite.setLayout(gridLayout2);

			Label label = new Label(grpBeanComposite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			label.setText("temperatureControl");
			sampleEnvironment = new ComboWrapper(grpBeanComposite, SWT.READ_ONLY);
			sampleEnvironment.setItems(new String[] { "none", "pulsetubecryostat", "furnace", "lakeshore" });
			sampleEnvironment.select(0);
			sampleEnvironment.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

			sampleEnvironment.addValueListener(new ValueAdapter("sampleEnvironment") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateEnvironmentType();
				}
			});

			grpEnvironmentParameters = new Group(grpBeanComposite, SWT.NONE);
			environmentLayout = new StackLayout();
			grpEnvironmentParameters.setLayout(environmentLayout);
			grpEnvironmentParameters.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));

			blankEnvironmentComposite = new Composite(grpEnvironmentParameters, SWT.NONE);
			pulseTubeCryostatParameters = new B18PulseTubeCryostatComposite(grpEnvironmentParameters, SWT.NONE, bean);
			pulseTubeCryostatParameters.setVisible(false);
			pulseTubeCryostatParameters.setEditorClass(PulseTubeCryostatParameters.class);
			pulseTubeCryostatParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);

			new Label(pulseTubeCryostatParameters, SWT.NONE);
			new Label(pulseTubeCryostatParameters.getSetPoint(), SWT.NONE);
			new Label(pulseTubeCryostatParameters.getTolerance(), SWT.NONE);
			new Label(pulseTubeCryostatParameters.getTime(), SWT.NONE);

			furnaceParameters = new B18FurnaceComposite(grpEnvironmentParameters, SWT.NONE);
			furnaceParameters.setVisible(false);
			furnaceParameters.setEditorClass(FurnaceParameters.class);
			furnaceParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);

			new Label(furnaceParameters, SWT.NONE);
			new Label(furnaceParameters.getTolerance(), SWT.NONE);
			new Label(furnaceParameters.getTime(), SWT.NONE);

			lakeshoreComposite = new LakeshoreComposite(grpEnvironmentParameters, SWT.NONE);
			lakeshoreComposite.setVisible(false);
			lakeshoreComposite.setEditorClass(LakeshoreParameters.class);
			lakeshoreComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);

			new Label(lakeshoreComposite, SWT.NONE);
			new Label(lakeshoreComposite.getTolerance(), SWT.NONE);
			new Label(lakeshoreComposite.getTime(), SWT.NONE);

			temperatureExpandableComposite.setClient(tempComp);

			sampleEnvironment.addValueListener(listenerToUpdateBeanFromUI);
			pulseTubeCryostatParameters.addValueListener(listenerToUpdateBeanFromUI);
			furnaceParameters.addValueListener(listenerToUpdateBeanFromUI);
			lakeshoreComposite.addValueListener(listenerToUpdateBeanFromUI);
		}
	}

	public void createWheel() {
		if (wheelComp == null) {

			wheelComp = new Composite(wheelExpandableComposite, SWT.NONE);
			GridLayout gridLayout_4 = new GridLayout();
			gridLayout_4.numColumns = 2;
			wheelComp.setLayout(gridLayout_4);

			grpSampleWheel = new Group(wheelComp, SWT.NONE);
			grpSampleWheel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			GridLayout gridLayout3 = new GridLayout();
			gridLayout3.numColumns = 2;
			grpSampleWheel.setLayout(gridLayout3);

			sampleWheelParametersComposite = new SampleWheelParametersComposite(grpSampleWheel, SWT.NONE, bean);
			sampleWheelParametersComposite.setBounds(10, 29, 400, 230);
			sampleWheelParametersComposite.setEditorClass(SampleWheelParameters.class);
			sampleWheelParametersComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);
			sampleWheelParametersComposite.layout();

			sampleWheelParametersComposite.addValueListener(listenerToUpdateBeanFromUI);

			wheelExpandableComposite.setClient(wheelComp);
		}
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		try {
			GridUtils.startMultiLayout(scrolledComposite);
			super.linkUI(isPageChange);
			if (sampleEnvironment != null) {
				sampleEnvironment.select(sampleEnvironment.getSelectionIndex());
				updateEnvironmentType();
			}
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	private enum SAMPLESTAGE_TYPE {
		XY_THETA("xythetastage"), LN2_CRYO("ln2cryostage"), SX_CRYO("sxcryostage"), USER("userstage");
		private String typeString;
		SAMPLESTAGE_TYPE(String typeString) {
			this.setTypeString(typeString);
		}
		public String getTypeString() {
			return typeString;
		}
		public void setTypeString(String typeString) {
			this.typeString = typeString;
		}
	};

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

	public List<String> getSelectedSampleStages() {
		return selectedSampleStages;
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
}
