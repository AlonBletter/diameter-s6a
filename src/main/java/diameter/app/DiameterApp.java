package diameter.app;

import diameter.csv.parser.CsvParserImpl;
import diameter.domain.factory.MessageFactoryImpl;
import diameter.reporter.SummaryReporterImpl;
import diameter.transaction.TransactionManagerImpl;
import diameter.validator.MessageValidatorImpl;

public final class DiameterApp {
    public static void main(String[] args) {
        AppManager appManager =
                new AppManager(new CsvParserImpl(), new MessageFactoryImpl(), TransactionManagerImpl.getInstance(),
                               new MessageValidatorImpl(), new SummaryReporterImpl());
        appManager.run(args);
    }
}