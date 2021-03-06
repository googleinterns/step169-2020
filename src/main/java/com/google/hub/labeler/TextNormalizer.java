package com.google.hub.labeler;

import java.util.List;
import java.util.ArrayList;

class TextNormalizer {
    
    private static String leaveOut = "\".'";
    private static String splitTargets = "\n ";

    public static List<String> getNormalizedWords(String text) {
      if (text != null) {
        text = text.toLowerCase();
        List<String> words = new ArrayList<String>();
        String word = "";
        for (char ch : text.toCharArray()) {
            if (!leaveOut.contains(ch + "")) {
                if (splitTargets.contains(ch + "")) {
                    if (!word.equals("")) {
                        words.add(word);
                    }
                    word = "";
                } else {
                    word += ch;
                }
            }
        }
        if (!word.equals("")) {
            words.add(word);
        }
        return words;
    } else {
      return new ArrayList<String>();
    }
  }
}