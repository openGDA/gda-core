package uk.ac.diamond.daq.mapping.ui.experiment;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;

/**
 * A POJO class for the stage manager view.
 */
public class StageManagerView {

	@Inject
	private MappingStageInfo mappingStageInfo;
	@Inject
	private IGuiGeneratorService guiGenerator;
	@Inject
	private BeamPositionPlotter beamPositionPlotter;

	public StageManagerView() {
	}

	@PostConstruct
	public void createView(Composite parent) {

		// It'd really be better if the beam position plotter could initialise itself when the map plot view was
		// created, but there doesn't seem to be a good way to hook into that, so we use the creation of the GUI
		// elements for this view as a proxy since it happens at around the same time.
		beamPositionPlotter.init();

		parent.setLayout(new FillLayout());
		parent.setBackgroundMode(SWT.INHERIT_FORCE); // stop the ScrolledComposite being grey regardless of theme colour
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite mainComposite = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(mainComposite);
		scrolledComposite.setContent(mainComposite);

		Control stageInfoGui = guiGenerator.generateGui(mappingStageInfo, mainComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(stageInfoGui);

		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
}
