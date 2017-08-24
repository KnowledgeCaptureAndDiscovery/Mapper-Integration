package edu.isi.wings.opmm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
public static String MD5(String text) throws NoSuchAlgorithmException
{
	MessageDigest md=MessageDigest.getInstance("MD5");
	md.update(text.getBytes());
	byte b[]=md.digest();
	StringBuffer sb=new StringBuffer();
	for(byte b1:b)
	{
		sb.append(Integer.toHexString(b1 & 0xff).toString());
	}
	return sb.toString();
	
}

}
