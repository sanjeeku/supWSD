package it.si3p.supwsd.modules.parser;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import it.si3p.supwsd.modules.parser.xml.lexical.LexicalHandler;
import it.si3p.supwsd.modules.parser.xml.semeval13.SemEval13Handler;
import it.si3p.supwsd.modules.parser.xml.semeval13.SemEval13HandlerFast;
import it.si3p.supwsd.modules.parser.xml.semeval15.SemEval15Handler;
import it.si3p.supwsd.modules.parser.xml.semeval15.SemEval15HandlerFast;
import it.si3p.supwsd.modules.parser.xml.semeval7.SemEval7Handler;
import it.si3p.supwsd.modules.parser.xml.semeval7.SemEval7HandlerFast;
import it.si3p.supwsd.modules.parser.xml.senseval.SensEvalHandler;
import it.si3p.supwsd.modules.parser.xml.senseval.SensEvalHandlerFast;

/**
 * @author papandrea
 *
 */
public class ParserFactory {

	private static ParserFactory instance;

	private ParserFactory() {

	}

	public static ParserFactory getInstance() {

		if (instance == null)
			instance = new ParserFactory();

		return instance;
	}

	public Parser getParser(ParserType parserType,boolean fast) throws ParserConfigurationException, SAXException {

		Parser parser = null;

		switch (parserType) {

		case SENSEVAL:

			parser = new XMLParser(fast?new SensEvalHandlerFast():new SensEvalHandler());
			break;

		case SEMEVAL7:

			parser = new XMLParser(fast?new SemEval7HandlerFast():new SemEval7Handler());
			break;
			
		case SEMEVAL13:

			parser = new XMLParser(fast?new SemEval13HandlerFast():new SemEval13Handler());
			break;

		case SEMEVAL15:

			parser = new XMLParser(fast?new SemEval15HandlerFast():new SemEval15Handler());
			break;

		case LEXICAL:
			parser = new XMLParser(new LexicalHandler());
			break;
			
		case PLAIN:
			
			parser=new PlainParser();
			break;
		}
		
		return parser;
	}
}
