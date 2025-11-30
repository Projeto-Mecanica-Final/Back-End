# 🔧 Sistema de Gestão para Oficina Mecânica

API REST desenvolvida com Spring Boot para gerenciamento completo de oficinas mecânicas, incluindo controle de estoque, ordens de serviço, agendamentos, vendas e faturamento.

## 📋 Funcionalidades

- **Gestão de Clientes e Veículos**
    - Cadastro completo de clientes (CPF, telefone, email, endereço)
    - Registro de veículos por cliente (placa, modelo, marca, ano)

- **Ordens de Serviço e Orçamentos**
    - Criação de orçamentos para aprovação do cliente
    - Conversão de orçamentos em ordens de serviço
    - Controle de status (Agendado, Em Andamento, Concluído, Cancelado)
    - Registro de peças e serviços utilizados
    - Diagnóstico e valor de mão de obra

- **Agendamentos**
    - Agenda de mecânicos
    - Validação de disponibilidade
    - Sincronização automática com ordens de serviço

- **Controle de Estoque**
    - Cadastro de produtos/peças
    - Controle de quantidade mínima
    - Alertas de estoque baixo
    - Baixa automática ao vincular em OS

- **Vendas no Balcão**
    - Venda direta de peças/produtos
    - Múltiplas formas de pagamento
    - Geração automática de faturamento

- **Faturamento**
    - Consolidação de vendas e serviços
    - Relatórios por período
    - Total faturado por dia/mês

- **Autenticação e Autorização**
    - Login com email/senha (JWT)
    - Login com Google OAuth2
    - Controle de perfis: Admin, Atendente, Mecânico

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
    - Spring Web
    - Spring Data JPA
    - Spring Security
    - Spring OAuth2 Client
- **PostgreSQL** - Banco de dados
- **JWT (jjwt 0.11.5)** - Autenticação
- **Lombok** - Redução de boilerplate
- **Hibernate Validator** - Validações
- **Springdoc OpenAPI** - Documentação Swagger
- **Maven** - Gerenciamento de dependências

## 📁 Estrutura do Projeto

```
src/main/java/com/oficinamecanica/OficinaMecanica/
├── config/              # Configurações (Security, Swagger, DataLoader)
├── controllers/         # Endpoints REST
├── dto/                 # Objetos de transferência de dados
├── enums/              # Enumerações (Status, Roles, FormaPagamento)
├── exceptions/         # Tratamento global de exceções
├── models/             # Entidades JPA
├── repositories/       # Repositories Spring Data
├── security/           # JWT, OAuth2, UserDetails
└── services/           # Lógica de negócio
```

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- PostgreSQL 12+
- Maven 3.6+

### Configuração do Banco de Dados

1. Crie um banco PostgreSQL:
```sql
CREATE DATABASE mecanica;
```

2. Configure as credenciais em `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mecanica
spring.datasource.username=postgres
spring.datasource.password=root
```

### Executar a Aplicação

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/oficina-mecanica-api.git

# Entre no diretório
cd oficina-mecanica-api

# Execute com Maven
./mvnw spring-boot:run

# Ou no Windows
mvnw.cmd spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8084`

## 📚 Documentação da API

Acesse a documentação Swagger em:
```
http://localhost:8084/swagger-ui.html
```

## 🔐 Usuários Padrão

O sistema cria automaticamente 3 usuários para testes:

| Email | Senha | Perfil |
|-------|-------|--------|
| admin@oficina.com | senha123 | ADMIN |
| atendente@oficina.com | senha123 | ATENDENTE |
| mecanico@oficina.com | senha123 | MECANICO |

## 🔑 Autenticação

### Login Local (JWT)
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@oficina.com",
  "senha": "senha123"
}
```

**Resposta:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "usuario": {
    "cdUsuario": 1,
    "nmUsuario": "João Admin Silva",
    "email": "admin@oficina.com",
    "roles": ["ROLE_ADMIN"]
  }
}
```

### Login com Google OAuth2
```
GET /oauth2/authorization/google
```

## 📝 Principais Endpoints

### Clientes
- `GET /api/clientes` - Listar clientes ativos
- `POST /api/clientes` - Cadastrar cliente
- `PUT /api/clientes/{id}` - Atualizar cliente
- `DELETE /api/clientes/{id}` - Inativar cliente

### Ordens de Serviço
- `GET /api/ordens-servico` - Listar ordens
- `POST /api/ordens-servico` - Criar ordem/orçamento
- `PATCH /api/ordens-servico/{id}/iniciar` - Iniciar serviço
- `PATCH /api/ordens-servico/{id}/concluir` - Concluir e faturar

### Agendamentos
- `GET /api/agendamentos/futuros` - Agendamentos futuros
- `POST /api/agendamentos` - Criar agendamento
- `PATCH /api/agendamentos/{id}/status` - Atualizar status

### Produtos
- `GET /api/produtos` - Listar produtos ativos
- `GET /api/produtos/estoque-baixo` - Produtos com estoque baixo
- `POST /api/produtos` - Cadastrar produto

### Vendas
- `POST /api/vendas` - Realizar venda no balcão
- `GET /api/vendas/total-dia` - Total vendido no dia

## 🔒 Controle de Acesso

| Endpoint | ADMIN | ATENDENTE | MECÂNICO |
|----------|-------|-----------|----------|
| Usuários (POST/DELETE) | ✅ | ❌ | ❌ |
| Clientes (POST/PUT/DELETE) | ✅ | ✅ | ❌ |
| Clientes (GET) | ✅ | ✅ | ✅ |
| Ordens de Serviço | ✅ | ✅ | ✅ (limitado) |
| Agendamentos | ✅ | ✅ | ✅ |
| Vendas | ✅ | ✅ | ❌ |
| Faturamento | ✅ | ❌ | ❌ |





