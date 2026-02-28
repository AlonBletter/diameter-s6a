package diameter.app;

import diameter.csv.model.MessagesSummary;
import diameter.domain.message.DiameterMessage;
import diameter.domain.MessageFactory;
import diameter.csv.model.CsvRow;
import diameter.csv.parser.CsvParser;
import diameter.exception.transaction.TransactionException;
import diameter.exception.validation.ValidationException;
import diameter.transaction.TransactionManager;
import diameter.io.FileReader;
import diameter.validator.MessageValidator;
import diameter.validator.ValidationResult;

import java.util.List;

public final class AppManager {
    private final CsvParser          parser;
    private final MessageValidator   validator;
    private final TransactionManager transactionManager;
    private final MessagesSummary    messagesSummary;

    public AppManager() {
        this.parser = new CsvParser();
        this.validator = new MessageValidator();
        this.transactionManager = TransactionManager.getInstance();
        this.messagesSummary = new MessagesSummary();
    }

    public void run(String[] args) {
        if (args == null || args.length != 1) {
            System.err.println("Usage: <app> <path-to-csv>");
            return;
        }

        var rows = getCsvRows(args);
        handleMessagesToTransactions(rows);
    }

    private void handleMessagesToTransactions(List<CsvRow> rows) {
        for (var row : rows) {
            try {
                messagesSummary.incrementTotalMessages();
                DiameterMessage  diameterMessage = MessageFactory.createMessage(row);
                ValidationResult validationResult = validator.validate(diameterMessage);

                if (validationResult.isValid()) {
                    messagesSummary.incrementNumberOfValidMessages();
                    transactionManager.addMessage(diameterMessage);
                } else {
                    messagesSummary.incrementNumberOfInvalidMessages();
                }
            } catch (TransactionException e) {
                System.err.println(e.getMessage());
            } catch (ValidationException e) {
                messagesSummary.incrementNumberOfInvalidMessages();
            } catch (Exception e) {
                System.err.println("Unexpected error processing message: " + e.getMessage());
            }
        }

        messagesSummary.setNumberOfCompletedTransactions(transactionManager.getNumberOfCompleteTransactions());
        messagesSummary.setNumberOfIncompleteTransactions(transactionManager.getNumberOfIncompleteTransactions());

        System.out.println(messagesSummary.getReport());
    }

    private List<CsvRow> getCsvRows(String[] args) {
        List<String> csvContent = FileReader.getLinesFromFile(args);
        return parser.parse(csvContent);
    }
}

