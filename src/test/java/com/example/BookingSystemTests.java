package com.example;

import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Gör så att JUnit förstår Mockito-annotationer
class BookingSystemTests {

    // Skriv tester med JUnit 5 /AssertJ --> Enhetstester med minst 90 % code coverage
    // Skapa lämpliga test doubles för beroenden
    // Testa både lyckade och misslyckade scenarion
    // Använd parametriserade tester där det är lämpligt --> För att testa null?
    // Dokumentera testfall med tydliga beskrivningar (Javadocs?)

    // Arrange / Given --> Skapa nödvändig data
    // Act / When --> Anropa metoder/service
    // Assert / Then (Kontrollera resultatet med AssertJ)
    // Verify (Kontrollera interaktionen med Mockito)


    @Mock // Låtsas-delarna (Mocks) vår klass behöver för att kunna köra, gör ingenting själva utan väntar på vår order
    private TimeProvider timeProvider;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private NotificationService notificationService;

  // Skapar den riktiga klassen (med logiken vi vill testa/SUT) och stoppa automatiskt in låtsas-delarna i den
    @InjectMocks
    private BookingSystem bookingSystem;

    private final LocalDateTime FIXED_NOW = LocalDateTime.of(2026,1, 27,10,0);
    private final String ROOM_ID = "101";



    // HAPPY CASE
    @Test
    void bookRoom_shouldCreateNewBooking_AndSendConfirmation() throws NotificationException {
        // Arange

        // Fixerar tiden för vår klocka
        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);

        // Skapar ny rumsbokning
        Room room = new Room(ROOM_ID,"Villa Suite");
        // Instruera Mockito vad findById() ska returnera
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        LocalDateTime startTime = FIXED_NOW.plusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(2);

        // Act
        boolean success = bookingSystem.bookRoom(ROOM_ID, startTime, endTime);

        // Assert
        assertThat(success).isTrue();

        // Verify
       verify(roomRepository, times(1)).save(room);
       verify(notificationService).sendBookingConfirmation(any(Booking.class));

    }

    // Fel vid bokning av rum:
    // null --> startTime/endTime/roomId (3x)
    // När rummet inte existerar i databasen

    // HAPPY CASE
    @Test
    void getAvailableRooms_shouldDisplayAvailableRooms_WithinSelectedTimeFrame() {

        // Arrange

        // Skapar bokningsbara rum manuellt
        Room room1 = new Room("101","Villa-Suite");
        Room room2 = new Room("103","Economy");

        LocalDateTime startTime = FIXED_NOW.plusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(2);

        // Gör room2 upptaget genom att skapa en ny bokning som krockar
        room2.addBooking(new Booking("b1","103", startTime, endTime));

        // Instruera Mockito vad findAll() ska returnera
        when(roomRepository.findAll()).thenReturn(List.of(room1,room2));

        // Act
        List<Room> result = bookingSystem.getAvailableRooms(startTime, endTime);

        // Assert
        assertThat(result)
                .as("Listan bör endast visa det lediga rummet")
                .hasSize(1)
                .contains(room1)
                .doesNotContain(room2);

    }

    // Fel vid visning av tillgängliga rum
    // null --> startTime/endTime (2x)

    // HAPPY CASE
//    @Test
//    void cancelBooking_shouldRemoveCurrentBooking_AndSendConfirmation() throws NotificationException {
//    }

    // Fel vid avbokning av rum:
    // null --> bookinId
}