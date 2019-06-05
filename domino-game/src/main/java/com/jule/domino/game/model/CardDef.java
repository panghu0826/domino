package com.jule.domino.game.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/***
 * 扑克牌定义
 * 
 * @author yang.rao
 *
 */
public class CardDef {

	protected final transient static Logger log = LoggerFactory.getLogger(CardDef.class);

	/************** 扑克牌定义 *************/
	public static final byte fan_A = 0;// ♠A
	public static final byte mei_A = 1;// ♥A
	public static final byte hon_A = 2;// ♣Ahon
	public static final byte hei_A = 3;// ♦A

	public static final byte fan_2 = 4;
	public static final byte mei_2 = 5;
	public static final byte hon_2 = 6;
	public static final byte hei_2 = 7;

	public static final byte fan_3 = 8;
	public static final byte mei_3 = 9;
	public static final byte hon_3 = 10;
	public static final byte hei_3 = 11;

	public static final byte fan_4 = 12;
	public static final byte mei_4 = 13;
	public static final byte hon_4 = 14;
	public static final byte hei_4 = 15;

	public static final byte fan_5 = 16;
	public static final byte mei_5 = 17;
	public static final byte hon_5 = 18;
	public static final byte hei_5 = 19;

	public static final byte fan_6 = 20;
	public static final byte mei_6 = 21;
	public static final byte hon_6 = 22;
	public static final byte hei_6 = 23;

	public static final byte fan_7 = 24;
	public static final byte mei_7 = 25;
	public static final byte hon_7 = 26;
	public static final byte hei_7 = 27;

	public static final byte fan_8 = 28;
	public static final byte mei_8 = 29;
	public static final byte hon_8 = 30;
	public static final byte hei_8 = 31;

	public static final byte fan_9 = 32;
	public static final byte mei_9 = 33;
	public static final byte hon_9 = 34;
	public static final byte hei_9 = 35;

	public static final byte fan_10 = 36;
	public static final byte mei_10 = 37;
	public static final byte hon_10 = 38;
	public static final byte hei_10 = 39;

	public static final byte fan_J = 40;
	public static final byte mei_J = 41;
	public static final byte hon_J = 42;
	public static final byte hei_J = 43;

	public static final byte fan_Q = 44;
	public static final byte mei_Q = 45;
	public static final byte hon_Q = 46;
	public static final byte hei_Q = 47;

	public static final byte fan_K = 48;
	public static final byte mei_K = 49;
	public static final byte hon_K = 50;
	public static final byte hei_K = 51;

	public static final byte xiao_wang = 52;
	public static final byte da_wang = 53;
	/***
	 * 获取牌面值
	 *
	 * @return
	 */
	public static int getCardValue(byte card) {
		return (card / 4) + 1;
	}

	/***
	 * 获取牌面花色
	 *
	 * @param card
	 * @return
	 */
	public static int getCardColor(byte card) {
		return (card % 4) + 1;
	}

//	/***
//	 * 获取牌面花色
//	 *
//	 * @param card
//	 * @return
//	 */
//	public static int getCardColor(byte card) {
//		int value = card % 4;
//		int color = -1;
//		switch (value) {
//		case 0:
//			color = 4;// 黑桃为4
//			break;
//		case 1:
//			color = 3;// 红桃
//			break;
//		case 2:
//			color = 2;// 梅花
//			break;
//		case 3:
//			color = 1;// 方块
//			break;
//		default:
//			break;
//		}
//		return color;
//	}

	public static int compare(byte card1, byte card2) {
		if (card1 == card2) {
			return 0;
		}
		int bankcardValue = CardDef.getCardValue(card1);
		int nomalcardValue = CardDef.getCardValue(card2);
		if (bankcardValue > nomalcardValue) {
			return 1;
		} else if (bankcardValue < nomalcardValue) {
			return -1;
		} else {
			if (CardDef.getCardColor(card1) > CardDef.getCardColor(card2)) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	/***
	 * 获取牌面分值(比如，J Q K的牌面分值，为10)
	 *
	 * @param card
	 * @return
	 */
	public static int getCardScoreValue(byte card) {
		int value = getCardValue(card);
		return value >= 10 ? 10 : value;
	}

	private static List<Byte> cache = new ArrayList<>();

	public static void setCache(String cards) {
		if (cards == null || cards.equals("")) {
			cache = null;
			return;
		}
		List<Byte> list = new ArrayList<>();
		String[] src = cards.split("_");
		try {
			for (String s : src) {
				list.add(Byte.parseByte(s));
			}
		} catch (Exception e) {
			log.error("配牌发生错误:" + cards);
			e.printStackTrace();
		}
		if (list.size() > 0) {
			cache = list;
		} else {
			cache = null;
		}
	}

	/***
	 * 获取一副洗过的扑克牌
	 *
	 * @return
	 */
	public static List<Byte> getShuffleCards() {
		if (cache == null || cache.size() == 0) {
			List<Byte> cards = new ArrayList<>(52);
			for (byte i = 0; i < 52; i++) {
				cards.add(i);
			}
			Collections.shuffle(cards);
			return cards;
		} else {
			log.info("采用配牌实例化玩家的牌！");
			if (cache.size() == 52) {
				return new ArrayList<>(cache);
			} else {
				List<Byte> cards = new ArrayList<>(52);
				for (int i = 0; i < cache.size(); i++) {
					cards.add(cache.get(i));
				}
				for (int i = 0; i < 52; i++) {
					if (!cards.contains((byte) i)) {
						cards.add((byte) i);
					}
				}
				return cards;
			}
		}
	}
}
