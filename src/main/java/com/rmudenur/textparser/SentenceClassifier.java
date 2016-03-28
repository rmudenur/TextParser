package com.rmudenur.textparser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

/**
 * @author rmudenur
 */
public class SentenceClassifier {
	Logger log = LoggerFactory.getLogger(SentenceClassifier.class);
	private BufferedReader bufferedReader;
	private FileInputStream fileInputStream;
	private DataInputStream dataInputStream;
	private ObjectStream<String> lineStream;

	public static void main(String[] args) {
	
		SentenceClassifier sentenceClassifier = new SentenceClassifier(); 
		try {
			sentenceClassifier.processSentences();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@SuppressWarnings("deprecation")
	public void processSentences() throws Exception {

		long startTime = System.currentTimeMillis();

		try {
			String inputText = this.readTextFromFile("openlnp_model/input.txt");
			String sentences[] = this.getSentenceDetectorInstance("openlnp_model/en-sent.bin").sentDetect(inputText);

			//StringBuilder buildStopWords = new StringBuilder("\\b(").append(this.getStopWords()).append(")\\b");
			StringBuilder buildStopWords = new StringBuilder("\\b(").append(this.readTextFromFile("openlnp_model/stopwords.txt")).append(")\\b");
			
			Pattern patternStopWords = Pattern.compile(buildStopWords.toString(), Pattern.CASE_INSENSITIVE);

			POSTaggerME posTagerME = this.applyPartOfSpeachTag();
			List<String> lstNouns;
			for (String sentence : sentences) {

				Matcher matcher = patternStopWords.matcher(sentence);
				String cleanedSentence = matcher.replaceAll("");

				lineStream = new PlainTextByLineStream(new StringReader(cleanedSentence));

				String line = lineStream.read();
				String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE.tokenize(line);
				String[] tags = posTagerME.tag(whitespaceTokenizerLine);

				lstNouns = new ArrayList<String>();
				String preNoun = null;
				for (int i = 0; i < whitespaceTokenizerLine.length; i++) {
					if (tags[i].equalsIgnoreCase("NN") || tags[i].equalsIgnoreCase("NNP") || tags[i].equalsIgnoreCase("NNS")
							|| tags[i].equalsIgnoreCase("NNPS")) {
						lstNouns.add(whitespaceTokenizerLine[i].toUpperCase());
						if(preNoun != null) lstNouns.add(whitespaceTokenizerLine[i].toUpperCase() + " " + preNoun.toUpperCase());
						preNoun = whitespaceTokenizerLine[i].toUpperCase();
					}
				}

				log.info("\n\n" + sentence + "\t");
				for (String nounName : lstNouns) {
					log.info("<" + nounName + ">" + "\t");
				}

				if(sentence.contains("?")) {
					log.info("\t" + "<QUESTION>" + "\t");
				} else if(sentence.contains("!")) {
					log.info("\t" + "<EMOTION>" + "\t");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		System.out.println("Total Time taken : " + (endTime - startTime));

	}

	//Use this method for reading the input text and stopwords text
	private String readTextFromFile(String fileName) throws IOException {
		StringBuffer sbfInputText = null;
		String line;

		try( FileInputStream fileInputStream = new FileInputStream(fileName);
				DataInputStream dataInputStream = new DataInputStream(fileInputStream);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
		    ) {
			sbfInputText = new StringBuffer();
			while ((line = bufferedReader.readLine()) != null) {
				sbfInputText = sbfInputText.append(line);
			}
		} catch(IOException ioEx) {
			log.error("Exception Occurred while reading the file : " + fileName);
			ioEx.printStackTrace();
		}
		return sbfInputText.toString();
	}
	
	

	private POSTaggerME applyPartOfSpeachTag() throws InvalidFormatException, IOException {

		InputStream inputStreamPOSTag = new FileInputStream("openlnp_model/en-pos-maxent.bin");
		POSModel model1 = new POSModel(inputStreamPOSTag);
		return new POSTaggerME(model1);
	}

	
	private SentenceDetector getSentenceDetectorInstance(String openlnpFileName) throws IOException {
		SentenceDetector sentenceDetector = null;
		try(InputStream inputStreamSentenceDetector = new FileInputStream(openlnpFileName)) {
		SentenceModel sentenceModel = new SentenceModel(inputStreamSentenceDetector);
		sentenceDetector = new SentenceDetectorME(sentenceModel);
		}catch(IOException ioEx) {
			log.error("Exception Occurred while reading the OpenLNP file : " + openlnpFileName);
			ioEx.printStackTrace();
		}

		return sentenceDetector; 

	}
	
	// Unused method - Delete it later
	private String readInputText() throws IOException {
		StringBuffer sbfInputText = new StringBuffer();;
		String line;

		try( FileInputStream     fileInputStream         = new FileInputStream("openlnp_model/input.txt");
				DataInputStream dataInputStream = new DataInputStream(fileInputStream);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
		    ) {
			while ((line = bufferedReader.readLine()) != null) {
				sbfInputText = sbfInputText.append(line);
			}
		} 

		return sbfInputText.toString();

	}

	// Unused method - Delete it later
	private String getStopWords() throws IOException {
		fileInputStream = new FileInputStream("openlnp_model/stopwords.txt");
		dataInputStream = new DataInputStream(fileInputStream);
		bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

		return bufferedReader.readLine();
	}
	
}