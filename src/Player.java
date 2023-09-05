import java.util.ArrayList;

public class Player {
	String name;
	int balance;
	double bet;
	ArrayList<Card> handList = new ArrayList<>();

	public Player(String name) {
		this.name=name;
	}
	
	public void resetHand(){
		handList = new ArrayList<>();
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

	public int bestHand() {
		ArrayList<Integer> hands = bjPoints();
		if (hands.get(1) <= 21)
			return hands.get(1);
		else if (hands.get(0) <= 21)
			return hands.get(0);
		else return 0;//0代表爆了
	}
	
	public void printHand(){
		for(Card c: handList){
			System.out.print(c.rank+"  ");
		}
		System.out.println("bestHand: "+bestHand());
	}
}
