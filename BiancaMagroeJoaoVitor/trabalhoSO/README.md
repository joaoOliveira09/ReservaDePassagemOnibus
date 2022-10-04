# reservonibus

Esse é um template pro trabalho de SO.

O objetivo é implementar um servidor que responda páginas HTML.

Nessas páginas deve ser possível fazer uma reserva para um ônibus.

## Observações

Esse projeto é uma (super) simplificação do projeto [SOBus](https://github.com/nirewen/sobus).

A lógica é a mesma, nenhum HTML é escrito na camada do servidor, somente gerado.

Cada HTML fica na pasta de resources.
Os arquivos têm elementos que são substituidos pelo servidor.

Alguns detalhes ficaram de fora pelo bem da simplificação, mas que não são dificeis de adicionar, são eles:

-   Estilização com CSS;
-   Resposta de imagens (existe um comentário em `Connection.java, linha 103` falando sobre);
-   Verificação de assento vago (vide `Connection.java, linhas 144 e 184`);
-   Diferenciação de assento vago e ocupado na página inicial.

A implementação é usando semáforos, enquanto a do SOBus é usando monitores (synchronized), vale a pena dar uma olhada se quiser mudar a implementação.

## Explicação

Algumas porções do código estão comentadas para melhor entendimento.

Como esse é um template, serve para explicar como tudo funciona para a própria implementação.

~~Eu sei que provavelmente vão usar esse template sem mudar nada, só tirar os comentários...~~

---

O servidor (Server.java) roda na porta 8080 em um while (true), para que a aplicação não pare.

Dentro do while, uma conexão é esperada (server.accept())

Então, é criado um novo objeto de conexão, que vai gerenciar a resposta do servidor.

Na conexão (Connection.java), é lido o método usado (GET) e o recurso sendo acessado (parte da URL no navegador, depois do localhost:8080`/isso`).

Dependendo do recurso sendo acessado, o servidor responde de acordo.

Acessando o HTML na pasta resources, substituindo o que deve ser substituido e respondendo os bytes no OutputStream.

Ao enviar o formulário, um log é gerado no arquivo de log `log.txt`.

Tão como o assento é marcado como reservado.

É no Logger.java que a implementação do Produtores-Consumidores está.

Caso um recurso não exista, a página 404.html é exibida por padrão.
