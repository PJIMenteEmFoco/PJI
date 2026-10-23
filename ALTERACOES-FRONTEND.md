# Ajustes visuais de 23 de outubro de 2026

As alterações estão limitadas a HTML, CSS e JavaScript de apresentação. Nenhuma classe Java, controller, service, repository, entidade, configuração, dependência Maven ou regra de segurança foi modificada.

## O que mudou

- Os cartões de atividades do aluno e os cartões de alunos da equipe usam a mesma animação de hover. A preferência por movimento reduzido continua sendo respeitada.
- A barra de Meu progresso usa o laranja `#E69C2E`. O valor e o máximo da barra não foram alterados.
- Os 39 templates dos perfis aluno, professor e psicopedagogo têm acesso ao painel de notificações. O painel abre sobre a tela atual e também padroniza a página de notificações já existente.
- Títulos, parágrafos, campos e ações dos relatórios têm espaçamentos e quebras de linha mais regulares.

## Arquivos para integração

- `src/main/resources/static/css/ajustes-front.css`: estilos novos, sem substituir as folhas de estilo originais.
- `src/main/resources/static/script/ajustes-front.js`: abertura e fechamento do painel e uso do seletor visual já existente.
- `src/main/resources/static/script/mef-notificacoes-backend.js`: apresentação das notificações, pesquisa e reutilização no painel. Esse arquivo é JavaScript do navegador; a função de requisições HTTP e CSRF foi preservada.
- Templates dos três perfis: referências aos novos arquivos, acesso a notificações e metadados CSRF onde faltavam. Expressões, atributos Thymeleaf originais e formulários foram preservados.

O painel utiliza as rotas existentes: `GET /api/notificacoes`, `PATCH /api/notificacoes/{id}/lida`, `PATCH /api/notificacoes/lidas` e `PATCH /api/notificacoes/preferencia`. A preferência continua enviando `{ "receber": true/false }`. Não foram criadas rotas no servidor. A rota de página de notificações declarada no controller do ZIP é a do professor; o painel permite o acesso nos demais perfis pela mesma API autenticada.

## Validação e limites

Foram verificados os acessos nos 39 templates, o hover dos cartões, a cor da barra, os filtros, a leitura individual e em lote, a preferência, o tratamento de erros, o token CSRF, o retorno de foco e o layout em 1365, 390 e 360 pixels, nos dois temas. Os arquivos do backend e os bindings originais dos 45 templates foram comparados com o ZIP recebido.

A validação da interface usou uma pré-visualização com respostas simuladas da API; não executou o Spring Boot nem o banco de dados. O ZIP original referencia recursos que não estão incluídos nele, como `/script/mef-comum.js`, `/script/mef-relatorios-backend.js` e `/css/mef-integracao.css`. Esses componentes da integração original não foram recriados nem substituídos. Os ajustes de apresentação ficam prontos para uso junto da versão completa da aplicação.
