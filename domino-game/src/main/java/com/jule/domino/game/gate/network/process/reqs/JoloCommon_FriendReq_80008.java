package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.ClubModel;
import com.jule.domino.game.dao.bean.ClubNoteModel;
import com.jule.domino.game.dao.bean.FriendTableModel;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.TableService;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 请求牌局记录
 */
public class JoloCommon_FriendReq_80008 extends Req {

    private final static Logger logger = LoggerFactory.getLogger(JoloCommon_FriendReq_80008.class);

    private JoloAuth.JoloCommon_FriendReq req;

    public JoloCommon_FriendReq_80008(int functionId) {
        super(functionId);
    }

    @Override
    public void readPayLoadImpl(ByteBuf buf) throws Exception {
        byte[] blob = new byte[buf.readableBytes()];
        buf.readBytes(blob);
        req = JoloAuth.JoloCommon_FriendReq.parseFrom(blob);
        RabbitMqSender.me.producer(functionId, req.toString());
    }

    @Override
    public void processImpl() throws Exception {
        JoloAuth.JoloCommon_FriendAck.Builder ack = JoloAuth.JoloCommon_FriendAck.newBuilder();
        String userId = req.getUserId();
        int args = req.getArgs();
        ack.setArgs(args).setUserId(userId);
        //俱乐部成员使用friend_table表 之前的好友表
        try {
            //1.开启功能 2.关闭功能 3.成员列表 4.请求加入 5.同意加入 6.移除成员 7.查看房间列表 8.改俱乐部名称 9.改成员备注
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            if (args == 1) { //开启功能
                if (user.getMoney() >= 10) {
                    user.setMoney(user.getMoney() - 10);
                    DBUtil.updateByPrimaryKey(user);
                    ClubModel cm = new ClubModel();
                    cm.setName(req.getClubName());
                    cm.setUserId(userId);
                    cm.setNickName(user.getNick_name());
                    cm.setIcoUrl(user.getIco_url());
                    cm.setTime(new Date());
                    DBUtil.insertClub(cm);
                    ClubModel clubModel = DBUtil.selectLastData(userId);
                    sendResponse(functionId | 0x08000000, ack.addClubInfos(setClubInfo(clubModel)).setResult(1).build().toByteArray());
                } else {
                    sendResponse(functionId | 0x08000000, ack.setResult(-1).setResultMsg("房卡数量不足。").build().toByteArray());
                }
            } else if (args == 2) { //关闭功能 todo 待定

            } else if (args == 3) { //成员列表
                sendResponse(functionId | 0x08000000, ack.setClubUsers(setClubUsers(req.getClubId(), req.getUserId())).setResult(1).build().toByteArray());
            } else if (args == 4) { //请求加入 1.未同意  2.成员
                FriendTableModel ftmA = DBUtil.selectByUserIdAndFriendId(String.valueOf(req.getClubId()), req.getUserId());
                if (ftmA == null) {
                    FriendTableModel ftmMe = new FriendTableModel();
                    ftmMe.setUserId(String.valueOf(req.getClubId()));
                    ftmMe.setFriendUserId(user.getId());
                    ftmMe.setFriendNickName(user.getNick_name());
                    ftmMe.setFriendIcoUrl(user.getIco_url());
                    ftmMe.setState(1);
                    DBUtil.insertFriend(ftmMe);
                }
                sendResponse(functionId | 0x08000000, ack.setResult(1).build().toByteArray());
            } else if (args == 5) { //同意好友
                FriendTableModel ftm = DBUtil.selectByUserIdAndFriendId(String.valueOf(req.getClubId()), req.getUserId());
                ftm.setAddTime(new Date());
                ftm.setState(2);
                DBUtil.updateFriend(ftm);
                sendResponse(functionId | 0x08000000, ack.setClubUsers(setClubUsers(req.getClubId(), req.getUserId())).setResult(1).build().toByteArray());
            } else if (args == 6) {
                DBUtil.deleteFriend(String.valueOf(req.getClubId()), req.getUserId());
                sendResponse(functionId | 0x08000000, ack.setResult(1).build().toByteArray());
            } else if (args == 7) { //查看房间列表
                sendResponse(functionId | 0x08000000, ack.addAllRoomInfos(setRoomInfos(req.getClubId())).setResult(1).build().toByteArray());
            } else if (args == 8) {    //修改俱乐部名称
                ClubModel cm = DBUtil.selectByClubId(req.getClubId());
                cm.setName(req.getClubName());
                DBUtil.updateByClub(cm);
                sendResponse(functionId | 0x08000000, ack.setResult(1).build().toByteArray());
            } else if (args == 9) {    //修改成员备注 todo 待定
                String userMe = req.getClubName();//此处是发起请求的玩家id
                String userYou = req.getUserId();//将要设置备注的玩家id
                ClubNoteModel cnm = DBUtil.selectByClubNote(req.getClubId(), userMe, userYou);
                if (cnm == null) {
                    ClubNoteModel clubNoteModel = new ClubNoteModel();
                    clubNoteModel.setClubId(req.getClubId());
                    clubNoteModel.setUserId(userMe);
                    clubNoteModel.setNoteUserId(userYou);
                    clubNoteModel.setNoteName(req.getUserName());
                    DBUtil.insertClubNote(clubNoteModel);
                } else {
                    cnm.setNoteName(req.getUserName());
                    DBUtil.updateByClubNote(cnm);
                }
                sendResponse(functionId | 0x08000000, ack.setResult(1).build().toByteArray());
            } else if (args == 10) {   //查看俱乐部列表
                List<ClubModel> list = DBUtil.selectByClubId(req.getUserId());
                sendResponse(functionId | 0x08000000, ack.addAllClubInfos(setClubInfos(list)).setResult(1).build().toByteArray());
            }
        } catch (Exception e) {
            sendResponse(functionId | 0x08000000, ack.setResult(-1).setResultMsg("请求失败").build().toByteArray());
            e.printStackTrace();
        }
    }

    private JoloAuth.JoloCommon_ClubInfos setClubInfo(ClubModel cm) {
        return JoloAuth.JoloCommon_ClubInfos.newBuilder()
                .setClubId(cm.getId())
                .setClubName(cm.getName())
                .setUserId(cm.getUserId())
                .setNickName(cm.getNickName())
                .setIcoUrl(cm.getIcoUrl())
                .setTime(cm.getTime()).build();
    }

    private List<JoloAuth.JoloCommon_ClubInfos> setClubInfos(List<ClubModel> list) {
        List<JoloAuth.JoloCommon_ClubInfos> array = new ArrayList<>();
        list.forEach(cm ->
                array.add(JoloAuth.JoloCommon_ClubInfos.newBuilder()
                        .setClubId(cm.getId())
                        .setClubName(cm.getName())
                        .setUserId(cm.getUserId())
                        .setNickName(cm.getNickName())
                        .setIcoUrl(cm.getIcoUrl())
                        .setTime(cm.getTime()).build())
        );
        return array;
    }

    private JoloAuth.JoloCommon_ClubUsers setClubUsers(int clubId, String userId) {
        List<FriendTableModel> list = DBUtil.selectFriendByClubId(String.valueOf(clubId));
        List<JoloAuth.JoloCommon_UserInfos> array = new ArrayList<>();
        list.forEach(e ->
                array.add(JoloAuth.JoloCommon_UserInfos.newBuilder()
                        .setUserId(e.getFriendUserId())
                        .setNickName(e.getFriendNickName())
                        .setIcoUrl(e.getFriendIcoUrl())
                        .setState(e.getState()).build())
        );
        List<JoloAuth.JoloCommon_UserNotes> list1 = new ArrayList<>();
        List<ClubNoteModel> list2 = DBUtil.selectByClubIdAndUserId(clubId, userId);
        list2.forEach(e ->
                list1.add(JoloAuth.JoloCommon_UserNotes.newBuilder()
                        .setNoteUserId(e.getNoteUserId())
                        .setNoteName(e.getNoteName()).build())
        );
        return JoloAuth.JoloCommon_ClubUsers.newBuilder().addAllUserInfos(array).addAllUserNotes(list1).build();
    }

    private List<JoloAuth.JoloCommon_RoomInfos> setRoomInfos(int clubId) {
        Collection<AbstractTable> collection = TableService.getInstance().getTableList("1", "10");//此处的参数直接写死
        List<JoloAuth.JoloCommon_RoomInfos> array = new ArrayList<>();
        if (collection != null) {
            collection.forEach(table -> {
                if (table.getClubId() == clubId) {
                    List<JoloAuth.JoloCommon_PlayInfos> list = new ArrayList<>();
                    table.getInGamePlayersBySeatNum().values().forEach(player ->
                            list.add(JoloAuth.JoloCommon_PlayInfos.newBuilder()
                                    .setUserId(player.getPlayerId())
                                    .setNickName(player.getNickName())
                                    .setIcoUrl(player.getIcon())
                                    .setSeatNum(player.getSeatNum()).build())
                    );
                    array.add(JoloAuth.JoloCommon_RoomInfos.newBuilder()
                            .setGameId(table.getGameType())
                            .setTableId(table.getTableId())
                            .setPlayerNum(table.getPlayerNum())
                            .setGameNum(table.getGameNum())
                            .setMaxBet(table.getBetMaxScore())
                            .setCurrGameNum(table.getCurrGameNum())
                            .addAllPlayerS(list).build());
                }
            });
        }
        return array;
    }
}
