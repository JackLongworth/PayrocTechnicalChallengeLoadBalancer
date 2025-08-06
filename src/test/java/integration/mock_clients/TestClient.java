package integration.mock_clients;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class TestClient {
    public static String sendAndReceive(String host, int port, String message) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            log.info("Socket connected to {} and attempting send", socket.getRemoteSocketAddress());
            socket.getOutputStream().write(message.getBytes());
            socket.getOutputStream().flush();

            byte[] buffer = new byte[1024];
            int read = socket.getInputStream().read(buffer);
            return new String(buffer, 0, read);
        }
    }
}