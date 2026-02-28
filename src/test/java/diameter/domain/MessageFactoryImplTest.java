package diameter.domain;

import diameter.csv.model.CsvRow;
import diameter.domain.factory.MessageFactory;
import diameter.domain.factory.MessageFactoryImpl;
import diameter.domain.message.*;
import diameter.exception.validation.DiameterMessageValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MessageFactoryImpl Tests")
class MessageFactoryImplTest {

    private MessageFactory messageFactory;

    @BeforeEach
    void setUp() {
        messageFactory = new MessageFactoryImpl();
    }

    @Nested
    @DisplayName("AIR Message Creation")
    class AirMessageCreation {

        @Test
        @DisplayName("Should create AIR message with valid request data")
        void shouldCreateAirMessageWithValidRequestData() {
            CsvRow row = createCsvRow(MessageType.AIR, true, "sess-1", "mme1.example.com",
                    "example.com", "user1", null, null);

            DiameterMessage message = messageFactory.createDiameterMessage(row);

            assertInstanceOf(AIR.class, message);
            assertEquals(MessageType.AIR, message.getMessageType());
            assertTrue(message.getIsRequest());
            assertEquals("sess-1", message.getSessionId());
            assertEquals("mme1.example.com", message.getOriginHost());
            assertEquals("example.com", message.getOriginRealm());
            assertEquals("user1", message.getUserName());
        }

        @Test
        @DisplayName("Should throw exception when AIR has is_request=false")
        void shouldThrowExceptionWhenAirHasIsRequestFalse() {
            CsvRow row = createCsvRow(MessageType.AIR, false, "sess-1", "mme1.example.com",
                    "example.com", "user1", null, null);

            DiameterMessageValidationException exception = assertThrows(
                DiameterMessageValidationException.class,
                () -> messageFactory.createDiameterMessage(row)
            );
            assertTrue(exception.getMessage().contains("Type mismatch"));
        }
    }

    @Nested
    @DisplayName("AIA Message Creation")
    class AiaMessageCreation {

        @Test
        @DisplayName("Should create AIA message with valid answer data")
        void shouldCreateAiaMessageWithValidAnswerData() {
            CsvRow row = createCsvRow(MessageType.AIA, false, "sess-1", "hss1.example.com",
                    "example.com", null, null, "2001");

            DiameterMessage message = messageFactory.createDiameterMessage(row);

            assertInstanceOf(AIA.class, message);
            assertEquals(MessageType.AIA, message.getMessageType());
            assertFalse(message.getIsRequest());
        }

        @Test
        @DisplayName("Should throw exception when AIA has is_request=true")
        void shouldThrowExceptionWhenAiaHasIsRequestTrue() {
            CsvRow row = createCsvRow(MessageType.AIA, true, "sess-1", "hss1.example.com",
                    "example.com", null, null, "2001");

            assertThrows(DiameterMessageValidationException.class,
                () -> messageFactory.createDiameterMessage(row)
            );
        }
    }

    @Nested
    @DisplayName("ULR Message Creation")
    class UlrMessageCreation {

        @Test
        @DisplayName("Should create ULR message with valid request data")
        void shouldCreateUlrMessageWithValidRequestData() {
            CsvRow row = createCsvRow(MessageType.ULR, true, "sess-2", "mme1.example.com",
                    "example.com", "user1", "00101", null);

            DiameterMessage message = messageFactory.createDiameterMessage(row);

            assertInstanceOf(ULR.class, message);
            assertEquals(MessageType.ULR, message.getMessageType());
            assertTrue(message.getIsRequest());
        }

        @Test
        @DisplayName("Should throw exception when ULR has is_request=false")
        void shouldThrowExceptionWhenUlrHasIsRequestFalse() {
            CsvRow row = createCsvRow(MessageType.ULR, false, "sess-2", "mme1.example.com",
                    "example.com", "user1", "00101", null);

            assertThrows(DiameterMessageValidationException.class,
                () -> messageFactory.createDiameterMessage(row)
            );
        }
    }

    @Nested
    @DisplayName("ULA Message Creation")
    class UlaMessageCreation {

        @Test
        @DisplayName("Should create ULA message with valid answer data")
        void shouldCreateUlaMessageWithValidAnswerData() {
            CsvRow row = createCsvRow(MessageType.ULA, false, "sess-2", "hss1.example.com",
                    "example.com", null, null, "2001");

            DiameterMessage message = messageFactory.createDiameterMessage(row);

            assertInstanceOf(ULA.class, message);
            assertEquals(MessageType.ULA, message.getMessageType());
            assertFalse(message.getIsRequest());
        }

        @Test
        @DisplayName("Should throw exception when ULA has is_request=true")
        void shouldThrowExceptionWhenUlaHasIsRequestTrue() {
            CsvRow row = createCsvRow(MessageType.ULA, true, "sess-2", "hss1.example.com",
                    "example.com", null, null, "2001");

            assertThrows(DiameterMessageValidationException.class,
                () -> messageFactory.createDiameterMessage(row)
            );
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw exception when CsvRow is null")
        void shouldThrowExceptionWhenCsvRowIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> messageFactory.createDiameterMessage(null)
            );
        }

        @Test
        @DisplayName("Should throw exception when message type is null")
        void shouldThrowExceptionWhenMessageTypeIsNull() {
            CsvRow row = createCsvRow(null, true, "sess-1", "mme1.example.com",
                    "example.com", "user1", null, null);

            assertThrows(IllegalArgumentException.class,
                () -> messageFactory.createDiameterMessage(row)
            );
        }
    }

    @Nested
    @DisplayName("Field Propagation")
    class FieldPropagation {

        @Test
        @DisplayName("Should propagate all fields to request message")
        void shouldPropagateAllFieldsToRequestMessage() {
            CsvRow row = createCsvRow(MessageType.AIR, true, "session-123", "origin-host",
                    "origin-realm", "test-user", null, null);

            DiameterMessage message = messageFactory.createDiameterMessage(row);

            assertEquals("session-123", message.getSessionId());
            assertEquals("origin-host", message.getOriginHost());
            assertEquals("origin-realm", message.getOriginRealm());
            assertEquals("test-user", message.getUserName());
        }

        @Test
        @DisplayName("Should propagate null fields without error")
        void shouldPropagateNullFieldsWithoutError() {
            CsvRow row = createCsvRow(MessageType.AIR, true, null, null, null, null, null, null);

            DiameterMessage message = messageFactory.createDiameterMessage(row);

            assertNull(message.getSessionId());
            assertNull(message.getOriginHost());
            assertNull(message.getOriginRealm());
            assertNull(message.getUserName());
        }
    }

    private CsvRow createCsvRow(MessageType messageType, boolean isRequest, String sessionId,
                                 String originHost, String originRealm, String userName,
                                 String visitedPlmnId, String resultCode) {
        return new CsvRow(messageType, isRequest, sessionId, originHost, originRealm,
                userName, visitedPlmnId, resultCode);
    }
}

