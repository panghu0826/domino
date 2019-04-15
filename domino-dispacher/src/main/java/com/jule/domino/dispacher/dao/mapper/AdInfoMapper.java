package com.jule.domino.dispacher.dao.mapper;


import com.jule.domino.dispacher.dao.bean.AdInfoModel;

public interface AdInfoMapper {
    /**
     * 插入
     * @param model
     * @return
     */
    int insert(AdInfoModel model);

    /**
     * 查询
     * @param uid
     * @return
     */
    AdInfoModel selectByPrimaryKey( String uid );

    /**
     * 更新
     * @param model
     * @return
     */
    int updateByPrimaryKey(AdInfoModel model);
}