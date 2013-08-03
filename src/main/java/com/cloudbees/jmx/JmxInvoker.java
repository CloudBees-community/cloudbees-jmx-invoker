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

import com.cloudbees.util.Strings2;
import com.cloudbees.util.nio.Files2;
import com.sun.tools.attach.VirtualMachine;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ObjectNameOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Invoke arbitrary JMX operation or attribute connecting to the target local JVM thanks to the
 * <a href="http://docs.oracle.com/javase/6/docs/jdk/api/attach/spec/index.html">Attach API</a>
 * (see <a href="https://blogs.oracle.com/CoreJavaTechTips/entry/the_attach_api">The Attach API</a>).
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class JmxInvoker {
    public static final String NULL_VALUE = "##NULL##";
    private Logger logger = LoggerFactory.getLogger(JmxInvoker.class);

    public static void main(String[] args) throws Exception {

        long timeBeforeInMillis = System.currentTimeMillis();

        JmxInvokerArguments arguments = new JmxInvokerArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        try {
            parser.parseArgument(args);
            arguments.cmdLineParser = parser;
            if (Strings2.isEmpty(arguments.pid) && arguments.pidFile == null) {
                throw new CmdLineException(parser, "Options --pid and --pid-file can NOT be both null");
            } else if (!Strings2.isEmpty(arguments.pid) && arguments.pidFile != null) {
                throw new CmdLineException(parser, "Options --pid and --pid-file can NOT be both defined");
            } else if (
                    (arguments.attribute == null || arguments.attribute.length == 0) &&
                            (arguments.operation == null || arguments.operation.length == 0) &&
                            arguments.listMbeans == false && arguments.describeMbeans == false) {
                throw new CmdLineException(parser, "Option --attribute or --operation or --list-mbeans or --describe-mbeans must be defined");
            } else if (
                    (arguments.attribute != null && arguments.attribute.length > 0) &&
                            (arguments.operation != null && arguments.operation.length > 0)) {
                throw new CmdLineException(parser, "Options --attribute and --operation can NOT be both defined");
            }


            String logLevel;
            if (arguments.superVerbose) {
                logLevel = "TRACE";
            } else if (arguments.verbose) {
                logLevel = "DEBUG";
            } else {
                logLevel = "WARN";
            }
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, logLevel);

            Map<ObjectName, Result> results = new JmxInvoker().process(arguments);
            for (Map.Entry<ObjectName, Result> entry : results.entrySet()) {
                System.out.println(entry.getValue().description);
            }
        } catch (CmdLineException e) {
            System.err.println("INVALID INVOCATION: " + e.getMessage());
            System.err.println("Arguments: " + Strings2.join(args, " "));
            System.err.println("Usage:");
            parser.printUsage(System.err);
            throw e;
        } catch (Exception e) {
            System.err.println("INVALID INVOCATION: " + e.getMessage());
            System.err.println("Arguments: " + Strings2.join(args, " "));
            e.printStackTrace();
            throw e;
        } finally {
            if(arguments.verbose ||arguments.superVerbose) {
                System.out.println("Duration: " + (System.currentTimeMillis() - timeBeforeInMillis) + "ms");
            }
        }
    }

    private static void setSystemPropertyIfNotDefined(String systemPropertyName, String value) {
        if (!System.getProperties().contains(systemPropertyName))
            System.setProperty(systemPropertyName, value);
    }

    public Map<ObjectName, Result> process(JmxInvokerArguments arguments) throws IOException {

        String pid = (arguments.pid == null) ? Files2.readFile(arguments.pidFile, "US-ASCII") : arguments.pid;
        pid = pid.replace("\n", "").trim();

        ObjectName on = arguments.objectName;

        String[] op = arguments.operation;

        String operationName = op == null || op.length == 0 ? null : op[0];
        String[] operationArguments = op == null || op.length < 2 ? new String[0] : Arrays.copyOfRange(op, 1, op.length);

        String[] attr = arguments.attribute;
        String attributeName = attr == null || attr.length == 0 ? null : attr[0];
        String attributeValue = attr == null || attr.length < 2 ? null : attr[1];

        MBeanServerConnection mbeanServer = connectToMbeanServer(pid);

        Map<ObjectName, Result> results = new TreeMap<ObjectName, Result>();

        Set<ObjectName> objectNames = mbeanServer.queryNames(on, null);
        if (objectNames.isEmpty()) {
            logger.warn("No mbean found for ObjectName {}", on);
        }

        for (ObjectName objectName : objectNames) {
            Result result;
            try {
                if (operationName != null) {
                    result = invokeOperation(mbeanServer, objectName, operationName, operationArguments);
                } else if (attributeName != null) {
                    result = invokeAttribute(mbeanServer, objectName, attributeName, attributeValue);
                } else if (arguments.describeMbeans) {
                    result = describeMbean(mbeanServer, objectName);
                } else if (arguments.listMbeans) {
                    result = new Result(objectName, objectName.toString(), objectName.toString());
                } else {
                    throw new CmdLineException(arguments.cmdLineParser, "NO SEARCH_MBEANS OR OPERATION OR ATTRIBUTE DEFINED");
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String msg = "## EXCEPTION ##\n" + sw.toString();
                result = new Result(objectName, msg, msg);
            }

            results.put(objectName, result);

        }

        logger.info("INVOCATION RESULT");
        logger.info("#################");
        logger.info("JVM pid: {}", pid);
        logger.info("Searched object-name: {}", on);
        if (operationName != null) {
            logger.info("Invoke operation {}{}", operationName, Arrays.asList(operationArguments));
        } else if (attributeValue == null) {
            logger.info("Get attribute {}", attributeName);
        } else {
            logger.info("Set attribute {}: {}", attributeName, attributeValue);
        }
        for (Map.Entry<ObjectName, Result> entry : results.entrySet()) {
            logger.info("{}", entry.getKey());
            logger.info("\t{}", entry.getValue());
        }

        return results;

    }

    protected Result describeMbean(@Nonnull MBeanServerConnection mbeanServer, @Nonnull ObjectName objectName) throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        out.println("# MBEAN");
        out.println(objectName.toString());
        out.println();
        out.println("## OPERATIONS");
        List<MBeanOperationInfo> operations = Arrays.asList(mbeanInfo.getOperations());
        Collections.sort(operations, new Comparator<MBeanOperationInfo>() {
            @Override
            public int compare(MBeanOperationInfo o1, MBeanOperationInfo o2) {
                return o1.getName().compareTo(o1.getName());
            }
        });

        for (MBeanOperationInfo opInfo : operations) {
            out.print("* " + opInfo.getName() + "(");
            MBeanParameterInfo[] signature = opInfo.getSignature();
            for (int i = 0; i < signature.length; i++) {
                MBeanParameterInfo paramInfo = signature[i];
                out.print(paramInfo.getType() + " " + paramInfo.getName());
                if (i < signature.length - 1) {
                    out.print(", ");
                }
            }

            out.print("):" + opInfo.getReturnType() /* + " - " + opInfo.getDescription() */);
            out.println();
        }
        out.println();
        out.println("## ATTRIBUTES");
        List<MBeanAttributeInfo> attributes = Arrays.asList(mbeanInfo.getAttributes());
        Collections.sort(attributes, new Comparator<MBeanAttributeInfo>() {
            @Override
            public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (MBeanAttributeInfo attrInfo : attributes) {
            out.println("* " + attrInfo.getName() + ": " + attrInfo.getType() + " - " +
                    (attrInfo.isReadable() ? "r" : "") + (attrInfo.isWritable() ? "w" : "") /* + " - " +
                    attrInfo.getDescription() */);
        }

        String description = sw.getBuffer().toString();
        return new Result(objectName, description, description);
    }

    /**
     * @param mbeanServer
     * @param objectName
     * @param attributeName
     * @param attributeValue if <code>null</code>, this is a read access.
     * @return if this is a read access, the returned value (may be null); if it is a write access, return <code>void.class</code>.
     * @throws IOException
     * @throws JMException
     */
    public Result invokeAttribute(@Nonnull MBeanServerConnection mbeanServer, @Nonnull ObjectName objectName, @Nonnull String attributeName, @Nullable String attributeValue) throws IOException, JMException {
        MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);
        MBeanAttributeInfo attributeInfo = null;
        for (MBeanAttributeInfo mai : mbeanInfo.getAttributes()) {
            if (mai.getName().equals(attributeName)) {
                attributeInfo = mai;
                break;
            }
        }
        if (attributeInfo == null) {
            throw new IllegalArgumentException("No attribute '" + attributeName + "' found on '" + objectName + "'. Existing attributes: " + Arrays.asList(mbeanInfo.getAttributes()));
        }

        String description;
        Object resultValue;

        if (attributeValue == null) {
            if (attributeInfo.isReadable()) {
                Object attribute = mbeanServer.getAttribute(objectName, attributeName);
                logger.info("get attribute value {}:{}:{}", objectName, attributeName, attribute);
                resultValue = attribute;
                description = "Get attribute value " + objectName + ":" + attributeName + ": " + resultValue;
            } else {
                throw new IllegalArgumentException("Attribute '" + attributeName + "' is not readable on '" + objectName + "': " + attributeInfo);
            }
        } else {
            if (attributeInfo.isWritable()) {
                Object value = convertValue(attributeValue, attributeInfo.getType());
                mbeanServer.setAttribute(objectName, new Attribute(attributeName, value));
                logger.info("set attribute value {}:{}:{}", objectName, attributeName, value);

                description = "Set attribute value " + objectName + ":" + attributeName + ": " + value;
                resultValue = void.class;
            } else {
                throw new IllegalArgumentException("Attribute '" + attributeName + "' is not writable on '" + objectName + "': " + attributeInfo);
            }
        }

        return new Result(objectName, resultValue, description);
    }

    @Nullable
    public Result invokeOperation(@Nonnull MBeanServerConnection mBeanServer, @Nonnull ObjectName on, @Nonnull String operationName, @Nonnull String... arguments) throws JMException, IOException {

        logger.debug("invokeOperation({},{}, {}, {})...", on, operationName, Arrays.asList(arguments));
        MBeanInfo mbeanInfo = mBeanServer.getMBeanInfo(on);

        List<MBeanOperationInfo> candidates = new ArrayList<MBeanOperationInfo>();
        for (MBeanOperationInfo mbeanOperationInfo : mbeanInfo.getOperations()) {
            if (mbeanOperationInfo.getName().equals(operationName) && mbeanOperationInfo.getSignature().length == arguments.length) {
                candidates.add(mbeanOperationInfo);
                logger.debug("Select matching operation {}", mbeanOperationInfo);
            } else {
                logger.trace("Ignore non matching operation {}", mbeanOperationInfo);
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Operation '" + operationName + "(" + Strings2.join(arguments, ", ") + ")' NOT found on " + on);
        } else if (candidates.size() > 1) {
            throw new IllegalArgumentException("More than 1 (" + candidates.size() + ") operation '" + operationName + "(" + Strings2.join(arguments, ", ") +
                    ")' found on '" + on + "': " + candidates);
        }
        MBeanOperationInfo beanOperationInfo = candidates.get(0);

        MBeanParameterInfo[] mbeanParameterInfos = beanOperationInfo.getSignature();

        List<String> signature = new ArrayList<String>();
        for (MBeanParameterInfo mbeanParameterInfo : mbeanParameterInfos) {
            signature.add(mbeanParameterInfo.getType());
        }

        Object[] convertedArguments = convertValues(arguments, signature);

        logger.debug("Invoke {}:{}({}) ...", on, operationName, Arrays.asList(convertedArguments));

        Object result = mBeanServer.invoke(on, operationName, convertedArguments, signature.toArray(new String[0]));

        if ("void".equals(beanOperationInfo.getReturnType()) && result == null) {
            result = "void";
        }

        logger.info("Invoke {}:{}({}): {}", on, operationName, Arrays.asList(convertedArguments), result);

        String description = "Invoke operation " + on + ":" + operationName + "(" + Strings2.join(convertedArguments, ", ") + "): " + Strings2.toString(result);
        return new Result(on, result, description);
    }

    /**
     * Convert given values
     *
     * @return
     */
    @Nonnull
    protected Object[] convertValues(@Nonnull String[] arguments, @Nonnull List<String> signature) {
        if (arguments.length != signature.size())
            throw new IllegalArgumentException("arguments and signature must have the same length (" + arguments.length + " vs. " + signature.size() + "");

        Object[] results = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            results[i] = convertValue(arguments[i], signature.get(i));
        }
        return results;
    }

    /**
     * Convert the given <code>value</code> to the given <code>targetType</code>.
     * </p>
     * Special value '{@value #NULL_VALUE}' is seen as <code>null</code>.
     *
     * @param value
     * @param targetType The value {@link Class#getName()} such as <code>java.lang.String</code> if <code>null</code>, target type is assumed to be String.
     * @return the converted value
     * @see javax.management.MBeanParameterInfo#getType()
     */
    @Nullable
    private Object convertValue(@Nullable String value, @Nullable String targetType) {
        if (NULL_VALUE.equals(value) || null == value) {
            return null;
        } else if (String.class.getName().equals(targetType)) {
            return value;
        } else if (int.class.getName().equals(targetType) || Integer.class.getName().equals(targetType)) {
            return new Integer(value);
        } else if (float.class.getName().equals(targetType) || Float.class.getName().equals(targetType)) {
            return new Float(value);
        } else if (boolean.class.getName().equals(targetType) || Boolean.class.getName().equals(targetType)) {
            return Boolean.valueOf(value);
        } else {
            logger.warn("Unexpected type {} for value {}, return String", targetType, value);
            return value;
        }
    }

    @Nonnull
    protected MBeanServerConnection connectToMbeanServer(@Nonnull String pid) throws IOException {
        VirtualMachine vm;
        try {
            Integer.parseInt(pid);
        } catch (Exception e) {
            logger.warn("Exception parsing PID '{}'", pid, e);
        }
        try {
            vm = VirtualMachine.attach(pid);
        } catch (Exception e) {
            throw new IllegalStateException("Exception attaching VM with PID '" + pid + "'", e);
        }


        logger.trace("VM Agent Properties");
        for (String key : new TreeSet<String>(vm.getAgentProperties().stringPropertyNames())) {
            logger.trace("\t {}: {}", key, vm.getAgentProperties().get(key));
        }
        logger.trace("VM System Properties");
        for (String key : new TreeSet<String>(vm.getSystemProperties().stringPropertyNames())) {
            logger.trace("\t {}: {}", key, vm.getSystemProperties().get(key));
        }


        String connectorAddress =
                vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
        if (connectorAddress == null) {
            String agent = vm.getSystemProperties().getProperty(
                    "java.home") + File.separator + "lib" + File.separator +
                    "management-agent.jar";
            try {
                vm.loadAgent(agent);
            } catch (Exception e) {
                throw new IllegalStateException("Exception loading agent " + agent);
            }
            connectorAddress = vm.getAgentProperties().getProperty(
                    "com.sun.management.jmxremote.localConnectorAddress");
        }

        if (connectorAddress == null) {
            throw new IllegalStateException("Could not attach to pid: " + pid + ". No connector available");
        }
        logger.trace("Connect to {} ...", connectorAddress);

        JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectorAddress));

        return connector.getMBeanServerConnection();
    }

    static {
        // configure Logback SimpleLogger
        setSystemPropertyIfNotDefined(SimpleLogger.SHOW_DATE_TIME_KEY, "true");
        setSystemPropertyIfNotDefined(SimpleLogger.DATE_TIME_FORMAT_KEY, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        setSystemPropertyIfNotDefined(SimpleLogger.LOG_FILE_KEY, "System.out");
        setSystemPropertyIfNotDefined(SimpleLogger.SHOW_LOG_NAME_KEY, "false");
        setSystemPropertyIfNotDefined(SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
    }

    static class JmxInvokerArguments {

        @Option(name = "-p", aliases = "--pid", required = false, metaVar = "PID", usage = "PID of the JVM to attach to. " +
                "--pid or --pid-file required")
        public String pid;
        @Option(name = "--pid-file", required = false, metaVar = "PID_FILE", usage = "PID FILE of the JVM to attach to. " +
                "--pid or --pid-file required")
        public File pidFile;
        @Option(name = "-on", aliases = "--object-name", required = true, handler = ObjectNameOptionHandler.class, metaVar = "OBJECT_NAME", usage = "ObjectName of the MBean(s) to invoke, can contain wildcards (*). If more than one MBean match, all MBeans are invoked")
        public ObjectName objectName;
        @Option(name = "-attr", aliases = "--attribute", required = false, handler = StringArrayOptionHandler.class, metaVar = "ATTRIBUTE_NAME [VAL]", usage = "attribute to read or to update. " +
                "If a VAL is passed, then it is a write action, otherwise, it is a read action")
        public String[] attribute;
        @Option(name = "-op", aliases = "--operation", required = false, handler = StringArrayOptionHandler.class, metaVar = "OPERATION_NAME [ARG1 [ARG2  ...]]", usage = "operation to invoke with arguments")
        public String[] operation;
        @Option(name = "-v", aliases = {"-x", "--verbose"}, usage = "print debug info ")
        public boolean verbose;
        @Option(name = "-vvv", aliases = "-xxx", usage = "print super verbose debug info ")
        public boolean superVerbose;
        @Option(name = "-h", aliases = "--help", usage = "print help")
        public boolean showHelp;
        public CmdLineParser cmdLineParser;
        @Option(name = "-l", aliases = "--list-mbeans", required = false, usage = "list mbeans")
        public boolean listMbeans;
        @Option(name = "-d", aliases = "--describe-mbeans", required = false, usage = "describe mbeans")
        public boolean describeMbeans;

    }

    public static class Result {
        ObjectName objectName;
        Object value;
        String description;

        public Result(ObjectName objectName, Object value, String description) {
            this.objectName = objectName;
            this.value = value;
            this.description = description;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "objectName=" + objectName +
                    ", value=" + value +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
