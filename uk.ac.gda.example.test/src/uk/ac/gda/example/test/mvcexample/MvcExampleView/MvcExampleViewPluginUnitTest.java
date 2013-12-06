package uk.ac.gda.example.test.mvcexample.MvcExampleView;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.device.motor.DummyMotor;
import gda.device.scannable.ScannableMotor;
import gda.factory.FactoryException;
import gda.rcp.util.OSGIServiceRegister;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.client.observablemodels.ScannableWrapper;
import uk.ac.gda.example.mvcexample.MvcExampleModel;
import uk.ac.gda.example.mvcexample.MvcExampleView;

public class MvcExampleViewPluginUnitTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetBtnSelected() throws Exception {
		MyMvcExampleModel model = new MyMvcExampleModel();

		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(MvcExampleModel.class);
		modelReg.setService(model);
		modelReg.afterPropertiesSet();

		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		MvcExampleView view = (MvcExampleView) window.getActivePage().showView(
				MvcExampleView.ID);
		window.getActivePage().activate(view);

		ActionFactory.IWorkbenchAction maximizeAction = ActionFactory.MAXIMIZE
				.create(window);
		maximizeAction.run(); // Will maximize the active part
		for (int i = 0; i < 60; i++) {
			delay(1000);
			model.setSelected(!model.isSelected());
		}

	}

	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	private void delay(long waitTimeMillis) {
		Display display = Display.getCurrent();

		// If this is the UI thread,
		// then process input.

		if (display != null) {
			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
			while (System.currentTimeMillis() < endTimeMillis) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.update();
		}
		// Otherwise, perform a simple sleep.

		else {
			try {
				Thread.sleep(waitTimeMillis);
			} catch (InterruptedException e) {
				// Ignored.
			}
		}
	}

	class MyMvcExampleModel implements MvcExampleModel{
		private final PropertyChangeSupport pcs = new PropertyChangeSupport(
				this);

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			this.pcs.addPropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			this.pcs.removePropertyChangeListener(listener);
		}

		boolean selected;
		ScannableMotionUnits scannable;

		ScannableWrapper wrapper;
		
		
		@Override
		public boolean isSelected() {
			return selected;
		}

		@Override
		public void setSelected(boolean selected) {
			this.pcs.firePropertyChange(MvcExampleModel.SELECTED_PROPERTY_NAME, this.selected, this.selected=selected);
		}

		double position;
		@Override
		public double getPosition() {
			return position;
		}

		@Override
		public void setPosition(double position) {
			this.pcs.firePropertyChange(MvcExampleModel.POSITION_PROPERTY_NAME, this.position, this.position=position);
		}

		@Override
		public ScannableWrapper getScannableWrapper() throws Exception{
			if( wrapper == null){
				DummyMotor dummyMotor = new DummyMotor();
				dummyMotor.setName("dummy_motor");
				dummyMotor.configure();
				ScannableMotor scannable =  new ScannableMotor();
				scannable.setMotor(dummyMotor);
				scannable.setName("motor1");
				scannable.configure();
				wrapper = new ScannableWrapper(scannable);
			}
			return wrapper;
		}

	};

}
