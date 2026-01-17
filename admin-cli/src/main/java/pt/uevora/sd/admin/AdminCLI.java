// Caminho: admin-cli/src/main/java/pt/uevora/sd/admin/AdminCLI.java

package pt.uevora.sd.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.vandermeer.asciitable.AsciiTable;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Cliente de Administração CLI para o Sistema de Monitorização Ambiental
 */
public class AdminCLI {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper objectMapper = createObjectMapper();
    private static final Scanner scanner = new Scanner(System.in);
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║   Sistema de Monitorização Ambiental - UÉvora         ║");
        System.out.println("║   Cliente de Administração                             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        boolean running = true;
        while (running) {
            running = showMainMenu();
        }

        scanner.close();
        try {
            httpClient.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar cliente HTTP: " + e.getMessage());
        }
    }

    /**
     * Menu Principal
     */
    private static boolean showMainMenu() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("          MENU PRINCIPAL");
        System.out.println("═══════════════════════════════════════");
        System.out.println("1. Gestão de Dispositivos");
        System.out.println("2. Consulta de Métricas");
        System.out.println("3. Estatísticas do Sistema");
        System.out.println("4. Sair");
        System.out.println("═══════════════════════════════════════");
        System.out.print("Escolha uma opção: ");

        int choice = readInt();
        System.out.println();

        switch (choice) {
            case 1:
                deviceManagementMenu();
                break;
            case 2:
                metricsQueryMenu();
                break;
            case 3:
                systemStatistics();
                break;
            case 4:
                System.out.println("A sair... Até breve!");
                return false;
            default:
                System.out.println("Opção inválida!");
        }

        return true;
    }

    /**
     * Menu de Gestão de Dispositivos
     */
    private static void deviceManagementMenu() {
        System.out.println("\n───────────────────────────────────────");
        System.out.println("     GESTÃO DE DISPOSITIVOS");
        System.out.println("───────────────────────────────────────");
        System.out.println("1. Listar todos os dispositivos");
        System.out.println("2. Adicionar novo dispositivo");
        System.out.println("3. Atualizar dispositivo existente");
        System.out.println("4. Remover dispositivo");
        System.out.println("5. Visualizar detalhes de um dispositivo");
        System.out.println("6. Voltar");
        System.out.println("───────────────────────────────────────");
        System.out.print("Escolha uma opção: ");

        int choice = readInt();
        System.out.println();

        switch (choice) {
            case 1:
                listAllDevices();
                break;
            case 2:
                addNewDevice();
                break;
            case 3:
                updateDevice();
                break;
            case 4:
                removeDevice();
                break;
            case 5:
                viewDeviceDetails();
                break;
            case 6:
                return;
            default:
                System.out.println("❌ Opção inválida!");
        }
    }

    /**
     * Menu de Consulta de Métricas
     */
    private static void metricsQueryMenu() {
        System.out.println("\n───────────────────────────────────────");
        System.out.println("      CONSULTA DE MÉTRICAS");
        System.out.println("───────────────────────────────────────");
        System.out.println("1. Consultar por Sala");
        System.out.println("2. Consultar por Departamento");
        System.out.println("3. Consultar por Piso");
        System.out.println("4. Consultar por Edifício");
        System.out.println("5. Métricas brutas de um dispositivo");
        System.out.println("6. Voltar");
        System.out.println("───────────────────────────────────────");
        System.out.print("Escolha uma opção: ");

        int choice = readInt();
        System.out.println();

        switch (choice) {
            case 1:
                queryMetricsByLevel("sala", "Sala");
                break;
            case 2:
                queryMetricsByLevel("departamento", "Departamento");
                break;
            case 3:
                queryMetricsByLevel("piso", "Piso");
                break;
            case 4:
                queryMetricsByLevel("edificio", "Edifício");
                break;
            case 5:
                queryRawMetrics();
                break;
            case 6:
                return;
            default:
                System.out.println("❌ Opção inválida!");
        }
    }

    /**
     * Listar todos os dispositivos
     */
    private static void listAllDevices() {
        try {
            HttpGet request = new HttpGet(BASE_URL + "/devices");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());
                List<Map<String, Object>> devices = objectMapper.readValue(json, new TypeReference<>() {});

                if (devices.isEmpty()) {
                    System.out.println("ℹ️  Nenhum dispositivo registado.");
                    return;
                }

                AsciiTable table = new AsciiTable();
                table.addRule();
                table.addRow("ID", "Protocolo", "Sala", "Departamento", "Piso", "Edifício", "Estado");
                table.addRule();

                for (Map<String, Object> device : devices) {
                    table.addRow(
                            device.get("id"),
                            device.get("protocol"),
                            device.get("room"),
                            device.get("department"),
                            device.get("floor"),
                            device.get("building"),
                            device.get("status")
                    );
                    table.addRule();
                }

                System.out.println(table.render());
                System.out.println("Total de dispositivos: " + devices.size());
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar dispositivos: " + e.getMessage());
        }
    }

    /**
     * Adicionar novo dispositivo
     */
    private static void addNewDevice() {
        System.out.println("─── Adicionar Novo Dispositivo ───");

        System.out.print("ID do dispositivo: ");
        String id = scanner.nextLine();

        System.out.print("Protocolo (MQTT/GRPC/REST): ");
        String protocol = scanner.nextLine().toUpperCase();

        System.out.print("Sala: ");
        String room = scanner.nextLine();

        System.out.print("Departamento: ");
        String department = scanner.nextLine();

        System.out.print("Piso: ");
        String floor = scanner.nextLine();

        System.out.print("Edifício: ");
        String building = scanner.nextLine();

        try {
            Map<String, Object> device = new HashMap<>();
            device.put("id", id);
            device.put("protocol", protocol);
            device.put("room", room);
            device.put("department", department);
            device.put("floor", floor);
            device.put("building", building);
            device.put("status", "ACTIVE");

            String json = objectMapper.writeValueAsString(device);

            HttpPost request = new HttpPost(BASE_URL + "/devices");
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 201) {
                    System.out.println("✅ Dispositivo criado com sucesso!");
                } else {
                    System.out.println("❌ Erro ao criar dispositivo. Código: " + response.getCode());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    /**
     * Atualizar dispositivo
     */
    private static void updateDevice() {
        System.out.print("ID do dispositivo a atualizar: ");
        String id = scanner.nextLine();

        System.out.print("Novo Protocolo (MQTT/GRPC/REST): ");
        String protocol = scanner.nextLine().toUpperCase();

        System.out.print("Nova Sala: ");
        String room = scanner.nextLine();

        System.out.print("Novo Departamento: ");
        String department = scanner.nextLine();

        System.out.print("Novo Piso: ");
        String floor = scanner.nextLine();

        System.out.print("Novo Edifício: ");
        String building = scanner.nextLine();

        System.out.print("Estado (ACTIVE/INACTIVE): ");
        String status = scanner.nextLine().toUpperCase();

        try {
            Map<String, Object> device = new HashMap<>();
            device.put("id", id);
            device.put("protocol", protocol);
            device.put("room", room);
            device.put("department", department);
            device.put("floor", floor);
            device.put("building", building);
            device.put("status", status);

            String json = objectMapper.writeValueAsString(device);

            HttpPut request = new HttpPut(BASE_URL + "/devices/" + id);
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    System.out.println("✅ Dispositivo atualizado com sucesso!");
                } else if (response.getCode() == 404) {
                    System.out.println("❌ Dispositivo não encontrado!");
                } else {
                    System.out.println("❌ Erro ao atualizar dispositivo. Código: " + response.getCode());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    /**
     * Remover dispositivo
     */
    private static void removeDevice() {
        System.out.print("ID do dispositivo a remover: ");
        String id = scanner.nextLine();

        System.out.print("Tem certeza? (S/N): ");
        String confirm = scanner.nextLine();

        if (!confirm.equalsIgnoreCase("S")) {
            System.out.println("Operação cancelada.");
            return;
        }

        try {
            HttpDelete request = new HttpDelete(BASE_URL + "/devices/" + id);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 204) {
                    System.out.println("✅ Dispositivo removido com sucesso!");
                } else if (response.getCode() == 404) {
                    System.out.println("❌ Dispositivo não encontrado!");
                } else {
                    System.out.println("❌ Erro ao remover dispositivo. Código: " + response.getCode());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    /**
     * Visualizar detalhes de um dispositivo
     */
    private static void viewDeviceDetails() {
        System.out.print("ID do dispositivo: ");
        String id = scanner.nextLine();

        try {
            HttpGet request = new HttpGet(BASE_URL + "/devices/" + id);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    Map<String, Object> device = objectMapper.readValue(json, new TypeReference<>() {});

                    System.out.println("\n═══ Detalhes do Dispositivo ═══");
                    System.out.println("ID: " + device.get("id"));
                    System.out.println("Protocolo: " + device.get("protocol"));
                    System.out.println("Sala: " + device.get("room"));
                    System.out.println("Departamento: " + device.get("department"));
                    System.out.println("Piso: " + device.get("floor"));
                    System.out.println("Edifício: " + device.get("building"));
                    System.out.println("Estado: " + device.get("status"));
                    System.out.println("Criado em: " + device.get("createdAt"));
                    System.out.println("Atualizado em: " + device.get("updatedAt"));
                    System.out.println("═══════════════════════════════");
                } else if (response.getCode() == 404) {
                    System.out.println("❌ Dispositivo não encontrado!");
                } else {
                    System.out.println("❌ Erro. Código: " + response.getCode());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    /**
     * Consultar métricas por nível (sala, departamento, piso, edifício)
     */
    private static void queryMetricsByLevel(String level, String levelName) {
        System.out.print("ID da " + levelName + ": ");
        String id = scanner.nextLine();

        System.out.print("Especificar intervalo de datas? (S/N): ");
        String useDates = scanner.nextLine();

        String url = BASE_URL + "/metrics/average?level=" + level + "&id=" + id;

        if (useDates.equalsIgnoreCase("S")) {
            System.out.print("Data início (yyyy-MM-ddTHH:mm:ss): ");
            String from = scanner.nextLine();
            System.out.print("Data fim (yyyy-MM-ddTHH:mm:ss): ");
            String to = scanner.nextLine();
            url += "&from=" + from + "&to=" + to;
        }

        try {
            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    Map<String, Object> result = objectMapper.readValue(json, new TypeReference<>() {});

                    System.out.println("\n═══ Métricas Agregadas ═══");
                    System.out.println("Nível: " + result.get("level"));
                    System.out.println("ID: " + result.get("id"));
                    System.out.println("Temperatura Média: " + String.format("%.2f", result.get("avgTemperature")) + "°C");
                    System.out.println("Humidade Média: " + String.format("%.2f", result.get("avgHumidity")) + "%");
                    System.out.println("Período: " + result.get("from") + " até " + result.get("to"));
                    System.out.println("Número de métricas: " + result.get("count"));
                    System.out.println("═════════════════════════");
                } else {
                    System.out.println("❌ Erro ao consultar métricas. Código: " + response.getCode());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    /**
     * Consultar métricas brutas de um dispositivo
     */
    private static void queryRawMetrics() {
        System.out.print("ID do dispositivo: ");
        String deviceId = scanner.nextLine();

        System.out.print("Especificar intervalo de datas? (S/N): ");
        String useDates = scanner.nextLine();

        String url = BASE_URL + "/metrics/raw?deviceId=" + deviceId;

        if (useDates.equalsIgnoreCase("S")) {
            System.out.print("Data início (yyyy-MM-ddTHH:mm:ss): ");
            String from = scanner.nextLine();
            System.out.print("Data fim (yyyy-MM-ddTHH:mm:ss): ");
            String to = scanner.nextLine();
            url += "&from=" + from + "&to=" + to;
        }

        try {
            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    List<Map<String, Object>> metrics = objectMapper.readValue(json, new TypeReference<>() {});

                    if (metrics.isEmpty()) {
                        System.out.println("ℹ️  Nenhuma métrica encontrada.");
                        return;
                    }

                    AsciiTable table = new AsciiTable();
                    table.addRule();
                    table.addRow("Dispositivo", "Temperatura (°C)", "Humidade (%)", "Timestamp");
                    table.addRule();

                    for (Map<String, Object> metric : metrics) {
                        table.addRow(
                                metric.get("deviceId"),
                                String.format("%.2f", metric.get("temperature")),
                                String.format("%.2f", metric.get("humidity")),
                                metric.get("timestamp")
                        );
                        table.addRule();
                    }

                    System.out.println(table.render());
                    System.out.println("Total de métricas: " + metrics.size());
                } else {
                    System.out.println("❌ Erro ao consultar métricas. Código: " + response.getCode());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    /**
     * Estatísticas do Sistema
     */
    private static void systemStatistics() {
        try {
            HttpGet request = new HttpGet(BASE_URL + "/devices");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());
                List<Map<String, Object>> devices = objectMapper.readValue(json, new TypeReference<>() {});

                long mqttCount = devices.stream().filter(d -> "MQTT".equals(d.get("protocol"))).count();
                long grpcCount = devices.stream().filter(d -> "GRPC".equals(d.get("protocol"))).count();
                long restCount = devices.stream().filter(d -> "REST".equals(d.get("protocol"))).count();
                long activeCount = devices.stream().filter(d -> "ACTIVE".equals(d.get("status"))).count();

                System.out.println("\n╔════════════════════════════════════════╗");
                System.out.println("║     ESTATÍSTICAS DO SISTEMA            ║");
                System.out.println("╠════════════════════════════════════════╣");
                System.out.println("║ Total de Dispositivos: " + String.format("%15d", devices.size()) + " ║");
                System.out.println("║ Dispositivos Ativos:   " + String.format("%15d", activeCount) + " ║");
                System.out.println("║ Dispositivos MQTT:     " + String.format("%15d", mqttCount) + " ║");
                System.out.println("║ Dispositivos gRPC:     " + String.format("%15d", grpcCount) + " ║");
                System.out.println("║ Dispositivos REST:     " + String.format("%15d", restCount) + " ║");
                System.out.println("╚════════════════════════════════════════╝");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao obter estatísticas: " + e.getMessage());
        }
    }

    /**
     * Utilitários
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private static int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}