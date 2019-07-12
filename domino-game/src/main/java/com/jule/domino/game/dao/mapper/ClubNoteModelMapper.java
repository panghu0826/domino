package com.jule.domino.game.dao.mapper;

import com.jule.domino.game.dao.bean.ClubNoteModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClubNoteModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ClubNoteModel record);

    ClubNoteModel selectByPrimaryKey(@Param("clubId") int clubId, @Param("userA") String userA, @Param("userB") String userB);

    List<ClubNoteModel> selectByClubIdAndUserId(@Param("clubId") int clubId, @Param("userId") String userId);

    int updateByPrimaryKey(ClubNoteModel record);
}