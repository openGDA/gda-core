package uk.ac.diamond.daq.mapping.ui.experiment;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;

/**
 * A POJO class for the stage manager view.
 */
public class StageManagerView {

	private static final Logger logger = LoggerFactory.getLogger(StageManagerView.class);

	private static final String STATE_KEY_MAPPING_STAGE_INFO = "mappingStageInfo.json";

	@Inject
	private MappingStageInfo mappingStageInfo;
	@Inject
	private IGuiGeneratorService guiGenerator;
	@Inject
	private BeamPositionPlotter beamPositionPlotter;
	@Inject
	private IMarshallerService marshallerService;

	public StageManagerView() {
	}

	@PostConstruct
	public void createView(Composite parent, MPart part) {
		loadPreviousState(part); // Restore the MappingStageInfo to state when the client shut down

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

	@PersistState
	public void saveState(MPart part) {
		try {
			logger.trace("Saving the current state of the mapping stage information");
			String json = marshallerService.marshal(mappingStageInfo);
			part.getPersistedState().put(STATE_KEY_MAPPING_STAGE_INFO, json);
		} catch (Exception e) {
			logger.error("Could not save the current mapping stage information", e);
		}
	}

	private void loadPreviousState(MPart part) {
		String json = part.getPersistedState().get(STATE_KEY_MAPPING_STAGE_INFO);
		if (json != null) {
			logger.trace("Restoring the previous state of the mapping stage information");
			try {
				final MappingStageInfo previous = marshallerService.unmarshal(json, MappingStageInfo.class);
				mappingStageInfo.merge(previous); // We need to update the existing bean as it is injected into other objects
			} catch (Exception e) {
				logger.error("Failed to restore the previous state of the mapping stage information");
			}
		}

	}

}
