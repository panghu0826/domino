package com.jule.domino.game.gate.network.process.reqs;

import JoloProtobuf.AuthSvr.JoloAuth;
import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.RedisConst;
import com.jule.domino.game.dao.DBUtil;
import com.jule.domino.game.dao.bean.FriendTableModel;
import com.jule.domino.game.gate.network.protocol.Req;
import com.jule.domino.game.log.producer.RabbitMqSender;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
        List<JoloAuth.JoloCommon_UserInfos> array = new ArrayList<>();
        String userId = req.getMeUserId();
        String friendUserId = req.getYouUserId();
        int args = req.getArgs();
        ack.setArgs(args).setUserId(userId).setYouUserId(friendUserId);
        try {
            User user = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + userId, User.class);
            switch (args) {
                case 1://开启好友功能
                    user.setMei_code("true");
                    DBUtil.updateByPrimaryKey(user);
                    break;
                case 2://关闭好友功能
                    user.setMei_code("false");
                    DBUtil.updateByPrimaryKey(user);
                    break;
                case 3://好友列表
                    List<FriendTableModel> list = DBUtil.selectFriendByUserId(userId);
                    list.forEach(e ->
                            array.add(JoloAuth.JoloCommon_UserInfos.newBuilder()
                                    .setUserId(e.getFriendUserId())
                                    .setNickName(e.getFriendNickName())
                                    .setIcoUrl(e.getFriendIcoUrl())
                                    .setState(e.getState()).build())
                    );
                    break;
                case 4://请求好友
                    //1.未同意  2.等待同意 3.好友
                    User friend = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + friendUserId, User.class);
                    if (friend == null) {//如果缓存找不到则查库
                        friend = DBUtil.selectByPrimaryKey(friendUserId);
                    }
                    FriendTableModel ftmA = DBUtil.selectByUserIdAndFriendId(userId, friendUserId);
                    if (ftmA == null) {
                        FriendTableModel ftmMe = new FriendTableModel();
                        ftmMe.setUserId(userId);
                        ftmMe.setFriendUserId(friendUserId);
                        ftmMe.setFriendNickName(friend.getNick_name());
                        ftmMe.setFriendIcoUrl(friend.getIco_url());
                        ftmMe.setState(2);
                        DBUtil.insertFriend(ftmMe);
                    }
                    FriendTableModel ftmB = DBUtil.selectByUserIdAndFriendId(friendUserId, userId);
                    if (ftmB == null) {
                        FriendTableModel ftmYou = new FriendTableModel();
                        ftmYou.setUserId(friendUserId);
                        ftmYou.setFriendUserId(userId);
                        ftmYou.setFriendNickName(user.getNick_name());
                        ftmYou.setFriendIcoUrl(user.getIco_url());
                        ftmYou.setState(1);
                        DBUtil.insertFriend(ftmYou);
                    }
                    break;
                case 5://同意好友
                    FriendTableModel ftmme = DBUtil.selectByUserIdAndFriendId(userId, friendUserId);
                    ftmme.setAddTime(new Date());
                    ftmme.setState(3);
                    DBUtil.updateFriend(ftmme);
                    FriendTableModel ftmyou = DBUtil.selectByUserIdAndFriendId(friendUserId, userId);
                    ftmyou.setAddTime(new Date());
                    ftmyou.setState(3);
                    DBUtil.updateFriend(ftmyou);
                    break;
                case 6://移除好友
                    DBUtil.deleteFriend(userId, friendUserId);
                    DBUtil.deleteFriend(friendUserId, userId);
                    break;
                case 7://查看是否好友
                    FriendTableModel ftm = DBUtil.selectByUserIdAndFriendId(userId, friendUserId);
                    if (ftm == null) {//没有好友申请记录
                        User otherParty = StoredObjManager.hget(RedisConst.USER_INFO.getProfix(), RedisConst.USER_INFO.getField() + friendUserId, User.class);
                        if (otherParty == null) {//如果缓存找不到则查库
                            otherParty = DBUtil.selectByPrimaryKey(friendUserId);
                        }
                        array.add(JoloAuth.JoloCommon_UserInfos.newBuilder()
                                .setUserId(otherParty.getId())
                                .setNickName(otherParty.getNick_name())
                                .setIcoUrl(otherParty.getIco_url())
                                .setState(0).build());
                    } else {
                        array.add(JoloAuth.JoloCommon_UserInfos.newBuilder()
                                .setUserId(ftm.getFriendUserId())
                                .setNickName(ftm.getFriendNickName())
                                .setIcoUrl(ftm.getFriendIcoUrl())
                                .setState(ftm.getState()).build());
                    }
                    break;
            }
            sendResponse(functionId | 0x08000000, ack.setResult(1).addAllUserInfos(array).build().toByteArray());
        } catch (Exception e) {
            sendResponse(functionId | 0x08000000, ack.setResult(-1).setResultMsg("请求失败").addAllUserInfos(array).build().toByteArray());
            e.printStackTrace();
        }
    }
}
