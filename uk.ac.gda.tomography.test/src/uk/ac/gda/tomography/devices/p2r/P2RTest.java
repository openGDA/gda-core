package uk.ac.gda.tomography.devices.p2r;

import gda.device.motor.simplemotor.SimpleMotor;
import gda.device.motor.simplemotor.SimpleMotorViaIndexedController;
import gda.io.socket.SocketBidiAsciiCommunicator;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class P2RTest {

	@Test
	public void testDummy() throws Exception {
		DummyP2RBidiAsciiCommunicator communicator = new DummyP2RBidiAsciiCommunicator();
		P2RMotorController cont = new P2RMotorController();
		cont.setBidiAsciiCommunicator(communicator);
		cont.afterPropertiesSet();
		SimpleMotorViaIndexedController singleMotorController = new SimpleMotorViaIndexedController();
		singleMotorController.setSimc(cont);
		singleMotorController.setIndex(0);
		singleMotorController.afterPropertiesSet();
		SimpleMotor motor = new SimpleMotor();
		motor.setName("simpleMotor");
		motor.setSmc(singleMotorController);
		motor.afterPropertiesSet();
		motor.moveTo(10.);
		double position = motor.getPosition();
		Assert.assertEquals(10., position,.001);
	}

	@Ignore
	public void testTelnet() throws Exception {
		SocketBidiAsciiCommunicator communicator = new SocketBidiAsciiCommunicator();
		communicator.setAddress("172.23.6.180");
		communicator.setPort(23);
		communicator.afterPropertiesSet();
		P2RMotorController cont = new P2RMotorController();
		cont.setBidiAsciiCommunicator(communicator);
		cont.afterPropertiesSet();
		SimpleMotorViaIndexedController singleMotorController = new SimpleMotorViaIndexedController();
		singleMotorController.setSimc(cont);
		singleMotorController.setIndex(0);
		singleMotorController.afterPropertiesSet();
		SimpleMotor motor = new SimpleMotor();
		motor.setName("simpleMotor");
		motor.setSmc(singleMotorController);
		motor.afterPropertiesSet();
		motor.moveTo(10.);
		while(motor.isMoving()){
			Thread.sleep(50);
		}
		double position = motor.getPosition();
		Assert.assertEquals(10., position,.001);
	}
	
}
