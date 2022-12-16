
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private final int PORT = 9999;
    private final int THREADS_QUANTITY = 64;
    private ExecutorService service;

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server Started");

        } catch (IOException e) {
            e.printStackTrace();
        }
        service = Executors.newFixedThreadPool(THREADS_QUANTITY);
        listenConnection();
    }

    public void listenConnection() {
        while (true) {
            try {
                final var socket = serverSocket.accept();

                service.submit(new Thread(new ServerThreads(socket)));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
