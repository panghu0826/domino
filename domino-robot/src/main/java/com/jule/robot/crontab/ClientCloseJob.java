package com.jule.robot.crontab;

import com.jule.core.service.CronService;
import com.jule.robot.service.holder.RobotClientHolder;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;


public class ClientCloseJob implements Job {
    private final static Logger logger = LoggerFactory.getLogger(ClientCloseJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        WebSocketClient client = (WebSocketClient) context.getJobDetail().getJobDataMap().get("client");
        Integer num = (Integer) context.getJobDetail().getJobDataMap().get("exec_num");
        int totalLength = (int)context.getJobDetail().getJobDataMap().get("totalLength");
        int functionId =(int)context.getJobDetail().getJobDataMap().get("functionId");
        int gameId= (int)context.getJobDetail().getJobDataMap().get("gameId");
        int reqNum= (int)context.getJobDetail().getJobDataMap().get("reqNum");
        byte[] bodyBytes= (byte[]) context.getJobDetail().getJobDataMap().get("bodyBytes");
        int gameSvrId= (int)context.getJobDetail().getJobDataMap().get("gameSvrId");
        String clientNum= (String)context.getJobDetail().getJobDataMap().get("clientNum");
        if (num == null) {
            context.getJobDetail().getJobDataMap().put("exec_num", 1);
            num = 1;
        } else {
            context.getJobDetail().getJobDataMap().put("exec_num", num + 1);
        }
        logger.info("TEST TEST ");
        if (!client.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {

            logger.debug("can't open, currClientSize->" + RobotClientHolder.getCurrClientCnt() + ", clientNum->" + clientNum);

            if (num > 50) {
                //如果尝试连接5次还没有打开，那么关闭这个连接
                client.close();
                CronService.getInstance().cancel(context.getJobDetail());
                return;
            }
            return;
        }
        logger.info("WebSocket.READYSTATE.OPEN 2:"+client.getReadyState());
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        buffer.putInt(totalLength);
        buffer.putInt(functionId);
        buffer.putInt(gameId);
        buffer.putInt(gameSvrId);
        buffer.putInt(0);
        buffer.putInt(reqNum);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.put(bodyBytes);
        client.send(buffer.array());
        CronService.getInstance().cancel(context.getJobDetail());
    }
}
