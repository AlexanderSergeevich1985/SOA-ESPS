package com.soaesps.aggregator.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import com.soaesps.aggregator.domain.CompletionTask;
import com.soaesps.aggregator.domain.RegisteredTask;
import com.soaesps.aggregator.domain.ReplyMsg;
import com.soaesps.aggregator.domain.TaskFailed;
import com.soaesps.aggregator.service.AggregatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("serviceActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ServiceActor extends UntypedAbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Autowired
    private AggregatorService aggregatorService;

    private ActorRef workerRouter = getContext().actorOf(FromConfig.getInstance()
            .props(Props.create(WorkerActor.class)),"workerRouter");

    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof RegisteredTask) {
            RegisteredTask task = (RegisteredTask) message;
            aggregatorService.registerTask(task);
            this.getSender().tell(new ReplyMsg(), getSelf());
            getContext().stop(getSelf());
        }
        else if(message instanceof CompletionTask) {
            CompletionTask task = (CompletionTask) message;
            if(aggregatorService.completeTask(task)) {
                this.getSender().tell(new ReplyMsg(), getSelf());
            }
            else {
                this.getSender().tell(new TaskFailed("Task failed, timeout expiration"), getSelf());
            }
            getContext().stop(getSelf());
        }
        else {
            unhandled(message);
        }
    }
}