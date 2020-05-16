package csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class CSVReader implements Iterable<List<String[]>> {

    private final BufferedReader reader;

    private final char delimiter;

    private final int rowsCount;

    public CSVReader(Reader reader, char delimiter, int rowsCount) {
        this.reader = new BufferedReader(reader);
        this.delimiter = delimiter;
        this.rowsCount = rowsCount;
    }

    @Override
    public Iterator<List<String[]>> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<List<String[]>> {

        private String[] lastReadRow;

        @Override
        public boolean hasNext() {
            if (lastReadRow != null) {
                return true;
            } else {
                String[] row = readRow();
                lastReadRow = row;
                return row != null;
            }
        }

        @Override
        public List<String[]> next() {
            List<String[]> buffer = new ArrayList<>(rowsCount + 1);

            if (lastReadRow != null) {
                String[] temp = this.lastReadRow;
                lastReadRow = null;
                buffer.add(temp);

                readRows(buffer, rowsCount - 1);
            } else {
                readRows(buffer, rowsCount);
            }
            return buffer;
        }

        private String[] readRow() {
            String row;
            try {
                row = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (row == null) {
                return null;
            }

            String[] data = new String[5];

            int i = 0;
            int previousIndex = -1;
            do {
                int currentIndex = row.indexOf(delimiter, previousIndex + 1);
                String substring = row.substring(previousIndex + 1, currentIndex == -1 ? row.length() : currentIndex);
                data[i] = substring;
                previousIndex = currentIndex;
                i++;
            } while (i < data.length && previousIndex != -1);

            return data;
        }

        private void readRows(List<String[]> buffer, int count) {
            for (int i = 0; i < count; i++) {
                String[] row = readRow();
                if (row == null) {
                    if (buffer.isEmpty()) {
                        throw new NoSuchElementException();
                    } else {
                        return;
                    }
                }
                buffer.add(row);
            }
        }
    }
}
