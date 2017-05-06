package helpers;

public class KeyValuePair<T1, T2> {
	public T1 key;
	public T2 value;
	
	public KeyValuePair(T1 key, T2 value)
	{
		this.key = key;
		this.value = value;
	}
}
