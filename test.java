import java.util.ArrayList;

public class test {

	public static void main(String[] args) {
		ArrayList<String> arr = new ArrayList<String>();
		swapChar("abcdef", "abcdef".length(), arr);
		for(String a: arr) {
			System.out.println(a);
		}
	}
	
	public static void swapChar(String test, int end, ArrayList<String> arr) {
		ArrayList<String> output = new ArrayList<String>();
		if(end<4) {
			output.add(test);
		}
		else if(end == 4) {
			output.add(swapCharPair(swapCharPair(test, 0, 2), 1, 3));
		}
		else {
			output.add(swapCharPair(swapCharPair(test, end-4, end-2), end-3, end-1));
			swapChar(test, end-1, arr); 
		}
	}
	
	public static String swapCharPair(String a, int i, int j) {
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

}
