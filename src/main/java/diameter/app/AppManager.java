package diameter.app;

import diameter.csv.parser.CsvParser;
import diameter.exception.validation.DiameterMessageValidationException;
import diameter.reporter.SummaryReporter;
import diameter.domain.message.DiameterMessage;
import diameter.domain.MessageFactory;
import diameter.csv.model.CsvRow;
import diameter.exception.transaction.TransactionException;
import diameter.transaction.TransactionManager;
import diameter.io.FileReader;
import diameter.validator.MessageValidator;
import diameter.validator.ValidationResult;

import java.util.List;

public final class AppManager {
    private final CsvParser          csvParser;
    private final MessageFactory     messageFactory;
    private final MessageValidator   validator;
    private final TransactionManager transactionManager;
    private final SummaryReporter    summaryReporter;

    public AppManager(CsvParser csvParser,
                      MessageFactory messageFactory,
                      TransactionManager transactionManager,
                      MessageValidator validator,
                      SummaryReporter summaryReporter) {
        this.csvParser = csvParser;
        this.messageFactory = messageFactory;
        this.validator = validator;
        this.transactionManager = transactionManager;
        this.summaryReporter = summaryReporter;
    }

    public void run(String[] args) {
        if (args == null || args.length != 1) {
            System.err.println("Usage: <app> <path-to-csv>");
            return;
        }

        List<CsvRow> rows = getCsvRows(args);
        handleMessagesToTransactions(rows);
    }

    private void handleMessagesToTransactions(List<CsvRow> csvRows) {
        for (CsvRow csvRow : csvRows) {
            try {
                summaryReporter.incrementTotalMessages();
                DiameterMessage  diameterMessage = messageFactory.createDiameterMessage(csvRow);
                ValidationResult validationResult = validator.validate(diameterMessage);
                handleValidationResult(validationResult, diameterMessage);
            } catch (TransactionException e) {
                System.err.println(e.getMessage());
            } catch (DiameterMessageValidationException e) {
                summaryReporter.incrementNumberOfInvalidMessages();
            } catch (Exception e) {
                System.err.println("Unexpected error processing message: " + e.getMessage());
            }
        }

        summaryReporter.setNumberOfCompletedTransactions(transactionManager.getNumberOfCompleteTransactions());
        summaryReporter.setNumberOfIncompleteTransactions(transactionManager.getNumberOfIncompleteTransactions());

        System.out.println(summaryReporter.getReport());
    }

    private List<CsvRow> getCsvRows(String[] args) {
        List<String> csvContent = FileReader.getLinesFromFile(args);
        return csvParser.parse(csvContent);
    }

    private void handleValidationResult(ValidationResult validationResult, DiameterMessage diameterMessage) {
        if (validationResult.isValid()) {
            summaryReporter.incrementNumberOfValidMessages();
            transactionManager.processDiameterMessage(diameterMessage);
        } else {
            summaryReporter.incrementNumberOfInvalidMessages();
        }
    }
}

