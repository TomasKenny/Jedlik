package org.apache.pdfbox.pdfwriter;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObjectKey;

/**
 * this is en entry in the xref section of the physical pdf document
 * generated by the COSWriter.
 *
 * @author Michael Traut
 */
public class COSWriterXRefEntry implements Comparable<COSWriterXRefEntry>
{
    private long offset;
    private COSBase object;
    private COSObjectKey key;
    private boolean free = false;
    private static final COSWriterXRefEntry NULLENTRY;

    static
    {
    	NULLENTRY = new COSWriterXRefEntry(0, null, new COSObjectKey(0, 65535));
    	NULLENTRY.setFree(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(COSWriterXRefEntry obj)
    {
    	if (obj != null)
    	{
    		if (getKey().getNumber() < obj.getKey().getNumber())
    		{
    			return -1;
    		}
    		else if (getKey().getNumber() > obj.getKey().getNumber())
    		{
    			return 1;
    		}
    		return 0;
    	}
    	return -1;
    }

    /**
     * This will return a null entry: 0000000000 65535 f.
     * 
     * @return null COSWriterXRefEntry
     */
    public static COSWriterXRefEntry getNullEntry()
    {
      return NULLENTRY;
    }
    
    /**
     * This will get the Object key.
     *
     * @return The object key.
     */
    public COSObjectKey  getKey()
    {
        return key;
    }

    /**
     * This will get the offset into the document.
     *
     * @return The offset into the document.
     */
    public long getOffset()
    {
        return offset;
    }

    /**
     * Gets the xref 'free' attribute.
     *
     * @return The free attribute.
     */
    public boolean isFree()
    {
        return free;
    }

    /**
     * This will set the free attribute.
     *
     * @param newFree The newly freed attribute.
     */
    public void setFree(boolean newFree)
    {
        free = newFree;
    }

    /**
     * This will set the object key.
     *
     * @param newKey The new object key.
     */
    private void setKey(COSObjectKey  newKey)
    {
        key = newKey;
    }

    /**
     * The offset attribute.
     *
     * @param newOffset The new value for the offset.
     */
    public final void setOffset(long newOffset)
    {
        offset = newOffset;
    }

    /**
     * COSWriterXRefEntry constructor comment.
     *
     * @param start The start attribute.
     * @param obj The COS object that this entry represents.
     * @param keyValue The key to the COS object.
     */
    public COSWriterXRefEntry(long start, COSBase obj, COSObjectKey keyValue)
    {
        super();
        setOffset(start);
        setObject(obj);
        setKey(keyValue);
    }

    /**
     * This will get the object.
     *
     * @return The object.
     */
    public COSBase getObject()
    {
        return object;
    }

    /**
     * This will set the object for this xref.
     *
     * @param newObject The object that is being set.
     */
    private void setObject(COSBase newObject)
    {
        object = newObject;
    }
}
