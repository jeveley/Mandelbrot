import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
class DrawingArea extends JPanel implements MouseListener, MouseMotionListener{
	private Image image;
	protected Graphics graphics;
	protected int width, height;
	private int threadsNum = 4;
	protected int maxCount;
	protected double viewX = 0.0;
	protected double viewY = 0.0;
	protected double zoom = 1;
	protected boolean smooth = false;
	protected boolean antialiased = false;
	private boolean dragging = false;
	private boolean rectSelect = true;
	private Mandel[] threads;
	private int mouseX, mouseY;
	private int dragX, dragY;
	private int colorsDistance = 18;
	private final int[][] colorValues = {
			{0, 0, 0}, {0, 0, 35}, {0, 30, 102}, {24, 106, 247},
			{147, 192, 255}, {254, 255, 221}, {255, 233, 186}, {255, 170, 50}, 
			{255,153,0}, {255, 61, 35}, {255, 0, 0}, {99, 0, 0}
	};
	public Color colors[];
	public void initializeColors() {
		int n = colorsDistance;
		colors = new Color[colorValues.length*n];
		for (int i = 0; i < colorValues.length; ++i) { // interpolate all colors
			int[] c1 = colorValues[i]; // first referential color
			int[] c2 = colorValues[(i + 1) % colorValues.length]; // second ref. color
			for (int j = 0; j < n; ++j) // linear interpolation of RGB values
				colors[i*n + j] = new Color(
						(c1[0] * (n - 1 - j) + c2[0] * j) / (n - 1),
						(c1[1] * (n - 1 - j) + c2[1] * j) / (n - 1),
						(c1[2] * (n - 1 - j) + c2[2] * j) / (n - 1));
		}
	}
	public DrawingArea(int w, int h, int maxCnt){
		addMouseListener(this);
		addMouseMotionListener(this);
		maxCount = maxCnt;
		width = w;
		height = h;
		initializeColors();
		threads = new Mandel[threadsNum];
	}

	public void startMandelDrawing(){
		redraw();
	}

	private void redraw() {
		Dimension size = getSize();
		if (image == null || size.width != width || size.height != height) {
			width = size.width;
			height = size.height;
			image = createImage(width, height);
			graphics = image.getGraphics();
		}
		for(int i=0;i<threads.length;++i){
			if (threads[i] != null && threads[i].isAlive()) {
				threads[i].interrupt();
			}
			else{
				if(threads[i]!=null)System.out.println(threads[i] + "" + threads[i].isAlive());
				threads[i] = new Mandel(this, colors, i, threads.length);
				threads[i].setPriority(Thread.MIN_PRIORITY);
				threads[i].start();

			}
		}
	}

	@Override
	public void paint(Graphics g){
		if (image == null) // nothing to show
			return;
		Dimension size = getSize();
		if (size.width != width || size.height != height) {
			redraw();
			return;
		}
		g.drawImage(image, 0, 0, null);
		if (dragging) {
			g.setColor(Color.black);
			g.setXORMode(Color.white);
			if (rectSelect) {
				int x = Math.min(mouseX, dragX);
				int y = Math.min(mouseY, dragY);
				double w = mouseX + dragX - 2 * x;
				double h = mouseY + dragY - 2 * y;
				double r = Math.max(w / width, h / height);
				g.drawRect(x, y, (int)(width * r), (int)(height * r));
			}
			else
				g.drawLine(mouseX, mouseY, dragX, dragY);
		}
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = dragX = e.getX();
		mouseY = dragY = e.getY();
		dragging = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dragging = false;
		int x = e.getX();
		int y = e.getY();
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			double r = zoom/Math.min(width, height); // actual pixel size
			if (!rectSelect) { // moved
				viewX += r*(mouseX - x);
				viewY += r*(mouseY - y);
			}
			else if (x == mouseX && y == mouseY) { // zoom in
				viewX += 0.5 * x * r;
				viewY += 0.5 * y * r;
				zoom *= 0.5;
			}
			else { // zoomed
				int mx = Math.min(x, mouseX);
				int my = Math.min(y, mouseY);
				viewX += mx * r;
				viewY += my * r;
				double w = x + mouseX - 2 * mx;
				double h = y + mouseY - 2 * my;
				zoom *= Math.max(w / width, h / height);
			}
			redraw();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			dragX = e.getX();
			dragY = e.getY();
			repaint();
		}
	}

	public void restore(){
		viewX = viewY = 0.0;
		zoom = 1;
		redraw();
	}

	public void zoomIn(){
		viewX += 0.25 * zoom;
		viewY += 0.25 * zoom;
		zoom *= 0.5;
		redraw();
	}

	public void zoomOut(){
		viewX -= 0.5 * zoom;
		viewY -= 0.5 * zoom;
		zoom *= 2.0;
		redraw();
	}

	public void setSmoothing(boolean val){
		smooth = val;
		redraw();
	}

	public void setAntialiasing(boolean val){
		antialiased = val;
		redraw();
	}

	public void setMoveState(boolean val){
		rectSelect = !val;
		if(val)
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		else
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		if (dragging)
			repaint();
	}

	public void setMaxCount(int maxCnt) {
		maxCount = maxCnt;
		redraw();
	}

	//unused
	@Override
	public void mouseClicked(MouseEvent arg0) {}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mouseMoved(MouseEvent arg0) {}
}
