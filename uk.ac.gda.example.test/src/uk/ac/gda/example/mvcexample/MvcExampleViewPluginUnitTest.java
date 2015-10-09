package uk.ac.gda.example.mvcexample;

import gda.device.motor.DummyMotor;
import gda.device.scannable.ScannableMotor;
import gda.rcp.util.OSGIServiceRegister;

import java.util.ArrayList;

import junit.framework.Assert;

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.beans.ObservableModel;
import uk.ac.gda.client.observablemodels.ScannableWrapper;

public class MvcExampleViewPluginUnitTest {

	private static MvcExampleView view;
	private static MvcExampleView view2;
	private static MyMvcExampleModel model;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		model = new MyMvcExampleModel();
		ObservableList items = model.getItems();
		MyMvcExampleItem e = new MyMvcExampleItem("Item", 0.);
		items.add(e);

		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(MvcExampleModel.class);
		modelReg.setService(model);
		modelReg.afterPropertiesSet();

		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		view = (MvcExampleView) window.getActivePage().showView(
				MvcExampleView.ID);
		window.getActivePage().activate(view);
		ActionFactory.IWorkbenchAction maximizeAction = ActionFactory.MAXIMIZE
				.create(window);
		maximizeAction.run(); // Will maximize the active part

		view2 = (MvcExampleView) window.getActivePage().showView(
				"uk.ac.gda.example.mvcexample.MvcExampleViewTest");
		window.getActivePage().activate(view2);

	}

	@AfterClass
	public static void tearDownAfterClass() {
		waitForJobs();
//		delay(20000);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.hideView(view);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.hideView(view2);
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testSetBtnSelected() {
		model.setSelected(false);
		Assert.assertEquals(false, view.btn1.getSelection());
		model.setSelected(true);
		Assert.assertEquals(true, view.btn1.getSelection());

	}

	@Test
	public void testMotor() throws Exception {
		Assert.assertEquals("0 mm", view.motorPosControl._getTextForTesting());
		model.scannable.asynchronousMoveTo(1.);
		delay(2000);
		Assert.assertEquals("1 mm", view.motorPosControl._getTextForTesting());
	}
	@Test
	public void testPosition() {
		model.setPosition(10.);
		Assert.assertEquals("10", view.numberControl._getTextForTesting());
	}


	@Test
	public void testItems() {
		ObservableList items = model.getItems();
		for( int i=0; i<10; i++){
			MyMvcExampleItem e = new MyMvcExampleItem("ItemA"+i, 0.);
			items.add(e);
		}
		delay(1000);
		for( int i=0; i<10; i++){
			for( int j=0; j<10; j++){
				((MyMvcExampleItem)items.get(j)).setValue(i+j);
			}
			delay(1000);
		}
		Assert.assertEquals("18.0",view.viewer.getTable().getItem(9).getText(0));
		items.clear();
		delay(1000);
		for( int i=0; i<10; i++){
			MyMvcExampleItem e = new MyMvcExampleItem("ItemB"+i, 0.);
			items.add(e);
		}
		delay(1000);

	}

	/**
	 * Process UI input but do not return for the specified time interval.
	 *
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	private static void delay(long waitTimeMillis) {
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

	/**
	 * Wait until all background tasks are complete.
	 */
	public static void waitForJobs() {
		while (!Job.getJobManager().isIdle())
			delay(1000);
	}
}

class MyMvcExampleModel  extends ObservableModel  implements MvcExampleModel {

	boolean selected;
	ScannableWrapper wrapper;

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean selected) {
		firePropertyChange(MvcExampleModel.SELECTED_PROPERTY_NAME,
				this.selected, this.selected = selected);
	}

	double position;

	@Override
	public double getPosition() {
		return position;
	}

	@Override
	public void setPosition(double position) {
		firePropertyChange(MvcExampleModel.POSITION_PROPERTY_NAME,
				this.position, this.position = position);
	}
	protected DummyMotor dummyMotor;
	protected ScannableMotor scannable;

	@Override
	public ScannableWrapper getScannableWrapper() throws Exception {
		if (wrapper == null) {
			dummyMotor = new DummyMotor();
			dummyMotor.setName("dummy_motor");
			dummyMotor.configure();
			scannable = new ScannableMotor();
			scannable.setMotor(dummyMotor);
			scannable.setName("motor1");
			scannable.setUserUnits("mm");
			scannable.configure();
			wrapper = new ScannableWrapper(scannable);
		}
		return wrapper;
	}

	WritableList items = new WritableList(new ArrayList<MvcExampleItem>(), MvcExampleItem.class);

	@Override
	public WritableList getItems() {
		return items;
	}



};



class MyMvcExampleItem extends ObservableModel implements MvcExampleItem {

	double value;
	@Override
	public double getValue() {
		return value;
	}


	public void setValue(double newVal){
		firePropertyChange(MvcExampleItem.VALUE_PROPERTY_NAME,
				this.value, this.value = newVal);
	}

	String name;
	@Override
	public String getName() {
		return name;
	}


	public MyMvcExampleItem(String name, double value) {
		super();
		this.value = value;
		this.name = name;
	}
}