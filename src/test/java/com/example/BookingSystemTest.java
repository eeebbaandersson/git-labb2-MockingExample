package com.example;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Gör så att JUnit förstår Mockito-annotationer
@TestMethodOrder(MethodOrderer.MethodName.class)
class BookingSystemTest {

    // Skriv tester med JUnit 5 /AssertJ --> Enhetstester med minst 90 % code coverage av klassen BookingSystem
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

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026,1, 27,10,0);
    private static final String ROOM_ID = "101";

    // --- BOOK ROOM ---

    // HAPPY CASE --> Lyckad bokning av rum
    @Test
    @Tag("bookRoom")
    void bookRoom_shouldReturnTrue_AndSaveBooking_WhenDataIsValid() throws NotificationException {
        // Arange

        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);

        // Skapar rum manuellt
        Room room = new Room(ROOM_ID,"Villa Suite");

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        LocalDateTime startTime = FIXED_NOW.plusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(2);

        // Act
        boolean result = bookingSystem.bookRoom(ROOM_ID, startTime, endTime);

        // Assert
        assertThat(result).isTrue();
        // kontrollera att rum-objektet fick en korrekt bokning med upgifter
        // assertThat(room).

        // Verify
       verify(roomRepository, times(1)).save(room);
       verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    // Kasta Exception om någon av startTime, endTime eller roomId är null
    @ParameterizedTest
    @MethodSource("bookRoom_nullArgumentProvider")
    @Tag("bookRoom")
    void bookRoom_shouldThrowException_WhenArgumentsAreNull(String roomId, LocalDateTime startTime, LocalDateTime endTime) throws NotificationException {
        // Act+Assert
        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");

        // Verify
        verifyNoInteractions(roomRepository,notificationService);
        verifyNoInteractions(notificationService);
    }

    // Kasta Exception vid försök att boka ett rum i dåtid
    @Test
    @Tag("bookRoom")
    void bookRoom_shouldThrowException_WhenStartTimeIsInPast() {
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

    // Kasta Exception vid försök att sätta sluttid före starttid
    @Test
    @Tag("bookRoom")
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
    @Test
    @Tag("bookRoom")
    void bookRoom_shouldReturnTrue_EvenWhenNotificationFails() throws NotificationException {
        // Arrange
        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);

        Room room = new Room(ROOM_ID,"Villa Suite");
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));


        // Riggar felet
        doThrow(new NotificationException("Fel vid notifiering"))
                .when(notificationService).sendBookingConfirmation(any());

        LocalDateTime startTime = FIXED_NOW.plusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(2);

        // Act
        boolean result = bookingSystem.bookRoom(ROOM_ID, startTime, endTime);

        // Assert
        assertThat(result).isTrue();

        // Verify
        verify(roomRepository, times(1)).save(room);
        verify(notificationService).sendBookingConfirmation(any());
    }

    // Om rummet inte är tillgängligt att boka
    @Test
    @Tag("bookRoom")
    void bookRoom_shouldReturnFalse_WhenRoomIsAlreadyOccupied() throws NotificationException {
        // Arrange
        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);

        Room room = new Room(ROOM_ID,"Villa Suite");

        // Simulerar vår första bokning kl 12-14
        LocalDateTime existingStart = FIXED_NOW.plusHours(2);
        LocalDateTime existingEnd = FIXED_NOW.plusHours(4);
        room.addBooking(new Booking("existingBooking",ROOM_ID,existingStart,existingEnd));

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        // Simulera andra bokningen som ska "krocka" kl 11-13
        LocalDateTime requestedStartTime = FIXED_NOW.plusHours(1);
        LocalDateTime requestedEndTime = FIXED_NOW.plusHours(3);

        // Act
        boolean result = bookingSystem.bookRoom(ROOM_ID, requestedStartTime , requestedEndTime);

        //Assert
        assertThat(result).isFalse();

        // Verify
        verify(roomRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    // Kasta Exception om rummet inte existerar
    @Test
    @Tag("bookRoom")
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
    @Tag("getAvailableRooms")
    void getAvailableRooms_shouldReturnOnlyAvailableRooms_WhenSomeRoomsAreOccupied() throws NotificationException {
        // Arrange

        // Skapar rum manuellt
        Room room1 = new Room("101","Villa-Suite");
        Room room2 = new Room("103","Economy");

        LocalDateTime startTime = FIXED_NOW.plusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(2);

        // Gör room2 upptaget genom att skapa en ny bokning som krockar
        room2.addBooking(new Booking("b1","103", startTime, endTime));

        when(roomRepository.findAll()).thenReturn(List.of(room1,room2));

        // Act
        List<Room> result = bookingSystem.getAvailableRooms(startTime, endTime);

        // Assert
        assertThat(result)
                .as("Listan bör endast visa det lediga rummet")
                .hasSize(1)
                .contains(room1)
                .doesNotContain(room2);

        // Verify
        verify(roomRepository).findAll();
        verifyNoInteractions(notificationService);

    }

    // Kasta Exception om startTime eller endTime är null
    @ParameterizedTest
    @MethodSource("getAvailableRooms_nullArgumentProvider")
    @Tag("getAvailableRooms")
    void getAvailableRooms_shouldThrowException_WhenArgumentsAreNull(LocalDateTime startTime, LocalDateTime endTime) {
        // Act+ Assert
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Måste ange både start- och sluttid");

        // Verify
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(notificationService);
    }

    // Kasta Exception om sluttiden är före starttiden
    @Test
    @Tag("getAvailableRooms")
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

    // HAPPY CASE --> Tar bort aktuell rumsbokning
    @Test
    @Tag("cancelBooking")
    void cancelBooking_shouldRemoveCurrentBooking_AndSendConfirmation() throws NotificationException {
        // Arrange
        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);

        Room room = new Room(ROOM_ID,"Villa Suite");

        LocalDateTime startTime = FIXED_NOW.plusHours(4);
        LocalDateTime endTime = FIXED_NOW.plusHours(6);
        Booking booking = new Booking("b3",ROOM_ID, startTime, endTime);

        room.addBooking(booking);

        when(roomRepository.findAll()).thenReturn(List.of(room));

        // Act
        boolean result = bookingSystem.cancelBooking("b3");

        // Assert
        assertThat(result).isTrue();
        // Kontrollerar att bokningen faktiskt raderats
        assertThat(room.hasBooking("b3")).isFalse();

        // Verify
        // Kontrollera att ändrigen/uppdateringen av rummet skedde
        verify(roomRepository,times(1)).save(room);
        verify(notificationService).sendCancellationConfirmation(booking);
    }

    // Kasta Exception om bookingId är null
    @Test
    @Tag("cancelBooking")
    void cancelBooking_shouldThrowException_WhenBookingIdIsNull() {
        // Act+Assert
        assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Boknings-id kan inte vara null");

        // Verify
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(notificationService);
    }

    // Om en rumsbokning är tom
    @Test
    @Tag("cancelBooking")
    void cancelBooking_shouldReturnFalse_WhenBookingDoesNotExist() throws NotificationException {
        // Arrange
        when(roomRepository.findAll()).thenReturn(List.of());

        // Act
        boolean result = bookingSystem.cancelBooking("noonExistingId");

        // Assert
        assertThat(result).isFalse();

        // Verify
        verify(roomRepository).findAll();
        verify(roomRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }


    // Kasta Exception vid avbokning om starttiden är före currentTime
    @Test
    @Tag("cancelBooking")
    void cancelBooking_shouldThrowException_WhenStartTimeIsBeforeCurrentTime() {
        // Arange
        when(timeProvider.getCurrentTime()).thenReturn(FIXED_NOW);

        Room room = new Room(ROOM_ID,"Villa Suite");

        LocalDateTime startTime = FIXED_NOW.minusHours(1);
        LocalDateTime endTime = FIXED_NOW.plusHours(2);
        room.addBooking(new Booking("b2",ROOM_ID,startTime,endTime));

        when(roomRepository.findAll()).thenReturn(List.of(room));

        // Act+Assert
        assertThatThrownBy(() -> bookingSystem.cancelBooking("b2"))
        .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kan inte avboka påbörjad eller avslutad bokning");

        //Verify
        verify(roomRepository, never()).save(any());
        verifyNoInteractions(notificationService);

    }

    static List<Arguments> bookRoom_nullArgumentProvider() {
        return List.of(arguments(null, FIXED_NOW.plusHours(1), FIXED_NOW.plusHours(2)),
                arguments(ROOM_ID, null, FIXED_NOW.plusHours(2)),
                        arguments(ROOM_ID, FIXED_NOW.plusHours(2), null)
        );
    }

    static List<Arguments> getAvailableRooms_nullArgumentProvider() {
        return List.of(arguments(null, FIXED_NOW.plusHours(1)),
                arguments(FIXED_NOW.plusHours(2), null)
        );
    }
}

