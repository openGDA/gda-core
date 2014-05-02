================================
 Writing & Running GDA Test Code
================================
 
This document contains guidance of how to write unit and integration tests.

Add your new test class/case to its test fragment project if it already exits, following the same package structure as the class to be tested;

If test fragment doesn't exist yet, please create a new test fragment project and select the plugin containing class to be tested as Host, add plugin dependency to "org.junit4" and "uk.ac.gda.test.helper", then create your new test class in the same package structure as the class to be tested;

A test fragment project must and can only test the codes in its host plugin.

If you want to create a test that depends on more plugins than the host plugin then put the test in a test plugin rather than a fragment

A test fragment project should only depend on org.junit4, uk.ac.gda.test.helper, and any third party library or plugin, and must not depend on any other GDA plugin projects.

Please do not dump every test cases in uk.ac.gda.test.helper plugin without very careful consideration. We should aim to keep this plugin as small as possible by breaking down help utility into small pieces to justify its existence in this plugin and to identify the real test fragment every piece is best belong to.

GDA uses JUNIT4 and mockito to develop test cases. A good comparision of the classic JUNIT and Mock objects methods of testing is given in an article Mocks Aren't Stubs by Martin Fowler which is a good read.

All test cases in the 'test' folder in each plugin is moved to the 'src' folder of a fragment project named with '.test' appending to the original plugin name. The original plugin is set as the host plugin to this fragment project, so that the test classes can access all the methods in the classes to be tested.

All the test data in 'testfiles' folder in each plugin is also moved with the test classes into their test fragments, respectively.

Test classes that shared by more than one test fragments - such as, the helper and utility classes are in plugin "uk.ac.gda.test.helpers", so that individual fragments can depend on it. Everything in ""uk.ac.gda.test.helpers" are exported.
"uk.ac.gda.test.helper" currently have to dependency on core, common, nexus, libs, common.client, swingclient, plus other 3rd party software.

In order to develop and run jython unit test cases within eclipse IDE using scisoftpy, you must set the PYTHONPATH of the projects and/or target platform correctly so your tested scripts have access to the libraries available from there.
1. enable your Target Platform tp as PyDev project;
2. run your jython unit test case within eclipse IDE;
3. if you have class not found error, find out where the missing class is and add its PATH to its containing plugin's PYTHONPATH via context-sensitive 'properties' menu.

Alternatively, you can leave tp alone, modify variable ${tpp_loc} in the PYTHONPATH of uk.ac.diamond.scisoft.python plugin to point to the current active target platform. Because it is PyDev, you have to use the absolute path for the target platform plugins, which means it have to be changed when your checkout the project to a different location each time.
