package org.broadinstitute.sting.alignment.bwa.bwt;

import org.broadinstitute.sting.utils.StingException;
import org.broadinstitute.sting.alignment.bwa.packing.IntPackedInputStream;
import org.broadinstitute.sting.alignment.bwa.packing.PackUtils;

import java.io.*;
import java.nio.ByteOrder;

/**
 * A reader for suffix arrays in permanent storage.
 *
 * @author mhanna
 * @version 0.1
 */
public class SuffixArrayReader {
    /**
     * Input stream from which to read suffix array data.
     */
    private InputStream inputStream;

    /**
     * BWT to use to fill in missing data.
     */
    private BWT bwt;

    /**
     * Create a new suffix array reader.
     * @param inputFile File in which the suffix array is stored.
     * @param bwt BWT to use when filling in missing data.
     */
    public SuffixArrayReader(File inputFile, BWT bwt) {
        try {
            this.inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            this.bwt = bwt;
        }
        catch( FileNotFoundException ex ) {
            throw new StingException("Unable to open input file", ex);
        }
    }

    /**
     * Read a suffix array from the input stream.
     * @return The suffix array stored in the input stream.
     */
    public SuffixArray read() {
        IntPackedInputStream intPackedInputStream = new IntPackedInputStream(inputStream, ByteOrder.LITTLE_ENDIAN);

        int inverseSA0;
        int[] occurrences;
        int[] suffixArray;
        int suffixArrayInterval;

        try {
            inverseSA0 = intPackedInputStream.read();
            occurrences = new int[PackUtils.ALPHABET_SIZE];
            intPackedInputStream.read(occurrences);
            // Throw away the suffix array size in bytes and use the occurrences table directly.
            suffixArrayInterval = intPackedInputStream.read();
            suffixArray = new int[occurrences[occurrences.length-1]+1/suffixArrayInterval];
            intPackedInputStream.read(suffixArray);
        }
        catch( IOException ex ) {
            throw new StingException("Unable to read BWT from input stream.", ex);
        }

        return new SuffixArray(inverseSA0, new Counts(occurrences,true), suffixArray, suffixArrayInterval, bwt);
    }


    /**
     * Close the input stream.
     */
    public void close() {
        try {
            inputStream.close();
        }
        catch( IOException ex ) {
            throw new StingException("Unable to close input file", ex);
        }
    }    
}
