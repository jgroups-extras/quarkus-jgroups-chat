# quarkus-jgroups-chat
Chat demo using the quarkus-jgroups extension



### Running the demo
`mvn package` creates a runner JAR in the `target` directory. This can be run via the `java -jar JAR` command.
`mvn package -Pnative` creates the native demo.

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