package uk.ac.diamond.daq.client.gui.camera.energy;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.daq.client.gui.camera.energy.EnergyController.EnergySelectionType;

public class BeamEnergyDialog {
	private BeamEnergyComposite imagingBeamEnergyComposite;
	private BeamEnergyComposite diffractionBeamEnergyComposite;
	
	private Shell shell;
	
	public BeamEnergyDialog(Display display) {
		shell = new Shell(SWT.CLOSE | SWT.MIN | SWT.MAX);
		shell.setText("Energy");
		
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(shell);
		
		Label label;
		
		label = new Label(shell, SWT.NONE);
		label.setText("Diffracation");
		GridDataFactory.swtDefaults().applyTo(label);
		
		label = new Label(shell, SWT.SEPARATOR | SWT.VERTICAL);
		GridDataFactory.swtDefaults().span(1, 2).applyTo(label);
		
		label = new Label(shell, SWT.NONE);
		label.setText("Imaging");
		GridDataFactory.swtDefaults().applyTo(label);
		
		EnergyController diffractionEnergyController = new EnergyController(EnergySelectionType.monoBeamOnly);
		diffractionBeamEnergyComposite = new BeamEnergyComposite(shell, diffractionEnergyController);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(diffractionBeamEnergyComposite);
		
		EnergyController imagingEnergyController = new EnergyController(EnergySelectionType.pinkAndMonoBeams);
		imagingBeamEnergyComposite = new BeamEnergyComposite(shell, imagingEnergyController);
		GridDataFactory.swtDefaults().applyTo(imagingBeamEnergyComposite);

		shell.pack();
	}

	public static void main(String[] args) {
		Display display = new Display ();
		BeamEnergyDialog beamEnergyDialog = new BeamEnergyDialog(display);
		beamEnergyDialog.shell.open();
		while (!beamEnergyDialog.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
