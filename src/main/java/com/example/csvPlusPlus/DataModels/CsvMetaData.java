package main.java.com.example.csvPlusPlus.DataModels;

import java.text.NumberFormat;
import java.util.List;

public class CsvMetaData {

    private List<String> headers;
    private String fileName;
    private int rowCount;
    private long sizeInKb;


    public String[][] getFormattedMetaData() {
        String[][] data = {
                {"Number of Columns: ", Integer.toString(getHeaders().size())},
                {"Number of Rows: ", NumberFormat.getInstance().format(getRowCount())},
                {"Size of File: ", String.format("%.2f MB", getSizeInKb() * 0.0000009765625)}
        };

        return data;
    }

    public CsvMetaData(Builder builder) {
        this.headers = builder.headers;
        this.fileName = builder.fileName;
        this.rowCount = builder.rowCount;
        this.sizeInKb = builder.sizeInKb;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public int getRowCount() {
        return rowCount;
    }

    public long getSizeInKb() {
        return sizeInKb;
    }

    public String getFileName() {
        return fileName;
    }

    public static class Builder {
        private List<String> headers;
        private String fileName;
        private int rowCount;
        private long sizeInKb;

        public Builder withHeaders(List<String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withRowCount(int rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public Builder withSizeInKb(long sizeInKb) {
            this.sizeInKb = sizeInKb;
            return this;
        }


        public CsvMetaData build() {
            return new CsvMetaData(this);
        }
    }
}


