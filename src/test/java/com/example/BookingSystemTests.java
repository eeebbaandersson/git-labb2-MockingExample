package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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

    // --- BOOK ROOM ---

    // HAPPY CASE --> Lyckad bokning av rum
    @Test
    void bookRoom_shouldCreateNewBooking_AndSendConfirmation() throws NotificationException {
        // Arange

        // Fixerar tiden
        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);

        // Skapar rum manuellt
        Room room = new Room(ROOM_ID,"Villa Suite");
        // Instruera Mockito vad findById() ska returnera
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        LocalDateTime startTime = FIXED_NOW.plusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(2);

        // Act
        boolean result = bookingSystem.bookRoom(ROOM_ID, startTime, endTime);

        // Assert
        assertThat(result).isTrue();

        // Verify
       verify(roomRepository, times(1)).save(room);
       verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    // null --> startTime/endTime/roomId (3x) Parameteriserat test?


    // Kasta fel vid försök att boka ett rum i dåtid
    @Test
    void bookRoom_shouldThrowException_WhenBookingInThePast() {
        // Arange
        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);
        LocalDateTime startTime = FIXED_NOW.minusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(1);

        // Act+Assert
        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kan inte boka tid i dåtid");

        // Verify
        verify(roomRepository, never()).save(any());
    }

    // Kasta fel vid försök att sätta sluttid före starttid
    @Test
    void bookRoom_shouldThrowException_WhenEndTimeIsBeforeStartTime() {
        // Arrange
        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);
        LocalDateTime startTime = FIXED_NOW.plusHours(1);
        LocalDateTime endTime = FIXED_NOW.minusHours(1);

        // Act+Assert
        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, startTime, endTime))
        .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");

        verify(roomRepository, never()).save(any());
    }

    // Bokningen ska genomföras även om notifieringen misslyckas
    // När rummet inte är tillgängligt att boka (!room.isAvailable)?

    // Kasta fel om rummet inte existerar
    @Test
    void bookRoom_shouldThrowException_WhenRoomDoesNotExist() throws NotificationException {
        // Arrange
        String nonExistentId = "999";
        when(roomRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);

        LocalDateTime startTime = FIXED_NOW.plusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(2);

        // Act+Assert
       assertThatThrownBy(() -> {
           bookingSystem.bookRoom(nonExistentId, startTime, endTime);
       }).isInstanceOf(IllegalArgumentException.class)
               .hasMessageContaining("Rummet existerar inte");

       // Verify
       verify(roomRepository, never()).save(any());
       verify(notificationService, never()).sendBookingConfirmation(any());
      // verifyNoInteractions(notificationService);

    }

    // --- GET AVAILABLE ROOM ---

    // HAPPY CASE --> Visa alla tillgängliga rum
    @Test
    void getAvailableRooms_shouldDisplayAvailableRooms_WithinSelectedTimeFrame() {
        // Arrange

        // Skapar rum manuellt
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


    // null --> startTime/endTime (2x) --> Parameteriserat test?


    // Kasta exception när sluttiden är före starttiden
    @Test
    void getAvailableRooms_shouldThrowException_WhenEndTimeIsBeforeStartTime() {
        // Arrange
        LocalDateTime startTime = FIXED_NOW.plusHours(2);
        LocalDateTime endTime = FIXED_NOW.plusHours(1);

        // Act+Assert
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");


        // Verify --> Bekräfta att det aldrig skedde någon hämtning av rum från databasen
        verifyNoInteractions(roomRepository);
    }



    // --- CANCEL BOOKING ---

    // HAPPY CASE --> Ta bort en rumsbokning
//    @Test
//    void cancelBooking_shouldRemoveCurrentBooking_AndSendConfirmation() throws NotificationException {
//    }


    // null --> bookinId
    // roomWithBooking.isEmpty()
    // booking.getStartTime().isBefore

}