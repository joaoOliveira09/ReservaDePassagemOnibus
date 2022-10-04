package br.ufsm.csi.so;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import lombok.SneakyThrows;


public class Connection implements Runnable {
    private Socket socket;

    public Connection(Socket socket) {
        this.socket = socket;
    }

    @Override
    @SneakyThrows
    public void run() {
        InputStream in = this.socket.getInputStream();
        OutputStream out = this.socket.getOutputStream();

        // scanner pra ler o InputStream
        Scanner scanner = new Scanner(in);

        if (!scanner.hasNext()) {
            scanner.close();

            return;
        }

        String method = scanner.next();
        String path = scanner.next();

        // printar o request no console
        System.out.println(method + " " + path);

        String[] dirAndParams = path.split("\\?");

        // recurso acessado é o indice 0
        String recurso = dirAndParams[0];
        // queries foram interpretadas do url
        Map<String, String> query = this.parseQuery(dirAndParams.length > 1
                ? dirAndParams[1].split("&")
                : null);

        byte[] contentBytes = null;

        String header = """
                HTTP/1.1 200 OK
                Content-Type: text/html; charset=UTF-8


                """;


        // request de CSS
        if (recurso.startsWith("/css/")) {
            contentBytes = this.getBytes(recurso);

            if (contentBytes != null)
                header = header.replace("text/html", "text/css");
        }

        // Pagina inicial!
        if (recurso.equals("/")) {
            contentBytes = this.getBytes("index.html");

            String html = new String(contentBytes);

            String elementos = "";

            for (Assento assento : Server.assentos.values()) {
                // criar botoes
                String elemento = "<button type=\"button\" class=\"btn btn-primary\"><a";

                elemento += " class=\"assento\"";
                // TODO: não adicionar se o assento estiver ocupado!
                elemento += " href=\"/reservar?id=" + assento.getId() + "\"";
                elemento += ">" + assento.getId() + "</a></button>";

                elementos += elemento + "\n";
            }

            // substitui os assentos
            html = html.replace("<assentos />", elementos);

            contentBytes = html.getBytes();
        }

        if (recurso.equals("/reservar")) {
            contentBytes = this.getBytes("reservar.html");

            String html = new String(contentBytes);

            // substituir o ID no html
            html = html.replace("{{id}}", query.get("id"));

            contentBytes = html.getBytes();
        }

        if (recurso.equals("/confirmar")) {
            // header de redirecionar
            header = """
                    HTTP/1.1 302 Found
                    Content-Type: text/html; charset=UTF-8
                    Location: /


                    """;

            // tranca a região crítica
            Server.mutex.acquire();

            int id = Integer.parseInt(query.get("id"));
            Assento assento = Server.assentos.get(id);

            // TODO: Verificar se o assento está vago
            for( Assento a : Server.assentos.values()){
                if (a.getId() == id && a.isOcupado() == false){
                    String nome = query.get("nome");
                    String dataHora[] = query.get("data_hora").split("T");
                    String data = dataHora[0];
                    String hora = dataHora[1];

                    assento.setNome(nome);
                    assento.setData(data);
                    assento.setHora(hora);
                    assento.setOcupado(true);

                    Server.logger.log(socket, assento);

                    System.out.println("LOG Nova reserva adicionada: " + assento.getId() + " | " + assento.getNome());
                    System.out.println(""+assento.getHora());
                    System.out.println(""+assento.getData());
                    System.out.println("======================================================================");
                    break;
                }
                else if (a.getId() == id && a.isOcupado() == true){
                    System.out.println("Assento Ocupado");
                    //recurso.equals("/confirmar");
                    contentBytes = this.getBytes("Erro.html");

                    String html = new String(contentBytes);

                    //String elementos = "";
                    break;
                }
            }

            // libera mutex
            Server.mutex.release();
        }

        // mostrar página de 404
        if (contentBytes == null) {
            contentBytes = this.getBytes("404.html");

            header = """
                    HTTP/1.1 404 Not Found
                    Content-Type: text/html; charset=UTF-8


                    """;
        }

        out.write(header.toString().getBytes());
        out.write(contentBytes);

        // close dos strems
        in.close();
        out.close();

        scanner.close();

        // fechar a conexão
        this.socket.close();
    }

    @SneakyThrows
    private Map<String, String> parseQuery(String[] query) {
        // não tem query!
        if (query == null)
            return null;

        Map<String, String> queries = new HashMap<>();

        for (String s : query) {
            // separar os ids
            String[] kvPair = s.split("=");

            // valor= Null
            if (kvPair.length == 1) {
                queries.put(kvPair[0], null);
            } else {
                queries.put(kvPair[0], URLDecoder.decode(kvPair[1], "UTF-8"));
            }
        }

        return queries;
    }

    @SneakyThrows
    private byte[] getBytes(String recurso) {
        if (recurso.startsWith("/"))
            recurso = recurso.substring(1);

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(recurso);

        if (is != null)
            return is.readAllBytes();

        return null;
    }
// tentativa de exibir a tabela

    public String tabelaReservas(ArrayList<Assento> assentos) {

        String linhas = "";
        String tabela;

        for (Assento assento : assentos) {
            linhas += "" +
                    "<tr>\n" +
                    "<th scope=\"row\">" + assento.getId() + "</th>\n" +
                    "<td>" + assento.getNome() + "</td>\n" +
                    "<td>" + assento.getData() + "</td>\n" +
                    "<td>" + assento.getHora() + "</td>\n" +
                   // "<td>" + reserva.getHora() + "</td>\n" +
                    "</tr>\n";
        }

        tabela = "" +
                "<table class=\"table table-bordered table-striped\">\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th scope=\"col\">Assentos</th>\n" +
                "<th scope=\"col\">Passageiro</th>\n" +
                "<th scope=\"col\">IP</th>\n" +
                "<th scope=\"col\">Data</th>\n" +
                "<th scope=\"col\">Hora</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                linhas +
                "</tbody>\n" +
                "</table>";

        return tabela;
    }
}
