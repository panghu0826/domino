package com.jule.domino.game.api.impl;

import com.jule.core.utils.MD5Util;
import com.jule.domino.game.api.BaseServlet;
import com.jule.domino.game.model.PlayerInfo;
import com.jule.domino.game.play.AbstractTable;
import com.jule.domino.game.service.UserTableService;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;


/**
 * 服务器启停
 */
@Path(value="/api/game")
public class GameServlet extends BaseServlet {

    private static final String public_key = "CE4239248A82C5C88FA7AB7B7841F274";

    private static final String err_str = "0";

    /**
     * 更新牌局中玩家的筹码信息
     * @param userId
     * @param money
     * @return
     */
    @POST
    @Path("/updateChips")
    @Produces({MediaType.APPLICATION_JSON})
    public String updateChips(
            @FormParam("userId") String userId,
            @FormParam("money") int money,
            @FormParam("sign") String sign){
        logger.info(MessageFormat.format("更新玩家筹码,user={0},money={1},sign={2}",userId,money,sign));
        String md5Str = MD5Util.encodeByMD5(userId+money+public_key);
        if (!md5Str.equals(sign)){
            logger.error(MessageFormat.format("支付更新异常、非法操作md5str={0},sign={1}",md5Str,sign));
            return err_str;
        }

        if (StringUtils.isEmpty(userId) || money <= 0){
            return err_str;
        }

        AbstractTable table = UserTableService.getInstance().getTableByUserId(userId);
        if (table == null){
            logger.info("玩家不在游戏中...");
            return err_str;
        }

        PlayerInfo player = table.getPlayer(userId);
        if (player != null){
            logger.info("更新玩家筹码成功");
            player.setPlayScoreStore(player.getPlayScoreStore() + money);
            table.getAllPlayers().put(userId,player);
            return String.valueOf(player.getPlayScoreStore());
        }
        return err_str;
    }

}
