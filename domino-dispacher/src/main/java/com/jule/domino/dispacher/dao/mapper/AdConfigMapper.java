package com.jule.domino.dispacher.dao.mapper;


import com.jule.domino.dispacher.dao.bean.AdConfigModel;

public interface AdConfigMapper {

    /**
     * 查询
     * @param id
     * @return
     */
    AdConfigModel selectByPrimaryKey( int id );

    /**
     * 查询
     * @return
     */
    AdConfigModel selectSingle();

}