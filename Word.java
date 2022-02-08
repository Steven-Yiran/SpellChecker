
public class Word{

	private String word; //the string associate with this Word object
	private boolean ignored = false; //set to true if the user wants to ignored all occurrences of the word
	private boolean replaced = false; //set to true if the user has decided to "replace all" instances of this word
	private String replaceWord = ""; //replacement word for all occurrences of misspelled word

	public Word(String w)
	{
		this.word = w;
	}
	
	/**
	 * Compare two Word objects based on their word alphabetically.
	 * @param otherWord the other Word object to compare with.
	 * @return an integer, <0 if less , >0 is larger, =0 equal.
	 */
	public int compareTo(Word otherWord) {
		return this.word.compareTo(otherWord.getWord());
	}
	
	//GETTER AND SETTER METHODS
	
	/**
	 * Setter method for replaced. Takes a boolean as input and returns nothing
	 */
	public void setReplaced(boolean b){
		replaced = b;
	}
	
	/**
	 * Getter method for replaced. Takes no input and returns a boolean.
	 */
	public boolean getReplaced(){
		return replaced;
	}
	
	/**
	 * Setter method for replaceWord. Takes a String as input and returns nothing.
	 */
	public void setReplaceWord(String w){
		replaceWord = w;
	}
	
	/**
	 * Getter method for replaceWord. Takes no input and returns a String.
	 */
	public String getReplaceWord(){
		return replaceWord;
	}
	
	/**
	 * Setter method for word. Takes a String as input and returns nothing.
	 */
	public void setWord(String w)
	{
		word = w;
	}
	
	/**
	 * Getter method for word. Takes no input and returns a String.
	 */
	public String getWord()
	{
		return word;
	}
		
	/**
	 * Getter method for ignored. Takes no input, returns boolean.
	 */
	public boolean getIgnored(){
		return ignored;
	}
	
	/**
	 * Setter method for ignored. Takes a boolean as input, returns nothing
	 */
	public void setIgnored(boolean newIgnored){
		ignored = newIgnored;
	}
	
	
}
