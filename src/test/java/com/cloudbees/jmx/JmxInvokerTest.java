/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudbees.jmx;

import com.cloudbees.jmx.model.MockMemoryPool;
import com.cloudbees.jmx.model.MockSimpleJmxBean;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineParser;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class JmxInvokerTest {

    static MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    static ObjectName mockEdenSpacePoolObjectName;
    static MockSimpleJmxBean mockSimpleJmxBean = new MockSimpleJmxBean();
    static ObjectName mockSimpleBeanObjectName;

    @BeforeClass
    public static void beforeClass() throws Exception {
        mockEdenSpacePoolObjectName = new ObjectName("test:type=MemoryPool,name=PS Eden Space");
        mbeanServer.registerMBean(new MockMemoryPool("PS Eden Space", 87359488L), mockEdenSpacePoolObjectName);
        mockSimpleBeanObjectName = new ObjectName("test:type=SimpleJmxBean");
        mbeanServer.registerMBean(mockSimpleJmxBean, mockSimpleBeanObjectName);

    }

    @AfterClass
    public static void afterClass() throws Exception {
        mbeanServer.unregisterMBean(mockEdenSpacePoolObjectName);
        mbeanServer.unregisterMBean(mockSimpleBeanObjectName);
    }

    @Test
    public void testInvokeOperation() throws Exception {
        JmxInvoker jmxInvoker = new JmxInvoker();
        // String arg1, boolean arg2, Boolean arg3, Integer arg4, int arg5
        Object actual = jmxInvoker.invokeOperation(mbeanServer, mockSimpleBeanObjectName, "anOperation", "str1", "false", "true", "1", "2");
        System.out.println(actual);
    }

    @Test
    public void testSetStringAttribute() throws Exception {
        JmxInvoker jmxInvoker = new JmxInvoker();
        mockSimpleJmxBean.setZeAttribute("aValue");
        jmxInvoker.invokeAttribute(mbeanServer, mockSimpleBeanObjectName, "ZeAttribute", "zeValueForTest");
        Assert.assertEquals(mockSimpleJmxBean.getZeAttribute(), "zeValueForTest");
    }

    @Test
    public void testSetBoolAttribute() throws Exception {
        JmxInvoker jmxInvoker = new JmxInvoker();
        mockSimpleJmxBean.setBoolAttribute(false);
        jmxInvoker.invokeAttribute(mbeanServer, mockSimpleBeanObjectName, "BoolAttribute", "true");
        Assert.assertEquals(mockSimpleJmxBean.isBoolAttribute(), true);
    }

    @Test
    public void testSetBooleanAttribute() throws Exception {
        JmxInvoker jmxInvoker = new JmxInvoker();
        mockSimpleJmxBean.setBooleanAttribute(Boolean.FALSE);
        jmxInvoker.invokeAttribute(mbeanServer, mockSimpleBeanObjectName, "BooleanAttribute", "true");
        Assert.assertEquals(mockSimpleJmxBean.getBooleanAttribute(), Boolean.TRUE);
    }

    @Test
    public void testSetIntAttribute() throws Exception {
        JmxInvoker jmxInvoker = new JmxInvoker();
        mockSimpleJmxBean.setIntAttribute(-1);
        jmxInvoker.invokeAttribute(mbeanServer, mockSimpleBeanObjectName, "IntAttribute", "3");
        Assert.assertEquals(mockSimpleJmxBean.getIntAttribute(), 3);
    }

    @Test
    public void testSetIntegerAttribute() throws Exception {
        JmxInvoker jmxInvoker = new JmxInvoker();
        mockSimpleJmxBean.setIntegerAttribute(-1);
        jmxInvoker.invokeAttribute(mbeanServer, mockSimpleBeanObjectName, "IntegerAttribute", "3");
        Assert.assertEquals(mockSimpleJmxBean.getIntegerAttribute(), new Integer(3));
    }

    @Test
    public void testGetStringAttribute() throws Exception {
        JmxInvoker jmxInvoker = new JmxInvoker();
        mockSimpleJmxBean.setZeAttribute("aValueToRead");
        JmxInvoker.Result actual = jmxInvoker.invokeAttribute(mbeanServer, mockSimpleBeanObjectName, "ZeAttribute", null);
        Assert.assertEquals(actual.value, "aValueToRead");
    }

    @Test
    public void testMainInvokeOperation() throws Exception {
        String[] args = {"-v", "-p", "12345", "-on", mockSimpleBeanObjectName.toString(), "-op", "anOperation", "str1", "false", "true", "1", "2"};
        JmxInvoker.JmxInvokerArguments arguments = new JmxInvoker.JmxInvokerArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        parser.parseArgument(args);

        JmxInvoker jmxInvoker = new JmxInvoker() {
            @Override
            protected MBeanServerConnection connectToMbeanServer(String pid) throws IOException {
                return mbeanServer;
            }
        };
        jmxInvoker.process(arguments);
    }

    @Test
    public void testMainListMbeans() throws Exception {
        String[] args = {"-v", "-p", "12345", "-on", mockSimpleBeanObjectName.getDomain() + ":*", "-l",};
        JmxInvoker.JmxInvokerArguments arguments = new JmxInvoker.JmxInvokerArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        parser.parseArgument(args);

        JmxInvoker jmxInvoker = new JmxInvoker() {
            @Override
            protected MBeanServerConnection connectToMbeanServer(String pid) throws IOException {
                return mbeanServer;
            }
        };
        Map<ObjectName, JmxInvoker.Result> results = jmxInvoker.process(arguments);
        Assert.assertEquals(2, results.size());
        System.out.println(results);
    }

    @Test
    public void testMainDescribeMbean() throws Exception {
        String[] args = {"-v", "-p", "12345", "-on", mockSimpleBeanObjectName.toString(), "-d",};
        JmxInvoker.JmxInvokerArguments arguments = new JmxInvoker.JmxInvokerArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        parser.parseArgument(args);

        JmxInvoker jmxInvoker = new JmxInvoker() {
            @Override
            protected MBeanServerConnection connectToMbeanServer(String pid) throws IOException {
                return mbeanServer;
            }
        };
        Map<ObjectName, JmxInvoker.Result> results = jmxInvoker.process(arguments);
        Assert.assertEquals(1, results.size());

        System.out.println(results);
    }

    @Test
    public void testMainParseDoubleQuote() throws Exception {
        String[] args = {"-v", "-p", "12345", "-on", "Catalina:type=DataSource,class=javax.sql.DataSource,name=\"jdbc/petclinic\"", "-attr", "numActive"};
        JmxInvoker.JmxInvokerArguments arguments = new JmxInvoker.JmxInvokerArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        parser.parseArgument(args);


        System.out.println(arguments.objectName);
    }
}
