package com.jule.domino.log.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志系统的日志原因定义
 *
 *
 */
public interface LogReasons {

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.TYPE })
	public @interface ReasonDesc {
		/**
		 * 原因的文字描述
		 *
		 * @return
		 */
		String value();
	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.TYPE })
	public @interface LogDesc {
		/**
		 * 日志描述
		 *
		 * @return
		 */
		String desc();
	}

	/**
	 * LogReason的通用接口
	 */
	public static interface ILogReason {
		/**
		 * 取得原因的序号
		 *
		 * @return
		 */
		public int getReason();

		/**
		 * 获取原因的文本
		 *
		 * @return
		 */
		public String getReasonText();
	}

	/**
	 * 经验的原因接口
	 *
	 * @param <E>
	 *            枚举类型
	 */
	public static interface IItemLogReason<E extends Enum<E>> extends
			ILogReason {
		public E getReasonEnum();
	}

	/**
	 * 通用日志
	 */
	public enum CommonLogReason implements ILogReason {
		@ReasonDesc("用户登录")
		LOGIN(1, "用户登录"),
		@ReasonDesc("用户登出")
		LOGOUT(2, "用户登出"),
		@ReasonDesc("支付下单")
		PAY_ORDERED(3, "支付下单"),
		@ReasonDesc("支付完成")
		PAY_COMPLETED(4, "支付完成"),
		@ReasonDesc("创建角色")
		CREATE_ROLE(5, "创建角色"),
		@ReasonDesc("游戏开局")
		GAME_PLAY(6, "游戏开局"),
		@ReasonDesc("游戏结算")
		GAME_SETTLE(7, "游戏结算"),
		@ReasonDesc("下注")
		GAME_BET(8, "下注"),
		@ReasonDesc("送礼")
		GAME_GIFT(9, "送礼"),
		@ReasonDesc("荷官")
		GAME_DEALER(10, "荷官"),
		@ReasonDesc("签到")
		GAME_SIGN(11, "签到"),
		@ReasonDesc("买入")
		GAME_BUY_IN(12, "买入"),
		@ReasonDesc("side_show")
		GAME_SIDE_SHOW(13, "side_show"),
		@ReasonDesc("发牌")
		GAME_GIVECARD(14, "发牌"),
		@ReasonDesc("比牌")
		GAME_SHOW(15, "比牌"),
		@ReasonDesc("换牌")
		GAME_CHANGECARD(16, "换牌"),
		@ReasonDesc("打赏")
		GAME_DEALER_REWARDS(17, "打赏"),
		@ReasonDesc("线下恢复")
		OFF_LINE_RECOVER(18, "线下恢复"),
		@ReasonDesc("user表更新")
		USER_UPDATE(19, "user表更新"),
		@ReasonDesc("后台添加")
		GM(20, "后台添加"),
		@ReasonDesc("入桌")
		JOIN_TABLE(21, "入桌"),
		@ReasonDesc("离桌")
		LEAVE_LEAVE(22, "离桌"),
		@ReasonDesc("换头像")
		CHANGE_ICO(23, "换头像"),
		@ReasonDesc("断线重连")
		RECONNECT(24, "断线重连"),
		@ReasonDesc("任务奖励")
		TASK_AWARD(25, "任务奖励"),
		@ReasonDesc("在线人数")
		ONLINENUMBER(26, "在线人数"),
		@ReasonDesc("邮件")
		MAIL_REWARD(27, "邮件领取"),
		@ReasonDesc("广告奖励")
		AD_GIVE(28, "广告奖励"),
		@ReasonDesc("后台扣除")
		GM_MIUNES(29, "后台扣除"),
		@ReasonDesc("系统扣除")
		SYSTEM_MIUNES(30, "系统扣除"),
		;

		/** 原因序号 */
		public final int reason;
		/** 原因文本 */
		public final String reasonText;

		private CommonLogReason(int reason, String reasonText) {
			this.reason = reason;
			this.reasonText = reasonText;
		}
		@Override
		public int getReason() {
			return reason;
		}
		@Override
		public String getReasonText() {
			return reasonText;
		}
	}

	/**
	 * 物品变更原因
	 *
	 */
	@LogDesc(desc = "物品日志")
	public enum ItemLogReason implements ILogReason {
		/** 装备强化 */
		@ReasonDesc("物品获得")
		ITEM_GEN(1, "物品获得"),
		@ReasonDesc("物品获得")
		ITEM_COST(2, "物品获得"),
		;

		/** 原因序号 */
		public final int reason;
		/** 原因文本 */
		public final String reasonText;

		private ItemLogReason(int reason, String reasonText) {
			this.reason = reason;
			this.reasonText = reasonText;
		}

		@Override
		public int getReason() {
			return reason;
		}

		@Override
		public String getReasonText() {
			return reasonText;
		}
	}



}
