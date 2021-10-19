package com.walt;

import com.google.common.collect.Ordering;
import com.walt.dao.*;
import com.walt.exceptions.WaltApplicationException;
import com.walt.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

	@TestConfiguration
	static class WaltServiceImplTestContextConfiguration {

		@Bean
		public WaltService waltService() {
			return new WaltServiceImpl();
		}
	}

	@Autowired
	WaltService waltService;

	@Resource
	CityRepository cityRepository;

	@Resource
	CustomerRepository customerRepository;

	@Resource
	DriverRepository driverRepository;

	@Resource
	DeliveryRepository deliveryRepository;

	@Resource
	RestaurantRepository restaurantRepository;

	@BeforeEach()
	public void prepareData(){

		City jerusalem = new City("Jerusalem");
		City tlv = new City("Tel-Aviv");
		City bash = new City("Beer-Sheva");
		City haifa = new City("Haifa");

		cityRepository.save(jerusalem);
		cityRepository.save(tlv);
		cityRepository.save(bash);
		cityRepository.save(haifa);

		createDrivers(jerusalem, tlv, bash, haifa);

		createCustomers(jerusalem, tlv, haifa);

		createRestaurant(jerusalem, tlv);
	}

	private void createRestaurant(City jerusalem, City tlv) {
		Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
		Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
		Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
		Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
		Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

		restaurantRepository.save(meat);
		restaurantRepository.save(vegan);
		restaurantRepository.save(cafe);
		restaurantRepository.save(chinese);
		restaurantRepository.save(mexican);
	}

	private void createCustomers(City jerusalem, City tlv, City haifa) {
		Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
		Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
		Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
		Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
		Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

		customerRepository.save(beethoven);
		customerRepository.save(mozart);
		customerRepository.save(chopin);
		customerRepository.save(rachmaninoff);
		customerRepository.save(bach);
	}

	private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
		Driver mary = new Driver("Mary", tlv);
		Driver patricia = new Driver("Patricia", tlv);
		Driver jennifer = new Driver("Jennifer", haifa);
		Driver james = new Driver("James", bash);
		Driver john = new Driver("John", bash);
		Driver robert = new Driver("Robert", jerusalem);
		Driver david = new Driver("David", jerusalem);
		Driver daniel = new Driver("Daniel", tlv);
		Driver noa = new Driver("Noa", haifa);
		Driver ofri = new Driver("Ofri", haifa);
		Driver nata = new Driver("Neta", jerusalem);

		driverRepository.save(mary);
		driverRepository.save(patricia);
		driverRepository.save(jennifer);
		driverRepository.save(james);
		driverRepository.save(john);
		driverRepository.save(robert);
		driverRepository.save(david);
		driverRepository.save(daniel);
		driverRepository.save(noa);
		driverRepository.save(ofri);
		driverRepository.save(nata);
	}

	@Test
	public void testBasics(){

		assertEquals(((List<City>) cityRepository.findAll()).size(),4);
		assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
	}

	/**
	 * check for missing customer
	 */
	@Test
	public void missingCustomer(){
		Exception exception = assertThrows(WaltApplicationException.class, () -> {
			Restaurant vegan = restaurantRepository.findByName("vegan");
			Date deliveryDate = new Date();
			waltService.createOrderAndAssignDriver(null, vegan, deliveryDate);
		});
		String expectedMessage = "Delivery Canceled - The customer is missing";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	/**
	 * check that the customer is not registered in the system
	 */
	@Test
	public void customerIsNotRegistered(){
		Exception exception = assertThrows(WaltApplicationException.class, () -> {
			City tlv = new City("Tel-Aviv");
			Customer intruder = new Customer("Intruder", tlv, "I'M INTRUDER");
			Restaurant vegan = restaurantRepository.findByName("vegan");
			Date deliveryDate = new Date();
			waltService.createOrderAndAssignDriver(intruder, vegan, deliveryDate);
		});
		String expectedMessage = "Delivery Canceled - The customer is not registered in the system";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	/**
	 * check for missing restaurant
	 */
	@Test
	public void missingRestaurant(){
		Exception exception = assertThrows(WaltApplicationException.class, () -> {
			Customer Beethoven = customerRepository.findByName("Beethoven");
			Date deliveryDate = new Date();
			waltService.createOrderAndAssignDriver(Beethoven, null, deliveryDate);
		});
		String expectedMessage = "Delivery Canceled - The restaurant is missing";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	/**
	 * check for missing deliveryDate
	 */
	@Test
	public void missingDeliveryDate(){
		Exception exception = assertThrows(WaltApplicationException.class, () -> {
			Customer Beethoven = customerRepository.findByName("Beethoven");
			Restaurant vegan = restaurantRepository.findByName("vegan");
			waltService.createOrderAndAssignDriver(Beethoven, vegan, null);
		});
		String expectedMessage = "Delivery Canceled - The delivery time is missing";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	/**
	 * check that the restaurant and the customer are not in the same city
	 */
	@Test
	public void customerAndRestaurantAreNotInTheSameCity(){
		Exception exception = assertThrows(WaltApplicationException.class, () -> {
			Restaurant vegan = restaurantRepository.findByName("vegan");
			Customer Mozart = customerRepository.findByName("Mozart");
			Date deliveryDate = new Date();
			waltService.createOrderAndAssignDriver(Mozart, vegan, deliveryDate);
		});
		String expectedMessage = "Delivery Canceled - Customer can not order delivery from restaurant outside the city";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	/**
	 * check that the restaurant and the customer are in the same city
	 * @throws WaltApplicationException 
	 */
	@Test
	public void customerAndRestaurantAreInTheSameCity() throws WaltApplicationException{
		Restaurant vegan = restaurantRepository.findByName("vegan");
		Customer Beethoven = customerRepository.findByName("Beethoven");
		Date deliveryDate = new Date();
		Delivery delivery = waltService.createOrderAndAssignDriver(Beethoven, vegan, deliveryDate);
		assertNotNull(delivery);
		assertEquals(delivery.getCustomer().getName() , "Beethoven");
		assertEquals(delivery.getCustomer().getCity().getName() , "Tel-Aviv");
		assertEquals(delivery.getRestaurant().getName() , "vegan");
		assertEquals(delivery.getRestaurant().getCity().getName() , "Tel-Aviv");
	}

	/**
	 * check that there is no driver available
	 */
	@Test
	public void thereIsNotAvailableDriver(){
		Restaurant vegan = restaurantRepository.findByName("vegan");
		Customer Beethoven = customerRepository.findByName("Beethoven");
		Date deliveryDate = new Date();

		try {
			waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
			waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
			waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		} catch (WaltApplicationException e) {}
		Exception exception = assertThrows(WaltApplicationException.class, () -> {
			waltService.createOrderAndAssignDriver(Beethoven, vegan, deliveryDate);
		});
		String expectedMessage = "Delivery Canceled - There is currently no driver available, please try again later";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	/**
	 * check that there is driver available
	 * @throws WaltApplicationException 
	 */
	@Test
	public void thereIsAvailableDriver() throws WaltApplicationException{
		Restaurant vegan = restaurantRepository.findByName("vegan");
		Customer Beethoven = customerRepository.findByName("Beethoven");
		Date deliveryDate = new Date();
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
	}

	/**
	 * check that drivers rank report is correct
	 * @throws WaltApplicationException 
	 */
	@Test
	public void driverRankReportIsCorrect() throws WaltApplicationException{
		Customer Beethoven= customerRepository.findByName("Beethoven");
		Restaurant vegan=restaurantRepository.findByName("vegan");
		Date deliveryDate = new Date();
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		assertEquals(waltService.getDriverRankReport().size(),3);
		assertTrue(waltService.getDriverRankReport().get(0).getDriver().getName().equals("Mary") ||
				waltService.getDriverRankReport().get(0).getDriver().getName().equals("Patricia") ||
				waltService.getDriverRankReport().get(0).getDriver().getName().equals("Daniel"));
		assertTrue(waltService.getDriverRankReport().get(0).getDriver().getCity().getName().equals(vegan.getCity().getName()));
		assertTrue(waltService.getDriverRankReport().get(0).getTotalDistance() < 20); 
	}

	/**
	 * check that drivers rank report is sorted
	 * @throws WaltApplicationException 
	 */
	@Test
	public void driverRankReportIsSorted() throws WaltApplicationException{
		Customer Beethoven= customerRepository.findByName("Beethoven");
		Restaurant vegan=restaurantRepository.findByName("vegan");
		Date deliveryDate = new Date();
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		List<Double> totalDistances = waltService.getDriverRankReport().stream().
				map(driverDistance -> driverDistance.getTotalDistance()).collect(Collectors.toList());
		assertEquals(true, Ordering.natural().reverse().isOrdered(totalDistances));
	}

	/**
	 * check that drivers rank report by city is correct
	 * @throws WaltApplicationException 
	 */
	@Test
	public void driverRankReportByCityIsCorrect() throws WaltApplicationException{
		Customer Beethoven= customerRepository.findByName("Beethoven");
		Restaurant vegan=restaurantRepository.findByName("vegan");
		City TelAviv = cityRepository.findByName("Tel-Aviv");
		Date deliveryDate = new Date();
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		List<DriverDistance> report = waltService.getDriverRankReportByCity(TelAviv);
		assertEquals(report.size(),3);
		assertTrue(report.get(0).getDriver().getName().equals("Mary") ||
				report.get(0).getDriver().getName().equals("Patricia") ||
				report.get(0).getDriver().getName().equals("Daniel"));
		assertEquals(report.get(0).getDriver().getCity().getName(), "Tel-Aviv");
		assertTrue(report.get(0).getTotalDistance() < 20); 
	}

	/**
	 * check that the driver rank report by city is sorted
	 * @throws WaltApplicationException 
	 */
	@Test
	public void driverRankReportByCityIsSorted() throws WaltApplicationException{
		Customer Beethoven= customerRepository.findByName("Beethoven");
		Restaurant vegan=restaurantRepository.findByName("vegan");
		City TelAviv = cityRepository.findByName("Tel-Aviv");
		Date deliveryDate = new Date();
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		waltService.createOrderAndAssignDriver(Beethoven,vegan,deliveryDate);
		List<Double> totalDistances = waltService.getDriverRankReportByCity(TelAviv).stream().
				map(driverDistance -> driverDistance.getTotalDistance()).collect(Collectors.toList());
		assertEquals(true, Ordering.natural().reverse().isOrdered(totalDistances));
	}
}
