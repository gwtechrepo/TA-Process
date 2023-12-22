package com.gwtech.in.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.Run;
import com.gwtech.in.service.FileWriterI;

public class MiscUtility {
	
	private static final Logger logger = Logger.getLogger(MiscUtility.class);
	private FileWriterI fileWriterI;
	
	public String fetchNumberFromText(String paraStyle) {
		String result = "";
		try {
			
			for (int i = 0; i < paraStyle.length(); i++) {

				// access each character
				char a = paraStyle.charAt(i);
				if (isNumber(a+"")) {
					result = a+"";
					break;
				}
			}
			
		}catch(Exception exception) {	exception.printStackTrace();	}
		return result;
	}
	
	
	public String removeStartEndSpaces(String paraText) {
		
		while (paraText.endsWith(" "))
			paraText = paraText.substring(0, (paraText.length() - 1));
		while (paraText.startsWith(" "))
			paraText = paraText.substring(1);
		
		while (paraText.endsWith("	"))
			paraText = paraText.substring(0, (paraText.length() - 1));
		while (paraText.startsWith("	"))
			paraText = paraText.substring(1);
		
		return paraText;
	}
	
	
	public String removeReturnUnit(String paraText) {
		
		while (paraText.endsWith("\r"))
			paraText = paraText.substring(0, (paraText.length() - 1));
		while (paraText.startsWith("\r"))
			paraText = paraText.substring(1);
		
		while (paraText.endsWith("\f"))
			paraText = paraText.substring(0, (paraText.length() - 1));
		while (paraText.startsWith("\f"))
			paraText = paraText.substring(1);
		
		while (paraText.endsWith("\t"))
			paraText = paraText.substring(0, (paraText.length() - 1));
		while (paraText.startsWith("\t"))
			paraText = paraText.substring(1);
		
		while (paraText.endsWith("\n"))
			paraText = paraText.substring(0, (paraText.length() - 1));
		while (paraText.startsWith("\n"))
			paraText = paraText.substring(1);
		
		return paraText;
	}
	
	
	/**
	 * isNumber
	 * @param data
	 * @return
	 */
	public boolean isNumber(String data) {
		boolean valid = true;
		try {
			Integer.parseInt(data);
		}catch(Exception exception) {
			valid = false;
		}
		return valid;
	}
	
	
	/**
	 * formatParaStyle
	 * @param paraStyle
	 * @return
	 */
	public String formatParaStyle(String paraStyle) {
		
		if (paraStyle.toLowerCase().contains("heading 1"))	paraStyle = "1 hd";
		if (paraStyle.toLowerCase().contains("heading 2"))	paraStyle = "2 hd";
		if (paraStyle.toLowerCase().contains("heading 3"))	paraStyle = "3 hd";
		if (paraStyle.toLowerCase().contains("heading 4"))	paraStyle = "4 hd";
		if (paraStyle.toLowerCase().contains("heading 5"))	paraStyle = "5 hd";
		if (paraStyle.toLowerCase().contains("heading 6"))	paraStyle = "6 hd";
		if (paraStyle.toLowerCase().contains("heading 7"))	paraStyle = "7 hd";
		
		return paraStyle;
	}
	
	
	/**
	 * fetchFloatLabel
	 * @param text
	 * @return
	 */
	public String fetchFloatLabel(String text) {
		
		String result = "";
		
		try {
			
			for (int i = 0; i < text.length(); i++) {

				char a = text.charAt(i);
				if (isNumber(a+"")) {
					result = a+"";
					break;
				}
			}
			
		}catch(Exception exception) {	exception.printStackTrace();	}
		return result;
	}
	
	
	public void DeleteFileFolder(String path) {

	    File file = new File(path);
	    if(file.exists())
	    {
	        do{
	            delete(file);
	        }while(file.exists());
	    }else
	    {
	        logger.info("File or Folder not found : "+path);
	    }

	}
	private void delete(File file)
	{
	    if(file.isDirectory())
	    {
	        String fileList[] = file.list();
	        if(fileList.length == 0)
	        {
//	            logger.info("Deleting Directory : "+file.getPath());
	            file.delete();
	        }else
	        {
	            int size = fileList.length;
	            for(int i = 0 ; i < size ; i++)
	            {
	                String fileName = fileList[i];
	                // logger.info("File path : "+file.getPath()+" and name :"+fileName);
	                String fullPath = file.getPath()+"/"+fileName;
	                File fileOrFolder = new File(fullPath);
	                // logger.info("Full Path :"+fileOrFolder.getPath());
	                delete(fileOrFolder);
	            }
	        }
	    }else
	    {
//	        logger.info("Deleting file : "+file.getPath());
	        file.delete();
	    }
	}
	
	
	public Boolean createDirIfNotExist(String outputDir) {

		File file = new File(outputDir);
		if (file.exists() == false) {
			file.mkdirs();
			logger.info(outputDir + " new created.. ");
			file = null;
			return true;
		} else {
			logger.info(outputDir + " aleady exist.. ");
			file = null;
			return true;
		}
		
	}


	public String fetchItemNumber(String floatLabel) {
		String itemNum = "";
		floatLabel = removeStartEndSpaces(floatLabel);
		if (floatLabel.endsWith("."))
			floatLabel = floatLabel.substring(0, (floatLabel.lastIndexOf(".")));
		if (floatLabel.lastIndexOf(".") > 0)
			itemNum = floatLabel.substring(floatLabel.lastIndexOf(".") + 1);
		else
			itemNum = floatLabel.substring(floatLabel.lastIndexOf(" ") + 1);
		itemNum = removeStartEndSpaces(itemNum);
		return itemNum.trim();
	}

//	@1 hd:<@bold-open><@alert-red-open><1><@alert-red-close> Chest Wall Anatomy & Function[[[SOFT-BREAK-ENTRY]]] The chest wall anatomy is designed in a way to allow for dynamic movement during respiration but also to serve as a protective barrier for the lungs, heart, and major vessels from external forces. Dogs and cats generally have 13 pairs of ribs<@symbol-open><\#U003B><@symbol-close> 9 of these rib pairs connect to the sternum via the costal cartilages while the 4 sternal ribs are joined together to form the costal arch.1 The shape of the thorax differs between breeds of dogs ranging from a broad, barrel-shape in brachycephalic breeds to a more deep, slender, keel-chest of sighthounds. [[[SOFT-BREAK-ENTRY]]] The lateral parts of the ribs are covered by thin muscle bellies of the serratus ventralis, latissimus dorsi, scalenus, and obliquus abdominis externus muscles.1 The group of pectoral muscles covers the ventral thorax<@symbol-open><\#U003B><@symbol-close> the epaxial muscles cover the dorsal thoracic vertebrae. Intercostal vessels and nerves typically run on the caudomedial aspect of each rib, and effort should be made to avoid these vessels and nerves when performing a thoracocentesis. (see Chapter 198, Thoracocentesis)[[[SOFT-BREAK-ENTRY]]] The diaphragm makes up the largest and most important muscle of respiration. It is innervated by the phrenic nerve and contraction of the diaphragm during inspiration leads to expansion of the chest cavity. It is the major muscle required for inspiration<@symbol-open><\#U003B><@symbol-close> so much so that paralysis of only the external intercostal muscles does not seriously affect breathing.2 During more active respiration, such as during exercise, the accessory muscles of inspiration are engaged. These include the external intercostal muscles, the scalene, and the sternomastoids.2[[[SOFT-BREAK-ENTRY]]] Exhalation is a much more passive process. Because the lungs and chest wall have elastic properties, they tend to return to a baseline homeostatic position without muscle engagement. When more forceful expiration is required (ie exercise, coughing, vomiting), then the muscles of the abdominal wall are recruited to push the diaphragm cranially.2[[[SOFT-BREAK-ENTRY]]] Neuronal control of breathing relies on contraction of the muscles of respiration, as well as response to signals from the medullary respiratory center (see Chapter 14, Control of Breathing).15 The phrenic nerve, which divides into the left and right branches, courses through the thorax to innervate the diaphragm. The phrenic nerve arises from the phrenic nucleus in the cervical spinal cord and then courses through cervical nerves 4-7.15 The medullary respiratory centers course through reticulospinal tracts to cervical nerves 5-7 as well. It is here that they synapse with interneurons that also lead to efferent signaling via the phrenic nerve.15 [[[SOFT-BREAK-ENTRY]]]<1> Diagnosis of Chest Wall Disease[[[SOFT-BREAK-ENTRY]]] History and physical examination findings are the mainstay of diagnosing chest wall disease in veterinary patients. Animals with chest wall disease can have a variety of breathing patterns<@symbol-open><\#U003B><@symbol-close> some may breathe normally or with increased effort, while others may demonstrate abnormal patterns of respiration such as a paradoxical breathing pattern. This is manifested clinically as an inward movement of the abdomen during inspiration with concomitant decreased outward movement of the chest wall. Injury to ribs and intercostal muscles may cause exaggeration of diaphragmatic contribution, resulting in profound outward movement of the abdomen on inspiration. Flail segments will move in the opposite direction of the rest of the chest wall. Routine monitoring of carbon dioxide should be performed, especially in an animal with a suspicion of chest wall disease, as hypoventilation secondary to decreased chest wall excursions is common (see Chapter 17, Hypoventilation). [[[SOFT-BREAK-ENTRY]]] Diagnostic imaging including thoracic radiographs and thoracic computed tomography may help to identify chest wall disease including rib fractures, chest wall neoplasia, diaphragmatic tears or rupture, and congenital abnormalities.<@bold-close>
	
	
	public List<String> createNewParaForSoftbreakentry(String paraText) {
		
		String paraTextNew = paraText;
		List<String> list = new ArrayList<String>();
		boolean isValid = false;
		
		paraTextNew = paraTextNew.replace("[[[SOFT-BREAK-ENTRY]]]\n", "[[[SOFT-BREAK-ENTRY]]]");
		paraTextNew = paraTextNew.replace("[[[SOFT-BREAK-ENTRY]]][[[SOFT-BREAK-ENTRY]]]", "[[[SOFT-BREAK-ENTRY]]]");
		paraTextNew = replaceUnknownCharBox(paraTextNew);
		
		while (paraTextNew.contains("[[[SOFT-BREAK-ENTRY]]]")) {
			
			String splitLineText = paraTextNew.substring(0, (paraTextNew.indexOf("[[[SOFT-BREAK-ENTRY]]]")));
			list.add(splitLineText);
			paraTextNew = paraTextNew.substring(paraTextNew.indexOf("[[[SOFT-BREAK-ENTRY]]]") + ("[[[SOFT-BREAK-ENTRY]]]".length()));
			
			isValid = true;
		}
		
		if (isValid) {
			if (paraTextNew.length() > 0)
			list.add(paraTextNew);
		}
		
		return list;
	}			
	
	
	public String removeExtraSpaces(String paraText) {
//		Constants.spacesArray
		String line = paraText;
		for (String strObject: Constants.spacesArray) {
			
			while(line.contains(strObject+strObject))
				line = line.replace(strObject+strObject, strObject);
			
			while(line.startsWith(strObject))
				line = line.substring(1);
			
			while(line.endsWith(strObject))
				line = line.substring(0, (line.length()-1));
		}
		
		for (String strObject: Constants.spacesArray) { 
			line = line.replace(strObject, " ");
		}
		
		while(line.contains("  "))	line = line.replace("  ", " ");
		while(line.contains(" "))	line = line.replace(" ", " ");
		
//		if (rowStyleStyle.toLowerCase().startsWith("eq") == false)
//			while(line.contains("/"))	line = line.replaceAll("/", " ");
		
		return line;
	}

	
//	@SuppressWarnings("unchecked")
//	public Set<Integer> fetchPageNumberByTextSearch(String pdfFilePath, String searchText) {
//
//		com.aspose.pdf.Document pdfDocument = null;
//		Set<Integer> refsPages = new HashSet<Integer>();
//		
//		try {
//
//			pdfDocument = new com.aspose.pdf.Document(pdfFilePath);
//
//			// Create TextAbsorber object to find all instances of the input search phrase
//			TextFragmentAbsorber textFragmentAbsorber = new TextFragmentAbsorber("^(?i)" + searchText, new TextSearchOptions(true));
//
//			// Accept the absorber for all pages of document
//			pdfDocument.getPages().accept(textFragmentAbsorber);
//
//			// Get the extracted text fragments into collection
//			TextFragmentCollection textFragmentCollection = textFragmentAbsorber.getTextFragments();
//
//			// Loop through the Text fragments
//			for (TextFragment textFragment : (Iterable<TextFragment>) textFragmentCollection) {
//				// Iterate through text segments
//				Integer searchPage = textFragment.getPage().getNumber();
//				refsPages.add(searchPage);
//			}
//
//		} catch (Exception exception) {
//			exception.printStackTrace();
//		} finally {
//
//			if (pdfDocument != null)
//				pdfDocument.close();
//		}
//		return refsPages;
//	}

	
	
	public void writesPagesOfRefsToTextFile(Set<Integer> refsPages, String filePath) {
		
		StringBuilder buffer = new StringBuilder();
		Iterator<Integer> value = refsPages.iterator();
		
        while (value.hasNext()) {
        	Integer page = value.next();
            buffer.append(page + "\n");
        }
		
		fileWriterI.write(buffer.toString(), filePath, false);
	}

	
	public com.aspose.words.Document applyRefsFiltering(com.aspose.words.Document doc) throws Exception {
		
		doc.getRange().replace("@REFS-BEGINS@", "");
		return doc;
	}

	@SuppressWarnings("unchecked")
	public String fetchParagraphNormalTextFromDoc(Paragraph paragraph, String listLabelObj) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		
		NodeCollection<Node> runs = paragraph.getChildNodes(NodeType.ANY, true);
		Constants.styleTagStack = new ArrayList<String>();
		
		for (Node runNode : (Iterable<Node>) runs) {
			
			String charText = "";
			
			if (runNode != null) {
				
				if (runNode.getNodeType() == NodeType.SPECIAL_CHAR) {
					charText = runNode.getText();
					buffer.append(charText);
				}
				else if (runNode.getNodeType() == NodeType.RUN){
					
					Run run = (Run)runNode;
					{
						
						if (run != null) {
							
							charText = run.getText();
							if (charText.contains("HYPERLINK"))	continue;
							
							charText = replaceExtraText(charText);
							charText = removePreSuffixExtraSpaces(charText);
							
							if (listLabelObj.length() > 0)
								charText = listLabelObj + " " + charText;
							buffer.append(charText);
						}
					}
				}
			}
		}
		buffer.append("\n");
		String data = buffer.toString();
		return data;
	}
	
	private String removePreSuffixExtraSpaces(String charText) {

		charText = charText.replaceAll("\\R$", "");

		return charText;
	}

	
	public void setFileWriterI(FileWriterI fileWriterI) {
		this.fileWriterI = fileWriterI;
	}


	private String replaceExtraText(String charText) {
		charText = charText.replace(" ", " ").replace(" ", " ").replace("", "");
		charText = charText.replace(" ", " ").replace(" ", " ").replace("", "");
		return charText;
		}



public String replaceUnknownCharBox(String paraText) {
		
		paraText = paraText.replace("", "");
		paraText = paraText.replace("", "");
		return paraText;
	}




	
}