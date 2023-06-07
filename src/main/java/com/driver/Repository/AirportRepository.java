package com.driver.Repository;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AirportRepository {

    static private final int basicPay = 3000;

    HashMap<String, Airport> airportHashMap;
    HashMap<Integer, Flight> flightHashMap;
    HashMap<Integer, Passenger> passengerHashMap;
    HashMap<Integer, List<Passenger>> flightBookings;
    HashMap<Integer, Integer> countOfBookingsOfPassengers;
    HashMap<Integer, Integer> totalRevenueOfFlights;

    public AirportRepository() {
        airportHashMap = new HashMap<>();
        flightHashMap = new HashMap<>();
        passengerHashMap = new HashMap<>();
        flightBookings = new HashMap<>();
        countOfBookingsOfPassengers = new HashMap<>();
        totalRevenueOfFlights = new HashMap<>();
    }

    public void addAirport(Airport airport) {
        airportHashMap.put(airport.getAirportName(), airport);
    }

    public String getLargestAirportName() {
        if(airportHashMap.isEmpty()) {
            return null;
        }

        String largestAirportName = "";
        int maxTerminals = -1;
        for(String airportName : airportHashMap.keySet()) {
            int cntOfTerminals = airportHashMap.get(airportName).getNoOfTerminals();
            if(cntOfTerminals > maxTerminals) {
                maxTerminals = cntOfTerminals;
                largestAirportName = airportName;
            } else if (cntOfTerminals == maxTerminals) {
                String[] arr = new String[] {largestAirportName, airportName};
                Arrays.sort(arr);
                largestAirportName = arr[0];
            }
        }
        return largestAirportName;
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity) {
        for(int flightId : flightHashMap.keySet()) {
            City from = flightHashMap.get(flightId).getFromCity();
            City to = flightHashMap.get(flightId).getToCity();
            if(from.equals(fromCity) && to.equals(toCity)) {
                return flightHashMap.get(flightId).getDuration();
            }
        }
        return -1;
    }

    public int getNumberOfPeopleOn(Date date, String airportName) {
        if(!airportHashMap.containsKey(airportName)) {
            return 0;
        }

        Airport airport = airportHashMap.get(airportName);
        City city = airport.getCity();

        int totalPassengers = 0;
        for(int flightId : flightHashMap.keySet()) {
            Date flightDate = flightHashMap.get(flightId).getFlightDate();
            City fromCity = flightHashMap.get(flightId).getFromCity();
            City toCity = flightHashMap.get(flightId).getToCity();

            if(date.equals(flightDate) && (city.equals(fromCity) || city.equals(toCity))) {
                int size = 0;
                if(flightBookings.containsKey(flightId)) {
                    size = flightBookings.get(flightId).size();
                }
                totalPassengers += size;
            }
        }

        return totalPassengers;
    }

    public int calculateFlightFare(Integer flightId) {
        if(!flightHashMap.containsKey(flightId)) {
            return 0;
        }

        int size = 0;
        if(flightBookings.containsKey(flightId)) {
            size = flightBookings.get(flightId).size();
        }

        return basicPay + 50 * size;
    }

    public String bookATicket(Integer flightId, Integer passengerId) {
        if(!flightBookings.containsKey(flightId)) {
            flightBookings.put(flightId, new ArrayList<>());
        }

        int maxCapacityOfFlight = flightHashMap.get(flightId).getMaxCapacity();
        int currentCapacityOfFlight = flightBookings.get(flightId).size();

        if(currentCapacityOfFlight > maxCapacityOfFlight) {
            return "FAILURE";
        }

        for(int id : flightBookings.keySet()) {
            List<Passenger> passengers = flightBookings.get(id);
            for(Passenger passenger : passengers) {
                if(passenger.getPassengerId() == id) {
                    return "FAILURE";
                }
            }
        }

        if(!passengerHashMap.containsKey(passengerId)) {
            return "FAILURE";
        }

        flightBookings.get(flightId).add(passengerHashMap.get(passengerId));

        countOfBookingsOfPassengers.put(
                passengerId, countOfBookingsOfPassengers.getOrDefault(passengerId, 0) + 1
        );

        totalRevenueOfFlights.put(flightId, totalRevenueOfFlights.getOrDefault(flightId, 0) + calculateFlightFare(flightId));
        return "SUCCESS";
    }

    public String cancelATicket(Integer flightId, Integer passengerId) {
        if(!flightBookings.containsKey(flightId)) {
            return "FAILURE";
        }

        int idx = 0;
        for(Passenger passenger : flightBookings.get(flightId)) {
            if(passenger.getPassengerId() == passengerId) {
                break;
            }
            idx++;
        }

        if(idx == flightBookings.get(flightId).size()) {
            return "FAILURE";
        }

        flightBookings.get(flightId).remove(idx);
        if(flightBookings.get(flightId).size() == 0) {
            flightBookings.remove(flightId);
        }

        countOfBookingsOfPassengers.put(
                passengerId, countOfBookingsOfPassengers.get(passengerId) - 1
        );

        if(countOfBookingsOfPassengers.get(passengerId) == 0) {
            countOfBookingsOfPassengers.remove(passengerId);
        }

        totalRevenueOfFlights.put(flightId, totalRevenueOfFlights.get(flightId) - (flightBookings.get(flightId).size()*50 + basicPay));
        if(totalRevenueOfFlights.get(flightId) == 0) {
            totalRevenueOfFlights.remove(flightId);
        }

        return "SUCCESS";
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId) {
        if(!countOfBookingsOfPassengers.containsKey(passengerId)) {
            return 0;
        }

        return countOfBookingsOfPassengers.get(passengerId);
    }

    public String addFlight(Flight flight) {
        flightHashMap.put(flight.getFlightId(), flight);
        return "SUCCESS";
    }

    public String getAirportNameFromFlightId(Integer flightId) {
        if(!flightHashMap.containsKey(flightId)) return null;
        Flight flight = flightHashMap.get(flightId);
        City fromCity = flight.getFromCity();
        for(String airportName : airportHashMap.keySet()) {
            if(airportHashMap.get(airportName).getCity().equals(fromCity)) {
                return airportName;
            }
        }
        return null;
    }

    public int calculateRevenueOfAFlight(Integer flightId) {
        if(!totalRevenueOfFlights.containsKey(flightId)) {
            return 0;
        }

        return totalRevenueOfFlights.get(flightId);
    }

    public String addPassenger(Passenger passenger) {
        passengerHashMap.put(passenger.getPassengerId(), passenger);
        return "SUCCESS";
    }
}
