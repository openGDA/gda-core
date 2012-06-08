diffcalc_path = gda.data.PathConstructor.createFromProperty("gda.root").split("/plugins")[0] + "/diffcalc/"
import sys
sys.path = [diffcalc_path] + sys.path
execfile(diffcalc_path + "/example/startup/sixcircle_with_scannables.py")
