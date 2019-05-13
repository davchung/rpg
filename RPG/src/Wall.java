
public class Wall extends Environment {

	private static int health = 20;
	public int getHealth() { return this.health; }
	private int hittable = 0;

	public Wall(double x, double y, double w, double h, int health) {
		super(x, y, w, h, false, health, "/sprites/wall_mid.png"); // uses Environment's constructor #1
	}

	public void hit() {
		if (RPGGame.ticks > hittable) {
			health -= 10;
			hittable = RPGGame.ticks + 26;
			return;
		}

	}

}
