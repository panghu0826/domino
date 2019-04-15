package com.boot.service;

import com.boot.dao.RoomConfigDao;
import com.boot.entity.RoomConfigModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author
 * @since 2018/7/18 18:27
 */

@Service
public class RoomConfigService {

    @Autowired
    private RoomConfigDao roomConfigDao;

    public List<RoomConfigModel> selectAllRoom(){
        return roomConfigDao.findAll();
    }

}
