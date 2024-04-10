/*
 * This class generates 16 roundKey for encrypting and decryption
*/
package com.hse.bse163.shakin;


public class KeyGenerator {

    private final int[] leftKey = new int[28];
    private final int[] rightKey = new int[28];
    private final int[][] allRoundKey = new int[16][48];

    public KeyGenerator(String keyWord) {
        int[] key64 = getEncryptedKeyword(keyWord);
        int[] key56 = getPermutedBy_PC1(key64);
        doKeySegmentation(key56);

        for (int round = 1; round <= 16; round++) {
            allRoundKey[round - 1] = getRoundKey(round);
        }
    }
    
    public int[] getRoundKeyForEncryption(int roundNumber) {
        return allRoundKey[roundNumber - 1];
    }

    public int[] getRoundKeyForDecryption(int roundNumber) {
        return allRoundKey[16 - roundNumber];
    }

    public int[] getRoundKey(int roundNumber) {
        int[] roundKey;
        doLeftShift(roundNumber);
        roundKey = getPermutedBy_PC2(combineLeftRight());

        return roundKey;
    }

    //this method converts string key to binary block
    public int[] getEncryptedKeyword(String keyWord) {
        byte[][] block = new byte[8][8];
        int[] encryptKey = new int[64];

        for (int i = 0; i < 8 && i < keyWord.length(); i++) {
            block[i] = getBinaryBits(keyWord.charAt(i));
        }

        int index = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                encryptKey[index] = block[i][j];
                index++;
            }
        }
        return encryptKey;
    }

    
    //this method converts a character to 8 byte array
    public byte[] getBinaryBits(int ch) {
        byte[] bin = new byte[8];
        for (int i = 0; i < 8; i++) {
            bin[7 - i] = (byte) ((ch >> i) & 1);
        }
        return bin;
    }

    
    
    //this method divide 56 bit key to two 28 bit left and right key
    public void doKeySegmentation(int[] key56) {
        int index = 0;
        System.arraycopy(key56, 0, leftKey, 0, 28);

        for (int i = 28; i < 56; i++) {
            rightKey[index] = key56[i];
            index++;
        }
    }

    
    //this method do all necessary left shift
    public void doLeftShift(int round) {
        int leftShiftNumber = AllData.numOfLeftRotation[(round - 1)];
        if (leftShiftNumber == 1) {
            doOneLeftShift(leftKey, rightKey);

        } else {
            doOneLeftShift(leftKey, rightKey);
            doOneLeftShift(leftKey, rightKey);
        }
    }

    
    //this method performs one left shift operation
    public void doOneLeftShift(int[] side1, int[] side2) {

        int temp = side1[0];
        for (int i = 1; i < side1.length; i++) {
            side1[i - 1] = side1[i];

        }
        side1[side1.length - 1] = temp;
        temp = side2[0];
        for (int i = 1; i < side2.length; i++) {
            side2[i - 1] = side2[i];
        }
        side2[side2.length - 1] = temp;
    }

    
    
    
    //combine 56 bits key
    public int[] combineLeftRight() {

        int[] key56 = new int[56];
        int index = 28;

        System.arraycopy(leftKey, 0, key56, 0, 28);

        for (int i = 0; i < 28; i++) {
            key56[index] = rightKey[i];
            index++;
        }

        return key56;
    }

    
    
    //return 56 bits key
    public int[] getPermutedBy_PC1(int[] key_in) {
        int[] store_num = AllData.getPermutedChoice1Table();
        int[] key_out = new int[56];
        int temp;
        int i = 0;
        int loop = 0;
        int check = 0;

        while (check != 56) {
            temp = store_num[i];
            if (temp == loop) {
                key_out[check] = key_in[loop - 1];
                loop = 0;
                check++;
                i++;
            }
            loop++;
        }
        return key_out;
    }

    
    
    //return 48 bits key
    public int[] getPermutedBy_PC2(int[] key_in) {
        int[] store_num = AllData.getPermutedChoice2Table();
        int[] key_out = new int[48];
        int temp;
        int i = 0;
        int loop = 0;
        int check = 0;

        while (check != 48) {
            temp = store_num[i];
            if (temp == loop) {
                key_out[check] = key_in[loop - 1];
                loop = 0;
                check++;
                i++;
            }
            loop++;
        }
        return key_out;
    }

}
