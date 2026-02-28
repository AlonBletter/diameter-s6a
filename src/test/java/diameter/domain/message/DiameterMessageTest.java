package diameter.domain.message;

import diameter.domain.MessageType;
import diameter.validator.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DiameterMessage Hierarchy Tests")
class DiameterMessageTest {

    @Nested
    @DisplayName("AIR Message Tests")
    class AirMessageTests {

        @Test
        @DisplayName("Should create AIR with correct message type")
        void shouldCreateAirWithCorrectMessageType() {
            AIR air = new AIR("sess-1", "host", "realm", "user");

            assertEquals(MessageType.AIR, air.getMessageType());
            assertTrue(air.getIsRequest());
        }

        @Test
        @DisplayName("Should store all field values correctly")
        void shouldStoreAllFieldValuesCorrectly() {
            AIR air = new AIR("session-123", "origin-host", "origin-realm", "test-user");

            assertEquals("session-123", air.getSessionId());
            assertEquals("origin-host", air.getOriginHost());
            assertEquals("origin-realm", air.getOriginRealm());
            assertEquals("test-user", air.getUserName());
        }

        @Test
        @DisplayName("Should validate all mandatory request AVPs")
        void shouldValidateAllMandatoryRequestAvps() {
            AIR air = new AIR(null, null, null, null);
            ValidationResult result = new ValidationResult();

            air.validate(result);

            assertFalse(result.isValid());
            assertEquals(4, result.getErrors().size());
        }

        @Test
        @DisplayName("Should pass validation with all AVPs present")
        void shouldPassValidationWithAllAvpsPresent() {
            AIR air = new AIR("sess-1", "host", "realm", "user");
            ValidationResult result = new ValidationResult();

            air.validate(result);

            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("ULR Message Tests")
    class UlrMessageTests {

        @Test
        @DisplayName("Should create ULR with correct message type")
        void shouldCreateUlrWithCorrectMessageType() {
            ULR ulr = new ULR("sess-2", "host", "realm", "user", "00101");

            assertEquals(MessageType.ULR, ulr.getMessageType());
            assertTrue(ulr.getIsRequest());
        }

        @Test
        @DisplayName("Should require Visited-PLMN-Id in addition to base request AVPs")
        void shouldRequireVisitedPlmnId() {
            ULR ulr = new ULR("sess-2", "host", "realm", "user", null);
            ValidationResult result = new ValidationResult();

            ulr.validate(result);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Visited-PLMN-Id")));
        }

        @Test
        @DisplayName("Should pass validation with all AVPs including Visited-PLMN-Id")
        void shouldPassValidationWithAllAvps() {
            ULR ulr = new ULR("sess-2", "host", "realm", "user", "00101");
            ValidationResult result = new ValidationResult();

            ulr.validate(result);

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should fail validation when Visited-PLMN-Id is empty string")
        void shouldFailValidationWhenVisitedPlmnIdEmpty() {
            ULR ulr = new ULR("sess-2", "host", "realm", "user", "");
            ValidationResult result = new ValidationResult();

            ulr.validate(result);

            assertFalse(result.isValid());
        }
    }

    @Nested
    @DisplayName("AIA Message Tests")
    class AiaMessageTests {

        @Test
        @DisplayName("Should create AIA with correct message type")
        void shouldCreateAiaWithCorrectMessageType() {
            AIA aia = new AIA("sess-1", "host", "realm", null, "2001");

            assertEquals(MessageType.AIA, aia.getMessageType());
            assertFalse(aia.getIsRequest());
        }

        @Test
        @DisplayName("Should require Result-Code for answers")
        void shouldRequireResultCode() {
            AIA aia = new AIA("sess-1", "host", "realm", null, null);
            ValidationResult result = new ValidationResult();

            aia.validate(result);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Result-Code")));
        }

        @Test
        @DisplayName("Should pass validation with Result-Code present")
        void shouldPassValidationWithResultCode() {
            AIA aia = new AIA("sess-1", "host", "realm", null, "2001");
            ValidationResult result = new ValidationResult();

            aia.validate(result);

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should not require User-Name for answers")
        void shouldNotRequireUserName() {
            AIA aia = new AIA("sess-1", "host", "realm", null, "2001");
            ValidationResult result = new ValidationResult();

            aia.validate(result);

            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("ULA Message Tests")
    class UlaMessageTests {

        @Test
        @DisplayName("Should create ULA with correct message type")
        void shouldCreateUlaWithCorrectMessageType() {
            ULA ula = new ULA("sess-2", "host", "realm", null, "2001");

            assertEquals(MessageType.ULA, ula.getMessageType());
            assertFalse(ula.getIsRequest());
        }

        @Test
        @DisplayName("Should require Result-Code for ULA")
        void shouldRequireResultCode() {
            ULA ula = new ULA("sess-2", "host", "realm", null, null);
            ValidationResult result = new ValidationResult();

            ula.validate(result);

            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should pass validation with Result-Code present")
        void shouldPassValidationWithResultCode() {
            ULA ula = new ULA("sess-2", "host", "realm", null, "2001");
            ValidationResult result = new ValidationResult();

            ula.validate(result);

            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("Request vs Answer Behavior")
    class RequestVsAnswerBehavior {

        @Test
        @DisplayName("All requests should have isRequest=true")
        void allRequestsShouldHaveIsRequestTrue() {
            AIR air = new AIR("sess-1", "host", "realm", "user");
            ULR ulr = new ULR("sess-2", "host", "realm", "user", "00101");

            assertTrue(air.getIsRequest());
            assertTrue(ulr.getIsRequest());
        }

        @Test
        @DisplayName("All answers should have isRequest=false")
        void allAnswersShouldHaveIsRequestFalse() {
            AIA aia = new AIA("sess-1", "host", "realm", null, "2001");
            ULA ula = new ULA("sess-2", "host", "realm", null, "2001");

            assertFalse(aia.getIsRequest());
            assertFalse(ula.getIsRequest());
        }
    }
}

