package Utils;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.Key;
import java.util.Base64;

/**
 * Classe de Criptage d'une chaine de caracteres
 * @author Grassi Wilfrid
 *
 * @date: 11/01/2024   
 * @Modification : 15/01/2024
 * @version: 1.1
 *  
 */
public class CryptoAES256
{
	private SecretKey mykeyAES256 = null;
	private String mykeyAES256Str = "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4";
/*    public static void main(String[] args) throws Exception 
	{
        String originalText = "Hello, AES-256 Encryption!";
        System.out.println("Original Text: " + originalText);

        // Generate a secret key
        SecretKey secretKey = generateSecretKey();

        // Encrypt the original text
        String encryptedText = encrypt(originalText, secretKey);
        System.out.println("Encrypted Text: " + encryptedText);

        // Decrypt the encrypted text
        String decryptedText = decrypt(encryptedText, secretKey);
        System.out.println("Decrypted Text: " + decryptedText);
    }*/


	/**
	 * 
	 * Constructeur de la classe CryptoAES256
	 * 
	 */
	public CryptoAES256()
	{
		try 
		{
			this.generateSecretKeys();
		} 
		catch (Exception e)
		{
			this.mykeyAES256 = null;
			this.mykeyAES256Str = null;
		}
	}
	
	/**
	 * 
	 * Constructeur de la classe CryptoAES256
	 * 
	 */
	public CryptoAES256(String _key)
	{
		this.mykeyAES256Str = _key;
		this.mykeyAES256 = this.ConvertStringToSecretKey(this.mykeyAES256Str);
	}
	
	/**
	 * 
	 * Methode de generation de la cle AES256
	 * 
	 * @return
	 * @throws Exception
	 */
    private void generateSecretKeys() throws Exception 
	{
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // Use AES-256
        this.mykeyAES256 = keyGenerator.generateKey();
        if(this.mykeyAES256 != null)
        	this.mykeyAES256Str = this.ConvertSecretKeyToString(this.mykeyAES256);
    }
    
    /**
     * 
     * @param __secretKey
     */
    public void setKeys(SecretKey _secretKey)
    {
    	this.mykeyAES256 = _secretKey;
    	this.mykeyAES256Str = this.ConvertSecretKeyToString(this.mykeyAES256);
    }
    
    /**
     * 
     * Lecture de la cle secrete EAS256
     * 
     * @return mykeyAES256
     */
    public SecretKey getKeyAES256()
    {
    	return this.mykeyAES256;
    }
    
    /**
     * 
     * Lecture de la cle secrete EAS256
     * 
     * @return mykeyAES256
     */
    public String getKeyAES256Str()
    {
    	return this.mykeyAES256Str;
    }

    /**
     * 
     * Conversion de la secretkey en String
     * 
     * @param secretKey
     * @return secretKeyStr
     */
    private String ConvertSecretKeyToString(SecretKey secretKey) 
    {
        // Convert the SecretKey to a byte array
        byte[] keyBytes = secretKey.getEncoded();

        // Encode the byte array to a Base64 string
        return Base64.getEncoder().encodeToString(keyBytes);
    }
    
    /**
     * 
     * Conversion de la secretkey en String
     * 
     * @param secretKey
     * @return secretKeyStr
     */
    public SecretKey ConvertStringToSecretKey(String keyString) 
    {
        // Decode the Base64-encoded string to get the byte array
        byte[] keyBytes = Base64.getDecoder().decode(keyString);

        // Create a SecretKey using the byte array
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    /**
     * 
     * Methode d'encryptage d'une chaine de caracteres
     * 
     * @param originalText
     * @param key
     * @return
     * @throws Exception
     */
	public String EncryptAES256(String originalText, Key key) throws Exception
	{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(originalText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
	
    /**
     * 
     * Methode d'encryptage d'un tableau de bytes
     * 
     * @param originalBytes
     * @param key
     * @return
     * @throws Exception
     */
	public byte[] EncryptAES256(byte [] originalBytes, Key key) throws Exception
	{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(originalBytes);
        return Base64.getEncoder().encode(encryptedBytes);
    }

    /**
     * 
     * Methode de decryptage d'une chaine de caractere
     * 
     * @param encryptedText
     * @param key
     * @return
     * @throws Exception
     */
	public String DecryptAES256(String encryptedText) throws Exception
	{
        Cipher cipher = Cipher.getInstance("AES");
        this.mykeyAES256 = ConvertStringToSecretKey(mykeyAES256Str);
        cipher.init(Cipher.DECRYPT_MODE, this.mykeyAES256);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
	
    /**
     * 
     * Methode de decryptage d'un tableau de bytes
     *  
     * @param encryptedBytes
     * @param key
     * @return
     * @throws Exception
     */
	public byte[] DecryptAES256(byte[] encryptedBytes, Key key) throws Exception
	{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedBytes);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return decryptedBytes;
    }
}