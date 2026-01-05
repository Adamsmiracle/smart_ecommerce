package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.ProductCategoryDao;
import com.amalitech.smartecommerce.dao.ProductCategoryDaoImpl;
import com.amalitech.smartecommerce.model.ProductCategory;

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
    public List<ProductCategory> getAllCategories() {
        return categoryDao.findAll();
    }

    @Override
    public boolean createCategory(ProductCategory category) {
        return categoryDao.insert(category);
    }

    @Override
    public boolean updateCategory(ProductCategory category) {
        return categoryDao.update(category);
    }

    @Override
    public boolean deleteCategory(UUID id) {
        return categoryDao.delete(id);
    }
}

