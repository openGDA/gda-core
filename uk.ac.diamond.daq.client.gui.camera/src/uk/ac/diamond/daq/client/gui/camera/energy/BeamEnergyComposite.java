package uk.ac.diamond.daq.client.gui.camera.energy;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.client.gui.camera.energy.EnergyController.EnergySelectionType;
import uk.ac.diamond.daq.client.gui.camera.energy.EnergyController.EnergyType;

public class BeamEnergyComposite extends Composite implements EnergyControllerListener {
	private static final int CONTROL_WIDTH = 200;
	
	private EnergyController controller;
	
	private Button monoBeamRadio = null;
	private Text monoEnergyText = null;
	private Button pinkBeamRadio = null;
	private ComboViewer comboViewer = null;
	
	private final Color black;
	private final Color red;
	
	private class EnergyTypeListener implements Listener {
		boolean armed = true;
		
		@Override
		public void handleEvent(Event event) {
			if (!armed) {
				return;
			}
			armed = false;
			if(monoBeamRadio != null 
					&& monoBeamRadio.getSelection() 
					&& controller.getEnergyType() == EnergyType.pink) {
				double energy = controller.getMonoEnergy();
				controller.setMonoEnergy(energy);
			}
			if (pinkBeamRadio != null 
					&& pinkBeamRadio.getSelection() 
					&& controller.getEnergyType() == EnergyType.mono) {
				DiscreteEnergy discreteEnergy = controller.getDiscreteEnergy();
				controller.setDiscreteEnergy(discreteEnergy);
			}
			armed = true;
		}
	}
	
	private EnergyTypeListener energyTypeListener = new EnergyTypeListener();
	
	private class MonoEnergyListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			try {
				double energy = Double.parseDouble(monoEnergyText.getText());
				controller.setMonoEnergy(energy);
				monoEnergyText.setForeground(black);
			} catch (NumberFormatException e) {
				monoEnergyText.setForeground(red);
			}
		}
	}
	
	private MonoEnergyListener monoEnergyListener = new MonoEnergyListener();
	
	private class PinkEnergyListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				DiscreteEnergy discreteEnergy = (DiscreteEnergy)selection.getFirstElement();
				controller.setDiscreteEnergy(discreteEnergy);
			}
		}
	}
	
	private PinkEnergyListener pinkEnergyListener = new PinkEnergyListener();
	
	public BeamEnergyComposite(Composite parent, EnergyController controller) {
		super(parent, SWT.NONE);
		
		black = new Color(parent.getDisplay(), 255, 0, 0);
		red = new Color(parent.getDisplay(), 255, 0, 0);
		this.controller = controller;
		controller.addListener(this);
		
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);
		
		if (controller.getEnergySelectionType() == EnergySelectionType.monoBeamOnly 
				|| controller.getEnergySelectionType() == EnergySelectionType.pinkAndMonoBeams) {
			monoBeamRadio = new Button (this, SWT.RADIO);
			monoBeamRadio.setText("Monochromatic");
			monoBeamRadio.addListener(SWT.Selection, energyTypeListener);
			GridDataFactory.swtDefaults().applyTo(monoBeamRadio);
			
			monoEnergyText = new Text (this, SWT.RIGHT);
			GridDataFactory.swtDefaults().hint(CONTROL_WIDTH, SWT.DEFAULT).applyTo(monoEnergyText);
		}
		
		if (controller.getEnergySelectionType() == EnergySelectionType.pinkBeamOnly 
				|| controller.getEnergySelectionType() == EnergySelectionType.pinkAndMonoBeams) {
			pinkBeamRadio = new Button (this, SWT.RADIO);
			pinkBeamRadio.setText("Polychromatic");
			pinkBeamRadio.addListener(SWT.Selection, energyTypeListener);
			GridDataFactory.swtDefaults().applyTo(pinkBeamRadio);

			comboViewer = new ComboViewer(this, SWT.NONE);
			GridDataFactory.swtDefaults().hint(CONTROL_WIDTH, SWT.DEFAULT).applyTo(comboViewer.getControl());
		}
		
		controller.notifyListeners();
	}
	
	@Override
	public void setMonoEnergy (double energy) {
		if (monoBeamRadio != null) {
			monoBeamRadio.setSelection(true);
		}
		if (monoEnergyText != null) {
			monoEnergyText.setEnabled(true);
			monoEnergyText.setText(Double.toString(energy));
			monoEnergyText.addListener(SWT.Selection, monoEnergyListener);
		}
		if (pinkBeamRadio != null) {
			pinkBeamRadio.setSelection(false);
		}
		if (comboViewer != null) {
			comboViewer.removeSelectionChangedListener(pinkEnergyListener);
			comboViewer.getCombo().setEnabled(false);
			removeItems();
		}
	}
	
	@Override
	public void setDiscreteEnergy(DiscreteEnergy discreteEnergy) {
		if (monoBeamRadio != null) {
			monoBeamRadio.setSelection(false);
		}
		if (monoEnergyText != null) {
			monoEnergyText.removeListener(SWT.Selection, monoEnergyListener);
			monoEnergyText.setEnabled(false);
			monoEnergyText.setText("");
		}
		if (pinkBeamRadio != null) {
			pinkBeamRadio.setSelection(true);
		}
		if (comboViewer != null) {
			comboViewer.getCombo().setEnabled(true);
			comboViewer.removeSelectionChangedListener(pinkEnergyListener);
			removeItems();
			comboViewer.add(controller.getDiscreteEnergies().toArray());
			if (discreteEnergy != null) {
				comboViewer.setSelection(new StructuredSelection(discreteEnergy));
			}
			comboViewer.addSelectionChangedListener(pinkEnergyListener);
		}
	}
	
	private void removeItems () {
		if (comboViewer != null) {
			DiscreteEnergy element = null;
			while (true){
				element = (DiscreteEnergy)comboViewer.getElementAt(0);
				if (element == null) {
					break;
				}
				comboViewer.remove(element);
			}
		}
	}
}
