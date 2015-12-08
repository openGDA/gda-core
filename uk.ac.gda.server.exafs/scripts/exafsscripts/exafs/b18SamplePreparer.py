from gdascripts.messages.handle_messages import simpleLog
from time import sleep
from gdascripts.metadata.metadata_commands import meta_add

class B18SamplePreparer:
	def __init__(self, sxcryo_scannable, xytheta_scannable, ln2cryo_scannable, lakeshore_scannable, furnace_scannable, pulsetube_scannable, generic_cryostat, samplewheel_scannable, user_scannable):
		self.sxcryo_scannable = sxcryo_scannable
		self.xytheta_scannable = xytheta_scannable
		self.ln2cryo_scannable = ln2cryo_scannable
		self.lakeshore_scannable = lakeshore_scannable
		self.furnace_scannable = furnace_scannable
		self.pulsetube_scannable = pulsetube_scannable
		self.generic_cryostat = generic_cryostat
		self.samplewheel_scannable = samplewheel_scannable
		self.user_scannable = user_scannable
		self.logging_enabled = True

	def setLoggingEnabled(self, enabled):
		self.logging_enabled = enabled

	def log(self, msg):
		if self.logging_enabled == True:
			simpleLog(msg)
		else:
			print msg

	def prepare(self, sampleParameters):

		enabled = sampleParameters.getSampleWheelParameters().isWheelEnabled()
		if enabled:
			self._control_sample_wheel(sampleParameters.getSampleWheelParameters())

		if sampleParameters.getStage() == "xythetastage":
			self._control_xytheta_stage(sampleParameters.getXYThetaStageParameters())
		elif sampleParameters.getStage() == "ln2cryostage":
			self._control_ln2cryo_stage(sampleParameters.getLN2CryoStageParameters())
		elif sampleParameters.getStage() == "sxcryostage":
			self._control_sxcryo_stage(sampleParameters.getSXCryoStageParameters())
		elif sampleParameters.getStage() == "userstage":
			self._control_user_stage(sampleParameters.getUserStageParameters())
		if sampleParameters.getTemperatureControl() != "None":
			if sampleParameters.getTemperatureControl() == "furnace":
				return self._control_furnace(sampleParameters.getFurnaceParameters())
			elif sampleParameters.getTemperatureControl() == "lakeshore":
				return self._control_lakeshore(sampleParameters.getLakeshoreParameters())
			elif sampleParameters.getTemperatureControl() == "pulsetubecryostat":
				return self._control_pulsetube(sampleParameters.getPulseTubeCryostatParameters())
			elif sampleParameters.getTemperatureControl() == "generic_cryostat":
				return self._control_generic_cryostat(sampleParameters.getGenericCryostatParameters())

	def _control_furnace(self, furnace_bean):
		self.log("furnace is the temperature controller")
		temperature = furnace_bean.getTemperature()
		tolerance = furnace_bean.getTolerance()
		wait_time = furnace_bean.getTime()
		only_read = furnace_bean.isControlFlag()
		meta_add(self.furnace_scannable)
		if only_read == False:
			self.log("controlling furnace")
			self.furnace_scannable(temperature);
			min = float(temperature) - float(tolerance)
			max = float(temperature) + float(tolerance)
			temp_final = False
			self.log("starting temperature control loop")
			while temp_final == False:
				temp_readback = float(self.furnace_scannable());
				if temp_readback in range(min, max):
					self.log("Temperature reached, checking if it has stabalised")
					finalised = True;
					time = 0;
					while finalised == True and time < wait_time:
						self.log("Temperature stable")
						temp_readback = float(self.furnace_scannable());
						if (temp_readback in range(min, max)) == False:
							self.log("Temperature unstable")
							finalised = False
						time += 1
						sleep(1)
					if finalised == True:
						temp_final = True
				else:
					self.log("Temperature = " + str(temp_readback))
					sleep(1)
		return [self.furnace_scannable]

	def _control_lakeshore(self, lakeshore_bean):
		self.log("Lakeshore is the temp controller")
		selectTemp0 = lakeshore_bean.isTempSelect0()
		selectTemp1 = lakeshore_bean.isTempSelect1()
		selectTemp2 = lakeshore_bean.isTempSelect2()
		selectTemp3 = lakeshore_bean.isTempSelect3()
		temp = lakeshore_bean.getSetPointSet()
		tolerance = lakeshore_bean.getTolerance()
		wait_time = lakeshore_bean.getTime()
		only_read = lakeshore_bean.isControlFlag()
		if selectTemp0 == True:
			self.lakeshore_scannable.setTempSelect(0);
		if selectTemp1 == True:
			self.lakeshore_scannable.setTempSelect(1);
		if selectTemp2 == True:
			self.lakeshore_scannable.setTempSelect(2);
		if selectTemp3 == True:
			self.lakeshore_scannable.setTempSelect(3);
		meta_add(self.lakeshore_scannable)
		if only_read == False:
			self.log("controlling lakeshore")
			if selectTemp0 == True:
				self.lakeshore_scannable.rawAsynchronousMoveTo([[0], temp]);
			if selectTemp1 == True:
				self.lakeshore_scannable.rawAsynchronousMoveTo([[1], temp]);
			if selectTemp2 == True:
				self.lakeshore_scannable.rawAsynchronousMoveTo([[2], temp]);
			if selectTemp3 == True:
				self.lakeshore_scannable.rawAsynchronousMoveTo([[3], temp]);
			min = float(temp) - float(tolerance)
			max = float(temp) + float(tolerance)
			temp_final = False
			self.log("Starting temperature control loop...")
			while temp_final == False:
				temp_readback = float(self.lakeshore_scannable());
				if temp_readback > min and temp_readback < max:
					self.log("Temperature reached, checking if it has stabilised.")
					finalised = True;
					time = 0;
					while finalised == True and time < wait_time:

						temp_readback = float(self.lakeshore_scannable());
						if temp_readback < min or temp_readback > max:
							self.log("Temperature unstable")
							finalised = False
						time += 1
						sleep(1)
					if finalised == True:
						self.log("Temperature stable")
						temp_final = True
				else:
					self.log("Temperature = " + str(temp_readback))
					sleep(1)
		return [self.lakeshore_scannable]

	def _control_pulsetube(self, bean):
		self.log("pulse tube is the temp controller")
		only_read = bean.isControlFlag()
		if only_read == False:
			temp = bean.getSetPoint()
			self.pulsetube_scannable.setTarget(temp)
			tolerance = bean.getTolerance()
			wait_time = bean.getTime()
			min = float(temp) - float(tolerance)
			max = float(temp) + float(tolerance)
			temp_final = False
			self.log("starting temperature control loop")
			while temp_final == False:
				self.pulsetube_scannable.collectData()
				temp_readback = float(self.pulsetube_scannable.readout()[0]);
				if temp_readback >= min and temp_readback <= max:
					self.log("Temperature reached, checking if it has stabilised")
					finalised = True;
					time = 0;
					while finalised == True and time < wait_time:
						self.log("Temperature stable")
						self.pulsetube_scannable.collectData()
						temp_readback = float(self.pulsetube_scannable.readout()[0]);
						if (temp_readback >= min and temp_readback <= max) == False:
							self.log("Temperature unstable")
							finalised = False
						time += 1
						sleep(1)
					if finalised == True:
						temp_final = True
				else:
					self.pulsetube_scannable.collectData()
					self.log("Temperature = " + str(self.pulsetube_scannable.readout()[0]))
					sleep(1)
		return [self.pulsetube_scannable]

	def _control_generic_cryostat(self, bean):
		self.log("generic_cryostat is the temp controller")
		only_read = bean.isControlFlag()
		if only_read == False:
			accuracy = bean.getAccuracy()
			self.generic_cryostat.setAccuracy(accuracy)
			temp = bean.getTemperature()
			self.generic_cryostat.setTargetTemperature(temp)

			wait_time = bean.getTime()
			temp_final = False
			self.log("starting temperature control loop")
			while temp_final == False:
				if self.generic_cryostat.isAtTargetTemperature():
					self.log("Temperature reached, checking if it has stabilised")
					finalised = True;
					time = 0;
					while finalised == True and time < wait_time:
						self.log("Temperature stable")
						if not self.generic_cryostat.isAtTargetTemperature():
							self.log("Temperature unstable")
							finalised = False
						time += 1
						sleep(1)
					if finalised == True:
						temp_final = True
				else:
					self.log("Temperature = " + str(self.generic_cryostat.getCurrentTemperature()))
					sleep(1)
		return [self.generic_cryostat]

	def _control_sxcryo_stage(self, bean):
		manual = bean.isManual()
		if manual:
			targetPosition = [bean.getHeight(), bean.getRot()]
		else:
			sample = bean.getSampleNumber()
			offset = bean.getCalibHeight()
			height = offset + (float(sample - 1) * -15.5)
			targetPosition = [height, bean.getRot()]
		self.log("moving sxcryostage (" + self.sxcryo_scannable.name + ") to " + str(targetPosition))
		self.sxcryo_scannable(targetPosition);
		self.log("sxcryostage move complete.")

	def _control_xytheta_stage(self, bean):
		targetPosition = [bean.getX(), bean.getY(), bean.getTheta()]
		self.log("moving xythetastage (" + self.xytheta_scannable.name + ") to " + str(targetPosition))
		self.xytheta_scannable(targetPosition);
		self.log("xythetastage move complete.")

	def _control_user_stage(self, bean):
		targetPosition = [bean.getAxis2(), bean.getAxis4(), bean.getAxis5(), bean.getAxis6(), bean.getAxis7(), bean.getAxis8()]
		self.log("moving userstage (" + self.user_scannable.name + ") to " + str(targetPosition))
		self.user_scannable(targetPosition);
		self.log("userstage move complete.")

	def _control_ln2cryo_stage(self, bean):
		manual = bean.isManual()
		if manual:
			targetPosition = [bean.getHeight(), bean.getAngle()]
			self.log("moving ln2cryostage (" + self.ln2cryo_scannable.name + ") to " + str(targetPosition))
			self.ln2cryo_scannable(targetPosition);
			self.log("ln2cryostage move complete.")
		else:
			sampleNumberA = bean.getSampleNumberA()
			sampleNumberB = bean.getSampleNumberB()
			cylinderType = bean .getCylinderType()
			height = bean.getCalibHeight() + (sampleNumberA - 1) * 17.0
			angleOffset = bean.getCalibAngle()
			if cylinderType == "trans":
				print "moving ln2 cryo transmission to ", sampleNumberA, ", ", sampleNumberB
				angle = angleOffset + ((sampleNumberB - 1) * 16.36)
			elif cylinderType == "fluo":
				print "moving ln2 cryo fluoresence to ", sampleNumberA, ", ", sampleNumberB
				if sampleNumberB < 5:
					angle = angleOffset + ((sampleNumberB - 1) * 22.5)
				else:
					angle = angleOffset + 180.0 + ((sampleNumberB - 5) * 22.5)
			targetPosition = [height, angle]
			print "Target positions = ", targetPosition
			self.ln2cryo_scannable(targetPosition);

	def _control_sample_wheel(self, bean):
		manual = bean.isManual()
		if manual:
			demand = bean.getDemand()
			self.log("moving sample wheel to " + str(demand))
			self.samplewheel_scannable(demand)
		else:
			filter = bean.getFilter()
			self.log("moving sample wheel to " + str(filter))
			self.samplewheel_scannable.moveToFilter(filter)
			self.samplewheel_scannable.waitWhileBusy()
		print "sample wheel move complete"
