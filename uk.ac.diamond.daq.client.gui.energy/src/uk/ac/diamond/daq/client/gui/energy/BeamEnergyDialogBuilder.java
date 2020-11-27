package uk.ac.diamond.daq.client.gui.energy;

import static uk.ac.gda.ui.tool.ClientMessages.BEAM_ENERGY_CONTROL;
import static uk.ac.gda.ui.tool.ClientMessages.DIFFRACTION;
import static uk.ac.gda.ui.tool.ClientMessages.IMAGING;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import uk.ac.diamond.daq.beamline.configuration.api.ConfigurationWorkflow;
import uk.ac.diamond.daq.client.gui.energy.EnergyWorkflowController.EnergySelectionType;
import uk.ac.diamond.daq.mapping.ui.properties.stages.ManagedScannable;
import uk.ac.diamond.daq.mapping.ui.stage.BeamSelector;
import uk.ac.gda.client.composites.MotorCompositeFactory;
import uk.ac.gda.client.properties.MotorProperties;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Assembles a dialog to configure the energy of one, or more beams. The methods
 * <ul>
 * <li>{@link #getDiffractionEnergyController()}</li>
 * <li>{@link #getImagingEnergyController()}</li>
 * </ul>
 *
 * allows the client to insert in the dialog only the necessary controllers.
 *
 * Each beam controller collects one or more server side findable beans,
 * instances of {@link ConfigurationWorkflow}. Using the client properties file,
 * each beamline may configure the beans name setting the following properties
 *
 * <ul>
 * <li>{@link DIFFRACTION_MONO_WORKFLOW_PROPERTY} (default value
 * {@link DIFFRACTION_MONO_WORKFLOW_PROPERTY_DEFAULT})</li>
 * <li>{@link DIFFRACTION_PINK_WORKFLOW_PROPERTY} (default value
 * {@link DIFFRACTION_PINK_WORKFLOW_PROPERTY_DEFAULT})</li>
 * <li>{@link IMAGING_MONO_WORKFLOW_PROPERTY} (default value
 * {@link IMAGING_MONO_WORKFLOW_PROPERTY_DEFAULT})</li>
 * </ul>
 *
 * @author Maurizio Nagni
 *
 */
public class BeamEnergyDialogBuilder {

	public static final String DIFFRACTION_MONO_WORKFLOW_PROPERTY = "beam.diffraction.mono.workflow";
	public static final String DIFFRACTION_MONO_WORKFLOW_PROPERTY_DEFAULT = "diffraction_mono_workflow";

	public static final String DIFFRACTION_PINK_WORKFLOW_PROPERTY = "beam.diffraction.pink.workflow";
	public static final String DIFFRACTION_PINK_WORKFLOW_PROPERTY_DEFAULT = "diffraction_pink_workflow";

	public static final String IMAGING_MONO_WORKFLOW_PROPERTY = "beam.imaging.mono.workflow";
	public static final String IMAGING_MONO_WORKFLOW_PROPERTY_DEFAULT = "imaging_mono_workflow";

	private static final String DIFFRACTION_MONO_WORKFLOW = LocalProperties.get(DIFFRACTION_MONO_WORKFLOW_PROPERTY,
			DIFFRACTION_MONO_WORKFLOW_PROPERTY_DEFAULT);
	private static final String DIFFRACTION_PINK_WORKFLOW = LocalProperties.get(DIFFRACTION_PINK_WORKFLOW_PROPERTY,
			DIFFRACTION_PINK_WORKFLOW_PROPERTY_DEFAULT);
	private static final String IMAGING_MONO_WORKFLOW = LocalProperties.get(IMAGING_MONO_WORKFLOW_PROPERTY,
			IMAGING_MONO_WORKFLOW_PROPERTY_DEFAULT);

	private EnergyWorkflowController diffractionController;
	private EnergyWorkflowController imagingController;

	private EnergyWorkflowController getDiffractionEnergyController() {
		ConfigurationWorkflow diffractionMono =
				(ConfigurationWorkflow) Finder.findOptional(DIFFRACTION_MONO_WORKFLOW).orElse(null);
		ConfigurationWorkflow diffractionPoly =
				(ConfigurationWorkflow) Finder.findOptional(DIFFRACTION_PINK_WORKFLOW).orElse(null);
		return new EnergyWorkflowController(EnergySelectionType.BOTH, diffractionMono, diffractionPoly);
	}

	private final EnergyWorkflowController getImagingEnergyController() {
		ConfigurationWorkflow imagingMono =
				(ConfigurationWorkflow) Finder.findOptional(IMAGING_MONO_WORKFLOW).orElse(null);
		return new EnergyWorkflowController(EnergySelectionType.MONO, imagingMono, null);
	}

	/**
	 * Add a controller for the diffraction beam finding the
	 * {@link ConfigurationWorkflow} beans named {@link #DIFFRACTION_MONO_WORKFLOW}
	 * and {@link #DIFFRACTION_PINK_WORKFLOW}
	 *
	 * @return an instance of this builder.
	 */
	public BeamEnergyDialogBuilder addDiffractionController() {
		this.diffractionController = getDiffractionEnergyController();
		return this;
	}

	/**
	 * Add a controller for the diffraction beam finding the
	 * {@link ConfigurationWorkflow} beans named {@link #DIFFRACTION_MONO_WORKFLOW}
	 * and {@link #DIFFRACTION_PINK_WORKFLOW}
	 *
	 * @return an instance of this builder.
	 */
	public BeamEnergyDialogBuilder addImagingController() {
		this.imagingController = getImagingEnergyController();
		return this;
	}

	public Dialog build(Shell parentShell) {
		return new BeamEnergyDialog(parentShell);
	}

	private class BeamEnergyDialog extends Dialog {

		public BeamEnergyDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
			addBeamSelectorWidget(container);

			Composite base;
			if (diffractionController != null && imagingController != null) {
				base = createClientCompositeWithGridLayout(container, SWT.NONE, 3);
			} else {
				base = createClientCompositeWithGridLayout(container, SWT.NONE, 1);
			}

			if (diffractionController != null) {
				createEnergyControl(DIFFRACTION, diffractionController, base);
			}
			if (diffractionController != null && imagingController != null) {
				createDivider(base);
			}
			if (imagingController != null) {
				createEnergyControl(IMAGING, imagingController, base);
			}
			return base;
		}

		private void addBeamSelectorWidget(Composite parent) {
			if (getBeamSelector() == null || !getBeamSelector().isAvailable()) {
				return;
			}
			MotorProperties beamSelectorProperties = new MotorProperties() {
				@Override
				public String getController() {
					return getBeamSelector().getScannablePropertiesDocument().getScannable();
				}

				@Override
				public String getName() {
					return getBeamSelector().getScannablePropertiesDocument().getLabel();
				}
			};
			new MotorCompositeFactory(beamSelectorProperties).createComposite(parent, SWT.HORIZONTAL);
		}

		private void createEnergyControl(ClientMessages title, EnergyWorkflowController controller, Composite parent) {
			Composite composite = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
			Label labelName = createClientLabel(composite, SWT.NONE, title);
			createClientGridDataFactory().align(SWT.BEGINNING, SWT.END).applyTo(labelName);
			new BeamEnergyControl(controller).draw(composite);
		}

		private void createDivider(Composite parent) {
			Composite composite = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
			ClientSWTElements.createClientGridDataFactory().applyTo(new Label(composite, SWT.SEPARATOR | SWT.VERTICAL));
		}

		@Override
		protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
			if (id == IDialogConstants.CANCEL_ID)
				return null;
			return super.createButton(parent, id, label, defaultButton);
		}

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText(getMessage(BEAM_ENERGY_CONTROL));
		}

		private ManagedScannable<String> getBeamSelector() {
			return BeamSelector.getManagedScannable();
		}

	}
}
