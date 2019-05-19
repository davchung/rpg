import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.JOptionPane;

public class RPGGame implements KeyListener {

	// these are all variables that allow the game to run
	private JFrame mainFrame = new JFrame("Role-Playing Game");
	private JPanel mainPanel;
	private static Timer timer;
	private static final int REFRESH_RATE = 10;
	public static int ticks = 0;

	// these are all variables that are involved with playing the game
	private int gameLevel = 1;
	private static Player player;
	private double pSpeed = 2.5; // player speed, TRY to keep this a factor of 50, but not obligated
	public static int lastR, lastD; // last direction the player was facing
	private int facing = 1;
	private Trader trader;
	private Map m;
	private Floor floor = new Floor();
	//private Attack pAttack; // player attack
	private Wall builtWall;

	// these are all variables related to GUIs
	private static Inventory i = new Inventory();
	private HelpPage hP = new HelpPage();
	private GameOver gO = new GameOver();
	private NextLevel nL = new NextLevel();
	private TradingPost tP = new TradingPost();

	// these variables are all ArrayLists of other variables
	private ArrayList<String> keys = new ArrayList<String>();
	private static ArrayList<GameObject> objects = new ArrayList<GameObject>();
	private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	private ArrayList<Wall> walls = new ArrayList<Wall>();
	private ArrayList<GameObject> damagedObjects = new ArrayList<GameObject>();
	private static ArrayList<Attack> enemyAttacks = new ArrayList<Attack>();
	private static ArrayList<Attack> special = new ArrayList<Attack>();
	private static ArrayList<Attack> primary = new ArrayList<Attack>();

	// these variables are all "switches" (imagine an on/off switch for a light
	// bulb)
	private boolean objDamaged = false;
	private boolean helpShown = false; // this makes the help page show up when
	// the user first opens the game so that they know how to play the game
	private boolean gameOver = false;
	private boolean invenShown = false; // inventory shown
	private boolean levelDone = false;
	private boolean tradeOpen = false;

	// these are getters for variables
	public static Player getPlayer() {
		return RPGGame.player;
	}
	public static ArrayList<Attack> getPrimary(){
		return primary;
	}
	public static ArrayList<Attack> getEnemyAttacks(){
		return enemyAttacks;
	}

	public static void setEnemyAttack(Attack atk) {
		enemyAttacks.add(atk);
	}

	public static ArrayList<GameObject> getObjects() {
		return RPGGame.objects;
	}

	public void setEnemies(ArrayList<Enemy> list) {
		for (Enemy e : list) {
			checkSpawns(e);
		}
	}

	public static ArrayList<Enemy> getEnemies() {
		return RPGGame.enemies;
	}

	public static ArrayList<Attack> getSpecial() {
		return RPGGame.special;
	}

	public static Inventory getInventory() {
		return i;
	}

	public void beginGame() {
		gameLevel = 1;
		player = new Knight(StartGame.SCREEN_HEIGHT / 2, StartGame.SCREEN_WIDTH / 2, 50, 50);
		// player = new Knight(100, 100, 50, 50);
		m = new Map(3, 2, 5);
		objects.addAll(m.getWalls());
		objects.addAll(m.getEObjs());
		objects.add(player);

		trader = new Trader();
		objects.add(trader);

		ArrayList<Enemy> list = new ArrayList<Enemy>();
		Demon d = null;
		Demon a = null;
		list.add(d);
		list.add(a);
		setEnemies(list);

		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPanel = new JPanel() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			// this is where all the drawing is done
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				// floor.drawFloor(g);//draws a floor...kinda
				for (GameObject go : objects) {
					go.draw(g); // draws all objects
				}
				player.draw(g, facing); // draws the player
				for (Attack a : special) {
					if (!a.expire()) {
						a.draw(g);
					}
				}
				for (Attack e : enemyAttacks) {
					e.draw(g);// draws all enemy attacks
				}
				for (Attack p: primary) {
					p.draw(g);
				}
				drawHitboxes(g); // draws all hitboxes. Dev-only.

				g.setColor(new Color(255, 255, 255));
				g.drawString("Player health: " + player.getHealth(), StartGame.SCREEN_WIDTH * 6 / 7, 25);
				g.setColor(new Color(255, 0, 0));
				for (GameObject go : objects) {
					if (go instanceof MoveableObject && ((MoveableObject) go).getLoss() != 0) {
						g.drawString("" + -((MoveableObject) go).getLoss(), (int) go.getCX() - 5, (int) go.getCY());
					}
				}
				if (objDamaged == true) {
					for (GameObject go : damagedObjects) {
						if (go.getHealth() < 100 && go.getHealth() > 0 && !go.getInvincibility()) {
							g.drawString("" + go.getHealth(), (int) go.getCX() - 8, (int) go.getCY());
						}
					}
				}

				if (helpShown == true) {
					hP.draw(g);
				}
				if (helpShown == false) {
					g.setColor(new Color(255, 255, 255));
					g.drawString("Press ? for help.", 30, 25);
				}

				if (levelDone == true) {
					nL.draw(g);
				}
				if (gameOver == true) {
					gO.draw(g);

				}

				if (invenShown == true) {
					i.draw(g);
				}
				if (tradeOpen == true) {
					tP.draw(g);
				}
			}
		};

		mainPanel.setBackground(new Color(40, 200, 240));
		// frame doesn't get minimized
		mainPanel.setPreferredSize(new Dimension(StartGame.SCREEN_WIDTH, StartGame.SCREEN_HEIGHT));
		mainFrame.add(mainPanel);
		// frame gets placed a little way from top and left side
		StartGame.center(mainFrame);
		mainFrame.pack();
		mainFrame.addKeyListener(this);
		// this timer controls the actions in the game and then repaints after each
		// update to data
		timer = new Timer(REFRESH_RATE, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mainPanel.repaint();
				controls();
				if (ticks > 50 || ticks == 0)
					movement();
				collision();
				update(); // updates movement
				ticks++;
			}

		});
		timer.start();
	}

	protected void update() {
		for (Object m : objects) {
			if (m instanceof MoveableObject) {
				((MoveableObject) m).update();
			}
		}
		//		for (Attack e : enemyAttacks) {
		//			e.update();
		//			for (Attack spec : special) {
		//				if (spec.collides(e)) {
		//					spec.change(e);
		//				}
		//			}
		//		}
		for (int i = 0;i<enemyAttacks.size();) {
			enemyAttacks.get(i).update();
			boolean coll = false;
			for (Attack spec:special) {
				if ((player instanceof Knight)&&spec.collides(enemyAttacks.get(i))) {
					spec.change(enemyAttacks.get(i));
					coll =true;
				}
			}
			if (!coll)
				i++;
		}
		for (Attack e: primary) {
			e.update();
		}
		for (Attack e : special) {
			e.update();
		}
		if (enemies.isEmpty()) {
			// this.levelDone=true; for testing
		}
		objects.removeAll(enemies);
		objects.addAll(enemies);
		objects.removeAll(enemyAttacks);
		objects.addAll(enemyAttacks);
		objects.removeAll(special);
		objects.addAll(special);
	}

	protected void drawHitboxes(Graphics g) {
		//g.drawRect((int) player.getLocX(), (int) player.getLocY(), (int) player.WIDTH, (int) player.WIDTH); // hitbox

		for (GameObject e : objects) {
			if (e instanceof MoveableObject) {
				g.drawRect((int) e.getLocX(), (int) e.getLocY(), e.WIDTH, e.HEIGHT);
			}
		}
		for (Attack r : enemyAttacks) {
			g.drawRect((int) (r.getLocX() + r.WIDTH / 10), (int) (r.getLocY() + r.HEIGHT / 10),
					(int) (r.WIDTH * 8 / 10), (int) (r.HEIGHT * 8 / 10));//
		}
		for (Attack p : primary) {
			g.drawRect((int) (p.getLocX() + p.WIDTH / 10), (int) (p.getLocY() + p.HEIGHT / 10),
					(int) (p.WIDTH * 8 / 10), (int) (p.HEIGHT * 8 / 10));
		}

	}

	private void checkSpawns(Enemy e2) {
		int x = GameObject.randInt(300, StartGame.SCREEN_WIDTH - 150) / 50;
		int y = GameObject.randInt(300, StartGame.SCREEN_HEIGHT - 150) / 50;
		e2 = new Demon(x * 50, y * 50, 100, 100, 1);
		for (GameObject w : objects) {
			if (!e2.equals(w) && !w.throughable && e2.collides(w)) {
				checkSpawns(e2);
				return;
			}
		}
		enemies.add(e2);
		objects.add(e2);
	}

	protected void movement() {
		for (Enemy enemy : enemies) {
			enemy.autoMove();
		}

	}

	protected void collision() {
		ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
		for (Attack p : primary) {
			if (p.expire())
				toRemove.add(p);
		}
		for (Attack a : special) {
			if (a.expire()) {
				toRemove.add(a);
			}
		}
		for (Attack e : enemyAttacks) {
			if (e.expire()) {
				toRemove.add(e);
			}
		}
		if (builtWall != null) {
			builtWall = null;
		}
		for (GameObject e : objects) {
			while (player.collides(e) && !e.throughable && !(player.equals(e))) {
				double dx = player.getCX() - e.getCX();
				double dy = player.getCY() - e.getCY();
				double m = Math.sqrt(dx * dx + dy * dy);
				dx = pSpeed * dx / m;
				dy = pSpeed * dy / m;
				player.moveX(dx / 5);
				player.moveY(dy / 5);
			}

			// tests if any enemy collides with the pAttack
			if (e instanceof Enemy) {
				if (((Enemy) e).getHealth() <= 0) {
					toRemove.add(e);
				}
				for (Attack p : primary) {
					if (p.collides(e)) {
						e.hit(p.getDamage(),p.getgameID());
					}
				}
			}
			if (!e.invincible && !(e instanceof Enemy)) {
				if (e.getHealth() <= 0)
					toRemove.add(e);
				for (Attack p : primary) {
					if (p.collides(e)) {
						e.hit(p.getDamage(),p.getgameID());
						damagedObjects.add(e);
						objDamaged = true;
					}
				}
				for (Attack a : enemyAttacks) {
					if (a.collides(e)) {
						e.hit(a.getDamage(),a.getgameID());
						damagedObjects.add(e);
						objDamaged = true;
					}
				}
			}

			if (player.collides(e) && e instanceof Coin) {
				toRemove.add(e);
			}
		}

		if (player.getHealth() <= 0) {
			toRemove.add(player);
			gameOver = true;
			pause();
		}
		for (Attack e : enemyAttacks) {
			if (e.collides(player)) {
				player.hit(e.getDamage(),e.getgameID());
			}
		}
		for (GameObject g : toRemove) {
			g.uponRemoval();
		}
		objects.removeAll(toRemove);
		enemies.removeAll(toRemove);
		primary.removeAll(toRemove);
		special.removeAll(toRemove);
		if (enemies.size() == 0) {
			// levelDone = true;
		}

	}

	private boolean wallCollision(GameObject object) {
		for (GameObject obs : objects) {
			if (!obs.throughable && object.collides(obs) && !(obs.equals(object)))
				return true;
		}
		return false;

	}

	// this allows the player to be controlled by W A S D
	private void controls() {
		int down = 0, right = 0;
		if (player.canMove()) {
			if (keys.contains("w")) {
				player.moveY(-pSpeed);
				down -= 1;
				while (wallCollision(player)) {
					player.moveY(pSpeed / 5);
				}
			}
			if (keys.contains("a")) {
				player.moveX(-pSpeed);
				right -= 1;
				while (wallCollision(player)) {
					player.moveX(pSpeed / 5);
				}
			}
			if (keys.contains("s")) {
				player.moveY(pSpeed);
				down += 1;
				while (wallCollision(player)) {
					player.moveY(-pSpeed / 5);
				}
			}
			if (keys.contains("d")) {
				player.moveX(pSpeed);
				right += 1;
				while (wallCollision(player)) {
					player.moveX(-pSpeed / 5);
				}
			}
			if (down != 0 || right != 0) {
				lastR = right;
				lastD = down;
			}
			if (right != 0) {
				facing = right;
			}
			player.setRight(right);
			player.setDown(down);
		}
		// this allows the j key to control attacking (main commands)
		if (keys.contains("j")) {
			if (player.canAttack()) {
				primary.add(player.getAttack());
				player.addCooldown(60);
			}
		}
		if (keys.contains("k")) {
			if (player.canSpecial()) {
				special.add(player.getSpecial());
				player.addSpecialCooldown(60);
			}
		}
	}

	public static void pause() {
		if (timer.isRunning()) {
			timer.stop();
		} else {
			timer.start();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		String lower = "" + e.getKeyChar();
		lower = lower.toLowerCase();
		if (!keys.contains(lower)) {
			keys.add(lower);
		}

		// pause button
		if (keys.contains("p")) {
			pause();
			if (helpShown == true) {
				helpShown = false;
			}
		}

		// help button
		if (keys.contains("?")) {
			helpShown = !helpShown;
			mainPanel.repaint();
			/*if (helpShown == true) {
				timer.stop();
			}*/
			pause();
		}

		// check inventory
		if (keys.contains("i")) {
			invenShown = !invenShown;
			mainPanel.repaint();
			pause();
		}

		// game over
		if (gameOver == true && (keys.contains("n"))) {
			objects.clear();
			enemies.clear();
			enemyAttacks.clear();
			new RPGGame().beginGame();
			mainFrame.dispose();
		}

		// next level
		if (levelDone == true && (keys.contains("n"))) {
			gameLevel++;
			System.out.println("Game Level: " + gameLevel);
		}

		// trading post
		if (keys.contains("k") && player.collides(trader)) {
			tradeOpen = !tradeOpen;
			mainPanel.repaint();
			pause();
		}

		// trading post - buy option 1
		if (keys.contains("1") && tradeOpen == true) {
			JOptionPane.showConfirmDialog(null, "Are you sure you want to purchase [1] ?");
			i.getItems().add(new Weapon("axe.png", 20));
			JOptionPane.showMessageDialog(null, "[1] has been added to your Inventory.");
			keys.remove(keys.indexOf("1"));
		}
		// trading post - buy option 2
		else if (keys.contains("2") && tradeOpen == true) {
			JOptionPane.showConfirmDialog(null, "Are you sure you want to purchase [2] ?");
			JOptionPane.showMessageDialog(null, "This feature does not work yet.");
			keys.remove(keys.indexOf("2"));
		}
		// trading post - buy option 3
		else if (keys.contains("3") && tradeOpen == true) {
			JOptionPane.showConfirmDialog(null, "Are you sure you want to purchase [3] ?");
			JOptionPane.showMessageDialog(null, "This feature does not work yet.");
			keys.remove(keys.indexOf("3"));
		}
		// trading post - buy option 4
		else if (keys.contains("4") && tradeOpen == true) {
			JOptionPane.showConfirmDialog(null, "Are you sure you want to purchase [4] ?");
			JOptionPane.showMessageDialog(null, "This feature does not work yet.");
			keys.remove(keys.indexOf("4"));
		}
		// trading post - buy option 5
		else if (keys.contains("5") && tradeOpen == true) {
			JOptionPane.showConfirmDialog(null, "Are you sure you want to purchase [5] ?");
			JOptionPane.showMessageDialog(null, "This feature does not work yet.");
			keys.remove(keys.indexOf("5"));
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		String lower = "" + e.getKeyChar();
		lower = lower.toLowerCase();
		if (keys.contains(lower)) {
			keys.remove(lower);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
