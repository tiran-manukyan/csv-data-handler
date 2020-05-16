package main;

import java.util.Objects;

public final class Params {

    private final int duplicateProductsMaxCount;

    private final int productResultRowsCount;

    private final char delimiter;

    private final String directoryPath;

    public Params(int duplicateProductsMaxCount, int productResultRowsCount, char delimiter, String directoryPath) {
        this.duplicateProductsMaxCount = duplicateProductsMaxCount;
        this.productResultRowsCount = productResultRowsCount;
        this.delimiter = delimiter;
        this.directoryPath = directoryPath;
    }

    public int getDuplicateProductsMaxCount() {
        return duplicateProductsMaxCount;
    }

    public int getProductResultRowsCount() {
        return productResultRowsCount;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public static Builder builder(String directoryPath) {
        return new Builder(directoryPath);
    }

    public static class Builder {
        private int duplicateProductsMaxCount = 20;

        private int productResultRowsCount = 1000;

        private char delimiter = ',';

        private final String directoryPath;

        private Builder(String directoryPath) {
            this.directoryPath = Objects.requireNonNull(directoryPath);
        }

        public Builder duplicateProductsMaxCount(int value) {
            this.duplicateProductsMaxCount = value;
            return this;
        }

        public Builder productResultRowsCount(int value) {
            this.productResultRowsCount = value;
            return this;
        }


        public Builder delimiter(char value) {
            this.delimiter = value;
            return this;
        }

        public Params build() {
            return new Params(duplicateProductsMaxCount, productResultRowsCount, delimiter, directoryPath);
        }
    }
}
