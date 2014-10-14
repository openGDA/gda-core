def getDataSetFromSFH(sfh, fieldname):
    sfhAxesNames = tuple(sfh.getHeadings())
    
    if fieldname in sfhAxesNames:
        return sfh.getDataSet(fieldname)
    
    if '.' in fieldname:
        # with scnname.fieldname strip off scnname.
        shortFieldname = fieldname.split('.')[1]
        if shortFieldname in sfhAxesNames:
            return sfh.getDataSet(shortFieldname)
    
    raise KeyError("Neither %s or %s in %s" % (fieldname, shortFieldname, ', '.join(sfhAxesNames)))