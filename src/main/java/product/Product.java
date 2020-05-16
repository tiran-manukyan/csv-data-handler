package product;

import java.util.List;
import java.util.Objects;

public final class Product {

    private final int productId;

    private final String name;

    private final String condition;

    private final String state;

    private final double price;

    public Product(int productId, String name, String condition, String state, double price) {
        this.productId = productId;
        this.name = name;
        this.condition = condition;
        this.state = state;
        this.price = price;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }

    public String getState() {
        return state;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Product product = (Product) o;
        return productId == product.productId
                && Double.compare(product.price, price) == 0
                && Objects.equals(name, product.name)
                && Objects.equals(condition, product.condition)
                && Objects.equals(state, product.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, condition, state, price);
    }

    /**
     * @throws IllegalArgumentException if data is incorrect
     */
    public static Product fromRaw(String[] data) {
        if (data.length != 5) {
            throw new IllegalArgumentException("data length must be 5");
        }
        int productId;
        try {
            productId = Integer.parseInt(checkNull(data[0]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
        String name = checkNull(data[1]);
        String condition = data[2];
        String state = data[3];
        double price;
        try {
            price = Double.parseDouble(checkNull(data[4]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        return new Product(productId, name, condition, state, price);
    }

    public static Product[] fromRaw(List<String[]> data, boolean ignoreInvalidRow) {
        Product[] products = new Product[data.size()];

        if (ignoreInvalidRow) {
            try {
                for (int i = 0; i < data.size(); i++) {
                    products[i] = fromRaw(data.get(i));
                }
            } catch (IllegalArgumentException ignored) {
            }
        } else {
            for (int i = 0; i < data.size(); i++) {
                products[i] = fromRaw(data.get(i));
            }
        }

        return products;
    }

    public String[] toRaw() {
        String[] data = new String[5];
        data[0] = String.valueOf(this.productId);
        data[1] = this.name;
        data[2] = this.condition;
        data[3] = this.state;
        data[4] = String.valueOf(this.price);
        return data;
    }

    private static <T> T checkNull(T o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }

        return o;
    }
}
