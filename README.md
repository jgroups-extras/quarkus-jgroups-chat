# quarkus-jgroups-chat
Chat demo using the quarkus-jgroups extension



### Running the demo
`mvn package` creates a runner JAR in the `target` directory. This can be run via the `java -jar JAR` command 
(in a first shell):

```
cd ./target
java -jar -Dquarkus.http.port=8080 quarkus-jgroups-chat-1.0.0-SNAPSHOT-runner.jar 
```

In a second shell, run
```
cd ./target
java -jar -Dquarkus.http.port=8081 quarkus-jgroups-chat-1.0.0-SNAPSHOT-runner.jar 
```

If this is on the same host, `quarkus.http.port` needs to be different for each instance. It does not need to be
specified if each instance is run on a different host.

The 2 instances should find each other and form a cluster. This is seen in the output:
```
\-------------------------------------------------------------------
GMS: address=belasmac-39930, cluster=quarkus-jgroups-chat, physical address=127.0.0.1:7800
-------------------------------------------------------------------
-- view: [belasmac-29858|9] (2) [belasmac-29858, belasmac-39930]
```
As can be seen view `belasmac-29858|9` has 2 members.

The chat instances can now be accessed via HTTP to send and receive messages. This can be done in a number of ways.
The first one is to subscribe to messages:
```
http://localhost:8080/streaming.html
```

This will use Javascript to listen for server-side events on `/chat/subscribe`.

To send messages, URL `/chat/send/message` can be used, e.g. via curl (`curl http://localhost:8080/chat/send/hello`)
or a web browser: 
```
http://localhost:8082/chat/send/hello%20world
```

This accesses the second instance. Note that a message sent to one of the 2 instances will be sent to _all_
instances, and each instance will print it to stdout. The above web client listening on `/chat/subscribe/` will
also print the message.



#### Creating the native demo
`mvn package -Pnative` creates the native demo. The rest of the steps is the same as for the non-native demo above.




Multiple instances can be run by changing the HTTP port, e.g.
-Dquarkus.http.port=8001

### Misc
Note that in order to generate a native image, the following properties need to be enabled on the quarkus-maven-plugin
(in the POM):

`<reportErrorsAtRuntime>true</reportErrorsAtRuntime>`: the native build analyzes code like this:

```
@Property(description="Use InetAddress.isReachable() to verify suspected member instead of regular messages")
protected boolean use_icmp;
...
if(use_icmp)
    s.forEach(this::verifySuspectWithICMP);
else
    verifySuspect(s);
```
Since `use_icmp` is not `final`, the analysis needs to look at both branches, including `verifySuspectWithICMP()`.

This method, however, calls a method (`NetworkInterface.isReachable()`) that's not implemented by the GraalVM, and
therefore terminates the build with an error.

The default for `use_icmp` is false and unless a user changes this, this branch of the code will never
be visited. However, this branch cannot be optimized away, as `use_icmp` might be set to true (even at runtime).

We have a couple of occurrences of this, so currently we decided to report issues caused by this at run time rather
than build time. 

`<enableJni>true</enableJni>`: don't know why this is needed, but if it isn't enabled, the native build fails with
a non-sensical error message. Will need to revisit with later Quarkus builds...