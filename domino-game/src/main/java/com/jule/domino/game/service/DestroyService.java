package com.jule.domino.game.service;

import com.jule.core.jedis.JedisPoolWrap;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.game.config.Config;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.RoomConfigModel;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.base.model.RoomTableRelationModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * 专门来搞破坏的一段代码
 *
 * 测试使用
 *
 */
public class DestroyService {

    private final static Logger logger = LoggerFactory.getLogger(DestroyService.class);

    public static final DestroyService OBJ = new DestroyService();

    private static List<RoomConfigModel> roomConfigList = new ArrayList<>();

    public DestroyService() {
    }

    public void start(){
        if (!Config.MODE_TEST){
            //不是测试模式
            return;
        }

        //2分钟执行一次destory()
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                ()->destory(),1, 5*1000, TimeUnit.MILLISECONDS
        );

    }

    /**
     * 游戏列表
     */
    private List<Integer> GAMEID_LIST = Arrays.asList(91001001,91001002,91001003,91001004);

    /**
     * 搞事情
     */
    public void destory(){
        logger.info("开始扫描全部房间");
        if (roomConfigList==null || roomConfigList.size() == 0){
            roomConfigList = DBUtil.getRoomConfigFromDb();
        }

        GAMEID_LIST.forEach(gameId->{
            roomConfigList.forEach(config->{
                String roomId = config.getRoomId();
                String key4RoomTableRelation = RedisConst.TABLE_INSTANCE.getProfix() + gameId + roomId;

                Map<String, String> tableMap = JedisPoolWrap.getInstance().hGetAll(key4RoomTableRelation);
                if (tableMap == null || tableMap.size() == 0){
                    return;
                }

                //遍历table
                tableMap.forEach((k,v)->{
                    RoomTableRelationModel model = StoredObjManager.getStoredObjInMap(RoomTableRelationModel.class, key4RoomTableRelation, k);
                    if (model == null){
                        return;
                    }

                    String tableId = model.getTableId();
                    String key4tableSeat = RedisConst.TABLE_SEAT.getProfix() + gameId + roomId + tableId;
                    Map<String, String> seatMap = JedisPoolWrap.getInstance().hGetAll(key4tableSeat);
                    if (seatMap == null){
                        return;
                    }

                    List<String> users = new ArrayList<>();
                    seatMap.forEach((k1,v1)->{
                        users.add(k1);
                    });

                    if (seatMap.size() <= 2){
                        return;
                    }
//                    logger.debug(MessageFormat.format("开始移除user={0},game={1},room={2},table={3}"
//                            ,users,gameId,roomId,tableId));
                    //如果桌位上人比较多、就随便起来一个
                    Random random = new Random();
                    int _index = random.nextInt(users.size());

                    String user = seatMap.get(users.get(_index));
                    if (StringUtils.isEmpty(user)){
                        return;
                    }

                    //把这个玩家信息干掉
                    AbstractTable table = UserTableService.getInstance().getTableByUserId(Long.parseLong("401556334833"));
//                    logger.debug("user:"+user);
                    if (table == null){
                        return;
                    }
                    //删除当前行动者
                    //PlayerInfo player = table.getAllPlayers().get(table.getCurrActionPlayerId());
                    //table.getAllPlayers().remove(player.getPlayerId());
                });
            });
        });

    }

}
