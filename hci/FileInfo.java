package hci;

import hci.utils.*;
import java.util.ArrayList;

public class FileInfo{
	private String lblFilePath;
	private String imageFilePath;
	private String fileName;

	public FileInfo(String lbl, String image, String file){
		lblFilePath = lbl;
		imageFilePath = image;
		fileName = file;
	}

	public FileInfo(){
		lblFilePath = null;
		imageFilePath = null;
		fileName = null;
	}

	public String getLBLFilePath(){
		return lblFilePath;
	}

	public String getImageFilePath(){
		return imageFilePath;
	}	

	public String getFileName(){
		return fileName;
	}

	public void setLBLFilePath(String lbl){
		lblFilePath = lbl;
	}

	public void setImageFilePath(String image){
		imageFilePath = image;
	}

	public void setFileName(String file){
		fileName = file;
	}
}