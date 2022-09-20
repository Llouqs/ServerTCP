import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server implements Runnable {

    private final ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ArrayList<String> phrases;

    public Server() {
        done = false;
        connections = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            getPhrases();
            server = new ServerSocket(8000);
            System.out.println("Server started!");
            ExecutorService pool = Executors.newCachedThreadPool();
            int i = 1;
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                handler.setWaitingTime(i++);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException ignored) {
        }
    }

    public void getPhrases() {
        InputStream inputStream = Server.class.getClassLoader().getResourceAsStream("phrases.txt");
        assert inputStream != null;
        Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines();
        phrases = lines.collect(Collectors.toCollection(ArrayList::new));
    }

    class ConnectionHandler implements Runnable {

        private final Socket client;
        private PrintWriter out;
        private int waitingTime;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                System.out.println("New connection!");
                while (!client.isClosed()) {
                    sendMessage(phrases.get((int) (Math.random() * phrases.size())));
                    Thread.sleep(waitingTime);
                }
            } catch (IOException | InterruptedException e) {
                shutdown();
            }
        }

        public void shutdown() {
            try {
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException ignored) {

            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void setWaitingTime(int wt) {
            waitingTime = wt * 3000;
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
