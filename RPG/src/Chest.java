
public class Chest extends GameObject{
	
	private double amountOfGold;
	
	public Chest(double x, double y, double w, double h, double gold) {
		super(x, y, w, h, "chest.png", false);
		setAmountOfGold(gold);
	}

	public double getAmountOfGold() {
		return amountOfGold;
	}

	public void setAmountOfGold(double amountOfGold) {
		this.amountOfGold = amountOfGold;
	}
	
}
