package com.hk.engine.util;

public class Version implements Cloneable
{
	public final int major, minor, revision;
	
	public Version(String version)
	{
		String[] v = version.split("\\.");
		major = Integer.parseInt(v[0]);
		minor = Integer.parseInt(v[1]);
		revision = Integer.parseInt(v[2]);
	}
	
	public int getCode()
	{
		return major * 100 + minor * 10 + revision;
	}
	
	public Version clone()
	{
		return new Version(toString());
	}
	
	public String toString()
	{
		return major + "." + minor + "." + revision;
	}
	
	public boolean equals(Object o)
	{
		return o instanceof Version && ((Version) o).major == major && ((Version) o).minor == minor && ((Version) o).revision == revision;
	}
	
	public int hashCode()
	{
		int hash = 17;
		hash = hash * 19 + major;
		hash = hash * 19 + minor;
		hash = hash * 19 + revision;
		return hash;
	}
}
