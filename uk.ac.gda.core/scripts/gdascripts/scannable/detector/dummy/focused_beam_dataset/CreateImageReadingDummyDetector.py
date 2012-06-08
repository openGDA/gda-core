from gdascripts.scannable.detector.dummy.ImageReadingDummyDetector import ImageReadingDummyDetector
COLLECTIONFOLDER = __file__.split("CreateImageReadingDummyDetector.py")[0] + '/ippimages/2009-6-25'
print COLLECTIONFOLDER
# Data from b16 taken on Friday 25th June 2009
# tboptZcoarse hadded a 75 element beam focusing lens mounted on it, and
# tbdetZcoarse PhotonicScience xray-eye camera read via Image Pro Plus

def create(idxscannable):
# >>>scan tbdetZcoarse 410 600 20 ipp 20 proi_max proi_peak tboptZcoarse
# Writing data to file:12883.dat
	scan12883 = {
			410.00 : 'ipp316.TIF',
			430.00 : 'ipp317.TIF',
			450.00 : 'ipp318.TIF',
			470.00 : 'ipp319.TIF',
			490.00 : 'ipp320.TIF',
			510.00 : 'ipp321.TIF',
			530.00 : 'ipp322.TIF',
			550.00 : 'ipp323.TIF',
			570.00 : 'ipp324.TIF',
			590.00 : 'ipp325.TIF',
			}
	
	scan12883bodged = scan12883.copy()
	del scan12883bodged[410.]

# go minval #490
# >> dscan tbdetZcoarse -20 20 16 ipp 20 proi_max proi_peak tboptZcoarse #12875
# Writing data to file:12875.dat	
	scan12875 = {
			470.00 : 'ipp192.TIF',
			472.50 : 'ipp193.TIF',
			475.00 : 'ipp194.TIF',
			477.50 : 'ipp195.TIF',
			480.00 : 'ipp196.TIF',
			482.50 : 'ipp197.TIF',
			485.00 : 'ipp198.TIF',
			487.50 : 'ipp199.TIF',
			490.00 : 'ipp200.TIF',
			492.50 : 'ipp201.TIF',
			495.00 : 'ipp202.TIF',
			497.50 : 'ipp203.TIF',
			500.00 : 'ipp204.TIF',
			502.50 : 'ipp205.TIF',
			505.00 : 'ipp206.TIF',
			507.50 : 'ipp207.TIF',
			510.00 : 'ipp208.TIF',			
				}	

			
	d = {}

	d.update(scan12883bodged)
	d.update(scan12875)
	s="  scan x 430 600 20 ipp 20 \n"
	s+="  pos x 490 #then:\n"
	s+="  rscan x -20 20 2.5 ipp 20"
	
	return ImageReadingDummyDetector( 'ippws4', idxscannable, COLLECTIONFOLDER, d, s)
