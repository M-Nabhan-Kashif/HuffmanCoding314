/*  Student information for assignment:
 *
 *  On <MY|OUR> honor, <NAME1> and <NAME2), this programming assignment is <MY|OUR> own work
 *  and <I|WE> have not provided this code to any other student.
 *
 *  Number of slip days used:
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID:
 *  email address:
 *  Grader name:
 *
 *  Student 2
 *  UTEID:
 *  email address:
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class SimpleHuffProcessor implements IHuffProcessor {
    private IHuffViewer myViewer;
    private HuffmanTree frequencyTree;
    private boolean preprocessed;
    private int[] freqs;
    private int headerFormat;
    private int savedBits;
    // TODO: maybe make the codesMap an instance variable

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * 
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind
     *                     of
     *                     header to use, standard count format, standard tree
     *                     format, or
     *                     possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     *         Note, to determine the number of
     *         bits saved, the number of bits written includes
     *         ALL bits that will be written including the
     *         magic number, the header format number, the header to
     *         reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        this.headerFormat = headerFormat;
        freqs = new int[256];
        int fileSize = getFrequencies(in);
        frequencyTree = new HuffmanTree(freqs);
        preprocessed = true;
        int compressedSize = BITS_PER_INT * 2; // 2 ints to indicate huffman encoding
        compressedSize += getHeaderSize(headerFormat);
        Map<Integer, String> valuesMap = frequencyTree.getValueMappings();
        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] != 0) {
                compressedSize += freqs[i] * valuesMap.get(i).length();
            }
        }
        compressedSize += valuesMap.get(PSEUDO_EOF).length();
        savedBits = fileSize - compressedSize;
        return savedBits;
    }

    /**
     * Calculates the size of the header of the compressed file
     * 
     * @param headerFormat the format of the file to compress
     * @return the bits of the header
     */
    private int getHeaderSize(int headerFormat) {
        int headerSize = 0;
        if (headerFormat == STORE_COUNTS) {
            headerSize += ALPH_SIZE * BITS_PER_INT;
        } else if (headerFormat == STORE_TREE) {
            headerSize += frequencyTree.size() +
                    frequencyTree.getNumValues() * (BITS_PER_WORD + 1)
                    + BITS_PER_INT;
        }
        return headerSize;
    }

    /**
     * Get the frequencies from every 8-bit word in an InputStream
     * 
     * @param in the InputStream to get the frequencies from
     * @return an array of frequencies.
     *         freqs[i] is the frequency of i
     * @throws IOException
     */
    private int getFrequencies(InputStream in) throws IOException {
        BitInputStream bitIn = new BitInputStream(in);
        // Each unique word is represented as an index in the array
        int bits = bitIn.readBits(BITS_PER_WORD);
        int fileBits = 0;
        while (bits > -1) {
            freqs[bits]++;
            fileBits += BITS_PER_WORD;
            bits = bitIn.readBits(BITS_PER_WORD);
        }
        bitIn.close();
        return fileBits;
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br>
     * pre: <code>preprocessCompress</code> must be called before this method
     * 
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than
     *              the input file.
     *              If this is false do not create the output file if it is larger
     *              than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        if (!preprocessed) {
            throw new IllegalStateException("preprocessCompress() must be " +
                    "called before calling compress()");
        }
        preprocessed = false;
        int bitsWritten = 0;
        if (savedBits > 0 || force) {
            BitInputStream input = new BitInputStream(in);
            BitOutputStream output = new BitOutputStream(out);
            // write the two sentinel values to indicate file type and compression format
            output.writeBits(BITS_PER_INT, MAGIC_NUMBER);
            output.writeBits(BITS_PER_INT, headerFormat);
            bitsWritten += writeHeader(output);
            bitsWritten += compressBody(input, output);
            input.close();
            output.close();
        } else {
            myViewer.showError("Compressed file has " + (savedBits * -1) +
                    " more bits than uncompressed file.\n" +
                    "Select \"force compression\" option to compress.");
        }
        return bitsWritten;
    }

    /**
     * Writes the header of the compressed file depending on the
     * headerFormat indicated in preprocessCompress
     * 
     * @param output the BitOutputStream to write to
     * @return the number of bits written on this header
     */
    private int writeHeader(BitOutputStream output) {
        int headerSize = 0;
        if (headerFormat == STORE_COUNTS) {
            for (int frequency : freqs) {
                headerSize += BITS_PER_INT;
                output.writeBits(BITS_PER_INT, frequency);
            }
        } else if (headerFormat == STORE_TREE) {
            headerSize += BITS_PER_INT;
            String bitTree = frequencyTree.getBitRepresentation();
            output.writeBits(BITS_PER_INT, bitTree.length());
            headerSize += writeBitsFromString(bitTree, output);
        }
        return headerSize;
    }

    /**
     * Writes the body of the compressed file using the HuffmanTree created in
     * preprocessCompress
     * 
     * @param input  the BitInputStream from the file being compressed
     * @param output the BitOutputStream being written to
     * @return the number of bits written on this body
     * @throws IOException
     */
    private int compressBody(BitInputStream input, BitOutputStream output) throws IOException {
        int bitsWritten = 0;
        Map<Integer, String> valuesMap = frequencyTree.getValueMappings();
        int nextBits = input.readBits(BITS_PER_WORD);
        while (nextBits > -1) {
            bitsWritten += writeBitsFromString(valuesMap.get(nextBits), output);
            nextBits = input.readBits(BITS_PER_WORD);
        }
        bitsWritten += writeBitsFromString(valuesMap.get(PSEUDO_EOF), output);
        return bitsWritten;
    }

    /**
     * Write bits into a BitOutputStream from a String and returns how many bits it
     * wrote
     * 
     * @param bits String representation of bits. Must only contain '0' or '1' chars
     * @param out  BitOutputStream where the bits are written
     * @return amount of bits that were written
     */
    private int writeBitsFromString(String bits, BitOutputStream out) {
        for (int i = 0; i < bits.length(); i++) {
            char c = bits.charAt(i);
            int toWrite = 0;
            if (c == '1') {
                toWrite = 1;
            }
            out.writeBits(1, toWrite);
        }
        return bits.length();
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * 
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        int bitCount = 0;
        BitInputStream input = new BitInputStream(in);
        int first = input.readBits(BITS_PER_INT);
        if (first != MAGIC_NUMBER) {
            input.close();
            throw new IOException("Required magic number not present.");
        }
        BitOutputStream output = new BitOutputStream(out);
        int format = input.readBits(BITS_PER_INT);
        // Recreate the tree
        if (format == STORE_COUNTS) {
            freqs = new int[ALPH_SIZE];
            for (int i = 0; i < ALPH_SIZE; i++) {
                freqs[i] = input.readBits(BITS_PER_INT);
            }
            frequencyTree = new HuffmanTree(freqs);
        } else if (format == STORE_TREE) {
            int treeBitSize = input.readBits(BITS_PER_INT);
            StringBuilder bitString = new StringBuilder();
            for (int i = 0; i < treeBitSize; i++) {
                int nextBit = input.readBits(1);
                if (nextBit == 0) {
                    bitString.append('0');
                } else if (nextBit == 1) {
                    bitString.append('1');
                }
            }
            frequencyTree = new HuffmanTree(bitString.toString());
        } else {
            // TODO: throw an exception if unsuported file type
            input.close();
            output.close();
            throw new IOException("Unsupported header format");
        }
        // Write the body
        // TODO: maybe this is all wrong
        boolean foundPEOF = false;
        Map<String, Integer> codeMappings = frequencyTree.getCodeMappings();
        String currentPath = "";
        while (!foundPEOF) {
            int nextBits = input.readBits(1);
            if (nextBits < 0) {
                // TODO: do this properly
                input.close();
                output.close();
                throw new IOException("No PSEUDO_EOF value.");
            }
            currentPath += nextBits;
            Integer value = codeMappings.get(currentPath);
            if (value != null) {
                if (value == PSEUDO_EOF) {
                    foundPEOF = true;
                } else {
                    bitCount += BITS_PER_WORD;
                    output.writeBits(BITS_PER_WORD, value);
                    currentPath = "";
                }
            }
        }
        input.close();
        output.close();
        return bitCount;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
