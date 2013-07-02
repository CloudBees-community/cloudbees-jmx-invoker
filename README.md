# CloudBees JMX Invoker



### Read JMX attribute:


```
java \
   -cp /Library/Java/JavaVirtualMachines/jdk1.7.jdk/Contents/Home/lib/tools.jar:target/cloudbees-jmx-invoker-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
   com.cloudbees.jmx.JmxInvoker \
   -vvv -p 11172 \
   -on Catalina:type=DataSource,class=javax.sql.DataSource,name="jdbc/petclinic" \
   -attr numActive

```


### Invoke JMX operation : get LogBack log level
```
java \
   -cp /Library/Java/JavaVirtualMachines/jdk1.7.jdk/Contents/Home/lib/tools.jar:target/cloudbees-jmx-invoker-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
   com.cloudbees.jmx.JmxInvoker \
   -v -p 11172 \
   -on "ch.qos.logback.classic:Name=cocktail-app,Type=ch.qos.logback.classic.jmx.JMXConfigurator" \
   -op getLoggerLevel org.jmxtrans
```

### Invoke JMX operation : set LogBack log level

```
java \
   -cp /Library/Java/JavaVirtualMachines/jdk1.7.jdk/Contents/Home/lib/tools.jar:target/cloudbees-jmx-invoker-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
   com.cloudbees.jmx.JmxInvoker \
   -v -p 11172 \
   -on "ch.qos.logback.classic:Name=cocktail-app,Type=ch.qos.logback.classic.jmx.JMXConfigurator" \
   -op setLoggerLevel org.jmxtrans DEBUG

```