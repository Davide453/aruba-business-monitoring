package it.aruba.monitoring.repository;

import it.aruba.monitoring.model.ServiceRecord;
import it.aruba.monitoring.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    @Query("SELECT COUNT(sr) FROM ServiceRecord sr WHERE sr.customerId = :customerId  AND sr.expirationDate < :today")
    long countExpiredServices(@Param("customerId") String customerId, @Param("today") LocalDate today);


    @Query("SELECT sr.customerId FROM ServiceRecord sr WHERE sr.status= 'EXPIRED' GROUP BY sr.customerId HAVING COUNT(*) > 1")
    List<String> findCustomersWithMultipleExpiredServices();

    @Query("SELECT sr.customerId FROM ServiceRecord sr WHERE sr.status= 'ACTIVE' AND sr.expirationDate between :now and :limit")
    List<String> findCustomersWithServicesExpiringSoon(LocalDate now, LocalDate limit);

    @Query("SELECT " +
            "sr.serviceType AS serviceType, " +
            "COUNT(sr) AS total " +
            "from ServiceRecord sr " +
            "where sr.status ='ACTIVE' " +
            "group by sr.serviceType")
    List<ServiceTypeCountProjection> countActiveServicesByType();


    interface ServiceTypeCountProjection {
        ServiceType getServiceType();

        Long getTotal();
    }

    @Query("SELECT " +
            "sr.customerId AS customerId, " +
            "AVG(sr.amount) AS average " +
            "from ServiceRecord sr " +
            "group by sr.customerId")
    List<AverageSpendPerCustomer> calculateAvgSpendPerCustomer();

    interface AverageSpendPerCustomer {
        String getCustomerId();

        BigDecimal getAverage();
    }
}
