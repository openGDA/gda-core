package uk.ac.diamond.daq.experiment.ui.plan;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

public class NameAndDescriptionSection extends ValidatablePart {
	
	private final ExperimentPlanBean planBean;
	
	private Text planNameText;
	private Text planDescriptionText;

	public NameAndDescriptionSection(ExperimentPlanBean planBean) {
		this.planBean = planBean;
	}
	
	@Override
	public void createPart(Composite parent) {
		Group composite = new Group(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		GridLayoutFactory.swtDefaults().margins(20, 20).applyTo(composite);
		
		composite.setText("Metadata");
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText("Plan name");
		
		planNameText = new Text(composite, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(planNameText);
		
		new Label(composite, SWT.NONE); // space
		
		new Label(composite, SWT.NONE).setText("Description");
		
		planDescriptionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(SWT.DEFAULT, 40).applyTo(planDescriptionText);
		
		bind();
	}

	@SuppressWarnings("unchecked")
	private void bind() {
		DataBindingContext dbc = new DataBindingContext();
		
		IObservableValue<String> nameTextObservable = WidgetProperties.text(SWT.Modify).observe(planNameText);
		IObservableValue<String> nameModelObservable = BeanProperties.value("planName").observe(planBean);
		
		dbc.bindValue(nameTextObservable, nameModelObservable);
		
		ISideEffect.create(nameTextObservable::getValue, name -> notifyValidationListener());
		
		IObservableValue<String> descriptionTextObservable = WidgetProperties.text(SWT.Modify).observe(planDescriptionText);
		IObservableValue<String> descriptionModelObservable = BeanProperties.value("planDescription").observe(planBean);
		
		dbc.bindValue(descriptionTextObservable, descriptionModelObservable);
	}
	
	@Override
	public boolean isValidSelection() {
		return planBean.getPlanName() != null && !planBean.getPlanName().isEmpty();
	}
}
