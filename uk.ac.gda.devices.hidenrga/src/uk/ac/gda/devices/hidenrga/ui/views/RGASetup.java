package uk.ac.gda.devices.hidenrga.ui.views;

import gda.device.HidenRGA;
import gda.factory.Finder;

import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGASetup extends ViewPart {

	public static String ID = "uk.ac.gda.devices.hidenrga.rgasetup";

	private static final Logger logger = LoggerFactory
			.getLogger(RGASetup.class);

	private Composite massesComposite;
	private Composite[] massesComposites;
	private HidenRGA rga;
	private Spinner[] massChoices;
	private Spinner sprNumberMasses;

	private ScrolledComposite mainScrolledComposite;

	public RGASetup() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new FillLayout());
		mainScrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.BORDER);
		mainScrolledComposite.setExpandHorizontal(true);
		mainScrolledComposite.setExpandVertical(true);

		Composite topComposite = new Composite(mainScrolledComposite, SWT.NONE);
		topComposite.setLayout(GridLayoutFactory.swtDefaults().numColumns(2)
				.create());

		boolean found = findRGA();
		if (!found) {
			Label lblError = new Label(topComposite, SWT.NONE);
			lblError.setLayoutData(GridDataFactory.swtDefaults().create());
			lblError.setText("Hiden RGA could not be found!");
			lblError.setForeground(PlatformUI.getWorkbench().getDisplay()
					.getSystemColor(SWT.COLOR_RED));
			return;
		}

		Label lblNumberMasses = new Label(topComposite, SWT.NONE);
		lblNumberMasses.setLayoutData(GridDataFactory.swtDefaults().create());
		lblNumberMasses.setText("Number masses:");

		sprNumberMasses = new Spinner(topComposite, SWT.READ_ONLY);
		sprNumberMasses.setLayoutData(GridDataFactory.swtDefaults().create());
		sprNumberMasses.setMinimum(1);
		sprNumberMasses.setMaximum(21);
		sprNumberMasses.setIncrement(1);
		sprNumberMasses.setSelection(1);
		sprNumberMasses.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				setVisibleItems(sprNumberMasses.getSelection());
			}
		});

		createMassesComposite(topComposite);

		updateMasses();

		mainScrolledComposite.setContent(topComposite);
		mainScrolledComposite.setMinSize(topComposite.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
	}

	private void updateMasses() {
		Set<Integer> masses = rga.getMasses();
		setVisibleItems(masses.size());

		int index = 0;
		for (Integer mass : masses) {
			massChoices[index].setSelection(mass);
			index++;
		}
	}

	private boolean findRGA() {
		rga = (HidenRGA) Finder.getInstance().find("rga");
		return rga != null;
	}

	private void createMassesComposite(Composite mainComposite) {
		massesComposite = new Composite(mainComposite, SWT.NONE);
		massesComposite.setLayout(GridLayoutFactory.swtDefaults().create());

		massesComposites = new Composite[21];
		massChoices = new Spinner[21];

		for (int i = 0; i < 21; i++) {
			massesComposites[i] = new Composite(massesComposite, SWT.NONE);
			massesComposites[i].setLayoutData(GridDataFactory.fillDefaults()
					.create());
			massesComposites[i].setLayout(GridLayoutFactory.fillDefaults()
					.numColumns(3).create());

			Label massLabel = new Label(massesComposites[i], SWT.NONE);
			massLabel.setLayoutData(GridDataFactory.fillDefaults().create());
			massLabel.setText((i + 1) + ":");

			massChoices[i] = new Spinner(massesComposites[i], SWT.NONE);
			massChoices[i].setLayoutData(GridDataFactory.fillDefaults()
					.create());
			massChoices[i].setMinimum(0);
			massChoices[i].setMaximum(128);

			Label amuLabel = new Label(massesComposites[i], SWT.NONE);
			amuLabel.setLayoutData(GridDataFactory.fillDefaults().create());
			amuLabel.setText("amu");
		}
	}

	protected void setVisibleItems(int selection) {
		for (int i = 1; i <= 21; i++) {
			if (i <= selection) {
				massesComposites[i - 1].setVisible(true);
			} else {
				massesComposites[i - 1].setVisible(false);
			}
		}
		massesComposite.layout();
		massesComposite.pack();
	}

	@Override
	public void setFocus() {
		massesComposites[0].setFocus();
	}

	/**
	 * Refresh the UI from the server-side object
	 */
	public void refresh() {
		if (rga != null) {
			updateMasses();
		}
	}

	/**
	 * Apply the masses shown in th UI to the server-side object
	 */
	public void apply() {
		if (rga != null) {
			int[] masses = new int[sprNumberMasses.getSelection()];
			for (int i = 0; i < sprNumberMasses.getSelection(); i++) {
				masses[i] = massChoices[i].getSelection();
			}
			rga.setMasses(masses);
		}
	}

	/**
	 * Start/stop recording masses to a file. This button could become out of
	 * sync if the recording is stop/started from the Jython commandline.
	 */
	public void toggleRecording() {
		try {
			if (!rga.isBusy()) {
				rga.startRecording();
			} else {
				rga.stopRecording();
			}
		} catch (Exception e) {
			logger.error("Exception when tryign to toggle RGA recording", e);
		}
	}

}
