package diameter.app;

import diameter.csv.parser.CsvParser;
import diameter.exception.validation.DiameterMessageValidationException;
import diameter.reporter.ProcessingResult;
import diameter.reporter.SummaryReporter;
import diameter.domain.message.DiameterMessage;
import diameter.domain.factory.MessageFactory;
import diameter.csv.model.CsvRow;
import diameter.exception.transaction.TransactionException;
import diameter.transaction.TransactionManager;
import diameter.io.FileReader;
import diameter.transaction.TransactionResult;
import diameter.validator.MessageValidator;
import diameter.validator.ValidationResult;

import java.util.ArrayList;
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
        List<ProcessingResult> results = new ArrayList<>();

        for (CsvRow csvRow : csvRows) {
            results.add(processSingleRow(csvRow));
        }

        TransactionResult transactionResult = transactionManager.getTransactionResult();

        summaryReporter.report(results, transactionResult);
    }

    private ProcessingResult processSingleRow(CsvRow csvRow) {
        try {
            ProcessingResult retVal;
            DiameterMessage  diameterMessage  = messageFactory.createDiameterMessage(csvRow);
            ValidationResult validationResult = validator.validate(diameterMessage);

            if (!validationResult.isValid()) {
                retVal = ProcessingResult.validationFailure();
            }
            else {
                transactionManager.processDiameterMessage(diameterMessage);
                retVal = ProcessingResult.success();
            }

            return retVal;
        }
        catch (DiameterMessageValidationException e) {
            return ProcessingResult.validationFailure();
        }
        catch (TransactionException e) {
            return ProcessingResult.error(e.getMessage());
        }
        catch (Exception e) {
            return ProcessingResult.error("Unexpected error: " + e.getMessage());
        }
    }

    private List<CsvRow> getCsvRows(String[] args) {
        List<String> csvContent = FileReader.getLinesFromFile(args);
        return csvParser.parse(csvContent);
    }
}

