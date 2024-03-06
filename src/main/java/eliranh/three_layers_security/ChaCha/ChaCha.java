package eliranh.three_layers_security.ChaCha;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ChaCha 
{
    public ChaCha()
    {

    }

    public byte[] decrypt_text(int[] key, int[] nonce, byte[] ciphertext)// same process as encryption.
   {
    int counter = 0;
    byte[] decryptedText = new byte[ciphertext.length];
    for (int i = 0; i < ciphertext.length; i += 64) {
      ChaChaState chaChaState = new ChaChaState(key, nonce, counter);
      int[] cipherState = chaChaState.state;
      for (int i2 = 0; i2 < 10; i2++) {
        cipherState = doubleRound(cipherState);
      }
      for (int i3 = 0; i3 < cipherState.length; i3++) {
        cipherState[i3] += chaChaState.state[i3];
      }

      byte[] keyStream = serializeState(cipherState);
      int blockSize = Math.min(64, ciphertext.length - i);

      for (int j = 0; j < blockSize; j++) {
        decryptedText[i + j] = (byte) (ciphertext[i + j] ^ keyStream[j]);
      }
      counter++;
    }
    return decryptedText;
   }
    public byte[] encrypt_text(byte[] plaintext, int[] key, int[] nonce) 
    {
        int counter = 0;// counter initial with zero
        byte[] encryptedText = new byte[plaintext.length];// initialization of encryptedText in plainText size
        for (int i = 0; i < plaintext.length; i += 64)// loop that runs over all the length of plainText for full data
                                                      // encryption
        {
          ChaChaState chaChaState = new ChaChaState(key, nonce, counter);// generate original state
          int[] cipherState = chaChaState.state;// copying the state for the doubleRound actions
          for (int i2 = 0; i2 < 10; i2++)// 20 rounds of quarterRound
          {
            cipherState = doubleRound(cipherState);
          }
          for (int i3 = 0; i3 < cipherState.length; i3++)// addition of the original state
          {
            cipherState[i3] += chaChaState.state[i3];
          }

          byte[] keyStream = serializeState(cipherState);// generation of the key stream
          int blockSize = Math.min(64, plaintext.length - i);// determination of the block size that will be XORed

          for (int j = 0; j < blockSize; j++)// XOR each plain text's byte with the key stream
          {
            encryptedText[i + j] = (byte) (plaintext[i + j] ^ keyStream[j]);
          }
          counter++;// counter increment
        }
        return encryptedText;
    }
    private byte[] serializeState(int[] state)// convertion of the state to byte[] key stream
  {
    ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
    for (int i : state) {
      buffer.putInt(i);
    }
    return buffer.array();
  }
  private int[] rowRound(int[] state)// runs the quarter round method order by the rows of the state
  {
    int[] cipher = state;
    cipher = quarterRound(cipher, 0, 1, 2, 3);
    cipher = quarterRound(cipher, 4, 5, 6, 7);
    cipher = quarterRound(cipher, 8, 9, 10, 11);
    cipher = quarterRound(cipher, 12, 13, 14, 15);
    return cipher;
  }

  private int[] columnRound(int[] state)// runs the quarter round method order by the columns of the state
  {
    int[] cipher = state;
    cipher = quarterRound(cipher, 0, 4, 8, 12);
    cipher = quarterRound(cipher, 1, 5, 9, 13);
    cipher = quarterRound(cipher, 2, 6, 10, 14);
    cipher = quarterRound(cipher, 3, 7, 11, 15);
    return cipher;
  }

  private int[] doubleRound(int[] state)// runs rowRound and columnRound methods
  {
    return rowRound(columnRound(state));
  }

  private int[] quarterRound(int[] data, int a, int b, int c, int d)// the core function of the algorithm
  {
    int t = 0;
    t = data[b] ^ (data[a] << 7 | data[a] >>> (32 - 7));
    data[b] = data[a];
    data[a] = t;// bitwise shift left, shift right, XOR, AND.
    t = data[c] ^ (data[b] << 9 | data[b] >>> (32 - 9));
    data[c] = data[b];
    data[b] = t;
    t = data[d] ^ (data[c] << 13 | data[c] >>> (32 - 13));
    data[d] = data[c];
    data[c] = t;
    t = data[a] ^ (data[d] << 18 | data[d] >>> (32 - 18));
    data[a] = data[d];
    data[d] = t;
    return data;
  }
}
