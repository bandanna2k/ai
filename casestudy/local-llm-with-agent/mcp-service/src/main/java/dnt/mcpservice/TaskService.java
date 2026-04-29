package dnt.mcpservice;

import io.vertx.core.Future;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaskService {

//    private final Map<String, Task> store = new ConcurrentHashMap<>();
//
//    public Future<Task> createAsync(String title, Priority priority) {
//        return Future.future(promise -> {
//            Task task = new Task(UUID.randomUUID().toString(), title, priority, "OPEN");
//            store.put(task.getId(), task);
//            promise.complete(task);
//        });
//    }
//
//    public Future<List<Task>> listAsync(String priority) {
//        return Future.future(promise -> {
//            List<Task> result = store.values().stream()
//                    .filter(t -> priority == null || t.getPriority().name().equals(priority))
//                    .toList();
//            promise.complete(result);
//        });
//    }
}