
public class Attack extends GameObject{
	
	private int expire;
	
	public Attack (int x, int y, int right, int down, int ticks, String s) {
<<<<<<< HEAD
<<<<<<< HEAD
		super(x ,y, 50, 50, true, Integer.MAX_VALUE, s); // uses GameObject's constructor #1
=======
		super(x ,y, 50, 50, true, s); // uses GameObject's constructor #1
>>>>>>> parent of bb8568c... Adjusted health for all gameobjects
=======
		super(x ,y, 50, 50, true, s); // uses GameObject's constructor #1
>>>>>>> parent of bb8568c... Adjusted health for all gameobjects
		setRight(right);
		setDown(down);
		expire = ticks+20;
		super.moveX(getRight()*50-25);
		super.moveY(getDown()*50-25);
	}

	public boolean expire(){
		if (RPGGame.ticks>expire) {
			return true;
		}
		return false;
	}
}

