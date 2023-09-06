import java.awt.Color;
import java.util.ArrayList;

import ecs100.UI;

public class BlackJack {
	ArrayList<Card> deckList = new ArrayList<>();
	ArrayList<Player> plaList = new ArrayList<>();
	int decks;
	double defBal;// 默认余额
	boolean[] button = new boolean[5];// 控制按钮的活性
	int PID;// 当前玩家编号

	public BlackJack(int d, int p, int b) {
		decks = d;
		defBal = b-100*p;
		for (int i = 0; i < p+1; i++) {// dealer也作为player，所以p+1
			plaList.add(new Player("Player "+(i)));
		}
		refreshUI();
		loadCards();
		activateMouse();
	}

	public void refreshUI() {
		UI.drawImage("img/1350.jpg", 0, 0);
		UI.drawImage("img/dealer.png", 300, 25, 180, 240);
		UI.drawImage("img/button.png", 200, 780);// 图片分辨率128*65
		UI.drawImage("img/button.png", 428, 780);
		if (button[2])
			UI.drawImage("img/button.png", 656, 780);
		else UI.drawImage("img/button_disabled.png", 656, 780);
		UI.drawImage("img/button.png", 656, 780);
		if (button[3])
			UI.drawImage("img/button.png", 884, 780);
		else UI.drawImage("img/button_disabled.png", 884, 780);
		UI.setFontSize(24);
		UI.setColor(new Color(23, 63, 63));
		UI.drawString("Stand", 230, 820);
		UI.drawString("Hit", 478, 820);
		UI.drawString("Double", 681, 820);
		UI.drawString("Split", 924, 820);
		UI.setColor(new Color(158, 78, 53));
		UI.drawRect(90, 550, 134, 196);
		UI.drawRect(390, 550, 134, 196);
		UI.drawRect(690, 550, 134, 196);
		UI.drawRect(990, 550, 134, 196);
		displayCard();
		UI.setColor(new Color(255, 255, 145));
		UI.drawString("Click Me", 370, 300);
		UI.drawString("$: "+defBal, 1000, 50);
		UI.drawImage("img/indicator.png", (PID-1)*300+50, 632, 30, 30);
	}

	/*根据玩家手牌数据，更新桌面上的牌*/
	public void displayCard() {
		UI.setColor(new Color(255, 255, 145));
		for (int p = 1; p < plaList.size(); p++) {// 玩家牌的位置
			for (int i = 0; i < plaList.get(p).handList.size(); i++) {
				String fileName = "img/"+plaList.get(p).handList.get(i).cardName;
				UI.drawImage(fileName, 300*(p-1)+90+i*30, 550);
			}
			UI.drawImage("img/chip.png", (p-1)*300+82, 460);// 分辨率40*40
			UI.drawString(""+plaList.get(p).bet, (p-1)*300+162, 488);
			UI.drawString(plaList.get(p).status, (p-1)*300+82, 533);// 手牌状态
		}
		for (int i = 0; i < plaList.get(0).handList.size(); i++) {
			String fileName = "img/"+plaList.get(0).handList.get(i).cardName;
			if (plaList.get(0).handList.get(i).fold)
				UI.drawImage("img/back.png", 540+i*30, 50);// 庄家牌的位置
			else UI.drawImage(fileName, 540+i*30, 50);
		}
	}

	public void activateMouse() {
		UI.setMouseListener(this::checkButton);
	}

	public void checkButton(String action, double x, double y) {
		if (action.equals("clicked")) {
//			UI.println(x+" "+y);
			if (x > 200 && x < 328 && y > 780 && y < 845 && button[0]) {// 相应的按钮需要在激活状态才有反应
				stand();
			} else if (x > 428 && x < 556 && y > 780 && y < 845 && button[1]) {
				hit();
			} else if (x > 656 && x < 784 && y > 780 && y < 845 && button[2]) {
				doubleBet();
//				UI.println("Double");
			} else if (x > 884 && x < 1012 && y > 780 && y < 845 && button[3]) {
				UI.println("Split");
			} else if (x > 300 && x < 480 && y > 25 && y < 265 && button[4]) {
//				UI.println("New Round");
				refreshUI();
				newRound();
			} else if (x > 80 && x < 1033 && y > 460 && y < 500 && button[4]) {
				UI.println("chip");
				placeBet((int)x, (int)y);
			}
		}
	}

	public void loadCards() {
		for (int i = 0; i < decks; i++) {
			for (int s = 1; s <= 4; s++) {
				for (int r = 1; r <= 13; r++) {
					Suit suit = null;
					switch (s) {
					case 1 -> { suit = Suit.Heart; break; }
					case 2 -> { suit = Suit.Spade; break; }
					case 3 -> { suit = Suit.Diamond; break; }
					case 4 -> { suit = Suit.Tree; break; }
					}
					Card c = new Card(suit, r, i);
					deckList.add(c);
				}
			}
		}
		button[4] = true;
	}

	public void newRound() {
		for (Player p : plaList) {
			p.resetHand();
		}
//		defBal = defBal-p.bet;
//		defBal -= 400;// 把deal减下去的加回来
		refreshUI();
		activateMouse();
		dealInitialCard();
		PID = 1;
		while (plaList.get(PID).status.equals("Black Jack")) {
			if (PID < plaList.size()-1) {
				PID++;
				refreshUI();
			}
		}
		// black jack之后的动作？？
		if (plaList.get(PID).handList.get(0).rank == plaList.get(PID).handList.get(1).rank) {
			boolean[] butStat = { true, true, true, true, false };
			button = butStat;
		} else {
			boolean[] butStat = { true, true, true, false, false };
			button = butStat;
		}
		refreshUI();
		activateMouse();// 如何把p传进去？
	}

	public void placeBet(int x, int y) {
		for (int p = 1; p < plaList.size(); p++) {
			if (x > (p-1)*300+82 && x < (p-1)*300+122 && y > 460 && y < 500) {
				plaList.get(p).bet += 100;
			}
		}
		refreshUI();
//		boolean[] butStat = { false, false, false, false, false };
//		button = butStat;
//		activateMouse();
	}

	public void dealInitialCard() {
		if (deckList.size() > 26) {// 少于26张就重新洗牌
			for (int i = 0; i < 2; i++) {
				for (int p = 0; p < plaList.size(); p++) {// p0代表dealer
					Card nextCard = drawCard();
					if (p == 0 && i == 1)// dealer的第二张牌要盖住
						nextCard.fold = true;
					plaList.get(p).addCard(nextCard);
					refreshUI();// displayCard() replaced
					UI.sleep(300);
				}
			}
		} else {
			UI.println("Cards reloading");
			deckList = new ArrayList<Card>();
			loadCards();
			dealInitialCard();
		}
	}

	public void hit() {
		plaList.get(PID).addCard(drawCard());
		refreshUI();// displayCard() replaced
		String status = plaList.get(PID).status;
		if (status.equals("Black Jack") || status.equals("Busted") || plaList.get(PID).bestHand() == 21) {
			stand();
		} else {
			boolean[] butStat = { true, true, false, false, false };
			button = butStat;
			activateMouse();
		}
	}

	public void doubleBet() {// 要考虑余额不足
		plaList.get(PID).bet *= 2;
		plaList.get(PID).addCard(drawCard());
		refreshUI();// displayCard() replaced
		stand();
	}

	public Card drawCard() {
		int drawInd = (int)(Math.random()*deckList.size());
		Card nextCard = deckList.get(drawInd);
		deckList.remove(drawInd);
		return nextCard;
	}

	public void stand() {
		button[2] = true;
		if (PID < plaList.size()-1) {
			PID++;
			if (plaList.get(PID).status.equals("Black Jack"))
				stand();
			refreshUI();
		} else {// 全部玩家执行完毕
			dealerAction();
		}
	}

	public void dealerAction() {
		plaList.get(0).handList.get(1).fold = false;
		refreshUI();// displayCard() replaced
		UI.sleep(300);
		ArrayList<Integer> dealHand = plaList.get(0).bjPoints();
		while (dealHand.get(1) < 17 || (dealHand.get(0) < 17 && dealHand.get(1) > 21)) {// 庄家拿牌规则：大数低于17，或者大数高于21但小数低于17（比如14/24）
			plaList.get(0).addCard(drawCard());// 庄家点数小于17自动抽牌
//			UI.sleep(300);
			refreshUI();// displayCard() replaced
			dealHand = plaList.get(0).bjPoints();
		}
		UI.println("dear's hand: ");// 测试用
		plaList.get(0).printHand();// 测试用
		UI.println(plaList.get(0).bestHand());
		double income = 0;
		for (int p = 1; p < plaList.size(); p++) {// 判定输赢
			if (plaList.get(p).bestHand() > plaList.get(0).bestHand()) {
				UI.println("player "+p+" wins");
				UI.setColor(Color.red);
				UI.drawString("Win", (p-1)*300+210, 533);
				if (plaList.get(p).status.equals("Black Jack"))
					income += plaList.get(p).bet*1.5;// BJ1.5倍赔率
				else income += plaList.get(p).bet;
			}
			if (plaList.get(p).bestHand() < plaList.get(0).bestHand() || plaList.get(p).bestHand() == 0) {// 比庄家小，或者爆掉。跟庄家一起爆掉算玩家输。
				UI.setColor(Color.green);
				UI.drawString("Lose", (p-1)*300+210, 533);
				UI.println("player "+p+" lose");
				income -= plaList.get(p).bet;
			} else if (plaList.get(p).bestHand() == plaList.get(0).bestHand()) {// 有问题
				UI.println("player "+p+" push");
				UI.setColor(Color.white);
				UI.drawString("Push", (p-1)*300+210, 533);
//				income = plaList.get(p).bet;
			}
		}
		defBal += income;
		if (income> 0) {
			UI.setColor(Color.red);
			UI.drawString("+"+(income), 1027, 80);
		} else if (income< 0) {
			UI.setColor(Color.green);
			UI.drawString(""+(income), 1027, 80);
		}
		tryAgain();
	}

	public void tryAgain() {// reset user bet
		if (defBal > 0) {
			boolean[] butStat = { false, false, false, false, true };
			button = butStat;
			for (Player p : plaList) {
				p.bet = 100;
			}
			UI.setColor(new Color(255, 255, 145));
			UI.drawString("Click Me", 370, 300);
			UI.drawString(plaList.get(0).status, 600, 280);// plaList.get(0).status
			activateMouse();
		} else {
			UI.println("Game Over");
			UI.println(deckList.size());
		}
	}

	public static void main(String[] args) {
		int decks = 4;
		int players = 4;
		int balance = 5000;
		new BlackJack(decks, players, balance);
	}
}
