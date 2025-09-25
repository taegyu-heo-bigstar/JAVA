public class ExceptionExample {
    public static void main(String[] args) {
        try {
            divide(10, 0);   // 0으로 나눔 → 예외 발생
        } catch (ArithmeticException e) {
            System.out.println("예외 발생: " + e.getMessage());
        }
    }

    // 메서드에서 예외를 던짐
    public static int divide(int a, int b) throws ArithmeticException {
        if (b == 0) {
            throw new ArithmeticException("0으로 나눌 수 없습니다.");
        }
        return a / b;
    }
}