package diameter.parser;

import diameter.model.CsvRow;

import java.util.ArrayList;
import java.util.List;

public class CsvParser {
    public List<CsvRow> parse(List<String> lines) {
        List<CsvRow> rows = new ArrayList<>();

        for (String line : lines) {
            rows.add(new CsvRow(line));
        }

        return rows;
    }
}

