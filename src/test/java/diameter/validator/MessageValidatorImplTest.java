package diameter.validator;

import diameter.domain.message.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MessageValidatorImpl Tests")
class MessageValidatorImplTest {

    private MessageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MessageValidatorImpl();
    }

    @Nested
    @DisplayName("AIR Validation")
    class AirValidation {

        @Test
        @DisplayName("Should validate valid AIR message")
        void shouldValidateValidAirMessage() {
            AIR air = new AIR("sess-1", "mme1.example.com", "example.com", "user1");

            ValidationResult result = validator.validate(air);

            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when Session-Id is missing")
        void shouldFailWhenSessionIdMissing() {
            AIR air = new AIR(null, "mme1.example.com", "example.com", "user1");

            ValidationResult result = validator.validate(air);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Session-Id")));
        }

        @Test
        @DisplayName("Should fail validation when Origin-Host is missing")
        void shouldFailWhenOriginHostMissing() {
            AIR air = new AIR("sess-1", null, "example.com", "user1");

            ValidationResult result = validator.validate(air);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Origin-Host")));
        }

        @Test
        @DisplayName("Should fail validation when Origin-Realm is missing")
        void shouldFailWhenOriginRealmMissing() {
            AIR air = new AIR("sess-1", "mme1.example.com", null, "user1");

            ValidationResult result = validator.validate(air);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Origin-Realm")));
        }

        @Test
        @DisplayName("Should fail validation when User-Name is missing")
        void shouldFailWhenUserNameMissing() {
            AIR air = new AIR("sess-1", "mme1.example.com", "example.com", null);

            ValidationResult result = validator.validate(air);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("User-Name")));
        }

        @Test
        @DisplayName("Should collect all validation errors")
        void shouldCollectAllValidationErrors() {
            AIR air = new AIR(null, null, null, null);

            ValidationResult result = validator.validate(air);

            assertFalse(result.isValid());
            assertEquals(4, result.getErrors().size());
        }

        @ParameterizedTest
        @DisplayName("Should fail validation when Session-Id is empty or blank")
        @NullAndEmptySource
        @ValueSource(strings = {"", "   "})
        void shouldFailWhenSessionIdIsEmptyOrBlank(String sessionId) {
            AIR air = new AIR(sessionId, "mme1.example.com", "example.com", "user1");

            ValidationResult result = validator.validate(air);

            assertFalse(result.isValid());
        }
    }

    @Nested
    @DisplayName("ULR Validation")
    class UlrValidation {

        @Test
        @DisplayName("Should validate valid ULR message")
        void shouldValidateValidUlrMessage() {
            ULR ulr = new ULR("sess-2", "mme1.example.com", "example.com", "user1", "00101");

            ValidationResult result = validator.validate(ulr);

            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when Visited-PLMN-Id is missing")
        void shouldFailWhenVisitedPlmnIdMissing() {
            ULR ulr = new ULR("sess-2", "mme1.example.com", "example.com", "user1", null);

            ValidationResult result = validator.validate(ulr);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Visited-PLMN-Id")));
        }

        @Test
        @DisplayName("Should fail validation when Visited-PLMN-Id is empty")
        void shouldFailWhenVisitedPlmnIdEmpty() {
            ULR ulr = new ULR("sess-2", "mme1.example.com", "example.com", "user1", "");

            ValidationResult result = validator.validate(ulr);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Visited-PLMN-Id")));
        }

        @Test
        @DisplayName("Should validate all mandatory request AVPs plus Visited-PLMN-Id")
        void shouldValidateAllMandatoryAvps() {
            ULR ulr = new ULR(null, null, null, null, null);

            ValidationResult result = validator.validate(ulr);

            assertFalse(result.isValid());
            assertEquals(5, result.getErrors().size()); // 4 base request AVPs + Visited-PLMN-Id
        }
    }

    @Nested
    @DisplayName("AIA Validation")
    class AiaValidation {

        @Test
        @DisplayName("Should validate valid AIA message")
        void shouldValidateValidAiaMessage() {
            AIA aia = new AIA("sess-1", "hss1.example.com", "example.com", null, "2001");

            ValidationResult result = validator.validate(aia);

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should fail validation when Result-Code is missing")
        void shouldFailWhenResultCodeMissing() {
            AIA aia = new AIA("sess-1", "hss1.example.com", "example.com", null, null);

            ValidationResult result = validator.validate(aia);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Result-Code")));
        }

        @Test
        @DisplayName("Should fail validation when Result-Code is empty")
        void shouldFailWhenResultCodeEmpty() {
            AIA aia = new AIA("sess-1", "hss1.example.com", "example.com", null, "");

            ValidationResult result = validator.validate(aia);

            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should not require User-Name for answers")
        void shouldNotRequireUserNameForAnswers() {
            AIA aia = new AIA("sess-1", "hss1.example.com", "example.com", null, "2001");

            ValidationResult result = validator.validate(aia);

            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("ULA Validation")
    class UlaValidation {

        @Test
        @DisplayName("Should validate valid ULA message")
        void shouldValidateValidUlaMessage() {
            ULA ula = new ULA("sess-2", "hss1.example.com", "example.com", null, "2001");

            ValidationResult result = validator.validate(ula);

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should fail validation when Result-Code is missing")
        void shouldFailWhenResultCodeMissing() {
            ULA ula = new ULA("sess-2", "hss1.example.com", "example.com", null, null);

            ValidationResult result = validator.validate(ula);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Result-Code")));
        }
    }

    @Nested
    @DisplayName("ValidationResult Behavior")
    class ValidationResultBehavior {

        @Test
        @DisplayName("Should return unmodifiable error list")
        void shouldReturnUnmodifiableErrorList() {
            AIR air = new AIR(null, "host", "realm", "user");
            ValidationResult result = validator.validate(air);

            assertThrows(UnsupportedOperationException.class,
                () -> result.getErrors().add("new error"));
        }

        @Test
        @DisplayName("Should preserve error order")
        void shouldPreserveErrorOrder() {
            AIR air = new AIR(null, null, null, null);
            ValidationResult result = validator.validate(air);

            assertEquals(4, result.getErrors().size());
            // Errors should be in the order they were validated
            assertTrue(result.getErrors().getFirst().contains("Session-Id"));
        }
    }
}
