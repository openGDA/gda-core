package org.eclipse.scanning.test.ui.composites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.binding.BeanService;
import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.device.ui.composites.MultiStepComposite;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.test.util.JUnit5ShellTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Flaky new-scanning test")
public class MultiStepCompositeTest extends JUnit5ShellTest {

	private IScannableDeviceService service;


	private AxialMultiStepModel     model;
	private MultiStepComposite ui;
	private IBeanController<AxialMultiStepModel>    controller;

	@Override
	protected Shell createShell(Display display) throws Exception {

		this.model = new AxialMultiStepModel();

		Shell shell = new Shell(display);
		shell.setText("Multi-Step");
		shell.setLayout(new GridLayout(1, false));

		this.ui = new MultiStepComposite(shell, SWT.NONE);

		this.controller = BeanService.getInstance().createController(ui, model);
		controller.beanToUI();
		controller.switchState(true);

		this.service = new MockScannableConnector(null);
		AnnotationManager manager = new AnnotationManager(Inject.class);
		manager.addDevices(ui);
		manager.invoke(Inject.class, service, model);

		shell.pack();
		shell.setSize(500, 500);
		shell.open();
		return shell;
	}

	@Test
	public void shellThere() {
		assertNotNull(bot.shell("Multi-Step")); // Bot throws exception anyway if null
	}

	@Disabled("DAQ-2088 This test passes when this class is run on its own (JUnit from Eclipse AND from ant) but not as part of the whole set of tests")
	@Test
	public void checkEnergy() {
		assertEquals(7, bot.comboBox(0).selectionIndex());
	}

	@Test
	public void noSteps() {
		assertEquals(0, bot.table(0).rowCount());
	}

	@Test
	public void addStep1() throws Exception {
		try {
			model.addRange(10000, 20000, 1000);
			synchExec(()->controller.beanToUI());

			assertEquals(1, bot.table(0).rowCount());
			assertEquals("10000.00 eV", bot.styledText(0).getText());
			assertEquals("20000.00 eV", bot.styledText(1).getText());
			assertEquals("1000.0000 eV",  bot.styledText(2).getText());
		} finally {
			model.clear();
		}
	}

	@Test
	public void addStep2() throws Exception {
		try {
			model.addRange(10.1, 20.2, 1.4);
			synchExec(()->controller.beanToUI());

			assertEquals(1, bot.table(0).rowCount());
			assertEquals("10.10 eV", bot.styledText(0).getText());
			assertEquals("20.20 eV", bot.styledText(1).getText());
			assertEquals("1.4000 eV",  bot.styledText(2).getText());
		} finally {
			model.clear();
		}
	}

	/**
	 * The UI only allows forwards steps.
	 * @throws Exception
	 */
	@Test
	public void negativeStep() throws Exception {
		try {
			model.addRange(20.2, 10.1, -1.4);
			synchExec(()->controller.beanToUI());

			assertEquals(1, bot.table(0).rowCount());
			assertEquals("20.20 eV", bot.styledText(0).getText());
			assertEquals("10.10 eV", bot.styledText(1).getText());
			assertEquals("-1.4000 eV", bot.styledText(2).getText());

			Color red = new Color(bot.getDisplay(), 255, 0, 0, 255);
			if (bot.styledText(2).foregroundColor().equals(red)) {
				throw new IllegalArgumentException("The foreground should be red as UI only allows forwards steps!");
			}

		} finally {
			model.clear();
		}
	}


	@Test
	public void nineSteps() throws Exception {
		try {
			for (int i = 0; i < 9; i++) {
				model.addRange(20+(i*1), 50+(i*1), 5);
			}
			synchExec(()->controller.beanToUI());

			assertEquals(9, bot.table(0).rowCount());
			assertTrue(bot.button(0).isEnabled());

		} finally {
			model.clear();
		}
	}

	@Test
	public void tenSteps() throws Exception {
		try {
			for (int i = 0; i < 10; i++) {
				model.addRange(20+(i*1), 50+(i*1), 5);
			}
			synchExec(()->controller.beanToUI());

			assertEquals(10, bot.table(0).rowCount());
			assertFalse(bot.button(0).isEnabled());

		} finally {
			model.clear();
		}
	}

	@Test
	public void elevenSteps() throws Exception {
		try {
			for (int i = 0; i < 11; i++) {
				model.addRange(20+(i*1), 50+(i*1), 5);
			}
			synchExec(()->controller.beanToUI());

			// The bean is over the add range so it should
			// still edit in the widget with an add button disabled
			assertEquals(11, bot.table(0).rowCount());
			assertFalse(bot.button(0).isEnabled());


		} finally {
			model.clear();
		}
	}

	@Test
	public void addNineSteps() throws Exception {
		try {
			for (int i = 0; i < 9; i++) {
				bot.button(0).click();
			}

			assertEquals(9, bot.table(0).rowCount());
			synchExec(()->controller.uiToBean());
			assertEquals(9, model.getModels().size());

		} finally {
			model.clear();
		}
	}

	@Test
	public void addElevenSteps() throws Exception {
		try {
			for (int i = 0; i < 11; i++) {
				if (bot.button().isEnabled()) bot.button(0).click();
			}

			assertEquals(10, bot.table(0).rowCount());
			synchExec(()->controller.uiToBean());
			assertEquals(10, model.getModels().size());


		} finally {
			model.clear();
		}
	}

	@Test
	public void addNineDeleteThree() throws Exception {
		try {
			for (int i = 0; i < 9; i++) {
				if (bot.button().isEnabled()) bot.button(0).click();
			}
			for (int i = 0; i < 3; i++) {
				bot.button(1).click();
			}

			assertEquals(6, bot.table(0).rowCount());
			synchExec(()->controller.uiToBean());
			assertEquals(6, model.getModels().size());


		} finally {
			model.clear();
		}
	}

	@Test
	public void addElevenDeleteThree() throws Exception {
		try {
			for (int i = 0; i < 11; i++) {
				if (bot.button().isEnabled()) bot.button(0).click();
			}
			for (int i = 0; i < 3; i++) {
				bot.button(1).click();
			}

			assertEquals(7, bot.table(0).rowCount()); // 7 not 8 because 10 is the limit.
			synchExec(()->controller.uiToBean());
			assertEquals(7, model.getModels().size());


		} finally {
			model.clear();
		}
	}

	/**
	 * Upper bound for 'energy' is 35000
	 * @throws Exception
	 */
	@Disabled("DAQ-2088 This test consistently fails on Jenkins only")
	@Test
	public void checkNotRedWhenWithinBounds() throws Exception {
		try {
			model.addRange(34000, 35000, 2500);
			synchExec(()->controller.beanToUI());
			Color red = new Color(bot.getDisplay(), 255, 0, 0, 255);
			assertFalse(bot.styledText(1).foregroundColor().equals(red));
			assertFalse(bot.styledText(2).foregroundColor().equals(red));


		} finally {
			model.clear();
		}
	}

	/**
	 * This test checks that the 'energy' scannable changed the default
	 * bounds to its bounds (35000)
	 * @throws Exception
	 */
	@Test
	public void checkRedWhenOutOfBounds() throws Exception {
		try {
			model.addRange(35000.1, 45000, 100);
			synchExec(()->controller.beanToUI());
			Color red = new Color(bot.getDisplay(), 255, 0, 0, 255);
			assertEquals(red, bot.styledText(0).foregroundColor());
			assertEquals(red, bot.styledText(1).foregroundColor());


		} finally {
			model.clear();
		}
	}

}
