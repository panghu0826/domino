package com.jule.domino.game.utils;

import java.util.List;

public class FindNode {
	
	private Byte findCard;
	
	private List<Byte> remain;
	
	//父寻找结点
	private FindNode parent;

	public Byte getFindCard() {
		return findCard;
	}

	public void setFindCard(Byte findCard) {
		this.findCard = findCard;
	}

	public List<Byte> getRemain() {
		return remain;
	}

	public void setRemain(List<Byte> remain) {
		this.remain = remain;
	}

	public FindNode getParent() {
		return parent;
	}

	public void setParent(FindNode parent) {
		this.parent = parent;
	}
}
