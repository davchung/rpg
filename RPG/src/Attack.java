
public class Attack extends GameObject{
	
	private int expire;
	
	public Attack (int x, int y, int w, int h, int right, int down, int ticks, String s) {
		super(x ,y, w, h, true,true,1, s); // uses GameObject's constructor #1
		expire = ticks+20;
		super.moveX(right*50-25);
		super.moveY(down*50-25);
	}

	public boolean expire(){
		if (RPGGame.ticks>expire) {
			return true;
		}
		return false;
	}
}

