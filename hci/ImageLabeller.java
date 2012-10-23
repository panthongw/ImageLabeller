package hci;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ItemEvent;

import java.util.ArrayList;

import hci.utils.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import hci.utils.Point;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Main class of the program - handles display of the main window
 * @author Michal
 *
 */
public class ImageLabeller extends JFrame {
	/**
	 * Image Variables for Saving
	 */	
  private BufferedImage bufferedImageSrc = null;
  private static BufferedImage bufferedImage = null;
  private static ArrayList<Point> currentPolygon = null;
  private static ArrayList<ArrayList<Point>> polygonsList = null;

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
	ImagePanel imagePanel = new ImagePanel();

	/**
	 * middle panel - holds image and middletoolbox panels
	 */
	JPanel middlePanel = null;

	/**
	 * open combobox - switch quickly between images
	 */
	JComboBox openComboBox = null;

	/**
	 * buttons to delete and edit labels
	 */
	JButton delLabelButton = null;
	JButton editLabelButton = null;

	/**
	 * scrollpane, listbox, and list model for storing the labels
	 */
	DefaultListModel labelsListModel = new DefaultListModel();
	JList labelsBox = null;
	JScrollPane labelsPane = null;
	ArrayList<Label> labelsList = new ArrayList<Label>();

	/**
	 * previous label index - needed to redraw that label green
	 */
	int prevLabelIdx = -1;

	/**
	 * curpolysave - keep current polygon to save for label array
	 */
	ArrayList<Point> curPolygonSave;

	/**
	 * labelcounter - counts number of labels for default label name display
	 */
	private static int labelCounter = 0;
	
	/**
	 * Launches file choose to retrieve an image path
	*/
	public void launchFileChooser(){
		String imagePath = null;
		JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "JPG & GIF Images", "jpg", "gif");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(this);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
        imagePath = chooser.getSelectedFile().getAbsolutePath().toString();
    }
    imagePanel.setImage(imagePath);

    // Setting Buffer Image variables to perform Save later
    try {
          bufferedImageSrc = ImageIO.read(new File(imagePath));
    			bufferedImage = toBufferedImage(bufferedImageSrc);
        } catch(IOException e) {
            System.out.println("Error Buffering Image");
        }

    openComboBox.addItem(chooser.getSelectedFile().toString());
    openComboBox.setSelectedItem(chooser.getSelectedFile().toString());
	}

	private void removeSelectedFileFromList(){
		openComboBox.removeItem(openComboBox.getSelectedItem());
		if(openComboBox.getItemCount() == 0){
			imagePanel = new ImagePanel();
		} else{
			imagePanel.setImage((String) openComboBox.getSelectedItem());
		}
	}

	private void changeFileInList(){
		imagePanel.setImage((String) openComboBox.getSelectedItem());
	}

	private void addLabelToList(){
		curPolygonSave = new ArrayList<Point>();
		for(int i = 0; i < imagePanel.getCurrentPolygon().size(); i++){
			curPolygonSave.add(imagePanel.getCurrentPolygon().get(i));
		}

		imagePanel.addNewPolygon();
		String str = JOptionPane.showInputDialog(null, "Enter label name : ",
													"label" + labelCounter, 1);
  		if(str == null || str.equals("")){
  			str = "label" + labelCounter;
  		}

  		labelCounter++;

  		labelsListModel.addElement(str);

  		//create label - polygon pair here
  		labelsList.add(new Label(curPolygonSave, str));

  		labelsBox.setSelectedIndex(labelsBox.getModel().getSize() - 1);

  		if(!editLabelButton.isEnabled()){
  			editLabelButton.setEnabled(true);
  			delLabelButton.setEnabled(true);
  		}
	}

	public void updateSelectedLabel(){
		
		if(labelsBox.isSelectionEmpty()){
			return;
		}

		//turn previous label green
		if(prevLabelIdx != -1 && prevLabelIdx < labelsBox.getModel().getSize()){
			imagePanel.drawPolygon(labelsList.get(prevLabelIdx).getPolygon(), Color.GREEN, true);
		}

		//make new label and turn it red
		Label curLabel = labelsList.get(labelsBox.getSelectedIndex());
		imagePanel.drawPolygon(curLabel.getPolygon(), Color.RED, true);
		prevLabelIdx = labelsBox.getSelectedIndex();

		editLabelButton.setEnabled(true);
  		delLabelButton.setEnabled(true);
	}

	public void editLabelText(){
		String str = JOptionPane.showInputDialog(null, "Enter label name : ",
													"label" + labelCounter, 1);
  		if(str == null || str.equals("")){
  			str = "label" + labelCounter;
  			labelCounter++;
  		}

  		((DefaultListModel)(labelsBox.getModel())).setElementAt(str, labelsBox.getSelectedIndex());
  		labelsList.get(labelsBox.getSelectedIndex()).setLabel(str);
	}

	public void removeLabelFromImage(){
		int selIdx = labelsBox.getSelectedIndex();
		labelsBox.clearSelection();
		labelsList.remove(selIdx);
		((DefaultListModel)(labelsBox.getModel())).removeElementAt(selIdx);

		imagePanel.reloadImage();

		imagePanel.getPolygonsList().clear();
		ArrayList<Point> temp;
		for(int i = 0; i < labelsList.size(); i++){
			temp = new ArrayList<Point>();

			for(int j = 0; j < labelsList.get(i).getPolygon().size(); j++){
				temp.add(labelsList.get(i).getPolygon().get(j));
			}

			imagePanel.getPolygonsList().add(temp);
		}

		imagePanel.drawAllPolygons();

		editLabelButton.setEnabled(false);
  		delLabelButton.setEnabled(false);
	}

	/**
	 * Saving functionality
	 */
	private void saveLabelledImage(String ext) {
				String fileName = JOptionPane.showInputDialog(null, "Enter File Name: ", "", 1);
 
        //Creates a new directory to store Image and Polygon Coordinates
        boolean dirSuccess = (new File("./images/"+fileName)).mkdirs();
        String filePath = "./images/"+fileName+"/"+fileName;
        File file = new File(filePath + "." + ext);
        try {
            ImageIO.write(bufferedImage, ext, file);  // ignore returned boolean
            System.out.println("Saving File to: " + file.getPath());
            writePointsToXML(fileName);
        } catch(IOException e) {
            System.out.println("Write error for " + file.getPath() +
                               ": " + e.getMessage());
        }
    }
 
    private BufferedImage toBufferedImage(Image src) {
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        int type = BufferedImage.TYPE_INT_RGB;  // other options
        BufferedImage dest = new BufferedImage(w, h, type);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return dest;
    }


    private void writePointsToXML(String fileName){
   		try {
   			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				
				Element rootElement = doc.createElement(fileName);
				doc.appendChild(rootElement);

				Element label;
				Element point;
				for (int i = 0; i < labelsList.size(); i++) {
					label = doc.createElement("Label");
					label.setAttribute("Name", labelsList.get(i).getLabel());
					rootElement.appendChild(label);

					for (int j = 0; j < labelsList.get(i).getPolygon().size(); j++) {
						point = doc.createElement("Point");
						point.setAttribute("X", ((Integer)labelsList.get(i).getPolygon().get(j).getX()).toString());
						point.setAttribute("Y", ((Integer)labelsList.get(i).getPolygon().get(j).getY()).toString());
						label.appendChild(point);
					}
				}
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File("./images/" + fileName + "/" + fileName + ".xml"));
		 
				transformer.transform(source, result);
		 
				System.out.println("File saved!");
 
   		} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
	  	} catch (TransformerException tfe) {
				tfe.printStackTrace();
	  	}
    }

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
	public void setupGUI() throws Exception {
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
        
        //Add combobox
		openComboBox = new JComboBox();
		openComboBox.setSize(550,25);
		openComboBox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e){
				changeFileInList();
			}
		});
		openComboBox.setToolTipText("Switch between open files");
		
		topToolboxPanel.add(openComboBox);

		//add buttons
		JButton openButton = new JButton("+");
		openButton.setMnemonic(KeyEvent.VK_N);
		openButton.setSize(20, 20);
		openButton.setEnabled(true);
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					//TODO: SAVE OLD FILE HERE BEFORE ADDING NEW ONE
			    	launchFileChooser();
			}
		});
		openButton.setToolTipText("Click to open a new image");
		
		JButton delButton = new JButton("-");
		delButton.setMnemonic(KeyEvent.VK_N);
		delButton.setSize(20, 20);
		delButton.setEnabled(true);
		delButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					//TODO: SAVE FILE BEFORE REMOVING
			    	removeSelectedFileFromList();
			}
		});
		delButton.setToolTipText("Click to remove the current image");

		topToolboxPanel.add(openButton);
		topToolboxPanel.add(delButton);

		//add toolbox to window
		appPanel.add(topToolboxPanel);
		

		//create middle panel with image and sidebar
		middlePanel = new JPanel();

        //Create and set up the image panel.
		imagePanel = new ImagePanel();
		imagePanel.setOpaque(true); //content panes must be opaque

		middlePanel.add(imagePanel);

		rightToolboxPanel = new JPanel();
		rightToolboxPanel.setLayout(new BoxLayout(rightToolboxPanel, BoxLayout.Y_AXIS));

		//add label "labels:"
		JLabel labelsLabel = new JLabel("Labels:");

		rightToolboxPanel.add(labelsLabel);

		//add labels list
		labelsBox = new JList(labelsListModel);
		labelsBox.setSize(500,200);
		labelsPane = new JScrollPane(labelsBox);

		labelsBox.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e){
				updateSelectedLabel();
			}
		});

		rightToolboxPanel.add(labelsPane);
		
        //Add buttons
		JButton openImageButton = new JButton("Open Image");
		openImageButton.setMnemonic(KeyEvent.VK_N);
		openImageButton.setSize(50, 20);
		openImageButton.setEnabled(true);
		openImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	launchFileChooser();
			}
		});
		openImageButton.setToolTipText("Click to open a new image");

		JButton saveLabelledImageButton = new JButton("Save");
		saveLabelledImageButton.setMnemonic(KeyEvent.VK_N);
		saveLabelledImageButton.setSize(50, 20);
		saveLabelledImageButton.setEnabled(true);
		saveLabelledImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	saveLabelledImage("jpg");
			}
		});
		saveLabelledImageButton.setToolTipText("Click to save labelled image");

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

		JButton doneEditingButton = new JButton("Label Done");
		doneEditingButton.setMnemonic(KeyEvent.VK_N);
		doneEditingButton.setSize(50, 20);
		doneEditingButton.setEnabled(true);
		doneEditingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	addLabelToList();
			}
		});
		doneEditingButton.setToolTipText("Click when finished editing label");
		
		rightToolboxPanel.add(saveLabelledImageButton);
		rightToolboxPanel.add(openImageButton);
		rightToolboxPanel.add(newPolyButton);
		rightToolboxPanel.add(doneEditingButton);
		
		//add toolbox to window
		middlePanel.add(rightToolboxPanel);

        appPanel.add(middlePanel);


        //create toolbox panel
        bottomToolboxPanel = new JPanel();
        
        //Add buttons
		editLabelButton = new JButton("Edit Label");
		editLabelButton.setMnemonic(KeyEvent.VK_N);
		editLabelButton.setSize(50, 20);
		editLabelButton.setEnabled(false);
		editLabelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	editLabelText();
			}
		});
		editLabelButton.setToolTipText("Click to edit a label");

		delLabelButton = new JButton("Delete Label");
		delLabelButton.setMnemonic(KeyEvent.VK_N);
		delLabelButton.setSize(50, 20);
		delLabelButton.setEnabled(false);
		delLabelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	removeLabelFromImage();
			}
		});
		editLabelButton.setToolTipText("Click to remove a label");
		
		bottomToolboxPanel.add(editLabelButton);
		bottomToolboxPanel.add(delLabelButton);
		
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
			window.setupGUI();
		} catch (Exception e) {
			System.err.println("Image: ");
			e.printStackTrace();
		}
	}
}
