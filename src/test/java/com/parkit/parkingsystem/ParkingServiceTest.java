package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.InteractiveShell;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static InteractiveShell interactiveShell;
    @Mock
    private static Ticket ticket;
    @Mock
    private static ParkingSpot parkingSpot;
    

    @BeforeEach
    private void setUpPerTest() {
        try {
      

            parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
 
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
    
    
    @Test
    public void testProcessIncomingVehicle() throws Exception {
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	when(inputReaderUtil.readSelection()).thenReturn(2);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2);
    	parkingService.getNextParkingNumberIfAvailable();
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        ticket.setPrice(0);
        ticket.setOutTime(null);
    	when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
    	when(ticketDAO.getNbTicket(inputReaderUtil.readVehicleRegistrationNumber())).thenReturn(2);
    	
    	parkingService.processIncomingVehicle();
    	
    	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    	verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    	verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }
    

    @Test
    public void processExitingVehicleTest() throws Exception{
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	Date outTime = new Date();
    	ticket.setOutTime(outTime);
    	when(ticketDAO.getNbTicket(anyString())).thenReturn(2);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	
    	parkingService.processExitingVehicle();
        
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
       
    }
    
    
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	when(ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber())).thenReturn(ticket);
    	Date outTime = new Date();
    	ticket.setOutTime(outTime);
    	when(ticketDAO.getNbTicket(inputReaderUtil.readVehicleRegistrationNumber())).thenReturn(2);
        when(ticketDAO.updateTicket(ticket)).thenReturn(false);
    	
    	parkingService.processExitingVehicle();
            
    	verify(ticketDAO, times(1)).getTicket(anyString());
        verify(ticketDAO, times(1)).getNbTicket(anyString());
        verify(ticketDAO, times(1)).updateTicket(ticket);
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    	
    }
    
    
    @Test
    public void testGetNextParkingNumberIfAvailable() throws Exception {
    	
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
    	parkingService.getNextParkingNumberIfAvailable();
    	
    	verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
    	
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        ticket.setPrice(0);
        ticket.setOutTime(null);
    	when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
    	when(ticketDAO.getNbTicket(inputReaderUtil.readVehicleRegistrationNumber())).thenReturn(2);
    	
    	parkingService.processIncomingVehicle();
    	
    	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    	verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    	verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    	
    }
    
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
    	
    	
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0); 
    	
    	Exception exception = assertThrows(Exception.class, () -> {
    	    parkingService.getNextParkingNumberIfAvailable();
    	});
    	
    	assertEquals("Error fetching parking number from DB. Parking slots might be full", exception.getMessage());

    }
    
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    	
    	when(inputReaderUtil.readSelection()).thenReturn(3);
    	
    	Exception exception = assertThrows(Exception.class, () -> {
    	    parkingService.getNextParkingNumberIfAvailable();
    	});
    	
    	assertEquals("Entered input is invalid", exception.getMessage());
    	
    	verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));
    }

}
