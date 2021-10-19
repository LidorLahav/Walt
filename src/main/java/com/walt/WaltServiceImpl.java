package com.walt;

import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.exceptions.WaltApplicationException;
import com.walt.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class WaltServiceImpl implements WaltService {

	@Autowired
	private DeliveryRepository deliveryRepository;
	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws WaltApplicationException {
		validateOrder(customer, restaurant, deliveryTime);
		City city = restaurant.getCity();
		Driver availableDriver = getAvailableDriver(city, deliveryTime);
		Delivery delivery = new Delivery(availableDriver, restaurant, customer, deliveryTime);
		WaltApplication.log.info("Delivery created - customer: " + customer.getName() + " | " + "restaurant: " + restaurant.getName() +
				" | " + "driver: " + availableDriver.getName());
		return deliveryRepository.save(delivery);
	}

	private void validateOrder(Customer customer, Restaurant restaurant, Date deliveryTime) throws WaltApplicationException {
		if (customer == null){
			WaltApplication.log.error("Delivery Canceled - The customer is missing");
			throw new WaltApplicationException("Delivery Canceled - The customer is missing");
		}
		if (customerRepository.findByName(customer.getName()) == null) {
			WaltApplication.log.error("Delivery Canceled - The customer is not registered in the system");
			throw new WaltApplicationException("Delivery Canceled - The customer is not registered in the system");
		}
		if(restaurant == null) {
			WaltApplication.log.error("Delivery Canceled - The restaurant is missing");
			throw new WaltApplicationException("Delivery Canceled - The restaurant is missing");
		}
		if(deliveryTime == null) {
			WaltApplication.log.error("Delivery Canceled - The delivery is missing");
			throw new WaltApplicationException("Delivery Canceled - The delivery time is missing");
		}
		if (!customer.getCity().getName().equals(restaurant.getCity().getName())){
			WaltApplication.log.error("Delivery Canceled - Customer can not order delivery from restaurant outside the city");
			throw new WaltApplicationException("Delivery Canceled - Customer can not order delivery from restaurant outside the city");
		}
	}

	private Driver getAvailableDriver(City city, Date deliveryTime) throws WaltApplicationException{
		try {
			List<Driver> drivers = deliveryRepository.findDriversWhoHaveNotYetMadeDeliveries(city);
			if(drivers.isEmpty()) {
				drivers = deliveryRepository.findAvailableDrivers(city, oneHourBack(deliveryTime));
			}
			return drivers.get(0);
		} catch (Exception e) {
			WaltApplication.log.error("Delivery Canceled - There is currently no driver available");
			throw new WaltApplicationException("Delivery Canceled - There is currently no driver available, please try again later");
		}
	}

	private static Date oneHourBack(Date date) {
		Calendar calender = Calendar.getInstance();
		calender.setTime(date);
		calender.add(Calendar.HOUR_OF_DAY, -1);
		return calender.getTime();
	}

	@Override
	public List<DriverDistance> getDriverRankReport() {
		return deliveryRepository.allDriversOrderedByTotalDistance();
	}

	@Override
	public List<DriverDistance> getDriverRankReportByCity(City city) {
		return deliveryRepository.allDriversByCityOrderedByTotalDistance(city);
	}
}
