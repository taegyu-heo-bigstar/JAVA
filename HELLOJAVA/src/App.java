public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        printCharacter('*', 30);
        printCharacter('-', 30);
    }
    public static void printCharcater(char ch, int num){
        for (int cnt = 0; cnt < num; cnt++){
            System.out.print(ch);
        }
        System.out.println();
    }
}

