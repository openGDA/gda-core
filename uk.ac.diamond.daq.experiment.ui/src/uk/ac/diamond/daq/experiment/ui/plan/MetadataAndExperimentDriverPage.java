package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;


public class MetadataAndExperimentDriverPage extends WizardPage implements ValidationListener {
	
	private String experimentId;
	private ExperimentPlanBean planBean;
	
	private List<ValidatablePart> sections;
	
	MetadataAndExperimentDriverPage(String experimentId, ExperimentPlanBean planBean) {
		super(MetadataAndExperimentDriverPage.class.getSimpleName());
		setTitle("Metadata and experiment driver");
		setDescription("Add a title and description to your plan, and select experiment driver configuration if required");
		
		this.experimentId = experimentId;
		this.planBean = planBean;
		
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		sections = new ArrayList<>();
		sections.add(new NameAndDescriptionSection(planBean));
		sections.add(new DriverAndProfileSelectionSection(planBean, experimentId));
		
		sections.forEach(section -> {
			section.createPart(composite);
			section.addValidationListener(this);
		});
		
		setPageComplete(isPageComplete());
		
		setControl(composite);
	}
	
	@Override
	public boolean isPageComplete() {
		return sections.stream().allMatch(ValidatablePart::isValidSelection);
	}

	@Override
	public void handle(boolean valid) {
		setPageComplete(valid ? isPageComplete() : valid);
	}

}
