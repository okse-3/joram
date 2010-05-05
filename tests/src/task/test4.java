/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import joram.framework.TestCase;
import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.task.*;
import com.scalagent.task.composed.*;
import com.scalagent.task.util.*;

/**
 * Main to test the <code>Sequential<code> class.
 *
 * @see		Sequential
 */
public class test4 extends TestCase {
  public test4() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test4Agent test = new Test4Agent();
    String cp = System.getProperty("java.class.path");

    Sequential parent = new Sequential((short) 0, null, null);
    parent.addStatusListener(test.getId());
    test.parent = parent.getId();
    AgentId child[] = new AgentId[3];

    JavaMain task1 = new JavaMain((short) 0,
                                  parent.getId(), "task.Hello 0", cp);
//  Program task1 = new Program((short) 0, parent.getId(), "sh ./hello.sh 0");
    task1.addStatusListener(test.getId());
    task1.deploy();
    test.task1 = task1.getId();
    child[0] = task1.getId();

    JavaMain task2 = new JavaMain((short) 0,
                                  parent.getId(), "task.Hello 1", cp);
//  Program task2 = new Program((short) 0, parent.getId(), "sh ./hello.sh 1");
    task2.addStatusListener(test.getId());
    task2.deploy();
    test.task2 = task2.getId();
    child[1] = task2.getId();

    JavaMain task3 = new JavaMain((short) 0,
                                  parent.getId(), "task.Hello 2", cp);
//  Program task3 = new Program((short) 0, parent.getId(), "sh ./hello.sh 2");
    task3.addStatusListener(test.getId());
    task3.deploy();
    test.task3 = task3.getId();
    child[2] = task3.getId();

    parent.setChild(child);
    parent.deploy();
    Channel.sendTo(parent.getId(), new ResetNotification());

    test.deploy();
  }

  public static void main(String args[]) {
    new test4().runTest(args);
  }
}

class Test4Agent extends Agent {
  AgentId parent, task1, task2, task3;
  int state = 0;

  public Test4Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    TestCase.assertEquals("step#" + state, 
                          "com.scalagent.task.StatusNotification",
                          not.getClass().getName());
    if (state == 0) {
      // StatusNotification,parent,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 1) {
      // StatusNotification,task3,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task3, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 2) {
      // StatusNotification,task2,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 3) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 4) {
      // StatusNotification,parent,status=INIT,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.INIT);
      }
    } else if (state == 5) {
      // StatusNotification,task1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 6) {
      // StatusNotification,parent,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 7) {
      // StatusNotification,task1,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 8) {
      // StatusNotification,task2,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 9) {
      // StatusNotification,task2,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 10) {
      // StatusNotification,task3,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task3, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 11) {
      // StatusNotification,task3,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task3, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 12) {
      // StatusNotification,parent,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
      TestCase.endTest();
    }

    if (not instanceof StatusNotification) {
      StatusNotification status = (StatusNotification) not;
      TestCase.assertNull("step#" + state, status.getMessage());
      TestCase.assertNull("step#" + state, status.getResult());
    }

//  System.out.println("step#" + state + "->" + not);
    state += 1;
  }
}
