

import java.util.*;

public class MyLZW {

    private static int mode = 0;	
    private static final int MIN_CODE_LENGTH = 9;
    private static final int MAX_CODE_LENGTH = 16;
    private static final int R = 256; // number of input chars
    private static int L = 512; // number of codewords
    private static int W = MIN_CODE_LENGTH; // codeword width

    public static void compress() {
        int inBits = 0;
        int outBits = 0;
        float lastRatio = 1;
        float currentRatio = 0;
        boolean monStarted = false;

        BinaryStdOut.write(mode, 2); // Write mode
        String input = BinaryStdIn.readString();
        TST<Integer> st = getFirstCodebook(); // Initialize table
        int code = R + 1;  // R is codeword for EOF

        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            inBits += t * 8;
            outBits += W;

            if (t < input.length()) { // Add s to symbol table.
                if (code >= L) {
                    boolean full = !resizeCodeWidth(W + 1); // resize  width
                    if (full) // if W >= MAX_CODE_LENGTH
                    {
                        boolean clearBook = false;
                        if (mode == 1) { // Reset mode
                            clearBook = true;
                        } else if (mode == 2) { // Monitor mode
                            currentRatio = inBits / (float) outBits;
                            if (!monStarted) { // Start monitor
                                lastRatio = currentRatio;
                                monStarted = true;
                            } else if (lastRatio / currentRatio > 1.1) {
                                clearBook = true;
                                monStarted = false;
                            }
                        }
                        if (clearBook) { // Clear the codebook and reset codewidth
                            st = getFirstCodebook();
                            code = R + 1;
                            resizeCodeWidth(MIN_CODE_LENGTH);
                        }
                    }
                }
                if (code < L) {
                    st.put(input.substring(0, t + 1), code++); // Add s 
                }
            }
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    }

    public static void expand() {
        int inBits = 0;
        int outBits = 0;
        float currentRatio = 0;
        float lastRatio = 1;
        boolean monStart = false;

        mode = BinaryStdIn.readInt(2); // Read mode
        ArrayList<String> st = getFirstCodebookList(); // Initialize table
        int i = R + 1;
       
        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) {
            return;
        }
        String val = st.get(codeword);
        
        while (true) {
            inBits += val.length() * 8;
            outBits += W;
            if (i >= L) {
                boolean codebookFull = !resizeCodeWidth(W + 1);	// check if codeword needs resized
                if (codebookFull) {
                    boolean clearCodeBook = false;
                    if (mode == 1) { // Reset Mode
                        clearCodeBook = true;
                    } else if (mode == 2) { // Monitor mode
                        currentRatio = inBits / (float) outBits;
                        if (!monStart) { // Start monitoring
                            lastRatio = currentRatio;
                            monStart = true;
                        } else if (lastRatio / currentRatio > 1.1) {
                            clearCodeBook = true;
                            monStart = false;
                        }
                    }
                    if (clearCodeBook) {
                        st = getFirstCodebookList();
                        i = R + 1;
                        resizeCodeWidth(MIN_CODE_LENGTH);
                    }
                }
            }
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) {
                break;
            }
            String s = null;
            if (i == codeword) {
                s = val + val.charAt(0);
            } else {
                s = st.get(codeword);
            }

            if (i < L) {
                st.add(val + s.charAt(0));
                i++;
            }
            val = s;
        }
        BinaryStdOut.close();
    }

    public static TST<Integer> getFirstCodebook() {
        int i = 0;
        TST<Integer> table = new TST();
        while (i < R) {
            table.put("" + (char) i, i);
            i++;
        }
        return table;
    }

    public static ArrayList<String> getFirstCodebookList() {
        ArrayList<String> table = new ArrayList();
        int i = 0;
        while (i < R) {
            table.add("" + (char) i);
            i++;
        }
        table.add("");	// (unused) lookahead for EOF
        return table;
    }

    public static boolean resizeCodeWidth(int newWidth) {
        if (newWidth <= MAX_CODE_LENGTH) {
            W = newWidth;
            L = (int) Math.pow(2, W); // recalculate L=2^W
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        switch (args[0]) {
            case "-":
                String modeChoice = args[1].toLowerCase();
                switch (modeChoice) {
                    case "n": // Do nothing mode
                        mode = 0;
                        break;
                    case "r": // Reset mode
                        mode = 1; 
                        break;
                    case "m": // Monitor mode
                        mode = 2; 
                        break;
                }
                compress();
                break;
            case "+":
                expand();
                break;
            default:
                throw new IllegalArgumentException("Illegal command line argument");
        }
    }

}
