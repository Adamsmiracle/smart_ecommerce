package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.ProductDao;
import com.amalitech.smartecommerce.dao.ProductDaoImpl;
import com.amalitech.smartecommerce.model.Product;

import java.util.List;
import java.util.UUID;

public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao;

    public ProductServiceImpl() {
        this.productDao = new ProductDaoImpl();
    }

    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Product getProductById(UUID id) {
        return productDao.findById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    @Override
    public List<Product> getProductsByCategoryId(UUID categoryId) {
        return productDao.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        return productDao.searchByName(name);
    }

    @Override
    public Product createProduct(Product product) {
        return productDao.insert(product);
    }

    @Override
    public Product updateProduct(Product product) {
        return productDao.update(product);
    }

    @Override
    public boolean deleteProduct(UUID id) {
        return productDao.delete(id);
    }
}
