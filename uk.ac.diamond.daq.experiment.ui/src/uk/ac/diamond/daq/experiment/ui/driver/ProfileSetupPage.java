package uk.ac.diamond.daq.experiment.ui.driver;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class ProfileSetupPage extends WizardPage {
	
	ProfileSetupPage() {
		super("Profile");
	}
	
	@Inject
	private IEclipseContext injectionContext;
	
	private ProfileMode profileMode = ProfileMode.DISPLACEMENT;
	private ProfilePath profilePath = ProfilePath.FIXED_RATE;
	private Composite composite;
	private Composite editorComposite;
	
	private GridDataFactory stretch = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

	@Override
	public void createControl(Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		
		Label title = new Label(composite, SWT.NONE);
		title.setText("Profile setup");
		
		FontData[] fontData = title.getFont().getFontData();
		fontData[0].setHeight(14);
		title.setFont(new Font(Display.getDefault(), fontData[0]));
		
		Composite modeAndPath = new Composite(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(modeAndPath);
		stretch.applyTo(modeAndPath);
		
		Group modeGroup = new Group(modeAndPath, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(modeGroup);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(modeGroup);
		
		Label modeLabel = new Label(modeGroup, SWT.NONE);
		modeLabel.setText("Mode");
		GridDataFactory.fillDefaults().span(1, 2).applyTo(modeLabel);
		
		Button modeDisplacement = new Button(modeGroup, SWT.RADIO);
		modeDisplacement.setText("Displacement");
		modeDisplacement.addListener(SWT.Selection, e -> {
			profileMode = ProfileMode.DISPLACEMENT;
			updateEditor();
		});
		modeDisplacement.setSelection(true);
		
		Button modeLoad = new Button(modeGroup, SWT.RADIO);
		modeLoad.setText("Load");
		modeLoad.addListener(SWT.Selection, e -> {
			profileMode = ProfileMode.LOAD_DEPENDENT;
			updateEditor();
		});
		
		Group pathGroup = new Group(modeAndPath, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(pathGroup);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(pathGroup);
		
		Label pathLabel = new Label(pathGroup, SWT.NONE);
		pathLabel.setText("Path");
		GridDataFactory.fillDefaults().span(1, 2).applyTo(pathLabel);
		
		Button pathFixed = new Button(pathGroup, SWT.RADIO);
		pathFixed.setText("Fixed rate");
		pathFixed.addListener(SWT.Selection, e -> {
			profilePath = ProfilePath.FIXED_RATE;
			updateEditor();
		});
		pathFixed.setSelection(true);
		
		Button pathCustom = new Button(pathGroup, SWT.RADIO);
		pathCustom.setText("Custom profile");	
		pathCustom.addListener(SWT.Selection, e -> {
			profilePath = ProfilePath.CUSTOM_PROFILE;
			updateEditor();			
		});
		
		updateEditor();
		
		setControl(composite);
	}

	private ProfileEditor getProfileEditor() {
		if (profileMode == ProfileMode.DISPLACEMENT) {
			if (profilePath == ProfilePath.FIXED_RATE) {
				return ContextInjectionFactory.make(FixedRateDisplacementEditor.class, injectionContext);
			} else {
				return ContextInjectionFactory.make(CustomDisplacementEditor.class, injectionContext);
			}
		} else {
			if (profilePath == ProfilePath.FIXED_RATE) {
				return ContextInjectionFactory.make(FixedRateLoadEditor.class, injectionContext);
			} else {
				return ContextInjectionFactory.make(CustomLoadEditor.class, injectionContext);
			}
		}
	}
	
	private void updateEditor() {
		if (editorComposite!=null) {
			editorComposite.dispose();
			editorComposite = null;
		}
		
		editorComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(editorComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(editorComposite);

		ProfileEditor editor = getProfileEditor();
		editor.createControl(editorComposite);
		
		composite.layout();
	}

}
