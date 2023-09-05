import java.awt.Color;
import java.util.ArrayList;

import ecs100.UI;

public class BlackJack {
	ArrayList<Card> deckList = new ArrayList<>();
	ArrayList<Player> plaList = new ArrayList<>();
	int decks;
	int defBal;//默认余额
	boolean[] button = new boolean[5];//控制按钮的活性
	int PID = 1;//当前玩家编号

	public BlackJack(int d, int p, int b) {
		decks = d;
		defBal = b;
		for (int i = 0; i < p+1; i++) {// dealer也作为player，所以p+1
			plaList.add(new Player("Player "+(i)));
		}
		refreshUI();
		loadCards();
		newRound();
	}

	public void refreshUI() {
		UI.clearGraphics();
		UI.drawImage("img/1350.jpg", 0, 0);
		UI.drawImage("img/dealer.png", 300, 25, 180, 240);
		UI.drawImage("img/button.png", 200, 780);// 图片分辨率128*65
		UI.drawImage("img/button.png", 428, 780);
		UI.drawImage("img/button.png", 656, 780);
		UI.drawImage("img/button.png", 884, 780);
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
		UI.drawImage("img/indicator.png", (PID-1)*300+50, 632, 30, 30);
		displayCard();
		for (int p = 1; p < plaList.size(); p++) {
			UI.setColor(new Color(255, 255, 145));
			UI.drawString(""+plaList.get(p).bestHand(), (p-1)*300+82, 533);
		}
		UI.drawRect(0, 0, 1350, 900);
	}

	/*根据玩家手牌数据，更新桌面上的牌*/
	public void displayCard() {
		for (int p = 1; p < plaList.size(); p++) {
			for (int i = 0; i < plaList.get(p).handList.size(); i++) {
				String fileName = "img/"+plaList.get(p).handList.get(i).cardName;
				UI.drawImage(fileName, 300*(p-1)+90+i*30, 550);
			}
		}
		for (int i = 0; i < plaList.get(0).handList.size(); i++) {
			String fileName = "img/"+plaList.get(0).handList.get(i).cardName;
			if (plaList.get(0).handList.get(i).fold)
				UI.drawImage("img/back.png", 540+i*30, 50);
			else UI.drawImage(fileName, 540+i*30, 50);
		}
	}

	public void activateMouse() {
		UI.setMouseListener(this::checkButton);
	}

	public void checkButton(String action, double x, double y) {
		if (action.equals("clicked")) {
			UI.println(x+" "+y);
			if (x > 200 && x < 328 && y > 780 && y < 845 && button[0]) {// 相应的按钮需要在激活状态才有反应
				stand();
			} else if (x > 428 && x < 556 && y > 780 && y < 845 && button[1]) {
				hit();
//				UI.removeMouseListener();
			} else if (x > 656 && x < 784 && y > 780 && y < 845 && button[2]) {
				UI.println("Double");
			} else if (x > 884 && x < 1012 && y > 780 && y < 845 && button[3]) {
				UI.println("Split");
			} else if (x > 300 && x < 480 && y > 25 && y < 265 && button[4]) {
				UI.print("New Round");
				refreshUI();
				newRound();
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
	}

	public void newRound() {
		for (Player p : plaList) {
			p.resetHand();
		}
		PID = 1;
		refreshUI();
		dealInitialCard();
		if (plaList.get(PID).bjPoints().contains(21)) {
			UI.println("black jack!");
			// black jack之后的动作？？
		} else {
			if (plaList.get(PID).handList.get(0).equals(plaList.get(PID).handList.get(1))) {
				boolean[] butStat = { true, true, true, true, false };
				button = butStat;
			} else {
				boolean[] butStat = { true, true, true, false, false };
				button = butStat;
			}
			activateMouse();// 如何把p传进去？
		}
	}

	public void dealInitialCard() {
		if (deckList.size() > 26) {// 少于26张就重新洗牌
			for (int i = 0; i < 2; i++) {
				for (int p = 0; p < plaList.size(); p++) {// p0代表dealer
					Card nextCard = drawCard();
					if (p == 0 && i == 1)// dealer的第二张牌要盖住
						nextCard.fold = true;
					plaList.get(p).handList.add(nextCard);
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
		plaList.get(PID).handList.add(drawCard());
		refreshUI();// displayCard() replaced
		if (plaList.get(PID).bjPoints().contains(21)) {
			UI.println("black jack!");
			// black jack之后的动作？？
		} else if (plaList.get(PID).bjPoints().get(0) > 21) {
			UI.println("busted");// 输出一些信息提示
			PID++;
			refreshUI();
		} else {
			boolean[] butStat = { true, true, false, false };
			button = butStat;
			activateMouse();
		}
	}

	public Card drawCard() {
		int drawInd = (int)(Math.random()*deckList.size());
		Card nextCard = deckList.get(drawInd);
		deckList.remove(drawInd);
		return nextCard;
	}

	public void stand() {
		if (PID < plaList.size()-1) {
			PID++;
			refreshUI();
		} else {// 全部玩家执行完毕
			plaList.get(0).handList.get(1).fold = false;
			refreshUI();// displayCard() replaced
			ArrayList<Integer> dealHand = plaList.get(0).bjPoints();
			while (dealHand.get(1) < 17 || (dealHand.get(0) < 17 && dealHand.get(1) > 21)) {// 庄家拿牌规则：大数低于17，或者大数高于21但小数低于17（比如14/24）
				plaList.get(0).handList.add(drawCard());// 庄家点数小于17自动抽牌
				UI.sleep(300);
				refreshUI();// displayCard() replaced
				dealHand = plaList.get(0).bjPoints();
			}
			UI.println("dear's hand: ");// 测试用
			plaList.get(0).printHand();// 测试用
			UI.println(plaList.get(0).bestHand());
			for (int p = 1; p < plaList.size(); p++) {
				if (plaList.get(p).bestHand() > plaList.get(0).bestHand()) {
					UI.println("player "+p+" wins");
					UI.setColor(Color.red);
					UI.drawString("Win", (p-1)*300+182, 533);
				}
				if (plaList.get(p).bestHand() < plaList.get(0).bestHand()) {
					UI.setColor(Color.green);
					UI.drawString("Lose", (p-1)*300+182, 533);
					UI.println("player "+p+" lose");
				}
				if (plaList.get(p).bestHand() == plaList.get(0).bestHand()) {
					UI.println("player "+p+" draw");
					UI.setColor(Color.white);
					UI.drawString("Push", (p-1)*300+200, 533);
				}
//				plaList.get(p).printHand();
				calcuChips();
			}
		}
	}

	public void calcuChips() {
		if (defBal > 0) {
			boolean[] butStat = { false, false, false, false, true };
			button = butStat;
			activateMouse();
		} else {
			UI.println("Game Over");
			UI.println(deckList.size());
		}
	}

	public static void main(String[] args) {
		int decks = 4;
		int players = 3;
		int balance = 1000;
		new BlackJack(decks, players, balance);
	}
}
