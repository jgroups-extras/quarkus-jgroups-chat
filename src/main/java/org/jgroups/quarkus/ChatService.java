package org.jgroups.quarkus;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jgroups.JChannel;
import org.jgroups.Receiver;
import org.jgroups.util.Util;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Objects;

/**
 * @author Bela Ban
 * @since  1.0.0
 */
@ApplicationScoped
public class ChatService {

    @Inject
    JChannel channel;

    protected void init(@Observes StartupEvent evt) throws Exception {
        if(!channel.isConnected())
            channel.connect("quarkus-chat");
        System.out.printf("-- ChatService: channel %s was started on evt %s\n", channel.getAddress(), evt);
    }

    protected void destroy(@Observes ShutdownEvent evt) {
        Util.close(channel);
        System.out.printf("-- ChatService: channel was closed; evt=%s\n", evt);
    }


    public ChatService register(Receiver r) {
        if(channel != null && r != null)
            channel.setReceiver(r);
        return this;
    }

    public ChatService send(String msg) throws Exception {
        channel.send(null, Objects.requireNonNull(msg).getBytes());
        return this;
    }



}
