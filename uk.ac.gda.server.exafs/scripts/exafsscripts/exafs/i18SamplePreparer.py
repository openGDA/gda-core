from gdascripts.messages.handle_messages import simpleLog
from time import sleep

class I18SamplePreparer:
	def __init__(self, rcpController, D7A, D7B, kb_vfm_x):
		self.logging_enabled = True
		self.rcpController = rcpController
		self.D7A = D7A
		self.D7B = D7B
		self.kb_vfm_x = kb_vfm_x
	
	def setLoggingEnabled(self, enabled):
		self.logging_enabled = enabled
	
	def log(self, msg):
		if self.logging_enabled == True:
			simpleLog(msg)
		else:
			print msg
			
	def setStageScannables(self,stage1_x,stage1_y,stage1_z,stage3_x,stage3_y,stage3_z):
		self.stage1_x = stage1_x
		self.stage1_y = stage1_y
		self.stage1_z = stage1_z
		self.stage3_x = stage3_x
		self.stage3_y = stage3_y
		self.stage3_z = stage3_z
		
		self.stage_x = stage1_x
		self.stage_y = stage1_y
		self.stage_z = stage1_z

	def setStage(self, stage):
		if stage==1:
			self.stage_x = self.stage1_x
			self.stage_y = self.stage1_y
			self.stage_z = self.stage1_z
		elif stage==3:
			self.stage_x = self.stage3_x
			self.stage_y = self.stage3_y
			self.stage_z = self.stage3_z
		else:
			print "please enter 1 or 3 as a parameter where 1 is the small stage and 3 is the large stage"

	
	def prepare(self, sampleParameters):
		
		self.rcpController.openPerspective("org.diamond.exafs.ui.PlottingPerspective")

		stage = sampleParameters.getSampleStageParameters()
		self.log( "Moving stage x to:" + str(stage.getX()))
		self.stage_x(stage.getX())
		self.log( "Moving stage y to:" + str(stage.getY()))
		self.stage_y(stage.getY())
		self.log( "Moving stage z to:" + str(stage.getZ()))
		self.stage_z(stage.getZ())

		att1 = sampleParameters.getAttenuatorParameter1()
		att2 = sampleParameters.getAttenuatorParameter2()
		
		self.log( "Moving D7A to:" + att1.getSelectedPosition())
		self.D7A(att1.getSelectedPosition())
		self.log( "Moving D7B to:" + att2.getSelectedPosition())
		self.D7B(att2.getSelectedPosition())
		
		if sampleParameters.isVfmxActive():
			self.log( "Moving kb_vfm_x to:" + str(sampleParameters.getVfmx()))
			self.kb_vfm_x(sampleParameters.getVfmx())