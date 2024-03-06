package eliranh.three_layers_security.Classes;

public class Key 
{
    private int[] chachaKey;
    private int[] chachaNonce;
    private byte[] aesKey;
    private byte[] aesIv;

    public Key(int[] chachaKey, int[] chachaNonce, byte[] aesKey, byte[] aesIv) 
    {
        this.chachaKey = chachaKey;
        this.chachaNonce = chachaNonce;
        this.aesKey = aesKey;
        this.aesIv = aesIv;
    }

    public int[] getChachaKey() {
        return chachaKey;
    }

    public int[] getChachaNonce() {
        return chachaNonce;
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    public byte[] getAesIv() {
        return aesIv;
    }
}
