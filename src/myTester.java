public class myTester {
    public static void main (String[] args) {
        PriorityQueue<Integer> test = new PriorityQueue<>();
        test.enqueue(7);
        test.enqueue(3);
        test.enqueue(-10);
        test.enqueue(7);
        test.enqueue(20);
        while (!test.isEmpty()) {
            System.out.println(test.dequeue());
        }

    }
}
