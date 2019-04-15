package com.jule.domino.dispacher.dao.mapper;

import com.jule.domino.dispacher.dao.bean.BlackWhiteModel;

import java.util.List;


public interface BlackWhiteMapper {

    BlackWhiteModel selectByPrimaryKey(String uid );

    List<BlackWhiteModel> selectAllBlack();

}