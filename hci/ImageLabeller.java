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
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
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
  private ArrayList<ArrayList<Point>> polygonsList;
  private FileInfo fileInfo =  null;
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
	JPanel rightToolboxPanel = null;
	
	/**
	 * image panel - displays image and editing area
	 */
	ImagePanel imagePanel;

	/**
	 * middle panel - holds image and middletoolbox panels
	 */
	JPanel mainPanel = null;

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
	int [] prevLabelIndices = null;

	/**
	 * curpolysave - keep current polygon to save for label array
	 */
	ArrayList<Point> curPolygonSave;

	/**
	 * labelcounter - counts number of labels for default label name display
	 */
	private static int labelCounter = 0;

	/**
	 * error handling popup vars
	 */
	JDialog errorPopup = null;
	JPanel popupMenuPanel = null;
	JLabel errorLabel = null;

	//need to draw in paint because don't show up otherwise
	JButton	newProjectButton,
		openProjectButton,
		saveNewLabelledImageButton;
	
	/**
	 * Launches file choose to retrieve an image path
	*/
	public void launchFileChooser(String openAction){
		String filePath = "";
		String fileExt = "";
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter;
		
		if (openAction.equals("NEW")) {
			labelsBox.removeAll();
			labelsList.clear();
			labelCounter = 0;
			((DefaultListModel)(labelsBox.getModel())).clear();
			imagePanel.getPolygonsList().clear();
			imagePanel.drawAllPolygons();
			fileInfo = null;
			//Sets File Filter that is allowed to be opened
			filter = new FileNameExtensionFilter("JPG & GIF Images", "jpg", "gif");	
    	chooser.setFileFilter(filter);
		}
		else{
			// Opening existing project
			labelsBox.removeAll();
			labelsList.clear();
			labelCounter = 0;
			fileInfo = null;
			((DefaultListModel)(labelsBox.getModel())).clear();
			filter = new FileNameExtensionFilter("Label Files", "lbl");
			chooser.setFileFilter(filter);
			chooser.setCurrentDirectory(new File("./projects/"));
		}

    int returnVal = chooser.showOpenDialog(this);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
        filePath = chooser.getSelectedFile().getAbsolutePath().toString();
    }
    
    //Determines which follow up function to call
		fileExt = filePath.substring((filePath.lastIndexOf(".")+1), filePath.length());
		System.out.println("File Type Chosen: " + fileExt);
		if(fileExt.equals("lbl")){
			openLBLFile(filePath);
		}
		else{
			openImage(filePath);
		}
	}

	public void openImage(String imagePath){
		imagePanel.setImage(imagePath);

    // Setting Buffer Image variables to perform Save later
    try {
          bufferedImageSrc = ImageIO.read(new File(imagePath));
    			bufferedImage = toBufferedImage(bufferedImageSrc);
        } catch(IOException e) {
            System.out.println("Error Buffering Image");
        }
	}

	private void addLabelToList(){

		//check to see if polygon is completable
		if(imagePanel.getCurrentPolygon().size() < 3){
			errorLabel.setText("Error: label has no shape.");
			errorPopup.setLocationRelativeTo(this);
			errorPopup.setVisible(true);
			return;
		}

		//save the current polygon
		curPolygonSave = new ArrayList<Point>();
		for(int i = 0; i < imagePanel.getCurrentPolygon().size(); i++){
			curPolygonSave.add(imagePanel.getCurrentPolygon().get(i));
		}

		//ask for label name
		imagePanel.addNewPolygon();
		String str = JOptionPane.showInputDialog(null, "Enter label name : ",
													"label" + labelCounter, 1);
  		if(str == null || str.equals("")){
  			str = "label" + labelCounter;
  		}
  		labelCounter++;

  		//add label to list of labels
  		labelsListModel.addElement(str);

  		//create label - polygon pair here
  		labelsList.add(new Label(curPolygonSave, str));
  		labelsBox.setSelectedIndex(labelsBox.getModel().getSize() - 1);

  		//enable label buttons
  		if(!editLabelButton.isEnabled()){
  			editLabelButton.setEnabled(true);
  			delLabelButton.setEnabled(true);
  		}
	}

	public void updateSelectedLabel(){
		int i;

		//if nothing is selected, then return
		if(labelsBox.isSelectionEmpty()){
			return;
		}

		//turn previous label(s) green
		if(prevLabelIndices != null){
			for(int prevLabelIdx : prevLabelIndices){
				if(prevLabelIdx != -1 && prevLabelIdx < labelsBox.getModel().getSize()){
					imagePanel.drawPolygon(labelsList.get(prevLabelIdx).getPolygon(), Color.GREEN, true);
				}
			}
		}

		//get all selected labels
		int [] selectedLabels = labelsBox.getSelectedIndices();

		//make new label(s) and turn it(them) red
		Label curLabel;
		for(i = 0; i < selectedLabels.length; i++){
			curLabel = labelsList.get(selectedLabels[i]);
			imagePanel.drawPolygon(curLabel.getPolygon(), Color.RED, true);
		}

		//update previous indices for turning green later
		prevLabelIndices = new int[selectedLabels.length];
		for(i = 0; i < selectedLabels.length; i++){
			prevLabelIndices[i] = selectedLabels[i];
		}

		//a label is selected, so the label buttons are enabled
		//if multiple labels are selected, they cannot be edited at once, only deleted
  		delLabelButton.setEnabled(true);
  		if(selectedLabels.length == 1){
  			editLabelButton.setEnabled(true);
  		}
  		else{
  			editLabelButton.setEnabled(false);
  		}
	}

	public void editLabelText(){

		//bring up prompt to edit label text
		String str = JOptionPane.showInputDialog(null, "Enter label name : ",
													"label" + labelCounter, 1);
  		if(str == null || str.equals("")){
  			str = "label" + labelCounter;
  			labelCounter++;
  		}

  		//update label name in stored labels
  		((DefaultListModel)(labelsBox.getModel())).setElementAt(str, labelsBox.getSelectedIndex());
  		labelsList.get(labelsBox.getSelectedIndex()).setLabel(str);
	}

	public void removeLabelFromImage(){
		int i;

		//remove the label from the lists
		int [] selectedLabels = labelsBox.getSelectedIndices();
		labelsBox.clearSelection();
		for(i = selectedLabels.length - 1; i >= 0; i--){
			labelsList.remove(selectedLabels[i]);
			((DefaultListModel)(labelsBox.getModel())).removeElementAt(selectedLabels[i]);
		}

		//reset image to be clear of labels
		imagePanel.clearLabels();

		//reset polygons list in imagePanel to remove the polygon
		//corresponding to the label to remove
		imagePanel.getPolygonsList().clear();
		ArrayList<Point> temp;
		for(i = 0; i < labelsList.size(); i++){
			temp = new ArrayList<Point>();

			for(int j = 0; j < labelsList.get(i).getPolygon().size(); j++){
				temp.add(labelsList.get(i).getPolygon().get(j));
			}

			imagePanel.getPolygonsList().add(temp);
		}

		//redraw all polygons without label
		imagePanel.drawAllPolygons();

		//nothing is selected, so disable label buttons
		editLabelButton.setEnabled(false);
  		delLabelButton.setEnabled(false);
	}

	/**
	 * Saving functionality
	 */
	private void saveNewLabelledImage(String ext) {
		String fileName = JOptionPane.showInputDialog(null, "Enter File Name: ", "", 1);
		if(fileName == null || fileName.equals("")){
			return;
		}
		File projectDir = new File("./projects");
		File imageDir = new File("./projects/images");
		String filePath = "./projects/images/";

		//Creates a new directory to store Projects lbl files and new directory for corresponding Images
		if(!projectDir.exists()){
			projectDir.mkdirs();
			if (!imageDir.exists()) {
				imageDir.mkdirs();	
			}
		}
        File file = new File(filePath + fileName + "." + ext);
        try {
            ImageIO.write(bufferedImage, ext, file);  // ignore returned boolean
            System.out.println("Saving File to: " + file.getPath());
            writePointsToXML(fileName, file.getPath());
        } catch(IOException e) {
            System.out.println("Write error for " + file.getPath() +
                               ": " + e.getMessage());
        }
    }
 		
    private void saveLabelledImage() {
        overwritePointsToXML();
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

    private void writePointsToXML(String fileName, String imageFilePath){
   		try {
   			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				String lblFilePath = ("./projects/"+ fileName + ".lbl");
				Element rootElement = doc.createElement(fileName);

				fileInfo = new FileInfo(lblFilePath, imageFilePath, fileName); // Tracks information for saving existing file

				rootElement.setAttribute("ImagePath", imageFilePath);
				rootElement.setAttribute("LBLPath", lblFilePath);
				rootElement.setAttribute("FileName", fileName);
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
				StreamResult result = new StreamResult(new File(lblFilePath));
		 
				transformer.transform(source, result);
		 
				System.out.println("File saved!");
 
   		} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
	  	} catch (TransformerException tfe) {
				tfe.printStackTrace();
	  	}
    }

    private void overwritePointsToXML(){
    	try {
   			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement(fileInfo.getFileName());

				rootElement.setAttribute("ImagePath", fileInfo.getImageFilePath());
				rootElement.setAttribute("LBLPath", fileInfo.getLBLFilePath());
				rootElement.setAttribute("FileName", fileInfo.getFileName());
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
				StreamResult result = new StreamResult(new File(fileInfo.getLBLFilePath()));
		 
				transformer.transform(source, result);
		 
				System.out.println("File saved!");
 
   		} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
	  	} catch (TransformerException tfe) {
				tfe.printStackTrace();
	  	}
    }

   /**
	 * Handles Opening of Files
	 */
    public void openLBLFile(String lblFilePath){

			try {
				ArrayList<Label> extractedLabelsList = new ArrayList<Label>();
				ArrayList<Point> tmpPolygon;
				Label tmpLabel;
				Point tmpPoint;
				String imageFilePath = "";
				String tmpLabelName = "";
				String fileName = "";
				Element labelElement = null;
				Element pointElement = null;
				Node labelNode = null;
				Node pointNode = null;
				NodeList labelNodeList = null; 
				NodeList pointNodeList = null;

				File fXmlFile = new File(lblFilePath);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				
				doc.getDocumentElement().normalize();
		 		
		 		//Opening associated Image to the LBL file
		 		imageFilePath = doc.getDocumentElement().getAttribute("ImagePath");
		 		openImage(imageFilePath);

		 		fileName = doc.getDocumentElement().getAttribute("FileName");

		 		fileInfo = new FileInfo(lblFilePath, imageFilePath, fileName);

				labelNodeList = doc.getElementsByTagName("Label");
		 		polygonsList = new ArrayList<ArrayList<Point>>();
				for (int i = 0; i < labelNodeList.getLength(); i++) {
				 labelNode = labelNodeList.item(i);
			   tmpPolygon = new ArrayList<Point>();
			   if (labelNode.getNodeType() == Node.ELEMENT_NODE) {
			      labelElement = (Element) labelNode;
			      tmpLabelName = labelElement.getAttribute("Name");

			      System.out.println("Label: " + tmpLabelName);

			      ((DefaultListModel)(labelsBox.getModel())).addElement((Object)tmpLabelName);
			      pointNodeList = labelElement.getChildNodes();
			      
	 					for (int j = 0; j < pointNodeList.getLength(); j++) {
	 						pointNode = pointNodeList.item(j);
	 						pointElement = (Element) pointNode;
	 						if (pointNode.getNodeType() == Node.ELEMENT_NODE) {
	 							tmpPoint = new Point(Integer.parseInt(pointElement.getAttribute("X")), Integer.parseInt(pointElement.getAttribute("Y")));
	 							tmpPolygon.add(tmpPoint);


	 							System.out.println("X: " + pointElement.getAttribute("X"));
	 							System.out.println("Y: " + pointElement.getAttribute("Y"));
	 						}
	 						polygonsList.add(tmpPolygon);	
	 					}   
			   }
			   tmpLabel = new Label(tmpPolygon, tmpLabelName);
			   extractedLabelsList.add(tmpLabel);
			}
			labelsList = extractedLabelsList;
			imagePanel.setPolygonsList(polygonsList);
			imagePanel.drawAllPolygons();
	  } catch (Exception e) {
			e.printStackTrace();
	  }
	}

	/**
	 * handles Cancel Label button action
	 */
	public void cancelLabel() {

		//redraw polygons without partial label
		Label curLabel = null;
		boolean currentExists = false;
		if(!labelsBox.isSelectionEmpty()){
			curLabel = labelsList.get(labelsBox.getSelectedIndex());
			currentExists = true;
		}
		imagePanel.clearLabels();
		imagePanel.setCurrentPolygon(new ArrayList<Point>());
		imagePanel.drawAllPolygons();

		//redraw selected label in red
		if(currentExists){
			imagePanel.setCurrentPolygon(curLabel.getPolygon());
		}
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

		//initialize error popup
		errorPopup = new JDialog(this);
		popupMenuPanel = new JPanel();
		errorPopup.setSize(200, 100);
		popupMenuPanel.setLayout(new BoxLayout(popupMenuPanel, BoxLayout.Y_AXIS));
		errorPopup.add(popupMenuPanel);

		errorLabel = new JLabel("Error");

		JButton popupCancelButton = new JButton("Cancel");
		popupCancelButton.setMnemonic(KeyEvent.VK_N);
		popupCancelButton.setSize(20, 20);
		popupCancelButton.setEnabled(true);
		popupCancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	errorPopup.setVisible(false);
			}
		});

		JButton popupCancelCurLabelButton = new JButton("Cancel Label");
		popupCancelCurLabelButton.setMnemonic(KeyEvent.VK_N);
		popupCancelCurLabelButton.setSize(20, 20);
		popupCancelCurLabelButton.setEnabled(true);
		popupCancelCurLabelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					cancelLabel();
					errorPopup.setVisible(false);
			}
		});

		popupMenuPanel.add(errorLabel);
		popupMenuPanel.add(popupCancelButton);
		popupMenuPanel.add(popupCancelCurLabelButton);

		//setup main window panel
		appPanel = new JPanel();
		appPanel.setLayout(new BoxLayout(appPanel, BoxLayout.Y_AXIS));
		this.setLayout(new BoxLayout(appPanel, BoxLayout.Y_AXIS));
		this.setContentPane(appPanel);

	    //create toolbox panel
	    topToolboxPanel = new JPanel();
	    topToolboxPanel.setLayout(new BoxLayout(topToolboxPanel, BoxLayout.X_AXIS));
	    topToolboxPanel.setOpaque(true);
	    
        //Add buttons
		newProjectButton = new JButton("New Project");
		newProjectButton.setMnemonic(KeyEvent.VK_N);
		newProjectButton.setSize(50, 20);
		newProjectButton.setEnabled(true);
		newProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	launchFileChooser("NEW");
			}
		});
		newProjectButton.setToolTipText("Click to select an image for a new project");

		openProjectButton = new JButton("Open Project");
		openProjectButton.setMnemonic(KeyEvent.VK_N);
		openProjectButton.setSize(50, 20);
		openProjectButton.setEnabled(true);
		openProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	launchFileChooser("EXISTING");
			}
		});
		openProjectButton.setToolTipText("Click to open an existing project");

		saveNewLabelledImageButton = new JButton("Save");
		saveNewLabelledImageButton.setMnemonic(KeyEvent.VK_N);
		saveNewLabelledImageButton.setSize(50, 20);
		saveNewLabelledImageButton.setPreferredSize(new Dimension(100, 30));
		saveNewLabelledImageButton.setEnabled(true);
		saveNewLabelledImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
						if (fileInfo != null) {
							saveLabelledImage();
						}
			    	else{
			    		saveNewLabelledImage("jpg");	
			    	}
			}
		});
		saveNewLabelledImageButton.setToolTipText("Click to save labelled image");

		topToolboxPanel.add(newProjectButton);
		topToolboxPanel.add(openProjectButton);
		topToolboxPanel.add(saveNewLabelledImageButton);
		topToolboxPanel.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
		
		//add toolbox to window
		appPanel.add(topToolboxPanel);


		//create middle panel with image and sidebar
		mainPanel = new JPanel();

        //Create and set up the image panel.
		imagePanel = new ImagePanel("./images/default_image.png");
		imagePanel.setOpaque(false); //content panes must be opaque

		mainPanel.add(imagePanel);

		rightToolboxPanel = new JPanel();
		rightToolboxPanel.setLayout(new BoxLayout(rightToolboxPanel, BoxLayout.Y_AXIS));

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

		JButton newPolyButton = new JButton("Cancel Label");
		newPolyButton.setMnemonic(KeyEvent.VK_N);
		newPolyButton.setSize(50, 20);
		newPolyButton.setEnabled(true);
		newPolyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    	cancelLabel();
			}
		});
		newPolyButton.setToolTipText("Click to stop current label");

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
		
		JPanel labelManagePanel = new JPanel();
		labelManagePanel.setLayout(new BoxLayout(labelManagePanel, BoxLayout.Y_AXIS));
		labelManagePanel.add(new JLabel("Labels:"));
		labelManagePanel.add(labelsPane);
		labelManagePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		JPanel editDeletePanel = new JPanel();
		editDeletePanel.setLayout(new BoxLayout(editDeletePanel, BoxLayout.X_AXIS));
		editDeletePanel.add(editLabelButton);
		editDeletePanel.add(delLabelButton);
		labelManagePanel.add(editDeletePanel);
		labelManagePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		JPanel cancelCreatePanel = new JPanel();
		cancelCreatePanel.setLayout(new BoxLayout(cancelCreatePanel, BoxLayout.X_AXIS));
		labelManagePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		cancelCreatePanel.add(doneEditingButton);
		cancelCreatePanel.add(newPolyButton);

		rightToolboxPanel.add(labelManagePanel);
		rightToolboxPanel.add(cancelCreatePanel);
		
		//add toolbox to window
		mainPanel.add(rightToolboxPanel);
		
		appPanel.add(topToolboxPanel, BorderLayout.NORTH);
    	appPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
    	appPanel.add(mainPanel, BorderLayout.SOUTH);
    	
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
