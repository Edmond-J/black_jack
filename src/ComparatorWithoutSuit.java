import java.util.Comparator;

public class ComparatorWithoutSuit implements Comparator<Card> {
	public ComparatorWithoutSuit() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Card c1, Card c2) {
		return c2.rank-c1.rank;
	}
}
