import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatAppServer {
    private Map<String, PrintWriter> clientes;

    public ChatAppServer() {
        clientes = new HashMap<>();
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado.");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                out.println("Ingrese su nick:");
                String nick = in.readLine();

                System.out.println("Cliente " + nick + " identificado.");

                // Guardar el PrintWriter asociado al nick del cliente
                clientes.put(nick, out);

                // Crear un hilo para manejar las comunicaciones del cliente
                ClienteHandler clienteHandler = new ClienteHandler(clientSocket, nick);
                Thread clienteThread = new Thread(clienteHandler);
                clienteThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String remitente, String destinatario, String mensaje) {
        if (clientes.containsKey(destinatario)) {
            PrintWriter out = clientes.get(destinatario);
            out.println(remitente + ": " + mensaje);
        }
    }

    private class ClienteHandler implements Runnable {
        private Socket socket;
        private String nick;
        private BufferedReader in;

        public ClienteHandler(Socket socket, String nick) {
            this.socket = socket;
            this.nick = nick;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String mensaje;

                while ((mensaje = in.readLine()) != null) {
                    System.out.println("Mensaje recibido de " + nick + ": " + mensaje);
                    // Obtener destinatario del mensaje
                    String[] partes = mensaje.split(":");
                    if (partes.length >= 2) {
                        String destinatario = partes[0].trim();
                        String contenido = partes[1].trim();
                        enviarMensaje(nick, destinatario, contenido);
                    }
                }

                // Si el cliente cierra la conexión, eliminarlo de la lista
                clientes.remove(nick);
                System.out.println("Cliente " + nick + " desconectado.");

                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatAppServer servidor = new ChatAppServer();
        servidor.run();
    }
}
