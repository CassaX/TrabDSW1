# Sistema para compra/venda de veículos

## Integrantes
- Matheus Henrique Cassatti
- Nathalia Brasilino Gimenes
- Pedro Henrique Ghiotto

## Requisitos Implementados
### Cadastros Básicos
-  **R1**: CRUD de clientes (admin only) (Nathalia)
-  **R2**: CRUD de lojas (admin only) (Nathalia)
-  Cadastro de clientes: email, senha, CPF, nome, telefone, sexo, data nascimento (Nathalia)
-  Cadastro de lojas: email, senha, CNPJ, nome, descrição (Nathalia)
-  **R3**: Cadastro de veículos por lojas autenticadas (placa, modelo, chassi, ano, km, descrição, valor) (Pedro)
-  **R4**: Listagem pública de veículos com filtro por modelo (Pedro)
-  **R6**: Listagem de veículos por loja (apenas para a loja dona) (Pedro)
-  **R5**: Proposta de compra por clientes autenticados (valor, condições, data) (Nathalia)
-  **R7**: Listagem de propostas por cliente com status (ABERTO, ACEITO, NÃO ACEITO) (Matheus)
-  **R8**: Processamento de propostas por lojas (com notificação por email) (Matheus)
-  **R9**: Internacionalização (Português e Inglês) (Matheus)
-  **R10**: Validação de formulários e tratamento de erros (Nathalia)
-  Upload de até 10 fotos por veículo (Pedro)
-  Sistema de autenticação segura (roles: ADMIN, LOJA, CLIENTE) (Matheus)
-  Envio de emails automáticos (confirmações e notificações) (Matheus)

## Processo de Desenvolvimento
- Durante a integração das partes desenvolvidas separadamente, fizemos várias alterações em conjunto nos arquivos para garantir o funcionamento do sistema. 
- Todos os membros trabalharam juntos para ajustar os controladores, serviços e frontend.

## Como Executar o Projeto
Para executar o sistema:

1. Clone o repositório: `git clone https://github.com/CassaX/TrabDSW1.git`
2. Execute o projeto usando Maven: `mvn spring-boot:run`
3. Acesse o sistema em: `http://localhost:8080`

**Credencial de administrador pré-cadastrada:** (Ou altere com suas credenciais de banco de dados e e-mail no `application.properties`)

Email: `admin@veiculos.com`  
Senha: `admin123`

*O sistema já inclui alguns dados iniciais para facilitar os testes.*

## REST API
### Exemplos para Clientes
- POST `http://localhost:8080/api/clientes`
  ``` json
  {
    "nome": "Cliente Teste",
    "email": "teste@email.com",
    "senha": "123456",
    "CPF": "123.456.789-00",
    "telefone": "(11) 91234-5678",
    "sexo": "Feminino",
    "dataNascimento": "2000-05-15"
  }

- GET `http://localhost:8080/api/clientes`
- GET `http://localhost:8080/api/clientes/1`
- PUT `http://localhost:8080/api/clientes/6`
  ``` json
  {
    "nome": "Cliente teste atualizado",
    "email": "teste@email.com",
    "senha": "123456",
    "CPF": "123.456.789-00",
    "telefone": "(11) 90000-0000",
    "sexo": "Feminino",
    "dataNascimento": "2000-05-15"
  }
- DELETE `http://localhost:8080/api/clientes/6`
