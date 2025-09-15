# CertifAI Back-end

---

Back-end da aplicação web CertifAI, responsável por gerenciar autenticação, autorização, regras de negócio e persistência de dados. Desenvolvido com **Spring Boot**, o projeto utiliza práticas modernas de desenvolvimento e segurança, incluindo OAuth2, JWT, OpenAPI, JPA, e integração com serviços como o **SendGrid**.


## 👥 Equipe

- [Luana Vitória](https://github.com/LuBrito371)
- [Matheus Marcos](https://github.com/Matheusxr77)
- [Mayara Karoline](https://github.com/mayeufraferreira)

## 🚀 Tecnologias Utilizadas

### Core
- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Web** – API REST
- **Spring Security** – Autenticação e autorização
- **Spring Data JPA** – Acesso a banco de dados
- **PostgreSQL** – Banco de dados relacional
- **JWT & OAuth2** – Segurança com tokens e login social (Google)
- **SendGrid** – Envio de e-mails
- **Thymeleaf** – Template Engine (usado em partes do auth)
- **OpenAPI/Swagger** – Documentação interativa da API

### Utilitários e Bibliotecas
- **Lombok** – Redução de boilerplate
- **MapStruct** – Mapeamento entre DTOs e entidades
- **Caffeine** – Cache de alta performance
- **Springdoc** – Geração automática da documentação OpenAPI
- **JUnit & Spring Security Test** – Testes unitários e de segurança

---

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── br/com/certifai/
│   │       ├── config/         # Configurações de segurança, CORS, Swagger, etc.
│   │       ├── controller/     # Endpoints REST da aplicação
│   │       ├── dto/            # Objetos de transferência de dados
│   │       ├── enums/          # Enumerações utilizadas na lógica de negócio
│   │       ├── exception/      # Tratamento de exceções globais
│   │       ├── mappers/        # Conversores MapStruct entre entidades e DTOs
│   │       ├── model/          # Entidades JPA
│   │       ├── repository/     # Interfaces de acesso ao banco de dados
│   │       ├── requests/       # Objetos de entrada para os endpoints
│   │       ├── response/       # Objetos de saída dos endpoints
│   │       ├── service/        # Regras de negócio e lógica de aplicação
│   │       └── util/           # Utilitários gerais
│   └── resources/
│       ├── application.properties  # Configurações da aplicação
└── test/                         # Testes automatizados
```

## 🏗️ Arquitetura

O back-end CertifAI segue uma arquitetura modular baseada no padrão MVC + Service Layer, com forte separação de responsabilidades e organização por pacotes funcionais.

- **Model** (model/) – Entidades JPA que representam tabelas do banco de dados.
- **DTO** (dto/, requests/, response/) – Objetos de transferência de dados entre camadas, com propósito específico de entrada (requests) e saída (response).
- **Controller** (controller/) – Pontos de entrada da API (REST controllers).
- **Service** (service/) – Camada intermediária de lógica de negócio.
   - interfaces/ – Contratos (interfaces) de serviços.
   - impl/ – Implementações concretas dos serviços.
- **Repository** (repository/) – Interfaces Spring Data JPA para acesso ao banco.
- **Security** (config/security/) – Toda configuração de autenticação, JWT, OAuth2 e controle de acesso.
- **Exception** (exception/) – Tratamento global de exceções com detalhes customizados.
- **Mappers** (mappers/) – Conversão entre entidades e DTOs via MapStruct.
- **Util** (util/) – Classes utilitárias como JwtUtil.
- **Validation** (config/validation/) – Configurações de validação assíncrona.
- **WebConfig** (config/web/) – Configurações CORS e Swagger/OpenAPI.

### Características Arquiteturais 
- Separação rigorosa por responsabilidade
- Autenticação com **OAuth2 (Google)** + **JWT**
- Injeção de dependências com **Spring IoC**
- Camada de serviços testável e reaproveitável
- Uso de **DTOs** para controle de exposição de dados
- Mapeamento automático entre entidade - **DTO com MapStruct**
- Configurações desacopladas por ambiente via application.properties
- Suporte a **Swagger** para documentação automática

## 🔐 Segurança

O sistema implementa:

- Autenticação com **JWT** e login social via **Google OAuth**
- Redirecionamento por função (ADMIN, PROFESSOR, ESTUDANTE)
- Logout customizado
- Configurações de **CORS** e **HTTPS**

## 🧪 Testes

Inclui cobertura de testes com:

- **JUnit 5**
- **Spring Security Test**

## 📋 Pré-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL
- Variáveis de ambiente (ou .env) com:
  - DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD
  - SENDGRID_API_KEY, JWT_SECRET
  - CLIENT_ID, CLIENT_SECRET (Google OAuth)

## 🚀 Como Executar

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/certifai-backend.git
cd certifai-backend
```

### 2. Instale as dependências
```bash
mvn versions:use-latest-versions
mvn clean install
```

### 3. Execute o projeto
```bash
./mvnw spring-boot:run
```

## 🌐 Deploy

A aplicação está configurada para deploy no Render.com: (https://certifai-backend.onrender.com/certifai)

## 🧭 Integração com Front-end

Este back-end se comunica com o front-end React hospedado em: (http://localhost:5173/) ou (https://certifai-front-ruby.vercel.app)

## Informações sobre o Projeto

- [Termo de Abertura do Projeto (TAP)](https://drive.google.com/file/d/1duV6n9zu65UGFHoc0tcblKy3vAvIQrbE/view?usp=sharing)
- [Estudo de Viabilidade](https://drive.google.com/file/d/10sI4_1nXo9czEw0gfo5yPX--armzI8Ae/view?usp=sharing)
- [Plano de Projeto](https://drive.google.com/file/d/1iVBfyaF3_tIIAzdl543iz5sF2q-njH8U/view?usp=sharing)