import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
class MandelGui extends JFrame implements ActionListener {
	private static  int width = 800;
	private static int height = 600;
	private int maxCount = 192;
	private JToggleButton zoomBtn, moveBtn;
	private JButton zoomIn, zoomOut, zoomRestore, upMaxBtn, downMaxBtn;
	private Checkbox antialias, smoothing;
	private JTextField maxCountText;
	private DrawingArea drawingArea;
	public MandelGui() {
		setTitle("Mandelbrot Drawing");
		setMinimumSize(new Dimension(width, height));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(width, height);
		
		ImageIcon zoomIco = new ImageIcon("zoom.png");
		ImageIcon moveIco = new ImageIcon("move.png");
		ImageIcon zoominIco = new ImageIcon("zoom-in.png");
		ImageIcon zoomoutIco = new ImageIcon("zoom-out.png");
		ImageIcon zoomrestoreIco = new ImageIcon("zoom-best-fit.png");
		ImageIcon upIco = new ImageIcon("arrow_up.png");
		ImageIcon downIco = new ImageIcon("arrow_down.png");
		
		JPanel menuPanel = new JPanel();
		menuPanel.setAlignmentX(LEFT_ALIGNMENT);
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.LINE_AXIS));
		
		drawingArea = new DrawingArea(width, height, maxCount);
		
		ButtonGroup group = new ButtonGroup();
		zoomBtn = new JToggleButton(zoomIco);
		zoomBtn.setSelected(true);
		zoomBtn.addActionListener(this);
		zoomBtn.setActionCommand("zoom");
		moveBtn = new JToggleButton(moveIco);
		moveBtn.addActionListener(this);
		moveBtn.setActionCommand("move");
		group.add(zoomBtn);
		group.add(moveBtn);

		zoomIn = new JButton(zoominIco);
		zoomIn.setMargin(new Insets(0, 0, 0, 0));
		zoomIn.addActionListener(this);
		zoomIn.setActionCommand("zoomin");

		zoomOut = new JButton(zoomoutIco);
		zoomOut.setMargin(new Insets(0, 0, 0, 0));
		zoomOut.addActionListener(this);
		zoomOut.setActionCommand("zoomout");

		zoomRestore = new JButton(zoomrestoreIco);
		zoomRestore.setMargin(new Insets(0, 0, 0, 0));
		zoomRestore.addActionListener(this);
		zoomRestore.setActionCommand("restore");
		
		maxCountText = new JTextField(String.valueOf(maxCount), 4);
		maxCountText.setEditable(false);
		maxCountText.setMaximumSize(new Dimension(50, 20));

		downMaxBtn = new JButton(downIco);
		downMaxBtn.addActionListener(this);
		downMaxBtn.setActionCommand("down");
		
		upMaxBtn = new JButton(upIco);
		upMaxBtn.addActionListener(this);
		upMaxBtn.setActionCommand("up");
		
		antialias = new Checkbox("Antialiasing");
		antialias.setMaximumSize(new Dimension(120, 20));
		antialias.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				drawingArea.setAntialiasing(arg0.getStateChange()==ItemEvent.SELECTED);
			}
		});
		
		smoothing = new Checkbox("Smoothing");
		smoothing.setMaximumSize(new Dimension(120, 20));
		smoothing.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				drawingArea.setSmoothing(arg0.getStateChange()==ItemEvent.SELECTED);
			}
		});
		
		menuPanel.add(zoomBtn);
		menuPanel.add(moveBtn);
		menuPanel.add(Box.createRigidArea(new Dimension(50, 0)));
		menuPanel.add(zoomIn);
		menuPanel.add(zoomRestore);
		menuPanel.add(zoomOut);
		menuPanel.add(Box.createRigidArea(new Dimension(50, 0)));
		menuPanel.add(downMaxBtn);
		menuPanel.add(maxCountText);
		menuPanel.add(upMaxBtn);
		menuPanel.add(Box.createRigidArea(new Dimension(50, 0)));
		menuPanel.add(antialias);
		menuPanel.add(smoothing);
		add(menuPanel, BorderLayout.NORTH);
		add(drawingArea);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				drawingArea.startMandelDrawing();
			}
		});
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd.equals("zoom"))
			drawingArea.setMoveState(false);
		else if(cmd.equals("move"))
			drawingArea.setMoveState(true);
		else if(cmd.equals("zoomin"))
			drawingArea.zoomIn();
		else if(cmd.equals("zoomout"))
			drawingArea.zoomOut();
		else if(cmd.equals("restore"))
			drawingArea.restore();
		else if(cmd.equals("down")){
			maxCount -= maxCount / 4;
			maxCountText.setText(String.valueOf(maxCount));
			drawingArea.setMaxCount(maxCount);
		}
		else if(cmd.equals("up")){
			maxCount += maxCount / 4;
			maxCountText.setText(String.valueOf(maxCount));
			drawingArea.setMaxCount(maxCount);
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MandelGui mg = new MandelGui();
				mg.setVisible(true);
			}
		});
	}
}
