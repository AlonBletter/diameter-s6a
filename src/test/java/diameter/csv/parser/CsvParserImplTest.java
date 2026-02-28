package diameter.csv.parser;

import diameter.csv.model.CsvRow;
import diameter.domain.MessageType;
import diameter.exception.csv.CsvValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvParserImpl Tests")
class CsvParserImplTest {
    private              CsvParser parser;
    private static final String    VALID_HEADER =
            "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code";

    @BeforeEach
    void setUp() {
        parser = new CsvParserImpl();
    }

    @Nested
    @DisplayName("Header Validation")
    class HeaderValidation {

        @Test
        @DisplayName("Should throw exception when CSV is null")
        void shouldThrowExceptionWhenCsvIsNull() {
            assertThrows(CsvValidationException.class, () -> parser.parse(null));
        }

        @Test
        @DisplayName("Should throw exception when CSV is empty")
        void shouldThrowExceptionWhenCsvIsEmpty() {
            assertThrows(CsvValidationException.class, () -> parser.parse(Collections.emptyList()));
        }

        @Test
        @DisplayName("Should throw exception when header is blank")
        void shouldThrowExceptionWhenHeaderIsBlank() {
            List<String> lines = List.of("   ");
            assertThrows(CsvValidationException.class, () -> parser.parse(lines));
        }

        @Test
        @DisplayName("Should throw exception when required column is missing")
        void shouldThrowExceptionWhenRequiredColumnIsMissing() {
            String       incompleteHeader = "message_type,is_request,session_id";
            List<String> lines            = List.of(incompleteHeader);

            CsvValidationException exception = assertThrows(CsvValidationException.class, () -> parser.parse(lines));
            assertTrue(exception.getMessage().contains("Missing required column"));
        }

        @Test
        @DisplayName("Should throw exception for unknown column")
        void shouldThrowExceptionForUnknownColumn() {
            String       invalidHeader =
                    "message_type,is_request,session_id,origin_host,origin_realm,user_name,visited_plmn_id,result_code,unknown_column";

            List<String> lines         = List.of(invalidHeader);

            CsvValidationException exception = assertThrows(CsvValidationException.class, () -> parser.parse(lines));
            assertTrue(exception.getMessage().contains("Unknown CSV column"));
        }

        @Test
        @DisplayName("Should accept valid header with different column order")
        void shouldAcceptValidHeaderWithDifferentOrder() {
            String reorderedHeader =
                    "result_code,visited_plmn_id,user_name,origin_realm,origin_host,session_id,is_request,message_type";

            String       dataLine = "2001,,user1,example.com,hss1.example.com,sess-1,false,AIA";
            List<String> lines    = List.of(reorderedHeader, dataLine);
            List<CsvRow> result   = parser.parse(lines);

            assertEquals(1, result.size());
            assertEquals(MessageType.AIA, result.getFirst().getMessageType());
            assertEquals("sess-1", result.getFirst().getSessionId());
        }
    }

    @Nested
    @DisplayName("Data Line Parsing")
    class DataLineParsing {

        @Test
        @DisplayName("Should parse valid AIR message")
        void shouldParseValidAirMessage() {
            List<String> lines =
                    List.of(VALID_HEADER, "AIR,true,sess-1,mme1.example.com,example.com,001010123456789,,");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
            CsvRow row = result.getFirst();
            assertEquals(MessageType.AIR, row.getMessageType());
            assertTrue(row.getIsRequest());
            assertEquals("sess-1", row.getSessionId());
            assertEquals("mme1.example.com", row.getOriginHost());
            assertEquals("example.com", row.getOriginRealm());
            assertEquals("001010123456789", row.getUserName());
            assertNull(row.getVisitedPlmnId());
            assertNull(row.getResultCode());
        }

        @Test
        @DisplayName("Should parse valid ULR message with visited PLMN ID")
        void shouldParseValidUlrMessage() {
            List<String> lines =
                    List.of(VALID_HEADER, "ULR,true,sess-2,mme1.example.com,example.com,001010123456789,00101,");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
            CsvRow row = result.getFirst();
            assertEquals(MessageType.ULR, row.getMessageType());
            assertTrue(row.getIsRequest());
            assertEquals("00101", row.getVisitedPlmnId());
        }

        @Test
        @DisplayName("Should parse valid AIA answer message")
        void shouldParseValidAiaMessage() {
            List<String> lines  = List.of(VALID_HEADER, "AIA,false,sess-1,hss1.example.com,example.com,,,2001");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
            CsvRow row = result.getFirst();
            assertEquals(MessageType.AIA, row.getMessageType());
            assertFalse(row.getIsRequest());
            assertEquals("2001", row.getResultCode());
        }

        @Test
        @DisplayName("Should parse valid ULA answer message")
        void shouldParseValidUlaMessage() {
            List<String> lines  = List.of(VALID_HEADER, "ULA,false,sess-2,hss1.example.com,example.com,,,2001");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
            CsvRow row = result.getFirst();
            assertEquals(MessageType.ULA, row.getMessageType());
            assertFalse(row.getIsRequest());
        }

        @Test
        @DisplayName("Should parse multiple messages")
        void shouldParseMultipleMessages() {
            List<String> lines = List.of(VALID_HEADER, "AIR,true,sess-1,mme1.example.com,example.com,user1,,",
                                         "AIA,false,sess-1,hss1.example.com,example.com,,,2001",
                                         "ULR,true,sess-2,mme1.example.com,example.com,user2,00101,",
                                         "ULA,false,sess-2,hss1.example.com,example.com,,,2001");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(4, result.size());
        }

        @Test
        @DisplayName("Should treat whitespace-only fields as null")
        void shouldTreatWhitespaceOnlyFieldsAsNull() {
            List<String> lines  = List.of(VALID_HEADER, "AIA,false,sess-1,hss1.example.com,example.com,   ,   ,2001");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
            assertNull(result.getFirst().getUserName());
            assertNull(result.getFirst().getVisitedPlmnId());
        }

        @Test
        @DisplayName("Should trim field values")
        void shouldTrimFieldValues() {
            List<String> lines =
                    List.of(VALID_HEADER, "AIR,true,  sess-1  ,  mme1.example.com  ,  example.com  ,  user1  ,,");

            List<CsvRow> result = parser.parse(lines);

            assertEquals("sess-1", result.getFirst().getSessionId());
            assertEquals("mme1.example.com", result.getFirst().getOriginHost());
        }
    }

    @Nested
    @DisplayName("Line Validation")
    class LineValidation {

        @Test
        @DisplayName("Should skip line with invalid message type")
        void shouldSkipLineWithInvalidMessageType() {
            List<String> lines = List.of(VALID_HEADER, "INVALID_TYPE,true,sess-1,mme1.example.com,example.com,user1,,",
                                         "AIR,true,sess-2,mme1.example.com,example.com,user2,,");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
            assertEquals("sess-2", result.getFirst().getSessionId());
        }

        @Test
        @DisplayName("Should skip line with invalid is_request value")
        void shouldSkipLineWithInvalidIsRequestValue() {
            List<String> lines = List.of(VALID_HEADER, "AIR,maybe,sess-1,mme1.example.com,example.com,user1,,",
                                         "AIR,true,sess-2,mme1.example.com,example.com,user2,,");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
            assertEquals("sess-2", result.getFirst().getSessionId());
        }

        @Test
        @DisplayName("Should skip line with fewer columns than expected")
        void shouldSkipLineWithFewerColumns() {
            List<String> lines =
                    List.of(VALID_HEADER, "AIR,true,sess-1", "AIR,true,sess-2,mme1.example.com,example.com,user2,,");

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
            assertEquals("sess-2", result.getFirst().getSessionId());
        }

        @ParameterizedTest
        @DisplayName("Should accept valid is_request values")
        @CsvSource({"true", "false", "TRUE", "FALSE", "True", "False"})
        void shouldAcceptValidIsRequestValues(String isRequestValue) {
            List<String> lines = List.of(VALID_HEADER,
                                         String.format("AIR,%s,sess-1,mme1.example.com,example.com,user1,,",
                                                       isRequestValue));

            List<CsvRow> result = parser.parse(lines);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should return empty list when only header is present")
        void shouldReturnEmptyListWhenOnlyHeaderPresent() {
            List<String> lines = List.of(VALID_HEADER);

            List<CsvRow> result = parser.parse(lines);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle all lines being invalid")
        void shouldHandleAllLinesBeingInvalid() {
            List<String> lines = List.of(VALID_HEADER, "INVALID,true,sess-1,mme1.example.com,example.com,user1,,",
                                         "ALSO_INVALID,false,sess-2,hss1.example.com,example.com,,,2001");

            List<CsvRow> result = parser.parse(lines);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle empty string in message type")
        void shouldHandleEmptyMessageType() {
            List<String> lines = List.of(VALID_HEADER, ",true,sess-1,mme1.example.com,example.com,user1,,");

            List<CsvRow> result = parser.parse(lines);

            assertTrue(result.isEmpty());
        }
    }
}

