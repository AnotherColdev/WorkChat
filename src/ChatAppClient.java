import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatAppClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatAppClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje) {
        out.println(mensaje);
    }

    public void recibirMensajes() {
        try {
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                System.out.println(mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("\n Ingrese su nick: \n");
        String nick = scanner.nextLine();

        ChatAppClient cliente = new ChatAppClient("localhost", 1234);

        // Enviar el nick al servidor
        cliente.enviarMensaje(nick);

        // Recibir mensajes en un hilo separado
        Thread recibirHilo = new Thread(cliente::recibirMensajes);
        recibirHilo.start();

        // Bucle para leer y enviar mensajes
        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.print("Ingrese el nick del destinatario: ");
            String destinatario = scanner.nextLine();
            System.out.print("Ingrese el mensaje: ");
            String mensaje = scanner.nextLine();
            cliente.enviarMensaje("\n" + destinatario + ": " + mensaje);
        }
    }
}
