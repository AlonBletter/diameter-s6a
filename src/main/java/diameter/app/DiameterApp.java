package diameter.app;

import diameter.csv.parser.CsvParserImpl;
import diameter.domain.factory.MessageFactoryImpl;
import diameter.io.FileReaderImpl;
import diameter.reporter.SummaryReporterImpl;
import diameter.transaction.TransactionManagerImpl;
import diameter.validator.MessageValidatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiameterApp {
    private static final Logger LOG = LoggerFactory.getLogger(DiameterApp.class);

    public static void main(String[] args) {
        LOG.info("Diameter S6a Processor starting");
        long startTime = System.currentTimeMillis();

        try {
            AppManager appManager = new AppManager(new FileReaderImpl(), new CsvParserImpl(), new MessageFactoryImpl(),
                                                   TransactionManagerImpl.getInstance(), new MessageValidatorImpl(),
                                                   new SummaryReporterImpl());
            appManager.run(args);

            long duration = System.currentTimeMillis() - startTime;
            LOG.info("Diameter S6a Processor completed successfully in {}ms", duration);
        }
        catch (Exception e) {
            LOG.error("Diameter S6a Processor terminated with error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}