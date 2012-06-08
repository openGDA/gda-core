#
# Demo of the LogginScriptController
#
#

from gda.example.scriptcontroller.logging import ExampleLoggingMessage
from gda.example.scriptcontroller.logging import OtherExampleLoggingMessage
from gda.jython.scriptcontroller.logging import LoggingScriptController
from time import sleep

scriptName          = "My data collection script"
percentComplete     = "0%"
progress            = "Haven't started yet"
sampleTemperature   = "20.0C"
controller          = finder.find("MyLoggingScriptController")
unique_id           = LoggingScriptController.createUniqueID(scriptName); # unique for this use of this script

msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)

progress = "Setting up equipment..."
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(2)

progress = "Starting data collection..."
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected 1/5 points."
percentComplete="20%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected 2/5 points."
percentComplete="40%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected 3/5 points."
percentComplete="60%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected 4/5 points."
percentComplete="80%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected all points."
percentComplete="100%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(1)

progress = "Completed data collection successfully."
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)






scriptName          = "Other collection script"
percentComplete     = "0%"
progress            = "Haven't started yet"
sampleTemperature   = "20.0C"
controller          = finder.find("MyLoggingScriptController")
unique_id           = LoggingScriptController.createUniqueID(scriptName); # unique for this use of this script

msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)

progress = "Setting up equipment..."
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(2)

progress = "Starting data collection..."
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected 1/5 points."
percentComplete="20%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected 2/5 points."
percentComplete="40%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected 3/5 points."
percentComplete="60%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected 4/5 points."
percentComplete="80%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(3)

progress = "Collected all points."
percentComplete="100%"
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)
sleep(1)

progress = "Completed data collection successfully."
print progress
msg = ExampleLoggingMessage(unique_id,scriptName,progress,percentComplete,sampleTemperature)
controller.update(None,msg)


print "Third script, using a different script controller"
scriptName          = "Third collection script"
progress            = "Haven't started yet"
controller          = finder.find("MyOtherLoggingScriptController")
unique_id           = LoggingScriptController.createUniqueID(scriptName); # unique for this use of this script

msg = OtherExampleLoggingMessage(unique_id,scriptName,progress)
controller.update(None,msg)

progress = "Setting up equipment..."
print progress
msg = OtherExampleLoggingMessage(unique_id,scriptName,progress)
controller.update(None,msg)
sleep(2)

progress = "Starting data collection..."
print progress
msg = OtherExampleLoggingMessage(unique_id,scriptName,progress)
controller.update(None,msg)
sleep(3)

progress = "Collected all points."
print progress
msg = OtherExampleLoggingMessage(unique_id,scriptName,progress)
controller.update(None,msg)
sleep(1)

progress = "Completed data collection successfully."
print progress
msg = OtherExampleLoggingMessage(unique_id,scriptName,progress)
controller.update(None,msg)




