import java.util.*;
import tester.*;


/**
 * A class that defines a new permutation code, as well as methods for encoding
 * and decoding of the messages that use this code.
 */
class PermutationCode {
  // The original list of characters to be encoded
  ArrayList<String> alphabet = 
      new ArrayList<String>(Arrays.asList(
                  "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", 
                  "k", "l", "m", "n", "o", "p", "q", "r", "s", 
                  "t", "u", "v", "w", "x", "y", "z"));

  // The encoded alphabet: the 1-string at index 0 is the encoding of "a",
  // the 1-string at index 1 is the encoding of "b", etc.
  ArrayList<String> code;

  // A random number generator
  Random rand;

  // Create a new random instance of the encoder/decoder with a new permutation code 
  PermutationCode() {
    this(new Random());
  }
  
  // Create a particular random instance of the encoder/decoder
  PermutationCode(Random r) {
    this.rand = r;
    this.code = this.initEncoder();
  }

  // Create a new instance of the encoder/decoder with the given code 
  PermutationCode(ArrayList<String> code) {
    this.code = code;
  }

  // Initialize the encoding permutation of the characters
  ArrayList<String> initEncoder() {
    
    ArrayList<String> encodedList = new ArrayList<String>();
    ArrayList<String> alphabetCopy = new ArrayList<String>(Arrays.asList(
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", 
        "k", "l", "m", "n", "o", "p", "q", "r", "s", 
        "t", "u", "v", "w", "x", "y", "z"));
    
    for (int i = 0;
        i < alphabet.size();
        i = i + 1) {
      int randomNum = this.rand.nextInt(alphabetCopy.size());
      encodedList.add(alphabetCopy.get(randomNum));
      alphabetCopy.remove(alphabetCopy.get(randomNum));
    }  
    return encodedList;
  }

  // produce an encoded String from the given String
  // You can assume the given string consists only of lowercase characters
  String encode(String source) {
    String msg = "";
    for (int i = 0;
            i < source.length();
            i = i + 1) {         
      msg = msg + this.code.get(this.alphabet.indexOf(source.substring(i, i + 1)));
    }
    return msg;
  }

  // produce a decoded String from the given String
  // You can assume the given string consists only of lowercase characters
  String decode(String code) {
    String msg = "";
    for (int i = 0;
        i < code.length();
        i = i + 1) {        
      msg = msg + this.alphabet.get(this.code.indexOf(code.substring(i, i + 1)));
    }
    return msg;
  }
}


class ExamplesPermutationCode {
  
  PermutationCode randAlphabet = new PermutationCode(new Random(1));
  PermutationCode randAlphabet1 = new PermutationCode(new Random(2));
  
  ArrayList<String> randAlphabetLetters = new ArrayList<String>(Arrays.asList(
      "s", "d", "x", "p", "o", "q", "a", "v", "h", "g", 
      "k", "b", "z", "w", "n", "u", "r", "m", "c", 
      "j", "e", "t", "l", "y", "f", "i"));
  ArrayList<String> randAlphabet1Letters = new ArrayList<String>(Arrays.asList(
      "y", "f", "a", "l", "k", "r", "g", "m", "b", "o", 
      "e", "i", "x", "j", "u", "p", "s", "h", "t", 
      "n", "d", "w", "c", "q", "v", "z"));
  
  boolean testInitEncoder(Tester t) {
    return t.checkExpect(randAlphabet.initEncoder(), this.randAlphabetLetters)
        && t.checkExpect(randAlphabet.initEncoder(), this.randAlphabet1Letters);
  }
  
  // tests the encode method
  boolean testEncode(Tester t) {
    return t.checkExpect(randAlphabet.encode(""), "")
        && t.checkExpect(randAlphabet.encode("a"), "r")
        && t.checkExpect(randAlphabet.encode("abc"), "rnh")
        && t.checkExpect(randAlphabet.encode("borat"), "nkjrs")
        && t.checkExpect(randAlphabet1.encode(""), "")
        && t.checkExpect(randAlphabet1.encode("a"), "s")
        && t.checkExpect(randAlphabet1.encode("abc"), "sxi")
        && t.checkExpect(randAlphabet1.encode("borat"), "xecsd");
  }
  
  // tests the decode method
  boolean testDecode(Tester t) {
    return t.checkExpect(randAlphabet.decode(""), "")
        && t.checkExpect(randAlphabet.decode("r"), "a")
        && t.checkExpect(randAlphabet.decode("rnh"), "abc")
        && t.checkExpect(randAlphabet.decode("nkjrs"), "borat")
        && t.checkExpect(randAlphabet1.decode(""), "")
        && t.checkExpect(randAlphabet1.decode("s"), "a")
        && t.checkExpect(randAlphabet1.decode("sxi"), "abc")
        && t.checkExpect(randAlphabet1.decode("xecsd"), "borat");
  }
}




