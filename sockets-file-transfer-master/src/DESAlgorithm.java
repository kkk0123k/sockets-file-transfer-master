/*
 * This class implements DES Algorithm and perform encryption and decryption
 */
package com.hse.bse163.shakin;

public class DESAlgorithm {

    private final int[] left = new int[32];
    private final int[] right = new int[32];

    private int[] roundKey = new int[48];
    private final int[] SBox_Out = new int[32];
    KeyGenerator key;

    public DESAlgorithm(String key_word) {
        key = new KeyGenerator(key_word);
    }

    //this method encrypt any plaintext
    //mode 1 is for encryption
    public String encrypt(String plain_text) {
        return binToString(encrypt_decrypt(1, plain_text));
    }

    
    
    //this method encrypt any plaintext. mode 1 for encryption 
    public String decrypt(String cipher_text) {
        return binToString(encrypt_decrypt(2, cipher_text));  //2 for decryption
    }

    
    
    //this method encrypt or decrypt 64 bit block
    public int[] encrypt_decrypt(int mode, String text) {

        int[] input_block = getBinaryForTextBlock(text);
        int[] initial_perm_out = getInitialPermuted(input_block);
        doSegmentation(initial_perm_out);

        int round = 1;
        while (round <= 16) {
            performOneRound(mode, round);
            round++;
        }

        swap32();
        int[] final_perm_in = getConcatenated();

        return getFinalPermuted(final_perm_in);
    }

    
    
    //this method converts text block to binary stream
    public static int[] getBinaryForTextBlock(String plain_text) {
        byte[][] block = new byte[8][8];
        int[] binary_text = new int[64];

        for (int i = 0; i < 8 && i < plain_text.length(); i++) {
            block[i] = getBinaryBits(plain_text.charAt(i));
        }

        int index = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                binary_text[index] = block[i][j];
                index++;
            }
        }
        return binary_text;
    }

  
    
    //this method converts a character to 8 byte array
    public static byte[] getBinaryBits(int ch) {
        byte[] bin = new byte[8];
        for (int i = 0; i < 8; i++) {
            bin[7 - i] = (byte) ((ch >> i) & 1);
        }
        return bin;
    }

   
    

    //this method perform each round for encryption and decryption 
    public void performOneRound(int mode, int round) {
        if (mode == 1) {
            roundKey = key.getRoundKeyForEncryption(round);
        } else if (mode == 2) {
            roundKey = key.getRoundKeyForDecryption(round);
        }

        int[] func_out = doDESFunction(right, roundKey);
        int[] result = getXOR_32Bit(left, func_out);

        for (int i = 0; i < 32; i++) {
            left[i] = right[i];
            right[i] = result[i];
        }
    }

   
    
    //this method do initial permutation
    public int[] getInitialPermuted(int[] perm_in) {
        int[] perm_out = new int[64];
        int[] store_num = AllData.getInitialPermutationTable();
        int temp;
        int i = 0;
        int loop = 0;
        int check = 0;
        while (perm_in.length != check) {
            temp = store_num[i];
            if (temp == loop) {
                perm_out[check] = perm_in[loop - 1];
                loop = 0;
                check++;
                i++;
            }
            loop++;
        }
        return perm_out;
    }

    


   //this method divide 64 bit block to two 32 bit block. 
    public void doSegmentation(int[] perm_out) {
        int index = 0;
        System.arraycopy(perm_out, 0, left, 0, 32);

        for (int i = 32; i < 64; i++) {
            right[index] = perm_out[i];
            index++;
        }
    }

    
    
    //this method performs des function
    public int[] doDESFunction(int[] right_in, int[] roundKey) {

        int[] right_out = doExpansion(right_in);
        int[] sbox_in = getXOR_48Bit(right_out, roundKey);
        doSubstitution(sbox_in);

        return doPermutation(SBox_Out);
    }

    
    
    /**
     * this method converts 32 bit right half to 48 bit using expansion table
     *
     * @return 48 bit right half
     */
    public int[] doExpansion(int[] right_in) {
        int[] store_num = AllData.getExpansionTable();
        int[] right_out = new int[48];
        int temp;
        int i = 0;
        int loop = 0;
        int check = 0;

        while (check != 48) {
            temp = store_num[i];
            if (temp == loop) {
                right_out[check] = right_in[loop - 1];
                loop = 0;
                check++;
                i++;
            }
            loop++;
        }
        return right_out;
    }

    
    
    //exclusive or between right_out and key
    public int[] getXOR_48Bit(int[] side1, int[] side2) {
        int index = 0;
        int[] result = new int[48];

        for (int i = 0; i < side1.length; i++) {
            if (side1[i] == side2[i]) {
                result[index] = 0;
            } else {
                result[index] = 1;
            }
            index++;
        }
        return result;
    }

 
    
    /**
     * this method perform substitution. it makes 48 bit XOR_Out to 32 bit
     * SBox_Out
     *
     */
    public void doSubstitution(int[] XOR_Out) {
        int[] temp = new int[6];
        int count = 0;
        int choice = 0;
        int i;
        int index = 0;
        while (count != 48) {
            for (i = 0; i < 6; i++) {
                temp[i] = XOR_Out[i + count];
            }

            int num = getOutputFromSBox(choice, getSBoxRow(temp), getSBoxColumn(temp));
            make32bit(choice, num);

            index++;
            choice++;
            count += 6;
        }

        Reverse(SBox_Out);
    }

   
    

    //this method do another straight permutation to s boxes output.
    public int[] doPermutation(int[] SBox_out) {
        int[] func_out = new int[32];
        int[] store_num = AllData.getStraightPermutationTable();
        int temp;
        int i = 0;
        int loop = 0;
        int check = 0;
        while (check != 32) {
            temp = store_num[i];
            if (temp == loop) {
                func_out[check] = SBox_out[loop - 1];
                loop = 0;
                check++;
                i++;
            }
            loop++;
        }

        return func_out;
    }

    
    
    //this method calculates decimal row number for each s box
    public int getSBoxRow(int[] num) {
        return 2 * num[0] + num[5];
    }

    
    
    
    //this method calculates decimal column number for each s box
    public int getSBoxColumn(int[] num) {
        return 8 * num[1] + 4 * num[2] + 2 * num[3] + num[4];
    }

    
    
    
    //this method gives the decimal output from each s boxes
    public int getOutputFromSBox(int choice, int row, int col) {
        return switch (choice) {
            case 0 -> AllData.SBOX1[row][col];
            case 1 -> AllData.SBOX2[row][col];
            case 2 -> AllData.SBOX3[row][col];
            case 3 -> AllData.SBOX4[row][col];
            case 4 -> AllData.SBOX5[row][col];
            case 5 -> AllData.SBOX6[row][col];
            case 6 -> AllData.SBOX7[row][col];
            case 7 -> AllData.SBOX8[row][col];
            default -> 0;
        };
    }

    
    
    
    //this method combine all s boxes output to make 32 bit
    public void make32bit(int index, int num) {
        int num1, num2, num3;
        num1 = num;

        for (int i = 0; i < 4; i++) {
            num2 = num1 % 2;
            num3 = num1 / 2;
            num1 = num3;
            SBox_Out[(index * 4) + i] = num2;
        }
    }

    
    
    
    //this method reverse the bit sequence 
    public void Reverse(int[] num) {
        int count = 0;
        int fix = 3;
        int temp1, temp2;
        while (count != 32) {
            for (int i = 0; i < 2; i++) {
                temp1 = num[count + i];
                num[count + i] = num[fix - (count + i)];
                num[fix - (count + i)] = temp1;
            }
            fix += 8;
            count += 4;
        }
    }

    
    
    
    //this method XOR left half 32 bit to function_output 32 bit 
    public int[] getXOR_32Bit(int side1[], int[] side2) {
        int index = 0;
        int[] result = new int[32];

        for (int i = 0; i < side1.length; i++) {
            if (side1[i] == side2[i]) {
                result[index] = 0;
            } else {
                result[index] = 1;
            }
            index++;
        }
        return result;
    }

    
    
    
    //this method swap right and left half for each festal round
    public void swap32() {
        int temp;
        for (int i = 0; i < 32; i++) {
            temp = left[i];
            left[i] = right[i];
            right[i] = temp;
        }
    }

    
    
    
    //this method finally concat left and right half
    public int[] getConcatenated() {
        int index = 32;
        int[] result = new int[64];
        System.arraycopy(left, 0, result, 0, 32);

        for (int i = 0; i < 32; i++) {
            result[index] = right[i];
            index++;
        }
        return result;
    }

    
    
    
    //this method do final permutation 
    public int[] getFinalPermuted(int[] perm_in) {
        int[] perm_out = new int[64];
        int[] store_num = AllData.getFinalPermutationTable();
        int temp;
        int i = 0;
        int loop = 0;
        int check = 0;

        while (perm_in.length != check) {
            temp = store_num[i];
            if (temp == loop) {
                perm_out[check] = perm_in[loop - 1];
                loop = 0;
                check++;
                i++;
            }
            loop++;
        }
        return perm_out;
    }

    
    
    //this method converts binary to array to String
    public static String binToString(int[] array) {
        StringBuffer sb = new StringBuffer();
        StringBuilder output = new StringBuilder();

        byte[] byteArray = new byte[4];
        int value, index = 0;

        for (int j = 0; j < array.length; j = j + 4) {
            for (int i = 0; i <= 3; i++) {
                byteArray[i] = (byte) array[index + i];
            }
            index = index + 4;

            int decimal = byteArray[0] * 8 + byteArray[1] * 4 + byteArray[2] * 2 + byteArray[3];
            sb.append(Integer.toString(decimal, 16));
        }

        String hex = new String(sb);

        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return new String(output);
    }

}
