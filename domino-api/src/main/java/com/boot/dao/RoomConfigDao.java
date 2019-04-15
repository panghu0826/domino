package com.boot.dao;

import com.boot.entity.RoomConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoomConfigDao extends JpaRepository<RoomConfigModel,Integer> {
}