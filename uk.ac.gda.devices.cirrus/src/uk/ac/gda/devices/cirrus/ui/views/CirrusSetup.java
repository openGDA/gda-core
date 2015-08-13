package uk.ac.gda.devices.cirrus.ui.views;

import gda.factory.Finder;

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

import uk.ac.gda.devices.cirrus.Cirrus;

public class CirrusSetup extends ViewPart {

	public static String ID = "uk.ac.gda.devices.cirrus.cirrussetup";

	private Composite massesComposite;
	private Composite[] massesComposites;
	private Cirrus cirrus;
	private Spinner[] massChoices;
	private Spinner sprNumberMasses;

	private ScrolledComposite mainScrolledComposite;

	private Label lblMessages;

	public CirrusSetup() {
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

		lblMessages = new Label(topComposite, SWT.NONE);
		lblMessages.setLayoutData(GridDataFactory.swtDefaults().span(2, 1)
				.create());
		lblMessages.setText("");

		String collectionRateToolTip = "The rate data will be logged when recording. If 0 then RGA will be run as fast as possible.";

		Label lbCollectionRate = new Label(topComposite, SWT.NONE);
		lbCollectionRate.setLayoutData(GridDataFactory.swtDefaults().create());
		lbCollectionRate.setText("Collection rate (s):");
		lbCollectionRate.setToolTipText(collectionRateToolTip);

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
		Integer[] masses = cirrus.getMasses();
		setVisibleItems(masses.length);

		int index = 0;
		for (Integer mass : masses) {
			massChoices[index].setSelection(mass);
			index++;
		}
		sprNumberMasses.setSelection(masses.length);
	}

	private boolean findRGA() {
		cirrus = (Cirrus) Finder.getInstance().find("rga");
		return cirrus != null;
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
		if (cirrus != null) {
			updateMasses();
		}
	}

	/**
	 * Apply the masses shown in th UI to the server-side object
	 */
	public void apply() {
		if (cirrus != null) {
			Integer[] masses = new Integer[sprNumberMasses.getSelection()];
			for (int i = 0; i < sprNumberMasses.getSelection(); i++) {
				masses[i] = massChoices[i].getSelection();
			}
			cirrus.setMasses(masses);
		}
	}
}
