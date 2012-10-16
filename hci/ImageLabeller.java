package hci;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main class of the program - handles display of the main window
 * @author Michal
 *
 */
public class ImageLabeller extends JFrame {
	/**
	 * some java stuff to get rid of warnings
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * main window panel
	 */
	JPanel appPanel = null;
	
	/**
	 * toolbox - put all buttons and stuff in these two toolboxes
	 */
	JPanel topToolboxPanel = null;
	JPanel bottomToolboxPanel = null;
	JPanel rightToolboxPanel = null;
	
	/**
	 * image panel - displays image and editing area
	 */
	ImagePanel imagePanel = null;

	/**
	 * middle panel - holds image and middletoolbox panels
	 */
	JPanel middlePanel = null;
	
	/**
	 * handles New Object button action
	 */
	public void addNewPolygon() {
		imagePanel.addNewPolygon();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		imagePanel.paint(g); //update image panel
	}
	
	/**
	 * sets up application window
	 * @param imageFilename image to be loaded for editing
	 * @throws Exception
	 */
	public void setupGUI(String imageFilename) throws Exception {
		this.addWindowListener(new WindowAdapter() {
		  	public void windowClosing(WindowEvent event) {
		  		//here we exit the program (maybe we should ask if the user really wants to do it?)
		  		//maybe we also want to store the polygons somewhere? and read them next time
		  		System.out.println("Bye bye!");
		    	System.exit(0);
		  	}
		});

		//setup main window panel
		appPanel = new JPanel();
		appPanel.setLayout(new BoxLayout(appPanel, BoxLayout.Y_AXIS));
		this.setLayout(new BoxLayout(appPanel, BoxLayout.Y_AXIS));
		this.setContentPane(appPanel);

 
		//create top toolbox panel
        topToolboxPanel = new JPanel();
        
        //Add button
		JButton randbutton = new JButton("exit");
		randbutton.setMnemonic(KeyEvent.VK_N);
		randbutton.setSize(50, 20);
		randbutton.setEnabled(true);
		randbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	System.exit(1);
			}
		});
		randbutton.setToolTipText("Click to exit");
		
		topToolboxPanel.add(randbutton);
		
		//add toolbox to window
		appPanel.add(topToolboxPanel);
		

		//create middle panel with image and sidebar
		middlePanel = new JPanel();
		//middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));

        //Create and set up the image panel.
		imagePanel = new ImagePanel(imageFilename);
		imagePanel.setOpaque(true); //content panes must be opaque

		middlePanel.add(imagePanel);

		rightToolboxPanel = new JPanel();
		
        //Add button
		JButton newPolyButton = new JButton("New object");
		newPolyButton.setMnemonic(KeyEvent.VK_N);
		newPolyButton.setSize(50, 20);
		newPolyButton.setEnabled(true);
		newPolyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	addNewPolygon();
			}
		});
		newPolyButton.setToolTipText("Click to add new object");
		
		rightToolboxPanel.add(newPolyButton);
		
		//add toolbox to window
		middlePanel.add(rightToolboxPanel);

        appPanel.add(middlePanel);


        //create toolbox panel
        bottomToolboxPanel = new JPanel();
        
        //Add button
		JButton editLabelButton = new JButton("Edit Label");
		editLabelButton.setMnemonic(KeyEvent.VK_N);
		editLabelButton.setSize(50, 20);
		editLabelButton.setEnabled(true);
		editLabelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	addNewPolygon();
			}
		});
		editLabelButton.setToolTipText("Click to edit a label");
		
		bottomToolboxPanel.add(editLabelButton);
		
		//add toolbox to window
		appPanel.add(bottomToolboxPanel);
		
		//display all the stuff
		this.pack();
        this.setVisible(true);
	}
	
	/**
	 * Runs the program
	 * @param argv path to an image
	 */
	public static void main(String argv[]) {
		try {
			//create a window and display the image
			ImageLabeller window = new ImageLabeller();
			window.setupGUI(argv[0]);
		} catch (Exception e) {
			System.err.println("Image: " + argv[0]);
			e.printStackTrace();
		}
	}
}
