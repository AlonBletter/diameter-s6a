package diameter.csv.parser;

import diameter.csv.model.CsvRow;

import java.util.List;

public interface CsvParser {
    List<CsvRow> parse(List<String> lines);
}
