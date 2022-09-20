import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client implements Runnable {

    private Socket client;

    @Override
    public void run() {
        try {
            Socket client = new Socket("127.0.0.1", 8000);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String inMessage;
            while (!client.isClosed()) {
                if ((inMessage = in.readLine()) != null){
                    System.out.println(inMessage);
                }
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException ignored) {

        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}

