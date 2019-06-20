package org.jgroups.quarkus;

import io.quarkus.runtime.StartupEvent;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.MessageBatch;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Bela Ban
 * @since  1.0.0
 */
@Path("/chat")
public class ChatResource extends ReceiverAdapter {

    @Inject ChatService chat;

    protected void init(@Observes StartupEvent evt) throws Exception {
        System.out.printf("-- ChatResource was started: evt=%s\n", evt);
        chat.register(this);
    }

    //protected void destroy(@Observes ShutdownEvent evt) {
      //  System.out.printf("-- ChatResource was stopped: evt=%s\n", evt);
    //}


    @GET
    @Path("/send/{msg}")
    public String sendMessage(@PathParam("msg") String msg) throws Exception {
        chat.send(msg);
        return String.format("message \"%s\" was sent on channel \n", msg);
    }

    public void receive(Message msg) {
        System.out.printf("-- msg from %s: %s\n", msg.src(), new String(msg.getRawBuffer(), msg.getOffset(), msg.getLength()));
    }

    public void receive(MessageBatch batch) {
        for(Message msg: batch) {
            System.out.printf("-- (batched) msg from %s: %s\n", msg.src(), new String(msg.getRawBuffer(), msg.getOffset(), msg.getLength()));
        }
    }

    public void viewAccepted(View view) {
        System.out.printf("-- new view: %s\n", view);
    }

}
