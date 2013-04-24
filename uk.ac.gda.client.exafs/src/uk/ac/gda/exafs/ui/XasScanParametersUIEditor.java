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

import gda.util.Converter;
import gda.util.exafs.Element;

import java.text.DecimalFormat;
import java.util.EventObject;
import java.util.List;

import org.dawnsci.plotting.api.annotation.AnnotationUtils;
import org.dawnsci.plotting.api.annotation.IAnnotation;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.ACTIVE_MODE;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBoxAndFixedExpression;
import uk.ac.gda.richbeans.components.scalebox.ScaleBoxAndFixedExpression.ExpressionProvider;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.util.schema.SchemaReader;

/**
 * An editor part designed to be a page in a multipage editor. This class was auto-generated using RCP Developer and
 * extends RichBeanEditorPart which provides the link between the editor and the bean. Only fields which implement
 * IFieldWidget will be synchronised with the bean automatically. Any field which is both an IFieldWidget and named the
 * same as a field in the bean will find its way into the bean and from the bean when the editor is opened.
 */
public class XasScanParametersUIEditor extends ElementEdgeEditor implements IPropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(XasScanParametersUIEditor.class);

	private ComboWrapper exafsTimeType;
	private ComboWrapper abGafChoice;
	private ComboWrapper exafsStepType;

	private ScaleBox gaf3;
	private ScaleBox gaf2;
	private ScaleBox gaf1;
	private ScaleBox b, a, preEdgeTime, exafsTime, exafsStep, edgeTime, edgeStep, preEdgeStep, initialEnergy;
	private ScaleBox exafsFromTime;
	private ScaleBox exafsToTime;

	private ScaleBoxAndFixedExpression finalEnergy, c;

	private Link aLabel, bLabel, cLabel, e0Label, e1Label;

	private Label exafsFromLabel, exafsToLabel;
	private Label exafsStepLabel;
	private Label exafsStepEnergyLabel;
	private Label kWeightingLabel;
	private Label kStartLabel;

	private SelectionAdapter e0Listener, e1Listener, abGafListener, aListener, bListener, cListener, exafsTimeListener;

	private ScaleBox kWeighting;
	private ScaleBoxAndFixedExpression kStart;

	private String cachedElement;

	private Group topCentre;
	private Label gaf3Label;
	private GridData gd_centre;
	private GridLayout gridLayout_1;

	private boolean energyInK = ExafsActivator.getDefault().getPreferenceStore()
			.getBoolean(ExafsPreferenceConstants.EXAFS_FINAL_ANGSTROM);
	private IRegion aLine;
	private IRegion bLine;
	private IRegion cLine;
	private IRegion edgeLine;
	private boolean showLineAnnotations = false;
	private XasScanParameters bean;
	
	public XasScanParametersUIEditor(final String path, final RichBeanMultiPageEditorPart containingEditor,
			final XasScanParameters xasScanParameters) {

		super(path, containingEditor.getMappingUrl(), containingEditor, xasScanParameters);

		bean = xasScanParameters;
		
		containingEditor.addPageChangedListener(new IPageChangedListener() {
			@Override
			public void pageChanged(PageChangedEvent event) {
				final int ipage = containingEditor.getActivePage();
				if (ipage == 1)
					cachedElement = ((XasScanParameters) xasScanParameters).getElement();
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {

		this.scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		// Automatic section generated by RCP Developer
		this.container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new FillLayout());
		scrolledComposite.setContent(container);

		final Group main = new Group(container, SWT.NONE);
		main.setText("XAS Parameters");
		main.setLayout(new GridLayout(3, false));

		createElementEdgeArea(main);

		final Composite centre = new Composite(main, SWT.NONE);
		centre.setLayout(new GridLayout(1, false));
		centre.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		createScanParameters(centre);
		createStepParameters(centre);

		final Composite right = new Composite(main, SWT.NONE);
		right.setLayout(new GridLayout(1, false));
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.expandContainer = main;

		createEstimationComposite(right);
		createPlotRegions();

		scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		ExafsActivator.getDefault().getPreferenceStore().addPropertyChangeListener(this);

		updateEdgeRegion();

		updateExafsTimeType();
		
		updateLayout();
	}

	/**
	 * Creates the ILineTrace and the IRegions for specifying the locations of the boundaries
	 */
	@SuppressWarnings("unused")
	private void createPlotRegions() {

		try {
			this.aLine = plottingsystem.createRegion("a", RegionType.XAXIS_LINE);
			aLine.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			plottingsystem.addRegion(aLine);
			new ARegionSynchronizer(aLine, getA(), getGaf1());
			aLine.setMobile(ExafsActivator.getDefault().getPreferenceStore()
					.getBoolean(ExafsPreferenceConstants.EXAFS_GRAPH_EDITABLE));
		} catch (Exception e) {
			logger.error("Cannot create region for position of a!", e);
		}

		try {
			this.bLine = plottingsystem.createRegion("b", RegionType.XAXIS_LINE);
			bLine.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
			plottingsystem.addRegion(bLine);
			new BRegionSynchronizer(bLine, getB(), getC(), getGaf2(), getGaf3());
			bLine.setMobile(ExafsActivator.getDefault().getPreferenceStore()
					.getBoolean(ExafsPreferenceConstants.EXAFS_GRAPH_EDITABLE));
		} catch (Exception e) {
			logger.error("Cannot create region for position of b!", e);
		}

		try {
			this.cLine = plottingsystem.createRegion("c", RegionType.XAXIS_LINE);
			cLine.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			plottingsystem.addRegion(cLine);
			new CRegionSynchronizer(cLine, getC(), getGaf3());
			cLine.setMobile(ExafsActivator.getDefault().getPreferenceStore()
					.getBoolean(ExafsPreferenceConstants.EXAFS_GRAPH_EDITABLE)
					&& !ExafsActivator.getDefault().getPreferenceStore()
							.getBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK));
		} catch (Exception e) {
			logger.error("Cannot create region for position of c!", e);
		}

		try {
			this.edgeLine = plottingsystem.createRegion("edge", RegionType.XAXIS_LINE);
			edgeLine.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			plottingsystem.addRegion(edgeLine);
			new EdgeRegionSynchronizer(edgeLine, getEdgeEnergy());
			edgeLine.setMobile(false);
		} catch (Exception e) {
			logger.error("Cannot create region for position of edge energy!", e);
		}
	}

	public void drawLines() {

		double aEnergy = getA().getNumericValue();
		double bEnergy = getB().getNumericValue();
		double edgeEnergy = getEdgeEnergy().getNumericValue();
		double cEnergy = getC().getNumericValue();
		if (energyInK) {
			cEnergy = getKInEv().getValue(cEnergy);
		}

		double[] pnt = new double[] { aEnergy, 0d };
		aLine.setROI(new LinearROI(pnt, pnt));
		pnt = new double[] { bEnergy, 0d };
		bLine.setROI(new LinearROI(pnt, pnt));
		pnt = new double[] { cEnergy, 0d };
		cLine.setROI(new LinearROI(pnt, pnt));
		pnt = new double[] { edgeEnergy, 0d };
		edgeLine.setROI(new LinearROI(pnt, pnt));

		plottingsystem.repaint();

	}

	private abstract class RegionSynchronizer extends ValueAdapter implements IROIListener {
		protected ScaleBox abc;

		RegionSynchronizer(IRegion line, ScaleBox abc) {
			line.addROIListener(this);

			this.abc = abc;
			abc.addValueListener(this);
			this.name = "RegionSynchronizer";
		}

		protected abstract Double getNewEnergyFromScaleBox(final ValueEvent e);

		protected abstract void updateScaleBoxes(final double energy, final EventObject e);

		@Override
		public void roiDragged(ROIEvent evt) {
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			update(evt);
		}

		@Override
		public void valueChangePerformed(ValueEvent e) {
			update(e);
		}

		@Override
		public void roiSelected(ROIEvent evt) {
		}

		protected void update(EventObject e) {
			if (suspendGraphUpdate)
				return;
			try {
				suspendGraphUpdate = true;
				Double newEnergy;
				// event from one of the ScaleBoxes
				if (e instanceof ValueEvent) {
					newEnergy = getNewEnergyFromScaleBox((ValueEvent) e);
				}
				// event from the ROI in the graph being dragged
				else {
					IROI roi = ((ROIEvent) e).getROI();
					newEnergy = roi.getPoint()[0];
				}
				updateScaleBoxes(newEnergy, e);
				drawLines();
				updatePlottedPoints();
			} finally {
				suspendGraphUpdate = false;
			}
		}
	}

	private class ARegionSynchronizer extends RegionSynchronizer {

		private ScaleBox gaf;

		ARegionSynchronizer(IRegion line, ScaleBox abc, ScaleBox gaf) {
			super(line, abc);
			this.gaf = gaf;
			this.gaf.addValueListener(this);
			this.name = "A Region Synchronizer";
		}

		@Override
		protected void updateScaleBoxes(double energy, final EventObject e) {
			if (e.getSource() != abc) {
				abc.off();
				abc.setNumericValue(energy);
				abc.on();
			}
			if (e.getSource() != gaf) {
				gaf.off();
				gaf.setNumericValue(calcGaf1or2(energy));
				gaf.on();
			}
		}

		@Override
		protected Double getNewEnergyFromScaleBox(ValueEvent e) {
			Double energy;
			if (e.getSource() == abc) {
				energy = abc.getNumericValue();
			} else {
				energy = calcAorB(gaf.getNumericValue());
			}
			return energy;
		}
	}

	private class BRegionSynchronizer extends RegionSynchronizer {

		private ScaleBox gafb;
		private ScaleBox gafc;
		private ScaleBox cScaleBox;

		BRegionSynchronizer(IRegion line, ScaleBox abc, ScaleBox abc3, ScaleBox gaf2, ScaleBox gaf3) {
			super(line, abc);
			this.cScaleBox = abc3;
			this.gafb = gaf2;
			this.gafb.addValueListener(this);
			this.gafc = gaf3;
			this.name = "B Region Synchronizer";
		}

		@Override
		protected void updateScaleBoxes(double energy, final EventObject e) {
			if (e.getSource() != abc) {
				abc.off();
				abc.setNumericValue(energy);
				abc.on();
			}
			if (e.getSource() != gafb) {
				gafb.off();
				gafb.setNumericValue(calcGaf1or2(energy));
				gafb.on();
			}
			if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK)) {
				Double newGafCValue = calcGaf3(energy);
				abc.off();
				cScaleBox.off();
				gafc.setValue(newGafCValue);
				cScaleBox.setNumericValue(calcC(newGafCValue));
				abc.on();
				cScaleBox.on();
			}
		}

		@Override
		protected Double getNewEnergyFromScaleBox(ValueEvent e) {
			Double energy;
			if (e.getSource() == abc) {
				energy = abc.getNumericValue();
			} else {
				energy = calcAorB(gafb.getNumericValue());
			}
			return energy;
		}
	}

	private class CRegionSynchronizer extends RegionSynchronizer {

		private ScaleBox gaf;

		CRegionSynchronizer(IRegion line, ScaleBox abc, ScaleBox gaf3) {
			super(line, abc);
			this.gaf = gaf3;
			this.gaf.addValueListener(this);
			this.name = "C Region Synchronizer";
		}

		@Override
		protected void updateScaleBoxes(double energy, final EventObject e) {
			if (e.getSource() != abc) {
				abc.off();
				abc.setNumericValue(energy);
				abc.on();
			}
			if (e.getSource() != gaf) {
				gaf.off();
				gaf.setNumericValue(calcGaf3(energy));
				gaf.on();
			}
		}

		@Override
		protected Double getNewEnergyFromScaleBox(ValueEvent e) {
			Double energy;
			if (e.getSource() == abc) {
				energy = abc.getNumericValue();
				if (energyInK) {
					energy = getKInEv().getValue(energy);
				}
			} else {
				energy = calcC(gaf.getNumericValue());
			}
			return energy;
		}
	}

	private class EdgeRegionSynchronizer extends RegionSynchronizer {

		EdgeRegionSynchronizer(IRegion line, ScaleBox abc) {
			super(line, abc);
			this.name = "Edge Region Synchronizer";
		}

		@Override
		protected void updateScaleBoxes(double energy, final EventObject e) {
			// not need to update the UI if it was the source of the original event
			if (e instanceof ROIEvent) {
				abc.off();
				abc.setNumericValue(energy);
				abc.on();
			}
		}

		@Override
		protected Double getNewEnergyFromScaleBox(ValueEvent e) {
			return abc.getNumericValue();
		}
	}

	private void createStepParameters(Composite centre) {

		final Group edgeParametersGroup = new Group(centre, SWT.NONE);
		edgeParametersGroup.setText("Step Parameters");
		final GridData gd_edgeParametersGroup = new GridData(SWT.FILL, SWT.TOP, false, false);
		gd_edgeParametersGroup.widthHint = 450;
		edgeParametersGroup.setLayoutData(gd_edgeParametersGroup);
		final GridLayout gridLayout_4 = new GridLayout();
		gridLayout_4.numColumns = 2;
		edgeParametersGroup.setLayout(gridLayout_4);

		final Label stepLabel = new Label(edgeParametersGroup, SWT.NONE);
		stepLabel.setText("Pre-Edge Energy Step");

		preEdgeStep = new ScaleBox(edgeParametersGroup, SWT.NONE);
		preEdgeStep.setMaximum(20);
		preEdgeStep.setMinimum(0.1);
		preEdgeStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		preEdgeStep.setUnit("eV");
		preEdgeStep.setDoNotUseExpressions(true);

		final Label preedgeStepTimeLabel = new Label(edgeParametersGroup, SWT.NONE);
		preedgeStepTimeLabel.setText("Pre-Edge Time Step");

		preEdgeTime = new ScaleBox(edgeParametersGroup, SWT.NONE);
		preEdgeTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		preEdgeTime.setUnit("s");
		preEdgeTime.setMaximum(20);
		preEdgeTime.setMinimum(0.1);
		preEdgeTime.setDoNotUseExpressions(true);

		final Label stepLabel_1 = new Label(edgeParametersGroup, SWT.NONE);
		stepLabel_1.setText("Edge Energy Step");

		edgeStep = new ScaleBox(edgeParametersGroup, SWT.NONE);
		edgeStep.setMinimum(0.1);
		edgeStep.setMaximum(20);
		edgeStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		edgeStep.setUnit("eV");
		edgeStep.setDoNotUseExpressions(true);

		final Label edgeStepTimeLabel = new Label(edgeParametersGroup, SWT.NONE);
		edgeStepTimeLabel.setText("Edge Time Step");

		edgeTime = new ScaleBox(edgeParametersGroup, SWT.NONE);
		edgeTime.setMinimum(0.1);
		edgeTime.setMaximum(20);
		edgeTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		edgeTime.setUnit("s");
		edgeTime.setDoNotUseExpressions(true);

		final Label scanTypeLabel = new Label(edgeParametersGroup, SWT.NONE);
		scanTypeLabel.setText("Exafs Step Type");

		exafsStepType = new ComboWrapper(edgeParametersGroup, SWT.READ_ONLY);
		final GridData gd_postEdgeStepType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_postEdgeStepType.minimumWidth = 100;
		exafsStepType.setLayoutData(gd_postEdgeStepType);

		this.exafsStepEnergyLabel = new Label(edgeParametersGroup, SWT.NONE);
		exafsStepEnergyLabel.setText("                ");

		exafsStep = new ScaleBox(edgeParametersGroup, SWT.NONE);
		exafsStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		exafsStep.setUnit("eV");
		exafsStep.setMaximum(100d);
		exafsStep.setDecimalPlaces(3);
		exafsStep.setDoNotUseExpressions(true);

		final Label exafsTimeTypeLabel = new Label(edgeParametersGroup, SWT.NONE);
		exafsTimeTypeLabel.setText("Exafs Time Type");

		exafsTimeType = new ComboWrapper(edgeParametersGroup, SWT.READ_ONLY);
		exafsTimeType.setItems(new String[] { "Constant Time", "Variable Time" });
		exafsTimeType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		this.exafsStepLabel = new Label(edgeParametersGroup, SWT.NONE);
		exafsStepLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		exafsStepLabel.setText("Exafs Time Step");

		exafsTime = new ScaleBox(edgeParametersGroup, SWT.NONE);
		exafsTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		exafsTime.setUnit("s");
		exafsTime.setMaximum(20d);
		exafsTime.setDoNotUseExpressions(true);

		this.exafsFromLabel = new Label(edgeParametersGroup, SWT.NONE);
		exafsFromLabel.setText("Exafs From Time");

		this.exafsFromTime = new ScaleBox(edgeParametersGroup, SWT.NONE);
		exafsFromTime.setMinimum(0.1);
		exafsFromTime.setMaximum(20);
		exafsFromTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		exafsFromTime.setUnit("s");
		exafsFromTime.setDoNotUseExpressions(true);

		this.exafsToLabel = new Label(edgeParametersGroup, SWT.NONE);
		exafsToLabel.setText("Exafs To Time");

		this.exafsToTime = new ScaleBox(edgeParametersGroup, SWT.NONE);
		exafsToTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		exafsToTime.setMaximum(20);
		exafsToTime.setMinimum(exafsFromTime);
		exafsToTime.setUnit("s");
		exafsToTime.setDoNotUseExpressions(true);

		this.kWeightingLabel = new Label(edgeParametersGroup, SWT.NONE);
		kWeightingLabel.setText("K Weighting");

		this.kWeighting = new ScaleBox(edgeParametersGroup, SWT.NONE);
		kWeighting.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		kWeighting.setMaximum(3);
	}

	private void createScanParameters(Composite centre) {

		topCentre = new Group(centre, SWT.BORDER);
		topCentre.setText("Scan Parameters");

		gd_centre = new GridData(SWT.FILL, SWT.TOP, false, false);
		gd_centre.minimumWidth = 250;
		gd_centre.widthHint = 500;
		topCentre.setLayoutData(gd_centre);
		gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		topCentre.setLayout(gridLayout_1);

		this.e0Label = new Link(topCentre, SWT.NONE);
		e0Label.setText("<a>Initial Energy</a>");
		e0Label.setToolTipText("Click to open preferences (and allow/disallow editing initial energy).");
		this.e0Listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openPreferences();
			}

		};
		e0Label.addSelectionListener(e0Listener);

		initialEnergy = new ScaleBox(topCentre, SWT.NONE);
		initialEnergy.setDoNotUseExpressions(true);
		initialEnergy.setUnit("eV");
		initialEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		this.e1Label = new Link(topCentre, SWT.NONE);
		e1Label.setText("<a>Final Energy</a>");
		e1Label.setToolTipText("Click to open preferences (and allow/disallow editing final energy).");
		this.e1Listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openPreferences();
			}
		};
		e1Label.addSelectionListener(e1Listener);

		if (energyInK) {
			finalEnergy = new ScaleBoxAndFixedExpression(topCentre, SWT.NONE, getKInEv());
			finalEnergy.setDoNotUseExpressions(true);
			finalEnergy.setLabelWidth(10000);
			finalEnergy.setLabelDecimalPlaces(1);
			finalEnergy.setLabelUnit("eV");
			finalEnergy.setPrefix(" ");
			finalEnergy.setUnit("Å\u207B\u00b9");
			finalEnergy.setMaximum(100.0);
			finalEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			initialEnergy.setMaximum(40000.0);
			finalEnergy.setMinimum(0);
		} else {
			finalEnergy = new ScaleBoxAndFixedExpression(topCentre, SWT.NONE, getKProvider());
			finalEnergy.setDoNotUseExpressions(true);
			finalEnergy.setLabelUnit("Å\u207B\u00b9");
			finalEnergy.setLabelWidth(100);
			finalEnergy.setLabelDecimalPlaces(3);
			finalEnergy.setPrefix(" ");
			finalEnergy.setUnit("eV");
			finalEnergy.setMaximum(40000.0);
			finalEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			initialEnergy.setMaximum(finalEnergy);
			finalEnergy.setMinimum(initialEnergy);

		}

		final Label edgeRegionLabel = new Label(topCentre, SWT.NONE);
		edgeRegionLabel.setText("Edge Region");

		abGafChoice = new ComboWrapper(topCentre, SWT.READ_ONLY);
		abGafChoice.setItems(new String[] { "A/B", "Gaf1/Gaf2" });
		abGafChoice.select(0);
		abGafChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Label gaf1Label = new Label(topCentre, SWT.NONE);
		gaf1Label.setText("Gaf1");
		gaf1Label.setToolTipText("Gamma function 1: B = Edge Energy - (Core Hole x gaf1)");

		gaf1 = new ScaleBox(topCentre, SWT.NONE);
		gaf1.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
		gaf1.setNumericValue(30);
		gaf1.setDoNotUseExpressions(true);
		gaf1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		gaf1.addValueListener(new ValueAdapter("gaf1Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				Double newValue;
				if (e.getValue() != null) {
					if (e.getValue() instanceof Double) {
						newValue = (Double) e.getValue();
					} else {
						newValue = e.getDoubleValue();
					}
				} else {
					newValue = gaf1.getNumericValue();
				}
				double newA = calcAorB(newValue);
				final double minEnergy = getInitialEnergy().getNumericValue();
				if (newA > minEnergy) {
					getA().off();
					getA().setNumericValue(newA);
					getA().on();
				}
			}
		});

		final Label gaf2Label = new Label(topCentre, SWT.NONE);
		gaf2Label.setText("Gaf2");
		gaf2Label.setToolTipText("Gamma function 2: B = Edge Energy - (Core Hole x gaf2)");

		gaf2 = new ScaleBox(topCentre, SWT.NONE);
		gaf2.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
		gaf2.setNumericValue(10);
		gaf2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		gaf2.setDoNotUseExpressions(true);
		gaf2.addValueListener(new ValueAdapter("gaf2Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				Double newValue;
				if (e.getValue() != null) {
					if (e.getValue() instanceof Double) {
						newValue = (Double) e.getValue();
					} else {
						newValue = e.getDoubleValue();
					}
				} else {
					newValue = gaf2.getNumericValue();
				}

				double newB = calcAorB(newValue);
				getB().off();
				getB().setNumericValue(newB);
				getB().on();

				// change C as well if the preference has been set to do so
				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK)) {
					getC().off();
					getGaf3().off();
					getC().setNumericValue(calcC(newValue));
					getGaf3().setNumericValue(newValue);
					getC().on();
					getGaf3().on();
				}
			}
		});

		gaf3Label = new Label(topCentre, SWT.NONE);
		gaf3Label.setText("Gaf3");
		gaf3Label.setToolTipText("Gamma function 3: C = Edge Energy + (Core Hole x gaf3)");
		gaf3 = new ScaleBox(topCentre, SWT.NONE);
		gaf3.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
		gaf3.setNumericValue(10);
		gaf3.setDoNotUseExpressions(true);
		gaf3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		gaf3.addValueListener(new ValueAdapter("gaf3Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {

				Double newValue;
				if (e.getValue() != null) {
					if (e.getValue() instanceof Double) {
						newValue = (Double) e.getValue();
					} else {
						newValue = e.getDoubleValue();
					}
				} else {
					newValue = gaf3.getNumericValue();
				}
				getC().off();
				getC().setNumericValue(calcC(newValue));
				getC().on();
			}
		});

		aLabel = new Link(topCentre, SWT.NONE);
		aLabel.setText("<a>A</a>");
		aLabel.setToolTipText("Click to open preferences.");
		this.aListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openPreferences();
			}
		};
		aLabel.addSelectionListener(aListener);
		a = new ScaleBox(topCentre, SWT.NONE);
		a.setDoNotUseExpressions(true);
		a.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
		a.setUnit("eV");
		a.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		a.setMinimum(initialEnergy);

		bLabel = new Link(topCentre, SWT.NONE);
		bLabel.setText("<a>B</a>");
		bLabel.setToolTipText("Click to open preferences.");
		this.bListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openPreferences();
			}
		};
		bLabel.addSelectionListener(bListener);
		b = new ScaleBox(topCentre, SWT.NONE);
		b.setDoNotUseExpressions(true);
		b.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
		b.setUnit("eV");
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		b.setMinimum(initialEnergy);
		a.setMaximum(b);

		cLabel = new Link(topCentre, SWT.NONE);
		cLabel.setText("<a>C</a>");
		cLabel.setToolTipText("Click to open preferences.");
		this.cListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openPreferences();
			}
		};
		cLabel.addSelectionListener(cListener);
		if (energyInK) {
			c = new ScaleBoxAndFixedExpression(topCentre, SWT.NONE, getKInEv());
			c.setDoNotUseExpressions(true);
			c.setLabelUnit("eV");
			c.setLabelDecimalPlaces(1);
			c.setLabelWidth(1000000);
			c.setPrefix(" ");
			c.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
			c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			c.setUnit("Å\u207B\u00b9");
			c.setMinimum(0);
			c.setMaximum(100);
		} else {
			c = new ScaleBoxAndFixedExpression(topCentre, SWT.NONE, getKProvider());
			c.setDoNotUseExpressions(true);
			c.setLabelUnit("Å\u207B\u00b9");
			c.setLabelDecimalPlaces(3);
			c.setLabelWidth(100);
			c.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
			c.setUnit("eV");
			c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			c.setMinimum(b);
			c.setMaximum(finalEnergy);
			b.setMaximum(c);
		}

		this.kStartLabel = new Label(topCentre, SWT.NONE);
		kStartLabel.setText("K Start");
		kStartLabel.setVisible(false);
		this.kStart = new ScaleBoxAndFixedExpression(topCentre, SWT.NONE, getKProvider());
		kStart.setLabelUnit("Å\u207B\u00b9");
		kStart.setPrefix(" ");
		kStart.setLabelDecimalPlaces(3);
		kStart.setLabelWidth(100);
		kStart.setUnit("eV");
		kStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		kStart.setEditable(false);
		kStart.setVisible(false);
		kStart.setMaximum(finalEnergy);
		kStart.addValueListener(new ValueAdapter("kStartListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				final double ed = getEdgeValue();
				logger.info("the k start value is " + ((ed - getB().getNumericValue()) + ed));
				getKStart().off();
				getKStart().setNumericValue((ed - getB().getNumericValue()) + ed);
				getKStart().on();
			}
		});
	}

	private ExpressionProvider getKProvider() {
		return new ExpressionProvider() {

			@Override
			public double getValue(double e) {
				if (!Double.isNaN(e)) {
					Converter.setEdgeEnergy(getEdgeValue() / 1000.0);
					return Converter.convert(e, Converter.EV, Converter.PERANGSTROM);
				}
				return e;
			}

			@Override
			public IFieldWidget[] getPrecedents() {
				return null;
			}
		};
	}

	private ExpressionProvider getKInEv() {
		return new ExpressionProvider() {
			@Override
			public double getValue(double e) {
				if (!Double.isNaN(e)) {
					Converter.setEdgeEnergy(getEdgeValue() / 1000.0);
					return Converter.convert(e, Converter.PERANGSTROM, Converter.EV);
				}
				return e;
			}

			@Override
			public IFieldWidget[] getPrecedents() {
				return null;
			}
		};
	}

	@Override
	public void linkUI(final boolean isPageChange) {

		try {
			setPointsUpdate(false);

			final XasScanParameters scanParams = (XasScanParameters) editingBean;

			// Manually added parts for bounds and editor notifications.
			// Get choices for edge from schema
			// Set default values, bean may have changed
			try {
				final SchemaReader reader = setupElementAndEdge("XasScanParameters");

				List<String> choices = reader.getAllowedChoices("XasScanParameters", "exafsStepType");
				exafsStepType.setItems(choices.toArray(new String[choices.size()]));

				if (scanParams.getExafsStepType() == null) {
					exafsStepType.select(0);
				}

				if (scanParams.getExafsTime() == null) {
					exafsTimeType.select(1);
				}

				if (scanParams.getA() == null) {
					abGafChoice.select(1);
				}

			} catch (Exception e1) {
				logger.error("Cannot process ui.", e1);
			}

			exafsStepType.addValueListener(new ValueAdapter("exafsStepTypeListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateExafsStepType();
				}
			});

			exafsTimeType.addValueListener(new ValueAdapter("exafsTimeTypeListener") {
				
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateExafsTimeType();
				}
			});

			updateExafsTimeType();
			updateEdgeRegion();
			updateLayout();
			setPointsUpdate(false);
			updateElement(ELEMENT_EVENT_TYPE.INIT); // Must be before linkUI or switched on status fires events that
			// lose original value.

			// Sets value and switches on the listeners.
			super.linkUI(isPageChange); // Will also switch back on widgets.
			setPointsUpdate(false);
			suspendGraphUpdate = true;

			updateExafsStepType();
			updateKStartIfVisible();
			getA().checkBounds();
			getB().checkBounds();
			getC().checkBounds();
			getInitialEnergy().checkBounds();

			getFinalEnergy().checkBounds();
			// Final energy and C are saved in the xml file in eV if the user preference is for Angstroms, it may be
			// loaded in wrong units
			if (c.isOn()) {
				c.off();
			}
			if (finalEnergy.isOn()) {
				finalEnergy.off();
			}
			if (energyInK) {
				correctFinalEnergy();
				correctC();
			}

			rebuildGraph();
			setupEstimationWidgets();
		} catch (Exception e) {
			logger.error("Error trying to linkUI in the xas scan editor", e);
		} finally {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						setPointsUpdate(true);
						suspendGraphUpdate = false;
						dirtyContainer.setDirty(false);
					} catch (Exception e1) {
						logger.error("Cannot update XAS points", e1);
					}
				}
			});
		}
	}

	private void rebuildGraph() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					suspendGraphUpdate = true;
					drawLines();
					updatePlottedPoints();
					plottingsystem.repaint(true);
				} catch (Exception e1) {
					logger.error("Cannot update XAS points", e1);
				} finally {
					setPointsUpdate(true);
					suspendGraphUpdate = false;					
				}
			}
		});
	}
	
	
	@Override
	public void updatePlottedPoints() {
			super.updatePlottedPoints();

			ITrace trace = plottingsystem.getTrace("\u0394E (eV)");
			IDataset ds = trace.getData();
			Double max;
			if (ds instanceof DoubleDataset){
				max = (Double) ds.max();
			} else {
				max = Double.valueOf(ds.max().toString());
			}
//			Integer plotMax = (Integer) ds.max();
			Double stepHeight = (double) (max /6);

			try {
				double aEnergy = getA().getNumericValue();
				double bEnergy = getB().getNumericValue();
				double edgeEnergy = getEdgeEnergy().getNumericValue();
				double cEnergy = getC().getNumericValue();
				if (energyInK) {
					cEnergy = getKInEv().getValue(cEnergy);
				}
				plottingsystem.clearAnnotations();
				IAnnotation aAnnotation = AnnotationUtils.replaceCreateAnnotation(plottingsystem, "A energy");
				aAnnotation.setLocation(aEnergy, stepHeight * 2);
				aAnnotation.setVisible(showLineAnnotations);
				aAnnotation.setShowPosition(false);
				plottingsystem.addAnnotation(aAnnotation);
				IAnnotation bAnnotation = AnnotationUtils.replaceCreateAnnotation(plottingsystem, "B energy");
				bAnnotation.setLocation(bEnergy, stepHeight * 3);
				bAnnotation.setVisible(showLineAnnotations);
				bAnnotation.setShowPosition(false);
				plottingsystem.addAnnotation(bAnnotation);
				IAnnotation edgeAnnotation = AnnotationUtils.replaceCreateAnnotation(plottingsystem, "Edge energy");
				edgeAnnotation.setLocation(edgeEnergy, stepHeight * 4);
				edgeAnnotation.setVisible(showLineAnnotations);
				edgeAnnotation.setShowPosition(false);
				plottingsystem.addAnnotation(edgeAnnotation);
				IAnnotation cAnnotation = AnnotationUtils.replaceCreateAnnotation(plottingsystem, "C energy");
				cAnnotation.setLocation(cEnergy, stepHeight * 5);
				cAnnotation.setVisible(showLineAnnotations);
				cAnnotation.setShowPosition(false);
				plottingsystem.addAnnotation(cAnnotation);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}
	}

	@Override
	protected void setPointsUpdate(boolean isUpdate) {
		updateValueAllowed = isUpdate;
		if (isUpdate) {
			updatePointsLabels();
			a.on();
			b.on();
			c.on();
			gaf1.on();
			gaf2.on();
			gaf3.on();
			initialEnergy.on();
			finalEnergy.on();
			edge.on();
			element.on();
			exafsStep.on();
			edgeStep.on();
			edgeTime.on();
			preEdgeStep.on();
			preEdgeTime.on();
			exafsFromTime.on();
			exafsToTime.on();
			exafsTime.on();
			getKWeighting().on();
			exafsStepType.on();
			getCoreHole_unused().on();
			getEdgeEnergy().on();
		} else {
			a.off();
			b.off();
			c.off();
			gaf1.off();
			gaf2.off();
			gaf3.off();
			initialEnergy.off();
			finalEnergy.off();
			edge.off();
			element.off();
			exafsStep.off();
			edgeStep.off();
			edgeTime.off();
			preEdgeStep.off();
			preEdgeTime.off();
			exafsFromTime.off();
			exafsToTime.off();
			exafsTime.off();
			getKWeighting().off();
			exafsStepType.off();
			getCoreHole_unused().off();
			getEdgeEnergy().off();
		}
		// not sure if this works as it relies on calling every getter method in the class...
//		try {
//			BeanUI.switchState(this, isUpdate);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			logger.error("TODO put description of error here", e);
//		}
	}

	private void correctC() {
		double value = getC().getBoundValue();
		getC().setValue(getKProvider().getValue(value));
	}

	private void correctFinalEnergy() {
		double value = getFinalEnergy().getBoundValue();
		getFinalEnergy().setValue(getKProvider().getValue(value));
	}

	@Override
	protected void updateElement(ELEMENT_EVENT_TYPE type) {

		try {
			if (!updateValueAllowed) {
				setPointsUpdate(false);
			}

			super.updateElement(type);
			getSelectedElement(type);

			final double edgeValue = getEdgeValue();
			b.setMaximum(edgeValue);
			if (!energyInK)
				c.setMinimum(edgeValue);

			// Hack warning - this is required to deal with the fact that element can be updated
			// by a page change. *NOTE* The situation of needing ELEMENT_EVENT_TYPE at all would
			// not be around if the UI paradigm of having element sometimes changing initialEnergy
			// and sometimes not being required by the beam line scientists.
			if (type != ELEMENT_EVENT_TYPE.INIT && cachedElement != null) {
				if (cachedElement.equals(((XasScanParameters) editingBean).getElement())) {
					type = ELEMENT_EVENT_TYPE.INIT;
					cachedElement = null;
				}
			}

			// this part is ONLY for when the element has just been changed.
			if (type != ELEMENT_EVENT_TYPE.INIT) {
				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.A_ELEMENT_LINK)) {
					getA().setValue(getAfromElement());
				}
				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.B_ELEMENT_LINK)) {
					getB().setValue(getBfromElement());
				}
				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.C_ELEMENT_LINK)) {
					Double value = getCfromElement();
					getC().setValue(value);
				}

				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.INITIAL_ENERGY_ELEMENT_LINK)) {
					getInitialEnergy().setValue(getInitialEnergyFromElement());
				}
				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.FINAL_ENERGY_ELEMENT_LINK)) {
					getFinalEnergy().setValue(getFinalEnergyFromElement());
				}
				rebuildGraph();
			} else {
				final XasScanParameters scanParams = (XasScanParameters) editingBean;
				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.A_ELEMENT_LINK)) {
					if (scanParams.getA() == null) {
						if (scanParams.getGaf1() == null) {
							getA().setValue(getAfromElement());
						} else {
							getA().setValue(calcAorB(scanParams.getGaf1()));
						}
					}
				}
				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.B_ELEMENT_LINK)) {
					if (scanParams.getB() == null) {
						if (scanParams.getGaf1() == null) {
							getB().setValue(getBfromElement());
						} else {
							getB().setValue(calcAorB(scanParams.getGaf2()));
						}
					}
				}
				if (ExafsActivator.getDefault().getPreferenceStore()
						.getBoolean(ExafsPreferenceConstants.C_ELEMENT_LINK)) {
					if (scanParams.getC() == null) {
						if (scanParams.getGaf1() == null) {
							getC().setValue(getCfromElement());
						} else {
							getC().setValue(calcC(scanParams.getGaf3()));
						}
					}
				}
			}
			updateKStartIfVisible();
		} catch (Exception ne) {
			logger.error("Cannot set value", ne);
		} finally {
			setPointsUpdate(true);
		}
	}

	@Override
	protected String[] getLineLabels() {
		return new String[] { "Edge", "A", "B", "C" };
	}

	@Override
	protected double[] getLineValues() {
		return new double[] { getEdgeValue(), (Double) getA().getValue(), (Double) getB().getValue(),
				(Double) getC().getValue() };
	}

	@Override
	protected java.awt.Color[] getLineColours() {
		return new java.awt.Color[] { java.awt.Color.GREEN.brighter().brighter().brighter(), java.awt.Color.RED,
				java.awt.Color.ORANGE, java.awt.Color.BLUE.brighter().brighter().brighter() };
	}

	private double getGaf1Value() {
		final double g = gaf1.getNumericValue();
		return Double.isNaN(g) ? 30 : g;
	}

	private double getGaf2Value() {
		final double g = gaf2.getNumericValue();
		return Double.isNaN(g) ? 10 : g;
	}

	private double getGaf3Value() {
		final double g = gaf3.getNumericValue();
		return Double.isNaN(g) ? 10 : g;
	}

	protected double getInitialEnergyFromElement() throws Exception {
		final Element ele = getElementUseBean();
		final String edge = getEdgeUseBean();
		return ele.getInitialEnergy(edge);
	}

	protected double getFinalEnergyFromElement() throws Exception {
		final Element ele = getElementUseBean();
		final String edge = getEdgeUseBean();
		double fEnergy = ele.getFinalEnergy(edge);
		if (energyInK)
			return getFinalEnergyFromElementInCorrectUnits(fEnergy);
		return fEnergy;
	}

	private double getFinalEnergyFromElementInCorrectUnits(double energy) {
		return getKProvider().getValue(energy);
	}

	protected double getAfromElement() throws Exception {
		return getABfromElement(getGaf1Value());
	}

	protected double getBfromElement() throws Exception {
		return getABfromElement(getGaf2Value());
	}

	private double getABfromElement(double abValue) throws Exception {
		final Element ele = getElementUseBean();
		final String edge = getEdgeUseBean();
		final double ed = getEdgeValue();
		if (ele == null || edge == null)
			return 0d;
		return ed - (abValue * ele.getCoreHole(edge));
	}

	protected double getCfromElement() throws Exception {
		final Element ele = getElementUseBean();
		final String edge = getEdgeUseBean();
		final double ed = getEdgeValue();
		double en = 0.0;
		if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK)) {
			en = ed + (getGaf2Value() * ele.getCoreHole(edge));
		}
		en = ed + (getGaf3Value() * ele.getCoreHole(edge));
		if (energyInK)
			return getKProvider().getValue(en);
		return en;

	}

	protected void updateEdgeRegion() {
		final boolean isAB = abGafChoice.getSelectionIndex() == 0;
		getA().setActive(isAB);
		getB().setActive(isAB);
		if (isAB) {
			boolean cMirrorsB = ExafsActivator.getDefault().getPreferenceStore()
					.getBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK);
			getC().setActive(!cMirrorsB);
			cLine.setMobile(!cMirrorsB);

		} else {
			getC().setActive(false);
		}
		getCoreHole_unused().setActive(!isAB);
		getGaf1().setActive(!isAB);
		getGaf2().setActive(!isAB);
		if (!isAB) {
			boolean cMirrorsB = ExafsActivator.getDefault().getPreferenceStore()
					.getBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK);
			getGaf3().setActive(!cMirrorsB);
			cLine.setMobile(!cMirrorsB);

		} else {
			getGaf3().setActive(false);
		}
	}

	protected void updateExafsStepType() {

		final int index = exafsStepType.getSelectionIndex();

		if (index == 0) { // k
			getExafsStep().setUnit("Å\u207B\u00b9"); // Å^-1
			exafsStepEnergyLabel.setText("Exafs Step");
		} else {
			getExafsStep().setUnit("eV");
			exafsStepEnergyLabel.setText("Exafs Step Energy");
		}
		exafsStepEnergyLabel.redraw();
		exafsStepEnergyLabel.getParent().layout();
	}

	private double calculateKStart() {
		final double ed = getEdgeValue();
		return ed - getB().getNumericValue() + ed;

	}

	protected void updateKStart(boolean show) {
		getKStart().setNumericValue(calculateKStart());
		getKStart().setActive(show);
		this.kStartLabel.setVisible(show);
	}

	protected void updateKStartIfVisible() {
		if (getKStart().isVisible()) {
			getKStart().setNumericValue(calculateKStart());
		}
	}

	protected void updateExafsTimeType() {
		String exafsTimeTypeVal = bean.getExafsTimeType();
		
		boolean isVariableTime=false;
		if(exafsTimeTypeVal.equals("Variable Time"))
			isVariableTime=true;
			
		
		// Bean fields use active
		getExafsTime().setActive(!isVariableTime);
		getExafsFromTime().setActive(isVariableTime);
		getExafsToTime().setActive(isVariableTime);
		getKWeighting().setActive(isVariableTime);
		// Labels visible
		this.exafsStepLabel.setVisible(!isVariableTime);
		exafsToLabel.setVisible(isVariableTime);
		exafsFromLabel.setVisible(isVariableTime);
		kWeightingLabel.setVisible(isVariableTime);
	}

	@Override
	public void setFocus() {
		// Nothing
	}

	public ScaleBox getFinalEnergy() {
		return finalEnergy;
	}

	public ScaleBox getInitialEnergy() {
		return initialEnergy;
	}

	public ScaleBox getPreEdgeStep() {
		return preEdgeStep;
	}

	public ComboWrapper getEdge() {
		return edge;
	}

	public ComboWrapper getElement() {
		return element;
	}

	public ScaleBox getEdgeStep() {
		return edgeStep;
	}

	public ScaleBox getEdgeTime() {
		return edgeTime;
	}

	public ScaleBox getExafsStep() {
		return exafsStep;
	}

	public ScaleBox getExafsTime() {
		return exafsTime;
	}

	public ScaleBox getPreEdgeTime() {
		return preEdgeTime;
	}

	public ScaleBox getA() {
		return a;
	}

	public ScaleBox getB() {
		return b;
	}

	public ScaleBox getC() {
		return c;
	}

	@Override
	protected String getRichEditorTabText() {
		return "XAS Scan";
	}

	public ComboWrapper getExafsStepType() {
		return exafsStepType;
	}

	public ScaleBox getGaf1() {
		return gaf1;
	}

	public ScaleBox getGaf2() {
		return gaf2;
	}

	public ScaleBox getGaf3() {
		return gaf3;
	}

	public ScaleBox getExafsFromTime() {
		return exafsFromTime;
	}

	public ScaleBox getExafsToTime() {
		return exafsToTime;
	}

	public ScaleBox getKWeighting() {
		return kWeighting;
	}

	public ScaleBox getKStart() {
		return kStart;
	}

	/**
	 * Used during testing.
	 * 
	 * @param ichoice
	 *            0 for AB for Gaf
	 */
	public void _testSetGapChoice(final int ichoice) {
		this.abGafChoice.select(ichoice);
	}

	public void _testSetTimeType(final int itype) {
		this.exafsTimeType.select(itype);
		updateExafsTimeType();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		try {

			if (event.getProperty().equals(ExafsPreferenceConstants.INITIAL_ENERGY_ELEMENT_LINK)) {
				final Boolean isLink = (Boolean) event.getNewValue();
				getInitialEnergy().setEnabled(!isLink);
				if (isLink) {
					getInitialEnergy().setValue(getInitialEnergyFromElement());
				}

			} else if (event.getProperty().equals(ExafsPreferenceConstants.FINAL_ENERGY_ELEMENT_LINK)) {
				final Boolean isLink = (Boolean) event.getNewValue();
				getFinalEnergy().setEnabled(!isLink);
				if (isLink) {
					getFinalEnergy().setValue(getFinalEnergyFromElement());
				}

			} else if (event.getProperty().equals(ExafsPreferenceConstants.A_ELEMENT_LINK)) {
				final Boolean isLink = (Boolean) event.getNewValue();
				if (isLink) {
					getA().setValue(getAfromElement());
				}

			} else if (event.getProperty().equals(ExafsPreferenceConstants.B_ELEMENT_LINK)) {
				final Boolean isLink = (Boolean) event.getNewValue();
				if (isLink) {
					getB().setValue(getBfromElement());
				}

			} else if (event.getProperty().equals(ExafsPreferenceConstants.C_ELEMENT_LINK)) {
				final Boolean isLink = (Boolean) event.getNewValue();
				if (isLink) {
					getC().setValue(getCfromElement());
				}
			} else if (event.getProperty().equals(ExafsPreferenceConstants.C_MIRRORS_B_LINK)) {
				final Boolean isLink = (Boolean) event.getNewValue();
				if (isLink) {
					getC().setValue(getCfromElement());
				}
				updateEdgeRegion();
			} else if (event.getProperty().equals(ExafsPreferenceConstants.EXAFS_GRAPH_EDITABLE)) {
				if (aLine != null)
					aLine.setMobile((Boolean) event.getNewValue());
				if (bLine != null)
					bLine.setMobile((Boolean) event.getNewValue());
				if (cLine != null)
					cLine.setMobile((Boolean) event.getNewValue());
			}
			updateKStartIfVisible();
		} catch (Exception ne) {
			logger.error("Cannot update values", ne);
		}
	}

	@Override
	public void dispose() {
		if (e0Label != null && !e0Label.isDisposed())
			this.e0Label.removeSelectionListener(e0Listener);
		if (e1Label != null && !e1Label.isDisposed())
			this.e1Label.removeSelectionListener(e1Listener);
		if (aLabel != null && !aLabel.isDisposed())
			this.aLabel.removeSelectionListener(aListener);
		if (bLabel != null && !bLabel.isDisposed())
			this.bLabel.removeSelectionListener(bListener);
		if (cLabel != null && !cLabel.isDisposed())
			this.cLabel.removeSelectionListener(cListener);
		ExafsActivator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
	}

	@Override
	public Object updateFromUIAndReturnEditingBean() throws Exception {
		BeanUI.uiToBean(this, editingBean);
		return fetchEditingBean();
	}

	@Override
	protected Object fetchEditingBean() {
		if (energyInK) {
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			((XasScanParameters) this.editingBean).setFinalEnergy(Double.valueOf(twoDForm.format(getKInEv().getValue(
					getFinalEnergy().getBoundValue()))));
			((XasScanParameters) this.editingBean).setC(Double.valueOf(twoDForm.format(getKInEv().getValue(
					getC().getBoundValue()))));
		}
		return editingBean;
	}

	protected Double calcAorB(double gaf) {
		final String value = getCoreHole_unused().getValue();
		if (value == null || "".equals(value))
			return null;
		final double ed = getEdgeValue();
		final double core = Double.parseDouble(value);
		return ed - (gaf * core);
	}

	protected Double calcC(double gaf) {
		final String value = getCoreHole_unused().getValue();
		if (value == null || "".equals(value))
			return null;
		final double ed = getEdgeValue();
		final double core = Double.parseDouble(value);
		final double energyInEv = ed + (gaf * core);
		if (energyInK) {
			return getKProvider().getValue(energyInEv);
		}
		return energyInEv;
	}

	protected double calcGaf1or2(double latestTargetValue) {
		final double ed = getEdgeValue();
		final double coreHole = Double.parseDouble(getCoreHole_unused().getValue());
		double gaf1 = Math.round((ed - latestTargetValue) / coreHole);
		return gaf1;
	}

	protected double calcGaf3(double latestTargetValue) {
		final double ed = getEdgeValue();
		final double coreHole = Double.parseDouble(getCoreHole_unused().getValue());
		double gaf3 = Math.abs(Math.round((latestTargetValue - ed) / coreHole));
		return gaf3;
	}

	public ComboWrapper getExafsTimeType() {
		return exafsTimeType;
	}

	public ComboWrapper getAbGafChoice() {
		return abGafChoice;
	}
}
