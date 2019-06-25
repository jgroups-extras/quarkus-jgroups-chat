package org.jgroups.quarkus;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.MessageBatch;
import org.jgroups.util.Util;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Bela Ban
 * @since  1.0.0
 */
@ApplicationScoped
@Path("/chat")
public class ChatResource extends ReceiverAdapter implements Publisher<String> {
    protected final Set<Subscriber<? super String>> subscribers=new HashSet<>();

    @Inject JChannel channel;


    protected void init(@Observes StartupEvent evt) throws Exception {
        channel.setReceiver(this);
    }

    protected void destroy(@Observes ShutdownEvent evt) {
        Util.close(channel);
        subscribers.forEach(Subscriber::onComplete);
        subscribers.clear();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/send/{msg}")
    public String sendMessage(@PathParam("msg") String msg) throws Exception {
        channel.send(null, Objects.requireNonNull(msg).getBytes());
        return String.format("message \"%s\" was sent on channel \n", msg);
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Path("/subscribe")
    public Publisher<String> greeting() {
        return this;
    }

    public void receive(Message msg) {
        onNext(msg);
    }

    public void receive(MessageBatch batch) {
        for(Message msg: batch)
            onNext(msg);
    }

    public void viewAccepted(View view) {
        System.out.printf("-- new view: %s\n", view);
    }



    public void subscribe(Subscriber<? super String> s) {
        if(s != null)
            subscribers.add(s);
    }

    protected void onNext(Message msg) {
        String s=new String(msg.getRawBuffer(), msg.getOffset(), msg.getLength());
        System.out.printf("-- from %s: %s\n", msg.src(), s);
        subscribers.forEach(sub -> sub.onNext(s));
    }

}
