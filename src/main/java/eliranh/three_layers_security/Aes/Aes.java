package eliranh.three_layers_security.Aes;

import java.util.Arrays;

public class Aes 
{
  private static AesTables tables; 

  public Aes()
  {
    this.tables = new AesTables();
  }

  public byte[] encrypt(byte[] plainText, byte[] key, byte[] iv) 
  {  
    byte[][] expandedKey = expandKey(key);
    byte[] previousBlock = iv;

    int paddingLength = 16 - (plainText.length % 16);
    plainText = Arrays.copyOf(plainText, plainText.length + paddingLength);
    for (int i = 0; i < paddingLength; i++) 
    {
        plainText[plainText.length - 1 - i] = (byte) paddingLength;
    }    

    int numBlocks = plainText.length / 16;
    byte[] cipherText = new byte[plainText.length];

//    Notification.show("number of blocks "+numBlocks);
    for (int b = 0; b < numBlocks; b++) 
    {
      byte[] block = Arrays.copyOfRange(plainText, b * 16, (b + 1) * 16);

      // XOR the block with the previous ciphertext block (or IV for the first block)
      for (int i = 0; i < 16; i++) 
      {
        block[i] ^= previousBlock[i];
      }

      byte[][] state = generateState(block);

      // System.out.println("plainText after iv added: ");
      // printState(state);
      // Perform 14 rounds of transformations for AES-256
      for (int round = 0; round < 14; round++) 
      {
        state = subBytes(state);
        // System.out.println("subBytes ended");
        // printState(state);
        state = shiftRows(state);
        // System.out.println("shiftRows ended");
        // printState(state);
        if (round != 13) // Skip mixColumns on the last round
        {
          state = mixColumns(state);
          // System.out.println("mixColumns ended");
          // printState(state);
        }
        state = addRoundKey(state, expandedKey, round);
        // System.out.println("addRoundKey ended");
        // printState(state);
        // System.out.println("-------------------round "+(round+1)+" is done!-------------------------");
      }

      // Convert the state back to a byte array and store it in the ciphertext
      byte[] encryptedBlock = convertStateToByteArray(state);
      System.arraycopy(encryptedBlock, 0, cipherText, b * 16, 16);

      // Use the encrypted block as the previous ciphertext block for the next round
      previousBlock = encryptedBlock;
    }
    return cipherText;
  }
  private void printState(byte[][] state)
  {
    for (int i = 0; i < state.length; i++) {
      for (int j = 0; j < state[i].length; j++) {
          System.out.printf("%02X ", state[i][j]);
      }
      System.out.println();
  }
  }
  private byte[] convertStateToByteArray(byte[][] state) 
  {
    byte[] block = new byte[16];
    for (int i = 0; i < 4; i++) 
    {
      for (int j = 0; j < 4; j++) 
      {
        block[i * 4 + j] = (byte) state[j][i];
      }
    }
    return block;
  }

  private byte[][] addRoundKey(byte[][] state, byte[][] expandedKey, int round) 
  {
    int[][] roundKey = new int[4][4];

    // Extract the round key from the expanded key
    for (int i = 0; i < 4; i++) 
    {
      for (int j = 0; j < 4; j++) 
      {
        roundKey[i][j] = expandedKey[round * 4 + i][j];
      }
    }
    // Perform the AddRoundKey operation
    for (int i = 0; i < state.length; i++) 
    {
      for (int j = 0; j < state[i].length; j++) 
      {
        state[i][j] ^= roundKey[i][j];
      }
    }
    return state;
  }

  private byte[][] mixColumns(byte[][] state) 
  {
    AesTables tables = new AesTables();
    byte[][] tempState = new byte[4][4];
    for (int c = 0; c < 4; c++) 
    {
      for (int r = 0; r < 4; r++) 
      {
        tempState[r][c] = 0;
        for (int i = 0; i < 4; i++) 
        {
          int low = state[i][c] & 0x0F;
          int high = state[i][c] & 0xF0;
          int num = low^high;
          tempState[r][c] ^= multiply(tables.mixColumnsMatrix[r][i], num);
        }
      }
    }

    for (int r = 0; r < 4; r++) 
    {
      for (int c = 0; c < 4; c++) 
      {
        state[r][c] = tempState[r][c];
      }
    }
    return state;
  }

  private int multiply(int a, int b) 
  {
    //System.out.println("matrix: "+a);
    //System.out.println("state num: "+b);
    int result = 0;
    while (a != 0 && b != 0) 
    {
      if ((b & 1) != 0) 
      {
        result ^= a;
      }
      boolean highBitSet = (a & 0x80) != 0;
      a <<= 1;
      if (highBitSet) 
      {
        a ^= 0x1b; // This is the primitive polynomial x^8 + x^4 + x^3 + x + 1
      }
      b >>= 1;
    }

    return result & 0xFF;
  }

  private byte[][] shiftRows(byte[][] state) 
  {
    for (int row = 1; row < 4; row++) 
    {
      byte[] tempRow = new byte[4];
      for (int col = 0; col < 4; col++) 
      {
        tempRow[col] = state[row][(col + row) % 4];
      }
      state[row] = tempRow;
    }
    return state;
  }

  private byte[][] subBytes(byte[][] state) 
  {
    AesTables tables = new AesTables();
    for (int row = 0; row < 4; row++) 
    {

      for (int col = 0; col < 4; col++) 
      { 
        int sBoxRow = (state[row][col] & 0xF0) >> 4; // get the higher 4 bits
        int sBoxCol = state[row][col] & 0x0F; // get the lower 4 bits
        state[row][col] = (byte)tables.SBOX[sBoxRow][sBoxCol];
      }
    }
    return state;
  }

  private byte[][] generateState(byte[] plainText) 
  {
    byte[][] state = new byte[4][4];
    for (int i = 0; i < plainText.length; i++) 
    {
      state[i % 4][i / 4] = plainText[i];
    }

    return state;
  }

  private static byte[][] expandKey(byte[] key) 
  {
    byte[][] roundKeys = new byte[60][4]; // 15 round keys for AES-256
    byte[] temp = new byte[4];

    // copy the original key to the round keys array
    for (int i = 0; i < 8; i++) 
    {
      System.arraycopy(key, i * 4, roundKeys[i], 0, 4);
    }

    for (int i = 8; i < 60; i++) 
    {
      System.arraycopy(roundKeys[i - 1], 0, temp, 0, 4);

      //the key schedule core
      if (i % 8 == 0) 
      {
        temp = scheduleCore(temp, i / 8);
      }

      for (int j = 0; j < 4; j++) 
      {
        roundKeys[i][j] = (byte) (roundKeys[i - 8][j] ^ temp[j]);
      }
    }

    return roundKeys;
  }

  private static byte[] scheduleCore(byte[] in, int i) 
  {
    // Rotate
    byte t = in[0];
    System.arraycopy(in, 1, in, 0, 3);
    in[3] = t;

    AesTables tables = new AesTables();
    // SubBytes
    for (int j = 0; j < 4; j++) 
    {
      int high = (in[j] & 0xF0) >> 4;
      int low = in[j] & 0x0F;
      in[j] = (byte) tables.SBOX[high][low];
    }

    // Rcon
    in[0] ^= tables.RCON[i];

    return in;
  }  

  public byte[] decrypt(byte[] cipherText, byte[] key, byte[] iv) 
{
    //System.out.println("************* decryption begins ************");
    
    byte[][] expandedKey = expandKey(key);

    int numBlocks = cipherText.length / 16;
    byte[] plainText = new byte[cipherText.length];
    byte[] previousBlock = iv;

    for (int b = 0; b < numBlocks; b++) 
    {
        byte[] block = Arrays.copyOfRange(cipherText, b * 16, (b + 1) * 16);
        byte[][] state = generateState(block);

        // Perform 14 rounds of transformations for AES-256
        for (int round = 13; round >= 0; round--) 
        {
            state = addRoundKey(state, expandedKey, round);
            //System.out.println("addRoundKey ended");
            //printState(state);
            if (round != 13) // Skip invMixColumns on the first round
            {
                state = invMixColumns(state);
               // System.out.println("mixColumns ended");
               // printState(state);
            }
            state = invShiftRows(state);
           // System.out.println("shiftRows ended");
           // printState(state);
            state = invSubBytes(state);
           // System.out.println("subBytes ended");
           // printState(state);
            //System.out.println("-------------------round "+(round+1)+" is done!-------------------------");
        }

        // Convert the state back to a byte array
        byte[] decryptedBlock = convertStateToByteArray(state);

        // XOR the block with the previous ciphertext block (or IV for the first block)
        for (int i = 0; i < 16; i++) 
        {
            decryptedBlock[i] ^= previousBlock[i];
        }

        System.arraycopy(decryptedBlock, 0, plainText, b * 16, 16);

        // Use the original encrypted block as the previous ciphertext block for the next round
        previousBlock = block;
    }
    int paddingLength = plainText[plainText.length - 1];
    plainText = Arrays.copyOf(plainText, plainText.length - paddingLength);

    return plainText;
}

  private byte[][] invSubBytes(byte[][] state) 
  {
    for (int row = 0; row < state.length; row++) 
    {
        for (int col = 0; col < state[row].length; col++) 
        {
            int sBoxRow = (state[row][col] & 0xF0) >> 4; // get the higher 4 bits
            int sBoxCol = state[row][col] & 0x0F; // get the lower 4 bits
            state[row][col] = (byte)tables.RSBOX[sBoxRow][sBoxCol];
        }
    }
    return state;
  }

  private byte[][] invShiftRows(byte[][] state) 
   {
    for (int row = 1; row < 4; row++) {
        byte[] tempRow = new byte[4];
        for (int col = 0; col < 4; col++) {
            tempRow[col] = state[row][(col + 4 - row) % 4];
        }
        state[row] = tempRow;
    }
    return state;
   }

   private byte[][] invMixColumns(byte[][] state) 
  {
    AesTables tables = new AesTables();
    byte[][] tempState = new byte[4][4];
    for (int c = 0; c < 4; c++) 
    {
      for (int r = 0; r < 4; r++) 
      {
        tempState[r][c] = 0;
        for (int i = 0; i < 4; i++) 
        {
          int low = state[i][c] & 0x0F;
          int high = state[i][c] & 0xF0;
          int num = low^high;
          tempState[r][c] ^= multiply(tables.invMixColumnsMatrix[r][i], num);
        }
      }
    }
    for (int r = 0; r < 4; r++) 
    {
      for (int c = 0; c < 4; c++) 
      {
        state[r][c] = tempState[r][c];
      }
    }
    return state;
  }
}
