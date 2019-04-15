package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.base.dao.bean.Product;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(String pid);

    int insert(Product record);

    Product selectByPrimaryKey(String pid);

    List<Product> selectAll();

    List<Product> selectAllByType(String containType);

    int updateByPrimaryKey(Product record);
}