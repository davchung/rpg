import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class RPGGame implements KeyListener {

	// these are all variables that allow the game to run
	private JFrame mainFrame = new JFrame("Role-Playing Game");
	private JPanel mainPanel;
	private static Timer timer;
	private static final int REFRESH_RATE = 10;
	public static int ticks = 0;

	// these are all variables that are involved with playing the game
	private int gameLevel = 1;
	private static Knight player;
	private double pSpeed = 2.5; // player speed, TRY to keep this a factor of 50, but not obligated
	public static int lastR, lastD; // last direction the player was facing
	private int facing = 1;
	private Demon demon;
	private Trader trader;
	private Map m = new Map(5);
	Floor floor = new Floor();

	private Attack pAttack; // player attack
	private static Attack eAttack; // enemy attack
	private Wall builtWall;

	// these are all variables related to GUIs
	private Inventory i = new Inventory();
	private ChestLoot cL = new ChestLoot();
	private HelpPage hP = new HelpPage();
	private GameOver gO = new GameOver();
	private NextLevel nL = new NextLevel();

	// these variables are all ArrayLists of other variables
	private ArrayList<String> keys = new ArrayList<String>();
	private static ArrayList<GameObject> objects = new ArrayList<GameObject>();
	private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	private ArrayList<Wall> walls = new ArrayList<Wall>();
	private ArrayList<GameObject> damagedObjects = new ArrayList<GameObject>();
	private ArrayList<Attack> enemyAttacks = new ArrayList<Attack>();

	// these variables are all "switches" (imagine an on/off switch for a light
	// bulb)
	private boolean objDamaged = false;
	private boolean helpPage = false;
	private boolean gameOver = false;
	private boolean iVisible = false; // inventory visible
	private boolean levelDone = false;
	private boolean tradeOpen = false;

	// these are getters for variables
	public static Player getPlayer() {
		return RPGGame.player;
	}

	public Demon getDemon() {
		return this.demon;
	}

	public static void setEnemyAttack(Attack atk) {
		RPGGame.eAttack = atk;
	}

	public static ArrayList<GameObject> getObjects() {
		return RPGGame.objects;
	}

	public static ArrayList<Enemy> getEnemies() {
		return RPGGame.enemies;
	}

	public void beginGame() {
		gameLevel = 1;
		player = new Knight(100, 100, 50, 50);
		objects.addAll(m.getWalls());
		objects.addAll(m.getEObjs());
		objects.add(player);
		checkSpawns();
		objects.add(demon);
		enemies.add(demon);
		trader = new Trader();
		objects.add(trader);

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
				//floor.drawFloor(g);
				for (GameObject go : objects) {
					go.draw(g);

				}
				player.draw(g, facing);

				if (pAttack != null && !pAttack.expire()) {
					pAttack.draw(g);
				}
				if (eAttack != null && !eAttack.expire()) {
					eAttack.draw(g);
				}

				g.drawString("Knight health: " + player.getHealth(), StartGame.SCREEN_WIDTH * 5 / 6, 65);
				g.drawString("Demon health: " + demon.getHealth(), StartGame.SCREEN_WIDTH * 5 / 6, 85);

				g.setColor(new Color(255, 0, 0));
				for (GameObject go:objects) {
					if (go instanceof MoveableObject&&((MoveableObject) go).getLoss()!=0) {
						//System.out.println(((MoveableObject) go).getLoss());
						g.drawString(""+-((MoveableObject) go).getLoss(), (int)go.getCX()-5, (int)go.getCY());
					}
				}
				if (objDamaged == true) {
					for (GameObject go : damagedObjects) {
						if (go.getHealth() < 100 && go.getHealth() > 0 && !go.invincibility()) {
							g.drawString("" + go.getHealth(), (int) go.getCX() - 8, (int) go.getCY());
						}
					}
				}

				if (helpPage == true) {
					hP.draw(g);
				}
				if (helpPage == false) {
					g.setColor(new Color(255, 255, 255));
					g.drawString("Press ? for help.", 20, 25);
				}

				if (levelDone == true) {
					nL.draw(g);
				}
				if (gameOver == true) {
					gO.draw(g);
				}

				if (iVisible == true) {
					i.draw(g);
				}
				if (tradeOpen == true) {

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
				if (ticks>10)
					movement();
				collision();
				update(); //updates last health
				ticks++;
			}

		});
		timer.start();
	}

	protected void update() {
		for (Object m: objects) {
			if (m instanceof MoveableObject)
				((MoveableObject) m).update();
		}

	}

	private void checkSpawns() {
		demon = new Demon(GameObject.randInt(200, 500), GameObject.randInt(200, 500), 100, 100, 1);
		for (GameObject w : objects) {
			if (demon.collides(w)&&!demon.equals(w)&&!w.throughable) {
				checkSpawns();
				return;
			}
		}
	}

	protected void movement() {
		for (Object enemy : enemies) {
			((Enemy) enemy).autoMove();
		}

	}

	protected void collision() {
		ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
		if (pAttack != null && pAttack.expire()) {
			pAttack = null;
		}
		if (eAttack != null && eAttack.expire()) {
			eAttack = null;
		}
		if (builtWall != null) {
			builtWall = null;
		}
		for (GameObject e : objects) {
			while (player.collides(e) && !e.throughable&&!(player.equals(e))) {
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
					levelDone = true;
				}
				if (pAttack != null && pAttack.collides(e)) {
					((Enemy) e).hit(player.getDamage());
				}
			}
			if(e instanceof Crate || e instanceof Chest || e instanceof Wall || e instanceof ExplosiveBarrel) {
				if (e.getHealth() <= 0)
					toRemove.add(e);
				if ((pAttack != null && pAttack.collides(e)) || (eAttack != null && eAttack.collides(e))) {
					e.hit();
					damagedObjects.add(e);
					objDamaged = true;
				}
			}
		}

		if (player.getHealth() <= 0) {
			toRemove.add(player);
			gameOver = true;
			pause();
		}
		if (eAttack != null && eAttack.collides(player)) {
			player.hit(demon.getDamage());
		}
		objects.removeAll(toRemove);
		enemies.removeAll(toRemove);
		for (GameObject go : toRemove) {
			if (go instanceof Wall) {
				i.addWalls(1);
			}
		}
		walls.removeAll(toRemove);

	}

	private boolean wallCollision(GameObject object) {
		for (GameObject obs : objects) {
			if (!obs.throughable && object.collides(obs)&&!(obs.equals(object)))
				return true;
		}
		return false;

	}

	// this allows the player to be controlled by W A S D
	private void controls() {
		int down = 0, right = 0;
		if (pAttack == null && builtWall == null) {
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

			// this allows the j key to control attacking (main commands)
			if (keys.contains("j")) {
				if (player.canMove(60)) {
					pAttack = player.getAttack();
				}
			}

			// this allows the k key to control using (secondary commands)
			if (keys.contains("k") && player.collides(trader)) {
				tradeOpen = true;
			}

			// for developers only!
			if (keys.contains("o")) {
				objects.removeAll(getEnemies());
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
			if (helpPage)
				helpPage = false;
		}

		// help button
		if (keys.contains("?")) {
			helpPage = !helpPage;
			mainPanel.repaint();
			pause();
		}

		// check inventory
		if (keys.contains("i")) {
			iVisible = !iVisible;
			if (!iVisible)
				pause();
		}

		// game over
		if (gameOver == true && (keys.contains("n"))) {
			objects.clear();
			enemies.clear();
			new RPGGame().beginGame();
			mainFrame.dispose();
		}

		// next level
		if (levelDone == true && (keys.contains("n"))) {
			gameLevel++;
			System.out.println("Game Level: " + gameLevel);
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
