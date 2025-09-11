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
    public static void loopWithException(){
        int divisor[] = {1, 2, 3, 4, 0};
        for (int cnt = 0; cnt < 10; cnt++){
            try{
                int share = 100 / divisor[cnt];
                System.out.println(share);
            }
            catch(java.lang.ArithmeticException e){
                System.out.println("wrong");
            }
            catch(java.lang.ArrayOutOfBoundsException e){
                System.out.println("wrong wrong");
            }
            finally{
                System.out.println("final");
            }
        }
        System.out.println("Done");
    }
}


