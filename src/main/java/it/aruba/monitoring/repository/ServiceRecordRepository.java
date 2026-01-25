package it.aruba.monitoring.repository;

import it.aruba.monitoring.model.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    @Query("SELECT COUNT(sr) FROM ServiceRecord sr WHERE sr.customerId = :customerId  AND sr.expirationDate < :today")
    long countExpiredServices(@Param("customerId") String customerId, @Param("today") LocalDate today);

}
