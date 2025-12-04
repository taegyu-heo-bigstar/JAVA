public class test {
    public static void main(String[] args) {
        testClass original = new testClass(10, 20);
        testClass copy = original.clone();
        
        System.out.println("Original a: " + original.a + ", b: " + original.b);
        System.out.println("Copy a: " + copy.a + ", b: " + copy.b);
        System.out.println();

        original.a = 30;
        original.b = 40; // This line will cause a compilation error since b is final
        System.out.println("After modifying original:");
        System.out.println("Original a: " + original.a + ", b: " + original.b);
        System.out.println("Copy a: " + copy.a + ", b: " + copy.b);
    }
}

class testClass implements Cloneable {
    int a;
    final int b;
    
    public testClass(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public testClass clone() {
        try {
            return (testClass) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
