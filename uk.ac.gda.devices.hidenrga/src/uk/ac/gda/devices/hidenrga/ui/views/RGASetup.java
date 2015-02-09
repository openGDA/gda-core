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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGASetup extends ViewPart {

	public static String ID = "uk.ac.gda.devices.hidenrga.rgasetup";

	private static final Logger logger = LoggerFactory.getLogger(RGASetup.class);

	private Composite massesComposite;
	private Composite[] massesComposites;
	private ScrolledComposite massesScrolledComposite;
	private HidenRGA rga;
	private Spinner[] massChoices;
	private Spinner sprNumberMasses;

	public RGASetup() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

		boolean found = findRGA();
		if (!found) {
			Label lblError = new Label(mainComposite, SWT.NONE);
			lblError.setLayoutData(GridDataFactory.swtDefaults().create());
			lblError.setText("Hiden RGA could not be found!");
			lblError.setForeground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}

		Label lblNumberMasses = new Label(mainComposite, SWT.NONE);
		lblNumberMasses.setLayoutData(GridDataFactory.swtDefaults().create());
		lblNumberMasses.setText("Number masses:");

		sprNumberMasses = new Spinner(mainComposite, SWT.READ_ONLY);
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

		createMassesComposite(mainComposite);

		updateMasses();
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
		massesScrolledComposite = new ScrolledComposite(mainComposite, SWT.V_SCROLL | SWT.BORDER);
		massesScrolledComposite.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());

		massesComposite = new Composite(massesScrolledComposite, SWT.NONE);
		massesComposite.setLayout(GridLayoutFactory.swtDefaults().create());
		massesScrolledComposite.setContent(massesComposite);

		massesComposites = new Composite[21];
		massChoices = new Spinner[21];

		for (int i = 0; i < 21; i++) {
			massesComposites[i] = new Composite(massesComposite, SWT.NONE);
			massesComposites[i].setLayoutData(GridDataFactory.fillDefaults().create());
			massesComposites[i].setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());

			Label massLabel = new Label(massesComposites[i], SWT.NONE);
			massLabel.setLayoutData(GridDataFactory.fillDefaults().create());
			massLabel.setText((i + 1) + ":");

			massChoices[i] = new Spinner(massesComposites[i], SWT.NONE);
			massChoices[i].setLayoutData(GridDataFactory.fillDefaults().create());
			massChoices[i].setMinimum(0);
			massChoices[i].setMaximum(128);

			Label amuLabel = new Label(massesComposites[i], SWT.NONE);
			amuLabel.setLayoutData(GridDataFactory.fillDefaults().create());
			amuLabel.setText("amu");
		}
		massesComposite.layout();
		massesComposite.setSize(massesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		massesScrolledComposite.setMinSize(massesComposite.computeSize(SWT.DEFAULT, 200));
	}

	protected void setVisibleItems(int selection) {
		for (int i = 1; i <= 21; i++) {
			if (i <= selection) {
				massesComposites[i - 1].setVisible(true);
			} else {
				massesComposites[i - 1].setVisible(false);
			}
		}
		massesScrolledComposite.layout();
		massesScrolledComposite.pack();
	}

	@Override
	public void setFocus() {
		massesComposites[0].setFocus();
	}

	public void refresh() {
		if (rga != null) {
			updateMasses();
		}
	}

	public void apply() {
		if (rga != null) {
			int[] masses = new int[sprNumberMasses.getSelection()];
			for (int i = 0; i < sprNumberMasses.getSelection(); i++) {
				masses[i] = massChoices[i].getSelection();
			}
			rga.setMasses(masses);
		}
	}

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
