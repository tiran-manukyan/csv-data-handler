package product;

import csv.CSVReader;
import main.Params;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class ProductService {

    private static final Comparator<Product> PRICE_ASCENDING_COMPARATOR = Comparator.comparingDouble(Product::getPrice);
    private static final Comparator<Product> PRICE_DESCENDING_COMPARATOR = Comparator.comparingDouble(Product::getPrice).reversed();

    public List<Product> getCheapestProducts(List<Path> csvFiles, Params params) {
        if (csvFiles.isEmpty()) {
            throw new IllegalArgumentException("Must be least one file");
        }
        List<Path> files = List.copyOf(csvFiles);

        ExecutorService processors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        BlockingQueue<Product[]> processedProducts = new LinkedBlockingQueue<>();

        int actionsCount = files.size() + 1;
        CountDownLatch processedActions = new CountDownLatch(actionsCount);
        files.forEach(path -> processors.submit(() -> {
            try {
                CSVReader parser = new CSVReader(new FileReader(path.toString()), params.getDelimiter(), params.getProductResultRowsCount());

                Deque<Product[]> productsQueue = new LinkedList<>();

                Iterator<List<String[]>> iterator = parser.iterator();
                if (iterator.hasNext()) { // read first portion
                    List<String[]> rows = iterator.next();
                    Product[] products = Product.fromRaw(rows, true);
                    Arrays.sort(products, PRICE_DESCENDING_COMPARATOR);
                    productsQueue.addLast(products);
                }

                while (iterator.hasNext()) { // read remaining portions and merge with existing
                    List<String[]> rows = iterator.next();
                    Product[] products = Product.fromRaw(rows, true);
                    Arrays.sort(products, PRICE_DESCENDING_COMPARATOR);
                    productsQueue.addLast(products);

                    Product[] products1 = productsQueue.removeFirst();
                    Product[] products2 = productsQueue.removeFirst();
                    Product[] mergedArray = mergeArrays(products1, products2, params.getProductResultRowsCount(), params.getDuplicateProductsMaxCount());
                    productsQueue.addLast(mergedArray);
                }

                processedProducts.put(productsQueue.removeFirst());

            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                processedActions.countDown();
            }
        }));

        Thread mergerThread = new Thread(() -> {
            try {
                int mergesCount = files.size() - 1;
                for (int i = 0; i < mergesCount; i++) {
                    Product[] products1 = processedProducts.take();
                    Product[] products2 = processedProducts.take();

                    Product[] mergedArray = mergeArrays(products1, products2, params.getProductResultRowsCount(), params.getDuplicateProductsMaxCount());
                    processedProducts.put(mergedArray);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                processedActions.countDown();
            }
        });

        mergerThread.start();

        try {
            processedActions.await();
            processors.shutdown();
            Product[] result = processedProducts.take();
            return Arrays.stream(result).filter(Objects::nonNull).sorted(PRICE_ASCENDING_COMPARATOR).collect(Collectors.toList());
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private Product[] mergeArrays(Product[] firstArray, Product[] secondArray, int resultSize, int duplicateMaxCount) {
        Map<Integer, Integer> productsCounter = new HashMap<>();
        Product[] result = new Product[Math.min(firstArray.length + secondArray.length, resultSize)];

        int firstIndex = firstArray.length - 1;
        int secondIndex = secondArray.length - 1;
        int insertionIndex = result.length - 1;

        while (insertionIndex >= 0) {
            if (firstIndex >= 0 && firstArray[firstIndex] != null) {
                if (secondIndex >= 0 && secondArray[secondIndex] != null) {

                    Product firstProduct = firstArray[firstIndex];
                    Product secondProduct = secondArray[secondIndex];
                    if (PRICE_DESCENDING_COMPARATOR.compare(firstProduct, secondProduct) > 0) {
                        int productId = firstProduct.getProductId();
                        Integer count = productsCounter.computeIfAbsent(productId, key -> 0);
                        if (count < duplicateMaxCount) {
                            result[insertionIndex--] = firstProduct;
                            productsCounter.put(productId, count + 1);
                        }
                        firstIndex--;
                    } else {
                        int productId = secondProduct.getProductId();
                        Integer count = productsCounter.computeIfAbsent(productId, key -> 0);
                        if (count < duplicateMaxCount) {
                            result[insertionIndex--] = secondProduct;
                            productsCounter.put(productId, count + 1);
                        }
                        secondIndex--;
                    }
                } else {
                    for (int i = firstIndex; i >= 0 && firstArray[i] != null; i--) {
                        Product product = firstArray[i];
                        int productId = product.getProductId();
                        Integer count = productsCounter.computeIfAbsent(productId, key -> 0);
                        if (count < duplicateMaxCount) {
                            result[insertionIndex--] = product;
                            productsCounter.put(productId, count + 1);
                        }
                    }
                    break;
                }
            } else {
                if (secondIndex >= 0 && secondArray[secondIndex] != null) {
                    for (int i = secondIndex; i >= 0 && secondArray[i] != null; i--) {
                        Product product = secondArray[i];
                        int productId = product.getProductId();
                        Integer count = productsCounter.computeIfAbsent(productId, key -> 0);
                        if (count < duplicateMaxCount) {
                            result[insertionIndex--] = product;
                            productsCounter.put(productId, count + 1);
                        }
                    }
                }
                break;
            }
        }

        return result;
    }
}
