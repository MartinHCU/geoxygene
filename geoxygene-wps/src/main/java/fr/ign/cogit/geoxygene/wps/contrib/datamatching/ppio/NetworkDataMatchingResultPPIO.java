/**
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL_V2-fr.txt see Licence_CeCILL_V2-en.txt
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info</a>
 * 
 * 
 * @copyright IGN
 */
package fr.ign.cogit.geoxygene.wps.contrib.datamatching.ppio;

import java.io.CharArrayWriter;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import com.thoughtworks.xstream.XStream;
import org.xml.sax.ContentHandler;
import org.geoserver.wps.ppio.XStreamPPIO;

import com.thoughtworks.xstream.io.xml.SaxWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;
// import com.thoughtworks.xstream.mapper.MapperWrapper;

import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.data.NetworkElementInterface;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.data.ResultNetwork;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.data.ResultNetworkElement;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.data.ResultatAppariement;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.xml.ResultatAppariementParser;

/**
 * A PPIO to generate good looking xml for the network data mathing process
 * results. -
 * 
 * @version 1.6
 */
public class NetworkDataMatchingResultPPIO extends XStreamPPIO {

  /** LOGGER. */
  private final static Logger LOGGER = Logger.getLogger(NetworkDataMatchingResultPPIO.class.getName());

  /**
   * Default constructor.
   */
  protected NetworkDataMatchingResultPPIO() {
    super(ResultatAppariement.class);
  }

  @Override
  public void encode(Object obj, ContentHandler handler) throws Exception {
    
    LOGGER.info("------------------------------------------------------------------------");
    LOGGER.info("Start encoding the result for output.");
    
    // Prepare xml encoding
    /*XStream xstream = new XStream();
    xstream.alias("ResultNetwork", ResultNetwork.class);
    
    // Write out xml
    SaxWriter writer = new SaxWriter();
    writer.setContentHandler(handler);
    xstream.marshal(obj, writer);*/
    
    // Get XML format for resultatAppariement
    ResultatAppariementParser resultatAppariementParser = new ResultatAppariementParser();
    String result = resultatAppariementParser.generateXMLResponse(((ResultatAppariement)obj));
    
    // Write out xml
    SaxWriter writer = new SaxWriter();
    writer.setContentHandler(handler);
    XStream xstream = new XStream();
    xstream.marshal(result, writer);
    
    /*JAXBContext context = JAXBContext.newInstance(ResultNetwork.class);
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    m.marshal(obj, writer);*/
    
    /*JAXBContext jc = JAXBContext.newInstance(ResultNetwork.class);
    Marshaller marshaller = jc.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
    OutputStreamWriter writer = new OutputStreamWriter(System.out, "ISO-8859-1");
    marshaller.marshal(obj, writer);*/
    
    LOGGER.info("End encoding the result for output.");
    LOGGER.info("------------------------------------------------------------------------");
  
  }

}
