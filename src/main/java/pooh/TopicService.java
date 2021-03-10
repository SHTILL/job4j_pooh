package pooh;

import net.jcip.annotations.ThreadSafe;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@ThreadSafe
public class TopicService implements Service {
    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentLinkedQueue<Req>>> queues = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Function<Req, Resp>> methods = new ConcurrentHashMap<>();

    public TopicService() {
        methods.put("POST", new TopicService.PostOperation());
        methods.put("GET", new TopicService.GetOperation());
    }

    private class PostOperation implements Function<Req, Resp> {
        @Override
        public Resp apply(Req req) {
            String queueName = req.valueOf("name");
            String id = req.valueOf("id");
            if ((queueName == null) || (id == null)) {
                return Resp.queuedResponse();
            }
            queues.compute(queueName, (name, stored) -> {
                ConcurrentMap<String, ConcurrentLinkedQueue<Req>> map = (stored == null) ? new ConcurrentHashMap<>() : stored;
                ConcurrentLinkedQueue<Req> q = map.getOrDefault(id, new ConcurrentLinkedQueue<>());
                q.add(req);
                map.put(id, q);
                return map;
            });
            return Resp.queuedResponse();
        }
    }

    private class GetOperation implements Function<Req, Resp> {
        @Override
        public Resp apply(Req req) {
            String queueName = req.valueOf("name");
            String id = req.valueOf("id");
            if ((queueName == null) || (id == null)) {
                return Resp.responseBadRequest();
            }
            ConcurrentMap<String, ConcurrentLinkedQueue<Req>> topic = queues.get(queueName);
            if (topic == null) {
                return Resp.emptyResponse();
            }
            Queue<Req> q = topic.get(id);
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