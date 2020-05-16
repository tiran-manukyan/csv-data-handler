package main;

import csv.CSVWriter;
import product.Product;
import product.ProductService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Main {

    public static void main(String[] args) throws IOException {
        Params params;
        try {
            params = resolveParams(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        List<Path> csvFiles = getCSVFiles(params.getDirectoryPath());
        if (csvFiles.isEmpty()) {
            System.err.println(String.format("No CSV files were found in the directory: %s", params.getDirectoryPath()));
            return;
        }

        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.println(String.format("%s - Starting process of %s CSV files...", startTime, csvFiles.size()));

        ProductService productService = new ProductService();

        List<Product> cheapestProducts = productService.getCheapestProducts(csvFiles, params);

        String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.println(String.format("%s - Process end", endTime));

        List<String[]> rawData = cheapestProducts.stream().map(Product::toRaw).collect(toList());

        writeProductsToCSVFile(rawData, params.getDelimiter());
    }

    private static Params resolveParams(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("no args found!");
        }

        Map<String, String> paramsMap = new HashMap<>();
        for (String arg : args) {
            String[] strings = arg.split("=");
            if (strings.length == 2) {
                String key = strings[0];
                String value = strings[1];

                paramsMap.put(key, value);
            } else {
                System.err.println(String.format("Invalid argument %s, ignoring...", arg));
            }
        }

        String directoryPath = paramsMap.get("directoryPath");
        if (directoryPath == null) {
            throw new IllegalArgumentException("Directory path is mandatory");
        }

        Params.Builder builder = Params.builder(directoryPath);

        String delimiter = paramsMap.get("delimiter");
        String productResultRowsCount = paramsMap.get("productResultRowsCount");
        String duplicateProductsMaxCount = paramsMap.get("duplicateProductsMaxCount");

        if (delimiter != null) {
            if (delimiter.length() != 1) {
                throw new IllegalArgumentException("delimiter length must be 1");
            } else {
                builder.delimiter(delimiter.charAt(0));
            }
        }

        if (productResultRowsCount != null) {
            try {
                builder.productResultRowsCount(Integer.parseInt(productResultRowsCount));
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("productResultRowsCount should be number");
            }
        }

        if (duplicateProductsMaxCount != null) {
            try {
                builder.duplicateProductsMaxCount(Integer.parseInt(duplicateProductsMaxCount));
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("duplicateProductsMaxCount should be number");
            }
        }

        return builder.build();
    }

    private static void writeProductsToCSVFile(List<String[]> data, char delimiter) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Specify output file: ");
        String filePath = scanner.nextLine();
        Path path = Path.of(filePath);
        if (Files.isDirectory(path)) {
            System.err.println("This is a existing directory, goodbye.");
            return;
        }

        CSVWriter writer;

        if (Files.exists(path)) {
            System.out.print("File is already exists, overwrite it? [Y/N] :");

            String answer = scanner.nextLine();
            switch (answer.toLowerCase()) {
                case "y":
                case "yes":
                    writer = new CSVWriter(true, delimiter);
                    break;
                default:
                    System.out.println("Goodbye!");
                    return;
            }
        } else {
            writer = new CSVWriter(false, delimiter);
        }

        writer.write(path, data);

        System.out.println("Output file: " + path.toAbsolutePath());
    }

    private static List<Path> getCSVFiles(String directoryPath) {
        try (Stream<Path> files = Files.walk(Paths.get(directoryPath), 1)) {
            return files.filter(path -> Files.isRegularFile(path)).filter(file -> file.toString().endsWith(".csv")).collect(toList());
        } catch (IOException e) {
            throw new RuntimeException("See cause.", e);
        }
    }
}
