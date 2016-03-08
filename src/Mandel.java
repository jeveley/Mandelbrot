import java.awt.Color;

public class Mandel extends Thread{
	private DrawingArea parent;
	int threadNum, threadTotal;
	public Color colors[];
	
	public Mandel(DrawingArea p, Color[] col, int thnum, int thtotal){
		parent = p;
		colors = col;
		threadNum = thnum;
		threadTotal = thtotal;
	}

	@Override
	public void run() {
		while (true) {
			while(draw());
			synchronized (this) {
				try {
					wait();
				}
				catch (InterruptedException e) {}
			}
		}
	}

	private boolean draw() {
		int k=8;
		for(int y = threadNum*parent.height/threadTotal; 
		y < threadNum*parent.height/threadTotal + parent.height/threadTotal; y += k){
			if (Thread.interrupted())
				return true;
			for (int x = 0; x < parent.width; x += k) {
				double r = parent.zoom / Math.min(parent.width, parent.height);
		        double dx = 2.5 * (x * r + parent.viewX) - 2;
		        double dy = 1.25 - 2.5 * (y * r + parent.viewY);
				Color color = getColorForPoint(dx, dy);
				synchronized (parent.graphics) {
					parent.graphics.setColor(color);
					parent.graphics.fillRect(x, y, k, k);
				}
			}
		}
		parent.repaint();
		for(int i=0;k>1;k/=2, ++i){
			for(int j=1;j<=3;++j){
				for(int y = threadNum*parent.height/threadTotal+ (2&j)*k/4; 
				y < threadNum*parent.height/threadTotal + parent.height/threadTotal + (2&j)*k/4; y += k){
					if (Thread.interrupted())
						return true;
					for (int x = (1&j)*k/2; x < parent.width + (1&j)*k/2; x += k) {
						double r = parent.zoom / Math.min(parent.width, parent.height);
				        double dx = 2.5 * (x * r + parent.viewX) - 2;
				        double dy = 1.25 - 2.5 * (y * r + parent.viewY);
						Color color = getColorForPoint(dx, dy);
						if (parent.antialiased) {
							Color c1 = getColorForPoint(dx - 0.25 * r, dy - 0.25 * r);
							Color c2 = getColorForPoint(dx + 0.25 * r, dy - 0.25 * r);
							Color c3 = getColorForPoint(dx + 0.25 * r, dy + 0.25 * r);
							Color c4 = getColorForPoint(dx - 0.25 * r, dy + 0.25 * r);
							int red = (color.getRed() + c1.getRed() + c2.getRed() + c3.getRed() + c4.getRed()) / 5;
							int green = (color.getGreen() + c1.getGreen() + c2.getGreen() + c3.getGreen() + c4.getGreen()) / 5;
							int blue = (color.getBlue() + c1.getBlue() + c2.getBlue() + c3.getBlue() + c4.getBlue()) / 5;
							color = new Color(red, green, blue);
						}
						synchronized (parent.graphics) {
							parent.graphics.setColor(color);
							parent.graphics.fillRect(x, y, k/2, k/2);
						}
					}parent.repaint();
				}
			}
		}
		return false;
	}

	private Color getColorForPoint(double x, double y) {
		int count = mandel(0.0, 0.0, x, y);
		Color color = colors[count / 256 % colors.length];
		if (parent.smooth) {
			Color color2 = colors[(count / 256 + colors.length - 1) % colors.length];
			int k1 = count % 256;
			int k2 = 255 - k1;
			int red = (k1 * color.getRed() + k2 * color2.getRed()) / 255;
			int green = (k1 * color.getGreen() + k2 * color2.getGreen()) / 255;
			int blue = (k1 * color.getBlue() + k2 * color2.getBlue()) / 255;
			color = new Color(red, green, blue);
		}
		return color;
	}

	private int mandel(double zRe, double zIm, double pRe, double pIm) {
		double zRe2 = zRe * zRe;
		double zIm2 = zIm * zIm;
		double zM2 = 0.0;
		int count = 0;
		while (zRe2 + zIm2 < 4.0 && count < parent.maxCount) {
			zM2 = zRe2 + zIm2;
			zIm = 2.0 * zRe * zIm + pIm;
			zRe = zRe2 - zIm2 + pRe;
			zRe2 = zRe * zRe;
			zIm2 = zIm * zIm;
			++count;
		}
		if (count == 0 || count == parent.maxCount)
			return 0;
		count *= 256;
		if(parent.smooth){
			zM2 += 0.000000001;
			return count + (int)(255.0 * Math.log(4 / zM2) / Math.log((zRe2 + zIm2) / zM2));
		}
		return count;
	}
}
