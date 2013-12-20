import time
from gda.factory import Finder 


class ExperimentParameters():
    
    def __init__(self,param1,param2):
        self.param1 = param1
        self.param2 = param2
        
    def __str__(self):
        return "Param1: " + str(self.param1) + " Param2: " +  str(self.param2)

class MyExperiment():
    
    def __init__(self,beanDefiningExperiment):
        self.bean = beanDefiningExperiment
        
    def runExperiment(self):
        print "I am now running the experiment using parameters in the bean",str(self.bean),"..."
        time.sleep(2)
        print "...and now the experiment is over."
        scriptController = Finder.getInstance().find("MyExperimentScriptController")
        if scriptController != None:
            print "Broadcasting experiment complete message..."
            scriptController.update("MyExperiment Script", "Experiment Complete!")
            
            