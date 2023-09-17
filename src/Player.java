import java.awt.Color;
import java.util.ArrayList;

import ecs100.UI;

public class Player {
	String name;
	int balance;
	private int seat;
	private int x;
	private int y;
	int bet;
	String status;
	ArrayList<Card> handList;
	boolean split;

	public Player(int s) {
		this.seat = s;
		x = 300*(s-1)+90;
		y = 600;
		bet = 100;
		status = "";
		handList = new ArrayList<>();
	}

	public Player(int s, int b) {
		seat = s;
		x = 300*(s-1)+90;
		y = 300;
		bet = b;
		status = "";
		handList = new ArrayList<>();
		split = true;
	}

	public void addCard(Card c) {
		handList.add(c);
		int hand = bestHand();
		int lower = bjPoints().get(0);
		int higher = bjPoints().get(1);
		if (hand == 22)
			status = "Black Jack";
		else if (hand == 0)
			status = "Busted";
		else if (lower != higher && higher < 21)
			status = lower+"/"+higher;
		else status = ""+hand;
	}

	public int bestHand() {
		ArrayList<Integer> hands = bjPoints();
		if (hands.contains(21) && handList.size() == 2)
			return 22;
		if (hands.get(1) <= 21)
			return hands.get(1);
		else if (hands.get(0) <= 21)
			return hands.get(0);
		else return 0;// 0代表爆了
	}

	public ArrayList<Integer> bjPoints() {
		ArrayList<Integer> scoreList = new ArrayList<>();
		int score = 0;
		boolean containA = false;
		for (Card c : handList) {
			if (c.rank == 1) {
				containA = true;
				score += c.rank;
			} else if (c.rank > 10)
				score += 10;
			else score += c.rank;
		}
		scoreList.add(score);
		if (containA)
			scoreList.add(score+10);// 保证小的在前，大的在后，或者前后一样
		else scoreList.add(score);
		return scoreList;
	}

	public boolean checkPair() {
		if (handList.size() > 2)
			return false;
		else if ((handList.get(0).rank == handList.get(1).rank)
				|| (handList.get(0).rank >= 10 && handList.get(1).rank >= 10))
			return true;
		else return false;
	}

	public void displayCard() {
		UI.setColor(new Color(255, 255, 145));
		for (int i = 0; i < handList.size(); i++) {
			String cardFile = "img/"+handList.get(i).cardName();
			UI.drawImage(cardFile, x+i*30, y);
			UI.drawString(status, x-8, y-18);// 手牌状态
		}
		UI.drawImage("img/chip.png", x-8, 510);// 分辨率40*40
		if (split == false)
			UI.drawString(""+bet, x+60, 538);
		else UI.drawString("+"+bet, x+120, 538);
	}

	public void drawIndicator() {
		UI.drawImage("img/indicator.png", x-40, y+82, 30, 30);
	}

	public void resetHand() {
		handList = new ArrayList<>();
	}

	public int result(int dealerHand) {
		int income = 0;
		if (bestHand() > dealerHand) {
//			UI.println("player "+seat+" wins");
			UI.setColor(Color.red);
			UI.drawString("Win", x+110, y-17);
			if (status.equals("Black Jack"))
				income += bet*1.5;// BJ1.5倍赔率
			else income += bet;
		}
		if (bestHand() < dealerHand || bestHand() == 0) {// 比庄家小，或者爆掉。跟庄家一起爆掉算玩家输。
			UI.setColor(Color.green);
			UI.drawString("Lose", x+110, y-17);
//			UI.println("player "+p+" lose");
			income -= bet;
		} else if (bestHand() == dealerHand) {// 有问题
//			UI.println("player "+p+" push");
			UI.setColor(Color.white);
			UI.drawString("Push", x+110, y-17);
			// income = plaList.get(p).bet;
		}
		return income;
	}

	public Player splitHand(Card ca, Card cb) {
		Player splitPlayer = new Player(seat, bet);
		splitPlayer.addCard(handList.get(1));
		splitPlayer.addCard(cb);
		handList.remove(1);
		addCard(ca);
		return splitPlayer;
	}

	public void printHand() {
		for (Card c : handList) {
			System.out.print(c.rank+"  ");
		}
		System.out.println("bestHand: "+bestHand());
	}
}
