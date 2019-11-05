package com.arabbank.bpm.tesseract.owp;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.arabbank.bpm.tesseract.controller.TesseractFormDBController;
import com.arabbank.bpm.tesseract.util.FileUtility;

import info.debatty.java.stringsimilarity.CharacterSubstitutionInterface;
import info.debatty.java.stringsimilarity.WeightedLevenshtein;


@Component
@PropertySource("classpath:application.properties")
public class OWPFormDictionary {
	
	private static final Logger logger = LoggerFactory.getLogger(OWPFormDictionary.class);
	@Autowired private Environment env;
	
	Set<String> words = new TreeSet<String>();


	public Map<String, Double> findSuggestions(String originalWord) {
		Map<String, Double> suggestedWords = new HashMap<String, Double>();
		
		String contents = FileUtility.readContents(env.getProperty("bpm.owp.words.file_path"));
		StringTokenizer st = new StringTokenizer(contents, " ");
		while (st.hasMoreElements()) {
			words.add(st.nextToken());
		}
		
		for (String word : words) {
			WeightedLevenshtein wl = new WeightedLevenshtein((new CharacterSubstitutionInterface() {
				public double cost(char c1, char c2) {

					// The cost for substituting 't' and 'r' is considered
					// smaller as these 2 are located next to each other
					// on a keyboard
					if (c1 == 't' && c2 == 'r') {
						return 0.5;
					}

					// For most cases, the cost of substituting 2 characters
					// is 1.0
					return 1.0;
				}
			}));

			double distance = wl.distance(originalWord, word);

			int differenceScore = (int) ((distance / originalWord.length()) * 100);
			if (differenceScore < 30)
				suggestedWords.put(word, distance);

		}
		suggestedWords=sortByValue(suggestedWords);
		return suggestedWords;
	}
	
	public static HashMap<String, Double> sortByValue(Map<String, Double> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// put data from Double list to hashmap
		HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}

}
