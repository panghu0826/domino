package com.jule.domino.auth.dao.mapper;

import com.jule.domino.auth.dao.bean.Payment;
import java.util.List;

public interface PaymentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Payment record);

    Payment selectByPrimaryKey(Integer id);

    Payment selectByStatement(String statement);

    List<Payment> selectAll();

    int updateByPrimaryKey(Payment record);

    List<Payment> selectAllByUser(String reserved2);
}