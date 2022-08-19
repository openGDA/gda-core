package org.eclipse.scanning.test.util;

import java.util.concurrent.BrokenBarrierException;

import org.eclipse.richbeans.test.utilities.ui.ShellTest;
import org.eclipse.swtbot.swt.finder.junit5.SWTBotJunit5Extension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SWTBotJunit5Extension.class)
public abstract class JUnit5ShellTest extends ShellTest {

	@BeforeAll
	static void beforeAll() {
		ShellTest.startUI();
	}

	@AfterAll
	static void afterAll() {
		ShellTest.startUI();
	}

	@BeforeEach
	void beforeEach() throws InterruptedException, BrokenBarrierException {
		super.setup();
	}

	@AfterEach
	void afterEach() throws InterruptedException {
		super.teardown();
	}

}
