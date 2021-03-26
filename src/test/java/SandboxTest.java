import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;

public class SandboxTest {

    @Test
    public void testThenCombine() {
        List<String> result = someTask("A")
                .thenCombine(someTask("B"), Arrays::asList)
                .thenCombine(someTask("C"), (a, b) -> {
                    List<String> all = new ArrayList<>(a);
                    all.add(b);
                    return all;
                })
                .join();

        assertEquals(Arrays.asList("A", "B", "C"), result);
    }

    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        CompletableFuture<String> t1 = someTask("A");
        CompletableFuture<String> t2 = someTask("B");
        CompletableFuture<String> t3 = someTask("C");

        List<String> result = Arrays.asList(t1.get(), t2.get(), t3.get());

        assertEquals(Arrays.asList("A", "B", "C"), result);
    }

    private final Executor executor = newFixedThreadPool(20);

    private CompletableFuture<String> someTask(String result) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SECONDS.sleep(1);
                return result;
            } catch (InterruptedException e) {
                currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
