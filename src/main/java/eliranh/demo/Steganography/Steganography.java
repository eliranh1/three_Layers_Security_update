package eliranh.demo.Steganography;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;


public class Steganography 
{
  public static boolean DEBUG = false; 
  public Steganography()
  {

  } 
  public byte[] encrypt(InputStream inputStream, byte[] content,String fileType) throws IOException, UnsupportedAudioFileException
  {
       if(fileType.equals("jpeg")) 
       return hideInJpeg(inputStream, content);
       else return hideInWav(inputStream, content);
  }

  public byte[] decrypt(byte[] fileData, String fileType, int length) throws IOException
  {
    if(fileType.equals("jpeg"))
    return extractFromJpeg(fileData);

    return extractFromWav(fileData, length);
  }

  private byte[] extractFromJpeg(byte[] data) throws IOException
   {
     InputStream inputStream = new ByteArrayInputStream(data);
     int marker,markerId;
     while((marker = inputStream.read())!=-1)
     {
      if(marker == 0xFF)
      {
        markerId = inputStream.read();
        if(markerId == 0xFE)
        {
         //Notification.show("FE found");
         int length = ((inputStream.read()<<8)| inputStream.read());
         byte[] hiddenData = new byte[length - 2];
         inputStream.read(hiddenData);   
         return hiddenData;
        }
      }
     }
     return null;
   } 
  private byte[] hideInJpeg(InputStream input,byte[] content) throws IOException 
  {
    // tranfer all image bytes to output stream for multiple use
    ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
    dataStream.write(input.readAllBytes());

    // input stream for search on bytes
    InputStream fullInput = new ByteArrayInputStream(dataStream.toByteArray());
    int marker, markerId, counter = 0;

    while ((marker = fullInput.read()) != -1) 
    {
      counter++;
      if (marker == 0xFF) {
        markerId = fullInput.read();
        counter++;
        if (markerId == 0xFE) // check if com segment exist
        {
          int length = (fullInput.read() << 8) | fullInput.read(); // the length of the com segment
          byte[] firstData = new byte[counter]; // the data of the file before the com segment (includes the markers).

          // reading to the firstData byte array only the first data
          InputStream firstInputStream = new ByteArrayInputStream(dataStream.toByteArray());
          firstInputStream.read(firstData);

          fullInput.skip(length - 2); // skipping the data that in the com segment
          byte[] secondData = fullInput.readAllBytes(); // reading the data that after the com segment

          length = content.length + 2;

          ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream(); // new output stream for the new builded
                                                                             // file
          outputStream2.writeBytes(firstData); // ordered write of the data
          outputStream2.write((length >> 8) & 0xFF);
          outputStream2.write(length & 0xFF);
          outputStream2.writeBytes(content);
          outputStream2.writeBytes(secondData);

          byte[] fullData = outputStream2.toByteArray(); // fully builded and injected byte array

          // String str = new String(fullData,StandardCharsets.US_ASCII); // debug
          // Notification.show(str);
          return fullData;
          // break;
        }
      }
    }
    if (marker == -1) // in case com segment not exist in the file
    {
      counter = 0;

      ByteArrayOutputStream comSegment = new ByteArrayOutputStream();// creating com segment with the text content
      comSegment.write(0xFF);                                          
      comSegment.write(0xFE);                                            
      int length = content.length + 2;                                   
      comSegment.write((length >> 8) & 0xFF);                            
      comSegment.write(length & 0xFF);                                  
      comSegment.writeBytes(content);

      fullInput = new ByteArrayInputStream(dataStream.toByteArray());
      while ((marker = fullInput.read()) != -1)// this loop parsing file and inject the com segment that created
      {
        counter++;
        if (marker == 0xFF) 
        {
          markerId = fullInput.read();
          counter++;
          if (markerId == 0xDB) //check if quantization table is reached
          {
            byte[] firstData = new byte[counter - 2];
            fullInput = new ByteArrayInputStream(dataStream.toByteArray());
            fullInput.read(firstData);

            byte[] secondData = fullInput.readAllBytes();

            ByteArrayOutputStream newFormatStream = new ByteArrayOutputStream();
            newFormatStream.write(firstData);
            newFormatStream.writeBytes(comSegment.toByteArray());
            newFormatStream.write(secondData);

            byte[] fullData = new byte[firstData.length + comSegment.size() + secondData.length];
            fullData = newFormatStream.toByteArray();

            return fullData;
          }
        }
      }
    }
    return null;
  }
  private byte[] hideInWav(InputStream inputStream, byte[] content)throws UnsupportedAudioFileException, IOException 
  {
        byte[] wavBytes = inputStream.readAllBytes();    
        int index = 44;
        for (int i = 0; i < content.length; i++) {
            for (int bit = 0; bit < 8; bit++) {

                int contentBit = (content[i] >> bit) & 1;
  
                wavBytes[index] = (byte) (wavBytes[index] & 0xFE);
                wavBytes[index] = (byte) (wavBytes[index] | contentBit);

                index++;
            }
        }
    System.out.println("stego hide in wave completed!");
    return wavBytes;
  }
  public byte[] extractFromWav(byte[] wavBytes, int contentLength) 
  {
    byte[] content = new byte[contentLength];

    // Start extracting the content after the WAV header (first 44 bytes)
    int index = 44;
    for (int i = 0; i < content.length; i++) {
        for (int bit = 0; bit < 8; bit++) {
            // Extract the LSB of the wav byte
            int wavBit = wavBytes[index] & 1;

            // Clear the bit in the content byte
            //content[i] = (byte) (content[i] & ~(1 << bit));

            // Insert the wav bit into the content byte
            content[i] = (byte) (content[i] | (wavBit << bit));

            index++;
        }
    }

    System.out.println("extraction from wave completed!");
    return content;
}

}
