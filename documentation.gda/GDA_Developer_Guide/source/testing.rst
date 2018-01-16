================================
 Writing & Running GDA Test Code
================================
 
This document contains guidance on how to write unit and integration tests.

Add your new test class/case to its test fragment project if it already exits, following the same package structure as the class to be tested.

If the test fragment doesn't exist yet, please create a new test fragment project and select the plugin containing the class to be tested as Host, add a plugin dependency to "org.junit4" and "uk.ac.gda.test.helpers", then create your new test class in the same package structure as the class to be tested.

A test fragment project must and can only test the code in its host plugin.

If you want to create a test that depends on more plugins than the host plugin then put the test in a test plugin rather than a fragment.

A test fragment project should only depend on org.junit4, uk.ac.gda.test.helpers, and any third party library or plugin, and must not depend on any other GDA plugin projects.

Please do not put things in the uk.ac.gda.test.helpers plugin without very careful consideration. We should aim to keep this plugin as small as possible by breaking down helper utilities into small pieces to justify their existence in this plugin and to identify the real test fragment every piece best belongs to.

GDA uses JUNIT4 and mockito to develop test cases. A good comparison of the classic JUNIT and Mock objects methods of testing is given in an article Mocks Aren't Stubs by Martin Fowler which is a good read.

All test cases in the 'test' folder in each plugin are moved to the 'src' folder of a fragment project named with '.test' appended to the original plugin name. The original plugin is set as the host plugin to this fragment project so that the test classes can access all the methods in the classes to be tested.

All the test data in the 'testfiles' folder in each plugin is also moved with the test classes into their test fragments, respectively.

Test classes that are shared by more than one test fragment, such as the helper and utility classes, are in the plugin "uk.ac.gda.test.helpers" so that individual fragments can depend on it. Everything in "uk.ac.gda.test.helpers" is exported.
"uk.ac.gda.test.helpers" currently has dependencies on core, common, nexus, libs, common.client, swingclient, plus other 3rd party software.

In order to develop and run jython unit test cases within the Eclipse IDE using scisoftpy, you must set the PYTHONPATH of the projects and/or target platform correctly so your tested scripts have access to the libraries available from there:

1. Enable your Target Platform tp as a PyDev project.
2. Run your jython unit test case within the Eclipse IDE.
3. If you get a class-not-found error, find out where the missing class is and add its PATH to its containing plugin's PYTHONPATH via the context-sensitive 'properties' menu.

Alternatively, you can leave tp alone and modify the variable ${tpp_loc} in the PYTHONPATH of the uk.ac.diamond.scisoft.python plugin to point to the current active target platform. Because it is PyDev, you have to use the absolute path for the target platform plugins which means you will have to change it if you checkout the project to a different location.
