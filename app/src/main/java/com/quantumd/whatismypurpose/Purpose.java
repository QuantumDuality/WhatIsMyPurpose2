package com.quantumd.whatismypurpose;

/**
 * Created by root on 21/10/17.
 */

public class Purpose {
    public boolean youPassButter(String str){
        //conditions
        boolean c1, c2, c3;
        String[] words = str.split(" ");

        //Only take into consideration sentences with two or three words
        //The first word can begin with u or Y
        c1 = words[0].charAt(0) == 'Y' || words[0].charAt(0) == 'y' || words[0].charAt(0) == 'y' || words[0].charAt(0) == 'u';
        switch (words.length){
            case 2:
                //For a sentence with two words, the first one must end with 's' and the second one with 'er'
                c2 = words[0].charAt(words[0].length()-1) == 's';
                c3 = words[1].charAt(words[1].length()-2) == 'e' && words[1].charAt(words[1].length()-1) == 'r';
                break;

            case 3:
                //For a sentence with three words, the second one must end with 's' and the third one with 'er'
                c2 = words[1].charAt(words[1].length()-1) == 's';
                c3 = words[2].charAt(words[2].length()-2) == 'e' && words[2].charAt(words[2].length()-1) == 'r';
                break;

            default:
                //For longer or shorter sentences, the conditions are false
                c1 = false;
                c2 = false;
                c3 = false;
                break;
        }

        return c1 && c2 && c3;
    }
    
}
