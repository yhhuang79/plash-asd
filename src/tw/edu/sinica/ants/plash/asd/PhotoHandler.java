package tw.edu.sinica.ants.plash.asd; 

/* import����class */
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes; 
import org.xml.sax.SAXException; 
import org.xml.sax.helpers.DefaultHandler; 

public class PhotoHandler extends DefaultHandler
{
  private int thumbnailNum=0;
  private List<String> list1;
  private List<String> list2;
  
  /* �^�ǸѪR��72���ۤ���T */
  public List<String> getSmallPhoto()
  { 
    return list1;
  }
  /* �^�ǸѪR��288���ۤ���T */
  public List<String> getBigPhoto()
  { 
    return list2;
  }

  /* XML���}�l�ѪR�ɩI�s��method */
  @Override 
  public void startDocument() throws SAXException
  { 
    list1 = new ArrayList<String>();
    list2 = new ArrayList<String>();
  } 
  /* XML��󵲧��ѪR�ɩI�s��method */
  @Override 
  public void endDocument() throws SAXException
  {
  }

  /* �ѪR��Element���}�Y�ɩI�s��method */
  @Override 
  public void startElement(String namespaceURI, String localName, 
               String qName, Attributes atts) throws SAXException
  { 
    if (localName.equals("thumbnail"))
    { 
      if(thumbnailNum==0)
      {
        /* �N�Ĥ@��url(�ѪR��72���ۤ��s��)�g�Jlist1 */
        list1.add(atts.getValue("url"));
      }
      else if(thumbnailNum==2)
      {
        /* �N�ĤT��url(�ѪR��288���ۤ��s��)�g�Jlist2 */
        list2.add(atts.getValue("url"));
      }
      thumbnailNum++;
    }
  }
  /* �ѪR��Element�������ɩI�s��method */
  @Override 
  public void endElement(String namespaceURI, String localName,
                         String qName) throws SAXException
  { 
    if (localName.equals("group"))
    { 
      /* �ѪR��group�������ɱNthumbnailNum���]��0 */
      thumbnailNum=0;
    }
  } 
}