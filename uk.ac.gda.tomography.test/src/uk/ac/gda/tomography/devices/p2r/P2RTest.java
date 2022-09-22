package uk.ac.gda.tomography.devices.p2r;

import gda.device.motor.simplemotor.SimpleMotor;
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
		cont.setPrefix("D");
		cont.setPosition_index(0);
		cont.setSpeed_index(3);
		cont.afterPropertiesSet();
		SimpleMotor motor = new SimpleMotor();
		motor.setName("simpleMotor");
		motor.setSmc(cont);
		motor.afterPropertiesSet();
		motor.moveTo(10.);
		double position = motor.getPosition();
		Assert.assertEquals(10., position,.001);
	}

	@Ignore
	public void testTelnet() throws Exception {
		SocketBidiAsciiCommunicator communicator = new SocketBidiAsciiCommunicator();
		communicator.setAddress("192.168.0.2");
		communicator.setPort(23);
		communicator.afterPropertiesSet();
		P2RMotorController cont = new P2RMotorController();
		cont.setBidiAsciiCommunicator(communicator);
		cont.setPrefix("D");
		cont.setPosition_index(0);
		cont.setSpeed_index(3);
		cont.afterPropertiesSet();
		SimpleMotor motor = new SimpleMotor();
		motor.setName("simpleMotor");
		motor.setSmc(cont);
		motor.afterPropertiesSet();
		motor.setSpeed(.1);
		motor.moveTo(0.);
		while(motor.isMoving()){
			Thread.sleep(50);
		}
		motor.moveTo(1.);
		while(motor.isMoving()){
			Thread.sleep(50);
		}
		double position = motor.getPosition();
		Assert.assertEquals(10., position,.005);
	}
	
}
