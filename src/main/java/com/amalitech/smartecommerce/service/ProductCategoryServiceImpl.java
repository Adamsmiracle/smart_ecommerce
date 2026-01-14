package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.ProductCategoryDao;
import com.amalitech.smartecommerce.dao.ProductCategoryDaoImpl;
import com.amalitech.smartecommerce.model.ProductCategory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryDao categoryDao;

    public ProductCategoryServiceImpl() {
        this.categoryDao = new ProductCategoryDaoImpl();
    }

    public ProductCategoryServiceImpl(ProductCategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public ProductCategory getCategoryById(UUID id) {
        return categoryDao.findById(id);
    }

    @Override
    public List<ProductCategory> getAllCategories() throws SQLException {
        return categoryDao.findAll();
    }

    @Override
    public ProductCategory createCategory(ProductCategory category) throws SQLException {
        return categoryDao.create(category);
    }

    @Override
    public ProductCategory updateCategory(ProductCategory category) throws SQLException {
        return categoryDao.update(category);
    }

    @Override
    public ProductCategory deleteCategory(UUID id) throws SQLException {
        return categoryDao.delete(id);
    }
}

