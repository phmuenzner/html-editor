package onlinehilfe.contentbuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.navigator.OnlinehilfeNavigatorContentProvider;

public class Html2Pdf {
	
	private final File htmlRoot;
	private final File transformationXsl;
	private final Properties properties;
	
	Map<String, String> htmlCharacterSequenceStringSubstitutorMap = new HashMap();
		
	public Html2Pdf(File htmlRoot, File transformationXsl, Properties properties) {
		this.htmlRoot = htmlRoot;
		this.transformationXsl = transformationXsl;
		this.properties = properties;
		
		
		
		//EscapeCharMapping
		htmlCharacterSequenceStringSubstitutorMap.put("&tilde;", "&#126;");
		htmlCharacterSequenceStringSubstitutorMap.put("&florin;", "&#131;");
		htmlCharacterSequenceStringSubstitutorMap.put("&elip;", "&#133;");
		htmlCharacterSequenceStringSubstitutorMap.put("&dag;", "&#134;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ddag;", "&#135;");
		htmlCharacterSequenceStringSubstitutorMap.put("&cflex;", "&#136;");
		htmlCharacterSequenceStringSubstitutorMap.put("&permil;", "&#137;");
		htmlCharacterSequenceStringSubstitutorMap.put("&uscore;", "&#138;");
		htmlCharacterSequenceStringSubstitutorMap.put("&OElig;", "&#140;");
		htmlCharacterSequenceStringSubstitutorMap.put("&lsquo;", "&#145;");
		htmlCharacterSequenceStringSubstitutorMap.put("&rsquo;", "&#146;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ldquo;", "&#147;");
		htmlCharacterSequenceStringSubstitutorMap.put("&rdquo;", "&#148;");
		htmlCharacterSequenceStringSubstitutorMap.put("&bullet;", "&#149;");
		htmlCharacterSequenceStringSubstitutorMap.put("&endash;", "&#150;");
		htmlCharacterSequenceStringSubstitutorMap.put("&emdash;", "&#151;");
		htmlCharacterSequenceStringSubstitutorMap.put("&trade;", "&#153;");
		htmlCharacterSequenceStringSubstitutorMap.put("&oelig;", "&#156;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Yuml;", "&#159;");
		htmlCharacterSequenceStringSubstitutorMap.put("&nbsp;", "&#160;");
		htmlCharacterSequenceStringSubstitutorMap.put("&iexcl;", "&#161;");
		htmlCharacterSequenceStringSubstitutorMap.put("&cent;", "&#162;");
		htmlCharacterSequenceStringSubstitutorMap.put("&pound;", "&#163;");
		htmlCharacterSequenceStringSubstitutorMap.put("&curren;", "&#164;");
		htmlCharacterSequenceStringSubstitutorMap.put("&yen;", "&#165;");
		htmlCharacterSequenceStringSubstitutorMap.put("&brvbar;", "&#166;");
		htmlCharacterSequenceStringSubstitutorMap.put("&sect;", "&#167;");
		htmlCharacterSequenceStringSubstitutorMap.put("&uml;", "&#168;");
		htmlCharacterSequenceStringSubstitutorMap.put("&copy;", "&#169;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ordf;", "&#170;");
		htmlCharacterSequenceStringSubstitutorMap.put("&laquo;", "&#171;");
		htmlCharacterSequenceStringSubstitutorMap.put("&not;", "&#172;");
		htmlCharacterSequenceStringSubstitutorMap.put("&shy;", "&#173;");
		htmlCharacterSequenceStringSubstitutorMap.put("&reg;", "&#174;");
		htmlCharacterSequenceStringSubstitutorMap.put("&macr;", "&#175;");
		htmlCharacterSequenceStringSubstitutorMap.put("&deg;", "&#176;");
		htmlCharacterSequenceStringSubstitutorMap.put("&plusmn;", "&#177;");
		htmlCharacterSequenceStringSubstitutorMap.put("&sup2;", "&#178;");
		htmlCharacterSequenceStringSubstitutorMap.put("&sup3;", "&#179;");
		htmlCharacterSequenceStringSubstitutorMap.put("&acute;", "&#180;");
		htmlCharacterSequenceStringSubstitutorMap.put("&micro;", "&#181;");
		htmlCharacterSequenceStringSubstitutorMap.put("&para;", "&#182;");
		htmlCharacterSequenceStringSubstitutorMap.put("&middot;", "&#183;");
		htmlCharacterSequenceStringSubstitutorMap.put("&cedil;", "&#184;");
		htmlCharacterSequenceStringSubstitutorMap.put("&sup1;", "&#185;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ordm;", "&#186;");
		htmlCharacterSequenceStringSubstitutorMap.put("&raquo;", "&#187;");
		htmlCharacterSequenceStringSubstitutorMap.put("&frac14;", "&#188;");
		htmlCharacterSequenceStringSubstitutorMap.put("&frac12;", "&#189;");
		htmlCharacterSequenceStringSubstitutorMap.put("&frac34;", "&#190;");
		htmlCharacterSequenceStringSubstitutorMap.put("&iquest;", "&#191;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Agrave;", "&#192;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Aacute;", "&#193;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Acirc;", "&#194;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Atilde;", "&#195;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Auml;", "&#196;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Aring;", "&#197;");
		htmlCharacterSequenceStringSubstitutorMap.put("&AElig;", "&#198;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ccedil;", "&#199;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Egrave;", "&#200;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Eacute;", "&#201;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ecirc;", "&#202;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Euml;", "&#203;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Igrave;", "&#204;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Iacute;", "&#205;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Icirc;", "&#206;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Iuml;", "&#207;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ETH;", "&#208;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ntilde;", "&#209;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ograve;", "&#210;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Oacute;", "&#211;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ocirc;", "&#212;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Otilde;", "&#213;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ouml;", "&#214;");
		htmlCharacterSequenceStringSubstitutorMap.put("&times;", "&#215;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Oslash;", "&#216;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ugrave;", "&#217;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Uacute;", "&#218;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ucirc;", "&#219;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Uuml;", "&#220;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Yacute;", "&#221;");
		htmlCharacterSequenceStringSubstitutorMap.put("&THORN;", "&#222;");
		htmlCharacterSequenceStringSubstitutorMap.put("&szlig;", "&#223;");
		htmlCharacterSequenceStringSubstitutorMap.put("&agrave;", "&#224;");
		htmlCharacterSequenceStringSubstitutorMap.put("&aacute;", "&#225;");
		htmlCharacterSequenceStringSubstitutorMap.put("&acirc;", "&#226;");
		htmlCharacterSequenceStringSubstitutorMap.put("&atilde;", "&#227;");
		htmlCharacterSequenceStringSubstitutorMap.put("&auml;", "&#228;");
		htmlCharacterSequenceStringSubstitutorMap.put("&aring;", "&#229;");
		htmlCharacterSequenceStringSubstitutorMap.put("&aelig;", "&#230;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ccedil;", "&#231;");
		htmlCharacterSequenceStringSubstitutorMap.put("&egrave;", "&#232;");
		htmlCharacterSequenceStringSubstitutorMap.put("&eacute;", "&#233;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ecirc;", "&#234;");
		htmlCharacterSequenceStringSubstitutorMap.put("&euml;", "&#235;");
		htmlCharacterSequenceStringSubstitutorMap.put("&igrave;", "&#236;");
		htmlCharacterSequenceStringSubstitutorMap.put("&iacute;", "&#237;");
		htmlCharacterSequenceStringSubstitutorMap.put("&icirc;", "&#238;");
		htmlCharacterSequenceStringSubstitutorMap.put("&iuml;", "&#239;");
		htmlCharacterSequenceStringSubstitutorMap.put("&eth;", "&#240;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ntilde;", "&#241;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ograve;", "&#242;");
		htmlCharacterSequenceStringSubstitutorMap.put("&oacute;", "&#243;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ocirc;", "&#244;");
		htmlCharacterSequenceStringSubstitutorMap.put("&otilde;", "&#245;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ouml;", "&#246;");
		htmlCharacterSequenceStringSubstitutorMap.put("&oslash;", "&#248;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ugrave;", "&#249;");
		htmlCharacterSequenceStringSubstitutorMap.put("&uacute;", "&#250;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ucirc;", "&#251;");
		htmlCharacterSequenceStringSubstitutorMap.put("&uuml;", "&#252;");
		htmlCharacterSequenceStringSubstitutorMap.put("&yacute;", "&#253;");
		htmlCharacterSequenceStringSubstitutorMap.put("&thorn;", "&#254;");
		htmlCharacterSequenceStringSubstitutorMap.put("&yuml;", "&#255;");
		htmlCharacterSequenceStringSubstitutorMap.put("&euro;", "&#x20AC;");

	}
	
	public void generatePdf(File contentHtml, OutputStream outputStream) throws IOException, FOPException, TransformerConfigurationException, TransformerException {
		
		FopFactory fopFactory = FopFactory.newInstance(htmlRoot.toURI());
		
		//Tranformiere zu XHTML
		org.jsoup.nodes.Document jsoupDocument = Jsoup.parse(contentHtml, FilesUtil.CHARSET_STRING);
		jsoupDocument.outputSettings().syntax(Syntax.xml);
		String output = jsoupDocument.outerHtml();
		output = substituteInStringBySubstitutorMap(output);
		
		InputStream contentXhtml = new ByteArrayInputStream(output.getBytes(FilesUtil.CHARSET));
		
		StreamSource contentXhtmlSource = new StreamSource(contentXhtml);
		StreamSource xslSource = new StreamSource(new FileInputStream(transformationXsl));		
		
		// Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outputStream);
		
		// Setup XSLT
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer(xslSource);
		
		// Resulting SAX events (the generated FO) must be piped through to FOP
		Result res = new SAXResult(fop.getDefaultHandler());

		//sorgt für lesbare Fehlermeldugen
		transformer.setErrorListener(new ErrorListener() {				
			@Override
			public void warning(TransformerException arg0) throws TransformerException {
				arg0.printStackTrace();
			}
			@Override
			public void fatalError(TransformerException arg0) throws TransformerException {
				arg0.printStackTrace();
			}
			@Override
			public void error(TransformerException arg0) throws TransformerException {
				arg0.printStackTrace();
			}
		});
		
		// Start XSLT transformation and FOP processing
		// That's where the XML is first transformed to XSL-FO and then
		// PDF is created
		transformer.transform(contentXhtmlSource, res);
	}
	
	private String substituteInStringBySubstitutorMap(final String text) {
		String internalText = text;
		for (Map.Entry<String, String> substitiontionEntry: htmlCharacterSequenceStringSubstitutorMap.entrySet()) {
			internalText = internalText.replace(substitiontionEntry.getKey(), substitiontionEntry.getValue());
		}
		return internalText;
	}
}
