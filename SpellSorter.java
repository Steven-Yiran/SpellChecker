/*Yiran Shi CS201
 * “I have neither given nor received unauthorized aid on this assignment”
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class SpellSorter {

	final static String alphabet = "abcdefghijklmnopqrstuvwxyz";
	final static int arrlen = 52; //26 upper + 26 lower alphabets

	public static void main(String[] args) throws IOException{
		//read the name of the dictionary file from command line argument
		runSpellCheck(args[0]);
	}
	
	/**
	 * Start and operate the Spell check interaction and also the spell sort methods.
	 * @param dicName the dictionary source filename.
	 * @throws IOException
	 */
	private static void runSpellCheck(String dicName) throws IOException {
		Scanner sc = new Scanner(System.in);
		ArrayList<String> output = new ArrayList<String>();//use to keep track of final output content
		QuadraticProbingHashTable<String> dictionary = setDic(dicName);
		ArrayList<Word>[] missedArr = setMissArr(arrlen);
		
		
		System.out.println("Reading in Dictionary...\nDictionary Read.\n" + 
				"Please enter a file to spell check>>");
		String testFile = sc.next();
		
		boolean doCheck = true;
		while(doCheck) {
			System.out.println("Print words (p), enter new file (f), or quit (q) ?");
			switch(sc.next()) {
			case "p":
				spellCheck(testFile, dictionary, missedArr, output);
				writeFile(output, testFile.substring(0, testFile.length()-4)+"_corrected.txt");
				System.out.println("Spell check complete!");
				break;
			case "f":
				System.out.println("Please enter a file to spell check>>");
				testFile = sc.next();
				break;
			case "q":
				System.out.println("\nGoodbye!");
				doCheck = false;
				break;
			}
		}
		runSpellSort(testFile, dictionary);
	}
	
	/**
	 * Read from the dictionary source file words and sort them into a hash table.
	 * @param filename the filename of the dictionary file.
	 * @param dicHash the hash table to sort dictionary words.
	 * @throws FileNotFoundException
	 */
	private static QuadraticProbingHashTable<String> setDic(String filename) throws FileNotFoundException{
		QuadraticProbingHashTable<String> dicHash = new QuadraticProbingHashTable<String>();
		Scanner filereader = new Scanner(new File(filename));
		while(filereader.hasNextLine()) {
			dicHash.insert(filereader.nextLine());
		}
		filereader.close();
		return dicHash;
	}
	
	/**
	 * Initialize the hash table to store the misspelled words
	 * @param len length of the array, in this case 52. 
	 * @return the initialized array of ArrayList. 
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<Word>[] setMissArr(int len) {
		ArrayList<Word>[] missedArr = new ArrayList[len];
		for(int i=0;i<missedArr.length;i++) { //initializing
			missedArr[i] = new ArrayList<Word>();
		}
		return missedArr;
	}
	
	/**
	 * For each word in the target file, run the spell check sequence.
	 * @param filename the file to be checked.
	 * @param dicHash the dictionary Hash Table.
	 * @param missArr the misspelled words table.
	 * @param output the output ArrayList for output file.
	 * @throws IOException
	 */
	private static void spellCheck(String filename, QuadraticProbingHashTable<String> dicHash, 
			ArrayList<Word>[] missArr, ArrayList<String> output) throws IOException {		
		Scanner filereader = new Scanner(new File(filename));
		boolean test = true;
		while(filereader.hasNext()) {
			for(String wordStr: filereader.nextLine().split(" ")) { //replaceAll("\\p{Punct}","")
				char puncTemp = '\0';
				
				if(wordStr.matches(".*\\p{Punct}")) { //word contain punctuation
					puncTemp = wordStr.charAt(wordStr.length()-1); //save the punctuation
					wordStr = wordStr.replaceAll("\\p{Punct}",""); //delete all punctuation
				}
				if(test) {
					if(dicHash.contains(wordStr)) {
						output.add(wordStr + puncTemp + " ");
					}
					else { //not in dictionary
						Word incorrect = new Word(wordStr);

						int index = findIndex(wordStr);
						if(containsWord(missArr[index], wordStr)){ //in misspell
							
							if(getIgnore(missArr[index], wordStr)) {
								incorrect.setIgnored(true);
								output.add(wordStr + puncTemp + " ");
							}
							else if(!getReplace(missArr[index], wordStr).equals("")) {
								String replace = getReplace(missArr[index], wordStr);
								incorrect.setReplaceWord(replace);
								output.add(replace + puncTemp + " ");
							}
							else {
								test = stanOps(dicHash, missArr[index], incorrect, output, puncTemp);
							}
						}
						else { //first time
							missArr[index].add(incorrect);
							test = stanOps(dicHash, missArr[index], incorrect, output, puncTemp);
						}
					}
				}
				else {//user choose to quit
					output.add(wordStr + puncTemp + " "); //add rest of the words 
				}
			}
			output.add("\n");;
		}
		filereader.close();
	}
	
	/**
	 * Determine the index of the String in a misspelled table.
	 * @param a the String of a word
	 * @return index as an integer.
	 */
	private static int findIndex(String a) {
		int index;
		if(Character.isUpperCase(a.charAt(0))) {
			index = (int) a.charAt(0) - 65; //65 is the ASCII index of 'A'
		}
		else {
			index = (int) a.charAt(0) - 71; //97 is the ASCII index of 'a', 97 - 26 = 71.
		}
		return index;
	}
	
	/**
	 * A list of operations when users didn't decide what to do with the word.
	 * Return false is user decided to quit.
	 * @param dicHash the dictionary Hash Table.
	 * @param missArrList the misspelled words Hash Table.
	 * @param incorrect the Word object.
	 * @param output the output ArrayList.
	 * @param punc the attached punctuation of a word.
	 * @return
	 */
	private static boolean stanOps(QuadraticProbingHashTable<String> dicHash, ArrayList<Word> missArrList, Word incorrect, 
			ArrayList<String> output, char punc) {
		Scanner sc = new Scanner(System.in);
		String wordStr = incorrect.getWord();
		System.out.println("--" + wordStr);
		System.out.println("ignore all (i), replace all (r), next(n), or quit (q)?");
		String choice = sc.next();
		if(choice.equals("q")) {
			output.add(wordStr + punc + " ");
			return false;
		}
		else {
			wordStr = misspellOps(dicHash, missArrList, incorrect, output, choice);
			output.add(wordStr + punc + " ");
		}
		return true;
	}
	
	/**
	 * Check if a list of Word objects contain a specific text.
	 * @param a the ArrayList of Word.
	 * @param text the target text String.
	 * @return boolean indicating if the ArrayList have the text.
	 */
	private static boolean containsWord(ArrayList<Word> a, String text) {
		for(Word word: a) {
			if(word.getWord().equals(text)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if a specific text in a list of Word objects is set to be ignored.
	 * @param a the ArrayList of Word.
	 * @param text the target text String.
	 * @return boolean indicating if the text is ignored.
	 */
	private static boolean getIgnore(ArrayList<Word> a, String text) {
		for(Word word:a) {
			if(word.getWord().equals(text) && word.getIgnored()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check the replacement word for a misspelled word in a list of Word objects.
	 * @param a the ArrayList of Word.
	 * @param text the target text String.
	 * @return the ignored String word (empty String if none).
	 */
	private static String getReplace(ArrayList<Word> a, String text) {
		for(Word word:a) {
			if(word.getWord().equals(text)) {
				return word.getReplaceWord();
			}
		}
		return "";
	}
	
	/**
	 * A list of user interactions for misspelled words.
	 * @param dic the dictionary hash table.
	 * @param missArr the misspelled words hash table.
	 * @param incorrect the Word object.
	 * @param output the ArrayList of output words.
	 * @param d the decision of user on the word (i, r, n).
	 * @return user's chosen word.
	 */
	private static String misspellOps(QuadraticProbingHashTable<String> dic, 
			ArrayList<Word> missArr, Word incorrect, ArrayList<String> output, String d) {
		Scanner sc = new Scanner(System.in);
		String currWord = incorrect.getWord();
		String replaceWord;
	
		switch(d) {
		case "i":
			setIgnore(missArr, incorrect.getWord()); //set ignore for all previous instances
			break;
		case "r":
			ArrayList<String> replaceChoice = gSuggestion(dic, currWord);
			if(!replaceChoice.isEmpty()) {//not empty suggestion
				replaceWord = getReplaceChoice(replaceChoice,missArr,incorrect, output);
				setReplace(missArr, incorrect.getWord(), replaceWord); //set replaceWord for all previous instances
				return replaceWord;
			}
			else { //empty suggestion -> chose again
				System.out.println("There are no words to replace it with.\n--" + currWord);
				if(sc.next().equals("i")) {
					setIgnore(missArr, incorrect.getWord()); //set ignore for all previous instances
				}
				break; //chose next
			}
		case "n":
			break;
		}
		return currWord;
	}	
	
	/**
	 * Set the ignore value of all Word Objects of the same text.
	 * @param a the ArrayList of Words.
	 * @param text the String to search for.
	 */
	private static void setIgnore(ArrayList<Word> a, String text) {
		for(Word word:a) {
			if(word.getWord().equals(text)) {
				word.setIgnored(true);
			}
		}
	}
	
	/**
	 * Set the replace word value of all Word Objects of the same text.
	 * @param a the ArrayList of Words.
	 * @param text the String to search for.
	 * @param r the replacement String.
	 */
	private static void setReplace(ArrayList<Word> a, String text, String r) {
		for(Word word:a) {
			if(word.getWord().equals(text)) {
				word.setReplaceWord(r);
			}
		}
	}
	
	/**
	 * Generates an ArrayList of possible corrections for a word by calling other methods.
	 * @param dic the dictionary Hash Table.
	 * @param test the test String.
	 * @return an ArrayList of possible words.
	 */
	private static ArrayList<String> gSuggestion(QuadraticProbingHashTable<String> dic, String test) {
		ArrayList<String> tempArr = new ArrayList<String>();//use to store all suggestions
		
		for(int i=0;i<5;i++) {
			switch(i) {
			case 0:
				ArrayList<String> result = new ArrayList<String>();
				swapChar(dic, test, test.length(), result);
				if(!result.isEmpty()) {
					tempArr.addAll(result);
				}
				break;
			case 1:
				if(!insertChar(dic, test).isEmpty()) {
					tempArr.addAll(insertChar(dic, test));
				}
				break;
			case 2:
				if(!deleteChar(dic, test).isEmpty()) {
					tempArr.addAll(deleteChar(dic, test));
				}
				break;
			case 3:
				if(!replaceChar(dic, test).isEmpty()) {
					tempArr.addAll(replaceChar(dic, test));
				}
				break;
			case 4:
				if(!splitChar(dic, test).isEmpty()) {
					tempArr.addAll(splitChar(dic, test));
				}
				break;
			}
		}
		return tempArr;
		}
	
	private static final String suggestFormat = "%s)%s ";
	private static final String outputFormat = "Replace with %sor next(n)";
	
	/**
	 * Ask user for what they decided to replace the word with.
	 * @param tempArr the array of corrections (assume not empty).
	 * @param missArr the array of misspelled words.
	 * @param incorrect the Word object.
	 * @param output the output ArrayList.
	 * @return the String that the user decided to use.
	 */
	private static String getReplaceChoice(ArrayList<String> tempArr, ArrayList<Word> missArr, 
			Word incorrect, ArrayList<String> output) {
		//generate tempOut
		Scanner sc = new Scanner(System.in);
		String tempOut = "";
		
		String currWord = incorrect.getWord();
		
		for(int i=0;i<tempArr.size();i++) {
			tempOut += String.format(suggestFormat, i+1, tempArr.get(i));
		}
		//give suggestions
		System.out.println(String.format(outputFormat, tempOut));
		//get user's choice
		String choice = sc.next(); 
		if(choice.equals("n")) {
			return currWord; 
		}
		else {
			//index is one less then the number displayed 
			int index = Integer.parseInt(choice)-1;
			String temp = tempArr.get(index); 
			return temp;
		}
	}
	
	/**
	 * Recursively swap each adjacent pair of characters in the word.
	 * @param dic the dictionary of all right words.
	 * @param test the String word to be tested.
	 * @param end resursive variable.
	 * @param output the ArrayList to add suggestion in. 
	 */
	private static void swapChar(QuadraticProbingHashTable<String> dic, String test, int end, ArrayList<String> output) {
		if(end<4) {
			//do nothing
		}
		else if(end == 4) { //base case
			String swaped = swapCharPair(swapCharPair(test, 0, 2), 1, 3);
			if(dic.contains(swaped)) {
				output.add(swaped);
			}
		}
		else { //recursive step
			String swaped = swapCharPair(swapCharPair(test, end-4, end-2), end-3, end-1);
			if(dic.contains(swaped)) {
				output.add(swaped);
			}
			swapChar(dic, test, end-1, output); 
		}
	}
	
	/**
	 * Swap the two char at given index for a String (to be used during recursion swapChar)
	 * @param a the target String.
	 * @param i the first swap index.
	 * @param j the second swap index. 
	 * @return the swapped String. 
	 */
	private static String swapCharPair(String a, int i, int j) {
		String output = "";
		char[] arr = a.toCharArray();
		char temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
		
		for(int k = 0;k<a.length();k++) {
			output += arr[k];
		}
		return output;
	}
	
	/**
	 * Insert in front, back, and between each adjacent pair of characters in the word, each letter from 'a' through 'z'.
	 * @param dic the dictionary of all right words.
	 * @param test the String word to be tested.
	 * @return an Arraylist of suggestions (null is no suggestions).
	 */
	private static ArrayList<String> insertChar(QuadraticProbingHashTable<String> dic, String test) {
		ArrayList<String> output = new ArrayList<String>();
		
		//add to front
		for(int i=0;i<alphabet.length();i++) {
			String tempChar = Character.toString(alphabet.charAt(i));
			String temp = tempChar + test;
			if(dic.contains(temp)) {
				output.add(temp);
			}
		}
		//add in between and back
		for(int i=0;i<alphabet.length();i++) {
			String tempChar = Character.toString(alphabet.charAt(i));
			String temp = "";
			for(int j=0;j<test.length();j++) {
				temp += test.substring(0,j);
				temp += test.charAt(j) + tempChar;
				temp += test.substring(j+1);
			}
			if(dic.contains(temp)) {
				output.add(temp);
			}
		}
		return output;
	}
	
	/**
	 * Delete each character from the word.
	 * @param dic the dictionary of all right words.
	 * @param test the String word to be tested.
	 * @return an Arraylist of suggestions (null is no suggestions).
	 */
	private static ArrayList<String> deleteChar(QuadraticProbingHashTable<String> dic, String test) {
		ArrayList<String> output = new ArrayList<String>();
		for(int i = 0; i<test.length();i++) {
			String temp = "";
			temp += test.substring(0, i) + test.substring(i+1);
			if(dic.contains(temp)) {
				output.add(temp);
			}
		}
		return output;
	}
	
	/**
	 * Replace each character in the word with each letter from 'a' through 'z'.
	 * @param dic the dictionary of all right words.
	 * @param test the String word to be tested.
	 * @return an Arraylist of suggestions (null is no suggestions).
	 */
	private static ArrayList<String> replaceChar(QuadraticProbingHashTable<String> dic, String test) {
		ArrayList<String> output = new ArrayList<String>();
		for(int i=0; i<test.length();i++) {
			for(int j=0;j<alphabet.length();j++) {
				String temp = "";
				if(i == 0) //replacing the first letter
					temp += alphabet.charAt(j) + test.substring(i+1);
				else
					temp += test.substring(0, i) + alphabet.charAt(j) + test.substring(i+1);
				if(dic.contains(temp)) {
					output.add(temp);
				}
			}
		}
		return output;
	}
	
	/**
	 * Split the word into a pair of words by adding a space in between 
	 * each adjacent pair of characters in the word.
	 * @param dic the dictionary of all right words.
	 * @param test the String word to be tested.
	 * @return an Arraylist of suggestions (null is no suggestions).
	 */
	private static ArrayList<String> splitChar(QuadraticProbingHashTable<String> dic, String test) {
		ArrayList<String> output = new ArrayList<String>();
		for(int i=0;i<test.length();i++) {
			String a = test.substring(0, i);
			String b = test.substring(i);
			if(dic.contains(a) && dic.contains(b)) {
				output.add(a + " " + b);
			}
		}
		return output;
	}
	
	/**
	 * Creates and writes contents into local files.
	 * @param content Strings to write.
	 * @param filename name of the output file.
	 * @throws IOException
	 */
	private static void writeFile(ArrayList<String> outArr, String filename) throws IOException{
		String content = "";
		for(int i=0;i<outArr.size();i++) {
			content += outArr.get(i);
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		out.write(content);
		out.close();
	}
	
	/***bucket sorting methods***/
	
	/**
	 * Execute methods to sort misspelled words, output sorted words into a file.
	 * @param testFile the String name of the test file.
	 * @param dictionary the Hash Table of correct words. 
	 * @throws IOException ***/
	private static void runSpellSort(String testFile, QuadraticProbingHashTable<String> dictionary) throws IOException {
		ArrayList<Word>[] missedArr = setMissArr(arrlen);
		//make sure all misspelled words are in the collection (even if user choose to quit first).
		missedArr = allMisspelled(testFile, dictionary);
		sortMisspell(missedArr);
		
		String outFile = testFile.substring(0, testFile.length()-4) + "_sorted.txt";
		String output = "";
		for(ArrayList<Word> arr:missedArr) {
			for(Word w:arr) {
				output += w.getWord() + "\n";
			}
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		out.write(output);
		out.close();
	}
	
	/**
	 * Return all misspelled words in a given file in an array of ArrayList.
	 * @param filename the file to be checked.
	 * @param dicHash dictionary Hash Table.
	 * @return misspelled Hash Table.
	 * @throws FileNotFoundException
	 */
	private static ArrayList<Word>[] allMisspelled(String filename, QuadraticProbingHashTable<String> dicHash) throws FileNotFoundException {
		ArrayList<Word>[] missArr = setMissArr(52);
		Scanner filereader = new Scanner(new File(filename));
		int index;

		while(filereader.hasNext()) {
			for(String wordStr: filereader.nextLine().replaceAll("\\p{Punct}","").split(" ")) {
				
				if(Character.isUpperCase(wordStr.charAt(0))) {
					index = (int) wordStr.charAt(0) - 65; //65 is the ASCII index of 'A'
				}
				else {
					index = (int) wordStr.charAt(0) - 71; //97 is the ASCII index of 'a', 97 - 26 = 71.
				}
				
				if(!dicHash.contains(wordStr) && !containsWord(missArr[index], wordStr)) {
					Word incorrect = new Word(wordStr);
					missArr[index].add(incorrect);
				}
			}
		}
		return missArr;
	}
	
	/**
	 * Sort each ArrayList in the misspell table.
	 * @param missTable the Hash Table to be sorted.
	 */
	private static void sortMisspell(ArrayList<Word>[] missTable) {
		for(ArrayList<Word> arr:missTable) {
			if(!arr.isEmpty()){
				if(arr.size() == 1) {
					
				}
				else {
					QuickSort(arr, 0, arr.size());
				}
			}
		}
	}
	
	/**
	 * Implement QuickSort by recursively calling partition method.
	 * @param arr the ArrayList to be sorted
	 * @param first the first index.
	 * @param last the last index.
	 */
	public static void QuickSort(ArrayList<Word> arr, int first, int last) {
		if(first-last<3) {
			sort(arr);
		}
		else {
			Word pivot = choosePivot(arr);
			int pivotIndex = arr.indexOf(pivot);
			int splitPoint = partition(arr, 0, arr.size()-1, pivot, pivotIndex);
			QuickSort(arr, first, splitPoint-1);
			QuickSort(arr, splitPoint+1, last);
		}
	}
	

	/**
	 * Performs insertion sort on a ArrayList.
	 * @param arr ArrayList of Words objects.
	 */
	private static void sort(ArrayList<Word> arr) {
		for(int i=1;i<arr.size();i++) {
			Word key = arr.get(i);
			int j = i-1;
			while(j>=0 && arr.get(j).compareTo(key) > 0) {
				arr.set(j+1, arr.get(j));
				j--;
			}
			arr.set(j+1, key);
		}
	}
	
	/**
	 * Choose a pivot using the median 3 method.
	 * @param a an ArrayList of Word.
	 * @return the pivot Word.
	 */
	private static Word choosePivot(ArrayList<Word> a) {
		int f,m,l;
		f = 0; //first
		m = a.size()/2; //median
		l = a.size()-1; //last
		ArrayList<Word> temp = new ArrayList<Word>();
		temp.add(a.get(f));
		temp.add(a.get(m));
		temp.add(a.get(l));
		sort(temp);
		return temp.get(temp.size()/2);
	}
	
	/**
	 * Sort a given ArrayList using quick sort methods.
	 * @param arr the ArrayList to be sorted
	 * @param first the first index
	 * @param last the last index
	 * @param pivot the pivot Word
	 * @param pivotIndex the index of the pivot Word.
	 */
	private static int partition(ArrayList<Word> arr, int first, int last, Word pivot, int pivotIndex) {
		//swap the pivot and the last element
		arr.set(pivotIndex, arr.get(last));
		arr.set(last, pivot);
		
		int i = first;
		int j = last -1;
		boolean loop = true;
		while(loop) {
			//A[i] <= pivot
			while(arr.get(i).compareTo(pivot)<=0) {
				i++;
			}
			while(arr.get(j).compareTo(pivot) >=0) {
				j--;
			}
			//A[i] > pivot or A[j]<pivot
			if(i<j) {
				Word temp = arr.get(i);
				arr.set(i, arr.get(j));
				arr.set(j, temp);
			}
			else {
				loop = false;
			}
		}
		//swap pivot back
		arr.set(i, pivot);
		return i;
	}
	
}
