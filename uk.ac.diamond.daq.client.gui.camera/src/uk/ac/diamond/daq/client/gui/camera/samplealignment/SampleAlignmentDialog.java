package uk.ac.diamond.daq.client.gui.camera.samplealignment;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gda.factory.Finder;
import uk.ac.diamond.daq.client.gui.camera.PositionValueControlComposite;
import uk.ac.diamond.daq.stage.MultipleStagePositioningService;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;

public class SampleAlignmentDialog {
	private static Shell instance;
	
	private SampleAlignmentDialog () {
		
	}

	public static void show(Display display, LiveStreamConnection liveStreamConnection) throws Exception {
		if (instance == null) {
			instance = new Shell(display, SWT.TITLE | SWT.RESIZE);
			instance.setSize(400, 650);
			instance.setText("Sample Alignment");
			
			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(instance);
			LivePlottingComposite plottingComposite = new LivePlottingComposite(instance, SWT.NONE, "");
			GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(plottingComposite);
			
			MultipleStagePositioningService multipleStagePositioningService = 
					Finder.getInstance().find("diadMultipleStagePositioningService");
			
			PositionValueControlComposite sampleAlignmentComposite = new PositionValueControlComposite(instance, 
					multipleStagePositioningService, SWT.NONE);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.END).hint(300, SWT.DEFAULT).applyTo(sampleAlignmentComposite);

			Composite buttonComposite = new Composite(instance, SWT.PUSH);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(buttonComposite);
			Label fillerLabel = new Label(buttonComposite, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fillerLabel);
			Button closeButton = new Button(buttonComposite, 0);
			closeButton.setText("Close");
			closeButton.addListener(SWT.Selection, e -> {
				plottingComposite.disconnect();
				instance.close();
				instance = null;
			});
			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(closeButton);

			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(buttonComposite);
		}
		instance.open();
	}
}
