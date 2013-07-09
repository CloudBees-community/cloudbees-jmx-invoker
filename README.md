# CloudBees JMX Invoker

Small script to invoke a JMX operation or access a JMX attribute of a Java process running locally.


## JMX Invoker parameters

```
 --pid-file PID_FILE                    : PID FILE of the JVM to attach to.
                                          --pid or --pid-file required
 -attr (--attribute) ATTRIBUTE_NAME     : attribute to read or to update. If a
 [VAL]                                  : VAL is passed, then it is a write
                                          action, otherwise, it is a read action
 -h (--help)                            : print help
 -on (--object-name) OBJECT_NAME        : ObjectName of the MBean(s) to invoke,
                                          can contain wildcards (*). If more
                                          than one MBean match, all MBeans are
                                          invoked
 -op (--operation) OPERATION_NAME       : operation to invoke with arguments
 [ARG1 [ARG2  ...]]                     :
 -p (--pid) PID                         : PID of the JVM to attach to. --pid or
                                          --pid-file required
 -v (-x, --verbose)                     : print debug info
 -vvv (-xxx)                            : print super verbose debug info
```

## Samples


### Read JMX attribute:


```
java \
   -cp $JAVA_HOME/lib/tools.jar:target/cloudbees-jmx-invoker-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
   com.cloudbees.jmx.JmxInvoker \
   -vvv -p 11172 \
   -on Catalina:type=DataSource,class=javax.sql.DataSource,name="jdbc/petclinic" \
   -attr numActive

```


### Invoke JMX operation : get LogBack log level
```
java \
   -cp $JAVA_HOME/lib/tools.jar:target/cloudbees-jmx-invoker-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
   com.cloudbees.jmx.JmxInvoker \
   -v -p 11172 \
   -on "ch.qos.logback.classic:Name=cocktail-app,Type=ch.qos.logback.classic.jmx.JMXConfigurator" \
   -op getLoggerLevel org.jmxtrans
```

### Invoke JMX operation : set LogBack log level

```
java \
   -cp $JAVA_HOME/lib/tools.jar:target/cloudbees-jmx-invoker-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
   com.cloudbees.jmx.JmxInvoker \
   -v -p 11172 \
   -on "ch.qos.logback.classic:Name=cocktail-app,Type=ch.qos.logback.classic.jmx.JMXConfigurator" \
   -op setLoggerLevel org.jmxtrans DEBUG

```