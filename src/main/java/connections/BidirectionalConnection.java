package connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BidirectionalConnection {

    private final Socket client;
    private final Socket backend;

    public BidirectionalConnection(Socket client, Socket backend) {
        this.client = client;
        this.backend = backend;
    }

    public void start() {
        CountDownLatch latch = new CountDownLatch(2);

        Thread.startVirtualThread(() -> {
            stream(client, backend);
            latch.countDown();
        });

        Thread.startVirtualThread(() -> {
            stream(backend, client);
            latch.countDown();
        });

        try {
            latch.await(); // Wait for both threads to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            closeQuietly(client);
            closeQuietly(backend);
        }
    }

    private void stream(Socket from, Socket to) {
        try {
            InputStream in = from.getInputStream();
            OutputStream out = to.getOutputStream();
            in.transferTo(out);
        } catch (IOException e) {
            log.debug("Couldn't stream data ({} -> {}): {}", from.getInetAddress(), to.getInetAddress(), e.getMessage());
        }
    }

    private void closeQuietly(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                log.info("Closing socket {}", socket.getInetAddress().toString());
                socket.close();
            } catch (IOException e) {
                log.debug("Error closing socket {}: {}", socket, e.getMessage());
            }
        }
    }
}
