import java.util.HashMap;
import java.util.Map;

public class HuffmanTree {
    private int size;
    private int numValues;
    private TreeNode root;

    /**
     * Creates a HuffmanTree from frequency values in an int array
     * freqs must represent a value-frequency pair
     * 
     * @param freqs the array of frequencies. For index i, freqs[i] must be the
     *              frequency of i.
     */
    public HuffmanTree(int[] freqs) {
        PriorityQueue<TreeNode> pQueue = new PriorityQueue<>();
        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] > 0) {
                TreeNode currentNode = new TreeNode(i, freqs[i]);
                pQueue.enqueue(currentNode);
            }
        }
        TreeNode pseudoEOF = new TreeNode(IHuffConstants.PSEUDO_EOF, 1);
        pQueue.enqueue(pseudoEOF);
        if (pQueue.size() < 2) {
            throw new IllegalArgumentException("pQueue must have at least 2 elements");
        }
        numValues = size = pQueue.size();
        while (pQueue.size() > 1) {
            TreeNode firstChild = pQueue.dequeue();
            TreeNode secondChild = pQueue.dequeue();
            TreeNode parent = new TreeNode(firstChild, 0, secondChild);
            size++;
            pQueue.enqueue(parent);
        }
        root = pQueue.dequeue();
    }

    /**
     * Constructor from a String containing the Standard Tree Format representation
     * of a HuffmanTree
     * 
     * @param standardTreeFormat
     */
    public HuffmanTree(String standardTreeFormat) {
        root = addNodes(standardTreeFormat, new int[1]);
    }

    /**
     * Helper for Standard Tree Format constructor
     * Builds a tree from a String containing the Standard Tree Format of a
     * HuffmanTree
     * 
     * @param STF          Standard Tree Format representation of a HuffmanTree
     * @param currentIndex the index of STF currently being analyzed
     * @return the root of the sub-tree created in this iteration of the method
     */
    private TreeNode addNodes(String STF, int[] currentIndex) {
        TreeNode branch;
        char c = STF.charAt(currentIndex[0]);
        if (c == '0') {
            branch = new TreeNode(0, 0);
            currentIndex[0]++;
            branch.setLeft(addNodes(STF, currentIndex));
            branch.setRight(addNodes(STF, currentIndex));
        } else if (c == '1') {
            currentIndex[0]++;
            String valueString = STF.substring(currentIndex[0],
                    currentIndex[0] + IHuffConstants.BITS_PER_WORD + 1);
            int value = Integer.parseInt(valueString, 2);
            branch = new TreeNode(value, 0);
            numValues++;
            currentIndex[0] += 9;
        } else {
            throw new IllegalArgumentException("STF must only contain 0 or 1");
        }
        size++;
        return branch;
    }

    /**
     * Creates a Map that ties every int value to a tree path encoding
     * 
     * @return a Map object relating Integer values on the tree to String
     *         representations of the encoding for that value
     */
    public Map<Integer, String> getValueMappings() {
        Map<Integer, String> result = new HashMap<>();
        addValueMappings(root, result, "");
        return result;
    }

    /**
     * Traverses the tree and adds the path to every leaf (value) in this
     * HuffmanTree
     * 
     * @param currentNode the current TreeNode being traversed
     * @param outputMap   the map to store the results
     * @param currentCode the current path taken
     */
    private void addValueMappings(TreeNode currentNode,
            Map<Integer, String> outputMap, String currentCode) {
        if (currentNode == null) {
            throw new IllegalArgumentException("currentNode must not be null");
        }
        if (currentNode.isLeaf()) {
            outputMap.put(currentNode.getValue(), currentCode.toString());
        } else {
            addValueMappings(currentNode.getLeft(), outputMap, currentCode + '0');
            addValueMappings(currentNode.getRight(), outputMap, currentCode + '1');
        }
    }

    /**
     *
     * @return
     */
    public Map<String, Integer> getCodeMappings() {
        Map<String, Integer> result = new HashMap<>();
        addCodeMappings(root, result, "");
        return result;
    }

    private void addCodeMappings(TreeNode currentNode, Map<String, Integer> outputMap,
            String currentCode) {
        if (currentNode == null) {
            throw new IllegalArgumentException("currentNode must not be null");
        }
        if (currentNode.isLeaf()) {
            outputMap.put(currentCode.toString(), currentNode.getValue());
        } else {
            addCodeMappings(currentNode.getLeft(), outputMap, currentCode + '0');
            addCodeMappings(currentNode.getRight(), outputMap, currentCode + '1');
        }
    }

    /**
     * Returns a binary representation of this HuffmanTree using Standard Tree
     * Format
     * 
     * @return a String with the Standard Tree Format representation
     */
    public String getBitRepresentation() {
        StringBuilder sb = new StringBuilder();
        getBits(root, sb);
        return sb.toString();
    }

    /**
     * Helper for getBitRepresentation
     * Traverses the tree and builds a string form the tree based off the Standard
     * Tree Format
     * 
     * @param currentNode current TreeNode being traversed
     * @param sb          StringBuilder object that will contain the bit
     *                    representation
     */
    private void getBits(TreeNode currentNode, StringBuilder sb) {
        if (currentNode.isLeaf()) {
            String valueBinary = Integer.toBinaryString(currentNode.getValue());
            while (valueBinary.length() < IHuffConstants.BITS_PER_WORD + 1) {
                valueBinary = '0' + valueBinary;
            }
            sb.append('1' + valueBinary);
        } else {
            sb.append('0');
            getBits(currentNode.getLeft(), sb);
            getBits(currentNode.getRight(), sb);
        }
    }

    /**
     * Get the size of this tree
     * 
     * @return size of this HuffmanTree
     */
    public int size() {
        return size;
    }

    /**
     * Get the number of values or leaf nodes in this HuffmanTree
     * 
     * @return the number of values
     */
    public int getNumValues() {
        return numValues;
    }
}