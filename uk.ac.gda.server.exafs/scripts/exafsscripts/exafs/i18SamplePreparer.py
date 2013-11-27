from gdascripts.messages.handle_messages import simpleLog
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter, NexusFileMetadata
from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
from time import sleep

class I18SamplePreparer:
	def __init__(self, rcpController, sc_MicroFocusSampleX, sc_MicroFocusSampleY, sc_sample_z, D7A, D7B, kb_vfm_x):
		self.logging_enabled = True
		self.rcpController = rcpController
		self.sc_MicroFocusSampleX = sc_MicroFocusSampleX
		self.sc_MicroFocusSampleY = sc_MicroFocusSampleY
		self.sc_sample_z = sc_sample_z
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
	
	def prepare(self, sampleParameters):
		
		self.rcpController.openPerspective("org.diamond.exafs.ui.PlottingPerspective")

		stage = sampleParameters.getSampleStageParameters()
		self.log("Moving sample stage to [" + str(stage.getX()) + ", " + str(stage.getY()) + ", " + str(stage.getZ()) + "]...")
		self.sc_MicroFocusSampleX(stage.getX())
		self.sc_MicroFocusSampleY(stage.getY())
		self.sc_sample_z(stage.getZ())

		att1 = sampleParameters.getAttenuatorParameter1()
		att2 = sampleParameters.getAttenuatorParameter2()
		self.log("Moving attenuators to [" + str(att1.getSelectedPosition()) + ", " + str(att2.getSelectedPosition()) + "]...")
		self.D7A(att1.getSelectedPosition())
		self.D7B(att2.getSelectedPosition())
		
		if sampleParameters.isVfmxActive():
			self.log( "Moving kb_vfm_x to " + str( sampleParameters.getVfmx()) )
			self.kb_vfm_x(sampleParameters.getVfmx())