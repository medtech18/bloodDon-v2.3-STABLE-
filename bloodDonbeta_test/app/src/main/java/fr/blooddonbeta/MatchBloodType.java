package fr.blooddonbeta;

import java.util.Map;

/**
 * Created by simohaj17 on 3/28/18.
 */

public class MatchBloodType
{

    MatchBloodType()
    {

    }


    public static String getRecipientOf(String donor )
    {
        switch (donor)
        {
            case "O -" :
                return "AB +,AB -,A +,A -, B +, B -, O + ,O -" ;
            case "O +" :
                return "AB +,A +, B +, O +" ;
            case "B -" :
                return "AB +,AB -, B +, B -";
            case "B +" :
                return "AB +,B +" ;
            case "A -" :
                return "AB +,AB -,A +,A -" ;
            case "A +" :
                return "AB +,A +" ;
            case "AB -" :
                return "AB +,AB -" ;
            case "AB +" :
                return "AB +" ;

        }

        return null;
    }

    public static String getDonorsOf(String donor )
    {
        switch (donor)
        {
            case "O -" :
                return "O -" ;
            case "O +" :
                return "O -, O +" ;
            case "B -" :
                return "O -,B -" ;
            case "B +" :
                return "O -, O +,B -, B +" ;
            case "A -" :
                return "O -,A -" ;
            case "A +" :
                return "O -,O +,A -,A +" ;
            case "AB -" :
                return "O -,B -,AB -" ;
            case "AB +" :
                return  "O -, O + , B - , B + ,A - ,A + ,AB - ,AB +";
        }

        return null;
    }



    public static StringBuilder covertToS(String[] arr)
    {
        StringBuilder builder = new StringBuilder();

        for(String s : arr) {
            builder.append(s);
        }

        return builder ;
    }

}
