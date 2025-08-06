package integration.mock_servers;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class EchoServer implements Runnable {

    private final int port;
    private ServerSocket serverSocket;

    private boolean running = true;

    public EchoServer(int port) {
        this.port = port;
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close(); // unblocks accept()
            }
        } catch (IOException e) {
            log.warn("Failed to close server socket: {}", e.getMessage());
        }
    }


    @Override
    public void run() {
        log.info("Echo server running on port: {}", port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            while (running || !Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                log.info("Echo server port {} connection from {}", port, socket.getRemoteSocketAddress());
                Thread.startVirtualThread(() -> {
                    try (socket) {
                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();

                        int firstByte = in.read();
                        if (firstByte == -1) {
                            log.info("Client closed without sending data");
                            return;
                        }

                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        buffer.write(firstByte);
                        while (in.available() > 0) {
                            buffer.write(in.read());
                        }

                        String text = buffer.toString(StandardCharsets.UTF_8);
                        String response = String.format("%s from port %d", text, port);

                        out.write(response.getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    } catch (Exception e) {
                        log.error("Something went wrong with echo server : {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Echo server failed: " + e.getMessage());
        }
    }
}
