package pooh;

import net.jcip.annotations.ThreadSafe;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@ThreadSafe
public class QueueService implements Service {
    private final ConcurrentMap<String, ConcurrentLinkedQueue<Req>> queues = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Function<Req, Resp>> methods = new ConcurrentHashMap<>();

    public QueueService() {
        methods.put("POST", new PostOperation());
        methods.put("GET", new GetOperation());
    }

    private class PostOperation implements Function<Req, Resp> {
        @Override
        public Resp apply(Req req) {
            String queueName = req.valueOf("name");
            if (queueName == null) {
                return Resp.queuedResponse();
            }
            queues.compute(queueName, (name, stored) -> {
                ConcurrentLinkedQueue<Req> q = (stored == null) ? new ConcurrentLinkedQueue<>() : stored;
                q.add(req);
                return q;
            });
            return Resp.queuedResponse();
        }
    }

    private class GetOperation implements Function<Req, Resp> {
        @Override
        public Resp apply(Req req) {
            String queueName = req.valueOf("name");
            if (queueName == null) {
                return Resp.responseBadRequest();
            }
            Queue<Req> q = queues.get(queueName);
            if (q == null) {
                return Resp.emptyResponse();
            }
            Req r = q.poll();
            if (r == null) {
                return Resp.emptyResponse();
            }
            return new Resp(r.valueOf("text")  + System.lineSeparator(), 200);
        }
    }

    @Override
    public Resp process(Req req) {
        Function<Req, Resp> method = methods.get(req.method());
        return method == null ? Resp.responseBadRequest() : method.apply(req);
    }
}
