import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import ecs100.UI;

public class BlackJack_Main {
	ArrayList<Card> deckList = new ArrayList<>();
	ArrayList<Player> plaList = new ArrayList<>();
	int decks = 6;
	int players;
	int round;
	int balance;
	int hiRecord;
	boolean[] button = new boolean[5];// 控制按钮的活性
	int PID;// 当前玩家编号

	public BlackJack_Main() {
		loadProfile();
		initialize();
	}

	public BlackJack_Main(int p, int b) {
		players = p;
		balance = b-100*p;
		initialize();
	}

	public void initialize() {
		for (int i = 0; i < players+1; i++) {// dealer也作为player，所以p+1
			plaList.add(new Player(i));
		}
		refreshUI();
		loadCards();
		activateMouse();
	}

	public void loadProfile() {
		File save = new File("data/save-black_jack.txt");
		if (save.exists()) {
			try {
				Scanner sc = new Scanner(new File("data/save-black_jack.txt"));
				sc.next();
				players = sc.nextInt();
				sc.next();
				balance = sc.nextInt();
				sc.next();
				round = sc.nextInt();
				sc.next();
				hiRecord = sc.nextInt();
				sc.close();
			} catch (IOException e) {
				UI.println("Error: "+e);
			}
		} else {
			players = 4;
			balance = 4600;
		}
	}

	public void refreshUI() {
		UI.drawImage("img/900.jpg", 0, 0);
		UI.drawImage("img/casino.png", 320, 45, 200, 200);
		refreshButton();
		UI.setFontSize(24);
		UI.setColor(new Color(23, 63, 63));
		UI.drawString("Stand", 230, 855);
		UI.drawString("Hit", 478, 855);
		UI.drawString("Double", 681, 855);
		UI.drawString("Split", 924, 855);
		UI.setColor(new Color(158, 78, 53));
		UI.drawRect(540, 50, 134, 196);
		UI.drawRect(90, 600, 134, 196);
		UI.drawRect(390, 600, 134, 196);
		UI.drawRect(690, 600, 134, 196);
		UI.drawRect(990, 600, 134, 196);
		displayCard();
		UI.setColor(new Color(255, 255, 145));
		UI.drawString("Deal", 395, 245);
		UI.drawString("Record: "+hiRecord, 850, 50);
		UI.drawString("$: "+balance, 1050, 50);
		UI.drawString("Round: "+round, 1200, 50);
		UI.drawString(""+deckList.size(), 100, 50);
		plaList.get(PID).drawIndicator();
//		UI.drawImage("img/indicator.png", (PID-1)*300+50, 632, 30, 30);
	}

	public void refreshButton() {
		if (button[0])
			UI.drawImage("img/button.png", 200, 815);// 图片分辨率128*65
		else UI.drawImage("img/button_disabled.png", 200, 815);// 图片分辨率128*65
		if (button[1])
			UI.drawImage("img/button.png", 428, 815);
		else UI.drawImage("img/button_disabled.png", 428, 815);
		if (button[2])
			UI.drawImage("img/button.png", 656, 815);
		else UI.drawImage("img/button_disabled.png", 656, 815);
		if (button[3])
			UI.drawImage("img/button.png", 884, 815);
		else UI.drawImage("img/button_disabled.png", 884, 815);
	}

	/*根据玩家手牌数据，更新桌面上的牌*/
	public void displayCard() {
		for (int p = 1; p < plaList.size(); p++) {// 玩家牌的位置
			plaList.get(p).displayCard();
		}
		for (int i = 0; i < plaList.get(0).handList.size(); i++) {
			String cardFile = "img/"+plaList.get(0).handList.get(i).cardName();
			if (plaList.get(0).handList.get(i).fold)
				UI.drawImage("img/back.png", 540+i*30, 50);// 庄家牌的位置
			else UI.drawImage(cardFile, 540+i*30, 50);
		}
	}

	public void activateMouse() {
		UI.setMouseListener(this::checkButton);
	}

	public void checkButton(String action, double x, double y) {
		if (action.equals("clicked")) {
//			UI.println(x+" "+y);
			if (x > 200 && x < 328 && y > 815 && y < 880 && button[0]) {// 相应的按钮需要在激活状态才有反应
				stand();
			} else if (x > 428 && x < 556 && y > 815 && y < 880 && button[1]) {
				hit();
			} else if (x > 656 && x < 784 && y > 815 && y < 880 && button[2]) {
				doubleBet();
			} else if (x > 884 && x < 1012 && y > 815 && y < 880 && button[3]) {
				split();
			} else if (x > 320 && x < 520 && y > 45 && y < 245 && button[4]) {
				newRound();
			} else if (x > 80 && x < 1033 && y > 510 && y < 550 && button[4]) {
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

	public void placeBet(int x, int y) {
		for (int p = 1; p < plaList.size(); p++) {
			if (x > (p-1)*300+82 && x < (p-1)*300+122 && y > 510 && y < 550 && plaList.get(p).bet < 500) {
				plaList.get(p).bet += 100;
				balance -= 100;
			}
		}
		refreshUI();
	}

	public void newRound() {
		round++;
		for (int i = 0; i < plaList.size(); i++) {
			plaList.get(i).resetHand();// 手牌清空
			if (plaList.get(i).split) {
				balance += plaList.get(i).bet;
				plaList.remove(i);// 把原来分裂出来的player移除掉
				i--;
			}
		}
		if (balance > hiRecord)
			hiRecord = balance;
		refreshUI();
		activateMouse();
		dealInitialCard();
		PID = 1;
		while (plaList.get(PID).status.equals("Black Jack") && PID < plaList.size()-1) {
			PID++;
			refreshUI();
		}
		if (PID == plaList.size()-1)
			dealerAction();
		// black jack之后的动作？？
		else {
			if (plaList.get(PID).checkPair()) {
				boolean[] butStat = { true, true, true, true, false };
				button = butStat;
			} else {
				boolean[] butStat = { true, true, true, false, false };
				button = butStat;
			}
			refreshUI();
			activateMouse();// 如何把p传进去？
		}
	}

	public void dealInitialCard() {
		if (deckList.size() < 52) {// 少于52张就重新洗牌
			UI.println("Cards reloading");
			deckList = new ArrayList<Card>();
			loadCards();
//			dealInitialCard();
//			return;
		}
		for (int i = 0; i < 2; i++) {
			for (int p = 0; p < plaList.size(); p++) {// p0代表dealer
				Card nextCard = drawCard();
				if (p == 0 && i == 1)// dealer的第二张牌要盖住
					nextCard.fold = true;
//					if (p == 1 && i == 0)// 测试用，手动输入所需要的牌
//					nextCard.rank = 10;
//					if (p == 1 && i == 1)
//					nextCard.rank = 11;
				plaList.get(p).addCard(nextCard);
				refreshUI();
				UI.sleep(300);
			}
		}
	}

	public void hit() {
		plaList.get(PID).addCard(drawCard());
		refreshUI();
		String status = plaList.get(PID).status;
		if (status.equals("Busted") || plaList.get(PID).bestHand() == 21) {
			stand();
		} else {
			boolean[] butStat = { true, true, false, false, false };
			button = butStat;
			refreshUI();
			activateMouse();
		}
	}

	public void stand() {
		if (PID >= plaList.size()-1) {
			dealerAction();
			return;
		}
		PID++;
		// refreshUI();
		if (plaList.get(PID).status.equals("Black Jack")) {
			stand();
			return;
		}
		if (plaList.get(PID).checkPair() && plaList.get(PID).split == false)// 分裂出来的牌不能继续分裂
			button[3] = true;
		else button[3] = false;
		button[2] = true;
		refreshUI();
		activateMouse();
	}

	public void doubleBet() {// 要考虑余额不足
		balance -= plaList.get(PID).bet;
		plaList.get(PID).bet *= 2;
		plaList.get(PID).addCard(drawCard());
		refreshUI();
		stand();
	}

	public void split() {
		balance -= plaList.get(PID).bet;
		Card ca = drawCard();
		Card cb = drawCard();
		Player newPlayer = plaList.get(PID).splitHand(ca, cb);
		plaList.add(PID+1, newPlayer);// 把分裂出来的player插入到原player后面
		button[3] = false;
//		button[2] = true;
		// refreshUI();
		if (plaList.get(PID).status.equals("Black Jack"))
			PID++;
		refreshUI();
		activateMouse();
	}

	public void dealerAction() {
		plaList.get(0).handList.get(1).fold = false;
		boolean[] butStat = { false, false, false, false, false };
		button = butStat;
		refreshUI();
//		UI.sleep(300);
		ArrayList<Integer> dealHand = plaList.get(0).bjPoints();
		while (dealHand.get(1) < 17 || (dealHand.get(0) < 17 && dealHand.get(1) > 21)) {// 庄家拿牌规则：大数低于17，或者大数高于21但小数低于17（比如14/24）
			UI.drawString(plaList.get(0).status, 600, 280);
			plaList.get(0).addCard(drawCard());// 庄家点数小于17自动抽牌
			UI.sleep(600);
			refreshUI();
			dealHand = plaList.get(0).bjPoints();
		}
		plaList.get(0).printHand();// 测试用
		int income = 0;
		for (int p = 1; p < plaList.size(); p++) {// 判定输赢
			income += plaList.get(p).result(plaList.get(0).bestHand());// 把dealer的牌传进去，计算出每个玩家的输赢情况
		}
		balance += income;

		if (income > 0) {
//			refreshUI();
			UI.setColor(Color.red);
			UI.drawString("+"+(income), 1077, 80);
		} else if (income < 0) {
//			refreshUI();
			UI.setColor(Color.green);
			UI.drawString(""+(income), 1077, 80);
		}
		tryAgain();
	}

	public void tryAgain() {// reset user bet
		try {
			PrintStream out = new PrintStream(new File("data/save-black_jack.txt"));
			out.println("players: "+players);
			out.println("balance: "+balance);
			out.println("round:  "+round);
			out.println("record: "+hiRecord);
			out.close();
		} catch (IOException e) {
			UI.println("Error: "+e);
		}
		if (balance > 0) {
			boolean[] butStat = { false, false, false, false, true };
			button = butStat;
			PID = 0;
			for (Player p : plaList) {
				balance += p.bet-100;
				p.bet = 100;
			}
			UI.drawString(plaList.get(0).status, 600, 280);// plaList.get(0).status
			activateMouse();
		} else {
			UI.println("Game Over");
			UI.println(deckList.size());
		}
	}

	public Card drawCard() {
		int drawInd = (int)(Math.random()*deckList.size());
		Card nextCard = deckList.get(drawInd);
		deckList.remove(drawInd);
		return nextCard;
	}

	public static void main(String[] args) {
		new BlackJack_Main();
	}
}
