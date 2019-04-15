package com.jule.domino.auth.dao.mapper;

import com.jule.domino.auth.dao.bean.Currency;
import java.util.List;

public interface CurrencyMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Currency record);

    Currency selectByPrimaryKey(Integer id);

    List<Currency> selectAll();

    int updateByPrimaryKey(Currency record);
}