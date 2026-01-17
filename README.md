# Sistema de Monitorização Ambiental - Universidade de Évora

Sistema distribuído para monitorizar temperatura e humidade em diversas áreas da Universidade de Évora, utilizando múltiplos protocolos de comunicação (MQTT, gRPC e REST).

## 📋 Índice

- [Estrutura do Projeto](#estrutura-do-projeto)
- [Requisitos](#requisitos)
- [Configuração da Base de Dados](#configuração-da-base-de-dados)
- [Configuração do MQTT Broker](#configuração-do-mqtt-broker)
- [Compilação](#compilação)
- [Execução](#execução)
- [Endpoints da API REST](#endpoints-da-api-rest)
- [Testes](#testes)

## 📁 Estrutura do Projeto

```
sd-t01-XXXXX-YYYYY/
├── pom.xml                      # POM pai
├── docker-compose.yml           # PostgreSQL + ActiveMQ
├── proto/
│   └── metrics.proto           # Definição gRPC
├── server/                      # Servidor Spring Boot
│   ├── pom.xml
│   └── src/main/java/pt/uevora/sd/server/
│       ├── ServerApplication.java
│       ├── model/              # Entidades JPA
│       ├── repository/         # Repositories
│       ├── service/            # Lógica de negócio
│       ├── controller/         # REST Controllers
│       ├── dto/                # Data Transfer Objects
│       ├── grpc/               # Serviço gRPC
│       ├── mqtt/               # MQTT Handler
│       └── config/             # Configurações
├── client-mqtt/                 # Simulador MQTT
├── client-grpc/                 # Simulador gRPC
├── client-rest/                 # Simulador REST
└── admin-cli/                   # Cliente de administração
```

## 🔧 Requisitos

- **Java 17** ou superior
- **Maven 3.8+**
- **Docker** e **Docker Compose** (para PostgreSQL e ActiveMQ)
- **PostgreSQL 16** (ou usar Docker)
- **ActiveMQ Artemis** (ou usar Docker)

## 🗄️ Configuração da Base de Dados

### Opção 1: Usando Docker (Recomendado)

1. Inicie os serviços usando Docker Compose:

```bash
docker-compose up -d
```

Isso irá iniciar:
- PostgreSQL na porta `5432`
- ActiveMQ na porta `1883` (MQTT) e `8161` (Web Console)
- PgAdmin na porta `5050` (opcional)

### Opção 2: PostgreSQL Manual

1. Instale o PostgreSQL 16

2. Crie a base de dados:

```sql
CREATE DATABASE environmental_monitoring;
```

3. Crie o utilizador:

```sql
CREATE USER uevora WITH PASSWORD 'uevora2026';
GRANT ALL PRIVILEGES ON DATABASE environmental_monitoring TO uevora;
```

4. Configure as permissões:

```sql
\c environmental_monitoring
GRANT ALL ON SCHEMA public TO uevora;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO uevora;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO uevora;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO uevora;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO uevora;
```

5. As tabelas serão criadas automaticamente pelo Hibernate na primeira execução.

### Estrutura das Tabelas

O JPA/Hibernate irá criar automaticamente as seguintes tabelas:

**Tabela `devices`:**
- `id` (VARCHAR, PK) - Identificador único do dispositivo
- `protocol` (VARCHAR) - Tipo de protocolo (MQTT, GRPC, REST)
- `room` (VARCHAR) - Sala
- `department` (VARCHAR) - Departamento
- `floor` (VARCHAR) - Piso
- `building` (VARCHAR) - Edifício
- `status` (VARCHAR) - Estado (ACTIVE, INACTIVE)
- `created_at` (TIMESTAMP) - Data de criação
- `updated_at` (TIMESTAMP) - Data de atualização

**Tabela `metrics`:**
- `id` (BIGSERIAL, PK) - ID auto-incrementado
- `device_id` (VARCHAR, FK) - Referência ao dispositivo
- `temperature` (DOUBLE) - Temperatura em °C
- `humidity` (DOUBLE) - Humidade em %
- `timestamp` (TIMESTAMP) - Data/hora da leitura
- `received_at` (TIMESTAMP) - Data/hora de recepção
- `room` (VARCHAR) - Sala (desnormalizado)
- `department` (VARCHAR) - Departamento (desnormalizado)
- `floor` (VARCHAR) - Piso (desnormalizado)
- `building` (VARCHAR) - Edifício (desnormalizado)

## 📡 Configuração do MQTT Broker

### Usando Docker (incluído no docker-compose.yml)

O ActiveMQ Artemis já está configurado com:
- Porta MQTT: `1883`
- Utilizador: `admin`
- Password: `admin`

### ActiveMQ Manual

1. Baixe o ActiveMQ Artemis: https://activemq.apache.org/components/artemis/

2. Crie uma instância:

```bash
./bin/artemis create mybroker
```

3. Configure as credenciais no ficheiro `broker.xml`

4. Inicie o broker:

```bash
./bin/artemis run
```

## 🔨 Compilação

Na raiz do projeto, execute:

```bash
mvn clean install
```

Isso irá:
1. Compilar todos os módulos
2. Gerar código a partir do ficheiro `.proto` (gRPC)
3. Criar JARs executáveis para cada módulo

## 🚀 Execução

### 1. Iniciar a Infraestrutura (PostgreSQL + ActiveMQ)

```bash
docker-compose up -d
```

### 2. Iniciar o Servidor

```bash
cd server
mvn spring-boot:run
```

Ou usando o JAR:

```bash
java -jar server/target/server-1.0.0.jar
```

O servidor estará disponível em:
- REST API: `http://localhost:8080`
- gRPC: `localhost:9090`
- MQTT: Conectado em `tcp://localhost:1883`

### 3. Registar Dispositivos

Antes de iniciar os simuladores, é necessário registar os dispositivos no sistema.

**Opção A: Usar o admin-cli**

```bash
cd admin-cli
mvn exec:java -Dexec.mainClass="pt.uevora.sd.admin.AdminCLI"
```

Escolha a opção "Gestão de Dispositivos" → "Adicionar novo dispositivo"

**Opção B: Usar curl**

```bash
# Registar sensor MQTT
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "id": "mqtt-sensor-001",
    "protocol": "MQTT",
    "room": "A101",
    "department": "Informatica",
    "floor": "Piso1",
    "building": "EdificioII",
    "status": "ACTIVE"
  }'

# Registar gateway gRPC
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "id": "grpc-gateway-001",
    "protocol": "GRPC",
    "room": "D301",
    "department": "Engenharia",
    "floor": "Piso3",
    "building": "EdificioIV",
    "status": "ACTIVE"
  }'

# Registar dispositivo REST
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "id": "rest-device-001",
    "protocol": "REST",
    "room": "F204",
    "department": "Biologia",
    "floor": "Piso2",
    "building": "EdificioVI",
    "status": "ACTIVE"
  }'
```

### 4. Iniciar os Simuladores

**Terminal 1 - Simulador MQTT:**

```bash
cd client-mqtt
mvn exec:java -Dexec.mainClass="pt.uevora.sd.client.mqtt.MqttClientSimulator"
```

**Terminal 2 - Simulador gRPC:**

```bash
cd client-grpc
mvn exec:java -Dexec.mainClass="pt.uevora.sd.client.grpc.GrpcClientSimulator"
```

**Terminal 3 - Simulador REST:**

```bash
cd client-rest
mvn exec:java -Dexec.mainClass="pt.uevora.sd.client.rest.RestClientSimulator"
```

### 5. Cliente de Administração

```bash
cd admin-cli
mvn exec:java -Dexec.mainClass="pt.uevora.sd.admin.AdminCLI"
```

## 📡 Endpoints da API REST

### Gestão de Dispositivos

- **POST** `/api/devices` - Criar dispositivo
- **GET** `/api/devices` - Listar todos os dispositivos
- **GET** `/api/devices/{id}` - Obter dispositivo por ID
- **PUT** `/api/devices/{id}` - Atualizar dispositivo
- **DELETE** `/api/devices/{id}` - Eliminar dispositivo

### Ingestão de Métricas

- **POST** `/api/metrics/ingest` - Receber métricas via REST

### Consulta de Métricas

- **GET** `/api/metrics/average?level={nivel}&id={id}&from={data}&to={data}`
    - `level`: sala, departamento, piso, edificio
    - `id`: ID da entidade
    - `from`, `to`: Intervalo de datas (opcional)

- **GET** `/api/metrics/raw?deviceId={id}&from={data}&to={data}`
    - Retorna métricas brutas de um dispositivo

## 🧪 Testes

### Testar Ingestão REST

```bash
curl -X POST http://localhost:8080/api/metrics/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "rest-device-001",
    "temperature": 22.5,
    "humidity": 65.0,
    "timestamp": "2024-12-08T14:30:00"
  }'
```

### Testar Consulta de Médias

```bash
# Média por sala
curl "http://localhost:8080/api/metrics/average?level=sala&id=A101"

# Média por departamento
curl "http://localhost:8080/api/metrics/average?level=departamento&id=Informatica"

# Média por edifício com intervalo
curl "http://localhost:8080/api/metrics/average?level=edificio&id=EdificioII&from=2024-12-08T00:00:00&to=2024-12-08T23:59:59"
```

### Testar Métricas Brutas

```bash
curl "http://localhost:8080/api/metrics/raw?deviceId=mqtt-sensor-001"
```

## 🔍 Monitorização

### Logs do Servidor

Os logs do servidor mostrarão:
- Métricas recebidas via MQTT, gRPC e REST
- Validações de dispositivos
- Erros e avisos

### Web Consoles

- **ActiveMQ Console**: http://localhost:8161 (admin/admin)
- **PgAdmin**: http://localhost:5050 (admin@uevora.pt/admin)

## 🛠️ Resolução de Problemas

### Erro de conexão PostgreSQL

Verifique se o PostgreSQL está a correr:

```bash
docker-compose ps
```

### Erro de conexão MQTT

Verifique se o ActiveMQ está ativo:

```bash
docker logs uevora-activemq
```

### Métricas descartadas

Certifique-se que o dispositivo está registado e com estado ACTIVE:

```bash
curl http://localhost:8080/api/devices
```

## 📊 Análise de Performance

Os três protocolos têm características diferentes:

- **MQTT**: Assíncrono, baixo overhead, ideal para sensores IoT simples
- **gRPC**: Síncrono, alto desempenho, protocolo binário eficiente
- **REST**: Síncrono, HTTP/JSON, universal e fácil de integrar

Consulte o relatório para análise detalhada de performance.

## 👥 Autores

- Aluno 1: [Número]
- Aluno 2: [Número]

## 📝 Licença

Projeto académico - Universidade de Évora - 2024/2025