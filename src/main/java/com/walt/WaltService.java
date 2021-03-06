package com.walt;

import com.walt.exceptions.WaltApplicationException;
import com.walt.model.*;

import java.util.Date;
import java.util.List;

public  interface WaltService{

    Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws WaltApplicationException;

    List<DriverDistance> getDriverRankReport();

    List<DriverDistance> getDriverRankReportByCity(City city);
}

