package testsuite;

public class TestUtility {
    public static double similarity(String s1, String s2) {
        String longer = (s1.length() > s2.length()) ?s1 : s2, shorter = (s1.length() > s2.length()) ? s2 : s1;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) return 1.0; /* both strings are zero length */
        double d = 0;
        int delta = (longer.length()-shorter.length());
        for (int i=0; i<(delta+1); i++){
            for (int si=0; si<shorter.length(); si++){
                if (longer.charAt(i+si)==shorter.charAt(si)) d++;
            }
        }
        return d/(longerLength+delta+1);
    }

    public static void assertContains(String s, String[] fragments){
        for( String f : fragments ) assert s.contains(f);
    }

    public static void assertNotContains(String s, String[] fragments){
        for( String f : fragments ) assert !s.contains(f);
    }

}
