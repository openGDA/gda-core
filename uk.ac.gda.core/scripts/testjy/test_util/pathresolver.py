'''
Created on 8 Oct 2009

@author: fy65
'''
import java
def replacePathSeparater(path):
    '''resolve the platform dependent path separater in file path''' 
    platform = java.lang.System.getProperty("os.name")
    if platform.startswith("Windows"):
        newpath = path.replace('\\', '/')
    elif platform.startswith("Linux"):
        newpath = path
    return newpath