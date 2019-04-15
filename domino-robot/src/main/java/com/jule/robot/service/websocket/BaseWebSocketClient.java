package com.jule.robot.service.websocket;

import com.jule.robot.config.Config;
import com.jule.robot.dao.DBUtil;
import com.jule.robot.model.HandCardTypeModel;
import com.jule.robot.model.PlayerInfo;
import com.jule.robot.service.holder.FunctionIdHolder;
import com.jule.robot.service.holder.RobotClientHolder;
import com.jule.robot.service.holder.RobotMoneyPoolHolder;
import io.netty.buffer.ByteBufUtil;
import lombok.Getter;
import lombok.Setter;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * webScoket连接对象的基类，业务逻辑层的webSocket数据传输封装类需要继承本基类
 */
@Getter@Setter
public abstract class BaseWebSocketClient {
    private final static Logger logger = LoggerFactory.getLogger(BaseWebSocketClient.class);
    protected int gameId;
    protected String roomId;
    protected String tableId;
    protected int seatNum = 0; //座位号
    protected String userId;
    protected String nickName = ""; //昵称
    protected int gameSvrId;
    protected int mixedCardIndex = 0; //本局内混牌ID(1-13的索引ID)
    private double bootAmount = 0; //牌局底注金额

    /**
     * 当前在牌局内游戏的玩家
     * <userId,PlayerInfo>
     */
    protected ConcurrentMap<String, PlayerInfo> onTablePlayers = new ConcurrentHashMap<>();
    /**
     * 已站起用户
     */
    protected ConcurrentMap<String, PlayerInfo> standupPlayers = new ConcurrentHashMap<>();

    /*
     * 已坐下的玩家列表<位置，playerInfo>
     * key：seatNum
     */
    protected Map<Integer, PlayerInfo> inGamePlayersBySeatNum = new ConcurrentHashMap<>();
    private WebSocketClient client = null;
    protected HandCardTypeModel cardModel = null;
    public void setCardModel(HandCardTypeModel cardModel) {
        if(null != cardModel){
            logger.info("机器人看牌，手牌->"+cardModel.toStringCard()+", userId->"+userId+", nickName->"+nickName+", roomId->"+roomId+", tableId->"+tableId+", gameId->"+gameId);
        }
        this.cardModel = cardModel;
    }

    private String clientNum = (int) (System.currentTimeMillis() / 1000) + "_" + new Random(System.currentTimeMillis()).nextInt(30);
    protected String gameOrderId = "";

    public BaseWebSocketClient() {
    }

    protected BaseWebSocketClient(String webSocketURI) {
        connectWebSocket(webSocketURI);
    }

    /**
     * 抽象方法：解析FunctionId，子类实现此方法，实现根据消息ID跳转到对应的逻辑方法中
     *
     * @param functionId
     * @param bytes      去掉header的消息体
     */
    protected abstract void routeFunctionId(int functionId, byte[] bytes);

    protected boolean connectWebSocket(String webSocketURI) {
        try {
            client = new WebSocketClient(new URI(webSocketURI), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake arg0) {
                    logger.debug("open connection. webURI->{}", webSocketURI);
                    webSocketOpen();
                }

                public void onMessage(String arg0) {
                    logger.debug("recive String msg. msg->" + arg0);
                }

                @Override
                public void onError(Exception arg0) {
                    logger.error("connection closed when onError, Message->{}", arg0.getMessage(), arg0);
                    webSocketOnError();
                }

                @Override
                public void onClose(int arg0, String arg1, boolean arg2) {
                    logger.warn("connection closed when onClose. arg0->{}, arg1->{}, arg2->{},webURI->{},gameId:{},roomId:{},tableId:{},userId:{},{}", arg0, arg1, arg2, webSocketURI,getGameId(),getRoomId(),getTableId(),getUserId(),arg2 ? "被动关闭" : "主动关闭");
                    webSocketOnClose("clientCLosed->"+(arg2 ? "被动关闭" : "主动关闭"));

                    //如果是服务器主动断开此连接,并且是Gate服务器，那么主动判断机器人身上是否还有携带的货币，如果有，那么退还到资金池
                    if(arg2 == true && webSocketURI.contains("gate")){
                        //如果机器人的最后一条是代入记录，那么为此机器人做代出操作
                        if(DBUtil.selectLastPoolRecordIsJoin(userId)){
                            logger.warn("机器人被gate服务器断开连接，尝试退还机器人资金到池中。gameId:{},roomId:{},tableId:{},userId:{}", getGameId(),getRoomId(),getTableId(),getUserId());
                            RobotMoneyPoolHolder.robotReturnMoneyToPool(userId, 0);
                        }
                    }
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    try {
                        logger.debug("receive Bytes msg, length->" + bytes.array().length);
                        ReceiveData(bytes);
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            };

            client.connect();
            logger.debug("open success, clientNum->" + clientNum);
            return true;
        } catch (Exception ex) {
            logger.error("ConnectWebSocket ERROR, msg = " + ex.getMessage(), ex);
        }
        return false;
    }

    protected void sendData(int functionId, int gameId, int reqNum, byte[] bodyBytes) {
        sendData(functionId, gameId, reqNum, bodyBytes, 0);
    }

    public void sendData(int functionId, int gameId, int reqNum, byte[] bodyBytes, int gameSvrId) {
        try {
            if (null != client) {
                logger.debug("-----> sendData(), getReadyState = "+client.getReadyState());
                int totalLength = 32 + bodyBytes.length;
                if (client.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
                    ByteBuffer buffer = ByteBuffer.allocate(totalLength);
                    buffer.putInt(ByteBufUtil.swapInt(totalLength));
                    buffer.putInt(ByteBufUtil.swapInt(functionId));
                    buffer.putInt(ByteBufUtil.swapInt(gameId));
                    buffer.putInt(ByteBufUtil.swapInt(gameSvrId));
                    buffer.putInt(ByteBufUtil.swapInt(0));
                    buffer.putInt(ByteBufUtil.swapInt(reqNum));
                    buffer.putInt(ByteBufUtil.swapInt(0));
                    buffer.putInt(ByteBufUtil.swapInt(0));
                    buffer.put(bodyBytes);

                    if (Config.GAME_ENCRYPE_ISOPEN){
                        byte[] keys = Config.GAME_ENCRYPE_KEY.getBytes();
                        for (int i = 0; i < 10; i++) {
                            byte tmp = buffer.get(i);
                            buffer.put(i, (byte) (tmp ^ keys[i]));
                        }
                    }
                    client.send(buffer.array());

                    Object[] arrObj = new Object[2];
                    arrObj[0] = new Date();
                    arrObj[1] = functionId;
                    RobotClientHolder.reqSendTimeMap.put(reqNum+"|"+userId, arrObj);
                }else{
                    logger.error("sendData:::client state is wrong state:"+client.getReadyState()+", functionId->"+functionId+", functionName->"+FunctionIdHolder.GetFunctionName(functionId)+", gameId->"+gameId);
                }
            } else {
                if(functionId != 50050){
                    logger.error("!!!!!!!! client is null, functionId->"+functionId+", functionName->"+FunctionIdHolder.GetFunctionName(functionId)+", gameId->"+gameId);
                }
            }
        } catch (Exception ex) {
            logger.error("sendData ERROR. ", ex);
        }
    }

    /**
     * 解析头并路由数据到逻辑方法
     *
     * @param bytes
     */
    protected void parseHeaderAndRouteBody(ByteBuffer bytes) {
        int length = ByteBufUtil.swapInt(bytes.getInt());
        int functionId = ByteBufUtil.swapInt(bytes.getInt());
        int gameId = ByteBufUtil.swapInt(bytes.getInt());
        int gameSvrId = ByteBufUtil.swapInt(bytes.getInt());
        int isAsync = ByteBufUtil.swapInt(bytes.getInt());
        int reqNum = ByteBufUtil.swapInt(bytes.getInt());
        int field1 = ByteBufUtil.swapInt(bytes.getInt());
        int field2 = ByteBufUtil.swapInt(bytes.getInt());
        int remainingLength = bytes.remaining();

        long timeDifference = getTimeDifference(reqNum);
        if(timeDifference > 1000){
            logger.error("req与Ack执行时间过长，functionId->"+functionId+", functionName->"+ FunctionIdHolder.GetFunctionName(functionId)+", userId->"+userId+", timeDifference->{}"+timeDifference);
        }
        logger.debug("ACK Header, functionId->"+functionId+", functionName->"+ FunctionIdHolder.GetFunctionName(functionId)+", reqNum->" + reqNum +", length->"+ length +", gameId->"+ gameId + ", gameSvrId->"+ gameSvrId
                +", isAsync->"+isAsync+", field1->"+field1+", field2->"+field2+", remainingLength->"+remainingLength+", bytesLength->"+bytes.array().length+", req和ack的时间差->"+timeDifference);
        RobotClientHolder.reqSendTimeMap.remove(reqNum+"|"+userId);

        byte[] ackBodyBytes = new byte[remainingLength];
        bytes.get(ackBodyBytes);

        routeFunctionId(functionId, ackBodyBytes);
    }

    protected void ReceiveData(ByteBuffer bytes) {
//        for (int i = 0; i < bytes.array().length; i++) {
//            if (i % 16 == 0) {
//                System.out.print("\n");
//            }
//            System.out.print(String.format(" %02X", bytes.array()[i]));
//        }

        parseHeaderAndRouteBody(bytes);
    }

    public void webSocketOpen(){

    }
    public void webSocketOnClose(String sourceFrom){
        RobotClientHolder.closeClient(userId, this, "onClose()触发，来源->"+sourceFrom);
        client = null;
    }
    public void webSocketOnError(){

        client = null;
    }
    public String toStringReplaceLine(){
        return System.getProperty("line.separator");
    }
    public void close() {
        if(null != client){
            client.close();
        }
    }

//    public int getRandomNum(int maxNum){
//        Random random = new Random(System.currentTimeMillis()+System.nanoTime());
//        return random.nextInt(maxNum)+1;
//    }

    private long getTimeDifference(int reqNum){
        String key = reqNum+"|"+userId;
        long timeDifference = 0;
        if(RobotClientHolder.reqSendTimeMap.containsKey(key)){
            Date reciveDate = new Date();
            Object[] arrObj = RobotClientHolder.reqSendTimeMap.get(key);
            Date sendDate = (Date)arrObj[0];
            int functionId = (int)arrObj[1];
            timeDifference = reciveDate.getTime() - sendDate.getTime();
        }
        return timeDifference;
    }
}

