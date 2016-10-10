package it.uniroma1.lcl.supWSD;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import it.uniroma1.lcl.supWSD.config.Config;
import it.uniroma1.lcl.supWSD.inventory.SenseInventory;
import it.uniroma1.lcl.supWSD.inventory.SenseInventoryFactory;
import it.uniroma1.lcl.supWSD.mns.MNS;
import it.uniroma1.lcl.supWSD.mns.MNSFactory;
import it.uniroma1.lcl.supWSD.modules.Tester;
import it.uniroma1.lcl.supWSD.modules.Trainer;
import it.uniroma1.lcl.supWSD.modules.classification.Serializer;
import it.uniroma1.lcl.supWSD.modules.classification.classifiers.Classifier;
import it.uniroma1.lcl.supWSD.modules.classification.classifiers.ClassifierFactory;
import it.uniroma1.lcl.supWSD.modules.parser.Parser;
import it.uniroma1.lcl.supWSD.modules.parser.ParserFactory;
import it.uniroma1.lcl.supWSD.modules.preprocessing.HybridPreprocessor;
import it.uniroma1.lcl.supWSD.modules.preprocessing.Preprocessor;
import it.uniroma1.lcl.supWSD.modules.preprocessing.StanfordPreprocessor;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.dependencyParser.DependencyParserFactory;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.dependencyParser.DependencyParserType;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.dependencyParser.DependencyParser;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.lemmatizer.Lemmatizer;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.lemmatizer.LemmatizerFactory;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.lemmatizer.LemmatizerType;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.splitter.Splitter;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.splitter.SplitterFactory;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.splitter.SplitterType;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.tagger.Tagger;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.tagger.TaggerFactory;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.tagger.TaggerType;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.tokenizer.Tokenizer;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.tokenizer.TokenizerFactory;
import it.uniroma1.lcl.supWSD.modules.preprocessing.units.tokenizer.TokenizerType;
import it.uniroma1.lcl.supWSD.modules.writer.Writer;
import it.uniroma1.lcl.supWSD.modules.writer.WriterFactory;

/**
 * @author Simone Papandrea
 *
 */
public class SupWSD {

	public static void train(String conf, String corpus, String keys) throws Exception {

		Trainer trainer;
		Config config;
		Parser parser;
		Classifier<?, ?> classifier;
		Preprocessor preprocessor;
		Map<String, SortedSet<String>> senses = null;

		senses = readSenses(keys);
		config = Config.load(conf);
		Serializer.setDirectory(config.getWorkingDir());
		Writer.setDirectory(config.getWorkingDir());
		parser = ParserFactory.getInstance().getParser(config.getParserType());
		classifier = ClassifierFactory.getInstance().getClassifier(config.getClassifierType());
		preprocessor = getPreprocessor(config.getSplitterType(), config.getTokenizerType(), config.getTaggerType(),
				config.getLemmatizerType(), config.getDParserType(), config.getSplitterModel(),
				config.getTokenizerModel(), config.getTaggerModel(), config.getLemmatizerModel(),
				config.getDParserModel());

		trainer = new Trainer(parser, preprocessor, config.getFeatureExtractors(), classifier, senses);
		trainer.execute(corpus);
	}

	public static void test(String conf, String corpus, String keys) throws Exception {

		Tester tester;
		Config config;
		Parser parser;
		MNS mns;
		Preprocessor preprocessor;
		Classifier<?, ?> classifier;
		Writer writer;
		SenseInventory senseInventory;
		Map<String, SortedSet<String>> senses = null;

		if (keys != null)
			senses = readSenses(keys);

		config = Config.load(conf);
		Serializer.setDirectory(config.getWorkingDir());
		Writer.setDirectory(config.getWorkingDir());
		parser = ParserFactory.getInstance().getParser(config.getParserType());
		mns = MNSFactory.getInstance().getMNS(config.getParserType(), config.getMNS());
		preprocessor = getPreprocessor(config.getSplitterType(), config.getTokenizerType(), config.getTaggerType(),
				config.getLemmatizerType(), config.getDParserType(), config.getSplitterModel(),
				config.getTokenizerModel(), config.getTaggerModel(), config.getLemmatizerModel(),
				config.getDParserModel());
		classifier = ClassifierFactory.getInstance().getClassifier(config.getClassifierType());
		writer = WriterFactory.getInstance().getWriter(config.getWriterType());
		senseInventory = SenseInventoryFactory.getInstance().getSenseInventory(config.getSenseInventory(),
				config.getDict());

		tester = new Tester(parser, mns, preprocessor, config.getFeatureExtractors(), classifier, writer, senses,
				senseInventory);
		tester.execute(corpus);
	}

	private static Preprocessor getPreprocessor(SplitterType splitterType, TokenizerType tokenizerType,
			TaggerType taggerType, LemmatizerType lemmatizerType, DependencyParserType dependencyParserType,
			String splitterModel, String tokenizerModel, String taggerModel, String lemmatizerModel,
			String dParserModel) throws IOException {

		Preprocessor preprocessor;
		Splitter splitter;
		Tokenizer tokenizer;
		Tagger tagger;
		Lemmatizer lemmatizer;
		DependencyParser dependencyParser;
		boolean split, pos, lemma, depparse;

		split = splitterType != null;
		pos = taggerType != null;
		lemma = lemmatizerType != null;
		depparse = dependencyParserType != null;

		if ((tokenizerType != null && tokenizerType.equals(TokenizerType.STANFORD) && tokenizerModel == null)
				&& (!split || (splitterType.equals(SplitterType.STANFORD) && splitterModel == null))
				&& (!pos || (split && taggerType.equals(TaggerType.STANFORD) && taggerModel == null))
				&& (!lemma || (pos && lemmatizerType.equals(LemmatizerType.STANFORD) && lemmatizerModel == null))
				&& (!depparse
						|| (pos && dependencyParserType.equals(DependencyParserType.STANFORD) && dParserModel == null)))

			preprocessor = new StanfordPreprocessor(split, pos, lemma, depparse);

		else {

			splitter = SplitterFactory.getInstance().getSplitter(splitterType, splitterModel);
			tokenizer = TokenizerFactory.getInstance().getTokenizer(tokenizerType, tokenizerModel);
			tagger = TaggerFactory.getInstance().getTagger(taggerType, taggerModel);
			lemmatizer = LemmatizerFactory.getInstance().getLemmatizer(lemmatizerType, lemmatizerModel);
			dependencyParser = DependencyParserFactory.getInstance().getDependecyParser(dependencyParserType,
					dParserModel);
			preprocessor = new HybridPreprocessor(splitter, tokenizer, tagger, lemmatizer, dependencyParser);
		}

		return preprocessor;
	}

	private static Map<String, SortedSet<String>> readSenses(String keysFile) throws IOException {

		Map<String, SortedSet<String>> keys;
		BufferedReader keyReader = null;
		final String regex = "\\s|\\t|\\n|\\r|\\f";
		String line, sense, tokens[];
		SortedSet<String> senses;

		keys = new HashMap<String, SortedSet<String>>();

		try {

			keyReader = new BufferedReader(new InputStreamReader(new FileInputStream(keysFile)));

			while ((line = keyReader.readLine()) != null) {

				tokens = line.split(regex);
				senses = new TreeSet<String>();

				for (int i = 2; i < tokens.length; i++) {

					sense = tokens[i];

					if (sense.equals("!!"))
						break;

					senses.add(sense);
				}

				keys.put(tokens[1], senses);
			}

		} finally {

			if (keyReader != null)
				try {
					keyReader.close();
				} catch (IOException e) {

				}
		}

		return keys;
	}
}