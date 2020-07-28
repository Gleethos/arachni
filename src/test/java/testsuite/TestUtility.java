package testsuite;

import java.util.Arrays;

public class TestUtility {
    public static double similarity(String s1, String s2) {
        String longer = (s1.length() > s2.length()) ?s1 : s2, shorter = (s1.length() > s2.length()) ? s2 : s1;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        if (longer.length() == 0) return 1.0; /* both strings are zero length */
        //double d = 0;
        int delta = (longer.length()-shorter.length());
        double[] shifts = new double[delta+1];
        double[] weights = new double[delta+1];
        double currentWeight = longer.length();
        double weightSum = 0;
        double modifier = (delta+1.0)/shorter.length();
        for (int i=0; i<(delta+1); i++){
            weights[i] = currentWeight;
            weightSum += currentWeight;
            currentWeight *= modifier;
            for (int si=0; si<shorter.length(); si++){
                if (longer.charAt(i+si)==shorter.charAt(si)) shifts[i]++;
            }
            shifts[i] /= longer.length();
        }
        Arrays.sort(shifts);
        Arrays.sort(weights);
        double similarity = 0;
        for (int i=0; i<(delta+1); i++) similarity += shifts[i]*(weights[i] / weightSum);
        //double similarity = (d)/(longer.length()*(delta/shorter.length()+1));
        assert similarity <= 1.0;
        return similarity;
    }

    public static void check(boolean contains, String s, String[] fragments){
        if ( contains ) assertContains( s, fragments );
        else assertNotContains( s, fragments );
    }

    public static void assertContains(String s, String[] fragments){
        for( String f : fragments ) assert s.contains(f);

    }

    public static void assertNotContains(String s, String[] fragments){
        for( String f : fragments ) assert !s.contains(f);
    }

}
