package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import uk.ac.gda.client.live.stream.view.CameraConfiguration;

public class ImagingCameraConfigurationComposite extends Composite {

	public ImagingCameraConfigurationComposite(Composite parent, CameraConfiguration cameraConfiguration, int style) {
		super(parent, style);

		GridLayoutFactory.swtDefaults().applyTo(this);

		TabFolder tabFolder = new TabFolder(this, SWT.TOP);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolder);

		TabItem exposureTab = new TabItem(tabFolder, SWT.NONE);
		exposureTab.setText("Exposure");

		ExposureConfigurationComposite exposureConfigurationComposite = new ExposureConfigurationComposite(tabFolder,
				cameraConfiguration, style);
		exposureTab.setControl(exposureConfigurationComposite);

		TabItem absorptionTab = new TabItem(tabFolder, SWT.NONE);
		absorptionTab.setText("Absorption");

		AbsorptionConfigurationComposite absorptionConfigurationComposite = new AbsorptionConfigurationComposite(
				tabFolder, SWT.NONE);
		absorptionTab.setControl(absorptionConfigurationComposite);

		DiadConfigurationComposite<DiadConfigurationModel> diadConfigurationComposite = new DiadConfigurationComposite<>(
				this, null, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(diadConfigurationComposite);
	}

}
