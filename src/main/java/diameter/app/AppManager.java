package diameter.app;

import diameter.domain.DiameterMessage;
import diameter.factory.MessageFactory;
import diameter.parser.CsvParser;
import diameter.transaction.TransactionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class AppManager {
    private final CsvParser parser;
    private final TransactionManager transactionManager;

    public AppManager() {
        this.parser = new CsvParser();
        this.transactionManager = TransactionManager.getInstance();
    }

    public void run(String[] args) {
        if (args == null || args.length != 1) {
            System.err.println("Usage: <app> <path-to-csv>");
            return;
        }

        List<String> csvContent = getLinesFromFile(args);
        var rows = parser.parse(csvContent);

        for (var row : rows) {
            DiameterMessage diameterMessage        = MessageFactory.createMessage(row);
            transactionManager.addTransaction(diameterMessage);
        }
    }

    private static List<String> getLinesFromFile(String[] args) {
        try {
            Path csvPath = Path.of(args[0]);
            return Files.readAllLines(csvPath);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

