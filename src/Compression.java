import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Scanner;

public class Compression { // The text to be compressed should be entered into a .txt filled called
							// "inputFile.txt" within the project folder.

	public static void main(String[] args) throws FileNotFoundException {
		Compressor compressor = new Compressor();
		File inputTextFile = new File("inputFile.txt"); // inputFile.txt is a text file that stores the text to be
														// compressed. The program reads from this file.
		Scanner scanner = new Scanner(inputTextFile);
		String toBeCompressed = "";
		String list = ""; // List will later be used in the MTF encoding.

		while (scanner.hasNextLine()) {
			String sentence = scanner.nextLine();
			toBeCompressed = toBeCompressed + sentence;
			list = list + sentence;
		}

		String BWstring = compressor.burrowsWheelerTransform(toBeCompressed); // This is the original string encoded
																				// with a BWT.
		System.out.println("Burrows-Wheeler transform obtained from file: " + BWstring);

		String MTFPositions = compressor.moveToFront(BWstring, list); // This is the MTF encoded BWstring.
		System.out.println("Burrows-Wheeler string encoded with Move-To-From encoding: " + MTFPositions + "\n");

		int[] positions = compressor.positionsAdjuster(list, MTFPositions); // This is the MTF encoded string but as
																			// integers.
		System.out.println("Non-gamma encoded MTF array: " + Arrays.toString(positions));

		String[] gammaEncodedArray = compressor.gammaEncoder(positions);
		System.out.println("Gamma encoded MTF array: " + Arrays.toString(gammaEncodedArray) + "\n"); // This is the
																										// gamma encoded
		// array of the
		// integer-converted MTF
		// array.
//		gammaEncodedArray = compressor.removeLeadingZeroes(gammaEncodedArray);
//		System.out.println("Gamma encoded array without leading zeroes: " + Arrays.toString(gammaEncodedArray) + "\n");

		String listBinary = new BigInteger(list.getBytes()).toString(2);
		System.out.println("Number of bits in original input: " + listBinary.length());

		int numberOfBits = compressor.countBits(gammaEncodedArray);
		System.out.println("Number of bits in gamma encoded final output: " + numberOfBits + "\n");

		System.out.println("Original text bitstring: " + listBinary.toString());

		String finalOutputBitString = String.join("", gammaEncodedArray);
		System.out.println("Gamma encoded bitstring: " + finalOutputBitString + "\n");

		float ratio = (float) listBinary.length() / (float) numberOfBits;
		System.out.println("Compression ratio: " + ratio + ":1");

		float compressionPercentage = (1 - ((float) numberOfBits / (float) listBinary.length())) * 100;
		System.out.printf("%s %.2f %s", "Text compressed by: ", compressionPercentage, "%");
	}

}

class Compressor {

	// Performs a Burrows-Wheeler transformation on the input string and returns the
	// Burrows-Wheeler transformed string.
	public String burrowsWheelerTransform(String input) {
		String[] transformedStrings = new String[input.length()];
		String burrowsWheelerString = "";

		for (int i = 0; i < input.length(); i++) {
			String before = input.substring(i);
			String after = input.substring(0, i);

			transformedStrings[(transformedStrings.length - 1) - i] = before + after;
		}
		Arrays.sort(transformedStrings);
		for (int i = 0; i < transformedStrings.length; i++) {
			burrowsWheelerString = burrowsWheelerString + transformedStrings[i].charAt(input.length() - 1);
		}
		return burrowsWheelerString;
	}

	public String moveToFront(String originalText, String BWstring) {
		for (int j = 0; j < BWstring.length(); j++) {
			int valueBeingMoved;
			for (int k = 0; k < originalText.length(); k++) {
				if (originalText.charAt(k) == BWstring.charAt(j)) {
					valueBeingMoved = k;
					originalText = charShifter(originalText, k);
				}
			}
		}
		return originalText;
	}

	// CharShifter() is an auxiliary class to moveToFront() and positionsAdjuster()
	public String charShifter(String givenString, int positionOfCharToBeMoved) {
		char[] brokenDownString = givenString.toCharArray();
		char movingChar = brokenDownString[positionOfCharToBeMoved];

		for (int i = positionOfCharToBeMoved; i > 0; i--) {
			brokenDownString[i] = brokenDownString[i - 1];
		}
		brokenDownString[0] = movingChar;
		String returnString = new String(brokenDownString);

		return returnString;
	}

	// Creates and returns an array called positions that holds the MTF encoded
	// string as integers.
	public int[] positionsAdjuster(String originalText, String BWtext) {
		int[] positions = new int[BWtext.length()];
		char[] originalCharArr = originalText.toCharArray();
		char[] BWtextCharArr = BWtext.toCharArray();

		for (int i = 0; i < BWtext.length(); i++) {
			for (int j = 0; j < originalText.length(); j++) {
				if (BWtextCharArr[j] == originalCharArr[i]) {
					positions[i] = j;
					String temp = new String(BWtextCharArr);
					temp = charShifter(temp, j);
					BWtextCharArr = temp.toCharArray();
					break;
				}
			}
			// System.out.println("Adjusted Array: " + Arrays.toString(BWtextCharArr)); //
			// This is a test print that shows how the array is being effected every pass.
		}
		return positions;
	}

	// This encodes the integer-converted MTF array into binary using
	// gamma-encoding.
	public String[] gammaEncoder(int[] positions) {
		String[] gammaEncodedArray = new String[positions.length];
		for (int i = 0; i < positions.length; i++) {

			String gammaCodedBinary = "";

			String carryToUnary = "";

			int regularInt = positions[i];

			if (regularInt > 0) {
				int carry = (int) (Math.log(regularInt) / Math.log(2));
				for (int j = 0; j < carry; j++) {
					carryToUnary = carryToUnary + "0";
				}
			}

			String regularIntToBinary = Integer.toBinaryString(regularInt);
			regularIntToBinary.substring(1);
			String unaryConcatenatedWithBinary = carryToUnary + regularIntToBinary;

			gammaCodedBinary = unaryConcatenatedWithBinary;
			gammaEncodedArray[i] = gammaCodedBinary;
		}
		return gammaEncodedArray;
	}

//	public String[] removeLeadingZeroes(String[] inputArray) {
//		int firstOnePosition = 0;
//		
//		for(int i = 0; i < inputArray.length; i++) {
//			char[] indiceHolder = inputArray[i].toCharArray();
//			for(int j = 0; j < indiceHolder.length; j++) {
//				if(indiceHolder[j] == '1') {
//					firstOnePosition = j;
//					break;
//				}
//			}
//			if(!inputArray[i].equals("0")) {
//			inputArray[i] = inputArray[i].substring(firstOnePosition);
//			}
//
//		}
//		return inputArray;
//	}

	// An auxiliary method to convert the indices of a provided int[] to each
	// indice's binary representation.
	public String[] convertToBinary(int[] inputArray) {
		String[] binaryArray = new String[inputArray.length];

		for (int i = 0; i < inputArray.length; i++) {
			binaryArray[i] = Integer.toBinaryString(inputArray[i]);
		}
		return binaryArray;
	}

	// An auxiliary method to total the number of bits in a provided String[] of
	// bit-strings.
	public int countBits(String[] bitArray) {
		int bitCounter = 0;

		for (int i = 0; i < bitArray.length; i++) {
			bitCounter = bitCounter + bitArray[i].length();
		}
		return bitCounter;
	}
}
