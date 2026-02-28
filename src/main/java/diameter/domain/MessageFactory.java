package diameter.domain;

import diameter.csv.model.CsvRow;
import diameter.domain.message.*;
import diameter.exception.validation.InvalidMessageTypeException;

import java.util.Map;
import java.util.function.Function;

public class MessageFactory {
    private static final Map<MessageType, MessageDefinition> messageDefinitions = Map.of(
            MessageType.AIR, new MessageDefinition(true, row -> new AIR(
                    row.getSessionId(),
                    row.getOriginHost(),
                    row.getOriginRealm(),
                    row.getUserName()
            )),
            MessageType.AIA, new MessageDefinition(false, row -> new AIA(
                    row.getSessionId(),
                    row.getOriginHost(),
                    row.getOriginRealm(),
                    row.getUserName(),
                    row.getResultCode()
            )),
            MessageType.ULR, new MessageDefinition(true, row -> new ULR(
                    row.getSessionId(),
                    row.getOriginHost(),
                    row.getOriginRealm(),
                    row.getUserName(),
                    row.getVisitedPlmnId()
            )),
            MessageType.ULA, new MessageDefinition(false, row -> new ULA(
                    row.getSessionId(),
                    row.getOriginHost(),
                    row.getOriginRealm(),
                    row.getUserName(),
                    row.getResultCode()
            ))
                                                                                        );

    public static DiameterMessage createMessage(CsvRow row) {
        if (row == null || row.getMessageType() == null) {
            throw new IllegalArgumentException("CSV row or message type cannot be null");
        }

        MessageType messageType = row.getMessageType();
        MessageDefinition definition = messageDefinitions.get(messageType);

        if (definition == null) {
            throw new InvalidMessageTypeException("Unsupported message type: " + messageType);
        }

        return definition.create(row);
    }

    private static class MessageDefinition {
        private final boolean                           expectedRequest;
        private final Function<CsvRow, DiameterMessage> constructor;

        public MessageDefinition(boolean expectedRequest, Function<CsvRow, DiameterMessage> constructor) {
            this.expectedRequest = expectedRequest;
            this.constructor = constructor;
        }

        public DiameterMessage create(CsvRow row) {
            if (row.getIsRequest() != expectedRequest) {
                throw new InvalidMessageTypeException(
                        "Type mismatch: " + row.getMessageType() + " expected is_request=" + expectedRequest);
            }
            return constructor.apply(row);
        }
    }
}

