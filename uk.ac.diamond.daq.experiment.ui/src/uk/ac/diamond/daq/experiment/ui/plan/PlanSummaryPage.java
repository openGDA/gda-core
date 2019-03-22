package uk.ac.diamond.daq.experiment.ui.plan;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

public class PlanSummaryPage  extends WizardPage {
	private ScrolledComposite scrolledComposite;
	private Label summaryLabel;
	
	protected PlanSummaryPage() {
		super(PlanSummaryPage.class.getSimpleName());
		
		setTitle("Plan Summary");
		setDescription("Defined Segments and Triggers");
	}
	

	@Override
	public void createControl(Composite parent) {
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);
		
		GridLayoutFactory.swtDefaults().applyTo(scrolledComposite);
		
		summaryLabel = new Label (scrolledComposite, SWT.NONE);
		
		scrolledComposite.setContent(summaryLabel);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		setControl(scrolledComposite);
	}
	
	private String decodeSignalSource (SignalSource signalSource, String sevName) {
		if (signalSource == SignalSource.POSITION) {
			return sevName;
		}
		return "Time";
	}
	
	public void refresh (ExperimentPlanBean plan) {
		StringBuilder summaryText = new StringBuilder();
		
		summaryText.append("Plan: ").append(plan.getPlanName());
		summaryText.append("\n\nDriver Name: ").append(plan.getExperimentDriverName());
		summaryText.append("\nProfile Name: ").append(plan.getExperimentDriverProfile());
		summaryText.append("\nDescription: ").append(plan.getPlanDescription());
		
		for (SegmentDescriptor segment : plan.getSegments()) {
			summaryText.append("\n\nSegment: ").append(segment.getLabel());
			summaryText.append("\n\tExit Criteria: ")
				.append(decodeSignalSource(segment.getSignalSource(), segment.getSampleEnvironmentVariableName()))
				.append(" ").append(segment.getInequality().name()).append(" ")
				.append(segment.getSignalSource() == SignalSource.POSITION 
					? segment.getInequalityArgument() : segment.getDuration());
			
			for (TriggerDescriptor trigger : segment.getTriggers()) {
				summaryText.append("\n\tTrigger: ").append(trigger.getLabel());
				summaryText.append("\n\t\tMesurement Type: ").append(trigger.getScanName());
				summaryText.append("\n\t\tSEV: ").append(decodeSignalSource(trigger.getSignalSource(), 
						trigger.getSampleEnvironmentVariableName()));
				summaryText.append("\n\t\tExecution Policy: ").append(trigger.getExecutionPolicy());
				if (trigger.getExecutionPolicy() == ExecutionPolicy.SINGLE) {
					summaryText.append("\n\t\t\tValue: ").append(trigger.getTarget());
					if(trigger.getSignalSource() == SignalSource.POSITION) {
						summaryText.append("\n\t\t\tTolerance: ").append(trigger.getTolerance());
					}
				}
				if (trigger.getExecutionPolicy() == ExecutionPolicy.REPEATING) {
					summaryText.append("\n\t\t\tInterval: ").append(trigger.getInterval());
				}
			}
		}

		summaryLabel.setText(summaryText.toString());
		summaryLabel.pack();
		scrolledComposite.setMinSize(summaryLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	@Override
	public boolean isPageComplete() {
		return true;
	}
}
