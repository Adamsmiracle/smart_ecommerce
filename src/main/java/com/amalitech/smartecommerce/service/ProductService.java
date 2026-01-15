package com.amalitech.smartecommerce.service;

import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductItem;


public interface ProductService {
    Product getProductById(UUID id);

    List<Product> getAllProducts();

    List<Product> getProductsByCategoryId(UUID categoryId);

    List<Product> searchProductsByName(String name);

    Product createProduct(Product product);

    Product createProductWithPrice(Product product, double price, int stock);

    Product updateProduct(Product product);

    Product deleteProduct(UUID id);

    ProductItem updateProductStock(ProductItem productItem);

    ProductItem getProductItemByProductId(UUID productId);

    List<ProductItem> getAllProductItems();
}

