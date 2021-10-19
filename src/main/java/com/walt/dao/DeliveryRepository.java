package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.Delivery;
import com.walt.model.DriverDistance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {

	@Query("SELECT Delivery.driver, COUNT(Delivery.id) AS numberOfDeliveries FROM Delivery Delivery "
			+ "WHERE Delivery.driver.city =:city AND Delivery.deliveryTime <:deliveryTime "
			+ "GROUP BY Delivery.driver ORDER BY numberOfDeliveries ASC")
	List<Driver> findAvailableDrivers(@Param("city") City city, @Param("deliveryTime") Date deliveryTime);

	@Query("SELECT driver FROM Driver driver WHERE driver NOT IN (SELECT Delivery.driver FROM Delivery Delivery) "
			+ "and driver.city =:city")
	List<Driver> findDriversWhoHaveNotYetMadeDeliveries(@Param("city") City city);

	@Query("SELECT Delivery.driver AS driver, SUM(Delivery.distance) AS totalDistance FROM Delivery Delivery "
			+ "GROUP BY driver ORDER BY totalDistance DESC")
	List<DriverDistance> allDriversOrderedByTotalDistance();

	@Query("SELECT Delivery.driver AS driver, SUM(Delivery.distance) AS totalDistance FROM Delivery Delivery "
			+ "WHERE Delivery.driver.city =:city GROUP BY driver ORDER BY totalDistance DESC")
	List<DriverDistance> allDriversByCityOrderedByTotalDistance(@Param("city") City city);

}
