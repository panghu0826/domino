package com.jule.domino.game.gameUtil;

import com.google.common.primitives.Ints;
import lombok.Setter;

import java.util.*;

/**
 * 
 * 洗牌
 *
 */
public class WashCard {


	private static int[] cards = {
			1 , 2 , 3 , 4 , 5 , 6 , 7 ,//♠
			8 , 9 , 10, 11, 12, 13, 14,//♥
			15, 16, 17, 18, 19, 20, 21,//♣
			22, 23, 24, 25, 26, 27, 28 //♦
	};

	private static int[] nnCards = {
			5 , 6 , 7 ,//♠
			12, 13, 14,//♥
			19, 20, 21,//♣
			26, 27, 28 //♦
	};

	/**
	 * 乱序手牌队列
	 */
	public static List<Integer> shuffle(int[] arrParam) {
		//复制一个新的数组，避免影响到原有原始数组的顺序
		int[] tmpArr = Arrays.copyOf(arrParam, arrParam.length);
		List<Integer> cardsList = Ints.asList(tmpArr);
		//乱序队列内容
		Collections.shuffle(cardsList);
		return cardsList;
	}

	/**
	 * 获得一整副手牌
	 *
	 * @return
	 */
	public static int[] getAllCard(int playType) {
		int[] allCards = null;
		if(playType == 1){
			allCards = cards;
		}
		List<Integer> cardsList = shuffle(allCards);
		return cardsList.stream().mapToInt(i->i).toArray();
	}
}
