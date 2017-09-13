//******************************************************************************
// file        : odsLib.java
// author      : jpl
// last change : 20170306
//******************************************************************************    
package fr.ligorax.Dixio;

//******************************************************************************
// imports
//******************************************************************************    

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

//******************************************************************************
// Class ODSLib
//******************************************************************************    
public class ODSLib
{
    //******************************************************************************
    // public fields
    //******************************************************************************
    public String draught;
    public int dictionary;
    public static int[] values;
    private static final String TAG = "ODSLib";

    //******************************************************************************
    // public constants
    //******************************************************************************
    // French dictionaries
    public static final int ODS2 = 2; // 1994
    public static final int ODS3 = 3; // 1999
    public static final int ODS4 = 4; // 2004
    public static final int ODS5 = 5; // 2008
    public static final int ODS6 = 6; // 2012
    public static final int ODS7 = 7; // 2016

    // Sort criteria
    public static final int SORT_ALPHA = 0;
    public static final int SORT_LENGTH = 1;
    public static final int SORT_VALUE = 2;

    // Best criteria
    public static final int BEST_LENGTH = 1;
    public static final int BEST_VALUE = 2;

    // Draught types
    public static final int DRAUGHT_RANDOM = 0;
    public static final int DRAUGHT_SCRABBLE = 1;
    public static final int DRAUGHT_SCRABBLE_NO_JOKERS = 2;
    public static final int DRAUGHT_VOWEL = 3;
    public static final int DRAUGHT_CONSONANT = 4;

    // Dictionary providers
    public static final int DICO_CNRTL = 0;
    public static final int DICO_WIKTIONARY = 1;

    //******************************************************************************
    // private vars
    //******************************************************************************
    private int ODS_ID;
    private Random randomGenerator;
    private Context context;

    //******************************************************************************
    // ODSLib Contructor #1
    //******************************************************************************
    public ODSLib(Context c)
    {
        this(c, ODS7); // Default is the last one
    }

    //******************************************************************************
    // ODSLib Contructor #2
    //******************************************************************************
    public ODSLib(Context c, int dictionary)
    {
        this.dictionary = dictionary;
        this.context = c;
        switch (this.dictionary)
        {
            case ODS2:
                ODS_ID = R.raw.ods2;
                break;
            case ODS3:
                ODS_ID = R.raw.ods3;
                break;
            case ODS4:
                ODS_ID = R.raw.ods4;
                break;
            case ODS5:
                ODS_ID = R.raw.ods5;
                break;
            case ODS6:
                ODS_ID = R.raw.ods6;
                break;
            case ODS7:
                ODS_ID = R.raw.ods7;
                break;
            default:
                ODS_ID = R.raw.ods7;
        }
        Log.i(TAG, Integer.toString(ODS_ID));
        // Log.i(TAG, context.toString());

        randomGenerator = new Random();
        this.values = new int[]
                {
                        1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1, 4, 10, 10, 10, 10, 0
                };
    }

    //******************************************************************************
    // main()
    //******************************************************************************
    /*
       public static void main(String[] args)
       {
          ODSLib odsLib = new ODSLib();
          odsLib.setDraught(getRandomDraught(DRAUGHT_SCRABBLE_NO_JOKERS, 8));
          displayWord(odsLib.getBestWord(BEST_LENGTH));
          displayWord(odsLib.getBestWord(BEST_VALUE));
          displayWords(sortWords(odsLib.findWords(), SORT_VALUE));
          System.out.println(odsLib.getDefinition(DICO_CNRTL, odsLib.getBestWord(BEST_VALUE)));
          displayWords(odsLib.findAnagrams());
       }
    */

    //******************************************************************************
    // setDraught()
    //******************************************************************************
    public void setDraught(String draught)
    {
        this.draught = draught;
        System.out.println("Draught : " + this.draught);
    }

    //******************************************************************************
    // setValues()
    //******************************************************************************
    public void setValues(int[] values)
    {
        this.values = values;
    }

    public String getODSVersion()
    {
        String version = "";
        switch (this.dictionary)
        {
            case ODS2:
                version = "ODS2";
                break;
            case ODS3:
                version = "ODS3";
                break;
            case ODS4:
                version = "ODS4";
                break;
            case ODS5:
                version = "ODS5";
                break;
            case ODS6:
                version = "ODS6";
                break;
            case ODS7:
                version = "ODS7";
                break;
            default:
                version = "ODS7";
        }
        return version;
    }

    //******************************************************************************
    // isValidWord()
    //******************************************************************************
    public boolean isValidWord(String word)
    {
        InputStream inputStream = context.getResources().openRawResource(ODS_ID);
        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        boolean rc = false;
        word = word.toUpperCase();

        // Loop through dictionary
        try
        {
            while ((line = buffReader.readLine()) != null)
            {
                if (line.equals(word))
                {
                    rc = true;
                    break;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return rc;
    }

    //******************************************************************************
    // canFormWord()
    //******************************************************************************
    public boolean canFormWord(String letters, String word)
    {
        // Foreach char check if that letter exists, if it does remove from letter string as we have used them.
        for (char c : word.toCharArray())
        {
            int charIndex = letters.indexOf(c);
            if (charIndex == -1)
            {
                return false; // If not found, the word can't be formed.
            }
            letters = letters.substring(0, charIndex) + letters.substring(charIndex + 1);
        }

        return true; // Making this word is possible with the set of letters we have.
    }

    //******************************************************************************
    // findWords()
    //******************************************************************************
    public ArrayList<String> findWords()
    {
        return findWords(this.draught);
    }


    //******************************************************************************
    // findWords()
    //******************************************************************************
    public ArrayList<String> findWords(String draught)
    {
        // Array list to store all the possible words that can be made.
        ArrayList<String> possibleWords = new ArrayList<>(500);
        // int longestPossibleWord = 1;
        InputStream inputStream = context.getResources().openRawResource(ODS_ID);
        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String word;

        String varLetters = "";
        String fixLetters = "";
        int pIndex = 0;

        for (int index = draught.indexOf('*'); index >= 0; index = draught.indexOf('*', index + 1))
        {
            // System.out.println(index);
            varLetters += "A";
            fixLetters = fixLetters + draught.substring(pIndex, index);
            pIndex = index + 1;
        }
        fixLetters = fixLetters + draught.substring(pIndex);
        // System.out.println("Draught    : " + draught);
        // System.out.println("varLetters : " + varLetters);
        // System.out.println("fixLetters : " + fixLetters);

        // Loop through dictionary
        try
        {
            while ((word = buffReader.readLine()) != null)
            {
                String letters = "";
                if (varLetters.length() > 0)
                {
                    int range = 1;
                    for (int i = 0; i < varLetters.length(); i++)
                    {
                        range = range * 26;
                    }
                    for (int i = 0; i < range; i++)
                    {
                        letters = fixLetters + varLetters;
                        // System.out.println("Letters : " + letters + " Guess " + Integer.toString(i) + " / " + Integer.toString(range));
                        if (canFormWord(letters, word))
                        { // If we can form this word with our letters.
                            /*
                            if (word.length() >= longestPossibleWord)
                            { // If longer than the longest possible word we have so far, add it to the list.
                                longestPossibleWord = word.length();
                            }
                            */
                            possibleWords.add(word);
                        }
                        varLetters = incString(varLetters);
                    }
                } else
                {
                    letters = draught;
                    if (canFormWord(letters, word))
                    { // If we can form this word with our letters.
                        /*
                        if (word.length() >= longestPossibleWord)
                        { // If longer than the longest possible word we have so far, add it to the list.
                            longestPossibleWord = word.length();
                        }
                        */
                        possibleWords.add(word);
                    }
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        // Remove duplicates
        possibleWords = new ArrayList<>(new LinkedHashSet<>(possibleWords));
        return (possibleWords);
    }

    //******************************************************************************
    // incString()
    //******************************************************************************
    // Increment a base 26 number (composed of "digits" A..Z), wrapping around
    // from ZZZ... to AAA...
    private String incString(String str)
    {
        char[] digits = str.toCharArray();

        for (int i = str.length() - 1; i >= 0; --i)
        {
            if (digits[i] == 'Z')
            {
                digits[i] = 'A';
            } else
            {
                digits[i] += 1;
                break;
            }
        }
        return new String(digits);
    }

    //******************************************************************************
    // getWords()
    //******************************************************************************
    public ArrayList<String> getWords(int letters)
    {
        ArrayList<String> words = new ArrayList<>();
        InputStream inputStream = context.getResources().openRawResource(ODS_ID);
        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String word;

        // Loop through dictionary
        try
        {
            while ((word = buffReader.readLine()) != null)
            {
                if (word.length() == letters)
                {
                    words.add(word);
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        // Remove duplicates
        words = new ArrayList<>(new LinkedHashSet<>(words));
        return words;
    }

    //******************************************************************************
    // getRandomWord()
    //******************************************************************************
    public String getRandomWord(int letters)
    {
        ArrayList<String> words = getWords(letters);
        int index = randomGenerator.nextInt(words.size());
        String word = words.get(index);
        System.out.println(word + " (" + getWordValue(word) + ")");
        return word;
    }

    //******************************************************************************
    // sortWords()
    //******************************************************************************
    public static ArrayList<String> sortWords(ArrayList<String> words, int criteria)
    {
        switch (criteria)
        {
            case SORT_LENGTH:
                Comparator<String> sortByLength = new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        if (o1.length() > o2.length())
                        {
                            return -1;
                        }

                        if (o2.length() > o1.length())
                        {
                            return 1;
                        }

                        return 0;
                    }
                };
                Collections.sort(words, sortByLength);
                break;

            case SORT_VALUE:
                Comparator<String> sortByValue = new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        if (getWordValue(o1) > getWordValue(o2))
                        {
                            return -1;
                        }

                        if (getWordValue(o2) > getWordValue(o1))
                        {
                            return 1;
                        }

                        return 0;
                    }
                };
                Collections.sort(words, sortByValue);
                break;

            default:
            case SORT_ALPHA:
                Comparator<String> sortAlpha = new Comparator<String>()
                {
                    @Override
                    public int compare(String s1, String s2)
                    {
                        return s1.compareToIgnoreCase(s2);
                    }
                };
                Collections.sort(words, sortAlpha);
                break;
        }
        return words;
    }

    public static int getWordsListSize(ArrayList<String> words)
    {
        return words.size();
    }

    public static int getWordsListBestLength(ArrayList<String> words)
    {
        ArrayList<String> sortedList = new ArrayList<>();
        sortedList = sortWords(words, SORT_LENGTH);
        return (sortedList.get(0).length());
    }

    public static int getWordsListBestValue(ArrayList<String> words)
    {
        ArrayList<String> sortedList = new ArrayList<>();
        sortedList = sortWords(words, SORT_VALUE);
        return (getWordValue(sortedList.get(0)));
    }

    //******************************************************************************
    // getWordValue()
    //******************************************************************************
    public static int getWordValue(String word)
    {
        int value = 0;
        int index;

        for (char c : word.toCharArray())
        {
            if (c == '*' || c >= 'a')   // jokers
            {
                index = 26;
            } else
            {
                index = ((int) c) - 65;
            }
            value += values[index];
        }
        return value;
    }

    //******************************************************************************
    // findAnagrams()
    //******************************************************************************
    public ArrayList<String> findAnagrams()
    {
        return findAnagrams(this.draught);
    }

    //******************************************************************************
    // findAnagrams()
    //******************************************************************************
    public ArrayList<String> findAnagrams(String letters)
    {
        ArrayList<String> words = findWords(letters);
        ArrayList<String> anagrams = new ArrayList<>();
        words = sortWords(words, SORT_LENGTH);
        for (int i = 0; i < words.size(); i++)
        {
            String word = words.get(i);
            if (word.length() < letters.length())
            {
                break;
            } else
            {
                anagrams.add(word);
            }
        }
        return anagrams;
    }

	//******************************************************************************
	// isWordInArray()
	//******************************************************************************
    public boolean isWordInArray(String word, ArrayList<String> array)
	 {
		 return array.contains(word);
	 }

    //******************************************************************************
    // displayWord()
    //******************************************************************************
    public static void displayWord(String word)
    {
        System.out.println("Word : " + word + " Length : " + Integer.toString(word.length()) + " Value : " + Integer.toString(getWordValue(word)));
    }

    //******************************************************************************
    // displayWords()
    //******************************************************************************
    public static void displayWords(ArrayList<String> words)
    {
        if (words.size() > 0)
        {
            for (int i = 0; i < words.size(); i++)
            {
                String word = words.get(i);
                displayWord(word);
            }
            System.out.println("Total : " + Integer.toString(words.size()));
        } else
        {
            System.out.println("Empty list of words.");
        }
    }

    //******************************************************************************
    // getBestWord()
    //******************************************************************************
    public String getBestWord(int criteria)
    {
        return getBestWord(this.draught, criteria);
    }

    //******************************************************************************
    // getBestWord()
    //******************************************************************************
    public String getBestWord(String letters, int criteria)
    {
        String word = "";

        ArrayList<String> words = findWords(letters);
        switch (criteria)
        {
            case BEST_LENGTH:
                words = sortWords(words, SORT_LENGTH);
                word = words.get(0);
                break;

            case BEST_VALUE:
                words = sortWords(words, SORT_VALUE);
                word = words.get(0);
                break;
        }
        return word;
    }

    //******************************************************************************
    // getDefinition()
    //******************************************************************************
    public String getDefinition(int dico, String word)
    {
        String definition = "Pas de connexion à internet.";
        switch (dico)
        {
            case DICO_CNRTL:
                try
                {
                    Document doc = Jsoup.connect("http://www.cnrtl.fr/definition/" + word).get();
                    Element content = doc.select("div#contentbox").first();
                    definition = content.toString();
                } catch (Exception e)
                {
                    // System.out.println(e.getMessage());
                    Log.e(TAG, e.toString());
                }
                break;

            case DICO_WIKTIONARY:
                try
                {
                    Document doc = Jsoup.connect("https://fr.wiktionary.org/wiki/" + word.toLowerCase()).get();
                    Element content = doc.select("div[id=content]").first();
                    definition = content.toString();
                } catch (Exception e)
                {
                    // System.out.println(e.getMessage());
                    Log.e(TAG, e.toString());
                }
                break;
        }
        return definition;
    }

    //******************************************************************************
    // getDicoVersion()
    //******************************************************************************
    public String getDicoVersion(int dico)
    {
        String version = "";
        switch (dico)
        {
            case DICO_CNRTL:
                version = "CNRTL (Centre National de Ressources Textuelles et Lexicales)";
                break;
            case DICO_WIKTIONARY:
                version = "Wiktionary (Wiktionnaire en version française)";
                break;
        }
        return version;
    }

    public String shuffleWord(String word)
	 {
		 List<String> letters = Arrays.asList(word.split(""));
		 Collections.shuffle(letters);
		 String shuffled = "";
		 for (String letter : letters) {
			 shuffled += letter;
		 }
		 return shuffled;
	 }

    //******************************************************************************
    // getRandomDraught()
    //******************************************************************************
    public static String getRandomDraught(int draughtType, int letters)
    {
        String out = "";
        Random r = new Random();

        switch (draughtType)
        {
            default:
            case DRAUGHT_SCRABBLE:
                ScrabbleFRSet setLetters = new ScrabbleFRSet();
                for (int i = 0; i < letters; i++)
                {
                    out += setLetters.pickLetter();
                }
                break;

            case DRAUGHT_SCRABBLE_NO_JOKERS:
                ScrabbleFRSet_NoJokers setLetters_NoJokers = new ScrabbleFRSet_NoJokers();
                for (int i = 0; i < letters; i++)
                {
                    out += setLetters_NoJokers.pickLetter();
                }
                break;

            case DRAUGHT_RANDOM:
                for (int i = 0; i < letters; i++)
                {
                    char c = (char) (r.nextInt(26) + 'A');
                    out += Character.toString(c);
                }
                break;

            case DRAUGHT_VOWEL:
                String setVowels = "AEIOUY";
                for (int i = 0; i < letters; i++)
                {
                    char c = (char) (setVowels.charAt(r.nextInt(setVowels.length())));
                    out += Character.toString(c);
                }
                break;

            case DRAUGHT_CONSONANT:
                String setConsonants = "BCDFGHJKLMNPQRSTVWXZ";
                for (int i = 0; i < letters; i++)
                {
                    char c = (char) (setConsonants.charAt(r.nextInt(setConsonants.length())));
                    out += Character.toString(c);
                }
                break;
        }
        return out;
    }

    private static class ScrabbleFRSet
    {

        protected List<String> letters = new ArrayList(Arrays.asList(
                "*", "*",
                "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E",
                "A", "A", "A", "A", "A", "A", "A", "A", "A",
                "I", "I", "I", "I", "I", "I", "I", "I",
                "N", "N", "N", "N", "N", "N",
                "O", "O", "O", "O", "O", "O",
                "R", "R", "R", "R", "R", "R",
                "S", "S", "S", "S", "S", "S",
                "T", "T", "T", "T", "T", "T",
                "U", "U", "U", "U", "U", "U",
                "L", "L", "L", "L", "L",
                "D", "D", "D",
                "M", "M", "M",
                "G", "G",
                "B", "B",
                "C", "C",
                "P", "P",
                "F", "F",
                "H", "H",
                "V", "V",
                "J", "Q", "K", "W", "X", "Y", "Z"
        ));
        protected List<String> remaining = letters;

        public String pickLetter()
        {
            if (remaining.isEmpty())
            {
                throw new IllegalStateException("No more letters");
            }
            int index = (int) (Math.random() * remaining.size());
            String result = remaining.remove(index);
            return result;
        }
    }

    private static class ScrabbleFRSet_NoJokers
    {

        protected List<String> letters = new ArrayList(Arrays.asList(
                "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E",
                "A", "A", "A", "A", "A", "A", "A", "A", "A",
                "I", "I", "I", "I", "I", "I", "I", "I",
                "N", "N", "N", "N", "N", "N",
                "O", "O", "O", "O", "O", "O",
                "R", "R", "R", "R", "R", "R",
                "S", "S", "S", "S", "S", "S",
                "T", "T", "T", "T", "T", "T",
                "U", "U", "U", "U", "U", "U",
                "L", "L", "L", "L", "L",
                "D", "D", "D",
                "M", "M", "M",
                "G", "G",
                "B", "B",
                "C", "C",
                "P", "P",
                "F", "F",
                "H", "H",
                "V", "V",
                "J", "Q", "K", "W", "X", "Y", "Z"
        ));
        protected List<String> remaining = letters;

        public String pickLetter()
        {
            if (remaining.isEmpty())
            {
                throw new IllegalStateException("No more letters");
            }
            int index = (int) (Math.random() * remaining.size());
            String result = remaining.remove(index);
            return result;
        }
    }
}
