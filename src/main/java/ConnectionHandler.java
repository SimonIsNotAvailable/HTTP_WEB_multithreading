import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandler {
    private final Socket socket;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers;

    public ConnectionHandler(Socket socket,
                             ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers) {
        this.socket = socket;
        this.handlers = handlers;
    }

    public void handle() {

        try {
            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final var out = new BufferedOutputStream(socket.getOutputStream());

            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            var request = new Request(parts[0], parts[1], parts[2], null, null);
            if (!handlers.containsKey(request.getMethod())) {
                send404NotFound(out);
                return;
            }

            var pathHandlers = handlers.get(request.getMethod());

            if (!pathHandlers.containsKey(request.getPath())) {
                send404NotFound(out);
                return;
            }

            var handler = pathHandlers.get(request.getPath());

            try {
                handler.handle(request, out);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                send500ServerError(out);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send500ServerError(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 500 Server Error\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void send404NotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}

