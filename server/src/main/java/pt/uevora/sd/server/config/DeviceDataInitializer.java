// Caminho: server/src/main/java/pt/uevora/sd/server/config/DeviceDataInitializer.java

package pt.uevora.sd.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.uevora.sd.server.model.Device;
import pt.uevora.sd.server.model.Device.DeviceStatus;
import pt.uevora.sd.server.model.Device.ProtocolType;
import pt.uevora.sd.server.repository.DeviceRepository;

import java.util.List;

/**
 * Inicializa dispositivos de teste na base de dados
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceDataInitializer implements CommandLineRunner {

    private final DeviceRepository deviceRepository;

    @Override
    public void run(String... args) {
        if (deviceRepository.count() == 0) {
            log.info("🔧 Inicializando dispositivos de teste...");

            List<Device> devices = List.of(
                    // ========== Dispositivos REST ==========
                    Device.builder()
                            .id("rest-device-001")
                            .protocol(ProtocolType.REST)
                            .room("A101")
                            .department("Informatica")
                            .floor("Piso1")
                            .building("EdificioII")
                            .status(DeviceStatus.ACTIVE)
                            .build(),

                    Device.builder()
                            .id("rest-device-002")
                            .protocol(ProtocolType.REST)
                            .room("B202")
                            .department("Matematica")
                            .floor("Piso2")
                            .building("EdificioI")
                            .status(DeviceStatus.ACTIVE)
                            .build(),

                    Device.builder()
                            .id("rest-device-003")
                            .protocol(ProtocolType.REST)
                            .room("C303")
                            .department("Fisica")
                            .floor("Piso3")
                            .building("EdificioIII")
                            .status(DeviceStatus.ACTIVE)
                            .build(),

                    // ========== Dispositivos MQTT ==========
                    Device.builder()
                            .id("mqtt-sensor-001")
                            .protocol(ProtocolType.MQTT)
                            .room("A101")
                            .department("Informatica")
                            .floor("Piso1")
                            .building("EdificioII")
                            .status(DeviceStatus.ACTIVE)
                            .build(),

                    Device.builder()
                            .id("mqtt-sensor-002")
                            .protocol(ProtocolType.MQTT)
                            .room("B205")
                            .department("Matematica")
                            .floor("Piso2")
                            .building("EdificioI")
                            .status(DeviceStatus.ACTIVE)
                            .build(),

                    Device.builder()
                            .id("mqtt-sensor-003")
                            .protocol(ProtocolType.MQTT)
                            .room("C103")
                            .department("Fisica")
                            .floor("Piso1")
                            .building("EdificioIII")
                            .status(DeviceStatus.ACTIVE)
                            .build(),

                    // ========== Dispositivos gRPC ==========
                    Device.builder()
                            .id("grpc-gateway-001")
                            .protocol(ProtocolType.GRPC)
                            .room("D301")
                            .department("Engenharia")
                            .floor("Piso3")
                            .building("EdificioIV")
                            .status(DeviceStatus.ACTIVE)
                            .build(),

                    Device.builder()
                            .id("grpc-gateway-002")
                            .protocol(ProtocolType.GRPC)
                            .room("E102")
                            .department("Quimica")
                            .floor("Piso1")
                            .building("EdificioV")
                            .status(DeviceStatus.ACTIVE)
                            .build()
            );

            deviceRepository.saveAll(devices);

            log.info("✅ {} dispositivos de teste criados com sucesso!", devices.size());
            log.info("📊 Dispositivos por protocolo:");
            log.info("   - REST: 3 dispositivos");
            log.info("   - MQTT: 3 dispositivos");
            log.info("   - gRPC: 2 dispositivos");
        } else {
            log.info("ℹ️ Dispositivos já existem na base de dados (total: {})", deviceRepository.count());
        }
    }
}