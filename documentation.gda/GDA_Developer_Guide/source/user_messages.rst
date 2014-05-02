==============
 User Messages
==============

The default behaviour is to save, in memory, the last 10 messages sent for each visit.

You can have the messages saved to a file for each visit by plugging in a different ``messageHandler`` to the ``JythonServer`` object:

.. code-block:: none
   :linenos:
   
   <bean id="..." class="gda.jython.JythonServer">
       
       ...
       
       <property name="messageHandler">
           <bean class="gda.messages.FileMessageHandler">
               <property name="userMessagesDirectory" value="${gda.var}/user_messages" />
           </bean>
       </property>
   </bean>

(In RCP GUIs, messages are saved to disk by the server, not by each client.)

When the **Messages** view is opened, it will display the message history (retrieved from the server).
