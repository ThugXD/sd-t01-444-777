package pt.uevora.sd.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.uevora.sd.server.model.Metric;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para acesso aos dados de métricas
 */
@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    /**
     * Encontra métricas por dispositivo
     */
    List<Metric> findByDeviceId(String deviceId);

    /**
     * Encontra métricas por dispositivo num intervalo de tempo
     */
    List<Metric> findByDeviceIdAndTimestampBetween(
            String deviceId,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Calcula média de temperatura e humidade por sala
     */
    @Query("SELECT AVG(m.temperature) as avgTemp, AVG(m.humidity) as avgHum " +
            "FROM Metric m WHERE m.room = :room " +
            "AND m.timestamp BETWEEN :start AND :end")
    Object[] findAverageByRoom(
            @Param("room") String room,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Calcula média de temperatura e humidade por departamento
     */
    @Query("SELECT AVG(m.temperature) as avgTemp, AVG(m.humidity) as avgHum " +
            "FROM Metric m WHERE m.department = :department " +
            "AND m.timestamp BETWEEN :start AND :end")
    Object[] findAverageByDepartment(
            @Param("department") String department,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Calcula média de temperatura e humidade por piso
     */
    @Query("SELECT AVG(m.temperature) as avgTemp, AVG(m.humidity) as avgHum " +
            "FROM Metric m WHERE m.floor = :floor " +
            "AND m.timestamp BETWEEN :start AND :end")
    Object[] findAverageByFloor(
            @Param("floor") String floor,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Calcula média de temperatura e humidade por edifício
     */
    @Query("SELECT AVG(m.temperature) as avgTemp, AVG(m.humidity) as avgHum " +
            "FROM Metric m WHERE m.building = :building " +
            "AND m.timestamp BETWEEN :start AND :end")
    Object[] findAverageByBuilding(
            @Param("building") String building,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Encontra métricas por sala num intervalo de tempo
     */
    List<Metric> findByRoomAndTimestampBetween(
            String room,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Encontra métricas por departamento num intervalo de tempo
     */
    List<Metric> findByDepartmentAndTimestampBetween(
            String department,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Encontra métricas por piso num intervalo de tempo
     */
    List<Metric> findByFloorAndTimestampBetween(
            String floor,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Encontra métricas por edifício num intervalo de tempo
     */
    List<Metric> findByBuildingAndTimestampBetween(
            String building,
            LocalDateTime start,
            LocalDateTime end
    );
}