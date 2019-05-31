package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.TableCreationRecordsModel;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TableCreationRecordsModelMapper {
    int deleteByPrimaryKey(@Param("tableId") String tableId);

    int insert(TableCreationRecordsModel record);

    TableCreationRecordsModel selectByPrimaryKey(@Param("tableId") String tableId);

    List<TableCreationRecordsModel> selectTableCreateByUserId(String createUserId);

    int selectLastId();

    List<TableCreationRecordsModel> selectAll();

    int updateByPrimaryKey(TableCreationRecordsModel record);
}