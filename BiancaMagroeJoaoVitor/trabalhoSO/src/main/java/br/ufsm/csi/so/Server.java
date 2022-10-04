
package br.ufsm.csi.so;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import lombok.SneakyThrows;

// classe inicial, cria o servidor e espera conexões
public class Server {
    public static Map<Integer, Assento> assentos = new HashMap<>();
    public static Semaphore mutex = new Semaphore(1);

    public static Logger logger = new Logger();

    @SneakyThrows
    public static void main(String[] args) {
        // gerar 24 assentos
        for (int id = 1; id < 24; id++) {
            assentos.put(id, new Assento(id));
        }

        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Rodando servidor em http://localhost:8080");

            while (true) {
                // aceita a conexão
                Socket socket = server.accept();

                // cria uma nova conexão
                Connection connection = new Connection(socket);

                // passar para a thread
                Thread thread = new Thread(connection);

                thread.start();
            }
        }
    }
}
