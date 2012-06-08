'''
Created on 1 Jul 2009

@author: mtq65916
'''

def square(x):
    return x * x

# http://effbot.org/pyfaq/tutor-what-is-if-name-main-for.htm
if __name__ == '__main__':
    print "test: square(42) ==", square(42)
