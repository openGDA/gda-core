config=LocalParameters.getXMLConfiguration()
config.setProperty("test",10)
config.save()
config.getInt("test")