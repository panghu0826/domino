package com.jule.domino.game.gameUtil;

import com.google.common.primitives.Ints;
import com.jule.domino.base.model.RoomTableRelationModel;

import java.util.*;

/**
 * 牌局内的发牌操作
 * 描述：获取初始的52张牌数组（无大小王），每发一次牌，便从中减少对应的已发牌型，保持数组中只包含还未发出的牌。
 */
public class DealCardForTable {
	//牌桌ID
	private RoomTableRelationModel roomTableRalation;
	//每局游戏的全局唯一ID
	private String gameOrderId;
	//乱序后的牌局手牌数组
	private int[] arrCards;

	public DealCardForTable(RoomTableRelationModel roomTableRalation, String gameOrderId){
		this.roomTableRalation = roomTableRalation;
		this.gameOrderId = gameOrderId;
		arrCards = WashCard.getAllCard();
	}


	/**
	 * 发牌（如果发到手中的是明牌，那么直接发牌；否则不发牌，等玩家see看牌时再随机发手牌）
	 * @param cardCount 发牌的手牌张数
	 * @param userId 用户ID
	 * @return
	 */
	public int[] hair_card(int cardCount,String userId) {
		//获得不重复的3张手牌
		int[] arrHandCards = getUnRepeatRandom(cardCount, arrCards);

		//删除掉已经发出的牌
		List<Integer> cardsList = new ArrayList(Ints.asList(arrCards));
		for(int i : arrHandCards){
			cardsList.remove(new Integer(i));
		}
		arrCards = cardsList.stream().mapToInt(i->i).toArray();

		return arrHandCards;
	}

	/**
	 * 获得不重复的N个随机手牌
	 * @param n 获取N个随机数
	 * @param arrValue 随机手牌的原始数组
	 * @return
	 */
	private int[] getUnRepeatRandom(int n, int[] arrValue){
		int maxInt = arrValue.length;
		if(n > maxInt){
			n = maxInt;
		}
		if(maxInt <= 0){
			return null;
		}

		Random random = new Random(System.currentTimeMillis());
		Set<Integer> randomSet = new HashSet();
		while(true){
			int randomInt = random.nextInt(maxInt);
			int value = arrValue[randomInt];
			randomSet.add(value);
			if(randomSet.size() >= n){
				break; //当集齐N个随机数时，退出循环
			}
		}
		return randomSet.stream().mapToInt(i->i).toArray();
	}
}
